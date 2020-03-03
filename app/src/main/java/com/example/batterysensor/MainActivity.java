package com.example.batterysensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
  TextView batteryTxt;
  public BroadcastReceiver mBatInfoReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
          int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
          batteryTxt.setText(level + "%");
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // batteryTxt.setText("Cool");
    this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    // Process process = Runtime.getRuntime().exec("su -c 'echo 0 >
    // /sys/devices/virtual/android_usb/android0/enable'");
  }
}
