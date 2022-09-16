package ir.behrooz.loan.model;

import org.greenrobot.greendao.Property;

import java.util.List;

public class SortModel {
    private Sort sort;
    private String title;
    private boolean checked;
    private String value;
    private Property property;

    public enum Sort{
        ASC, DESC
    }

    public SortModel(String title, Property property) {
        this.title = title;
        this.property = property;
        this.sort = Sort.ASC;
    }

    public SortModel(String title, String value) {
        this.title = title;
        this.value = value;
        this.sort = Sort.ASC;
    }

    public SortModel(String title, String value, Sort sort) {
        this.title = title;
        this.value = value;
        this.sort = sort;
    }

    public static String getIds(List<SortModel> list){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            SortModel item = list.get(i);
            builder.append(item.getValue());
            if (i < list.size() - 1)
                builder.append(",");
        }
        return builder.toString();
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }
}
