package com.example.eventify.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.example.eventify.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.medium_gray));
        
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}