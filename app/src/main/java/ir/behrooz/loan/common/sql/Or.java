package ir.behrooz.loan.common.sql;

import org.greenrobot.greendao.Property;

public class Or extends WhereCondition {
    public Or(Property property, String value, Operator operator) {
        super(property, value, operator, AndOr.OR);
    }
    public Or(Property property, String value) {
        super(property, value, Operator.EQUAL, AndOr.OR);
    }
}
