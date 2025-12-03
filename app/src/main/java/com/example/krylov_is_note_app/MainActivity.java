package com.example.krylov_is_note_app;

import android.content.Intent;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    private static final int MY_DEFAULT_DURATION = 1000;

    private static ActivityMainBinding binding;

    private ActionBarDrawerToggle drawerToggle;

    private CardSource data;
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

        setupNavigationMenu();
        initView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cards_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            data.addCardData(new CardData("Title " + data.size(),
                    "Note " + data.size()
                    ));
            adapter.notifyItemInserted(data.size() - 1);
            recyclerView.smoothScrollToPosition(data.size() - 1);
            return true;
        } else if (item.getItemId() == R.id.action_clear) {
            data.clearCardData();
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        recyclerView = findViewById(R.id.recycler_view_lines);
        data = new CardSourceImpl(getResources()).init();
        initRecyclerView();
    }

    private void initRecyclerView(/*RecyclerView recyclerView, CardSource data*/) {

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
}