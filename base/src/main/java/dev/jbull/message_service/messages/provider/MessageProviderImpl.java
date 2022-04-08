package dev.jbull.message_service.messages.provider;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.jbull.message_service.MessageServiceImpl;
import dev.jbull.message_service.exceptions.UnknownMessageException;
import dev.jbull.message_service.language.Language;
import dev.jbull.message_service.messages.Message;
import dev.jbull.message_service.messages.MessageImpl;
import dev.jbull.message_service.messages.cache.MessageCache;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageProviderImpl implements MessageProvider {
    private final MessageCache messageCache;
    private final String service;

    public MessageProviderImpl(String service){
        this.messageCache = new MessageCache(service);
        this.service = service;
    }

    @Override
    public void insertMessage(String messageId, String messageContent, String language) {
        if (MessageServiceImpl.get().isMariadbEnabled()){
            MessageServiceImpl.get().getMariadbSession().openConnectionAsync(callBack -> {
                try(Connection connection = callBack) {
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM messages WHERE MESSAGE_KEY = ? AND SERVICE = ?");
                    preparedStatement.setString(1, messageId);
                    preparedStatement.setString(2, service);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (!resultSet.next()){
                        preparedStatement = connection.prepareStatement("INSERT INTO messages(MESSAGE_KEY, MESSAGE, SERVICE) VALUES (?, ?, ?)");
                        preparedStatement.setString(1, messageId);
                        Document document = new Document();
                        document.put("MESSAGE_ID", messageId);
                        document.put(language, messageContent);
                        preparedStatement.setString(2, document.toJson());
                        preparedStatement.setString(3, service);
                        preparedStatement.executeUpdate();
                    }else {
                        preparedStatement = connection.prepareStatement("UPDATE messages SET MESSAGE = ? WHERE MESSAGE_KEY = ? AND SERVICE = ?");
                        Document document = Document.parse(resultSet.getString("MESSAGE"));
                        document.put(language, messageContent);
                        preparedStatement.setString(1, document.toJson());
                        preparedStatement.setString(2, messageId);
                        preparedStatement.setString(3, service);
                        preparedStatement.executeUpdate();
                    }
                }catch (SQLException exception){
                    exception.printStackTrace();
                }
            });
        }else if (MessageServiceImpl.get().isMongodbEnabled()){
            System.out.println("Mongodb enabled");
            if (MessageServiceImpl.get().getMongoDBSession().getMongoCollection("messages").countDocuments(Filters.eq("service", service)) == 0){
                Document document = new Document();
                document.put("service", service);
                Document document1 = new Document();
                document1.put("MESSAGE_ID", messageId);
                document1.put(language, messageContent);
                document.put(messageId, document1);
                MessageServiceImpl.get().getMongoDBSession().insertOne("messages", document);
                return;
            }else {
                Document document = (Document) MessageServiceImpl.get().getMongoDBSession().getMongoCollection("messages").find(Filters.eq("service", service)).first();
                Document document1 = new Document();
                document1.put("MESSAGE_ID", messageId);
                document1.put(language, messageContent);
                document.put(messageId, document1);
                MessageServiceImpl.get().getMongoDBSession().updateDocument("messages","service", service ,document);
                System.out.println("Mongodb enabled2");
            }
        }
    }

    @Override
    public Message getMessageById(String messageId) {
        if (messageCache.get(messageId) != null){
            return messageCache.get(messageId);
        }
        throw new UnknownMessageException("Message with id " + messageId + " does not exist");
    }

    @Override
    public void refresh(String messageId) {
        messageCache.remove(messageId);
        if (MessageServiceImpl.get().isMariadbEnabled()){
            MessageServiceImpl.get().getMariadbSession().openConnectionAsync(callBack -> {
                try(Connection connection = callBack) {
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM messages WHERE MESSAGE_KEY = ? AND SERVICE = ?");
                    preparedStatement.setString(1, messageId);
                    preparedStatement.setString(2, service);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()){
                        Document document = Document.parse(resultSet.getString("MESSAGE"));
                        messageCache.put(messageId, new MessageImpl(document));
                    }
                }catch (SQLException exception){
                    exception.printStackTrace();
                }
            });
        }else if (MessageServiceImpl.get().isMongodbEnabled()){
            Document document = (Document) MessageServiceImpl.get().getMongoDBSession().getMongoCollection("messages").find(Filters.eq("service", service)).first();
            if (document != null){
                Document document1 = (Document) document.get(messageId);
                messageCache.put(messageId, new MessageImpl(document1));
            }
        }
    }

    @Override
    public void refreshAllMessages() {
        messageCache.clear();
        if (MessageServiceImpl.get().isMongodbEnabled()){
            if (MessageServiceImpl.get().getMongoDBSession().getMongoCollection("messages").countDocuments(Filters.eq("service", service)) == 0){
                return;
            }
            Document document = MessageServiceImpl.get().getMongoDBSession().getDocument("messages", "service", service);
            for (String key : document.keySet()){
                if (key.equalsIgnoreCase("service"))continue;
                if (key.equalsIgnoreCase("_id"))continue;
                messageCache.put(key, new MessageImpl((Document) document.get(key)));
            }
        }else if (MessageServiceImpl.get().isMariadbEnabled()){
            MessageServiceImpl.get().getMariadbSession().openConnectionAsync(callBack -> {
                try(Connection connection = callBack) {
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM messages WHERE SERVICE = ?");
                    preparedStatement.setString(1, service);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()){
                        messageCache.put(resultSet.getString("MESSAGE_KEY"), new MessageImpl(Document.parse(resultSet.getString("MESSAGE"))));
                    }
                }catch (SQLException exception){
                    exception.printStackTrace();
                }
            });
        }
    }

    @Override
    public void updateMessage(String messageId, String newMessageContent, String language) {
        if (MessageServiceImpl.get().isMongodbEnabled()){
            Document document = MessageServiceImpl.get().getMongoDBSession().getDocument("messages", "service", service);
            Document document1 = Document.parse(document.get(messageId).toString());
            document1.put(language, newMessageContent);
            document.put(messageId, document1);
            MessageServiceImpl.get().getMongoDBSession().updateDocument("messages", "service", service, document);
        }
        else if (MessageServiceImpl.get().isMariadbEnabled()){
            MessageServiceImpl.get().getMariadbSession().openConnectionAsync(callBack -> {
                try(Connection connection = callBack) {
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM messages WHERE MESSAGE_KEY = ? AND SERVICE = ?");
                    preparedStatement.setString(1, messageId);
                    preparedStatement.setString(2, service);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()){
                        Document document = Document.parse(resultSet.getString("MESSAGE"));
                        document.put(language, newMessageContent);
                        preparedStatement = connection.prepareStatement("UPDATE messages SET MESSAGE = ? WHERE MESSAGE_KEY = ? AND SERVICE = ?");
                        preparedStatement.setString(1, document.toJson());
                        preparedStatement.setString(2, messageId);
                        preparedStatement.setString(3, service);
                        preparedStatement.executeUpdate();
                    }
                }catch (SQLException exception){
                    exception.printStackTrace();
                }
            });
        }
    }

    @Override
    public void deleteMessage(String messageId) {
        messageCache.remove(messageId);
        if (MessageServiceImpl.get().isMariadbEnabled()){
            MessageServiceImpl.get().getMariadbSession().openConnectionAsync(callBack -> {
                try(Connection connection = callBack) {
                    PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM messages WHERE MESSAGE_KEY = ? AND SERVICE = ?");
                    preparedStatement.setString(1, messageId);
                    preparedStatement.setString(2, service);
                    preparedStatement.execute();
                } catch (SQLException exception){
                    exception.printStackTrace();
                }
            });
        }
        else if (MessageServiceImpl.get().isMongodbEnabled()){
             Document document = (Document) MessageServiceImpl.get().getMongoDBSession().getMongoCollection("messages").find(Filters.eq("service", service)).first();
             if (document != null){
                 document.remove(messageId);
                 ReplaceOptions updateOptions = new ReplaceOptions();
                 updateOptions.upsert(true);
                 Bson bson = Filters.eq("service", service);
                 MessageServiceImpl.get().getMongoDBSession().getMongoCollection("messages").replaceOne(bson, null, updateOptions);
             }
        }
    }

    @Override
    public String translateMessage(Message message, UUID uuid, String... args) {
        String messageRaw = message.getInLanguage(Language.ENGLISH);
        AtomicInteger i = new AtomicInteger();
        AtomicInteger i1 = new AtomicInteger();
        AtomicInteger i2 = new AtomicInteger();
        AtomicBoolean isArgument = new AtomicBoolean(false);
        List<String> replacements = new ArrayList<>();
        String finalMessageRaw = messageRaw;
        messageRaw.chars().forEach(c -> {
            if (isArgument.get()) {
                if (c == '%') {
                    i.getAndIncrement();
                    i2.set(i.get());
                    replacements.add(finalMessageRaw.substring(i1.get(), i.get()));
                    isArgument.set(false);
                    return;
                }
            }
            if (c == '%') {
                isArgument.set(true);
                i1.set(i.get());
            }
            i.getAndIncrement();
        });
        for (String replacement : replacements) {
            if (replacement.startsWith("%message_") && replacement.endsWith("%")) {
                String serviceId = replacement.substring(9, replacement.length() - 1);
                String[] serviceIdSplit = serviceId.split("_");
                if (serviceIdSplit.length == 2) {
                    if (MessageServiceImpl.get().getAPI().getMessageProvider(serviceIdSplit[0]) == null){
                        MessageServiceImpl.get().getAPI().initMessageProvider(serviceIdSplit[0]);
                        MessageProvider provider = MessageServiceImpl.get().getAPI().getMessageProvider(serviceIdSplit[0]);
                        Message message1 = provider.getMessageById(serviceIdSplit[1].replace("%", ""));
                        messageRaw = messageRaw.replace(replacement, message1.getInLanguage(Language.ENGLISH));
                    }else {
                        Message message1 = MessageServiceImpl.get().getAPI().getMessageProvider(serviceIdSplit[0]).getMessageById(serviceIdSplit[1].replace("%", ""));
                        messageRaw = messageRaw.replace(replacement, message1.getInLanguage(Language.ENGLISH));
                    }
                }
            }else if (replacement.equalsIgnoreCase("%player_uuid%")){
                messageRaw = messageRaw.replace(replacement, uuid.toString());
            }else if (replacement.equalsIgnoreCase("%player_name%")){
                messageRaw = messageRaw.replace(replacement, Bukkit.getPlayer(uuid).getName());
            }
            if (MessageServiceImpl.get().getPlaceHolderList().containsKey(replacement)){
                messageRaw = messageRaw.replace(replacement, MessageServiceImpl.get().getPlaceHolderList().get(replacement).onPlaceholder(replacement, uuid));

            }
        }

        return messageRaw;
    }

    @Override
    public void sendMessageToUser(String message, UUID uuid) {
        MessageServiceImpl.get().getMessagingProvider().sendMessage("messages", new Document("uuid", uuid.toString()).append("message", message).toJson());
    }
}
