package com.jptechgenius.notevault.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jptechgenius.notevault.R;
import com.jptechgenius.notevault.ui.home.HomeActivity;
import com.jptechgenius.notevault.utils.Constants;

/**
 * Splash screen displayed for SPLASH_DELAY_MS before launching HomeActivity.
 * Applies a subtle fade-in + scale animation to the logo and title.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo = findViewById(R.id.ivSplashLogo);
        TextView tvAppName = findViewById(R.id.tvSplashAppName);
        TextView tvTagline = findViewById(R.id.tvSplashTagline);

        // Fade-in + scale animation for logo and text
        AnimationSet animSet = new AnimationSet(true);

        ScaleAnimation scale = new ScaleAnimation(
                0.7f, 1f, 0.7f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(700);

        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(700);

        animSet.addAnimation(scale);
        animSet.addAnimation(fade);
        animSet.setFillAfter(true);

        ivLogo.startAnimation(animSet);
        tvAppName.startAnimation(animSet);
        tvTagline.startAnimation(animSet);

        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }, Constants.SPLASH_DELAY_MS);
    }
}
