package dev.jbull.message_service.api;

import dev.jbull.message_service.messages.provider.MessageProvider;
import dev.jbull.message_service.messages.provider.MessageProviderImpl;

import java.util.HashMap;
import java.util.Map;

public class MessageServiceAPIImpl implements MessageServiceAPI{
    private Map<String, MessageProvider> stringMessageProviderMap = new HashMap<>();

    @Override
    public MessageProvider initMessageProvider(String service) {
        return stringMessageProviderMap.put(service, new MessageProviderImpl(service));
    }

    @Override
    public MessageProvider getMessageProvider(String service) {
        return stringMessageProviderMap.get(service);
    }
}
