package com.owl.wings;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
import com.owl.downloader.core.Session;
import com.owl.downloader.core.Task;
import com.owl.wings.databinding.ActivityMainBinding;
import com.owl.wings.databinding.NavigationHeaderMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 0;
    private static final int REQUEST_SELECT_FILE = 1;
    private static final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        if (!Util.checkPermissions(this, permissions))
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
        else launchService();
        setSupportActionBar(binding.toolbarMain);
        NavigationHeaderMainBinding.bind(binding.navigationViewMain.getHeaderView(0));
        binding.fabMain.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.create_task_title);
            View dialogView = View.inflate(this, R.layout.edit_text_url, null);
            builder.setView(dialogView);
            builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
                EditText input = dialogView.findViewById(R.id.edit_text_url);
                if (!Patterns.WEB_URL.matcher(input.getText()).matches())
                    Snackbar.make(binding.fabMain, R.string.invalid_url, Snackbar.LENGTH_LONG).setAction(R.string.ok, View::clearFocus).show();
                else {
                    Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                    intent.setData(Uri.parse(input.getText().toString()));
                    intent.putExtra(MainApplication.ACTION_CREATE, true);
                    intent.putExtra(MainApplication.FROM_FILE, false);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.setNeutralButton(R.string.select_file, (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), REQUEST_SELECT_FILE);
            });
            SimpleDialogFragment fragment = new SimpleDialogFragment(builder.create());
            fragment.show(getSupportFragmentManager(), "create task");
        });
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_overview, R.id.navigation_active, R.id.navigation_waiting, R.id.navigation_paused, R.id.navigation_completed, R.id.navigation_error).setDrawerLayout(binding.drawerLayout).build();
        binding.fragmentMain.post(() -> {
            NavController navController = Navigation.findNavController(this, R.id.fragment_main);
            NavigationUI.setupWithNavController(binding.toolbarMain, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.navigationViewMain, navController);
        });
    }

    private void launchService() {
        Intent service = new Intent(this, BasicService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service);
        } else startService(service);
        bindService(service, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                BasicService.Binder binder = (BasicService.Binder) service;
                Exception exception = binder.exception();
                if (exception != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.error).setPositiveButton(R.string.confirm, (dialog, which) -> finish()).setMessage(exception.toString()).setCancelable(false);
                    new SimpleDialogFragment(builder.create()).show(getSupportFragmentManager(), "error dialog");
                }
                unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        }, Service.BIND_IMPORTANT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                    intent.setData(uri);
                    intent.putExtra(MainApplication.ACTION_CREATE, true);
                    intent.putExtra(MainApplication.FROM_FILE, true);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragment_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_start_all: {
                for (Task task : Session.getInstance().getTasks()) {
                    if (task.status() == Task.Status.PAUSED || task.status() == Task.Status.ERROR)
                        task.start();
                }
                break;
            }
            case R.id.menu_main_pause_all: {
                for (Task task : Session.getInstance().getTasks()) {
                    if (task.status() == Task.Status.ACTIVE || task.status() == Task.Status.WAITING)
                        task.pause();
                }
                break;
            }
            case R.id.menu_main_setting: {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;
            }
            case R.id.menu_main_about: {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            }
            case R.id.menu_main_exit: {
                stopService(new Intent(MainActivity.this, BasicService.class));
                finish();
                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION) return;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle(R.string.missing_permission_title);
                builder.setMessage(R.string.missing_permission_message);
                builder.setPositiveButton(R.string.confirm, (dialog, which) -> finish());
                SimpleDialogFragment fragment = new SimpleDialogFragment(builder.create());
                fragment.show(getSupportFragmentManager(), "missing permission");
                return;
            }
        }
        launchService();
    }
}
