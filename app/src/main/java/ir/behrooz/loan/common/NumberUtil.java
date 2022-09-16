package ir.behrooz.loan.common;

import static ir.behrooz.loan.common.StringUtil.isNullOrEmpty;

public class NumberUtil {

    public static boolean isNullOrZero(Integer i) {
        return i == null || i.equals(0);
    }

    public static boolean isNullOrZero(Long l) {
        return l == null || l.equals(0L);
    }

    public static Long getLong(String value) {
        if (isNullOrEmpty(value))
            return 0L;
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static Long round(long number, long multiple) {
        long result = multiple;
        if (number % multiple == 0) {
            return number;
        }
        // If not already multiple of given number
        if (number % multiple != 0) {
            long division = number / multiple;
            result = division * multiple;
        }
        return result;
    }
}
