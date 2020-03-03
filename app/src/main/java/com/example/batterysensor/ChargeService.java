package com.example.batterysensor;

import android.app.Service;
import android.content.*;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

public class ChargeService extends Service {

  private static final String TAG = "ChargeService";
  public static final String BATTERY_UPDATE = "battery";
  Boolean mBound = false;
  BluetoothService btService;

  /** Defines callbacks for service binding, passed to bindService() */
  private ServiceConnection xConnection =
          new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
              // We've bound to LocalService, cast the IBinder and get LocalService instance
              BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
              btService = binder.getService();
              mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
              mBound = false;
            }
          };

  @Override
  public void onCreate() {
    Intent btIntent = new Intent(ChargeService.this, BluetoothService.class);
    bindService(btIntent, xConnection, Context.BIND_AUTO_CREATE);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    if (intent != null && intent.hasExtra(BootReceiver.ACTION_BOOT)) {
      AlarmReceiver.startAlarms(ChargeService.this.getApplicationContext());
    }
    new BatteryCheckAsync().execute();

    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private class BatteryCheckAsync extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... arg0) {
      // Battery State check - create log entries of current battery state
      BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
      int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

      if (batLevel == 99) {
        btService.sendData("1");
      }
      return null;
    }

    protected void onPostExecute() {
    }
  }
}
