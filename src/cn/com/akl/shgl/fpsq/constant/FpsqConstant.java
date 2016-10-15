package cn.com.akl.shgl.fpsq.constant;

/**
 * Created by huangming on 2015/4/29.
 */
public class FpsqConstant {

    /** 增值税发票. */
    public static final String FPLX_ZZSFP = "增值税发票";
    /** 普通发票. */
    public static final String FPLX_PTFP = "普通发票";
    /** 内部物流至客户. */
    public static final String FPFHFS_PYZKF = "079293";
    /** 平邮至客户. */
    public static final String FPFHFS_KDZKH = "079294";
    /** 快递至客户.     */
    public static final String FPFHFS_NBWLZKF = "079295";

    /** 查询发票申请的单身. */
    public static final String QUEYR_FPSQ_DH = "SELECT * FROM BO_AKL_FPSQ_S WHERE BINDID=?";
    /** 更新交付产品的开票信息.*/
    public static final String UPDATE_JFCP_KPXX = "UPDATE BO_AKL_WXJF_S SET SFYKP=? WHERE HH=? AND (SFYKP IS NULL Or SFYKP=?)";

}
