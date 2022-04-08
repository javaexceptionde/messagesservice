package dev.jbull.message_service;

import dev.jbull.core.Core;
import dev.jbull.core.service.CoreService;
import dev.jbull.core.service.Service;
import dev.jbull.database_service.DatabaseService;
import dev.jbull.database_service.mariadb.MariadbSession;
import dev.jbull.database_service.mongodb.MongoDBSession;
import dev.jbull.database_service.redis.RedisSession;
import dev.jbull.message_service.api.MessageServiceAPI;
import dev.jbull.message_service.api.MessageServiceAPIImpl;
import dev.jbull.message_service.commands.MessageCommand;
import dev.jbull.message_service.exceptions.UnknownMessageException;
import dev.jbull.message_service.listener.MessageListener;
import dev.jbull.message_service.messages.Message;
import dev.jbull.message_service.messages.Messages;
import dev.jbull.message_service.messages.provider.MessageProvider;
import dev.jbull.message_service.placeholder.PlaceHolderList;
import dev.jbull.message_service.placeholder.PlaceHolderListImpl;
import dev.jbull.messaging_service.MessagingService;
import dev.jbull.messaging_service.api.MessagingServiceAPI;

import java.sql.Connection;
import java.sql.SQLException;

@CoreService(
        name = "message-service",
        requiredServices = {
                "database-service"
        },
        autoLoad = false
)
public class MessageServiceImpl extends Service implements MessageService {
    private DatabaseService databaseService;
    private MongoDBSession mongoDBSession;
    private MariadbSession mariadbSession;
    private RedisSession redisSession;
    private MessageServiceAPI messageServiceAPI;
    private PlaceHolderList placeHolderList;
    private boolean redisEnabled;
    private boolean mongodbEnabled;
    private boolean mariadbEnabled;
    private dev.jbull.messaging_service.provider.MessageProvider messagingProvider;
    private static MessageServiceImpl instance;

    @Override
    public void onEnable() {
        instance = this;
        databaseService = (DatabaseService) Core.getCore().getCoreBridge().getService(DatabaseService.class);
        if (databaseService.getRedisServiceAPI() == null){
            redisEnabled = false;
        }else {
            redisEnabled = true;
            redisSession = databaseService.getRedisServiceAPI().startSession("MessageSession");
        }
        if (databaseService.getMongoDBServiceAPI() == null){
            mongodbEnabled = false;
        }else {
            mongodbEnabled = true;
            mongoDBSession = databaseService.getMongoDBServiceAPI().startSession("MessageSession");
        }
        if (databaseService.getMariadbServiceAPI() == null){
            mariadbEnabled = false;
        }else {
            mariadbEnabled = true;
            mariadbSession = databaseService.getMariadbServiceAPI().startSession("MessageSession");
            mariadbSession.openConnectionAsync(callBack -> {
                try(Connection connection = callBack){
                    connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `messages` (MESSAGE_KEY TEXT, MESSAGE TEXT, SERVICE TEXT)");
                }catch (SQLException exception){
                    exception.printStackTrace();
                }
            });
        }
        messageServiceAPI = new MessageServiceAPIImpl();
        MessageProvider messageProvider;
        messageServiceAPI.initMessageProvider("messages");
        messageProvider = messageServiceAPI.getMessageProvider("messages");
        placeHolderList = new PlaceHolderListImpl();
        messageProvider.refreshAllMessages();
        for (Messages value : Messages.values()) {
            try {
                Message message = messageProvider.getMessageById(value.toString());
            }catch (UnknownMessageException exception){
                System.out.println("Message " + value.toString() + " not found, creating...");
                messageProvider.insertMessage(value.toString(), value.getDefaultMessage(), value.getLanguage());
            }
        }
        this.getInstance().getServer().getCommandMap().register("message", new MessageCommand());
        messagingProvider = ((MessagingServiceAPI) Core.getCore().getCoreBridge().getService(MessagingService.class)).getMessageProvider();
        if (Core.getCore().isSpigot()){
            ((MessagingServiceAPI) Core.getCore().getCoreBridge().getService(MessagingService.class)).registerChannelMessageListener(new MessageListener());
        }
    }

    @Override
    public void onDisable() {

    }

    public static MessageServiceImpl get(){
        return instance;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public MariadbSession getMariadbSession() {
        return mariadbSession;
    }

    public MongoDBSession getMongoDBSession() {
        return mongoDBSession;
    }

    public RedisSession getRedisSession() {
        return redisSession;
    }

    public boolean isMariadbEnabled() {
        return mariadbEnabled;
    }

    public boolean isMongodbEnabled() {
        return mongodbEnabled;
    }

    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    @Override
    public MessageServiceAPI getAPI() {
        return messageServiceAPI;
    }

    @Override
    public PlaceHolderList getPlaceHolderList() {
        return placeHolderList;
    }

    public dev.jbull.messaging_service.provider.MessageProvider getMessagingProvider() {
        return messagingProvider;
    }
}
