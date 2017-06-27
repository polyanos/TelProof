package com.example.gregor.telproof;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_LOCATION_ACCESS = 2;

    private BluetoothLeScanner bluetoothLeScanner;
    private Map<String,BluetoothConnection> connectionList;
    private int updateNr;
    private Callback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionList = new HashMap<>();
        updateNr = 0;
        callback = new Callback();

        Button btn = (Button)findViewById(R.id.stop_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopBluetoothScan();
            }
        });

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_ACCESS);
        }else {
            startBluetoothScan();
        }
    }

    public BluetoothAdapter createConnection(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        return bluetoothAdapter;
    }

    public void startBluetoothScan(){
        List<ScanFilter> filterList = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0).build();
        ScanFilter filter = new ScanFilter.Builder().build();
        filterList.add(filter);


        BluetoothAdapter bluetoothAdapter = createConnection();

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(filterList, settings, callback);
    }

    public void stopBluetoothScan(){
        bluetoothLeScanner.stopScan(callback);
    }

    public void addBluetoothBeacon(ScanResult bluetoothPoint){
        // && bluetoothPoint.getScanRecord().getDeviceName().contains("INF2A")
        String key = bluetoothPoint.getDevice().getName();
        if(connectionList.containsKey(key)) {
            connectionList.get(key).setStrength(bluetoothPoint.getRssi());
        } else {
            BluetoothConnection conn = new BluetoothConnection(bluetoothPoint.getDevice().getName(), bluetoothPoint.getRssi());
            connectionList.put(conn.getName(), conn);
        }
        updateNr++;
        displayMap();
    }

    public void displayMap(){
        TextView view = (TextView) findViewById(R.id.connectionList);
        StringBuilder strBuilder = new StringBuilder();
        if(connectionList.size() > 0) {
            for (BluetoothConnection conn : connectionList.values()) {
                strBuilder.append(conn.getName());
                strBuilder.append(" ");
                strBuilder.append(conn.getStrength());
                strBuilder.append(" dBm");
                strBuilder.append(System.getProperty("line.separator"));
            }
        } else {
            strBuilder.append("No devices found.").append(System.getProperty("line.separator"));
        }
        strBuilder.append("Update nr = ").append(updateNr);
        view.setText(strBuilder.toString());
    }

    public void showError(String error){
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_LOCATION_ACCESS){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startBluetoothScan();
            } else{
                Toast.makeText(this, "Application needs to access location services, the app will now exit", Toast.LENGTH_LONG).show();
                System.exit(0);
            }
        }
    }

    private class Callback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addBluetoothBeacon(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for(ScanResult result : results){
                addBluetoothBeacon(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            showError(String.valueOf(errorCode));
        }
    }
}
