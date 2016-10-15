package cn.com.akl.shgl.sx.biz;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 截保日期计算
 * @author fengtao
 *
 */
public class DateBiz {
	
	private final static int begin = 2000;//开始日期
	private final static int year = 12;//年12月
	private final static int month = 30;//月30天
	private final static int week = 7;//周7天
	int transport = 9;//物流期9个月
	
	/**
	 * 截保日期
	 * @param warranty
	 * @param Yweeks
	 * @return
	 */
	public String getDeadline(int warranty, int[] Yweeks){
		int y = 0;
		int dd = 1;
		int Y = Yweeks[0];
		int weeks = Yweeks[1];
		transport = setTransport(Yweeks);
		
		double dds = (double)(weeks * week)/month + transport;
		double tmp = decimalFormat(dds);
		while(tmp > year){
			tmp -= year;
			y++;
		}
		int m = (int)tmp;
		double n = (double)(tmp - m);
		if(n > 0) m++;
		
		int yyyy = begin + warranty + Y + y;//年
		int MM = m;//月
		if(n > 0.0)
			dd = (int)Math.round(n*month);//日
		String deadline = yyyy + "-" + MM + "-" + dd;//截保时间
		
		return deadline;
	}
	
	/**
	 * 生产日期
	 * @param Yweeks
	 * @return
	 */
	public String getManufacture(int[] Yweeks){
		int Y = Yweeks[0];
		int weeks = Yweeks[1];
		double dds = (double)(weeks * week)/month;
		double tmp = decimalFormat(dds);
		
		int m = (int)tmp;
		double n = (double)(tmp - m);
		if(n > 0) m++;
		
		int yyyy = begin + Y;//年
		int MM = m;//月
		int dd = (int)Math.round(n*month);//日
		String manufacture = yyyy + "-" + MM + "-" + dd;//生产日期
		
		return manufacture;
	}
	
	/**
	 * 物流期
	 * @param Yweeks
	 * @return
	 */
	public int setTransport(int[] Yweeks){
		String manu = getManufacture(Yweeks);
		Date now = new Date();
		String df = new SimpleDateFormat("yyyy-MM-dd").format(now);
		int m = getMonths(df,manu);
		if(m > 0 && m < transport) transport = m;//(送修日 - 生产日) < 物流期，以差值为物流期
		return transport;
	}
	
	/**
	 * 根据SN号转换为：年和周数
	 * @param sn
	 * @return
	 */
	public int[] convertStr(String sn){
		int Y = 0;
		int weeks = 0;
		boolean flag = true;
		int[] Yweeks = new int[2];
		String[] regex = {"[A-Z]{2}\\d{3}[A-Z]{2}.*", "\\d{4}[A-Z]{2}.*", "[F-N]{1}\\d{2}.*"};
		for (int i = 0; flag && i < regex.length; i++) {
			Pattern p = Pattern.compile(regex[i]);
			Matcher mt = p.matcher(sn);
			if(mt.matches()){
				switch(i){
				case 0:
					Y = Integer.parseInt(sn.substring(2, 3));
					weeks = Integer.parseInt(sn.substring(3, 5));
					flag = false;
					break;
				case 1:
					Y = Integer.parseInt(sn.substring(0, 2));
					weeks = Integer.parseInt(sn.substring(2, 4));
					flag = false;
					break;
				case 2:
					char word = sn.substring(0, 1).charAt(0);
					Y = getY(word);
					weeks = Integer.parseInt(sn.substring(1, 3));
					flag = false;
					break;
				}
			}
			Yweeks[0] = Y; 
			Yweeks[1] = weeks; 
		}
		if(Yweeks[1] == 0 || Yweeks[0] == 0) throw new RuntimeException("您录入的SN["+sn+"]不在规则内，请核实属实后再继续办理！");
		return Yweeks;
	}
	
	/**
	 * UE系列：字母转换年
	 * @param word
	 * @return
	 */
	public int getY(char word){
		int Y = 0;
		switch(word){
		case 'F': Y = 11; break;
		case 'G': Y = 12; break;
		case 'H': Y = 13; break;
		case 'I': Y = 14; break;
		case 'J': Y = 15; break;
		case 'K': Y = 16; break;
		case 'L': Y = 17; break;
		case 'M': Y = 18; break;
		case 'N': Y = 19; break;
		default: Y =0; break;
		}
		return Y;
	}
	
	 /**
     * 计算两字符日期差（月）
     * @param dd
     * @param ss
     * @return
     */
    public static int getMonths(String dd, String ss){
    	long months = 0;
    	SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
    	try {
			Date begin = ft.parse(dd);
			Date end = ft.parse(ss);
			months = begin.getTime() - end.getTime();
			months = months/1000/60/60/24/30;
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return (int)months;
    }
    
    /**
     * 小数点格式为两位
     * @param dd
     * @return
     */
    public static double decimalFormat(double dd){
    	DecimalFormat df = new DecimalFormat(".##");//格式化
    	double tmp = Double.parseDouble(df.format(dd));
    	return tmp;
    }
}
