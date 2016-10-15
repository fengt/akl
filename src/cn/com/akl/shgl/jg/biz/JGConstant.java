package cn.com.akl.shgl.jg.biz;

public class JGConstant {

	/** 加工方式： 正常加工. */
	public static final String JGLX_ZCJG = "正常加工";
	/** 加工方式： 非正常加工. */
	public static final String JGLX_FZCJG = "非正常加工";
	/** 送修处理方式. */
	public static final String clfs0 = "064286";//换新
	public static final String clfs1 = "064289";//待收货
	/** 是. */
	public static final String is = "025000";

	/** 消耗品处理操作: 加、减消耗库存. */
	public static final String add = "+";
	public static final String subtract = "-";
	
	/** 加工项目类别. */
	public static final String QUERY_XMLB = "SELECT XMLB FROM BO_AKL_SH_JG_P WHERE BINDID=?";
	/** 送修单号. */
	public static final String QUERY_SXDH = "SELECT SXDH FROM BO_AKL_SH_JG_P WHERE BINDID=?";
	/** 加工类型. */
	public static final String QUERY_JGLX = "SELECT JGLX FROM BO_AKL_SH_JG_P WHERE BINDID=?";
	/** 加工完成单身. */
	public static final String QUERY_JGWC = "SELECT * FROM BO_AKL_SH_JGWC_S WHERE BINDID=?";
	/** 待加工. */
	public static final String QUERY_DJG = "SELECT * FROM BO_AKL_SH_DJG_S WHERE BINDID=?";
	/** 配件. */
	public static final String QUERY_PJ = "SELECT * FROM BO_AKL_SH_JG_PJXH_S WHERE BINDID=?";
	/** 加工规则. */
	public static final String QUERY_JGGZ = "SELECT * FROM BO_AKL_SH_JG_GZ_S WHERE BINDID=?";
	/** 加工规则消耗产品. */
	public static final String QUERY_JGGZ_XH = "SELECT * FROM BO_AKL_SH_JGGZ_VIEW WHERE GZBH=?";
	/** 加工规则完成产品. */
	public static final String QUERY_JGGZ_WC = "SELECT * FROM BO_AKL_SH_JGSC_VIEW WHERE GZBH=?";
	/** 加工规则配件产品. */
	public static final String QUERY_JGGZ_PJ = "SELECT * FROM BO_AKL_SH_JGPJ_VIEW WHERE GZBH=?";
	/** 加工完成汇总. */
	public static final String QUERY_JGGZ_HZ = "SELECT WLBH,WLMC,XH,GG,CPSX,SUM(SL)SL FROM BO_AKL_SH_JGWC_S WHERE BINDID=? GROUP BY WLBH,WLMC,XH,GG,CPSX";
	/** 非正常加工取送修数据. */
	public static final String QUERY_JGLX_FZCJG = "SELECT b.TPH,b.PCH,b.HWDM,b.SL FROM BO_AKL_SX_P a JOIN BO_AKL_SX_S b ON (a.BINDID=b.BINDID AND a.SXDH=?) "
			+ "LEFT JOIN BO_AKL_SHKC_S c ON (b.WLBH=c.WLBH AND b.SX=c.SX AND b.PCH=c.PCH AND b.HWDM=c.HWDM AND c.ZT=?) WHERE b.WLBH=? AND b.SX=? AND (b.CLFS='"+clfs0+"' OR b.CLFS='"+clfs1+"')";
	
	
	/** 更新送修单是否已加工. */
	public static final String UPDATE_SX_SFYJG = "UPDATE BO_AKL_SX_S SET SFYJG='"+is+"' WHERE BINDID=(SELECT BINDID FROM BO_AKL_SX_P WHERE SXDH=?) AND WLBH=? AND PCH=? AND HWDM=? AND SX=? AND SFTP='"+is+"' AND TPH=?";

}
