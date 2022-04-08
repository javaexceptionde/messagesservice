package dev.jbull.message_service.language;

public enum Language {
    GERMAN("GER", "Deutsch",  false),
    ENGLISH("ENG", "English", true);
    private final String TAG;
    private final String DISPLAYNAME;
    private final boolean ACTIVE;

    Language(String tag, String displayName, boolean active){
        this.ACTIVE = active;
        this.DISPLAYNAME = displayName;
        this.TAG = tag;
    }

    public String getDisplayName() {
        return DISPLAYNAME;
    }

    public String getTag() {
        return TAG;
    }

    public boolean isActive() {
        return ACTIVE;
    }
}
