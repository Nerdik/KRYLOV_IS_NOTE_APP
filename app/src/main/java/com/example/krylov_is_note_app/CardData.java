package com.example.krylov_is_note_app;

public class CardData {
    private String title;
    private String description;
    private long id;

    public CardData() {
        this.id = System.currentTimeMillis();
    }

    public CardData(String title, String description) {
        this.title = title;
        this.description = description;
        this.id = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}