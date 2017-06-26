package com.example.gregor.telproof;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_ENABLE_BT = 1;

    private BluetoothLeScanner bluetoothLeScanner;
    private Map<String,BluetoothConnection> connectionList;
    private int updateNr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionList = new HashMap<>();
        updateNr = 0;

        createConnection();
        startBluetoothScan();
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
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

        ScanCallback callback = new ScanCallback() {
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
                super.onScanFailed(errorCode);
            }
        };

        BluetoothAdapter bluetoothAdapter = createConnection();

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(callback);
    }

    public void stopBlueABoolean(){
        bluetoothLeScanner.stopScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {}
            @Override
            public void onBatchScanResults(List<ScanResult> results) {}
            @Override
            public void onScanFailed(int errorCode) {}
        });
    }

    public void addBluetoothBeacon(ScanResult bluetoothPoint){
        if(bluetoothPoint.getScanRecord() != null && bluetoothPoint.getScanRecord().getDeviceName() != null && bluetoothPoint.getScanRecord().getDeviceName().contains("Adafruit BLE Friend"))
        {
            BluetoothConnection conn = new BluetoothConnection(bluetoothPoint.getScanRecord().getDeviceName(), bluetoothPoint.getRssi());
            connectionList.put(conn.getName(), conn);
            updateNr++;
        }
        displayMap();
    }

    public void displayMap(){
        TextView view = (TextView) findViewById(R.id.connectionList);
        StringBuilder strBuilder = new StringBuilder();
        if(connectionList.size() > 0) {
            for (BluetoothConnection conn : connectionList.values()) {
                strBuilder.append(conn.getName());
                strBuilder.append(" - ");
                strBuilder.append(conn.getStrength());
                strBuilder.append(" dBm");
                strBuilder.append(System.getProperty("line.separator"));
            }
        } else {
            strBuilder.append("No devices found.");
        }
        view.setText(strBuilder.toString());
    }
}
