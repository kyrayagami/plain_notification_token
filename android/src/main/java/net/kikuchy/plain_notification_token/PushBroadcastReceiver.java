package net.kikuchy.plain_notification_token;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;


/*
import com.libertacao.libertacao.MyApp;
import com.libertacao.libertacao.R;
import com.libertacao.libertacao.persistence.UserPreferences;
import com.libertacao.libertacao.view.event.EventDetailActivity;
*/

import com.parse.ManifestInfo;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;

//import io.flutter.Log;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static net.kikuchy.plain_notification_token.PlainNotificationTokenPlugin.CLICK_ACTION_VALUE_adair;
import static net.kikuchy.plain_notification_token.PlainNotificationTokenPlugin.SELECT_NOTIFICATION;
import static net.kikuchy.plain_notification_token.PlainNotificationTokenPlugin.channelName;
//import timber.log.Timber;

public class PushBroadcastReceiver extends BroadcastReceiver {
    // TODO: group notifications
    // TODO: add default actions, like going
    // TODO: add delete actions, like hide notifications today


    // The name of the Intent extra which contains the JSON payload of the Notification.

    public static final String KEY_PUSH_DATA = "com.parse.Data";
    public static final String channelId = "plain_notification_token";
    private static final String PAYLOAD = "payload";
    public static final String PROPERTY_PUSH_ICON = "com.parse.push.notification_icon";

    //The name of the Intent fired when a push has been received.

    public static final String ACTION_PUSH_RECEIVE = "com.parse.push.intent.RECEIVE";


    //The name of the Intent fired when a notification has been opened.

    public static final String ACTION_PUSH_OPEN = "com.parse.push.intent.OPEN";

    // The name of the Intent fired when a notification has been dismissed.

    public static final String ACTION_PUSH_DELETE = "com.parse.push.intent.DELETE";

    protected static final int SMALL_NOTIFICATION_MAX_CHARACTER_LIMIT = 38;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        switch (intentAction) {
            case ACTION_PUSH_RECEIVE:
                onPushReceive(context, intent);
                break;
            case ACTION_PUSH_DELETE:
                onPushDismiss(context, intent);
                break;
            case ACTION_PUSH_OPEN:
                onPushOpen(context, intent);
                break;
        }
    }

    protected void onPushReceive(Context context, Intent intent) {
        Log.e("PushBroadcastReceiver","llego al onPushReceive");
//        if (!UserPreferences.isNotificationEnabled()) {
//            return;
//        }


        JSONObject pushData = null;
        try {
            pushData = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
        } catch (JSONException | NullPointerException e) {

            Log.e(KEY_PUSH_DATA,"Unexpected exception when receiving push data: " + e);
        }

        // If the push data includes an action string, that broadcast intent is fired.
        String action = null;
        if (pushData != null) {
            action = pushData.optString("action", null);
        }
        if (action != null) {
            Bundle extras = intent.getExtras();
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtras(extras);
            broadcastIntent.setAction(action);
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
        }

        Notification notification = getNotification(context, intent);

        if (notification != null) {
            Log.e("onPushReceive","va a llamar a PushNotificationManager");

            PushNotificationManager.getInstance().showNotification(context, notification);
        }
    }

    protected void onPushDismiss(Context context, Intent intent) {
        // do nothing
    }

    protected void onPushOpen(Context context, Intent intent) {
        // Send a Parse Analytics "push opened" event
        //ParseAnalytics.trackAppOpenedInBackground(intent);
        //PlainNotificationTokenPlugin.onNewIntent(intent);
        // aqui lamar al methodo de channel app
        PushNotificationManager.getInstance().initParse(context);
        Log.e("PushBroadcastReceiver","llego al onPushOpen");


        String uriString = null;

        String eventObjectId = null;

        try {
            JSONObject pushData = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
            uriString = pushData.optString("uri", null);
            eventObjectId = pushData.optString("eventObjectId", null);
        } catch (JSONException e) {
            Log.e(KEY_PUSH_DATA,"Unexpected JSONException when receiving push data: " + e);
        }
        Log.e("onPushOpen",intent.getExtras().toString());


        Class<? extends Activity> cls = getActivity(context, intent);
        Intent activityIntent;
        if (uriString != null) {
            activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
        } else if(!TextUtils.isEmpty(eventObjectId)) {
            //activityIntent = new Intent(EventDetailActivity.newIntent(context, eventObjectId));
            //activityIntent = new Intent(EventDetailActivity.newIntent(context, eventObjectId));
            activityIntent = new Intent(context, getMainActivityClass(context));
        } else {
            activityIntent = new Intent(context, cls);
        }

        //activityIntent.putExtras(intent.getExtras());



        JSONObject pnData = getPushData(intent);


        //uriString = pnData.optString("uri");
        //Intent activityIntent = uriString != null ? new Intent(context, getActivity(context, intent))
        //        : new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));

        activityIntent.putExtras(intent).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

        //ParseAnalytics.trackAppOpened(intent);
        // Send a Parse Analytics "push opened" event
        ParseAnalytics.trackAppOpenedInBackground(intent);

        // allow a urlHash parameter for hash as well as query params.
        // This lets the app know what to do at coldstart by opening a PN.
        // For example: navigate to a specific page of the app
        String urlHash = pnData.optString("urlHash");
        if (urlHash.startsWith("#") || urlHash.startsWith("?")) {
            activityIntent.putExtra("urlHash", urlHash);
        }

        context.startActivity(activityIntent);
        //LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        //case "select":
        //Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //context.sendBroadcast(closeDialog);
        //String packageName = context.getPackageName();
        //PackageManager pm = context.getPackageManager();
        //Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
        //context.startActivity(launchIntent);
        try{
            // solo se deberia ejecutar si la aplicacion ya se ha iniciado
            PlainNotificationTokenPlugin.callEvent("onResume", intent);
        }catch(Exception e){
            Log.e("","");
        }

    }

    private static Class getMainActivityClass(Context context) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null) {
            return null;
        }
        String className = launchIntent.getComponent().getClassName();
        Class<? extends Activity> cls = null;
        try {
            cls = (Class<? extends Activity>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            // do nothing
        }
        return cls;
    }

    /*protected int getSmallIconId(Context context, Intent intent) {
        return R.drawable.app_icon;
    }
    */
    protected int getSmallIconId(Context context, Intent intent) {
        Bundle metaData = ManifestInfo.getApplicationMetadata(context);
        int explicitId = 0;
        if (metaData != null) {
            explicitId = metaData.getInt(PROPERTY_PUSH_ICON);
        }
        return explicitId != 0 ? explicitId : ManifestInfo.getIconId();
    }

    protected Bitmap getLargeIcon(Context context, Intent intent) {
        return null;
    }

    private JSONObject getPushData(Intent intent) {
        try {
            return new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
        } catch (JSONException e) {
            Log.e("","Unexpected JSONException when receiving push data: " + e);
            return null;
        }
    }

    protected Notification getNotification(Context context, Intent intent) {
        JSONObject pushData = getPushData(intent);
        if (pushData == null || (!pushData.has("alert") && !pushData.has("title"))) {
            return null;
        }

        //String title = pushData.optString("title", MyApp.getAppContext().getString(R.string.app_name));
        String title = pushData.optString("title") != "" ? pushData.optString("title") : ManifestInfo.getDisplayName(context);
        //String alert = pushData.optString("alert", "Notification received.");
        String alert = pushData.optString("alert");
        String tickerText = String.format(Locale.getDefault(), "%s: %s", title, alert);

        Bundle extras = intent.getExtras();


        Random random = new Random();
        int contentIntentRequestCode = random.nextInt();
        int deleteIntentRequestCode = random.nextInt();

        // Security consideration: To protect the app from tampering, we require that intent filters
        // not be exported. To protect the app from information leaks, we restrict the packages which
        // may intercept the push intents.
        String packageName = context.getPackageName();

        Intent contentIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_OPEN);
        //Intent contentIntent = new Intent(NewTokenReceiveService.ACTION_REMOTE_MESSAGE);
        contentIntent.putExtras(extras);

        //////////////////// CLICK_ACTION_VALUE_adair

        contentIntent.putExtra("click_action", "action");
        //contentIntent.setAction(SELECT_NOTIFICATION);


        contentIntent.setPackage(packageName);

        Intent deleteIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_DELETE);
        deleteIntent.putExtras(extras);
        deleteIntent.setPackage(packageName);

        PendingIntent pContentIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode,
                deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//        // The purpose of setDefaults(Notification.DEFAULT_ALL) is to inherit notification properties
//        // from system defaults
//        NotificationCompat.Builder parseBuilder = new NotificationCompat.Builder(context, channelId);
//        parseBuilder.setContentTitle(title)
//                .setContentText(alert)
//                .setTicker(tickerText)
//                .setSmallIcon(this.getSmallIconId(context, intent))
//                .setLargeIcon(this.getLargeIcon(context, intent))
//                .setContentIntent(pContentIntent)
//                .setDeleteIntent(pDeleteIntent)
//                .setAutoCancel(true)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                .setLights(Color.GREEN, 1000, 300)
//                .setDefaults(Notification.DEFAULT_ALL);

//        if (alert != null
//                && alert.length() > SMALL_NOTIFICATION_MAX_CHARACTER_LIMIT) {
//            parseBuilder.setStyle(new NotificationCompat.Builder.BigTextStyle().bigText(alert));
//        }

        //notifiacion modificado
        NotificationCompat.Builder parseBuilder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(alert)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(alert))

                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pContentIntent)
                .setContentInfo(alert)
                .setDeleteIntent(pDeleteIntent)
                .setLargeIcon(this.getLargeIcon(context, intent))
                .setTicker(tickerText)
                //.setLargeIcon(icon)
                //.setSmallIcon(R.dr)
                //.setSmallIcon(fd)
                //.setColor(Color.RED)

                .setLights(Color.GREEN, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(this.getSmallIconId(context, intent))
                ;
                //.setSmallIcon(R.drawable.app_icon);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE); //getSystemService(context.NOTIFICATION_SERVICE);

        // Notification Channel is required for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channelL = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            //channelL.setDescription("channel description");
            channelL.setShowBadge(true);
            channelL.canShowBadge();
            channelL.enableLights(true);
            channelL.setLightColor(Color.GREEN);
            channelL.enableVibration(true);
            channelL.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            notificationManager.createNotificationChannel(channelL);
        }

        return parseBuilder.build();
    }

//
//    static void showNotification(Context context, JSONObject data){
//
//
//        /*AssetManager assetManager = registrar.context().getAssets();
//        String key = registrar.lookupKeyForAsset("icons/icon_c10.png");
//        AssetFileDescriptor fd = assetManager.openFd(key);*/
//
//        //ContextCompat.getDrawable(context, R.drawable.icon_c10.png)
//        Log.e("ADAIR SHOWNOTI","ENTRO AL SHOW NOTI");
//        Intent intent = new Intent(context , getMainActivityClass(context));
//        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        String title = data.optString("title") != "" ? data.optString("title") : ManifestInfo.getDisplayName(context);
//
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
//                .setContentTitle(title)
//                .setContentText(data.optString("alert") )
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(data.optString("alert")))
//
//                .setAutoCancel(true)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                .setContentIntent(pendingIntent)
//                .setContentInfo(data.optString("alert") )
//                //.setLargeIcon(icon)
//                //.setSmallIcon(R.dr)
//                //.setSmallIcon(fd)
//                //.setColor(Color.RED)
//
//                .setLights(Color.GREEN, 1000, 300)
//                .setDefaults(Notification.DEFAULT_VIBRATE)
//                .setSmallIcon(R.drawable.app_icon);
//
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE); //getSystemService(context.NOTIFICATION_SERVICE);
//
//        // Notification Channel is required for Android O and above
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channelL = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
//            //channelL.setDescription("channel description");
//            channelL.setShowBadge(true);
//            channelL.canShowBadge();
//            channelL.enableLights(true);
//            channelL.setLightColor(Color.GREEN);
//            channelL.enableVibration(true);
//            channelL.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
//            notificationManager.createNotificationChannel(channelL);
//        }
//        int random = new Random().nextInt(100000);
//        Log.e("RANDOM ", ""+random);
//        notificationManager.notify(random, notificationBuilder.build());
//    }

}
