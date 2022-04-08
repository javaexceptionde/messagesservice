package dev.jbull.message_service.messages;

import dev.jbull.message_service.language.Language;

public enum Messages {
    PREFIX("§8[§bMessageService§8] §7", Language.ENGLISH),
    MESSAGE_COMMAND_USAGE("%message_messages_PREFIX% §7You can use the following subcommands\ndelete all - Deletes all Messages from the Database", Language.ENGLISH),
    MESSAGE_COMMAND_DELETE_SUCCEED("%message_messages_PREFIX% §7Successfully deleted all Messages", Language.ENGLISH),
    MESSAGE_COMMAND_REFRESH_SUCCEED("%message_messages_PREFIX% §7Successfully refreshed all Messages", Language.ENGLISH);
    private final String language;
    private final String defaultMessage;

    Messages(String default_message, Language language){
        this.defaultMessage = default_message;
        this.language = language.toString();
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String getLanguage() {
        return language;
    }
}
