package net.kikuchy.plain_notification_token;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.RemoteMessage;
import com.parse.ManifestInfo;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.fcm.ParseFCM;
//import com.parse.fcm.ParseFCM;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import com.parse.ParsePushBroadcastReceiver;

//import static com.parse.Parse.getApplicationContext;

/**
 * PlainNotificationTokenPlugin
 */
public class PlainNotificationTokenPlugin implements MethodCallHandler, PluginRegistry.NewIntentListener {
    /**
     * Plugin registration.
     */
    private static final String CLICK_ACTION_VALUE = "FLUTTER_NOTIFICATION_CLICK";
    public static String CLICK_ACTION_VALUE_adair = "click_action_notification";

    static final String TAG = PlainNotificationTokenPlugin.class.getSimpleName();

    private String lastToken = null;
    private static MethodChannel channel;
    private Registrar registrar;
    public static String channelId = "plain_notification_token";
    static String channelName = "plain_notification_token";
    public static String SELECT_NOTIFICATION = "SELECT_NOTIFICATION";

    Activity context;

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "plain_notification_token");
        channel.setMethodCallHandler(new PlainNotificationTokenPlugin(channel, registrar));
        //PlainNotificationTokenPlugin.channel.setMethodCallHandler(new MediaNotificationPlugin(registrar));

        //PlainNotificationTokenPlugin plugin = new PlainNotificationTokenPlugin(channel, registrar);
        //PlainNotificationTokenPlugin plugin = new PlainNotificationTokenPlugin(channel, registrar);

        PlainNotificationTokenPlugin.channel = new MethodChannel(registrar.messenger(), "plain_notification_token");
        PlainNotificationTokenPlugin.channel.setMethodCallHandler(new PlainNotificationTokenPlugin(channel, registrar));

    }

    private PlainNotificationTokenPlugin(MethodChannel channel, Registrar registrar) {
        this.channel = channel;
        this.registrar =registrar;
        FirebaseApp.initializeApp(registrar.context());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NewTokenReceiveService.ACTION_TOKEN);
        intentFilter.addAction(NewTokenReceiveService.ACTION_REMOTE_MESSAGE);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(registrar.context());

        //manager.registerReceiver(this, intentFilter);

        this.context = registrar.activity();
    }

    @Override
    public void onMethodCall(final MethodCall call, final Result result) {
        /*if (call.method.equals("getToken")) {
            FirebaseInstanceId.getInstance()
                    .getInstanceId()
                    .addOnCompleteListener(
                            new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "getToken, error fetching instanceID: ", task.getException());
                                        result.success(null);
                                        return;
                                    }

                                    result.success(task.getResult().getToken());
                                }
                            });
        } else {
            result.notImplemented();
        }*/
        Log.e("onMethodCall", call.method);
        switch (call.method) {

            case "configure":
                break;
            case "getInstallationsObjectId":
                break;
            case "getSubscriptions":

                List<String> subscriptions = ParseInstallation.getCurrentInstallation().getList("channels");
                JSONArray subcriptionsArray = new JSONArray();
                if (subscriptions != null) {
                    subcriptionsArray= new JSONArray(subscriptions);
                }
                result.success(subcriptionsArray);

                break;
            case "subscribe":

                String channelSub = call.argument("channel");
                ParsePush.subscribeInBackground(channelSub);
                result.success(null);

                break;
            case "unsubscribe":

                String channelUnSub = call.argument("channel");
                ParsePush.unsubscribeInBackground(channelUnSub);
                result.success(null);

                break;
            case "getToken":
                FirebaseInstanceId.getInstance()
                        .getInstanceId()
                        .addOnCompleteListener(
                                new OnCompleteListener<InstanceIdResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                        if (!task.isSuccessful()) {
                                            Log.w(TAG, "getToken, error fetching instanceID: ", task.getException());
                                            result.success(null);
                                            return;
                                        }

                                        result.success(task.getResult().getToken());
                                    }
                                });
                break;
            case "initParse":
                Log.e("ADAIR", "Method initParse");
                String serverUrl= call.argument("serverUrl"), applicationId = call.argument("applicationId"), clientKey = call.argument("clientKey");
                //new ParsePushApplication().initParse(context, serverUrl, applicationId, clientKey);
                PushNotificationManager.getInstance().initParse(context);

                //initParse(context, serverUrl,applicationId, clientKey);
                Log.e("initParse", registrar.activity().toString());
                Log.e("initParse", registrar.activity().getIntent().toString());
                if (registrar.activity() != null){
                    Log.e("onLaunch","llego al onLaunch de test");
                    sendMessageFromIntent("onLaunch", registrar.activity().getIntent());
                }
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    public static void callEvent(String event, Intent intent) {
        Map<String, Object> message = new HashMap<>();
        switch (event) {
            case "onResume":
                Log.e("onMethodCall", "llego al onResume");
                Bundle extras = intent.getExtras();
                Log.e("callEvent", extras.toString());
                Log.e("callEvent",event);

                Map<String, Object> dataMap = new HashMap<>();

                for (String key : extras.keySet()) {
                    Object extra = extras.get(key);
                    if (extra != null) {
                        dataMap.put(key, extra);
                    }
                }
                message.put("data", dataMap.get("com.parse.Data"));
                break;
        }
        PlainNotificationTokenPlugin.channel.invokeMethod(event, message, new Result() {
            @Override
            public void success(Object o) {
                // this will be called with o = "some string"
            }

            @Override
            public void error(String s, String s1, Object o) {}

            @Override
            public void notImplemented() {}
        });
    }
     /*
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Log.e("onReceive","hola llego al onReceive del broadcast");
        Log.e("onReceive", "action " + action);

        if (action == null) {
            return;
        }

        if (action.equals(NewTokenReceiveService.ACTION_TOKEN)) {
            String token = intent.getStringExtra(NewTokenReceiveService.EXTRA_TOKEN);
            channel.invokeMethod("onToken", token);
        } else if (action.equals(NewTokenReceiveService.ACTION_REMOTE_MESSAGE)) {
            //JSONObject content2 =
            // RemoteMessage message = intent.getParcelableExtra(ParseFirebaseMessagingServiceAdair.EXTRA_REMOTE_MESSAGE);
            // com.google.firebase.messaging.RemoteMessage
            //Map<String, Object> content =  parseRemoteMessage(message);

            JSONObject content = getPushData(context, intent);


            if (content == null){
                Log.e("adairParse","Content es nulo");
            }
            //if (content.has("title")){
            //  Log.e("adairParse","tiene titulo" + content.optString("title"));
            //}
            Log.e("DATATEST","llego al data");
            RemoteMessage message = intent.getParcelableExtra(NewTokenReceiveService.EXTRA_REMOTE_MESSAGE);

            //channel.invokeMethod("onMessage", parseRemoteMessage(message) ); //se quito por que no me sirve
            try {
                //channel.invokeMethod( "onMessage", content);
                Log.e("TEST","TES TEST");
                channel.invokeMethod("onMessage", parseRemoteMessage(message) );
                //channel.invokeMethod("onMessage", message.getData().get("data"));
                Log.e("onReceive","todo bien con channel.invokeMethod");
            }catch (Exception e){
                Log.e("Exception",e.toString());
                Log.e("onReceive","ocurrio un error en el parce remote");
            }

//            RemoteMessage message = null;
//            try {
//                message = intent.getParcelableExtra(NewTokenReceiveService.EXTRA_REMOTE_MESSAGE);
//                Log.e("onReceive","todo bien con channel.invokeMethod");
//            }catch (Exception e){
//                Log.e("onReceive","ocurrio un error en el parce remote");
//            }
//            try{
//                channel.invokeMethod( "onMessage", message.getData().get("data"));
//                Log.e("onReceive","todo bien con channel.invokeMethod");
//            }catch (Exception e){
//                Log.e("onReceive","ocurrio un error en el channel.invokeMethod");
//            }


//            channel.invokeMethod("onMessage", content, new Result() {
//
//                @Override
//                public void success(Object o) {
//                    // this will be called with o = "some string"
//                }
//
//                @Override
//                public void error(String s, String s1, Object o) {}
//
//                @Override
//                public void notImplemented() {}
//            });

        }

    }
    */




    public static JSONObject getPushData(Context context, Intent intent) {
        JSONObject pnData = null;
        //Log.e("adairparse", intent.toString());
        RemoteMessage message = intent.getParcelableExtra(NewTokenReceiveService.EXTRA_REMOTE_MESSAGE);
        Log.e("ADAIR MESSAGE", message.toString());
        Log.e("ADAIR DATA", message.getData().toString());
        try {
            //pnData = new JSONObject(intent.getStringExtra(ParseFirebaseMessagingServiceAdair.ACTION_REMOTE_MESSAGE+"."+ParseFirebaseMessagingServiceAdair.EXTRA_REMOTE_MESSAGE));
            pnData = new JSONObject(message.getData().get("data"));
            Log.e("pnData", pnData.toString());
            Log.e("getPushData","aqui se deberia mostrar la notificacion");
            showNotification(context,pnData);
            //Log.e("getPushData","aqui se deberia mostrar la notificacion");
        } catch (Exception e) {
            Log.e("adairparse", "JSONException while parsing push data:", e);
        } finally {
            Log.e("adairparse", "Json parse succesfull");
            return pnData;
        }

    }

    private static JSONObject getOnlyPushData(Intent intent) {
        JSONObject pnData = null;
        try {
            pnData = new JSONObject(intent.getStringExtra("com.parse.Data"));
        } catch (JSONException e) {
            Log.e("getOnlyPushData", "JSONException while parsing push data:", e);
        } finally {
            return pnData;
        }
    }


    static void showNotification(Context context, JSONObject data){


        /*AssetManager assetManager = registrar.context().getAssets();
        String key = registrar.lookupKeyForAsset("icons/icon_c10.png");
        AssetFileDescriptor fd = assetManager.openFd(key);*/

        //ContextCompat.getDrawable(context, R.drawable.icon_c10.png)
        Log.e("ADAIR SHOWNOTI","ENTRO AL SHOW NOTI");
        Intent intent = new Intent(context , getMainActivityClass(context));
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String title = data.optString("title") != "" ? data.optString("title") : ManifestInfo.getDisplayName(context);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(data.optString("alert") )
                .setStyle(new NotificationCompat.BigTextStyle().bigText(data.optString("alert")))

                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(data.optString("alert") )
                //.setLargeIcon(icon)
                //.setSmallIcon(R.dr)
                //.setSmallIcon(fd)
                //.setColor(Color.RED)

                .setLights(Color.GREEN, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.app_icon);

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
        int random = new Random().nextInt(100000);
        Log.e("RANDOM ", ""+random);
        notificationManager.notify(random, notificationBuilder.build());
    }



    @NonNull
    private Map<String, Object> parseRemoteMessage(RemoteMessage message) {
        Map<String, Object> content = new HashMap<>();
        content.put("data", message.getData().get("data"));
        /*
        RemoteMessage.Notification notification = message.getNotification();

        Map<String, Object> notificationMap = new HashMap<>();
        content.optString("title");
        String title = notification != null ? notification.getTitle() : null;
        notificationMap.put("title", title);

        String body = notification != null ? notification.getBody() : null;
        notificationMap.put("data", body);

        content.put("notification", notificationMap);
        */
        return content;
    }


    /** @return true if intent contained a message to send. */
    private boolean sendMessageFromIntent(String method, Intent intent) {
        //Log.e("sendMessageFromIntent", intent.getStringExtra());
        //intent.getData()

        //Log.e("sendMessageFromIntent2",intent.getDataString());

        JSONObject pnData = getOnlyPushData(intent);
        try {
            Log.e("sendMessageFromIntent", pnData.toString());
        }catch (Exception e){
            Log.e("ERROR","sendMessageFromIntent getOnlyPushData");
        }
        /*
        try {
            Map<String, Object> message = new HashMap<>();
            Bundle extras = intent.getExtras();
            Log.e("sendMessageFromIntent", "sendMessageFromIntent");
            Log.e("Method",method);
            if (extras == null) {
                return false;
            }

            Map<String, Object> notificationMap = new HashMap<>();
            Map<String, Object> dataMap = new HashMap<>();

            for (String key : extras.keySet()) {
                Object extra = extras.get(key);
                if (extra != null) {
                    dataMap.put(key, extra);
                }
            }
            Log.e("sendMessageFromIntent", notificationMap.toString());
        }catch (Exception e){

        }
        */
        String action = "";
        ///boolean param = SELECT_NOTIFICATION.equals(registrar.activity().getIntent().getAction());
        ///Log.e("SELECT_NOTIFICATION", ""+param);

        try {
            //action = pnData.has("click_action")  ? pnData.optString("click_action") : "";
            action = intent.getStringExtra("click_action");
        }catch (Exception e){
            Log.e("SELECT_NOTIFICATION","erro al action " +e);
            action = "";
        }
        Log.e("ACTION",""+action);
        Log.e("intent.getAction()", ""+intent);
        if (PushBroadcastReceiver.ACTION_PUSH_OPEN.equals(intent.getAction())
                || "action".equals(action)) {
            Map<String, Object> message = new HashMap<>();
            Bundle extras = intent.getExtras();
            Log.e("sendMessageFromIntent", "sendMessageFromIntent");
            Log.e("Method",method);
            if (extras == null) {
                return false;
            }

            Map<String, Object> notificationMap = new HashMap<>();
            Map<String, Object> dataMap = new HashMap<>();

            for (String key : extras.keySet()) {
                Object extra = extras.get(key);
                if (extra != null) {
                    dataMap.put(key, extra);
                }
            }

            //message.put("notification", notificationMap);
            message.put("data", dataMap.get("com.parse.Data"));


            Log.e("dataMap",dataMap.toString());
            channel.invokeMethod(method, message);
            return true;
        }
        return false;
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

    @Override
    public boolean onNewIntent(Intent intent) {
        Log.e("holi", "adair");
        boolean res = sendMessageFromIntent("onResume", intent);
        if (res && registrar.activity() != null) {
            registrar.activity().setIntent(intent);
        }
        return res;
    }

    /*
    public static void NewIntent(Intent intent) {
        boolean res = sendMessageFromIntent("onResume", intent);
        if (res && registrar.activity() != null) {
            this.registrar.activity().setIntent(intent);
        }
        return res;
    } */



    /*public void initParse(Context context,String serverUrl, String applicationId,String clientKey){
        Log.e("ParsePushApplication","runining installation parse in ParsePushApplication");
        //String url = "http://192.168.2.1:1337/parse/";
        //String url = "http://10.0.0.6:1337/parse/";
        //String applicationId = "parseTestA";
        Log.e("ParsePush INIT","serverUrl" + serverUrl);
        Log.e("ParsePush INIT","applicationId" + applicationId);
        Parse.initialize(new Parse.Configuration.Builder(context)
                .applicationId(applicationId)
                .server(serverUrl)
                .clientKey(clientKey)
                .build()
        );

        try {
            createNewToken();

        }catch (RuntimeException ex){
            Log.e("ERRORRRRRRR","ERROR NO SE GUARDA EL TOKEN");
        }
    }*/


    public static void createNewToken(){
        Log.d("Parser-server","InitParse");
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e("", "getInstanceId failed", task.getException());
                            //result.success(true);
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.e("getInstanceId token",""+ token);
                        try {
                            Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
                            ParseFCM.register(token);
                            Log.e("FIrebaseMessage", "Se registro con exito");
                        } catch (Exception ex) {
                            Log.e("FIrebaseMessage", "No se pudo hacer el registro");
                        }
                        //ParseFCM.register(token);
                    }
                });
    }

}
