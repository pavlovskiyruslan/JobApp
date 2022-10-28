package com.example.levelgauge;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Iterator;


import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    SwitchCompat switchCompat;
    private Button btnScan;
    private Button btnDisconnect;
    private boolean mScanning;
    private static final long SCAN_PERIOD = 10000;
    private final Map<BluetoothDevice, Integer> mBtDevices = new HashMap<>();
    private TableLayout mTableDevices;
    public TextView tvStatusTop;
    private TextView tvReceivedData;
    private TextView txt_path;
    private Button btn_menustatus;
    private Button btn_phonenumber;
    private Button btndata;
    private Button btnhigh;
    private Button btncorrection;
    private Button btn_checkfile;
    private Button btn_settings;
    private EditText entercorrection;
    private Button btn_folder;
    private Button btn_ipadres;
    private ImageButton btn_sendtodevice;
    private Button btn_sendfilepath;
    private TextView tv_menustatus;
    private String path;
    public String temp_path = "/storage/emulated/0/";
    public String file_path;
    private static final int LONG_DELAY = 3500; // 3.5 seconds
    ProgressDialog progressDialog;
    // Tag used for logging
    private static final String TAG = "MainActivity";
    private Button btn_sentdata;
    // BLE
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeScanner mBtScanner = null;
    public BluetoothGatt mBluetoothGatt;
    private Handler mHandler = new Handler();
    private final HashMap<String, BluetoothGattCharacteristic> mGattCharacteristics = new HashMap<>();
    private final HashMap<String, String> gattAttributes = new HashMap<>();
    public final String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final String TEST_MESSAGE = "TEST_MESSAGE";
    private static final String ACTION_SCAN_TIMEOUT = "ACTION_SCAN_TIMEOUT";
    private static final String ACTION_DEVICE_NOT_FOUND = "ACTION_DEVICE_NOT_FOUND";
    public final String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public final String ACTION_WRITE_SUCCESS = "ACTION_WRITE_SUCCESS";
    private static final int FILE_SELECT_CODE = 0;
    public String fileName = null;
    //public final String ACTION_CHECK_CHARACTERISTICS = "ACTION_CHECK_CHARACTERISTICS";
    // Request codes
    private static final int REQUEST_ACCESS_COARSE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private final static int MY_PERMISSIONS_REQUEST_ENABLE_BT = 2;
    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;

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
    private Handler scanDelayedHandler;

    public int noOfRows;
    public int noOfColumns;
    public float [][] matrix;
    public static int gl_recei_len;
    public static String a;
    public static int tmparr_len;
    public static int get_flag;
    public final String DIR_SD = "Level Gauge";
    public final String FILENAME_SD = "History.txt";
    public int checksenttimes = 0;
    public String filerowstring = "7";
    public int amountofcells;
    public int curRow = 1;
    public int curCol = 1;
    public int indexM;
    private Boolean check_status;



    //public final UUID UUID_SVR_MAIN_SERVICE_DESCRIPTOR = UUID.fromString("c4913d0c-65d6-11eb-ae93-0242ac130002"); // UUID for notification descriptor
    public final UUID UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // UUID for notification descriptor
    public final static UUID UUID_BUTTON_SERVICE = UUID.fromString(BUTTON_SERVICE);
    public final static UUID UUID_LED_SERVICE = UUID.fromString(LED_SERVICE);
    public final static UUID UUID_LED0_STATE = UUID.fromString(LED0_STATE);
    public final static UUID UUID_LED1_STATE = UUID.fromString(LED1_STATE);
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
        entercorrection = findViewById(R.id.entercorrection);
        btn_sendtodevice = findViewById(R.id.btn_sendtodevice);
        btn_phonenumber = findViewById(R.id.btn_phonenumber);
        btn_menustatus = findViewById(R.id.btn_menustatus);
        //tv_menustatus = findViewById(R.id.tv_menustatus);
        invisible();

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.txt_layout);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(entercorrection.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);



        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
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
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                REQUEST_ACCESS_COARSE);
                    }
                });
                builder.show();

            } else {
                // Prompt user for location access
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        REQUEST_ACCESS_COARSE);
            }
        }
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        //    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
        //        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        //    } else {
        //        // Prompt user for location access
        //        ActivityCompat.requestPermissions(this,
        //               new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
        //                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        //    }
        //}

        switchCompat = findViewById(R.id.switchon);
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



        btn_settings = findViewById(R.id.btn_settings);
        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothGatt == null) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ПОЖАЛУЙСТА ПОДКЛЮЧИТЕСЬ К УСТРОЙСТВУ",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                Dialog Menudialog = new Dialog(MainActivity.this);
                Menudialog.setContentView(R.layout.dialog_scroll);
                Menudialog.setTitle("НАСТРОЙКИ");
                Button btncorrection = Menudialog.findViewById(R.id.btncorrection);
                btncorrection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Menudialog.dismiss();
                        btn_menustatus.setText("ЗАДАТЬ КОРРЕКЦИЮ");
                        btn_menustatus.setGravity(Gravity.CENTER);
                        makevissible();
                        sendcorrection();
                    }
                });
                Button btnhigh = Menudialog.findViewById(R.id.btnhigh);
                btnhigh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Menudialog.dismiss();
                        btn_menustatus.setText("ЗАДАТЬ ВЫСОТУ");
                        btn_menustatus.setGravity(Gravity.CENTER);
                        makevissible();
                        sendhigh();
                    }
                });


                Button btn_phonenumber = Menudialog.findViewById(R.id.btn_phonenumber);
                btn_phonenumber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Menudialog.dismiss();
                        btn_menustatus.setText("ИЗМЕНИТЬ НОМЕР ТЕЛЕФОНА");
                        btn_menustatus.setGravity(Gravity.CENTER);
                        makevissible();
                        sendphone();
                    }
                });


                Button btn_ipadres = Menudialog.findViewById(R.id.btn_ipadres);
                btn_ipadres.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Menudialog.dismiss();
                        btn_menustatus.setText("ЗАДАТЬ ВЫСОТУ");
                        btn_menustatus.setGravity(Gravity.CENTER);
                        makevissible();
                        ipadres();
                    }
                });
                SwitchCompat switchCompat = Menudialog.findViewById(R.id.switchon);
                switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            BluetoothGattService led_service = mBluetoothGatt.getService(UUID_LED_SERVICE);
                            BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);
                            byte[] value = new byte[1];
                            value[0] = (byte) ('n' & 0xFF);
                            tmpChar.setValue(value);
                            tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                            writeCharacteristic(tmpChar);
                            //When switch checked
                            switchCompat.setChecked(true);
                        }
                        else{
                            switchCompat.setChecked(false);
                            //When switch off
                        }

                }
                });


                Button btn_sendfilepath = Menudialog.findViewById(R.id.btn_sendfilepath);
                btn_sendfilepath.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Menudialog.dismiss();
                        showArrayofExcelFile("Вы хотите отправить файл ? ");
                    }
                });


                Button btn_data = Menudialog.findViewById(R.id.btndata);
                btn_data.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Menudialog.dismiss();
                            try {
                                Thread.sleep(1000); //Приостанавливает поток на 1 секунду
                            } catch (Exception e) {

                            }
                            if (mBluetoothGatt == null) {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "ПОЖАЛУЙСТА ПОДКЛЮЧИТЕСЬ К УСТРОЙСТВУ",
                                        Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return;
                            }
                            BluetoothGattService led_service = mBluetoothGatt.getService(UUID_LED_SERVICE);
                            mBluetoothGatt.requestMtu(50);
                            try {
                                Thread.sleep(1000); //Приостанавливает поток на 1 секунду
                            } catch (Exception e) {

                            }
                            if (led_service == null) {
                                return;
                            }
                            if(get_flag == 3){
                                BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);
                                byte[] value = new byte[1];
                                value[0] = (byte) ('x' & 0xFF);
                                tmpChar.setValue(value);
                                tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                                writeCharacteristic(tmpChar);
                                ShowProgressDialog();
                                return;
                            }
                            BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);
                            byte[] value = new byte[1];
                            value[0] = (byte) ('d' & 0xFF);
                            tmpChar.setValue(value);
                            tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                            writeCharacteristic(tmpChar);
                            ShowProgressDialog();

                    }
                });
                Button btn_folder = Menudialog.findViewById(R.id.btn_folder);
                btn_folder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Menudialog.dismiss();
                        showFileChooser();
                    }
                });
                Menudialog.show();

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
        mScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, MY_PERMISSIONS_REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        else if(requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK){
            Uri uri = data.getData();
            path = uri.getPath().split(":")[1];
            file_path = temp_path + path;
            ReadExcel();
            //((TextView) findViewById(R.id.tv_filepath)).setText(""+ file_path );
            //btn_folder.setText(file_path);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_COARSE) {
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
            }
        }
        if(requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
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
                    tvStatusTop.setText("Соединен");
                    btnDisconnect.setEnabled(true);
                    break;
                case ACTION_GATT_DISCONNECTED:
//                    stopTimer();
//                    dataCharacteristic = null;
                    invisible();
                    tvStatusTop.setText("Разъединен");
                    btnDisconnect.setEnabled(false);
                    tvReceivedData.setText(" ");
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
                    byte[] receivedMess = intent.getByteArrayExtra(EXTRA_DATA);
                    String receivedValueStr = "";
                    gl_recei_len = receivedMess.length;
                    System.out.println(gl_recei_len);
                    if (receivedMess.length > 0) {
                        StringBuilder dis = getDistance(receivedMess);
                        receivedValueStr = dis + "";
                        //if (dis > 500) {
                        //     receivedValueStr = "- - - - -";
                        // }
                    }

                    //String s = new String(receivedMess, StandardCharsets.UTF_8);

                    if (get_flag == 1) {
                        //tvReceivedData.append(receivedValueStr + "\n");
                        sentsuccess();
                        savetobd(receivedValueStr);
                    }
                    else if(get_flag == 2) {
                        get_flag = 0;
                        //((TextView) findViewById(R.id.txt_path)).setText("GET FLAG = 2");
                        amountofcells = noOfRows * noOfColumns + 1;
                        //for (int i = 0; i < noOfRows; i++) {
                        //    for (int j = 0; j < noOfColumns; j++) {
                                //((TextView) findViewById(R.id.txt_path)).append(matrix[i][j] + " ");
                                //System.out.print("[" + i +"]" + "[" + j + "]" +  matrix[i][j]);
                        //    }
                            //((TextView) findViewById(R.id.txt_path)).append("\n");
                            //System.out.println();
                        //}
                        checksenttimes++;

                        if(checksenttimes < amountofcells){
                            if(curRow < noOfRows - 1){
                                if(curCol < noOfColumns - 1){
                                    //a = "f" + curRow + curCol + matrix[curRow][curCol];
                                    a = "" + matrix[curRow][curCol];
                                    indexM = a.indexOf(".");
                                    System.out.println(indexM);
                                    if(indexM == 3){
                                        String r = "f" + a;
                                        curCol++;
                                        sentdata(r);
                                    }
                                    else if(indexM < 3){
                                        a = a.substring(6);
                                        a = "0" + a;
                                        String r = "f" + a;
                                        curCol++;
                                        sentdata(r);
                                    }
                                    else if(indexM > 3){
                                        a = a.substring(0);
                                        a = a + "0";
                                        String r = "f" + a;
                                        curCol++;
                                        sentdata(r);
                                    }

                                }
                                else if (curCol == noOfColumns - 1){
                                    //a = "f" + curRow + curCol + matrix[curRow][curCol];
                                    a = "" + matrix[curRow][curCol];
                                    indexM = a.indexOf(".");
                                    System.out.println(indexM);
                                    if(indexM == 3){
                                        String r = "f" + a;
                                        curCol++;
                                        sentdata(r);
                                    }
                                    else if(indexM < 3){
                                        a = a.substring(6);
                                        a = "0" + a;
                                        String r = "f" + a;
                                        curCol++;
                                        sentdata(r);
                                    }
                                    else if(indexM > 3){
                                        a = a.substring(0);
                                        a = a + "0";
                                        String r = "f" + a;
                                        curCol++;
                                        sentdata(r);
                                    }

                                }
                            }
                            else if(curRow < noOfRows){
                                if(curCol < noOfColumns - 1){
                                    //a = "f" + curRow + curCol + matrix[curRow][curCol];
                                    a = "" + matrix[curRow][curCol];
                                    indexM = a.indexOf(".");
                                    System.out.println(indexM);
                                    if(indexM == 3 && a.length() == 7){
                                        String r = "f" + a;
                                        curCol++;
                                        sentdata(r);
                                    }
                                    else if(indexM < 3){
                                        //a = a.substring(6);
                                        while((a.length() < 6) || (indexM < 3 )) {
                                            a = "0" + a;
                                            indexM = a.indexOf(".");
                                        }
                                        if(indexM == 3 && a.length() == 7){
                                            String r = "f" + a;
                                            curCol++;
                                            sentdata(r);
                                        }
                                        else if(indexM == 3 && a.length() != 7){
                                            while(a.length() != 7){
                                                a = a + "0";
                                                indexM = a.indexOf(".");
                                                String r = "f" + a;
                                                curCol++;
                                                sentdata(r);
                                            }
                                        }
                                    }
                                    else if(indexM > 3){
                                        a = a.substring(0);

                                        a = a + "0";
                                        String r = "f" + a;
                                        curCol++;
                                        sentdata(r);
                                    }
                                }
                                else if (curCol == noOfColumns - 1){
                                    //a = "f" + curRow + curCol + matrix[curRow][curCol];
                                    a = "" + matrix[curRow][curCol];
                                    indexM = a.indexOf(".");
                                    if(indexM == 3){
                                        String r = "f" + a;
                                        curRow++;
                                        sentdata(r);
                                        sentstop();
                                        StopProgressDialogSendFile();
                                    }
                                    else if(indexM < 3){
                                        a = a.substring(6);
                                        a = "0" + a;
                                        String r = "f" + a;
                                        curRow++;
                                        sentdata(r);
                                        sentstop();
                                        StopProgressDialogSendFile();
                                    }
                                    else if(indexM > 3){
                                        a = a.substring(0);
                                        a = a + "0";
                                        String r = "f" + a;
                                        curRow++;
                                        sentdata(r);
                                        sentstop();
                                        StopProgressDialogSendFile();
                                    }
                                    System.out.println(indexM);
                                    //curRow++;
                                    //sentdata(a);
                                    //sentstop();
                                    //StopProgressDialogSendFile();
                                }
                            }

                        }
                        else{
                            sentstop();
                            StopProgressDialogSendFile();
                            //StopProgressDialog();
                        }


                    }
                    else if(get_flag == 3){
                        System.out.println(receivedValueStr);
                        String[] output = receivedValueStr.split("v");
                        StringBuffer sb = new StringBuffer(output[1]);
                        sb.insert(1,".");
                        tvReceivedData.setText(output[0] + "СМ" + "       " + sb + "V");

                        //tvReceivedData.setText( + "СМ");
                        BluetoothGattService led_service = mBluetoothGatt.getService(UUID_LED_SERVICE);
                        BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);
                        byte[] value = new byte[1];
                        value[0] = (byte) ('n' & 0xFF);
                        tmpChar.setValue(value);
                        tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                        writeCharacteristic(tmpChar);
                        get_flag = 0;
                    }
                    else
                    {
                        sentstop();
                        //StopProgressDialog();
                    }
                    break;
//              case ACTION_TIMER_TIMEOUT:
//                    if (dataCharacteristic != null) {
//                        readCharacteristic(dataCharacteristic);
//                    }
//                    break;
                //case ACTION_CHECK_CHARACTERISTICS:
                //    int checkData = intent.getIntExtra(EXTRA_DATA, 777);
                //    ((TextView) findViewById(R.id.textView2)).setText(String.valueOf(checkData));
                //    //((TextView) findViewById(R.id.textView2)).setText(""+ checkData );
                //    break;


                case TEST_MESSAGE:
                    tvReceivedData.setText("MTU success !");
                    break;
            }
        }
    };

    //    private void stopTimer() {
//        timer.cancel();
//        timer.purge();
//    }


    private void ReadExcel() {

        try {
            File file = new File(file_path);   //creating a new file instance
            FileInputStream fis = new FileInputStream(file);   //obtaining bytes from the file
            //creating Workbook instance that refers to .xlsx file
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            Sheet sheet = wb.getSheetAt(0);
            noOfRows = sheet.getPhysicalNumberOfRows();
            noOfColumns = sheet.getRow(0).getLastCellNum();

            int numRow = noOfRows - 1;
            int numCol = noOfColumns - 1;
            System.out.println("Number of Colums = :" + noOfRows);
            System.out.println("Number of Colums = :" + noOfColumns);

            matrix = new float[noOfRows][noOfColumns];
            // Iterate through each row
            Iterator<Row> rowIterator = sheet.iterator();

            // Traversing over each row of XLSX file
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int curRowNumber = row.getRowNum();
                // For each row, iterate through each columns
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int curColNumber = cell.getColumnIndex();
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_STRING:
                            System.out.print(cell.getStringCellValue() + "\t");
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            String s = String.valueOf(cell.getNumericCellValue());
                            matrix[curRowNumber][curColNumber] = (float) cell.getNumericCellValue();
                            System.out.print(cell.getNumericCellValue() + "\t");
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            System.out.print(cell.getBooleanCellValue() + "\t");
                            break;
                        default:
                    }
                }
                System.out.println("");
            }

//creating a Sheet object to retrieve object
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private StringBuilder sentdata(String a) {
        //System.out.println(a);
        char c = 0;
        StringBuilder strBuilder = new StringBuilder();
        byte[] value = new byte[a.length()];
        for (int i = 0; i < a.length(); i++) {
            c = a.charAt(i);
            System.out.println(c);
            System.out.println(c);
            strBuilder.append(c);

            value[i] = (byte) (a.charAt(i));
        }
        BluetoothGattService led_service = mBluetoothGatt.getService(UUID_LED_SERVICE);
        BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);

        tmpChar.setValue(value);
        tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        writeCharacteristic(tmpChar);
        return strBuilder;
    }


    private StringBuilder getDistance(byte[] arr) {
        byte[] tmpArr = new byte[gl_recei_len];
        char ch = 0;
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < gl_recei_len; i++) {
            tmpArr[i] = arr[i];
        }
        tmparr_len = tmpArr.length;
        if (tmpArr[0] == 117){
            System.out.println("GETT DATA");

            for (int i = 1; i < 8; i++) {
                ch = (char) tmpArr[i];

                //System.out.println(i);
                //System.out.println(ch);
                strBuilder.append(ch);
            }
                get_flag = 3;
                return strBuilder;
        }
        if (tmpArr[25] == 88) {
            tmparr_len = 25;
            get_flag = 1;
        }

        if (tmpArr[1] == 70 && tmpArr[11] == 70 && tmpArr[24] == 70) {

            get_flag = 0;
            StopProgressDialog();
            BluetoothGattService led_service = mBluetoothGatt.getService(UUID_LED_SERVICE);
            BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);
            byte[] value = new byte[1];
            //value[0] = (byte) ('n' & 0xFF);
            tmpChar.setValue(value);
            tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            writeCharacteristic(tmpChar);
        }

        if (tmpArr[0] == 55 && tmpArr[1] == 55) {
            //ShowProgressDialog();
            get_flag = 2;
        }


        for (int i = 0; i < tmparr_len; i++) {
            ch = (char) tmpArr[i];

            //System.out.println(i);
            //System.out.println(ch);
            strBuilder.append(ch);
        }
        //System.out.println(strBuilder);
        return strBuilder;
    }


    private void ShowProgressDialog() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }


    private void StopProgressDialog() {
        progressDialog.dismiss();
        Toast toast = Toast.makeText(getApplicationContext(),
                "Данные успешно сохранены на устройстве",
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void immediatelycloseProgDialog(){
        progressDialog.dismiss();
    }


    private void StopProgressDialogSendFile() {

        progressDialog.dismiss();
        Toast toast = Toast.makeText(getApplicationContext(),
                "Данные успешно отправлены на устройстве",
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }

    private void invisible() {
        //tv_menustatus.setVisibility(View.INVISIBLE);
        btn_menustatus.setVisibility(View.INVISIBLE);
        btn_sendtodevice.setVisibility(View.INVISIBLE);
        entercorrection.setText(null);
        entercorrection.setVisibility(View.INVISIBLE);

    }

    private void makevissible() {
        //tv_menustatus.setTextSize(20);
        //tv_menustatus.setBackgroundColor(Color.parseColor("#FFFFFF"));
        //tv_menustatus.setVisibility(View.VISIBLE);
        btn_menustatus.setVisibility((View.VISIBLE));
        entercorrection.setVisibility(View.VISIBLE);
        btn_sendtodevice.setVisibility(View.VISIBLE);
    }

    private void sendcorrection(){
        btn_sendtodevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mBluetoothGatt == null) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ПОЖАЛУЙСТА ПОДКЛЮЧИТЕСЬ К УСТРОЙСТВУ",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }



                EditText entercorrection = findViewById(R.id.entercorrection);
                String text_pool = entercorrection.getText().toString();
                Pattern pattern = Pattern.compile("[~#@*+%{}.,N<>\\[\\]|\"\\_^]");
                Matcher matcher = pattern.matcher(text_pool);
                boolean matchFound = matcher.find();
                if(matchFound) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ТОЛЬКО ЦИФРЫ ОТ -9 ДО +9",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                } else {
                    System.out.println("Match not found");
                }


                Integer edittext_pool =  Integer.parseInt(text_pool);
                String number = "c" + entercorrection.getText().toString();


                BluetoothGattService led_service = mBluetoothGatt.getService(UUID_LED_SERVICE);
                BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);
                if (led_service == null) {
                    return;
                }
                if(edittext_pool > 9 || edittext_pool <= -9){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "МАКСИМАЛЬНОЕ ЗНАЧЕНИЕ ОТ -9 ДО +9",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }

                if (number.length() > 3) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "МАКСИМАЛЬНОЕ ЗНАЧЕНИЕ ОТ -9 ДО +9",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                } else if (number.length() == 1) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ПОЛЕ НЕ ДОЛЖНО БЫТЬ ПУСТЫМ, ВВЕДИТЕ ДАННЫЕ",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "УСПЕШНО",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    invisible();
                }
                //tmpChar.setValue(number);
                //tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                //writeCharacteristic(tmpChar);
                //entercorrection.onEditorAction(EditorInfo.IME_ACTION_DONE);
                //byte[] value = new byte[1];
                //value[0] = (byte) ('n' & 0xFF);
                //tmpChar.setValue(value);
                //tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                //writeCharacteristic(tmpChar);
            }
        });
    }

    private void ipadres(){
        btn_sendtodevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText entercorrection = findViewById(R.id.entercorrection);
                System.out.println(entercorrection.getText().toString());
                String number = "w" + entercorrection.getText().toString() + "\n";
                if (mBluetoothGatt == null) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ПОЖАЛУЙСТА ПОДКЛЮЧИТЕСЬ К УСТРОЙСТВУ",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }

                BluetoothGattService led_service = mBluetoothGatt.getService(UUID_LED_SERVICE);
                BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);
                if (led_service == null) {
                    return;
                }
                if (number.length() > 14) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ВВЕДИТЕ КОРРЕКТНЫЙ IP ADRESS В ФОРМАТЕ: 255/255/255/255",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                } else if (number.length() == 1) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ПОЛЕ НЕ ДОЛЖНО БЫТЬ ПУСТЫМ, ВВЕДИТЕ ДАННЫЕ",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "УСПЕШНО",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    invisible();
                }
                tmpChar.setValue(number);
                tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                writeCharacteristic(tmpChar);
                entercorrection.onEditorAction(EditorInfo.IME_ACTION_DONE);
                byte[] value = new byte[1];
                //value[0] = (byte) ('n' & 0xFF);
                tmpChar.setValue(value);
                tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                writeCharacteristic(tmpChar);
            }
        });
    }

    private void sendhigh(){
        btn_sendtodevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText entercorrection = findViewById(R.id.entercorrection);
                String number = "h" + entercorrection.getText().toString();
                if (mBluetoothGatt == null) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ПОЖАЛУЙСТА ПОДКЛЮЧИТЕСЬ К УСТРОЙСТВУ",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }

                String text_pool = entercorrection.getText().toString();
                Pattern pattern = Pattern.compile("[~#@*+%{}.,N<>\\[\\]|\"\\_^]");
                Matcher matcher = pattern.matcher(text_pool);
                boolean matchFound = matcher.find();
                if(matchFound) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ТОЛЬКО ЦИФРЫ ОТ -9 ДО +9",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                } else {
                    System.out.println("Match not found");
                }


                BluetoothGattService led_service = mBluetoothGatt.getService(UUID_LED_SERVICE);
                BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);
                if (led_service == null) {
                    return;
                }
                if (number.length() > 4) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "МАКСИМАЛЬНОЕ ЗНАЧЕНИЕ 999 СМ",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                } else if (number.length() == 1) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ПОЛЕ НЕ ДОЛЖНО БЫТЬ ПУСТЫМ, ВВЕДИТЕ ДАННЫЕ",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "УСПЕШНО",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    invisible();
                }
                tmpChar.setValue(number);
                tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                writeCharacteristic(tmpChar);
                entercorrection.onEditorAction(EditorInfo.IME_ACTION_DONE);
                byte[] value = new byte[1];
                //value[0] = (byte) ('n' & 0xFF);
                tmpChar.setValue(value);
                tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                writeCharacteristic(tmpChar);
            }
        });
    }

    private void sendphone(){
        btn_sendtodevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText entercorrection = findViewById(R.id.entercorrection);
                System.out.println(entercorrection.getText().toString());
                String number = "p" + entercorrection.getText().toString() + "\n";
                if (mBluetoothGatt == null) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ПОЖАЛУЙСТА ПОДКЛЮЧИТЕСЬ К УСТРОЙСТВУ",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                BluetoothGattService led_service = mBluetoothGatt.getService(UUID_LED_SERVICE);
                BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);
                if (led_service == null) {
                    return;
                }
                if (number.length() > 11) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ВВЕДИТЕ КОРРЕКТНЫЙ НОМЕР В ФОРМАТЕ: 90 900 1234",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                } else if (number.length() == 1) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "ПОЛЕ НЕ ДОЛЖНО БЫТЬ ПУСТЫМ, ВВЕДИТЕ ДАННЫЕ",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "УСПЕШНО",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    invisible();
                }
                tmpChar.setValue(number);
                tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                writeCharacteristic(tmpChar);
                entercorrection.onEditorAction(EditorInfo.IME_ACTION_DONE);
                byte[] value = new byte[1];
                //


                tmpChar.setValue(value);
                tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                writeCharacteristic(tmpChar);
            }
        });
    }




    private void sentsuccess() {
        BluetoothGattService led_service = mBluetoothGatt.getService(UUID_LED_SERVICE);
        BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);
        byte[] value = new byte[1];
        value[0] = (byte) ('s' & 0xFF);
        tmpChar.setValue(value);
        tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        writeCharacteristic(tmpChar);
    }

    private void sentstop() {
        BluetoothGattService led_service = mBluetoothGatt.getService(UUID_LED_SERVICE);
        BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);
        byte[] value = new byte[1];
        value[0] = (byte) ('x' & 0xFF);
        tmpChar.setValue(value);
        tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        writeCharacteristic(tmpChar);
    }


    private void savetobd(String testtext) {
        try {
            requestWritingPermision();
            File root = new File(Environment.getExternalStorageDirectory(), DIR_SD);
            if (!root.exists()) {
                root.mkdirs();
            }
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh:mm");
            String format = formatter.format(date);
            File log = new File(root, format + "history.txt");
            if (!log.exists()) {
                System.out.println("We had to make a new file.");
                log.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(log, true);
            String text = testtext + "\n";
            fileWriter.write(text);
            fileWriter.close();
            //Toast.makeText(MainActivity.this, "Text saved", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
        //
    }




    private void requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permission needed")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private void requestWritingPermision() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
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
    }

    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
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
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
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
    final Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            mScanning = false;
            mBtScanner.stopScan(mScanCallback);
            broadcastUpdate(ACTION_DEVICE_NOT_FOUND);
            Log.e("Scan device", "Scan STOPPED");
        }
    };

    private void scanLeDevice(final boolean enable) {
        if (mBtScanner == null) {
            mBtScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        if (enable) {
            mBtDevices.clear();

            scanDelayedHandler = new Handler(Looper.getMainLooper());
            scanDelayedHandler.postDelayed(scanRunnable, 10000);

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
            b.setBackgroundColor(getResources().getColor(R.color.white));
            b.setTextColor(getResources().getColor(R.color.cryola));
            b.setText("ПОДКЛЮЧИТЬ");
            b.setGravity(Gravity.CENTER);

            // Create action when clicking the connect button
            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    connectToDevice(savedDevice);
                }
            });

            // Add items to the row
            tr.addView(tvRssi);
            //tr.addView(tvEmpty1);
            tr.addView(tvDevice);
            //tr.addView(tvEmpty2);
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
            mTableDevices.removeAllViews();
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
                    //((TextView) findViewById(R.id.textView2)).setText("" + gatt.getService(UUID_BUTTON_SERVICE).getCharacteristics());
                   // ((TextView) findViewById(R.id.textView2)).setText("0"+ characteristic.getDescriptors());
                }
            }
            //((TextView) findViewById(R.id.textView2)).setText("1" + tmpChar.getValue());
                // Enable notification on the characteristic
                //UUID test = tmpChar();
                //tmpChar.setValue(0,0,0);
                //mBluetoothGatt.writeCharacteristic(tmpChar);
                //List<BluetoothGattDescriptor> descrip = tmpChar.getDescriptors();
                //((TextView) findViewById(R.id.textView2)).setText("Test"+ tmpChar);
                //((TextView) findViewById(R.id.textView2)).setText("Test"+ tmpChar.getValue());
                //broadcastUpdate(ACTION_CHECK_CHARACTERISTICS,tmpChar);

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

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
           if (status == BluetoothGatt.GATT_SUCCESS) {
               //broadcastUpdate(TEST_MESSAGE);
               enableButtonNotifications(gatt);

            }
        }

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


    private void invalidateScanButton() {
        if (!mScanning) {
            btnScan.setText("Сканировать");
        } else {
            btnScan.setText("Стоп");
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
        intentFilter.addAction(ACTION_SCAN_TIMEOUT);
        intentFilter.addAction(ACTION_DEVICE_NOT_FOUND);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        intentFilter.addAction(ACTION_WRITE_SUCCESS);
        return intentFilter;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "Bluetooth not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    private void showArrayofExcelFile(String text){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Excel Файл")
                .setMessage(text)
                .setCancelable(false)
                .setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Функция для отправки данных устройству;
                        sendFiletoDevice();
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Функция для отмены отправки файла и закрытия Диалогового окна
                        dialogInterface.cancel();

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void sendFiletoDevice(){
        try {
            Thread.sleep(1000); //Приостанавливает поток на 1 секунду
        } catch (Exception e) {

        }
        if (mBluetoothGatt == null) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "ПОЖАЛУЙСТА ПОДКЛЮЧИТЕСЬ К УСТРОЙСТВУ",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if(file_path == null)
        {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "ВЫБЕРИТЕ ФАЙЛ",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        BluetoothGattService led_service = mBluetoothGatt.getService(UUID_LED_SERVICE);
        mBluetoothGatt.requestMtu(50);
        try {
            Thread.sleep(1000); //Приостанавливает поток на 1 секунду
        } catch (Exception e) {

        }
        if (led_service == null) {
            return;
        }
        checksenttimes = 0;
        curCol=1;
        curRow=1;
        BluetoothGattCharacteristic tmpChar = led_service.getCharacteristic(UUID_LED0_STATE);
        byte[] value = new byte[1];
        value[0] = (byte) ('f' & 0xFF);
        tmpChar.setValue(value);
        tmpChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        writeCharacteristic(tmpChar);
        ShowProgressDialog();
    }



}


