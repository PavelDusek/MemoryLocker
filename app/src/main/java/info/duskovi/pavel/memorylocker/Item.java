package info.duskovi.pavel.memorylocker;

/**
 * Created by pavel on 17.3.17.
 */

public class Item {
    /**
     * rowid is corresponding rowid to this Item in the SQLLite database
     */
    public final int rowid;
    public final String question;
    public final String answer;

    public Item(int rowid, String question, String answer) {
        this.rowid = rowid;
        this.question = question;
        this.answer = answer;
    }
}
