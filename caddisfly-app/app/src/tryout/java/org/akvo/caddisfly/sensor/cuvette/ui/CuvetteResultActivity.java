package org.akvo.caddisfly.sensor.cuvette.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConstants;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.cuvette.bluetooth.Constants;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.BluetoothChatService;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.ResultAdapter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CuvetteResultActivity extends BaseActivity
        implements DeviceListDialog.OnDeviceSelectedListener,
        DeviceListDialog.OnDeviceCancelListener {

    private static final int REQUEST_ENABLE_BT = 3;
    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    MyInnerHandler mHandler = new MyInnerHandler(this);
    DialogFragment deviceDialog;
    private WeakReference<CuvetteResultActivity> mActivity;
    private Button buttonPause;
    // Layout Views
    private RecyclerView mConversationView;
    /**
     * Array adapter for the conversation thread
     */
    private ResultAdapter mConversationArrayAdapter;
    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;
    /**
     * `
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendMessage(intent.getStringExtra("cuvette_result"));
        }
    };
    private TextView textResult;
    private Handler resultHandler;
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            if (isFinishing()) {
                return;
            }
            if (mConversationArrayAdapter != null) {
                try {
                    List<ResultDetail> list = mConversationArrayAdapter.getList();

                    if (list != null) {
                        int listSize = list.size();
                        double total = 0;
                        int distanceZeroCount = 0;
                        int divisor = 0;
                        for (int i = listSize - 1; i >= 0; i--) {
                            ResultDetail resultDetail = list.get(i);
                            if (resultDetail.getResult() > 0) {
                                if (resultDetail.getDistance() <= AppPreferences.getColorDistanceTolerance()) {
                                    distanceZeroCount++;
                                }
                                if (distanceZeroCount > 9) {
                                    total += resultDetail.getResult();
                                    divisor++;
                                    distanceZeroCount = 0;
                                }
                            } else {
                                distanceZeroCount = 0;
                            }
                        }
                        if (divisor > 0) {
                            textResult.setText(String.format(Locale.getDefault(),
                                    "%.2f", total / divisor));
                        }
                    }

                } finally {
                    resultHandler.postDelayed(mStatusChecker, 1000);
                }
            }
        }
    };
    private boolean readPaused;
    private boolean ignoreNoResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cuvette_result);

        mConversationView = findViewById(R.id.in);
        textResult = findViewById(R.id.textResult);
        buttonPause = findViewById(R.id.buttonPause);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            releaseResources();
            finish();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver,
                new IntentFilter("CUVETTE_RESULT_ACTION")
        );

        resultHandler = new Handler();

        setTitle("View color stream");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mChatService == null || mChatService.getState() == BluetoothChatService.STATE_NONE) {
            showDeviceListDialog();
        } else {

            // Performing this check in onResume() covers the case in which BT was
            // not enabled during onStart(), so we were paused to enable it...
            // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
            if (mChatService != null) {
                // Only if the state is STATE_NONE, do we know that we haven't started already
                if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                    // Start the Bluetooth chat services
                    mChatService.start();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_stream, menu);
        return true;
    }

    private void showDeviceListDialog() {
        deviceDialog = DeviceListDialog.newInstance();
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        deviceDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        deviceDialog.show(ft, "deviceList");
    }

    private void releaseResources() {

        stopRepeatingTask();

        if (mActivity != null) {
            mActivity.clear();
            mActivity = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseResources();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

//    /**
//     * Makes this device discoverable for 300 seconds (5 minutes).
//     */
//    private void ensureDiscoverable() {
//        if (mBluetoothAdapter.getScanMode() !=
//                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            startActivity(discoverableIntent);
//        }
//    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
//            Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
//            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    protected void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * Establish connection with other device
     *
     * @param address An {@link Intent}
     * @param secure  Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(String address, boolean secure) {
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setupChat();
        } else {
            releaseResources();
            finish();
        }
    }

    @Override
    public void onDeviceSelected(String address) {
        connectDevice(address, true);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ResultAdapter();
//        mConversationArrayAdapter.setHasStableIds(true);
        ((DefaultItemAnimator) mConversationView.getItemAnimator())
                .setSupportsChangeAnimations(false);
        mConversationView.setLayoutManager(new LinearLayoutManager(this));
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        startRepeatingTask();
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        resultHandler.removeCallbacks(mStatusChecker);
    }

    public void onResetClick(View view) {
        mConversationArrayAdapter.clear();
        textResult.setText("");
        mConversationArrayAdapter.notifyDataSetChanged();
    }

    public void onPauseClick(View view) {
        pauseResume();
    }

    private void pauseResume() {
        readPaused = !readPaused;
        if (readPaused) {
            buttonPause.setText("Resume");
            buttonPause.setBackgroundColor(getResources().getColor(R.color.primary_dark));
            buttonPause.setTextColor(Color.WHITE);
        } else {
            buttonPause.setText("Pause");
            buttonPause.setBackgroundColor(Color.TRANSPARENT);
            buttonPause.setTextColor(getResources().getColor(R.color.text_primary));
            mConversationArrayAdapter.add("", false);
            mConversationArrayAdapter.notifyDataSetChanged();
            mConversationView.scrollToPosition(0);

        }
    }

    public void onIgnoreNoResultClick(View view) {
        ignoreNoResult = !ignoreNoResult;
    }

    @Override
    public void onDeviceCancel() {
        releaseResources();
        finish();
    }

    public void onExportClick(MenuItem item) {
        readPaused = true;
        buttonPause.setText("Resume");
        buttonPause.setBackgroundColor(getResources().getColor(R.color.primary_dark));
        buttonPause.setTextColor(Color.WHITE);
        StringBuilder stringBuilder = new StringBuilder();
        List<ResultDetail> list = mConversationArrayAdapter.getList();
        if (list != null && list.size() > 0) {
            stringBuilder.append("R,G,B,Result,Quality");
            stringBuilder.append(System.lineSeparator());
            for (int i = list.size() - 1; i >= 0; i--) {
                ResultDetail resultDetail = list.get(i);
                if (resultDetail.getResult() == -2) {
                    stringBuilder.append("Paused");
                } else {
                    stringBuilder.append(ColorUtil.getColorRgbString(resultDetail.getColor())
                            .replace("  ", ","));
                    stringBuilder.append(",");
                    if (resultDetail.getResult() > -1) {
                        stringBuilder.append(String.format(Locale.US, "%.2f", resultDetail.getResult()));
                    }
                    stringBuilder.append(",");
                    stringBuilder.append(resultDetail.getQuality());
                }
                stringBuilder.append(System.lineSeparator());
            }

            File folder = new File(FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false)
                    + File.separator + AppConstants.APP_FOLDER + File.separator + "qa");
            FileUtil.saveToFile(folder, "ColorStream.txt", stringBuilder.toString());
            Toast toast = Toast.makeText(this, "Exported to file", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
    }

    static class MyInnerHandler extends Handler {
        WeakReference<Activity> activityWeakReference;

        MyInnerHandler(Activity aFragment) {
            activityWeakReference = new WeakReference<>(aFragment);
        }

        @Override
        public void handleMessage(Message message) {
            CuvetteResultActivity activity = (CuvetteResultActivity) activityWeakReference.get();
            switch (message.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (message.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            activity.setStatus(activity.getString(R.string.title_connected_to,
                                    activity.mConnectedDeviceName));
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            activity.setStatus(R.string.deviceConnecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            activity.setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    if (!activity.readPaused) {
                        byte[] readBuf = (byte[]) message.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, message.arg1);
                        activity.mConversationArrayAdapter.add(readMessage, activity.ignoreNoResult);
                        activity.mConversationArrayAdapter.notifyDataSetChanged();
                        activity.mConversationView.scrollToPosition(0);
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    activity.mConnectedDeviceName = message.getData().getString(Constants.DEVICE_NAME);
//                    Toast.makeText(this, "Connected to "
//                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
