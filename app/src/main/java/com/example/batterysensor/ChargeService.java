package com.example.batterysensor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.support.annotation.Nullable;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

import static android.app.ProgressDialog.show;

public class ChargeService extends Service {
    String batteryTxt = "0";
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Registered callbacks
    private ServiceCallbacks serviceCallbacks;

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        ChargeService getService() {
            // Return this instance of MyService so clients can call public methods
            return ChargeService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    public BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batteryTxt = String.valueOf(level);
        }
    };

    public void onCreate() {
        //do your jobs here
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        //do your jobs here
//        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // if (batteryTxt.getText() == "95") {
        //     if (serviceCallbacks != null) {
        //         serviceCallbacks.stopCharge();
        //     }
        // }
        //String address = (String) intent.getExtras().get("address"); //recieve the address of bluetooth
        String address = intent.getStringExtra("address");
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
        try {
            btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            btSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }



        if (batteryTxt.equals("98")) {
            turnOnLed();
        }
        return super.onStartCommand(intent, flags, startID);
    }

    private void turnOnLed() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("1".toString().getBytes());
            } catch (IOException e) {
                //msg("Error");
            }
        }
    }

}
