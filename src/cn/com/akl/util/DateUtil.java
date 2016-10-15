package cn.com.akl.util;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 时间格式输出的辅助类
 * 
 * @author zhangran
 * 
 */
public class DateUtil {

    /**
     * 将Date类型的时间转换为yyyy-MM-dd HH:mm:ss.SSS"为格式的字符串形式
     * @param date
     * @return
     */
    public static String dateToStr(Date date) {
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return ymdhmsFormat.format(date);
    }

    /**
     * 将Date类型的时间转换为yyyy-MM-dd HH:mm:ss"为格式的字符串形式
     * @param date
     * @return
     */
    public static String dateToStrBys(Date date){
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return ymdhmsFormat.format(date);
    }
    public static String dateToStrBy(Date date) {
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd");
        return ymdhmsFormat.format(date);
    }
    /**
     * 将以"yyyy-MM-dd HH:mm:ss"为格式的字符串转换为Date类型的时间
     * @param str
     * @return
     * @throws ParseException
     */
    public static Date strToDate(String str)throws ParseException{
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return ymdhmsFormat.parse(str);
    }

    
    /**
     * 将格式为yyyy-MM-dd的字符串转换为Date类型
     * @param dateStr 格式为yyyy-MM-dd的字符串
     * @return 对应字符串格式的Date类型
     * @author zhangran
     * @throws ParseException 字符串格式出
     */
    public static Date strToShortDate(String dateStr) throws ParseException {
        DateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd");
        return ymdFormat.parse(dateStr);
    }
    
    /**
     * 将Date类型的时间,格式为"yyyyMMddHHmmss"的字符串
     * @param str
     * @return
     * @throws ParseException
     */
    public static String dateToLongStrBys(Date date){
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return ymdhmsFormat.format(date);
    }
    
    
    /**
     * 将Date类型的时间,格式为"yyyyMMdd"的字符串
     * @param str
     * @return
     * @throws ParseException
     */
    public static String dateToLongStrBys2(Date date){
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyyMMdd");
        return ymdhmsFormat.format(date);
    }
    
    /**
     * 返回年份
     * @param date
     * @return
     */
    public static String dateToStrYear(Date date) {
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy");
        return ymdhmsFormat.format(date);
    }
    /**
     * 返回月份
     * @param date
     * @return
     */
    public static String dateToStrMonth(Date date) {
        DateFormat ymdhmsFormat = new SimpleDateFormat("MM");
        return ymdhmsFormat.format(date);
    }
    /**
     * 返回日子
     * @param date
     * @return
     */
    public static String dateToStrDay(Date date) {
        DateFormat ymdhmsFormat = new SimpleDateFormat("dd");
        return ymdhmsFormat.format(date);
    }
}
