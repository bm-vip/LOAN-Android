package ir.behrooz.loan.common.sql;

public enum Operator {
    EQUAL("="),
    IS("IS"),
    IS_NOT("IS NOT"),
    GREATER(">"),
    GREATER_THAN(">="),
    LESS("<"),
    LESS_THAN("<=")
    ;

    Operator(String value) {
        this.value = value;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
