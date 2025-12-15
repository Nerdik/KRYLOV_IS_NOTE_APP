package com.example.krylov_is_note_app;

import androidx.lifecycle.ViewModel;

public class CardViewModel extends ViewModel {

    private String originalTitle = "";
    private String originalDescription = "";
    private String currentTitle = "";
    private String currentDescription = "";
    private int cardPosition = -1;

    public void initData(int position, String title, String description) {
        this.cardPosition = position;
        this.originalTitle = title;
        this.originalDescription = description;
        this.currentTitle = title;
        this.currentDescription = description;
    }

    public void updateTitle(String title) {
        this.currentTitle = title;
    }

    public void updateDescription(String description) {
        this.currentDescription = description;
    }

    public boolean isDataChanged() {
        return !currentTitle.equals(originalTitle) ||
                !currentDescription.equals(originalDescription);
    }

    public boolean validateTitle() {
        return currentTitle != null && !currentTitle.trim().isEmpty();
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public String getCurrentDescription() {
        return currentDescription;
    }

    public int getCardPosition() {
        return cardPosition;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }
}