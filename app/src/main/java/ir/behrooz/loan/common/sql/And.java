package ir.behrooz.loan.common.sql;

import org.greenrobot.greendao.Property;

public class And extends WhereCondition {
    public And(Property property, String value, Operator operator) {
        super(property, value, operator, AndOr.AND);
    }
    public And(Property property, String value) {
        super(property, value, Operator.EQUAL, AndOr.AND);
    }
}
