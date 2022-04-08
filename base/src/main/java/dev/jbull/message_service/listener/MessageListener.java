package dev.jbull.message_service.listener;

import dev.jbull.messaging_service.events.ChannelMessageEvent;
import dev.jbull.messaging_service.events.EventListener;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.UUID;

public class MessageListener extends EventListener {
    @Override
    public void onChannelMessage(ChannelMessageEvent channelMessageEvent) {
        if (channelMessageEvent.getChannel().equals("messages")) {
            Document document = Document.parse(channelMessageEvent.getMessage());
            if (Bukkit.getPlayer(UUID.fromString(document.getString("uuid"))) != null) {
                Bukkit.getPlayer(UUID.fromString(document.getString("uuid"))).sendMessage(document.getString("message"));
            }
        }
    }
}
