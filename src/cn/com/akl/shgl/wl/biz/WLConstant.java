package cn.com.akl.shgl.wl.biz;

/**
 * Created by huangming on 2015/4/29.
 */
public class WLConstant {

    /** 查询单身信息. */
    public static final String QUERY_WLDS = "SELECT * FROM BO_AKL_WLYSD_S WHERE BINDID=?";
    /** 查询明细汇总信息. */
    public static final String QUERY_WLMX_TOHZ = "SELECT XMLB, SUM(TJ) TJ,SUM(ZL) ZL,SUM(XS) XS,SUM(CPSL) CPSL,SUM(JE) JE FROM BO_AKL_WLYSD_S WHERE BINDID=? GROUP BY XMLB";
    /** 查询汇总单身. */
    public static final String QUERY_WLDS_HZ = "SELECT * FROM BO_AKL_WLYSD_XM_S WHERE BINDID=?";
    /** 更新待发货状态. */
    public static final String UPDATE_DFH_ZT = "UPDATE BO_AKL_DFH_P SET WLZT=? WHERE DH=? AND ISNULL(JLBZ,0)=?";

}
