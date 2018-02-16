package info.duskovi.pavel.memorylocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * This class is triggered by Android Alarm Manager at certain intervals.
 * It creates notifications with random item that is retrieved from the database.
 */

public class LockerBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //Intent lockerIntent = new Intent(context.getApplicationContext(), Locker.class);
        //PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 1, lockerIntent, 0);
        Log.i("BroadcastReceiver", "received intent");
        Item randomItem = getRandomItem(context);
        Intent notificationIntent = new Intent(context.getApplicationContext(), Locker.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), randomItem.rowid, notificationIntent, 0);
        Notification notification = new Notification.Builder(context.getApplicationContext())
                .setContentTitle("MemoryLocker:\n" + randomItem.question)
                .setContentText("…\n…\n" + randomItem.answer)
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(randomItem.rowid, notification);


    }
    private Item getRandomItem(Context context) {
        //connect to the SQL database and get random item
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
}
