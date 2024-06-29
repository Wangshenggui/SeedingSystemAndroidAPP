package Seeding.System;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MotorControlActivity extends AppCompatActivity {

    private TextView RxTextView;
    private Button SendDataButton;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private WebSocketServiceReceiver receiver;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motor_control);

        // 启动WebSocket服务
        Intent serviceIntent = new Intent(this, WebSocketService.class);
        startService(serviceIntent);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.Toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("电机控制");
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleText);

        RxTextView = findViewById(R.id.RxTextView);
        SendDataButton = findViewById(R.id.SendDataButton);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // 设置发送按钮的点击事件监听器
        SendDataButton.setOnClickListener(new View.OnClickListener() {
            private int counter = 0;

            @Override
            public void onClick(View v) {
                counter++;
                String json = "{\"giuty\":" + counter + "}";
                Intent intent = new Intent("SendWebSocketMessage");
                intent.putExtra("message", json);
                sendBroadcast(intent);
            }
        });

        // 注册广播接收器
        receiver = new WebSocketServiceReceiver();
        IntentFilter filter = new IntentFilter("WebSocketMessage");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    private class WebSocketServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("WebSocketMessage".equals(intent.getAction())) {
                String message = intent.getStringExtra("message");
                RxTextView.setText("Rx: " + message);
            }
        }
    }
}
