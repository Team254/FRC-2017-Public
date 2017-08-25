package com.team254.cheezdroid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.app.ActivityCompat;

import com.team254.cheezdroid.comm.RobotConnection;
import com.team254.cheezdroid.comm.RobotConnectionStateListener;
import com.team254.cheezdroid.comm.RobotConnectionStatusBroadcastReceiver;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.util.Timer;
import java.util.TimerTask;

public class VisionTrackerActivity extends Activity implements RobotConnectionStateListener, RobotEventListener {
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    private static boolean sLocked = true;

    private VisionTrackerGLSurfaceView mView;
    private TextView mProcMode;
    private ImageButton mLockButton, mPrefsButton, mViewTypeButton;
    private TextView mBatteryText;
    private ImageView mChargingIcon;
    private Preferences m_prefs;

    private View connectionStateView;
    private RobotConnectionStatusBroadcastReceiver rbr;
    private RobotEventBroadcastReceiver rer;
    private Timer mUpdateViewTimer;
    private Long mLastSelfieLaunch = 0L;

    private boolean mIsRunning;

    public static boolean isLocked() {
        return sLocked;
    }

    @Override
    public void shotTaken() {
        Log.i("VisionActivity", "Shot taken");
        playAirhorn();
    }

    @Override
    public void wantsVisionMode() {

    }

    @Override
    public void wantsIntakeMode() {
//        if (mIsRunning && (System.currentTimeMillis() - mLastSelfieLaunch > 1000)) {
//            Intent i = new Intent();
//            i.setClass(VisionTrackerActivity.this, SelfieActivity.class);
//            mLastSelfieLaunch = System.currentTimeMillis();
//            startActivity(i);
//        }
    }

    private class PowerStateBroadcastReceiver extends BroadcastReceiver {

        public PowerStateBroadcastReceiver(VisionTrackerActivity activity) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
            intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            activity.registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            VisionTrackerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateBatteryText();
                }
            });
        }
    }
    private PowerStateBroadcastReceiver mPbr;

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(parent.getActivity(),
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = parent.getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }

    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(this.getFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getFragmentManager(), FRAGMENT_DIALOG);
            } else {
                tryStartCamera();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsRunning = true;
        Log.i("VisionActivity", "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("VisionActivity", "onRestart");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //MjpgServer.getInstance().initFromAssets(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // Init app prefs
        m_prefs = new Preferences(this);

        setContentView(R.layout.activity);
        tryStartCamera();

        connectionStateView = findViewById(R.id.connectionState);
        mLockButton = (ImageButton) findViewById(R.id.lockButton);
        mViewTypeButton = (ImageButton) findViewById(R.id.viewSelectButton);
        mPrefsButton = (ImageButton) findViewById(R.id.hsvEditButton);
        mBatteryText = (TextView) findViewById(R.id.battery_text);
        mChargingIcon = (ImageView) findViewById(R.id.chargingIcon);

        updateBatteryText();

        rbr = new RobotConnectionStatusBroadcastReceiver(this, this);
        rer = new RobotEventBroadcastReceiver(this, this);

        // Listen for power events
        mPbr = new PowerStateBroadcastReceiver(this);

        if (sLocked) {
            setLockOn();
        } else {
            setLockOff();
        }

        mLockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sLocked) {
                    setLockOn();
                }
            }
        });
        mLockButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (sLocked) {
                    setLockOff();
                    return true;
                }
                return false;
            }
        });

        whitelistLockTasks();

        Log.i("VisionActivity", "onCreate");
    }

    private void tryStartCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        mView = (VisionTrackerGLSurfaceView) findViewById(R.id.my_gl_surface_view);
        mView.setCameraTextureListener(mView);
        mView.setPreferences(m_prefs);
        TextView tv = (TextView) findViewById(R.id.fps_text_view);
        mProcMode = (TextView) findViewById(R.id.proc_mode_text_view);
        mView.setProcessingMode(NativePart.DISP_MODE_TARGETS_PLUS);
        runOnUiThread(new Runnable() {
            public void run() {
                updateProcModeText();
            }
        });
    }

    @Override
    protected void onPause() {
        Log.i("VisionTrackerActivity", "onPause");
        if (mView != null) {
            mView.onPause();
        }
        if (mUpdateViewTimer != null) {
            mUpdateViewTimer.cancel();
            mUpdateViewTimer = null;
        }
        mIsRunning = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i("VisionActivity", "onResume " + mView);
        super.onResume();
        if (mView != null) {
            mView.onResume();
        }
        mUpdateViewTimer = new Timer();
        mUpdateViewTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateBatteryText();
                    }
                });
            }
        }, 0, 60000);
        mIsRunning = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void showViewOptions(View v) {
        mView.openOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.raw:
                mView.setProcessingMode(NativePart.DISP_MODE_RAW);
                break;
            case R.id.thresh:
                mView.setProcessingMode(NativePart.DISP_MODE_THRESH);
                break;
            case R.id.targets:
                mView.setProcessingMode(NativePart.DISP_MODE_TARGETS);
                break;
            case R.id.targets_plus:
                mView.setProcessingMode(NativePart.DISP_MODE_TARGETS_PLUS);
                break;
            default:
                return false;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateProcModeText();
            }
        });
        return true;
    }

    public void openBottomSheet(View v) {
        View view = getLayoutInflater().inflate(R.layout.hsv_bottom_sheet, null);
        LinearLayout container = (LinearLayout) view.findViewById(R.id.popup_window);
        container.getBackground().setAlpha(20);


        final Dialog mBottomSheetDialog = new Dialog(VisionTrackerActivity.this, R.style.MaterialDialogSheet);
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
        mBottomSheetDialog.show();

        final RangeSeekBar hSeekBar = (RangeSeekBar) view.findViewById(R.id.hSeekBar);
        setSeekBar(hSeekBar, getHRange());
        hSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> rangeSeekBar, Integer min, Integer max) {
                Log.i("H", min + " " + max);
                m_prefs.setThresholdHRange(min, max);
            }
        });

        final RangeSeekBar sSeekBar = (RangeSeekBar) view.findViewById(R.id.sSeekBar);
        setSeekBar(sSeekBar, getSRange());
        sSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> rangeSeekBar, Integer min, Integer max) {
                Log.i("S", min + " " + max);
                m_prefs.setThresholdSRange(min, max);
            }
        });

        final RangeSeekBar vSeekBar = (RangeSeekBar) view.findViewById(R.id.vSeekBar);
        setSeekBar(vSeekBar, getVRange());
        vSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> rangeSeekBar, Integer min, Integer max) {
                Log.i("V", min + " " + max);
                m_prefs.setThresholdVRange(min, max);
            }
        });

        Button restoreButton = (Button) view.findViewById(R.id.restoreDefaultsButton);
        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_prefs.restoreDefaults();
                setSeekBar(hSeekBar, getHRange());
                setSeekBar(sSeekBar, getSRange());
                setSeekBar(vSeekBar, getVRange());
            }
        });
    }

    private static void setSeekBar(RangeSeekBar<Integer> bar, Pair<Integer, Integer> values) {
        bar.setSelectedMinValue(values.first);
        bar.setSelectedMaxValue(values.second);
    }

    public Pair<Integer, Integer> getHRange() {
        return m_prefs.getThresholdHRange();
    }

    public Pair<Integer, Integer> getSRange() {
        return m_prefs.getThresholdSRange();
    }

    public Pair<Integer, Integer> getVRange() {
        return m_prefs.getThresholdVRange();
    }

    @Override
    public void robotConnected() {
        Log.i("MainActivity", "Robot Connected");
        mView.setRobotConnection(AppContext.getRobotConnection());
        connectionStateView.setBackgroundColor(ContextCompat.getColor(this, R.color.cheesy_poof_blue));
        stopBadConnectionAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(rbr);
        unregisterReceiver(mPbr);
        unregisterReceiver(rer);
    }

    public void startBadConnectionAnimation() {
        Animation animation = new ScaleAnimation(1, 1, 1, 20, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        animation.setDuration(200);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        connectionStateView.startAnimation(animation);
    }

    public void stopBadConnectionAnimation() {
        connectionStateView.clearAnimation();
    }

    @Override
    public void robotDisconnected() {
        Log.i("MainActivity", "Robot Disconnected");
        mView.setRobotConnection(null);
        connectionStateView.setBackgroundColor(ContextCompat.getColor(this, R.color.holo_red_light));
        if (isLocked()) {
            startBadConnectionAnimation();
        } else {
            stopBadConnectionAnimation();
        }
    }

    public void setLockOn() {
        sLocked = true;
        mLockButton.setImageResource(R.drawable.locked);
        mLockButton.clearAnimation();
        mLockButton.setBackgroundColor(Color.TRANSPARENT);
        mLockButton.setAlpha(0.75f);
        mPrefsButton.setVisibility(View.GONE);
        mViewTypeButton.setVisibility(View.GONE);
        startLockTask();
    }

    public void setLockOff() {
        sLocked = false;
        mPrefsButton.setVisibility(View.VISIBLE);
        mViewTypeButton.setVisibility(View.VISIBLE);
        mLockButton.setImageResource(R.drawable.unlocked);
        mLockButton.setBackgroundColor(Color.RED);
        mLockButton.setAlpha(1.0f);
        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(350);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        mLockButton.startAnimation(animation);
        stopLockTask();
        stopBadConnectionAnimation();
    }

    private void whitelistLockTasks() {
        DevicePolicyManager manager =
                (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = ChezyDeviceAdminReceiver.getComponentName(this);

        if (manager.isDeviceOwnerApp(getPackageName())) {
            manager.setLockTaskPackages(componentName, new String[]{getPackageName()});
        }
    }

    private void enableDeviceAdmin() {
        DevicePolicyManager manager =
                (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = ChezyDeviceAdminReceiver.getComponentName(this);

        if(!manager.isAdminActive(componentName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            startActivityForResult(intent, 0);
            return;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus && sLocked) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    private void updateBatteryText() {
        Intent batteryStatus =
                registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPercentage = 100f * level / scale;
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        mBatteryText.setText(Integer.toString((int)batteryPercentage) + "%");
        mChargingIcon.setVisibility(isCharging ? View.VISIBLE : View.GONE);
    }

    private void updateProcModeText() {
        mProcMode.setText("Proc Mode: "
                + VisionTrackerGLSurfaceView.PROC_MODE_NAMES[mView.getProcessingMode()]);
    }

    public void playAirhorn() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.airhorn);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
            }
        });
        mp.start();
    }
}
