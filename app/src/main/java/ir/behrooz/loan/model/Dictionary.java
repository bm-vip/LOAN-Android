package ir.behrooz.loan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by b.mohammadi on 3/4/2018.
 */
public class Dictionary {
    private String id;
    private String text;

    public Dictionary(String id, String text) {
        this.id = id;
        this.text = text;
    }
    public Dictionary() {
        this.id = "0";
        this.text = "---";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static List<String> getValues(List<Dictionary> list) {
        List<String> result = new ArrayList<>();
        for (Dictionary dictionary : list) {
            result.add(dictionary.getText());
        }
        return result;
    }
}
