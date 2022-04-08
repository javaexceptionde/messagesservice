package dev.jbull.message_service.messages;

import dev.jbull.message_service.language.Language;

public interface Message {

    public String getMessageId();

    public String getInLanguage(Language language);

    public String getMessage();


}
