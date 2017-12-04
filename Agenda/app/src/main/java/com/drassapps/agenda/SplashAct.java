package com.drassapps.agenda;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

public class SplashAct extends Activity{

    private static final long SPLASH_SCREEN_DELAY = 2500;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecemos solo el modo Portrait
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Eliminamos las barras de notifaciones
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.lay_splash);

        // Creamos la espera
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashAct.this, MainActivity.class);
                startActivity(mainIntent);
                // Le asignamos una animacion
                overridePendingTransition(R.anim.zoom_foward_in, R.anim.zoom_foward_out);
                finish();
            }
        };

        timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }
}
