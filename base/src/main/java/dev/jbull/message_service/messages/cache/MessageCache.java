package dev.jbull.message_service.messages.cache;

import dev.jbull.message_service.MessageServiceImpl;
import dev.jbull.message_service.language.Language;
import dev.jbull.message_service.messages.Message;
import dev.jbull.message_service.messages.MessageImpl;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MessageCache implements Map<String, Message> {
    private Map<String, Message> messageMap = new HashMap<>();
    private String identifier;

    public MessageCache(String identifier){
        this.identifier = identifier;
    }

    @Override
    public int size() {
        if (MessageServiceImpl.get().isRedisEnabled()){
            MessageServiceImpl.get().getRedisSession().openConnection(callBack -> {

            });
        }
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        if (MessageServiceImpl.get().isRedisEnabled()){
            AtomicBoolean atomicBoolean = new AtomicBoolean();
            MessageServiceImpl.get().getRedisSession().openConnection(callBack -> {
                atomicBoolean.set(callBack.exists(identifier + key.toString()));
            });
            return atomicBoolean.get();
        }
        return messageMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return messageMap.containsValue(value);
    }

    @Override
    public Message get(Object key) {
        AtomicReference<Message> atomicReference = new AtomicReference<>();
        if (MessageServiceImpl.get().isRedisEnabled()){
            MessageServiceImpl.get().getRedisSession().openConnection(callBack -> {
                if (!callBack.exists(identifier + key.toString())){
                    return;
                }
                String json = callBack.get(identifier + key.toString());
                System.out.println(identifier + key.toString());
                System.out.println(json);
                Document document = Document.parse(json);
                atomicReference.set(new MessageImpl(document));
            });
        }else {
            return messageMap.get(key);
        }
        return atomicReference.get();
    }

    @Nullable
    @Override
    public Message put(String key, Message value) {
        if (MessageServiceImpl.get().isRedisEnabled()){
            MessageServiceImpl.get().getRedisSession().openConnection(callBack -> {
                System.out.println(((MessageImpl) value).parse().toJson());
                callBack.set(identifier + key, ((MessageImpl)value).parse().toJson());
            });
            return value;
        }
        messageMap.put(key, value);
        return value;
    }

    @Override
    public Message remove(Object key) {
        if (MessageServiceImpl.get().isRedisEnabled()) {
            MessageServiceImpl.get().getRedisSession().openConnection(callBack -> {
                callBack.del(identifier + key.toString());
            });
        }else {
            messageMap.remove(key);
        }
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends Message> m) {

    }

    @Override
    public void clear() {
        messageMap.clear();
        if (MessageServiceImpl.get().isRedisEnabled()){
            MessageServiceImpl.get().getRedisSession().openConnectionAsync(callBack -> {
                Jedis jedis = callBack;
                jedis.keys(identifier + "*").forEach(jedis::del);
            });
        }
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return null;
    }

    @NotNull
    @Override
    public Collection<Message> values() {
        return null;
    }

    @NotNull
    @Override
    public Set<Entry<String, Message>> entrySet() {
        return null;
    }
}