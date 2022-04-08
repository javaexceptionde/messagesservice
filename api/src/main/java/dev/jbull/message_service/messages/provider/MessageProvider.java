package dev.jbull.message_service.messages.provider;

import dev.jbull.message_service.messages.Message;

import java.util.UUID;

public interface MessageProvider {

    public void insertMessage(String messageId, String messageContent, String language);

    public Message getMessageById(String messageId);

    public void refresh(String messageId);

    public void refreshAllMessages();

    public void updateMessage(String messageId, String newMessageContent, String language);

    public void deleteMessage(String messageId);

    public String translateMessage(Message message, UUID uuid, String... args);

    public void sendMessageToUser(String message, UUID uuid);
}
