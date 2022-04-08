package dev.jbull.message_service.commands;

import dev.jbull.core.Core;
import dev.jbull.message_service.MessageServiceImpl;
import dev.jbull.message_service.messages.Messages;
import dev.jbull.message_service.messages.provider.MessageProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MessageCommand extends BukkitCommand {
    MessageProvider provider = MessageServiceImpl.get().getAPI().getMessageProvider("messages");

    public MessageCommand() {
        super("message");
    }


    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player)){
            return false;
        }
        Player player = (Player) commandSender;
        if (args.length == 0){
            player.sendMessage(provider.translateMessage(provider.getMessageById(Messages.MESSAGE_COMMAND_USAGE.toString()), player.getUniqueId()));
        }else if (args.length == 2){
            if (args[0].equalsIgnoreCase("delete")){
                if (args[1].equalsIgnoreCase("all")){
                    player.sendMessage(provider.translateMessage(provider.getMessageById(Messages.MESSAGE_COMMAND_DELETE_SUCCEED.toString()), player.getUniqueId()));
                }
            }else if (args[0].equalsIgnoreCase("refresh")){
                if (args[1].equalsIgnoreCase("all")){
                    player.sendMessage(provider.translateMessage(provider.getMessageById(Messages.MESSAGE_COMMAND_REFRESH_SUCCEED.toString()), player.getUniqueId()));
                    provider.refreshAllMessages();
                }
            }
        }
        return false;
    }
}
