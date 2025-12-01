package com.example.krylov_is_note_app;

public interface CardSource {

    CardData getCardData(int position);
    int size();

    void deleteCardData(int position);
    void updateCardData(int position, CardData cardData);
    void addCardData(CardData cardData);
    void clearCardData();

}
