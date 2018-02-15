package info.duskovi.pavel.memorylocker;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Created by pavel on 15.2.18.
 */

public class LockerBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //Intent lockerIntent = new Intent(context.getApplicationContext(), Locker.class);
        //PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 1, lockerIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext());
        builder.setContentTitle("MemoryLocker")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light_normal)
                .setContentText("Check memory");

        Notification notification = builder.build();
        notification.notify();
        Toast.makeText(context, "Alarm", Toast.LENGTH_LONG).show();
    }
}
