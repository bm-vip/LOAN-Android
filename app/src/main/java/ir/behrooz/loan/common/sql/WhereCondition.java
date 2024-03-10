package ir.behrooz.loan.common.sql;

import org.greenrobot.greendao.Property;

public class WhereCondition {
    private Property property;
    private String value;
    private Operator oprator;
    private AndOr andOr;

    public WhereCondition(Property property, String value, Operator operator, AndOr andOr) {
        this.property = property;
        this.value = value;
        this.oprator = operator;
        this.andOr = andOr;
    }
    public WhereCondition(Property property, String value, Operator operator) {
        this.property = property;
        this.value = value;
        this.oprator = operator;
        this.andOr = null;
    }
    public WhereCondition(Property property, String value) {
        this.property = property;
        this.value = value;
        this.oprator = Operator.EQUAL;
        this.andOr = null;
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

    public Operator getOprator() {
        return oprator;
    }

    public void setOprator(Operator operator) {
        this.oprator = operator;
    }

    public AndOr getAndOr() {
        return andOr;
    }

    public void setAndOr(AndOr andOr) {
        this.andOr = andOr;
    }
}

