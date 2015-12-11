package com.example.ayami.sensortagtest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;


import android.app.Activity;
import android.bluetooth.*;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
//↓これなに？
// import jp.co.fenrir.BleSample.R;

import java.util.UUID;

public class BleActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback{
    private static final long SCAN_PERIOD =15000; // BLEスキャンのタイムアウト(ミリ秒)
    private static final String DEVICE_NAME = "CC2650 SensorTag";//機器の名前
    private static final String DEVICE_MOVEMENT_SERVICE_UUID ="F000AA80-0451-4000-B000-000000000000";
    private static final String DEVICE_MOVEMENT_DATA_UUID ="F000AA81-0451-4000-B000-000000000000 ";
    private static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    //サービス名はAccelerometer Serviceっぽい。キャラクタはどれかよくわからないorz

    private static final String TAG ="BLESample";
//    private BleStatus mStatus = BleStatus.DISCONNECTED;
    private Handler mHandler ;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    private TextView mStatusText;
    private final static int REQUEST_ENABLE_BT = 0;
//    private BleStatus nowStatus;
     //このテキストは☆をあらわしてる

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //mStatusText = (TextView)findViewById(R.id.text_status);

        mHandler = new Handler();

        System.out.println("すたーと！");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        findViewById(R.id.connectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("☆ボタンおしたよ！");
                connect();
            }
        });
        findViewById(R.id.disconnectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("☆ボタンおしたよ！");
                disconnect();
            }
        });

        //Bluetoothが接続が有効かチェック。上手くいかないときは、ダイアログ表示。なぜがgetAdapterが動かないので保留
        BluetoothManager manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        if (mBluetoothAdapter==null || !mBluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }



    }

    private void connect(){
        //BLE機器の検索
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //タイムアウトの処理
                mBluetoothAdapter.stopLeScan(BleActivity.this);
                Log.e("postDelayed","☆timeout!");
            }
        }, SCAN_PERIOD);
        mBluetoothAdapter.stopLeScan(this);
        mBluetoothAdapter.startLeScan(this);
        Log.i("postDelayed", "☆Scanning");
    }

    //BLE機器との接続を解除するもの
    private void disconnect(){
        if (mBluetoothGatt !=null){
            mBluetoothGatt.close();
            mBluetoothGatt=null;
            Log.i("disconnect", "☆mBluetoothGatt="+mBluetoothGatt.toString());
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device,int rssi,byte[] scanRecord){
        //機器との接続
        Log.d(TAG, "☆device found:" + device.getName());
        //機器名がSensorTagと一致するものを探す
        if(DEVICE_NAME.equals(device.getName())){
        Log.i("onLeScan","☆device Connecting");
            mBluetoothAdapter.stopLeScan(this);
            //機器が見つかれば即スキャンを停止(電力消費を抑えるため)
            mBluetoothGatt = device.connectGatt(this,false,mBluetoothGattCallback);
            //bluetoothと接続！　２番目をfalseにするとす、すぐに接続開始
            //雪辱完了や危機からの受信はBluetoothGattCalbackで処理

        }
    }
    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        //connectGatt()が成功するとこいつ↓が自動で呼ばれる。ここでサービスを検出
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "☆onConnectionStateChange:" + status + "->" + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //GaTT接続成功しているので、サービスを検索する
                gatt.discoverServices();
                //サービス検索の成否はmBluetoothGattCallback.onServiceDiscoverdで受ける
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mBluetoothGatt = null;
                Log.e("onConnectionStateChange","☆Gattが切れちゃった!");

            }
        }

        @Override
        //サービスの検出をする
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "☆onServiceDiscoverd received" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(UUID.fromString(DEVICE_MOVEMENT_SERVICE_UUID));
//                BluetoothGattService service = gatt.getService(UUID.fromString(DEVICE_MOVEMENT_SERVICE_UUID));
                //サービスを見つけたか判定
                if (service == null) {
                    Log.e("onServicesDiscovered", "☆service is not found!");
                } else {
                    Log.i("onServicesDiscovered", "☆service is founded");
                    System.out.println("☆service=" + service.toString());

                    System.out.println("☆characteristic = "+ service.getCharacteristic(UUID.fromString(DEVICE_MOVEMENT_DATA_UUID)).toString());

                    //BluetoothGattCharacteristic characteristic =null;


                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(DEVICE_MOVEMENT_DATA_UUID));
                    Log.i("onServicesDiscovered", "☆キャラクタリスティックを探しているよ");


                    //キャラクタリスティックを見つけたか判定
                    if (characteristic == null) {
                        Log.e("onServicesDiscovered", "☆characteristic is not found!");
                    } else {
                        Log.i("onServicesDiscovered", "☆characteristic is founded");
                        //Notificationを要求する
                        // ↓これ何だろ。あとて調べよっと
                        boolean registered = gatt.setCharacteristicNotification(characteristic, true);

                        //CharacteristicのNotification有効化
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                        //↓通知設定が完了したかどうかチェック
                        if (registered) {
                            Log.i("onServicesDiscovered", "☆Notification is registered");
                        } else {
                            Log.e("onServicesDiscovered", "☆Notification register failed!");
                        }
                    }

                }

            }


        }

        @Override
        //Notificationを受信し状態を取得する
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "☆onCharacteristicRead:" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                System.out.println("☆READ成功");
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "☆onCharacteristicChanged");
            //Characteristicの値更新通知

            if (DEVICE_MOVEMENT_DATA_UUID.equals(characteristic.getUuid().toString())) {
                System.out.println("☆通知あり！");
                Byte value = characteristic.getValue()[0];
                System.out.println(value.toString());
                //通知がある時の処理
            }

        }
    };
/*
    private void setStatus(BleStatus status) {
        mStatus = status; mHandler.sendMessage(status.message());
    }
    private enum BleStatus {
        DISCONNECTED, SCANNING, SCAN_FAILED, DEVICE_FOUND, SERVICE_NOT_FOUND, SERVICE_FOUND,
        CHARACTERISTIC_NOT_FOUND, NOTIFICATION_REGISTERED, NOTIFICATION_REGISTER_FAILED, CLOSED ;
        public Message message() {
            Message message = new Message();
            message.obj = this;
            return message;
        }
    }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ble, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}