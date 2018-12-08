package info.duskovi.pavel.memorylocker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by pavel on 8.12.18.
 */

class DatabaseAPI {
    private static final String ITEMS = "items";
    private static final String QUESTION = "question";
    private static final String ANSWER = "answer";
    private static final String CATEGORIES = "categories";
    private static final String CATEGORY = "category";
    private static final String CATEGORY_NAME = "category_name";

    SQLiteDatabase database;
    AppCompatActivity parent;

    DatabaseAPI(AppCompatActivity parent) {
        /**
         * Creates/Connects SQL lite database
         */
        this.parent = parent;
        try {
            database = parent.openOrCreateDatabase("MemoryLockerItems", parent.MODE_PRIVATE, null);
            //database.execSQL("DROP TABLE items");
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + ITEMS + " " +
                            "(" +
                                QUESTION + " VARCHAR, " +
                                ANSWER + " VARCHAR, " +
                                CATEGORY + " INTEGER" +
                            ")"
            );
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + CATEGORIES + " " +
                            "(" +
                                CATEGORY + " INTEGER PRIMERY KEY NOT NULL, " +
                                CATEGORY_NAME + " TEXT UNIQUE" +
                            ")"
            );
            //databaseStartValues();
        } catch (android.database.SQLException e) {
            databaseError(e.getMessage());
        }
    }

    void saveItem(Item item) {
        /**
         * Stores a answer+question item pair in the database
         */
        ContentValues cv = new ContentValues();
        cv.put(QUESTION, item.question);
        cv.put(ANSWER, item.answer);
        cv.put(CATEGORY, item.category.category_id);
        insert(ITEMS, ANSWER, cv);
    }

    void saveCategory(Category category) {
        /**
         * Creates a new category in the database
         */
        ContentValues cv = new ContentValues();
        cv.put(CATEGORY, category.category_id);
        cv.put(CATEGORY_NAME, category.category_name);
        insert(CATEGORIES, CATEGORY, cv);
    }

    private void insert(String tableName, String nullColumnHack, ContentValues values) {
        /**
         * Calls the SQLiteDatabase insert function
         */
        database.insert(tableName, nullColumnHack, values);
        Toast.makeText(parent, "Values saved.", Toast.LENGTH_SHORT).show();
    }

    void createCategory(String category_name) {
        ContentValues cv = new ContentValues();
        cv.put(CATEGORY_NAME, category_name);
        insert(CATEGORIES, CATEGORY_NAME, cv);
    }

    ArrayList<Item> getItems() {
        ArrayList<Item> items = new ArrayList<>();
        String sql = "SELECT " +
                "i.rowid, " +
                "i." + QUESTION + ", " +
                "i." + ANSWER + ", " +
                "c." + CATEGORY + ", " +
                "c." + CATEGORY_NAME +
                " FROM " + ITEMS + " AS i INNER JOIN " + CATEGORIES + " AS c " +
                "ON i." + CATEGORY + " = c." + CATEGORY;
        Log.i("pavellog", sql);
        Cursor c = database.rawQuery(sql, null);
        int rowidIndex = c.getColumnIndex("rowid");
        int questionIndex = c.getColumnIndex(QUESTION);
        int answerIndex = c.getColumnIndex(ANSWER);
        int categoryIdIndex = c.getColumnIndex(CATEGORY);
        int categoryNameIndex = c.getColumnIndex(CATEGORY_NAME);

        Item item;
        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            item = new Item(
                    c.getInt(rowidIndex),
                    c.getString(questionIndex),
                    c.getString(answerIndex),
                    new Category(c.getInt(categoryIdIndex), c.getString(categoryNameIndex))
            );
            items.add(item);
        }
        c.close();
        return items;
    }

    ArrayList<Item> getItemsForCategory(String category_name) {
        ArrayList<Item> items = new ArrayList<>();
        String sql = "SELECT " +
                "i.rowid, " +
                "i." + QUESTION + ", " +
                "i." + ANSWER + ", " +
                "c." + CATEGORY + ", " +
                "c." + CATEGORY_NAME +
                " FROM " + ITEMS + " AS i INNER JOIN " + CATEGORIES + " AS c " +
                "ON i." + CATEGORY + " = c." + CATEGORY + " " +
                "WHERE c." + CATEGORY_NAME + " = " + DatabaseUtils.sqlEscapeString(category_name);
        Log.i("pavellog", sql);
        Cursor c = database.rawQuery(sql, null);
        int rowidIndex = c.getColumnIndex("rowid");
        int questionIndex = c.getColumnIndex(QUESTION);
        int answerIndex = c.getColumnIndex(QUESTION);
        int categoryIdIndex = c.getColumnIndex(CATEGORY);
        int categoryNameIndex = c.getColumnIndex(CATEGORY_NAME);

        Item item;
        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            item = new Item(
                    c.getInt(rowidIndex),
                    c.getString(questionIndex),
                    c.getString(answerIndex),
                    new Category(c.getInt(categoryIdIndex), c.getString(categoryNameIndex))
            );
            items.add(item);
        }
        c.close();
        return items;
    }

    ArrayList<Category> getAllCategories() {
        String sql = "SELECT " +
                CATEGORY + ", " +
                CATEGORY_NAME +
                " FROM " + CATEGORIES;
        Log.i("pavellog", sql);
        Cursor c = database.rawQuery(sql, null);
        int categoryIdIndex = c.getColumnIndex(CATEGORY);
        int categoryNameIndex = c.getColumnIndex(CATEGORY_NAME);

        ArrayList<Category> categories = new ArrayList<>();
        Category category;
        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            category = new Category(c.getInt(categoryIdIndex), c.getString(categoryNameIndex));
            categories.add(category);
        }
        c.close();
        return categories;
    }

    void editItem(Item item) {
        /**
         * Updates an item entry in the database.
         */
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

    void deleteItem(final int rowid) {
        /**
         * Deletes a database entry from database.
         */
        database.execSQL( String.format(Locale.US, "DELETE FROM items WHERE rowid = %d", rowid));
    }

    void close() {
        database.close();
    }

    private void databaseError(String errorMessage) {
        Log.e("dberror", errorMessage);
        if (database.isOpen()) database.close();
    }

    void databaseStartValues() {
        Category general = new Category(0, "general");
        int i = 0;

        saveCategory(general);
        saveItem(new Item(i++, "milost (DE)", "die Gnade", general));
        saveItem(new Item(i++, "blud (EN)", "fallacy", general));
        saveItem(new Item(i++, "troufalost (EN)", "audacity", general));
        saveItem(new Item(i++, "⠁", "A", general));
        saveItem(new Item(i++, "⠃", "B", general));
        saveItem(new Item(i++, "⠉", "C", general));
        saveItem(new Item(i++, "⠙", "D", general));
        saveItem(new Item(i++, "⠑", "E", general));
        saveItem(new Item(i++, "⠋", "F", general));
        saveItem(new Item(i++, "⠛", "G", general));
        saveItem(new Item(i++, "⠓", "H", general));
        saveItem(new Item(i++, "⠊", "I", general));
        saveItem(new Item(i++, "⠚", "J", general));
        saveItem(new Item(i++, "⠅", "K", general));
        saveItem(new Item(i++, "⠇", "L", general));
        saveItem(new Item(i++, "⠍", "M", general));
        saveItem(new Item(i++, "⠝", "N", general));
        saveItem(new Item(i++, "⠕", "O", general));
        saveItem(new Item(i++, "⠏", "P", general));
        saveItem(new Item(i++, "⠟", "Q", general));
        saveItem(new Item(i++, "⠗", "R", general));
        saveItem(new Item(i++, "⠎", "S", general));
        saveItem(new Item(i++, "⠞", "T", general));
        saveItem(new Item(i++, "⠥", "U", general));
        saveItem(new Item(i++, "⠧", "V", general));
        saveItem(new Item(i++, "⠭", "X", general));
        saveItem(new Item(i++, "⠽", "Y", general));
        saveItem(new Item(i++, "⠵", "Z", general));
        saveItem(new Item(i++, "Matouš 22:34-38", "34 Když se farizeové doslechli, že umlčel saduceje, smluvili se 35 a jeden jejich zákoník se ho otázal, aby ho pokoušel: 36 \"Mistře, které přikázání v zákoně je největší?\" 37 On mu řekl: \"Miluj Hospodina, Boha svého, celým svým srdcem, celou svou duší a celou svou myslí. ́38 To je největší a první přikázání.\"", general));
        saveItem(new Item(i++, "2 Timoteovi 3:16-17", "16 Veškeré Písmo pochází z Božího Ducha a je dobré k učení, k usvědčování, k nápravě, k výchově ve spravedlnosti, 17 aby Boží člověk byl náležitě připraven ke každému dobrému činu", general));
        saveItem(new Item(i++, "Jozue 1:8", "Kniha tohoto zákona ať se nevzdálí od tvých úst. Rozjímej nad ním ve dne v noci, abys mohl bedlivě plnit vše, co je v něm zapsáno. Potom tě bude na tvé cestě provázet zdar, potom budeš jednat prozíravě.", general));
        saveItem(new Item(i++, "Jan 16:24", "Až dosud jste o nic neprosili v mém jménu. Proste a dostanete, aby vaše radost byla plná.", general));
        saveItem(new Item(i++, "Filipským 4:6-7", "6 Netrapte se žádnou starostí, ale v každé modlitbě a prosbě děkujte a předkládejte své žádosti Bohu. 7 A pokoj Boží, převyšující každé pomyšlení, bude střežit vaše srdce i mysl v Kristu Ježíši.", general));
        saveItem(new Item(i++, "Jakub 1:22", "Podle slova však také jednejte, nebuďte jen posluchači – to byste klamali sami sebe!", general));
        saveItem(new Item(i, "Židům 12:11", "Přísná výchova se ovšem v tu chvíli nikdy nezdá příjemná, nýbrž krušná, později však přináší ovoce pokoje a spravedlnost těm, kdo jí prošli.", general));
    }
}
