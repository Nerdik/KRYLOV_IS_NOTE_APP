package com.example.krylov_is_note_app;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotesViewModel extends AndroidViewModel {

    private static final String PREFS_NAME = "NoteAppPrefs";
    private static final String KEY_CARDS = "saved_cards";

    private final MutableLiveData<List<CardData>> notesLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<String> messageLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Integer> navigationLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<CardData> editNoteLiveData = new SingleLiveEvent<>();

    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();
    private List<CardData> notes = new ArrayList<>();

    public NotesViewModel(Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE);
        loadNotes();
    }

    public LiveData<List<CardData>> getNotes() {
        return notesLiveData;
    }

    public LiveData<String> getMessage() {
        return messageLiveData;
    }

    public LiveData<Integer> getNavigation() {
        return navigationLiveData;
    }

    public LiveData<CardData> getEditNote() {
        return editNoteLiveData;
    }

    private void loadNotes() {
        String cardsJson = sharedPreferences.getString(KEY_CARDS, null);

        if (cardsJson == null || cardsJson.isEmpty()) {
            notes = new ArrayList<>();
            loadInitialData();
            notesLiveData.setValue(new ArrayList<>(notes));
            messageLiveData.setValue("Заметки загружены");
            return;
        }


        Type type = new TypeToken<List<CardData>>(){}.getType();
        List<CardData> loaded = gson.fromJson(cardsJson, type);
        notes.clear();
        if (loaded != null) {
            notes.addAll(loaded);
        } else {
            loadInitialData();
        }
        notesLiveData.setValue(new ArrayList<>(notes));

    }

    private void loadInitialData() {
        notes.clear();
        String[] titles = getApplication().getResources().getStringArray(R.array.titles);
        String[] descriptions = getApplication().getResources().getStringArray(R.array.descriptions);

        for (int i = 0; i < Math.min(titles.length, descriptions.length); i++) {
            notes.add(new CardData(titles[i], descriptions[i]));
        }
    }

    private void saveNotes() {
        String cardsJson = gson.toJson(notes);
        sharedPreferences.edit().putString(KEY_CARDS, cardsJson).apply();
    }

    public void addNote(String title, String description) {
        CardData newCard = new CardData(title, description);
        notes.add(newCard);
        saveNotes();
        notesLiveData.setValue(new ArrayList<>(notes));
        messageLiveData.setValue("Заметка добавлена");
    }

    public void updateNote(int position, String title, String description) {
        if (position >= 0 && position < notes.size()) {
            CardData updatedCard = new CardData(title, description);
            updatedCard.setId(notes.get(position).getId());
            notes.set(position, updatedCard);
            saveNotes();
            notesLiveData.setValue(new ArrayList<>(notes));
            messageLiveData.setValue("Заметка обновлена");
        }
    }

    public void deleteNote(int position) {
        if (position >= 0 && position < notes.size()) {
            notes.remove(position);
            saveNotes();
            notesLiveData.setValue(new ArrayList<>(notes));
            messageLiveData.setValue("Заметка удалена");
        }
    }

    public void clearAllNotes() {
        notes.clear();
        saveNotes();
        notesLiveData.setValue(new ArrayList<>(notes));
        messageLiveData.setValue("Все заметки удалены");
    }

    public CardData getNote(int position) {
        if (position >= 0 && position < notes.size()) {
            return notes.get(position);
        }
        return null;
    }

    public void navigateToAbout() {
        navigationLiveData.setValue(1);
    }

    public void navigateToNotes() {
        navigationLiveData.setValue(0);
    }

    public void openNoteForEditing(int position) {
        CardData note = getNote(position);
        if (note != null) {
            editNoteLiveData.setValue(note);
        }
    }
}