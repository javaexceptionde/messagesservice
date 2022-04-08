package dev.jbull.message_service.placeholder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlaceHolderListImpl implements PlaceHolderList {
    private final Map<String, PlaceHolder> stringPlaceHolderMap = new HashMap<>();

    @Override
    public int size() {
        return stringPlaceHolderMap.size();
    }

    @Override
    public boolean isEmpty() {
        return stringPlaceHolderMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return stringPlaceHolderMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return stringPlaceHolderMap.containsValue(value);
    }

    @Override
    public PlaceHolder get(Object key) {
        return stringPlaceHolderMap.get(key);
    }

    @Nullable
    @Override
    public PlaceHolder put(String key, PlaceHolder value) {
        return stringPlaceHolderMap.put(key, value);
    }

    @Override
    public PlaceHolder remove(Object key) {
        return stringPlaceHolderMap.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends PlaceHolder> m) {
        stringPlaceHolderMap.putAll(m);
    }

    @Override
    public void clear() {
        stringPlaceHolderMap.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return stringPlaceHolderMap.keySet();
    }

    @NotNull
    @Override
    public Collection<PlaceHolder> values() {
        return stringPlaceHolderMap.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, PlaceHolder>> entrySet() {
        return stringPlaceHolderMap.entrySet();
    }
}
