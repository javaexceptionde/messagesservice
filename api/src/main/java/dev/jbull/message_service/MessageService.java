package dev.jbull.message_service;

import dev.jbull.message_service.api.MessageServiceAPI;
import dev.jbull.message_service.placeholder.PlaceHolderList;

public interface MessageService {

    MessageServiceAPI getAPI();

    PlaceHolderList getPlaceHolderList();

}
