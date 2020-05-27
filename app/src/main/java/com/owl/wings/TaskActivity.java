package com.owl.wings;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owl.downloader.core.Session;
import com.owl.downloader.core.Task;

import java.util.Objects;

public class TaskActivity extends AppCompatActivity {
    private Task task;
    private boolean create = false;
    private EditText directory;
    private TextView connectionCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        Toolbar toolbar = findViewById(R.id.toolbar_task);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab_task);
        fab.setOnClickListener(view -> {
            task.setDirectory(directory.getText().toString());
            task.setMaximumConnections(Integer.parseInt(connectionCount.getText().toString()));
            if (create) Session.getInstance().insertTask(task);
            finish();
        });
        TextView content = findViewById(R.id.task_content);
        Intent intent = getIntent();
        create = intent.getBooleanExtra(MainApplication.ACTION_CREATE, false);
        if (create) {
            Uri data = Objects.requireNonNull(intent.getData());
            boolean fromFile = intent.getBooleanExtra(MainApplication.FROM_FILE, false);
            if (fromFile) {
                task = null; //  TODO: Implement
            } else task = new TestTask(Util.resolveNameFromURI(this, data));
        } else {
            int taskId = intent.getIntExtra(MainApplication.TASK_ID, -1);
            for (Task task : Session.getInstance().getTasks()) {
                if (task.hashCode() == taskId) {
                    this.task = task;
                    break;
                }
            }
        }
        content.setText(task.name());
        initializeSetting();
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

    private void initializeSetting() {
        directory = findViewById(R.id.task_directory);
        directory.setText(task.getDirectory());
        SeekBar seekBar = findViewById(R.id.task_connection);
        connectionCount = findViewById(R.id.task_connection_count);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                connectionCount.setText(String.valueOf(progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            seekBar.setProgress(task.getMaximumConnections() - 1, false);
        else seekBar.setProgress(task.getMaximumConnections() - 1);
    }
}
