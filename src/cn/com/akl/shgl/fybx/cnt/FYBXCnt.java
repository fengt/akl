package cn.com.akl.shgl.fybx.cnt;

public class FYBXCnt {

	public static final String zt0 = " 报销中";
	public static final String zt1 = " 已报销";

	public static final String djlx0 = "0";//单据引发
	public static final String djlx1 = "1";//非单据引发

	/** 费用报销状态,未报销. */
	public static final String KFFYBXZT_WBX="0";
	/** 费用报销状态,已报销. */
	public static final String KFFYBXZT_YBX="1";

	/**
	 * 服务中心
	 */

	/** 单据类型. */
	public static final String QUERY_KF_P_DJLX = "SELECT DJLX FROM BO_AKL_FWZXBX_P WHERE BINDID=?";
	/** 查询费用明细子表. */
	public static final String QUERY_KF_S = "SELECT * FROM BO_AKL_KDFY_S WHERE BINDID=?";
    /** 查询发票汇总信息. */
    public static final String QUERY_KF_FPHZ = "SELECT BXXM, PZLX, XMLB, MIN (FYFSSJ) FYFSSJ, MAX (FYJSSJ) FYJSSJ, SUM (ZS) ZS, SUM (ZE) ZE FROM BO_AKL_FPMX_S WHERE BINDID = ? GROUP BY BXXM, XMLB, PZLX";
    /** 单头状态更新. */
    public static final String UPDATE_KF_P_ZT = "UPDATE BO_AKL_FWZXBX_P SET ZT=? WHERE BINDID=?";
    /** 更新发票汇总数据. */
    public static final String UPDATE_KF_FPHZ = "UPDATE BO_AKL_FWZXBX_FPHZ_S SET ZS=?, ZE=?, FYFSSJ=?, FYJSSJ=? WHERE BXXM=? AND PZLX=? AND BINDID=? AND XMLB=?";

	/**
	 * 总部
	 */
	/** 单据类型. */
	public static final String QUERY_ZB_P_DJLX = "SELECT DJLX FROM BO_AKL_ZBFYBXSQ_P WHERE BINDID=?";
	/** 查询费用明细子表. */
	public static final String QUERY_ZB_S = "SELECT * FROM BO_AKL_ZBFYBXFY_S WHERE BINDID=?";
	/** 单头状态更新. */
	public static final String UPDATE_ZB_P_ZT = "UPDATE BO_AKL_ZBFYBXSQ_P SET ZT=? WHERE BINDID=?";
	/** 查询总部费用. */
	public static final String QUERY_ZBFY_S = "SELECT * FROM BO_AKL_ZBFYBXFY_S WHERE BINDID=?";


	/**
	 * 相关单据（送修单、交付单）状态更新
	 */
	/** 更新送修费用状态. */
	public static final String UPDATE_SX_FYZT = "UPDATE BO_AKL_SX_P SET FYZT=? WHERE SXDH=?";
	/** 更新交付费用状态. */
	public static final String UPDATE_JF_FYZT = "UPDATE BO_AKL_WXJF_P SET FYZT=? WHERE JFDH=?";

}
