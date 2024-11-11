package com.example.eventify;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;

import com.example.eventify.databinding.ActivitySplashBinding;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        /*
         * Ovom opcijom je sakriven toolbar unutar ove aktivnosti
         * */
        int SPLASH_TIME_OUT = 5000;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                /*
                 * Intent je glavna klasa unutar Android-a za pokretanje ili prelazak na druge delove
                 * vase aplikacije. Da bi pokrenuli drugu aktivnost imamo dve opcije
                 * Prva opcija je eksplicitan intent, gde moramo da kazemo sa koje aktivnosti prelazimo na koju aktivnost:
                 * NPR: sa SplashScreenActivity.this prelazimo na HomeActivity.class
                 * Druga opcija je implicitni intent, gde ne moramo da kažemo gde prelazimo ali moramo
                 * da kažemo šta planiramo da uradimo.
                 */
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                /*
                 * Pozivom startActivity metode, saljemo poruku Android-u da on za nas pokrene drugu aktivnost,
                 * nakon cega korisnik biva prebacen na novu aktivnost.
                 **/
                startActivity(intent);
                /*
                 * Da ne bi moglo da se vrati na SplashScreen ako korisnik
                 * klikne na back dugme
                 * */
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}