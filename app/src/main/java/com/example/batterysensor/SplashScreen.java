package com.example.batterysensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreen extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash);

    Thread timerThread =
        new Thread() {
          public void run() {
            try {
              sleep(700);
            } catch (InterruptedException e) {
              e.printStackTrace();
            } finally {
              Intent intent = new Intent(SplashScreen.this, DeviceList.class);
              // Intent serviceIntent = new Intent(SplashScreen.this, ChargeService.class);
              // startService(serviceIntent);
              startActivity(intent);
            }
          }
        };
    timerThread.start();
  }

  @Override
  protected void onPause() {
    super.onPause();
    finish();
  }
}
