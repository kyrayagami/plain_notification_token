package net.kikuchy.plain_notification_token;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONObject;

//public class ScheduledNotificationBootReceiver extends BroadcastReceiver
public class ScheduledNotificationBootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        System.out.println("rebooted");
        Log.e("BootReceiver","hola llego al ScheduledNotificationBootReceiver");
        if (action != null && action.equals(android.content.Intent.ACTION_BOOT_COMPLETED)) {
            //PlainNotificationTokenPlugin.getPushData(context, intent);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            //PlainNotificationTokenPlugin.showNotification(context,pnData);
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
        }
    }
}