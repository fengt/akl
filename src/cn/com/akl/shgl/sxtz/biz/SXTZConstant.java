package cn.com.akl.shgl.sxtz.biz;

import cn.com.akl.shgl.sx.cnt.SXCnt;

/**
 * Created by huangming on 2015/5/8.
 */
public class SXTZConstant {

    /**
     * 原单据主表.
     */
    public static final String NEW_TABLE_MAIN = "BO_AKL_SH_SXTZ_P";
    /**
     * 修改单据子.
     */
    public static final String NEW_TABLE_SUB = "BO_AKL_SH_SXTZ_S";
    /**
     * 原单据子表.
     */
    public static final String OLD_TABLE_MAIN = "BO_AKL_SX_P";
    /**
     * 修改单据主表.
     */
    public static final String OLD_TABLE_SUB = "BO_AKL_SX_S";
    /**
     * 单号字段.
     */
    public static final String FROM_DH = "SXDH";

    /**
     * 抓取送修单号.
     */
    public static final String QUERY_FORM_SXDH = "SELECT " + FROM_DH + " FROM " + NEW_TABLE_MAIN + " WHERE BINDID=?";
    /**
     * 获取原送修单BINDID.
     */
    public static final String QUERY_FORM_PARENTBINDID = "SELECT PARENTBINDID FROM " + NEW_TABLE_MAIN + " WHERE BINDID=?";
    /*
    * 获取原送修单BINDID.
    */
    public static final String QUERY_OLD_FORM_BINDID = "SELECT BINDID FROM " + OLD_TABLE_MAIN + " WHERE " + FROM_DH + "=?";
    /**
     * 获取最后更新时间.
     */
    public static final String QUERY_FORM_LASTTIME = "SELECT LASTTIME FROM " + NEW_TABLE_MAIN + " WHERE BINDID=?";
    /**
     * 获取送修单的最后修改时间.
     */
    public static final String QUEYR_OLD_FORM_LASTTIME = "SELECT UPDATEDATE FROM " + OLD_TABLE_MAIN + " WHERE SXDH=?";
    /**
     * 查询原送修单子表记录.
     */
    public static final String QUERY_OLD_FORM_BODY = "SELECT * FROM " + OLD_TABLE_SUB + " WHERE BINDID=?";
    /**
     * 查询新送修单子表数据.
     */
    public static final String QUERY_NEW_FORM_BODY = "SELECT * FROM " + NEW_TABLE_SUB + " WHERE BINDID=?";
    /**
     * 查询原表单的最后时间.
     */
    public static final String QUERY_OLD_FORM_LASTTIME = "SELECT UPDATEDATE FROM " + SXTZConstant.OLD_TABLE_SUB + " WHERE ID=?";

    public static final String QUERY_YSX_S = "SELECT * FROM BO_AKL_SH_YSX_S WHERE BINDID=?";//查询原送修产品单身信息
    
    public static final String QUERY_XMLB = "SELECT XMLB FROM BO_AKL_SH_SXTZ_P WHERE BINDID=?";//查询项目类别
	public static final String QUERY_YSXFS = "SELECT YSXFS FROM BO_AKL_SH_SXTZ_P WHERE BINDID=?";//查询原送修方式
	public static final String QUERY_SXFS = "SELECT SXFS FROM BO_AKL_SH_SXTZ_P WHERE BINDID=?";//查询送修方式
	public static final String QUERY_YYWLX = "SELECT YYWLX FROM BO_AKL_SH_SXTZ_P WHERE BINDID=?";//查询原业务类型
	public static final String QUERY_YWLX = "SELECT YWLX FROM BO_AKL_SH_SXTZ_P WHERE BINDID=?";//查询业务类型
	public static final String QUERY_XMKF = "SELECT XMKF FROM BO_AKL_SH_SXTZ_P WHERE BINDID=?";//查询项目库房
    
    /**
     * 交付信息查询
     */
	public static final String QUERY_JF_SFYJ = "SELECT SFYJ FROM BO_AKL_WXJF_P WHERE JFDH=?";//查询交付是否邮寄
	public static final String QUERY_JF_BINDID = "SELECT BINDID FROM BO_AKL_WXJF_P WHERE JFDH=?";//查询交付BINDID
	
	public static final String QUERY_isExsitJFD = "SELECT JFDH FROM BO_AKL_WXJF_P WHERE ISEND=? AND SXDH=?";//查询是否有未完成的交付单
	
	public static final String QUERY_JF_S = "SELECT s.* FROM BO_AKL_WXJF_P p,BO_AKL_WXJF_S s WHERE p.BINDID=s.BINDID AND p.JFDH=?";//查询交付单子表
	
	public static final String QUERY_JF_S_PJ = "SELECT s.* FROM BO_AKL_WXJF_P p,BO_AKL_PJCP s WHERE p.BINDID=s.BINDID AND p.JFDH=?";//查询交付单配件
	
	/**
	 * 库存查询及更新
	 */
    public static final String QUERY_SXTZ_S_HZ = "SELECT WLBH,WLMC,XH,PCH,SUM(SL)SL FROM BO_AKL_SH_SXTZ_S WHERE BINDID=? GROUP BY WLBH,WLMC,XH,PCH";//查询送修产品单身信息（汇总）
    
    public static final String UPDATE_KCHZ = "UPDATE BO_AKL_SHKC_P SET RKSL=ISNULL(RKSL,0)+?,PCSL=ISNULL(PCSL,0)+? WHERE XMLB=? AND WLBH=? AND PCH=?";//更新库存信息
    
    public static final String UPDATE_KCMX_KWSL = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)+? WHERE XMLB=? AND WLBH=? AND HWDM=? AND PCH=? AND SX=? AND ZT='070247'";//更新库存（数量）
    
    public static final String DELETE_XLH = "DELETE FROM BO_AKL_SHKC_XLH_S WHERE GZTM=?";//删除序列号
    
    /**
     * 调整单据字段更新
     */
    public static final String UPDATE_SXTZ_P_ZT = "UPDATE BO_AKL_SH_SXTZ_P SET ZT='"+SXCnt.zt1+"' WHERE BINDID=?";//更新送修单头（状态）
	public static final String UPDATE_SXTZ_S_ZT = "UPDATE BO_AKL_SH_SXTZ_S SET ZT='"+SXCnt.zt2+"' WHERE BINDID=?";//更新送修单身（状态）
    public static final String UPDATE_SXTZ_HHAndHWDMAndSX = "UPDATE BO_AKL_SH_SXTZ_S SET SXCPHH=?,HWDM=?,SX=? WHERE ID=?";//跟新调整送修单身(行号、货位代码、属性)
    
    /**
     * 删除待发货记录（送修和交付）
     */
    public static final String DELETE_SX_DFH_P = "DELETE FROM BO_AKL_DFH_P WHERE BINDID=(SELECT BINDID FROM BO_AKL_SX_P WHERE SXDH=?)";//删除待发货主表
    public static final String DELETE_SX_DFH_S = "DELETE FROM BO_AKL_DFH_S WHERE BINDID=(SELECT BINDID FROM BO_AKL_SX_P WHERE SXDH=?)";//删除待发货子表
    
    public static final String DELETE_JF_DFH_P = "DELETE FROM BO_AKL_DFH_P WHERE BINDID=(SELECT BINDID FROM BO_AKL_WXJF_P WHERE JFDH=?)";//删除待发货主表
    public static final String DELETE_JF_DFH_S = "DELETE FROM BO_AKL_DFH_S WHERE BINDID=(SELECT BINDID FROM BO_AKL_WXJF_P WHERE JFDH=?)";//删除待发货子表
    
}
