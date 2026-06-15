package com.shivani.buddyroute.model;

public class Achievement {
    public String id;
    public String emoji;
    public String title;
    public String description;
    public boolean unlocked;
    public long unlockedAt;

    public Achievement(String id, String emoji,
                       String title, String description) {
        this.id = id;
        this.emoji = emoji;
        this.title = title;
        this.description = description;
        this.unlocked = false;
        this.unlockedAt = 0;
    }
}