package ir.behrooz.loan.common.sql;

import org.greenrobot.greendao.Property;

public class WhereCondition {
    private Property property;
    private String value;
    private Oprator oprator;
    private String andOr;

    public WhereCondition(Property property, String value, Oprator oprator) {
        this.property = property;
        this.value = value;
        this.oprator = oprator;
        this.andOr = "";
    }

    public WhereCondition(Property property, String value, Oprator oprator, String andOr) {
        this.property = property;
        this.value = value;
        this.oprator = oprator;
        this.andOr = andOr;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Oprator getOprator() {
        return oprator;
    }

    public void setOprator(Oprator oprator) {
        this.oprator = oprator;
    }

    public String getAndOr() {
        return andOr;
    }

    public void setAndOr(String andOr) {
        this.andOr = andOr;
    }
}
