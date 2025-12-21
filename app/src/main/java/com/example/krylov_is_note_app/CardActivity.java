package com.example.krylov_is_note_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.krylov_is_note_app.databinding.ActivityCardBinding;
import com.google.android.material.textfield.TextInputEditText;

public class CardActivity extends AppCompatActivity {

    private ActivityCardBinding binding;
    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private CardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CardViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("Edit note");
        }

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);

        Intent intent = getIntent();
        if (intent != null) {
            int cardPosition = intent.getIntExtra("CARD_POSITION", -1);
            String originalTitle = intent.getStringExtra("CARD_TITLE");
            String originalDescription = intent.getStringExtra("CARD_DESCRIPTION");

            viewModel.initData(cardPosition, originalTitle, originalDescription);

            editTextTitle.setText(originalTitle);
            editTextDescription.setText(originalDescription);
        }

        setupTextChangeListeners();

        binding.toolbar.setNavigationOnClickListener(v -> saveChangesAndFinish());
    }

    private void setupTextChangeListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateTitle(editTextTitle.getText().toString().trim());
                viewModel.updateDescription(editTextDescription.getText().toString().trim());
            }
        };

        editTextTitle.addTextChangedListener(textWatcher);
        editTextDescription.addTextChangedListener(textWatcher);
    }

    private void saveChangesAndFinish() {
        if (!viewModel.validateTitle()) {
            editTextTitle.setError("Title cannot be empty");
            editTextTitle.requestFocus();
            return;
        }

        if (viewModel.isDataChanged()) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("CARD_POSITION", viewModel.getCardPosition());
            resultIntent.putExtra("CARD_TITLE", viewModel.getCurrentTitle());
            resultIntent.putExtra("CARD_DESCRIPTION", viewModel.getCurrentDescription());
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