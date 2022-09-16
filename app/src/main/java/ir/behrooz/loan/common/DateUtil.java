package ir.behrooz.loan.common;

import com.mojtaba.materialdatetimepicker.utils.LanguageUtils;
import com.mojtaba.materialdatetimepicker.utils.PersianCalendar;

import java.util.Calendar;
import java.util.Date;

import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

import static ir.behrooz.loan.common.StringUtil.fixWeakCharacters;
import static ir.behrooz.loan.common.StringUtil.isNullOrEmpty;
import static ir.behrooz.loan.common.StringUtil.removeWeakCharacters;

/**
 * Created by Behrooz Mohamadi on 7/5/2018.
 */

public class DateUtil {

    public static String toPersianString(Date date, boolean latin) {
        if (date == null) return "";
        PersianDate pdate = new PersianDate(date);
        PersianDateFormat pdformater1 = new PersianDateFormat("Y/m/d");
        if (latin)
            return pdformater1.format(pdate);
        String persianNumbers = LanguageUtils.getPersianNumbers(pdformater1.format(pdate));
        return fixWeakCharacters(persianNumbers);
    }
    public static String toPersianString(Date date, String format) {
        if (date == null) return "";
        PersianDate pdate = new PersianDate(date);
        PersianDateFormat pdformater1 = new PersianDateFormat(format);
        return LanguageUtils.getPersianNumbers(pdformater1.format(pdate));
    }

    public static String toPersianWithTimeString(Date date) {
        PersianCalendar calendar = new PersianCalendar(date.getTime());
        String format = String.format("%d-%d-%d_%d-%d-%d", calendar.getPersianYear(), calendar.getPersianMonth(), calendar.getPersianDay(), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        return LanguageUtils.getLatinNumbers(format);
    }

    public static Date set(Date originDate, int dateType, int persianValue) {
        String[] persianDateArr = toPersianString(originDate, true).split("/");
        String year = persianDateArr[0];
        String month = persianDateArr[1];
        String day = persianDateArr[2];
        switch (dateType) {
            case Calendar.DAY_OF_MONTH:
                day = persianValue + "";
                break;
            case Calendar.MONTH:
                month = persianValue + "";
                break;
            case Calendar.YEAR:
                year = persianValue + "";
                break;
        }
        return toGregorian(addZero(year) + addZero(month) + addZero(day));
    }

    public static Date toGregorian(String date) {
        if (isNullOrEmpty(date) || date.equals("---"))
            return null;
        date = removeWeakCharacters(date);
        date = LanguageUtils.getLatinNumbers(date);
        int year = Integer.valueOf(date.substring(0, 4));
        int month = Integer.valueOf(date.substring(4, 6));
        int day = Integer.valueOf(date.substring(6, 8));
        PersianCalendar persianCalendar = new PersianCalendar();
        persianCalendar.setPersianDate(year, --month, day);
        return truncate(persianCalendar.getTime());
    }

    public static Date addMonth(Date date, int value) {
        String[] persianDateArr = toPersianString(date, true).split("/");
        Integer year = Integer.valueOf(persianDateArr[0]);
        Integer month = Integer.valueOf(persianDateArr[1]) + value;
        Integer day = Integer.valueOf(persianDateArr[2]);
        String persianDate = String.format("%d%s%s", year + month / 13, addZero(month % 13 + month / 13), addZero(day));
        return toGregorian(persianDate);
    }

    public static String addZero(int value) {
        if (value > 9)
            return value + "";
        return "0" + value;
    }

    public static String addZero(String value) {
        if (value.length() > 1)
            return value;
        return "0".concat(value);
    }

    public static Date truncate(Date date) {
        if (date == null)
            return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static void main(String[] args) {
        Date persianDate = toGregorian("1399/09/08");
        persianDate = addMonth(persianDate, 5);
        persianDate = set(persianDate, Calendar.DAY_OF_MONTH, 1);
        System.out.println(toPersianString(persianDate, true));
    }
}
