package com.example.eventify.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.example.eventify.R;
import com.example.eventify.services.auth.LoginService;
import com.example.eventify.utils.DeepLinkPayload;
import com.example.eventify.utils.DeepLinkStorage;

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

        DeepLinkPayload payload = DeepLinkPayload.fromIntent(getIntent());
        
        /*
         * Ovom opcijom je sakriven toolbar unutar ove aktivnosti
         * */
        int SPLASH_TIME_OUT = 2000;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LoginService loginService = new LoginService(SplashActivity.this);
                boolean hasValidToken = loginService.getToken() != null && loginService.isTokenValid();

                if (!hasValidToken && payload != null) {
                    // Sačuvaj pending deep link da se izvrši posle logina
                    DeepLinkStorage.save(SplashActivity.this, payload);
                }

                Intent intent = new Intent(
                        SplashActivity.this,
                        hasValidToken ? MainActivity.class : LoginActivity.class
                );

                // Ako je korisnik ulogovan, odmah prosledi payload dalje (Main-u)
                if (hasValidToken && payload != null) {
                    payload.putInto(intent);
                }

                startActivity(intent);
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
