package com.example.krylov_is_note_app;

import android.content.Intent;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.os.CountDownTimer;
import android.view.ViewTreeObserver;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.krylov_is_note_app.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MY_DEFAULT_DURATION = 1000;
    private static final String PREFS_NAME = "NoteAppPrefs";
    private static final String KEY_CARDS = "saved_cards";

    private ActivityMainBinding binding;
    private ActionBarDrawerToggle drawerToggle;
    private CardSource data;
    private MyAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        drawerToggle = new ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        binding.drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        gson = new Gson();
        setupNavigationMenu();
        initView();
        setupFloatingActionButton();
        setSplashScreenLoadingParameters();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cards_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear) {
            data.clearCardData();
            adapter.notifyDataSetChanged();
            saveCardsToPrefs();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        recyclerView = findViewById(R.id.recycler_view_lines);

        List<CardData> savedCards = loadCardsFromPrefs();
        if (savedCards.isEmpty()) {
            data = new CardSourceImpl(getResources()).init();
        } else {
            data = new CardSourceImpl(getResources()).init(savedCards);
        }

        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MyAdapter(data, this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.separator, null));
        recyclerView.addItemDecoration(itemDecoration);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(MY_DEFAULT_DURATION);
        animator.setRemoveDuration(MY_DEFAULT_DURATION);
        recyclerView.setItemAnimator(animator);

        adapter.SetOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onItemClick(View view, int position) {
                openCardActivity(position);
            }
        });
    }

    private void openCardActivity(int position) {
        CardData cardData = data.getCardData(position);

        Intent intent = new Intent(MainActivity.this, CardActivity.class);
        intent.putExtra("CARD_POSITION", position);
        intent.putExtra("CARD_TITLE", cardData.getTitle());
        intent.putExtra("CARD_DESCRIPTION", cardData.getDescription());

        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            int position = data.getIntExtra("CARD_POSITION", -1);
            String newTitle = data.getStringExtra("CARD_TITLE");
            String newDescription = data.getStringExtra("CARD_DESCRIPTION");

            if (position != -1) {
                this.data.updateCardData(position, new CardData(newTitle, newDescription));
                adapter.notifyItemChanged(position);
                saveCardsToPrefs();

                recyclerView.scrollToPosition(position);
            }
        }
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.card_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = adapter.getMenuPosition();

        if (item.getItemId() == R.id.action_update) {
            openCardActivity(position);
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            data.deleteCardData(position);
            adapter.notifyItemRemoved(position);
            saveCardsToPrefs();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void setupNavigationMenu() {
        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_notes) {
                binding.drawerLayout.closeDrawers();
            } else if (id == R.id.nav_about) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
                binding.drawerLayout.closeDrawers();
            }

            return true;
        });
    }

    private void setupFloatingActionButton() {
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardData newCard = new CardData("Title " + data.size(), "Note " + data.size());
                data.addCardData(newCard);
                adapter.notifyItemInserted(data.size() - 1);
                saveCardsToPrefs();
                recyclerView.smoothScrollToPosition(data.size() - 1);
            }
        });
    }


    private void saveCardsToPrefs() {
        List<CardData> cards = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            cards.add(data.getCardData(i));
        }

        String cardsJson = gson.toJson(cards);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_CARDS, cardsJson);
        editor.apply();
    }

    private List<CardData> loadCardsFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cardsJson = prefs.getString(KEY_CARDS, null);

        if (cardsJson == null || cardsJson.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<CardData>>(){}.getType();
            List<CardData> cards = gson.fromJson(cardsJson, type);
            return cards != null ? cards : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCardsToPrefs();
    }

    @Override
    protected void onDestroy() {
        if (binding != null) {
            binding = null;
        }
        super.onDestroy();
    }

    private void setSplashScreenLoadingParameters() {
        final Boolean[] isHideSplashScreen = {false};
        CountDownTimer countDownTimer = new CountDownTimer(5_000, 1_000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                isHideSplashScreen[0] = true;
            }
        }.start();


        // Set up an OnPreDrawListener to the root view.
        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        // Check whether the initial data is ready.
                        if (isHideSplashScreen[0]) {
                            // The content is ready. Start drawing.
                            content.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        } else {
                            // The content isn't ready. Suspend.
                            return false;
                        }
                    }
                });
    }
}