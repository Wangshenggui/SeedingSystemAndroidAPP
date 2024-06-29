package Seeding.System;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketService extends Service {

    private WebSocket webSocket;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String TAG = "WebSocketService";
    private SendMessageReceiver sendMessageReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        // 创建WebSocket连接
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://8.137.81.229:8880")
                .build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket connected");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                mainHandler.post(() -> {
                    Log.d(TAG, "Received: " + text);
                    Intent intent = new Intent("WebSocketMessage");
                    intent.putExtra("message", text);
                    sendBroadcast(intent);
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // Handle binary message
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
                Log.d(TAG, "WebSocket closing: " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closed: " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.d(TAG, "WebSocket failure", t);
            }
        });

        // 注册广播接收器
        sendMessageReceiver = new SendMessageReceiver();
        IntentFilter filter = new IntentFilter("SendWebSocketMessage");
        registerReceiver(sendMessageReceiver, filter);

        startForegroundService();
    }

    private void startForegroundService() {
        // 启动前台服务逻辑
        // 此处省略代码...
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Service destroyed");
        }
        if (sendMessageReceiver != null) {
            unregisterReceiver(sendMessageReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class SendMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("SendWebSocketMessage".equals(intent.getAction())) {
                String message = intent.getStringExtra("message");
                if (webSocket != null) {
                    webSocket.send(message);
                }
            }
        }
    }
}
