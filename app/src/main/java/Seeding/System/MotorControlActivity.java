package Seeding.System;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * MotorControlActivity是控制电机的活动类，通过WebSocket与远程服务器进行通信。
 */
public class MotorControlActivity extends AppCompatActivity {

    private WebSocket webSocket; // WebSocket实例
    private TextView RxTextView; // 用于显示接收到的消息的TextView
    private Button SendDataButton; // 发送数据的按钮
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // 主线程Handler，用于更新UI

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motor_control); // 设置布局文件

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.Toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("电机控制");
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleText);

        // 初始化视图组件
        RxTextView = findViewById(R.id.RxTextView); // 获取显示接收消息的TextView
        SendDataButton = findViewById(R.id.SendDataButton); // 获取发送数据的Button

        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 处理返回按钮的点击
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回上一个界面
                finish();
            }
        });

        // 创建OkHttpClient实例
        OkHttpClient client = new OkHttpClient();

        // 创建WebSocket连接的请求
        Request request = new Request.Builder()
                .url("ws://8.137.81.229:8880") // 使用提供的 WebSocket URL
                .build();

        // 通过OkHttpClient建立WebSocket连接，并设置WebSocketListener
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // WebSocket连接已打开时的处理
                // 这里可以添加逻辑处理WebSocket连接成功后的行为
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // 收到文本消息时的处理
                // 使用mainHandler更新UI显示接收到的消息
                mainHandler.post(() -> RxTextView.setText("Rx: " + text));
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // 收到二进制消息时的处理
                // 这里可以处理接收到的二进制数据
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                // WebSocket正在关闭时的处理
                webSocket.close(code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                // WebSocket已关闭时的处理
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                // WebSocket连接失败时的处理
                // 可以在这里处理连接失败的情况，例如显示错误消息或重连
            }
        });

        // 设置发送按钮的点击事件监听器
        SendDataButton.setOnClickListener(new View.OnClickListener() {
            private int counter = 0; // 发送计数器，每次点击递增

            @Override
            public void onClick(View v) {
                // 点击发送按钮时的处理
                counter++; // 计数器递增

                // 创建一个JSON格式的字符串，包含递增后的计数器值
                String json = "{\"giuty\":" + counter + "}";

                // 发送JSON字符串到WebSocket服务器
                if (webSocket != null) {
                    webSocket.send(json); // 发送消息
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在Activity销毁时关闭WebSocket连接
        if (webSocket != null) {
            webSocket.close(1000, "Activity destroyed"); // 关闭WebSocket连接
        }
    }
}
