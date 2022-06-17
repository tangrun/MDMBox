package com.tangrun.mdm.socketbox.ui;

import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.tangrun.mdm.socketbox.R;

/**
 <activity
     android:name="com.tangrun.mdm.socketbox.ui.DeviceRegistrationActivity"
     android:exported="true"
     android:launchMode="singleTask">
     <intent-filter>
         <action android:name="com.cdblue.DeviceRegistration"/>
         <category android:name="android.intent.category.DEFAULT"/>
     </intent-filter>
 </activity>
 */
public class DeviceRegistrationActivity extends DeviceRegistrationBaseActivity {
    public static OnRegistrationCallback sCallback;
    private Toolbar toolbar;
    private EditText etDebug;
    private TextView textView;
    private Button btStart;
    private Button btRecover;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (sCallback == null){
            showToast("sCallback为空");
            finish();
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sb_activity_device_registration);
        // 允许截屏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        toolbar = findViewById(R.id.toolbar);
        btStart = findViewById(R.id.bt_start);
        btRecover = findViewById(R.id.bt_recover);
        textView = findViewById(R.id.tv_tip);
        etDebug = findViewById(R.id.et_debug);

        setSuperView(textView);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btStart.setOnClickListener(v -> {
            if (isOwner()) {
                showInfoDialog("系统提示", "设备已完成激活，请勿重复操作！", null);
                return;
            }
            etDebug.setText("");
            startRegistration();
        });
        btRecover.setOnClickListener(v -> {
            recoveryApp();
        });
    }

    /**
     * 激活结果回调
     *
     * @param success
     * @param result
     */
    @Override
    public void onRegistrationResult(boolean success, String result) {
        if (!success) {
            sCallback.onUploadError(result);
        }
    }

    @Override
    public void onRemoveAdmin() {
        if (!isOwner()) return;
        showConfirmDialog(null, "是否取消激活？", new Runnable() {
            @Override
            public void run() {
                sCallback.removeProfileOwner();
            }
        }, null);
    }

    @Override
    protected void onConsole(String line) {
        etDebug.getEditableText().append("\n").append(line);
    }

    @Override
    protected void onOpenDebugMode() {
        btRecover.setVisibility(View.VISIBLE);
        btRecover.setOnClickListener(v -> {
            recoveryApp();
        });
        etDebug.getEditableText().append("------------");
    }

    @Override
    protected void onBoxMsg(String flag, String msg) {
        switch (flag == null ? "" : flag) {
            case "0":
                // 消息提示
                showInfoDialog("系统提示", msg, null);
                break;
            case "1":
                // 自动开始激活
                btStart.performClick();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isOwner() {
        return sCallback.isProfileOwner();
    }

    @Override
    protected com.tangrun.mdm.shell.pojo.ComponentName getProfileOwnerComponent() {
        ComponentName componentName = sCallback.getProfileOwnerComponent();
        return new com.tangrun.mdm.shell.pojo.ComponentName(componentName.getPackageName(), componentName.getClassName());
    }

    public interface OnRegistrationCallback {
        void onUploadError(String msg);

        ComponentName getProfileOwnerComponent();

        boolean isProfileOwner();

        void removeProfileOwner();
    }

}
