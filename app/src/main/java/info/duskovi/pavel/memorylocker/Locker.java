package info.duskovi.pavel.memorylocker;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class Locker extends AppCompatActivity {
    ArrayList<String> questions;
    ArrayList<Item> items;
    ListView itemsListView;
    Item item;
    DatabaseAPI database;
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
                Log.i("Menu", "addItemAlert");
                showAddItemAlert();
                return true;
            case R.id.import_items:
                Log.i("Menu", "importItems");
                Toast.makeText(getApplicationContext(), R.string.todo, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.export_items:
                Log.i("Menu", "exportItems");
                showExportItemsAlert();
                return true;
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
        items = new ArrayList<>();
        questions = new ArrayList<>();
        itemsListView = (ListView) findViewById(R.id.itemsListView);
        database = new DatabaseAPI(this);
        updateItems();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
                        database.saveItem(
                                new Item(
                                        getNewRowid(),
                                        ((EditText) addItemView.findViewById(R.id.newQuestion)).getText().toString(),
                                        ((EditText) addItemView.findViewById(R.id.newAnswer)).getText().toString(),
                                        new Category(0, "general")
                                )
                        );
                        Log.i("Info", String.format("Question size: %d", questions.size()));
                        updateItems();
                        Toast.makeText(getApplicationContext(), R.string.itemAdded, Toast.LENGTH_SHORT).show();
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
                        database.editItem(
                                new Item(
                                        currentItem.rowid,
                                        ((EditText) editItemView.findViewById(R.id.editQuestion)).getText().toString(),
                                        ((EditText) editItemView.findViewById(R.id.editAnswer)).getText().toString(),
                                        new Category(0, "general")
                                )
                        );
                        Log.i("Info", String.format("Question size: %d", questions.size()));
                        updateItems();
                        Toast.makeText(getApplicationContext(), R.string.itemEdited, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton(R.string.deleteItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        database.deleteItem(currentItem.rowid);
                        Log.i("Info", String.format("Question size: %d", questions.size()));
                        updateItems();
                        Toast.makeText(getApplicationContext(), R.string.itemDeleted, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showImportItemsAlert() {
        LayoutInflater layoutInflater = getLayoutInflater();
        final View importItemsView = layoutInflater.inflate(R.layout.import_items, null);

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_input_add)
                .setTitle(R.string.importItemsTitle)
                .setView(importItemsView)
                .setPositiveButton(R.string.importItemsPositiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.importItemsNegativeButton, null)
                .show();

    }

    private void importItems(String csv) {
        String[] lines = csv.split("\\r?\\n");

    }
    private void showExportItemsAlert() {
        LayoutInflater layoutInflater = getLayoutInflater();
        final View exportItemsView = layoutInflater.inflate(R.layout.export_items, null);
        final EditText exportItemsEditText = (EditText) exportItemsView.findViewById(R.id.exportItemsEditText);
        exportItemsEditText.setText(exportItems());

        AlertDialog exportItemAlert = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_menu_directions)
                .setTitle(R.string.exportItemsViewTitle)
                .setView(exportItemsView)
                .setPositiveButton(R.string.exportItemsPositiveButton, null)
                .show();
    }

    private String exportItems() {
        ArrayList<Item> items = database.getItems();
        String csv = "question,answer\n";

        for (Item item: items) {
            csv += String.format(Locale.US, "\"%s\",\"%s\"\n", item.question, item.answer);
        }
        return csv;
    }

    private void updateItems() {
        items = database.getItems();
        questions = getQuestions(items);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, questions);
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

    private ArrayList<String> getQuestions(ArrayList<Item> items) {
        ArrayList<String> questions = new ArrayList<>();
        for (Item item: items) {
            questions.add(item.question);
        }
        return questions;
    }

    private int getNewRowid() {
        /**
         * Finds a new unique id for a new entry.
         */
        int max = 0;
        int itemRowid;
        for (Item item: items) {
            itemRowid = item.rowid;
            if (max < itemRowid) max = itemRowid;
        }
        return max + 1;
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