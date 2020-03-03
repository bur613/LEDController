package com.example.batterysensor;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.Vector;

public class BluetoothService extends Service {

  private BluetoothAdapter mBluetoothAdapter;
  public static final String B_DEVICE = "MY DEVICE";
  public static final String B_UUID = "00001101-0000-1000-8000-00805f9b34fb";
  // 00000000-0000-1000-8000-00805f9b34fb

  private static final String CHANNEL_ID ="channel1";
  public static final int STATE_NONE = 0;
  public static final int STATE_LISTEN = 1;
  public static final int STATE_CONNECTING = 2;
  public static final int STATE_CONNECTED = 3;

  private ConnectBtThread mConnectThread;
  private static ConnectedBtThread mConnectedThread;

  private static Handler mHandler = null;
  public static int mState = STATE_NONE;
  public static String deviceName;
  public static BluetoothDevice sDevice = null;
  public Vector<Byte> packData = new Vector<>(2048);
  public boolean chargeDone = false;

  // IBinder mIBinder = new LocalBinder();

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    // mHandler = getApplication().getHandler();
    return mBinder;
  }

  private BroadcastReceiver sMessageReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      stopForeground(false);
      //stopSelf();
    }
  };

  public void toast(String mess) {
    Toast.makeText(this, mess, Toast.LENGTH_SHORT).show();
  }

  private final IBinder mBinder = new LocalBinder();

  public class LocalBinder extends Binder {
    BluetoothService getService() {
      // Return this instance of LocalService so clients can call public methods
      return BluetoothService.this;
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    createNotificationChannel();
    startInForeground();
    registerReceiver(sMessageReceiver, new IntentFilter("i'm audi"));

    String deviceg = intent.getStringExtra("bluetooth_device");

    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    connectToDevice(deviceg);

    // new BatteryCheckAsync().execute();

    return START_NOT_STICKY;
  }

  private synchronized void connectToDevice(String macAddress) {
    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
    if (mState == STATE_CONNECTING) {
      if (mConnectThread != null) {
        mConnectThread.cancel();
        mConnectThread = null;
      }
    }
    if (mConnectedThread != null) {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }
    mConnectThread = new ConnectBtThread(device);
    toast("connecting");
    mConnectThread.start();
    setState(STATE_CONNECTING);
    try {
      mConnectThread.join();
    } catch (Exception e) {
    }
    new BatteryCheckAsync().execute();
  }

  private void setState(int state) {
    mState = state;
    if (mHandler != null) {
      // mHandler.obtainMessage();
    }
  }

  public synchronized void stop() {
    setState(STATE_NONE);
    if (mConnectThread != null) {
      mConnectThread.cancel();
      mConnectThread = null;
    }
    if (mConnectedThread != null) {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }
    if (mBluetoothAdapter != null) {
      mBluetoothAdapter.cancelDiscovery();
    }

    stopSelf();
  }

  public void sendData(String message) {
    if (mConnectedThread != null) {
      mConnectedThread.write(message.getBytes());
      toast("sent data");
    } else {
      Toast.makeText(BluetoothService.this, "Failed to send data", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public boolean stopService(Intent name) {
    setState(STATE_NONE);

    if (mConnectThread != null) {
      mConnectThread.cancel();
      mConnectThread = null;
    }

    if (mConnectedThread != null) {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }

    mBluetoothAdapter.cancelDiscovery();
    return super.stopService(name);
  }

  /*private synchronized void connected(BluetoothSocket mmSocket){

      if (mConnectThread != null){
          mConnectThread.cancel();
          mConnectThread = null;
      }
      if (mConnectedThread != null){
          mConnectedThread.cancel();
          mConnectedThread = null;
      }

      mConnectedThread = new ConnectedBtThread(mmSocket);
      mConnectedThread.start();


      setState(STATE_CONNECTED);
  }*/

  private class ConnectBtThread extends Thread {
    private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;

    public ConnectBtThread(BluetoothDevice device) {
      mDevice = device;
      BluetoothSocket socket = null;
      try {
        socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(B_UUID));
      } catch (IOException e) {
        e.printStackTrace();
      }
      mSocket = socket;
    }

    @Override
    public void run() {
      mBluetoothAdapter.cancelDiscovery();

      try {
        mSocket.connect();
        Log.d("service", "connect thread run method (connected)");
        SharedPreferences pre = getSharedPreferences("BT_NAME", 0);
        pre.edit().putString("bluetooth_connected", mDevice.getName()).apply();

      } catch (IOException e) {

        try {
          mSocket.close();
          Log.d("service", "connect thread run method ( close function)");
        } catch (IOException e1) {
          e1.printStackTrace();
        }
        e.printStackTrace();
      }
      // connected(mSocket);
      mConnectedThread = new ConnectedBtThread(mSocket);
      mConnectedThread.start();
    }

    public void cancel() {

      try {
        mSocket.close();
        Log.d("service", "connect thread cancel method");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private class ConnectedBtThread extends Thread {
    private final BluetoothSocket cSocket;
    private final InputStream inS;
    private final OutputStream outS;

    private byte[] buffer;

    public ConnectedBtThread(BluetoothSocket socket) {
      cSocket = socket;
      InputStream tmpIn = null;
      OutputStream tmpOut = null;

      try {
        tmpIn = socket.getInputStream();

      } catch (IOException e) {
        e.printStackTrace();
      }

      try {
        tmpOut = socket.getOutputStream();
      } catch (IOException e) {
        e.printStackTrace();
      }

      inS = tmpIn;
      outS = tmpOut;
    }

    @Override
    public void run() {
      buffer = new byte[1024];
      int mByte;
      try {
        mByte = inS.read(buffer);
      } catch (IOException e) {
        e.printStackTrace();
      }
      Log.d("service", "connected thread run method");
    }

    public void write(byte[] buff) {
      try {
        outS.write(buff);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    private void cancel() {
      try {
        cSocket.close();
        Log.d("service", "connected thread cancel method");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onDestroy() {
    unregisterReceiver(sMessageReceiver);
  }

  private class BatteryCheckAsync extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      //Toast.makeText(BluetoothService.this, "got here...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Void... arg0) {
      while (!chargeDone) {
        // Battery State check - create log entries of current battery state
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        try {
          if (batLevel == 72) {
            sendData("1");
            //stopForeground(true);
            //stopSelf();
            Intent intent1 = new Intent("fgsdgdfgg");
            sendBroadcast(intent1);
            // Intent intent2 = new Intent("i'm audi");
            // sendBroadcast(intent2);
            chargeDone = true;
            return null;
          }
        } catch (Exception e) {

        }
      }
      return null;
    }

    protected void onPostExecute() {
      Toast.makeText(BluetoothService.this, "it finished", Toast.LENGTH_SHORT).show();
    }
  }

  private void startInForeground() {
    Intent notificationIntent = new Intent(this, ledControl.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Service")
            .setContentText("Running...")
            .setSmallIcon(R.drawable.ic_android)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build();
    
    startForeground(1, notification);
  }

  private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel serviceChannel = new NotificationChannel(
              CHANNEL_ID,
              "Foreground Service Channel",
              NotificationManager.IMPORTANCE_DEFAULT
      );
      NotificationManager manager = getSystemService(NotificationManager.class);
      manager.createNotificationChannel(serviceChannel);
    }
  }
}
