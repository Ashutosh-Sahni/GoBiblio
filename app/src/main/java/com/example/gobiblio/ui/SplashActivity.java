package com.example.gobiblio.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gobiblio.R;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2400;
    TextView appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // Remove Status bar
        setContentView(R.layout.activity_splash);

        initName();

        //Opening RegisterActivity automatically
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
            finish();
        }, SPLASH_TIME);

    }

    private void initName() {
        appName = findViewById(R.id.txt_name);

        //Setting two colors
        Spannable first = new SpannableString("Go");
        Spannable second = new SpannableString("Biblio");
        first.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorWhite)),0,first.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        second.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorYellow)),0,second.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        appName.setText(first);
        appName.append(second);
    }

}