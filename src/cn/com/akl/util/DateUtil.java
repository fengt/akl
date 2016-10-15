package cn.com.akl.util;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * ʱ���ʽ����ĸ�����
 * 
 * @author zhangran
 * 
 */
public class DateUtil {

    /**
     * ��Date���͵�ʱ��ת��Ϊyyyy-MM-dd HH:mm:ss.SSS"Ϊ��ʽ���ַ�����ʽ
     * @param date
     * @return
     */
    public static String dateToStr(Date date) {
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return ymdhmsFormat.format(date);
    }

    /**
     * ��Date���͵�ʱ��ת��Ϊyyyy-MM-dd HH:mm:ss"Ϊ��ʽ���ַ�����ʽ
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
     * ����"yyyy-MM-dd HH:mm:ss"Ϊ��ʽ���ַ���ת��ΪDate���͵�ʱ��
     * @param str
     * @return
     * @throws ParseException
     */
    public static Date strToDate(String str)throws ParseException{
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return ymdhmsFormat.parse(str);
    }

    
    /**
     * ����ʽΪyyyy-MM-dd���ַ���ת��ΪDate����
     * @param dateStr ��ʽΪyyyy-MM-dd���ַ���
     * @return ��Ӧ�ַ�����ʽ��Date����
     * @author zhangran
     * @throws ParseException �ַ�����ʽ��
     */
    public static Date strToShortDate(String dateStr) throws ParseException {
        DateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd");
        return ymdFormat.parse(dateStr);
    }
    
    /**
     * ��Date���͵�ʱ��,��ʽΪ"yyyyMMddHHmmss"���ַ���
     * @param str
     * @return
     * @throws ParseException
     */
    public static String dateToLongStrBys(Date date){
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return ymdhmsFormat.format(date);
    }
    
    
    /**
     * ��Date���͵�ʱ��,��ʽΪ"yyyyMMdd"���ַ���
     * @param str
     * @return
     * @throws ParseException
     */
    public static String dateToLongStrBys2(Date date){
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyyMMdd");
        return ymdhmsFormat.format(date);
    }
    
    /**
     * �������
     * @param date
     * @return
     */
    public static String dateToStrYear(Date date) {
        DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy");
        return ymdhmsFormat.format(date);
    }
    /**
     * �����·�
     * @param date
     * @return
     */
    public static String dateToStrMonth(Date date) {
        DateFormat ymdhmsFormat = new SimpleDateFormat("MM");
        return ymdhmsFormat.format(date);
    }
    /**
     * ��������
     * @param date
     * @return
     */
    public static String dateToStrDay(Date date) {
        DateFormat ymdhmsFormat = new SimpleDateFormat("dd");
        return ymdhmsFormat.format(date);
    }
}
