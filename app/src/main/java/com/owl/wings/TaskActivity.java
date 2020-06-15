package com.owl.wings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.owl.downloader.core.Session;
import com.owl.downloader.core.Task;
import com.owl.wings.databinding.ActivityTaskBinding;

import java.net.URI;
import java.util.Objects;

public class TaskActivity extends AppCompatActivity {
    private Task task;
    private boolean create = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTaskBinding binding = ActivityTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarTask);
        binding.fabTask.setOnClickListener(view -> {
            if (create) Session.getInstance().insertTask(task);
            finish();
        });
        Intent intent = getIntent();
        create = intent.getBooleanExtra(MainApplication.ACTION_CREATE, false);
        if (create) {
            Uri data = Objects.requireNonNull(intent.getData());
            boolean fromFile = intent.getBooleanExtra(MainApplication.FROM_FILE, false);
            if (fromFile) {
                task = null; //  TODO: Implement
            } else task = Session.fromUri(URI.create(data.toString()));
        } else {
            int taskId = intent.getIntExtra(MainApplication.TASK_ID, -1);
            for (Task task : Session.getInstance().getTasks()) {
                if (task.hashCode() == taskId) {
                    this.task = task;
                    break;
                }
            }
        }
        assert task != null;
        TaskViewModel viewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        viewModel.setTask(task);
        binding.pagerTask.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new TaskBasicFragment();
                    case 1:
                        return new TaskDetailFragment();
                    case 2:
                        return new TaskSettingFragment();
                    default:
                        throw new IndexOutOfBoundsException();
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });
        new TabLayoutMediator(binding.tabsTask, binding.pagerTask, (tab, position) -> {
            switch (position) {
                case 0: {
                    tab.setText(R.string.basic);
                    break;
                }
                case 1: {
                    tab.setText(R.string.files);
                    break;
                }
                case 2: {
                    tab.setText(R.string.setting);
                    break;
                }
                default:
                    throw new IndexOutOfBoundsException();
            }
        }).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!create) getMenuInflater().inflate(R.menu.task, menu);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.menu_task_delete: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle(R.string.delete).setMessage(R.string.delete_confirm);
                builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.cancel();
                });
                builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
                    if (task.status() == Task.Status.ACTIVE) task.pause();
                    Session.getInstance().removeTask(task);
                    finish();
                });
                new SimpleDialogFragment(builder.create()).show(getSupportFragmentManager(), "confirm delete");
                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void apply() {
        SharedPreferences preferences = getSharedPreferences(task.name(), Context.MODE_PRIVATE);
        task.setDirectory(preferences.getString("directory", MainApplication.DEFAULT_PATH));
        task.setMaximumConnections(preferences.getInt("maximum_connection_count", 5));
    }
}
