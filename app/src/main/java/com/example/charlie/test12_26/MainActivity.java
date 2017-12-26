package com.example.charlie.test12_26;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private TextView snum;
    ToggleButton mode_1;
    ToggleButton mode_2;
    public int modecheck1=0;
    public int modecheck2=0;
    static final int REQUEST_ENABLE_BLE=1;
    private static MyService myService;
    private static boolean mConnectState;
    private static boolean mServiceConnected;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToggleButton mode_1=(ToggleButton)findViewById(R.id.mode_1);
        ToggleButton mode_2=(ToggleButton)findViewById(R.id.mode_2);
        Button start = (Button) findViewById(R.id.start);
//        Button startbluetooth = (Button) findViewById(R.id.bluetooth);
//        snum=(TextView)findViewById(R.id.serialNumber);

        start.setOnClickListener(new MyButtonListener());
        mode_1.setOnCheckedChangeListener(new ModeOneListener());
        mode_2.setOnCheckedChangeListener(new ModeTwoListener());

        mServiceConnected = false;
        mConnectState = false;
        final BluetoothManager bluetoothManager =(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = null;

        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLE);
        }
        Log.d(TAG, "Bluetooth is Enabled");
        Intent gattServiceIntent = new Intent(MainActivity.this, MyService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "Starting BLE Service");
    }

    protected void onResume() {
        super.onResume();
        // Register the broadcast receiver. This specified the messages the main activity looks for from the PSoCCapSenseLedService
        final IntentFilter filter = new IntentFilter();
        filter.addAction(MyService.ACTION_BLESCAN_CALLBACK);
        filter.addAction(MyService.ACTION_CONNECTED);
        filter.addAction(MyService.ACTION_DISCONNECTED);
        filter.addAction(MyService.ACTION_SERVICES_DISCOVERED);
        filter.addAction(MyService.ACTION_DATA_RECEIVED);
        registerReceiver(mBleUpdateReceiver, filter);
    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBleUpdateReceiver);
    }

    protected void onDestroy() {
        super.onDestroy();
        myService.close();
        unbindService(mServiceConnection);
        myService = null;
        mServiceConnected = false;
    }

    class ModeOneListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton button1, boolean isChecked) {
           if(button1.isChecked()){
               modecheck1=1;
//               mode_2.setChecked(false);
               modecheck2=0;
           }
        }
    }

    class ModeTwoListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton button1, boolean isChecked) {
            if(button1.isChecked()){
                modecheck2=1;
//                mode_1.setChecked(false);
                modecheck1=0;
            }
        }
    }

    class MyButtonListener implements View.OnClickListener {
        public void onClick(View v){
            Intent sendMode = new Intent(MainActivity.this, MyService.class);
            sendMode.putExtra("mode1",modecheck1);
            sendMode.putExtra("mode2",modecheck2);
            startService(sendMode);

            if(mServiceConnected) {
                myService.scan();
            }
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        /**
         * This is called when the PSoCCapSenseLedService is connected
         *
         * @param componentName the component name of the service that has been connected
         * @param service service being bound
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            myService = ((MyService.LocalBinder) service).getService();
            mServiceConnected = true;
            myService.initialize();
        }

        /**
         * This is called when the PSoCCapSenseService is disconnected.
         *
         * @param componentName the component name of the service that has been connected
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected");
            myService = null;
        }
    };

    public void startBluetooth(View view){
        if(mServiceConnected) {
            myService.scan();
        }
    }

    private final BroadcastReceiver mBleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            assert action != null;
            switch(action){
                case MyService.ACTION_BLESCAN_CALLBACK:
                    myService.connect();
                    Log.d(TAG,"start connecting");
                    break;

                case MyService.ACTION_CONNECTED:
                    if (!mConnectState) {
                        mConnectState = true;
                        myService.discoverServices();
                        Log.d(TAG, "Connected to Device");
                    }

                    Intent start_cook=new Intent();
                    start_cook.setClass(MainActivity.this,StopActivity.class);
                    startActivity(start_cook);
                    break;

                case MyService.ACTION_SERVICES_DISCOVERED:
                    Log.d(TAG, "Services Discovered");
                    break;
                case MyService.ACTION_DATA_RECEIVED:
                    String sNum=myService.getSerialNumberValue();
                    snum.setText(sNum);
                    Log.d(TAG, "serial number read");
                    myService.disconnect();
                    break;
//              case MyService.ACTION_DATA_AVAILABLE:
                default:
                    break;
            }
        }
    };
}


