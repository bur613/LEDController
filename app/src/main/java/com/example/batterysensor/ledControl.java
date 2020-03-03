package com.example.batterysensor;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class ledControl extends AppCompatActivity {
  // private ChargeService ChargeService;
  private boolean bound = false;
  // Button btnOn, btnOff, btnDis;
  Button On, Off, Discnt, Abt;
  String address = null;
  BluetoothService btService;
  Boolean mBound = false;
  private ProgressDialog progress;
  BluetoothAdapter myBluetooth = null;
  BluetoothSocket btSocket = null;
  private boolean isBtConnected = false;
  // SPP UUID. Look for it
  static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    registerReceiver(mMessageReceiver, new IntentFilter("fgsdgdfgg"));

    Intent btsIntent = new Intent(ledControl.this, BluetoothService.class);
    bindService(btsIntent, mConnection, Context.BIND_AUTO_CREATE);

    Intent chargeIntent = new Intent(ledControl.this, ChargeService.class);
    // bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    Intent newint = getIntent();
    address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); // recieve the address of bluetooth

    Intent btIntent = new Intent(ledControl.this, BluetoothService.class);
    btIntent.putExtra("bluetooth_device", address);
    chargeIntent.putExtra("BATTERY_UPDATE", 0);
    ContextCompat.startForegroundService(this, btIntent);
    //startService(btIntent);

    setContentView(R.layout.activity_led_control);

    On = findViewById(R.id.on_btn);
    Off = findViewById(R.id.off_btn);
    Discnt = findViewById(R.id.dis_btn);
    Abt = findViewById(R.id.abt_btn);

    On.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            turnOnLed();
          }
        });

    Off.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            turnOffLed();
          }
        });

    Discnt.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Disconnect();
          }
        });

    Abt.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent about = new Intent(ledControl.this, AboutActivity.class);
            startActivity(about);
          }
        });

    // Intent serviceIntent = new Intent(ledControl.this, ChargeService.class);
    // startService(serviceIntent);
  }

  private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
          Intent intent2 = new Intent("i'm audi");
          sendBroadcast(intent2);
      }
  };

  /** Defines callbacks for service binding, passed to bindService() */
  private ServiceConnection mConnection =
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

  private void Disconnect() {
    if (btSocket != null) {
      try {
        btSocket.close();
      } catch (IOException e) {
        msg("Error");
      }
    }
    finish();
  }

  private void turnOffLed() {
    btService.sendData("0");
  }

  private void turnOnLed() {
    btService.sendData("1");
  }

  private void msg(String s) {
    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
  }

  public void about(View v) {
    if (v.getId() == R.id.abt) {
      Intent i = new Intent(ledControl.this, AboutActivity.class);
      startActivity(i);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_led_control, menu);
    return true;
  }

  @Override
  public void onDestroy() {
      //unregisterReceiver(mMessageReceiver);
      //Process.killProcess(Process.myPid());
      super.onDestroy();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
