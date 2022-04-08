package dev.jbull.message_service.messages;

import dev.jbull.message_service.language.Language;
import org.bson.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageImpl implements Message{
    private final String MESSAGE_ID;
    private final Map<Language, String> languageMessageMap = new HashMap<>();


    public MessageImpl(String messageId, Map<Language, String> languageMessageMap){
        MESSAGE_ID = messageId;
        this.languageMessageMap.putAll(languageMessageMap);
    }

    public MessageImpl(Document document){
        MESSAGE_ID = document.getString("MESSAGE_ID");
        document.keySet().forEach(s -> {
            if (s.equalsIgnoreCase("_id"))return;
            if (s.equalsIgnoreCase("MESSAGE_ID"))return;
            languageMessageMap.put(Language.valueOf(s), document.getString(s));
        });
    }

    @Override
    public String getMessageId() {
        return MESSAGE_ID;
    }

    @Override
    public String getInLanguage(Language language) {
        return languageMessageMap.get(language);
    }

    @Override
    public String getMessage() {
        return languageMessageMap.get(languageMessageMap.keySet().stream().findFirst().get());
    }

    public Document parse(){
        Document document = new Document();
        document.put("MESSAGE_ID", MESSAGE_ID);
        languageMessageMap.forEach((language, s) -> {
            document.put(language.toString(), s);
        });
        return document;
    }
}
