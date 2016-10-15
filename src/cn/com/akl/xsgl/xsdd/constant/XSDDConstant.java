package cn.com.akl.xsgl.xsdd.constant;

import java.math.BigDecimal;

public class XSDDConstant {

	public static final int EXCEL_COL_DDID = 0;
	public static final int EXCEL_COL_DH = 1;
	public static final int EXCEL_COL_WLBH = 2;
	public static final int EXCEL_COL_WLMC = 3;
	public static final int EXCEL_COL_WLGG = 4;
	public static final int EXCEL_COL_XH = 5;
	public static final int EXCEL_COL_ZL = 6;
	public static final int EXCEL_COL_TJ = 7;
	public static final int EXCEL_COL_KHSPBH = 8;
	public static final int EXCEL_COL_XSDJ = 9;
	public static final int EXCEL_COL_XSZDJ = 10;
	public static final int EXCEL_COL_WOS = 11;
	public static final int EXCEL_COL_KYSL = 12;
	public static final int EXCEL_COL_ZTSL = 13;
	public static final int EXCEL_COL_KC = 14;
	public static final int EXCEL_COL_XDSL = 15;
	public static final int EXCEL_COL_DFSL = 16;
	public static final int EXCEL_COL_MZL = 17;
	public static final int EXCEL_COL_SDZT = 18;
	public static final int EXCEL_COL_POSFALX = 19;
	public static final int EXCEL_COL_POSID = 20;
	public static final int EXCEL_COL_POSMC = 21;
	public static final int EXCEL_COL_POSZCDJ = 22;
	public static final int EXCEL_COL_POSJE = 23;
	public static final int EXCEL_COL_POSZCSL = 24;
	public static final int EXCEL_COL_FLFS = 25;
	public static final int EXCEL_COL_FLFAH = 26;
	public static final int EXCEL_COL_FLFAMC = 27;
	public static final int EXCEL_COL_FLZCD = 28;
	public static final int EXCEL_COL_FLZCJ = 29;
	public static final int EXCEL_COL_FLSL = 30;
	public static final int EXCEL_COL_JLDW = 31;
	public static final int EXCEL_COL_BZ = 32;
	
	public static final String YES = "025000";
	public static final String NO = "025001";

	public static final String ZT_WJS = "032000";
	public static final String ZT_JS = "032001";
	
	public static final String POS_FALX_ZJC = "035000"; 	//资金池
	public static final String POS_FALX_FA = "035001";	//正常方案

	public static final String FL_FALX_JGZC="1"; //价格支持
	
	public static final String FL_FLFS_XFL = "0";	// 先返利
	public static final String FL_FLFS_HFL = "1";	// 后返利
	public static final String FL_FLFS_BFL = "2";	// 不返利
	
	/**
	 * 已发货.
	 */
	public static final String XSDD_DDZT_YFH = "4";
	/**
	 *  已签收.
	 */
	public static final String XSDD_DDZT_YQS = "5";
	/**
	 *  签收确认.
	 */
	public static final String XSDD_DDZT_QRQS = "6";
	/**
	 *  已付款.
	 */
	public static final String XSDD_DDZT_YFK = "7";
	
	// 税率
	public static final float SL = 0.17f;
	
	// 精度
	public static final int FLOAT_SCALE = 4;
	// 小数进位方式
	public static final int ROUND_MODE = BigDecimal.ROUND_HALF_UP;
	
	/**
	 * 账期计算方式.
	 */
	public static final String ZQJSFS_QSRQ = "053133";
	public static final String ZQJSFS_FPRQ = "053134";
	
}
