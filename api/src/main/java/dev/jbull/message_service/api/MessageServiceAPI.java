package dev.jbull.message_service.api;

import dev.jbull.message_service.messages.provider.MessageProvider;

public interface MessageServiceAPI {

    public MessageProvider initMessageProvider(String service);

    public MessageProvider getMessageProvider(String service);

}
