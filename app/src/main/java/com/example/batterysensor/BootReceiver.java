package com.example.batterysensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
  private static final String TAG = "BootReceiver";
  public static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      if (intent.getAction().equalsIgnoreCase(ACTION_BOOT)) {
        // This intent action can only be set by the Android system after a boot
        Intent monitorIntent = new Intent(context, ChargeService.class);
        monitorIntent.putExtra("HANDLE_REBOOT", true);
        context.startService(monitorIntent);
      }
    } catch (Exception e) {

    }
  }
}
