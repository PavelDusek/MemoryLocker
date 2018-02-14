package info.duskovi.pavel.memorylocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Random;

public class Locker extends AppCompatActivity {
    ArrayList<String> questions;
    ArrayList<Item> items;
    ListView itemsListView;
    Item item;
    Random random;
    TextView question;
    SQLiteDatabase database;
    NotificationCompat.Builder notification;
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
                LayoutInflater layoutInflater = getLayoutInflater();
                final View addItemView = layoutInflater.inflate(R.layout.add_item, null);
                AlertDialog alert = new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_input_add)
                        .setTitle(R.string.addItemTitle)
                        .setView(addItemView)
                        .setPositiveButton(R.string.addItemPositiveButton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    insertIntoDatabase(new Item(((EditText) addItemView.findViewById(R.id.newQuestion)).getText().toString(), ((EditText) addItemView.findViewById(R.id.newAnswer)).getText().toString()));
                                    items = getItems();
                                    questions = getQuestions();
                                    Log.i("Info", String.format("Question size: %d", questions.size()));
                                    updateItemsListView();
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
                return true;
            case R.id.import_items:
                Toast.makeText(getApplicationContext(), R.string.todo, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.export_items:
                Toast.makeText(getApplicationContext(), R.string.todo, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.set_timer:
                Toast.makeText(getApplicationContext(), R.string.todo, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.about:
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
        random = new Random();

        try {
            database = this.openOrCreateDatabase("MemoryLockerItems", MODE_PRIVATE, null);
            //database.execSQL("DROP TABLE items");
            database.execSQL("CREATE TABLE IF NOT EXISTS items (question VARCHAR, answer VARCHAR)");
            //databaseStartValues();
            items = getItems();
            questions = getQuestions();
        } catch (android.database.SQLException e) {
            databaseError();
            e.printStackTrace();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        updateItemsListView();
    }
    private void insertIntoDatabase(Item item) {
        database.execSQL(
                String.format(
                   "INSERT INTO items (question, answer) VALUES (%s, %s)",
                   DatabaseUtils.sqlEscapeString(item.question),
                   DatabaseUtils.sqlEscapeString(item.answer)
                )
        );
    }
    private void updateItemsListView() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, questions);
        itemsListView.setAdapter(arrayAdapter);
        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), String.format("%s: %s", items.get(position).question, items.get(position).answer), Toast.LENGTH_LONG).show();
            }
        });
    }
    private ArrayList<Item> getItems() {
        ArrayList<Item> itemsArrayList = new ArrayList<Item>();
        Cursor c = database.rawQuery("SELECT * FROM items", null);
        int questionIndex = c.getColumnIndex("question");
        int answerIndex = c.getColumnIndex("answer");

        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            item = new Item(c.getString(questionIndex), c.getString(answerIndex));
            itemsArrayList.add(item);
        }
        Log.i("Info", String.format("Items length: %d", itemsArrayList.size()) );
        return itemsArrayList;
    }
    private ArrayList<String> getQuestions() {
        ArrayList<String> questionsArrayList = new ArrayList<String>();
        Cursor c = database.rawQuery("SELECT * FROM items", null);
        int questionIndex = c.getColumnIndex("question");
        int answerIndex = c.getColumnIndex("answer");

        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            item = new Item(c.getString(questionIndex), c.getString(answerIndex));
            questionsArrayList.add(item.question);
        }
        Log.i("Info", String.format("Questions length: %d", questionsArrayList.size()) );
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
        database.execSQL("INSERT INTO items (question, answer) VALUES ('Jošua 1:8', 'Kniha tohoto zákona ať se nevzdálí od tvých úst. Rozjímej nad ním ve dne v noci, abys mohl bedlivě plnit\n" +
                "vše, co je v něm zapsáno. Potom tě bude na tvé cestě provázet zdar, potom budeš jednat prozíravě.')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('Jan 16:24', 'Až dosud jste o nic neprosili v mém jménu. Proste a dostanete, aby vaše radost byla plná.')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('Filipským 4:6-7', '6 Netrapte se žádnou starostí, ale v každé modlitbě a prosbě děkujte a předkládejte své žádosti Bohu. 7 A\n" +
                "pokoj Boží, převyšující každé pomyšlení, bude střežit vaše srdce i mysl v Kristu Ježíši.')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('Jakub 1:22', 'Podle slova však také jednejte, nebuďte jen posluchači to byste klamali sami sebe! – to byste klamali sami sebe!')");
        database.execSQL("INSERT INTO items (question, answer) VALUES ('Židům 12:11', 'Přísná výchova se ovšem v tu chvíli nikdy nezdá příjemná, nýbrž krušná, později však přináší ovoce pokoje\n" +
                "a spravedlnost těm, kdo jí prošli.')");
    }
    public void validate(View view) {
        Toast.makeText(
                getApplicationContext(),
                String.format("Question: %s\nAnswer: %s", item.question, item.answer),
                Toast.LENGTH_LONG).show();
        newItem();
    }
    private void databaseError() {
        if (database.isOpen()) database.close();
    }
    public void newItem() {
        if (items.size() > 0) {
            int counter = random.nextInt(items.size());
            item = items.get(counter);
            question.setText(item.question);
        }
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