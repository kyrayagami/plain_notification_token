package net.kikuchy.plain_notification_token;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.parse.Parse;

import java.util.concurrent.atomic.AtomicInteger;

public class PushNotificationManager {
    public static class Singleton {
        private static final PushNotificationManager INSTANCE = new PushNotificationManager();
    }

    public static PushNotificationManager getInstance() {
        return Singleton.INSTANCE;
    }

    private final AtomicInteger notificationCount = new AtomicInteger(0);

    public void showNotification(Context context, Notification notification) {
        if (context != null && notification != null) {
            notificationCount.incrementAndGet();

            // Fire off the notification
            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Pick an id that probably won't overlap anything
            int notificationId = (int)System.currentTimeMillis();

            try {
                nm.notify(notificationId, notification);
                Log.e("PushNotificationManager","todo bien, debio de mostrar la notificacion");
            } catch (SecurityException e) {
                // Some phones throw an exception for unapproved vibration
                Log.e("PushNotificationManager","ocurrio un error en la notificacion");
                Log.e("PushNotificationManager",e.toString());
                notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
                nm.notify(notificationId, notification);
            }
        }
    }

    public void initParse(Context context){
        String serverUrl ="", applicationId ="", clientKey ="";
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            //myApiKey = bundle.getString("ParseAppId");
            serverUrl = bundle.getString("ParseServerUrl");
            applicationId = bundle.getString("ParseAppId");
            clientKey = bundle.getString("ParseClientKey");
            //Log.e("initParse","myApiKey  is : " + myApiKey);
        } catch (Exception e) {
            Log.e("initParse", "Dear developer. Don't forget to configure <meta-data android:name=\"ParseAppId\" android:value=\"testValue\"/> in your AndroidManifest.xml file.");
        }
        Log.e("ParsePushApplication","runining installation parse in ParsePushApplication");
        //String url = "http://192.168.2.1:1337/parse/";
        //String applicationId = "parseTestA";
        //serverUrl = "https://parseapi.back4app.com/";
        //applicationId = "xlX7CBM2dolI89gS8KeSymxdXaLPlPh2xrv9vPye";
        //clientKey = "aOgAQ4zg29o9hdYy8SFNFCUcnXWXUbIoijh5qq2R";
        Log.e("ParsePush INIT","applicationId " + applicationId);
        Log.e("ParsePush INIT","serverUrl " + serverUrl);
        Log.e("ParsePush INIT","clientKey " + clientKey);

        Parse.initialize(new Parse.Configuration.Builder(context)
                .applicationId(applicationId)
                .server(serverUrl)
                .clientKey(clientKey)
                .build()
        );
        try {
            PlainNotificationTokenPlugin.createNewToken();

        }catch (RuntimeException ex){
            Log.e("ERRORRRRRRR","ERROR NO SE GUARDA EL TOKEN");
        }
    }
}
