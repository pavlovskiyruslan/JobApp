package com.example.levelgauge;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button btnScan;
    private Button btnDisconnect;
    private boolean mScanning;
    private static final long SCAN_PERIOD = 10000;
    private final Map<BluetoothDevice, Integer> mBtDevices = new HashMap<>();
    private TableLayout mTableDevices;
    private TextView tvStatusTop;
    private TextView tvReceivedData;

    // Tag used for logging
    private static final String TAG = "MainActivity";

    // BLE
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothLeScanner mBtScanner = null;
    private BluetoothGatt mBluetoothGatt;
    private final HashMap<String, BluetoothGattCharacteristic> mGattCharacteristics = new HashMap<>();
    private final HashMap<String, String> gattAttributes = new HashMap<>();

    public final String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public final String ACTION_WRITE_SUCCESS = "ACTION_WRITE_SUCCESS";

    // Request codes
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE = 1;
    private final static int MY_PERMISSIONS_REQUEST_ENABLE_BT = 2;

    //public static String LED_SERVICE = "c4913d0c-65d6-11eb-ae93-0242ac130002";
    public static String LED_SERVICE = "F0001110-0451-4000-B000-000000000000";
    public static String BUTTON_SERVICE = "F0001120-0451-4000-B000-000000000000";
    public static String DATA_SERVICE = "F0001130-0451-4000-B000-000000000000";
    public static String LED0_STATE = "F0001111-0451-4000-B000-000000000000";
    //public static String LED1_STATE = "c4913f50-65d6-11eb-ae93-0242ac130002";
    public static String LED1_STATE = "F0001112-0451-4000-B000-000000000000";
    public static String BUTTON0_STATE = "F0001121-0451-4000-B000-000000000000";
    public static String BUTTON1_STATE = "F0001122-0451-4000-B000-000000000000";
    public static String STRING_CHAR = "F0001131-0451-4000-B000-000000000000";
    public static String STREAM_CHAR = "F0001132-0451-4000-B000-000000000000";

    //public final UUID UUID_SVR_MAIN_SERVICE_DESCRIPTOR = UUID.fromString("c4913d0c-65d6-11eb-ae93-0242ac130002"); // UUID for notification descriptor
    public final UUID UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // UUID for notification descriptor

    public final static UUID UUID_BUTTON_SERVICE = UUID.fromString(BUTTON_SERVICE);
    //public final UUID UUID_DATA_CHARACTERISTIC = UUID.fromString("F0001131-0451-4000-B000-000000000000"); // UUID for DATA SERVICE

    // Intent extras
    public final static String EXTRA_DATA = "EXTRA_DATA";
    private final String DEVICE_NAME = "Urovnemer";

    private final Queue<BluetoothGattCharacteristic> characteristicQueue = new LinkedList<>();
    private final Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<>();

    private final ArrayList<ScanFilter> mScanFilters = new ArrayList<>();
    private ScanSettings mScanSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.txt_layout);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = btManager.getAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show explanation on why this is needed
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can discover bluetooth devices");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                MY_PERMISSIONS_REQUEST_ACCESS_COARSE);
                    }
                });
                builder.show();

            } else {
                // Prompt user for location access
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE);
            }
        }

        tvReceivedData = findViewById(R.id.tvReceivedData);
        tvStatusTop = findViewById(R.id.tvStatusTop);
        mTableDevices = findViewById(R.id.devicesFound);

        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice(!mScanning);
            }
        });

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        // Services
        gattAttributes.put(LED_SERVICE.toLowerCase(), "Led Service");
        gattAttributes.put(BUTTON_SERVICE.toLowerCase(), "Button Service");
        gattAttributes.put(DATA_SERVICE.toLowerCase(), "Data Service");
        // Characteristics
        gattAttributes.put(LED0_STATE.toLowerCase(), "Led0 State");
        gattAttributes.put(LED1_STATE.toLowerCase(), "Led1 State");
        gattAttributes.put(BUTTON0_STATE.toLowerCase(), "Button0 State");
        gattAttributes.put(BUTTON1_STATE.toLowerCase(), "Button1 State");
        gattAttributes.put(STRING_CHAR.toLowerCase(), "String char");
        gattAttributes.put(STREAM_CHAR.toLowerCase(), "Stream char");

        btnDisconnect = findViewById(R.id.btnDisconnect);
        btnDisconnect.setEnabled(false);
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        ScanFilter filter = new ScanFilter.Builder().setDeviceName(DEVICE_NAME).build();
        mScanFilters.add(filter);

        // Configure default scan settings
        mScanSettings = new ScanSettings.Builder().build();
    }

//    private class IterateDevicesTask extends TimerTask {
//
//        @Override
//        public void run() {
//            broadcastUpdate(ACTION_TIMER_TIMEOUT);
//        }
//    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_GATT_CONNECTED:
                    tvStatusTop.setText("Connected");
                    btnDisconnect.setEnabled(true);
                    break;
                case ACTION_GATT_DISCONNECTED:
//                    stopTimer();
//                    dataCharacteristic = null;
                    tvStatusTop.setText("Disconnected");
                    btnDisconnect.setEnabled(false);
                    tvReceivedData.setText("0 cm");
                    break;
                case ACTION_GATT_SERVICES_DISCOVERED:
//                    BluetoothGattService dataService = getGattServiceByUuid(UUID_DATA_SERVICE);
//                    if (dataService != null) {
//                        dataCharacteristic = dataService.getCharacteristic(UUID_DATA_CHARACTERISTIC);
//                        if (dataCharacteristic != null) {
//                            timer = new Timer();
//                            timer.schedule(new IterateDevicesTask(), 0, 1000);
//                        }
//
//                    }
                    initializeGattServiceUIElements(getSupportedGattServices());
                    break;
                case ACTION_DATA_AVAILABLE:
                    byte[] receivedData = intent.getByteArrayExtra(EXTRA_DATA);
//                    StringBuilder sb = new StringBuilder();
//                    for (int i = 0; i < receivedData.length; i++) {
//                        sb.append(String.format("%02x", receivedData[i]));
//                    }
//                    tvReceivedData.setText(sb.toString());
                    String receivedValueStr = "0 cm";
                    if (receivedData.length > 0) {
                        short dis = getDistance(receivedData);
                        receivedValueStr = String.valueOf(dis) + " cm";
                        if (dis > 500) {
                            receivedValueStr = "- - - - -";
                        }
                    }
                    tvReceivedData.setText(receivedValueStr);
                    break;
//                case ACTION_TIMER_TIMEOUT:
//                    if (dataCharacteristic != null) {
//                        readCharacteristic(dataCharacteristic);
//                    }
//                    break;
            }
        }
    };

//    private void stopTimer() {
//        timer.cancel();
//        timer.purge();
//    }

    private short getDistance(byte[] arr) {
        byte[] tmpArr = new byte[2];
        tmpArr[0] = arr[0];
        tmpArr[1] = arr[1];
        return (short) (ByteBuffer.wrap(tmpArr).getShort() / 10);
    }

    private void initializeGattServiceUIElements(List<BluetoothGattService> gattServices) {
//        String uuid;
//        String serviceName;

        mGattCharacteristics.clear();

        Set<String> discoveredServiceUuids = new HashSet<>();
        for (BluetoothGattService s : gattServices) {
            discoveredServiceUuids.add(s.getUuid().toString());
            for (BluetoothGattCharacteristic c : s.getCharacteristics()) {
                discoveredServiceUuids.add(c.getUuid().toString());
            }
        }

//        for (BluetoothGattService gattService : gattServices) {
//            uuid = gattService.getUuid().toString();
//            String serviceGattValue = gattAttributes.get(uuid);
//            serviceName = (serviceGattValue == null) ? "DefaultServiceName" : serviceGattValue;
//
//            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
//            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                uuid = gattCharacteristic.getUuid().toString();
//                String characteristicGattValue = gattAttributes.get(uuid);
//                String characteristicName = (characteristicGattValue == null) ? "DefaultServiceName" : characteristicGattValue;
//
//                if (serviceName.contains("Led")) {
//                    Switch sw;
//                    if (characteristicName.contains("Led1")) {
//                        sw = findViewById(R.id.swGreenLed);
//                    } else {
//                        continue;
//                    }
//
//                    if (sw != null) {
//                        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                                // Write value to 1 if button is checked, and to 0 otherwise
//                                byte[] value = new byte[1];
//                                if (isChecked) {
//                                    value[0] = (byte) (1 & 0xFF);
//                                } else {
//
//                                    value[0] = (byte) (0 & 0xFF);
//                                }
//
//                                // Write value
//                                gattCharacteristic.setValue(value);
//                                writeCharacteristic(gattCharacteristic);
//                            }
//                        });
//                    }
//                    readCharacteristic(gattCharacteristic);
//                }
//            }
//        }
    }

    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBtAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "Bluetooth not initialized");
            return;
        }

        // Queue the characteristic to read, since several reads are done on startup
        characteristicQueue.add(characteristic);

        // If there is only 1 item in the queue, then read it. If more than 1, it is handled
        // asynchronously in the callback
        if ((characteristicQueue.size() == 1)) {
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }

    private void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBtAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        if (mBtAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "Bluetooth not initialized");
            return;
        }
        // Enable/disable notification
        mBluetoothGatt.setCharacteristicNotification(characteristic, enable);

        // Write descriptor for notification
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR);
        descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00, 0x00});
        writeGattDescriptor(descriptor);
    }

    private void writeGattDescriptor(BluetoothGattDescriptor d) {
        // Add descriptor to the write queue
        descriptorWriteQueue.add(d);
        // If there is only 1 item in the queue, then write it. If more than 1, it will be handled
        // in the onDescriptorWrite callback
        if (descriptorWriteQueue.size() == 1) {
            mBluetoothGatt.writeDescriptor(d);
        }
    }

    private List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }

        return mBluetoothGatt.getServices();
    }

    private BluetoothGattService getGattServiceByUuid(UUID uuid) {
        if (mBluetoothGatt == null) {
            return null;
        }

        return mBluetoothGatt.getService(uuid);
    }

    private void scanLeDevice(final boolean enable) {
        if (mBtScanner == null) {
            mBtScanner = mBtAdapter.getBluetoothLeScanner();
        }

        if (enable) {
            mBtDevices.clear();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBtScanner.stopScan(mScanCallback);
                    invalidateScanButton();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBtScanner.startScan(mScanFilters, mScanSettings, mScanCallback);
        } else {
            mScanning = false;
            mBtScanner.stopScan(mScanCallback);
        }
        invalidateScanButton();
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final BluetoothDevice btDevice = result.getDevice();
                    if (btDevice == null) {
                        Log.e("ScanCallback", "Could not get bluetooth device");
                        return;
                    }

                    String macAddress = btDevice.getAddress();
                    for (BluetoothDevice dev : mBtDevices.keySet()) {
                        if (dev.getAddress().equals(macAddress)) {
                            return;
                        }
                    }
                    mBtDevices.put(btDevice, result.getRssi());

                    updateDeviceTable();
                }
            });
        }
    };

    private void updateDeviceTable() {
        mTableDevices.removeAllViews();

        for (final BluetoothDevice savedDevice : mBtDevices.keySet()) {

            // Get RSSI of this device
            int rssi = mBtDevices.get(savedDevice);

            // Create a new row
            final TableRow tr = new TableRow(MainActivity.this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
            tr.setGravity(Gravity.CENTER);

            // Add Text view for rssi
            TextView tvRssi = new TextView(MainActivity.this);
            tvRssi.setText(rssi + " dBm");
            TableRow.LayoutParams params = new TableRow.LayoutParams(0);
            params.setMargins(20, 0, 20, 0);
            tvRssi.setLayoutParams(params);

            TextView tvEmpty1 = new TextView(MainActivity.this);
            tvEmpty1.setText("");
            tvEmpty1.setLayoutParams(new TableRow.LayoutParams(1));

            // Add Text view for device, displaying name and address
            TextView tvDevice = new TextView(MainActivity.this);
            String devName = savedDevice.getName() != null ? savedDevice.getName() : "Unidentified";
            tvDevice.setText(devName + "\r\n" + savedDevice.getAddress());
            tvDevice.setLayoutParams(new TableRow.LayoutParams(2));
            tvDevice.setGravity(Gravity.CENTER);

            TextView tvEmpty2 = new TextView(MainActivity.this);
            tvEmpty2.setText("");
            tvEmpty2.setLayoutParams(new TableRow.LayoutParams(3));

            // Add a connect button to the right
            Button b = new Button(MainActivity.this);
            b.setText("Connect");
            b.setGravity(Gravity.CENTER);

            // Create action when clicking the connect button
            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    connectToDevice(savedDevice);
                }
            });

            // Add items to the row
            tr.addView(tvRssi);
            tr.addView(tvEmpty1);
            tr.addView(tvDevice);
            tr.addView(tvEmpty2);
            tr.addView(b);

            // Add row to the table layout
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTableDevices.addView(tr);
                }
            });
        }
    }

    public void connectToDevice(BluetoothDevice btDevice) {

        if (mBluetoothGatt == null) {
            mBluetoothGatt = btDevice.connectGatt(this, false, mGattCallback);

            // Stop scanning
            if (mScanning) {
                scanLeDevice(false);
            }
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        private void enableButtonNotifications(BluetoothGatt gatt) {
            // Loop through the characteristics for the button service
            //for (BluetoothGattCharacteristic characteristic : gatt.getService(UUID_SVR_MAIN_SERVICE_DESCRIPTOR).getCharacteristics()) {
            for (BluetoothGattCharacteristic characteristic : gatt.getService(UUID_BUTTON_SERVICE).getCharacteristics()) {
                // Enable notification on the characteristic
                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    setCharacteristicNotification(characteristic, true);
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    intentAction = ACTION_GATT_CONNECTED;
                    broadcastUpdate(intentAction);
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i("gattCallback", "STATE_DISCONNECTED");
                    intentAction = ACTION_GATT_DISCONNECTED;
                    broadcastUpdate(intentAction);
                    // Close connection completely after disconnect, to be able
                    // to start clean.
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.close();
                        mBluetoothGatt = null;
                    }
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "onServicesDiscovered: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                enableButtonNotifications(gatt);
                //gatt.requestMtu(100);
            } else {
                Log.w(TAG, "onServicesDiscovered received with error: " + status);
            }
        }

//        @Override
//        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                enableButtonNotifications(gatt);
//            }
//        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            characteristicQueue.remove();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            } else {
                Log.d(TAG, "onCharacteristicRead error: " + status);
            }

            // Handle the next element from the queues
            if (characteristicQueue.size() > 0)
                mBluetoothGatt.readCharacteristic(characteristicQueue.element());
            else if (descriptorWriteQueue.size() > 0)
                mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Broadcast data written to the data service string characteristic
                if ((UUID.fromString(STRING_CHAR)).equals(characteristic.getUuid())) {
                    broadcastUpdate(ACTION_WRITE_SUCCESS);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // Broadcast the received notification
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Callback: Error writing GATT Descriptor: " + status);
            }

            // Pop the item that we just finishing writing
            descriptorWriteQueue.remove();

            // Continue handling items if there is more in the queues
            if (descriptorWriteQueue.size() > 0)
                mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
            else if (characteristicQueue.size() > 0)
                mBluetoothGatt.readCharacteristic(characteristicQueue.element());
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, MY_PERMISSIONS_REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Coarse location permission granted");
                } else {
                    // Access location was not granted. Display a warning.
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not display any bluetooth scan results.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                    btnScan.setEnabled(false);
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_PERMISSIONS_REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // Bluetooth was not enabled, end activity
                finish();
                return;
            }
        }
    }

    private void invalidateScanButton() {
        if (!mScanning) {
            btnScan.setText("Scan");
        } else {
            btnScan.setText("Stop");
        }
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, data);
        }
        sendBroadcast(intent);
    }

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        intentFilter.addAction(ACTION_WRITE_SUCCESS);
        //intentFilter.addAction(ACTION_TIMER_TIMEOUT);
        return intentFilter;
    }

    public void disconnect() {
        if (mBtAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "Bluetooth not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }
}