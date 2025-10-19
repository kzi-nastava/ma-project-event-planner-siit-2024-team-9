package com.example.eventify.services.others;

import android.content.Context;
import android.util.Log;

import com.example.eventify.models.others.Message;
import com.example.eventify.models.others.NotificationPayload;
import com.example.eventify.models.users.User;
import com.example.eventify.services.auth.LoginService;
import com.example.eventify.utils.NotifHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WebSocketService {
    private static final String TAG = "WebSocketService";
    private static final String WS_URL = "ws://192.168.0.27:8080/ws-native";

    private final Context appContext;
    private WebSocket webSocket;
    
    // Create custom LocalDateTime TypeAdapter
    private static class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString());
        }
    }
    
    // Create custom Gson with LocalDateTime TypeAdapter
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();
            
    private WebSocketListener listener;
    private User currentUser;
    private boolean isConnected = false;
    private boolean isSubscribed = false;

    public interface WebSocketListener {
        void onMessageReceived(Message message);
        void onConnectionEstablished();
        void onConnectionClosed();
        void onError(String error);
    }

    public WebSocketService(Context context, User currentUser) {
        this.appContext = context.getApplicationContext();
        this.currentUser = currentUser;
    }

    public void setListener(WebSocketListener listener) {
        this.listener = listener;
    }

    public void connect() {
        try {
            Log.d(TAG, "Attempting to connect to: " + WS_URL);
            WebSocketFactory factory = new WebSocketFactory();
            
            // Set connection timeout
            factory.setConnectionTimeout(10000); // 10 seconds
            
            webSocket = factory.createSocket(WS_URL);
            
            webSocket.addListener(new WebSocketAdapter() {
                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    Log.d(TAG, "WebSocket connected successfully");
                    isConnected = true;
                    
                    // Send STOMP CONNECT frame
                    sendStompConnect();
                    
                    if (listener != null) {
                        listener.onConnectionEstablished();
                    }
                }

                @Override
                public void onTextMessage(WebSocket websocket, String text) throws Exception {
                    Log.d(TAG, "Message received: " + text);
                    handleStompMessage(text);
                }

                @Override
                public void onDisconnected(WebSocket websocket, com.neovisionaries.ws.client.WebSocketFrame serverCloseFrame, 
                                         com.neovisionaries.ws.client.WebSocketFrame clientCloseFrame, boolean closedByServer) {
                    Log.d(TAG, "WebSocket disconnected");
                    isConnected = false;
                    isSubscribed = false;
                    if (listener != null) {
                        listener.onConnectionClosed();
                    }
                }

                @Override
                public void onError(WebSocket websocket, com.neovisionaries.ws.client.WebSocketException cause) throws Exception {
                    Log.e(TAG, "WebSocket error: " + cause.getMessage());
                    Log.e(TAG, "Error details: ", cause);
                    isConnected = false;
                    isSubscribed = false;
                    if (listener != null) {
                        listener.onError("Connection failed: " + cause.getMessage());
                    }
                }
                
                @Override
                public void onConnectError(WebSocket websocket, com.neovisionaries.ws.client.WebSocketException exception) throws Exception {
                    Log.e(TAG, "WebSocket connection error: " + exception.getMessage());
                    Log.e(TAG, "Connection error details: ", exception);
                    isConnected = false;
                    isSubscribed = false;
                    if (listener != null) {
                        listener.onError("Failed to connect: " + exception.getMessage());
                    }
                }
            });

            Log.d(TAG, "Starting WebSocket connection...");
            webSocket.connectAsynchronously();
        } catch (IOException e) {
            Log.e(TAG, "Failed to create WebSocket: " + e.getMessage());
            Log.e(TAG, "IOException details: ", e);
            if (listener != null) {
                listener.onError("Connection failed: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            Log.e(TAG, "Exception details: ", e);
            if (listener != null) {
                listener.onError("Unexpected error: " + e.getMessage());
            }
        }
    }

    private void sendStompConnect() {
        String connectFrame = "CONNECT\n" +
            "accept-version:1.1,1.0\n" +
            "heart-beat:10000,10000\n" +
            "\n" +
            "\0";
        webSocket.sendText(connectFrame);
        Log.d(TAG, "STOMP CONNECT sent");
    }

    private void subscribeToUserQueue() {
        if (!isSubscribed && currentUser != null) {
            String dest = "/topic/user/" + currentUser.getId() + "/notifications";
            String subscribeFrame = "SUBSCRIBE\n" +
                    "id:sub-" + currentUser.getId() + "\n" +
                    "destination:" + dest + "\n\n" +
                    "\0";
            webSocket.sendText(subscribeFrame);
            isSubscribed = true;
            Log.d(TAG, "Subscribed to " + dest);
        }
    }


    private void handleStompMessage(String message) {
        try {
            if (message.startsWith("CONNECTED")) {
                Log.d(TAG, "STOMP connection established");
                subscribeToUserQueue();
                return;
            }

            if (message.startsWith("MESSAGE")) {
                try {
                    // ====== Izvlačenje JSON tela iz STOMP poruke ======
                    String[] lines = message.split("\n");
                    StringBuilder bodyBuilder = new StringBuilder();
                    boolean isBody = false;

                    for (String line : lines) {
                        if (isBody) {
                            bodyBuilder.append(line).append("\n");
                        }
                        if (line.trim().isEmpty()) {
                            isBody = true; // telo počinje posle prazne linije
                        }
                    }

                    String body = bodyBuilder.toString().replace("\u0000", "").trim();
                    if (body.isEmpty()) {
                        Log.w(TAG, "STOMP MESSAGE body is empty");
                        return;
                    }

                    // ====== Parsiranje payload-a ======
                    NotificationPayload payload = gson.fromJson(body, NotificationPayload.class);
                    Log.d(TAG, "Primljena notifikacija: " + payload.title + " → " + payload.message);

                    // ====== Provera da li je 'mute' uključeno ======
                    LoginService loginService = new LoginService(appContext);
                    boolean isMuted = loginService.isNotificationsMuted();

                    if (!isMuted) {
                        NotifHelper.show(appContext, payload.title, payload.message, /* deepLink */ null);
                    } else {
                        Log.d(TAG, "Notifikacija utišana - nije prikazana korisniku");
                    }

                    // ====== Callback za UI refresh (opciono) ======
                    if (listener != null) {
                        listener.onMessageReceived(null); // možeš i payload da pošalješ
                    }

                } catch (Exception ex) {
                    Log.e(TAG, "Greška pri parsiranju STOMP MESSAGE: " + ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing STOMP message: " + e.getMessage());
        }
    }

    public void sendMessage(String content, User recipient) {
        if (webSocket != null && isConnected) {
            try {
                // Create complete Message with User objects
                Message message = new Message();
                message.setContent(content);
                message.setTimestamp(java.time.LocalDateTime.now());
                message.setSender(currentUser);
                message.setRecipient(recipient);
                
                // Convert Message to JSON (LocalDateTime will be serialized as ISO string)
                String messageJson = gson.toJson(message);
                
                String stompFrame = "SEND\n" +
                    "destination:/app/chat-private\n" +
                    "content-type:application/json\n" +
                    "\n" +
                    messageJson + "\0";
                
                webSocket.sendText(stompFrame);
                Log.d(TAG, "Message sent: " + content);
                Log.d(TAG, "JSON sent: " + messageJson);
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to send message: " + e.getMessage());
                Log.e(TAG, "Error details: ", e);
                if (listener != null) {
                    listener.onError("Failed to send message: " + e.getMessage());
                }
            }
        } else {
            Log.w(TAG, "WebSocket not connected");
            if (listener != null) {
                listener.onError("Not connected to chat server");
            }
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.disconnect();
            webSocket = null;
            isConnected = false;
            isSubscribed = false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
} 