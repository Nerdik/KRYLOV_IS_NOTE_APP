package com.example.krylov_is_note_app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.krylov_is_note_app.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "CHANNEL_ID";
    private static final int NOTIFICATION_ID = 42;
    private static final int REQUEST_CODE_EDIT_NOTE = 100;

    private ActivityMainBinding binding;
    private ActionBarDrawerToggle drawerToggle;
    private NotesViewModel viewModel;
    private MyAdapter adapter;
    private RecyclerView recyclerView;

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

        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        initView();
        setupObservers();
        setupFloatingActionButton();
        setupNavigationMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cards_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear) {
            viewModel.clearAllNotes();
            return true;
        }

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
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
            viewModel.deleteNote(position);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void initView() {
        recyclerView = findViewById(R.id.recycler_view_lines);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MyAdapter(new ArrayList<CardData>(), this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.separator, null));
        recyclerView.addItemDecoration(itemDecoration);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(1000);
        animator.setRemoveDuration(1000);
        recyclerView.setItemAnimator(animator);

        adapter.SetOnItemClickListener((view, position) -> openCardActivity(position));
    }

    private void setupObservers() {
        viewModel.getNotes().observe(this, notes -> {
            if (notes == null || notes.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                adapter.updateData(new ArrayList<>(notes));
            }
        });

        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigationMenu() {
        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_notes) {
                binding.drawerLayout.closeDrawers();
            } else if (id == R.id.nav_about) {
                startActivity(new Intent(this, SecondActivity.class));
                binding.drawerLayout.closeDrawers();
            }

            return true;
        });
    }

    private void setupFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            int count = viewModel.getNotes().getValue() != null ?
                    viewModel.getNotes().getValue().size() : 0;
            viewModel.addNote("Title " + count, "Note " + count);
            showNotification();
            recyclerView.smoothScrollToPosition(count);
        });
    }

    private void openCardActivity(int position) {
        CardData cardData = viewModel.getNote(position);
        if (cardData != null) {
            Intent intent = new Intent(MainActivity.this, CardActivity.class);
            intent.putExtra("CARD_POSITION", position);
            intent.putExtra("CARD_TITLE", cardData.getTitle());
            intent.putExtra("CARD_DESCRIPTION", cardData.getDescription());

            startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE);
            overridePendingTransition(R.anim.scale_up, R.anim.fade_out);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_NOTE && resultCode == RESULT_OK && data != null) {
            int position = data.getIntExtra("CARD_POSITION", -1);
            String newTitle = data.getStringExtra("CARD_TITLE");
            String newDescription = data.getStringExtra("CARD_DESCRIPTION");

            if (position != -1) {
                viewModel.updateNote(position, newTitle, newDescription);
                recyclerView.scrollToPosition(position);
            }
        }
    }

    private void showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.baseline_import_contacts_24)
                .setContentTitle("New note")
                .setContentText("Don't forget to update new note!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_ID);
            }
            return;
        }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        String name = "Name";
        String descriptionText = "Description";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(descriptionText);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_ID && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showNotification();
        } else {
            Toast.makeText(this, "Notification permission is not granted", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}