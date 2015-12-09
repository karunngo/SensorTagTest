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

public class BleActivity extends AppCompatActivity implements
BluetoothAdapter.LeScanCallback{
//Bluetoothが接続が有効かチェック。上手くいかないときは、ダイアログ表示。なぜがgetAdapterが動かないので保留
    BluetoothManager manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
    BluetoothAdapter mBluetoothAdapter = manager.getAdapter();
    if (mBluetoothAdapter==null || !mBluetoothAdapter.inEnabled()){
    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);



   private static final long SCAN_PERIOD =10000; // BLEスキャンのタイムアウト(ミリ秒)
    private static final String DEVICE_NAME = "SensorTag";//機器の名前
    //キャラクタリスティック、サービスのUUIDを調べ、String で登録すべし

        //BLE機器の検索
    private void connect(){
        mHandler.postDelayed(new Runnable(){
            @Override
        public void run(){
                //タイムアウトの処理
                mBluetoothAdapter.stopLeScan(BleActivity.this);
            }
        },SCAN_PERIOD);
        mBluetoothAdapter.startLeScan(this);
    }

    //機器との接続
    @Override
    public void onLeScan(BluetoothDevice device,int rssi,byte[] scanRecord){
        Log.d(TAG, "device found:" + device.getName());
        if("SensorTag".equals(device.getName())){
            //機器名がSensorTagと一致するものを探す

            mBluetoothAdapter.stopLeScan(this);
            //機器が見つかれば即スキャンを停止(電力消費を抑えるため)

            mBluetoothGatt = device.connectGatt(getApplicationContext(),false,mBluetoothGattCallback);
            //bluetoothと接続！　２番目をfalseにするとす、すぐに接続開始
            //雪辱完了や危機からの受信はBluetoothGattCalbackで処理

        }
    }

    //connectGatt()が成功するとこいつ↓が自動で呼ばれる。ここでサービスを検出
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt,int status,int newState){
        Log.d(TAG,"onConnectionStateChange:"+status+"->"+newState);

        if(newState== BluetoothProfile.STATE_CONNECTED) {
            //GaTT接続成功しているので、サービスを検索する
            gatt.discoverServices();
            //サービス検索の成否はmBluetoothGattCallback.onServiceDiscoverdで受ける
            }else if(newState==BluetoothProfile.STATE_DISCONNECTED){
            mBluetoothGatt = null;
        }
    }

}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    }

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
