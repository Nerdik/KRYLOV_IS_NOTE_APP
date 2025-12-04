package com.example.krylov_is_note_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.example.krylov_is_note_app.databinding.ActivityCardBinding;

public class CardActivity extends AppCompatActivity {

    private ActivityCardBinding binding;
    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;

    private int cardPosition;
    private String originalTitle;
    private String originalDescription;
    private boolean isDataChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("edit note");
        }

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);

        Intent intent = getIntent();
        if (intent != null) {
            cardPosition = intent.getIntExtra("CARD_POSITION", -1);
            originalTitle = intent.getStringExtra("CARD_TITLE");
            originalDescription = intent.getStringExtra("CARD_DESCRIPTION");

            editTextTitle.setText(originalTitle);
            editTextDescription.setText(originalDescription);
        }

        setupTextChangeListeners();

        binding.toolbar.setNavigationOnClickListener(v -> {
            saveChangesAndFinish();
        });
    }

    private void setupTextChangeListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                checkForChanges();
            }
        };

        editTextTitle.addTextChangedListener(textWatcher);
        editTextDescription.addTextChangedListener(textWatcher);
    }

    private void checkForChanges() {
        String currentTitle = editTextTitle.getText().toString().trim();
        String currentDescription = editTextDescription.getText().toString().trim();

        isDataChanged = !currentTitle.equals(originalTitle) ||
                !currentDescription.equals(originalDescription);
    }

    private void saveChangesAndFinish() {
        String newTitle = editTextTitle.getText().toString().trim();
        String newDescription = editTextDescription.getText().toString().trim();

        if (newTitle.isEmpty()) {
            editTextTitle.setError("Title cannot be empty");
            editTextTitle.requestFocus();
            return;
        }

        if (isDataChanged) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("CARD_POSITION", cardPosition);
            resultIntent.putExtra("CARD_TITLE", newTitle);
            resultIntent.putExtra("CARD_DESCRIPTION", newDescription);
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    @Override
    public void onBackPressed() {
        saveChangesAndFinish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}