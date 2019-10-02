package net.kikuchy.plain_notification_token;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.parse.PLog;
import com.parse.Parse;
import com.parse.PushRouter;
import com.parse.fcm.ParseFCM;

import org.json.JSONException;
import org.json.JSONObject;

public class NewTokenReceiveService extends FirebaseMessagingService {
    public static String ACTION_TOKEN = "ACTION_TOKEN";
    public static String EXTRA_TOKEN = "EXTRA_TOKEN";
    public static final String ACTION_REMOTE_MESSAGE = "net.kikuchy.plain_notification_token";
    public static final String EXTRA_REMOTE_MESSAGE = "notification";

    @Override
    public void onNewToken(String token) {
        final Intent intent = new Intent(ACTION_TOKEN);
        intent.putExtra(EXTRA_TOKEN, token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
/*
        Intent intent = new Intent(ACTION_REMOTE_MESSAGE);
        intent.putExtra(EXTRA_REMOTE_MESSAGE, remoteMessage);
        Log.e("TEST PARSE NEW","HOLA LLEGO AL onMessageReceived");
        try{
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            //PlainNotificationTokenPlugin.getPushData(this,intent);

        }catch(Exception e){
            Log.e("ERROR DEL BROADCAST", e.toString());
        }
        //super.onMessageReceived(remoteMessage);
        */
        super.onMessageReceived(remoteMessage);
        PLog.d("ParseFCM.TAG", "onMessageReceived");
        PLog.d("ParseFCM.TAG", remoteMessage.getData().toString());
        String pushId = remoteMessage.getData().get("push_id");
        String timestamp = remoteMessage.getData().get("time");
        String dataString = remoteMessage.getData().get("data");
        String channel = remoteMessage.getData().get("channel");

        JSONObject data = null;
        if (dataString != null) {
            try {
                data = new JSONObject(dataString);
            } catch (JSONException e) {
                PLog.e("ParseFCM.TAG", "Ignoring push because of JSON exception while processing: " + dataString, e);
                return;
            }
        }
        PushNotificationManager.getInstance().initParse(getApplicationContext());
        //initParse(getApplicationContext());

        PushRouter.getInstance().handlePush(pushId, timestamp, channel, data);
    }


}
