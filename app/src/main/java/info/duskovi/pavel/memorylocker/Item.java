package info.duskovi.pavel.memorylocker;

/**
 * Created by pavel on 17.3.17.
 */

public class Item {
    /**
     * rowid is corresponding rowid to this Item in the SQLLite database
     */
    final int rowid;
    final String question;
    final String answer;
    final Category category;

    public Item(int rowid, String question, String answer, Category category) {
        this.rowid = rowid;
        this.question = question;
        this.answer = answer;
        this.category = category;
    }
}
