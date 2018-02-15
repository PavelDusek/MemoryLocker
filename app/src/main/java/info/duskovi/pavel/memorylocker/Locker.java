package info.duskovi.pavel.memorylocker;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Locale;

public class Locker extends AppCompatActivity {
    ArrayList<String> questions;
    ArrayList<Item> items;
    ListView itemsListView;
    Item item;
    SQLiteDatabase database;
    NotificationCompat.Builder notification;
    SharedPreferences sharedPreferences;
    boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;
    boolean
            hour00, hour01, hour02, hour03, hour04, hour05, hour06, hour07, hour08, hour09, hour10, hour11,
            hour12, hour13, hour14, hour15, hour16, hour17, hour18, hour19, hour20, hour21, hour22, hour23;
    private static final int uniqueID = 45612;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        super.onOptionsItemSelected(menuItem);
        switch (menuItem.getItemId()) {
            case R.id.add_item:
                Log.i("Menu", "addItemAlert");
                showAddItemAlert();
                return true;
            case R.id.import_items:
                Log.i("Menu", "importItems");
                Toast.makeText(getApplicationContext(), R.string.todo, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.export_items:
                Log.i("Menu", "exportItems");
                Toast.makeText(getApplicationContext(), R.string.todo, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.set_timer: showSetTimerAlert(); return true;
            case R.id.about:
                Log.i("Menu", "showAboutAlert");
                showAboutAlert();
                return true;
            default:
                return false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker);
        Intent intent = new Intent(getApplicationContext(), Locker.class);
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);
        items = new ArrayList<Item>();
        questions = new ArrayList<String>();
        itemsListView = (ListView) findViewById(R.id.itemsListView);
        try {
            database = this.openOrCreateDatabase("MemoryLockerItems", MODE_PRIVATE, null);
            database.execSQL("DROP TABLE items");
            database.execSQL("CREATE TABLE IF NOT EXISTS items (question VARCHAR, answer VARCHAR)");
            databaseStartValues();
            updateItems();
        } catch (android.database.SQLException e) {
            databaseError();
            e.printStackTrace();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        /*********/
        /* TIMER */
        /*********/
        setDefaultSharedPreferences();
        setAlarm();
    }

    public void setAlarm() {
        Intent intent = new Intent(this, LockerBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //alarmManager.cancel(pendingIntent); //cancel if this has been run previously
        //Calendar nextRun = Calendar.getInstance();
        //nextRun.set(Calendar.HOUR_OF_DAY, nextRun.get(Calendar.HOUR_OF_DAY) + 1);
        //nextRun.set(Calendar.MINUTE, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000,AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);


        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, nextRun.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent);

        /*
        HashMap<String, Boolean> days = new HashMap<String, Boolean>();
        HashMap<String, Boolean> times = new HashMap<String, Boolean>();
        days.put("monday", monday); days.put("tuesday", tuesday); days.put("wednesday", wednesday); days.put("thursday", thursday); days.put("friday", friday); days.put("saturday", saturday); days.put("sunday", sunday);
        times.put("hour00", hour00); times.put("hour01", hour01); times.put("hour02", hour02); times.put("hour03", hour03); times.put("hour04", hour04); times.put("hour05", hour05); times.put("hour06", hour06); times.put("hour07", hour07); times.put("hour08", hour08); times.put("hour09", hour09); times.put("hour10", hour10); times.put("hour11", hour11); times.put("hour12", hour12); times.put("hour13", hour13); times.put("hour14", hour14); times.put("hour15", hour15); times.put("hour16", hour16); times.put("hour17", hour17); times.put("hour18", hour18); times.put("hour19", hour19); times.put("hour20", hour20); times.put("hour21", hour21); times.put("hour22", hour22); times.put("hour23", hour23);

        //Cancels all alarms and sets the alarms where appropriate (based on days and times)
        int daysCounter = 0;
        Iterator daysIterator = days.entrySet().iterator();
        while(daysIterator.hasNext()) {
            Map.Entry<String, Boolean> dayPair = (Map.Entry<String, Boolean>) daysIterator.next();
            String dayName = dayPair.getKey();
            Boolean day = dayPair.getValue();

            int timesCounter = 0;
            Iterator timesIterator = times.entrySet().iterator();
            while(timesIterator.hasNext()) {
                Map.Entry<String, Boolean> timePair = (Map.Entry<String, Boolean>) timesIterator.next();
                String timeName = timePair.getKey();
                Boolean time = timePair.getValue();

                //create Intent to communicate with AlarmManager
                Intent intent = new Intent(this, LockerBroadcastReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this.getApplicationContext(),
                        (daysCounter * 24)+timesCounter,
                        intent,
                        0
                );
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                //first cancel the alarm (if it had been set earlier)!!!
                alarmManager.cancel(pendingIntent);

                //set the alarm if appropriate
                if (day && time) {
                    alarmManager.setInexactRepeating();
                    alarmManager.set(AlarmManager.INTERVAL_HOUR)

                }
                timesIterator.remove();
                timesCounter++;
            }
            daysIterator.remove();
            daysCounter++;
        }
        */

    }
    private void showAddItemAlert() {
        LayoutInflater layoutInflater = getLayoutInflater();
        final View addItemView = layoutInflater.inflate(R.layout.add_item, null);
        AlertDialog addItemAlert = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_input_add)
                .setTitle(R.string.addItemTitle)
                .setView(addItemView)
                .setPositiveButton(R.string.addItemPositiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            insertIntoDatabase(
                                    new Item(
                                            getNewRowid(),
                                            ((EditText) addItemView.findViewById(R.id.newQuestion)).getText().toString(),
                                            ((EditText) addItemView.findViewById(R.id.newAnswer)).getText().toString()
                                    )
                            );
                            Log.i("Info", String.format("Question size: %d", questions.size()));
                            updateItems();
                            Toast.makeText(getApplicationContext(), R.string.itemAdded, Toast.LENGTH_SHORT).show();
                        } catch (android.database.SQLException e) {
                            e.printStackTrace();
                            databaseError();
                            Log.e("Error", e.getMessage());
                            Toast.makeText(getApplicationContext(), R.string.itemAddingFailed, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    private void showSetTimerAlert() {
        LayoutInflater layoutInflater = getLayoutInflater();
        final View setTimerView = layoutInflater.inflate(R.layout.set_timer, null);
        ((CheckBox) setTimerView.findViewById(R.id.MondayCheckBox)).setChecked(monday);
        ((CheckBox) setTimerView.findViewById(R.id.TuesdayCheckBox)).setChecked(tuesday);
        ((CheckBox) setTimerView.findViewById(R.id.WednesdayCheckBox)).setChecked(wednesday);
        ((CheckBox) setTimerView.findViewById(R.id.ThursdayCheckBox)).setChecked(thursday);
        ((CheckBox) setTimerView.findViewById(R.id.FridayCheckBox)).setChecked(friday);
        ((CheckBox) setTimerView.findViewById(R.id.SaturdayCheckBox)).setChecked(saturday);
        ((CheckBox) setTimerView.findViewById(R.id.SundayCheckBox)).setChecked(sunday);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox00)).setChecked(hour00);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox01)).setChecked(hour01);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox02)).setChecked(hour02);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox03)).setChecked(hour03);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox04)).setChecked(hour04);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox05)).setChecked(hour05);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox06)).setChecked(hour06);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox07)).setChecked(hour07);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox08)).setChecked(hour08);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox09)).setChecked(hour09);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox10)).setChecked(hour10);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox11)).setChecked(hour11);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox12)).setChecked(hour12);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox13)).setChecked(hour13);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox14)).setChecked(hour14);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox15)).setChecked(hour15);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox16)).setChecked(hour16);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox17)).setChecked(hour17);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox18)).setChecked(hour18);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox19)).setChecked(hour19);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox20)).setChecked(hour20);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox21)).setChecked(hour21);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox22)).setChecked(hour22);
        ((CheckBox) setTimerView.findViewById(R.id.CheckBox23)).setChecked(hour23);

        AlertDialog setTimerAlert = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_menu_manage)
                .setTitle(R.string.setTimerTitle)
                .setView(setTimerView)
                .setPositiveButton(R.string.setTimerPositiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        monday = ((CheckBox) setTimerView.findViewById(R.id.MondayCheckBox)).isChecked();
                        tuesday = ((CheckBox) setTimerView.findViewById(R.id.TuesdayCheckBox)).isChecked();
                        wednesday = ((CheckBox) setTimerView.findViewById(R.id.WednesdayCheckBox)).isChecked();
                        thursday = ((CheckBox) setTimerView.findViewById(R.id.ThursdayCheckBox)).isChecked();
                        friday = ((CheckBox) setTimerView.findViewById(R.id.FridayCheckBox)).isChecked();
                        saturday = ((CheckBox) setTimerView.findViewById(R.id.SaturdayCheckBox)).isChecked();
                        sunday = ((CheckBox) setTimerView.findViewById(R.id.SundayCheckBox)).isChecked();
                        hour00 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox00)).isChecked();
                        hour01 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox01)).isChecked();
                        hour02 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox02)).isChecked();
                        hour03 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox03)).isChecked();
                        hour04 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox04)).isChecked();
                        hour05 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox05)).isChecked();
                        hour06 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox06)).isChecked();
                        hour07 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox07)).isChecked();
                        hour08 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox08)).isChecked();
                        hour09 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox09)).isChecked();
                        hour10 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox10)).isChecked();
                        hour11 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox11)).isChecked();
                        hour12 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox12)).isChecked();
                        hour13 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox13)).isChecked();
                        hour14 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox14)).isChecked();
                        hour15 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox15)).isChecked();
                        hour16 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox16)).isChecked();
                        hour17 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox17)).isChecked();
                        hour18 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox18)).isChecked();
                        hour19 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox19)).isChecked();
                        hour20 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox20)).isChecked();
                        hour21 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox21)).isChecked();
                        hour22 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox22)).isChecked();
                        hour23 = ((CheckBox) setTimerView.findViewById(R.id.CheckBox23)).isChecked();

                        sharedPreferences.edit().putBoolean("monday", monday).apply();
                        sharedPreferences.edit().putBoolean("tuesday", tuesday).apply();
                        sharedPreferences.edit().putBoolean("wednesday", wednesday).apply();
                        sharedPreferences.edit().putBoolean("thursday", thursday).apply();
                        sharedPreferences.edit().putBoolean("friday", friday).apply();
                        sharedPreferences.edit().putBoolean("saturday", saturday).apply();
                        sharedPreferences.edit().putBoolean("sunday", sunday).apply();
                        sharedPreferences.edit().putBoolean("hour00", hour00).apply();
                        sharedPreferences.edit().putBoolean("hour01", hour01).apply();
                        sharedPreferences.edit().putBoolean("hour02", hour02).apply();
                        sharedPreferences.edit().putBoolean("hour03", hour03).apply();
                        sharedPreferences.edit().putBoolean("hour04", hour04).apply();
                        sharedPreferences.edit().putBoolean("hour05", hour05).apply();
                        sharedPreferences.edit().putBoolean("hour06", hour06).apply();
                        sharedPreferences.edit().putBoolean("hour07", hour07).apply();
                        sharedPreferences.edit().putBoolean("hour08", hour08).apply();
                        sharedPreferences.edit().putBoolean("hour09", hour09).apply();
                        sharedPreferences.edit().putBoolean("hour10", hour10).apply();
                        sharedPreferences.edit().putBoolean("hour11", hour11).apply();
                        sharedPreferences.edit().putBoolean("hour12", hour12).apply();
                        sharedPreferences.edit().putBoolean("hour13", hour13).apply();
                        sharedPreferences.edit().putBoolean("hour14", hour14).apply();
                        sharedPreferences.edit().putBoolean("hour15", hour15).apply();
                        sharedPreferences.edit().putBoolean("hour16", hour16).apply();
                        sharedPreferences.edit().putBoolean("hour17", hour17).apply();
                        sharedPreferences.edit().putBoolean("hour18", hour18).apply();
                        sharedPreferences.edit().putBoolean("hour19", hour19).apply();
                        sharedPreferences.edit().putBoolean("hour20", hour20).apply();
                        sharedPreferences.edit().putBoolean("hour21", hour21).apply();
                        sharedPreferences.edit().putBoolean("hour22", hour22).apply();
                        sharedPreferences.edit().putBoolean("hour23", hour23).apply();
                        Toast.makeText(getApplicationContext(), R.string.timerSaved, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    private void showAboutAlert() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.aboutTitle)
                .setMessage(R.string.aboutMessage)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }
                )
                .show();
    }
    private void showEditItemAlert(final int position) {
        LayoutInflater layoutInflater = getLayoutInflater();
        final View editItemView = layoutInflater.inflate(R.layout.edit_item, null);
        final Item currentItem = items.get(position);
        final EditText editQuestionEditText = (EditText) editItemView.findViewById(R.id.editQuestion);
        final EditText editAnswerEditText = (EditText) editItemView.findViewById(R.id.editAnswer);
        editQuestionEditText.setText(currentItem.question);
        editAnswerEditText.setText(currentItem.answer);

        AlertDialog editItemAlert = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_menu_edit)
                .setTitle(R.string.editItemTitle)
                .setView(editItemView)
                .setPositiveButton(R.string.editItemPositiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            editDatabase(
                                    new Item(
                                            currentItem.rowid,
                                            ((EditText) editItemView.findViewById(R.id.editQuestion)).getText().toString(),
                                            ((EditText) editItemView.findViewById(R.id.editAnswer)).getText().toString()
                                    )
                            );
                            Log.i("Info", String.format("Question size: %d", questions.size()));
                            updateItems();
                            Toast.makeText(getApplicationContext(), R.string.itemEdited, Toast.LENGTH_SHORT).show();
                        } catch (android.database.SQLException e) {
                            e.printStackTrace();
                            databaseError();
                            Log.e("Error", e.getMessage());
                            Toast.makeText(getApplicationContext(), R.string.itemEditFailed, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNeutralButton(R.string.deleteItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            deleteFromDatabase(currentItem.rowid);
                            Log.i("Info", String.format("Question size: %d", questions.size()));
                            updateItems();
                            Toast.makeText(getApplicationContext(), R.string.itemDeleted, Toast.LENGTH_SHORT).show();
                        } catch (android.database.SQLException e) {
                            e.printStackTrace();
                            databaseError();
                            Log.e("Error", e.getMessage());
                            Toast.makeText(getApplicationContext(), R.string.itemDeleteFailed, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    private void insertIntoDatabase(Item item) {
        database.execSQL(
                String.format(
                   Locale.US,
                   "INSERT INTO items (rowid, question, answer) VALUES (%d, %s, %s)",
                   item.rowid,
                   DatabaseUtils.sqlEscapeString(item.question),
                   DatabaseUtils.sqlEscapeString(item.answer)
                )
        );
    }
    private void editDatabase(Item item) {
        database.execSQL(
                String.format(
                        Locale.US,
                        "UPDATE items SET question = %s, answer = %s WHERE rowid = %d",
                        DatabaseUtils.sqlEscapeString(item.question),
                        DatabaseUtils.sqlEscapeString(item.answer),
                        item.rowid
                )
        );
    }
    private void deleteFromDatabase(final int rowid) {
        database.execSQL( String.format(Locale.US, "DELETE FROM items WHERE rowid = %d", rowid));
    }
    private void updateItems() {
        items = getItems();
        questions = getQuestions();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, questions);
        itemsListView.setAdapter(arrayAdapter);
        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), String.format("%s: %s", items.get(position).question, items.get(position).answer), Toast.LENGTH_LONG).show();
            }
        });
        itemsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showEditItemAlert(position);
                return true;
            }
        });
    }

    private int getNewRowid() {
        int max = 0;
        int itemRowid;
        for (int i = 0; i < items.size(); i++) {
            itemRowid = items.get(i).rowid;
            if (max < itemRowid) max = itemRowid;
        }
        return max + 1;
    }

    private void setDefaultSharedPreferences() {
        sharedPreferences = this.getSharedPreferences("info.duskovi.pavel.memorylocker", Context.MODE_PRIVATE);
        //set default values:
        monday = sharedPreferences.getBoolean("monday", false);
        tuesday = sharedPreferences.getBoolean("tuesday", false);
        wednesday = sharedPreferences.getBoolean("wednesday", false);
        thursday = sharedPreferences.getBoolean("thursday", false);
        friday = sharedPreferences.getBoolean("friday", false);
        saturday = sharedPreferences.getBoolean("saturday", false);
        sunday = sharedPreferences.getBoolean("sunday", false);
        hour00 = sharedPreferences.getBoolean("hour00", false);
        hour01 = sharedPreferences.getBoolean("hour01", false);
        hour02 = sharedPreferences.getBoolean("hour02", false);
        hour03 = sharedPreferences.getBoolean("hour03", false);
        hour04 = sharedPreferences.getBoolean("hour04", false);
        hour05 = sharedPreferences.getBoolean("hour05", false);
        hour06 = sharedPreferences.getBoolean("hour06", false);
        hour07 = sharedPreferences.getBoolean("hour07", false);
        hour08 = sharedPreferences.getBoolean("hour08", false);
        hour09 = sharedPreferences.getBoolean("hour09", false);
        hour10 = sharedPreferences.getBoolean("hour10", false);
        hour11 = sharedPreferences.getBoolean("hour11", false);
        hour12 = sharedPreferences.getBoolean("hour12", false);
        hour13 = sharedPreferences.getBoolean("hour13", false);
        hour14 = sharedPreferences.getBoolean("hour14", false);
        hour15 = sharedPreferences.getBoolean("hour15", false);
        hour16 = sharedPreferences.getBoolean("hour16", false);
        hour17 = sharedPreferences.getBoolean("hour17", false);
        hour18 = sharedPreferences.getBoolean("hour18", false);
        hour19 = sharedPreferences.getBoolean("hour19", false);
        hour20 = sharedPreferences.getBoolean("hour20", false);
        hour21 = sharedPreferences.getBoolean("hour21", false);
        hour22 = sharedPreferences.getBoolean("hour22", false);
        hour23 = sharedPreferences.getBoolean("hour23", false);

    }
    private ArrayList<Item> getItems() {
        ArrayList<Item> itemsArrayList = new ArrayList<Item>();
        Cursor c = database.rawQuery("SELECT rowid, * FROM items", null);
        int questionIndex = c.getColumnIndex("question");
        int answerIndex = c.getColumnIndex("answer");
        int rowidIndex = c.getColumnIndex("rowid");

        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            item = new Item(c.getInt(rowidIndex), c.getString(questionIndex), c.getString(answerIndex));
            itemsArrayList.add(item);
        }
        Log.i("Info", String.format("Items length: %d", itemsArrayList.size()) );
        c.close();
        return itemsArrayList;
    }
    private ArrayList<String> getQuestions() {
        ArrayList<String> questionsArrayList = new ArrayList<String>();
        Cursor c = database.rawQuery("SELECT rowid, * FROM items", null);
        int questionIndex = c.getColumnIndex("question");
        int answerIndex = c.getColumnIndex("answer");
        int rowidIndex = c.getColumnIndex("rowid");

        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            item = new Item(c.getInt(rowidIndex), c.getString(questionIndex), c.getString(answerIndex));
            questionsArrayList.add(item.question);
        }
        Log.i("Info", String.format("Questions length: %d", questionsArrayList.size()) );
        c.close();
        return questionsArrayList;
    }
    private void databaseStartValues() {
        //database.execSQL("INSERT INTO items (question, answer) VALUES ('milost (DE)', 'die Gnade')");
        //database.execSQL("INSERT INTO items (question, answer) VALUES ('blud (EN)', 'fallacy')");
        //database.execSQL("INSERT INTO items (question, answer) VALUES ('troufalost (EN)', 'audacity')");
        /*
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠁', 'A')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠃', 'B')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠉', 'C')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠙', 'D')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠑', 'E')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠋', 'F')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠛', 'G')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠓', 'H')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠊', 'I')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠚', 'J')");

        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠅', 'K')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠇', 'L')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠍', 'M')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠝', 'N')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠕', 'O')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠏', 'P')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠟', 'Q')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠗', 'R')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠎', 'S')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠞', 'T')");

        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠥', 'U')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠧', 'V')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠭', 'X')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠽', 'Y')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('⠵', 'Z')");
        */
        //database.execSQL("INSERT INTO items (question, answer) VALUES ('', '')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('Matouš 22:34-38', '34 Když se farizeové doslechli, že umlčel saduceje, smluvili se 35 a jeden jejich zákoník se ho otázal,\n" +
                "aby ho pokoušel: 36 \"Mistře, které přikázání v zákoně je největší?\" 37 On mu řekl: \"`Miluj Hospodina,\n" +
                "Boha svého, celým svým srdcem, celou svou duší a celou svou myslí. ́ 38 To je největší a první\n" +
                "přikázání.')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('2 Timoteovi 3:16-17', '16 Veškeré Písmo pochází z Božího Ducha a je dobré k učení, k usvědčování, k nápravě, k výchově ve\n" +
                "spravedlnosti, 17 aby Boží člověk byl náležitě připraven ke každému dobrému činu')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('Jozue 1:8', 'Kniha tohoto zákona ať se nevzdálí od tvých úst. Rozjímej nad ním ve dne v noci, abys mohl bedlivě plnit\n" +
                "vše, co je v něm zapsáno. Potom tě bude na tvé cestě provázet zdar, potom budeš jednat prozíravě.')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('Jan 16:24', 'Až dosud jste o nic neprosili v mém jménu. Proste a dostanete, aby vaše radost byla plná.')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('Filipským 4:6-7', '6 Netrapte se žádnou starostí, ale v každé modlitbě a prosbě děkujte a předkládejte své žádosti Bohu. 7 A\n" +
                "pokoj Boží, převyšující každé pomyšlení, bude střežit vaše srdce i mysl v Kristu Ježíši.')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('Jakub 1:22', 'Podle slova však také jednejte, nebuďte jen posluchači to byste klamali sami sebe! – to byste klamali sami sebe!')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('Židům 12:11', 'Přísná výchova se ovšem v tu chvíli nikdy nezdá příjemná, nýbrž krušná, později však přináší ovoce pokoje\n" +
                "a spravedlnost těm, kdo jí prošli.')");
    }
    private void databaseError() {
        if (database.isOpen()) database.close();
    }
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Locker Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }
    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }
    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}