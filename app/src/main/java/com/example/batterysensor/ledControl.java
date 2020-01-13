package com.example.batterysensor;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageButton;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.UUID;

import static android.app.ProgressDialog.*;

public class ledControl extends AppCompatActivity implements ServiceCallbacks {
    //private ChargeService ChargeService;
    private boolean bound = false;
    // Button btnOn, btnOff, btnDis;
    Button On, Off, Discnt, Abt;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(ledControl.this, ChargeService.class);
        //bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //recieve the address of bluetooth


        setContentView(R.layout.activity_led_control);

        On = (Button)findViewById(R.id.on_btn);
        Off = (Button)findViewById(R.id.off_btn);
        Discnt = (Button)findViewById(R.id.dis_btn);
        Abt = (Button)findViewById(R.id.abt_btn);

        new ConnectBT().execute();

        On.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnLed();
            }
        });

        Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffLed();
            }
        });

        Discnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });

        Abt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent about = new Intent(ledControl.this, AboutActivity.class);
                startActivity(about);
            }
        });

        Intent serviceIntent = new Intent(ledControl.this, ChargeService.class);
        serviceIntent.putExtra("address", address);
        this.startService(serviceIntent);

    }

    /** Callbacks for service binding, passed to bindService()
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            com.example.batterysensor.ChargeService.LocalBinder binder = (com.example.batterysensor.ChargeService.LocalBinder) service;
            ChargeService = binder.getService();
            bound = true;
            ChargeService.setCallbacks(ledControl.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
    */

    /* Defined by ServiceCallbacks interface */
    @Override
    public void stopCharge() {
        turnOnLed();
    }


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
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("0".toString().getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void turnOnLed() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("1".toString().getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = show(ledControl.this, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is is a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

}
