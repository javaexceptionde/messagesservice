package dev.jbull.message_service.placeholder;

import java.util.UUID;

public interface PlaceHolder {

    public String onPlaceholder(String placeholder, UUID sendTo);

}
