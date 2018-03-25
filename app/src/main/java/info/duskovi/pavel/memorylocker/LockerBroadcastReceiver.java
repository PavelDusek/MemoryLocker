package info.duskovi.pavel.memorylocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase;
import java.text.DecimalFormat;
import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * This class is triggered by Android Alarm Manager at certain intervals.
 * It creates notifications with random item that is retrieved from the database.
 */

public class LockerBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /**
         * This method calls runNotification to decide, if a notification should be triggered.
         * If true, it calls getRandomItem to get a random item
         * and it triggers a notification with this random item.
         */
        Log.i("BroadcastReceiver", "received intent");
        if (runNotification(context)) {
            Item randomItem = getRandomItem(context);
            Intent notificationIntent = new Intent(context.getApplicationContext(), Locker.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), randomItem.rowid, notificationIntent, 0);
            Notification notification = new Notification.Builder(context.getApplicationContext())
                    .setContentTitle("MemoryLocker")
                    .setContentText(randomItem.question)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setStyle(new Notification.BigTextStyle().bigText(randomItem.answer))
                    .build();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(randomItem.rowid, notification);
        }
    }
    protected Item getRandomItem(Context context) {
        /**
         * Connect to the SQL database and get random item
         */
        Log.i("Database", "Trying to connect");
        SQLiteDatabase database = context.openOrCreateDatabase("MemoryLockerItems", context.MODE_PRIVATE, null);
        Log.i("Database", "Connected");
        Cursor c = database.rawQuery("SELECT rowid, * FROM items ORDER BY RANDOM() LIMIT 1", null);
        int questionIndex = c.getColumnIndex("question");
        int answerIndex = c.getColumnIndex("answer");
        int rowidIndex = c.getColumnIndex("rowid");
        c.moveToPosition(0);
        Item item = new Item(c.getInt(rowidIndex), c.getString(questionIndex), c.getString(answerIndex));
        c.close();
        return item;
    }
    protected boolean runNotification(Context context) {
        /**
         * It connects to application shared preferences
         * and decides whether a notification with reminder should be run.
         */
        SharedPreferences sharedPreferences = context.getSharedPreferences("info.duskovi.pavel.memorylocker", Context.MODE_PRIVATE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        String day;
        switch(calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY: day = "monday"; break;
            case Calendar.TUESDAY: day = "tuesday"; break;
            case Calendar.WEDNESDAY: day = "wednesday"; break;
            case Calendar.THURSDAY: day = "thursday"; break;
            case Calendar.FRIDAY: day = "friday"; break;
            case Calendar.SATURDAY: day = "saturday"; break;
            case Calendar.SUNDAY: day = "sunday"; break;
            default: day = "";
        }
        DecimalFormat decimalFormat = new DecimalFormat("00");
        String hour = "hour" + decimalFormat.format(calendar.get(Calendar.HOUR_OF_DAY));
        Log.i("Day", day);
        Log.i("Hour", hour);
        return sharedPreferences.getBoolean(day, false) && sharedPreferences.getBoolean(hour, false);
    }
}
