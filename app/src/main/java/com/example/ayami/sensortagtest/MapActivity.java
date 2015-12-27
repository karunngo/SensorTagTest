package com.example.ayami.sensortagtest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View.OnClickListener;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class MapActivity extends AppCompatActivity {
    LocationManager manager;
    LocationListener localistener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
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
        findViewById(R.id.addGPSButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("☆GPSボタンおしたよ！");

            }
        });

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        localistener = new LocationListener() {
            //onLocationChange:位置情報が変化した時に呼び出される
            public void onLocationChanged(Location location) {
                Log.i("☆", "位置情報取得成功");
                String latitude = Double.toString(location.getLatitude());
                String longitude = Double.toString(location.getLongitude());
                System.out.println("☆緯度" + latitude + "経度" + longitude);
            }

            public void onProviderEnabled(String procider) {
            }

            public void onProviderDisabled(String procider) {
            }

            public void onStatusChanged(String provider, int Status, Bundle extras) {
            }
        };
    }

    @Override
    //リスナーを登録
    protected void onResume() {
        super.onResume();
        //Locationマネージャーにリスナーを設定
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, localistener);
    }
}
