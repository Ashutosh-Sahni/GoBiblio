package com.example.gobiblio.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gobiblio.MainActivity;
import com.example.gobiblio.R;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class LibraryActivity extends AppCompatActivity {

    ChipNavigationBar navigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_library);

        setBottomNavigation();

    }


    @SuppressLint("NonConstantResourceId")
    private void setBottomNavigation() {
        navigationBar = findViewById(R.id.navigationBar);
        navigationBar.setItemSelected(R.id.library, true);

        navigationBar.setOnItemSelectedListener(i -> {
            switch (i) {
                case R.id.home:
                    startActivity(new Intent(LibraryActivity.this, MainActivity.class));
                    break;
                case R.id.library:
                    break;
                case R.id.profile:
                    startActivity(new Intent(LibraryActivity.this, ProfileActivity.class));
                    break;
            }
        });
    }

}