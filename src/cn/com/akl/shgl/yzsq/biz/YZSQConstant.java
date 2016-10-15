package cn.com.akl.shgl.yzsq.biz;

/**
 * Created by huangming on 2015/4/28.
 */
public class YZSQConstant {

    public static final String[][] TABLE_MAP = new String[][]{
            {"营业执照", "BO_AKL_ZZGL_YYZZ_S"},
            {"组织机构代码证", "BO_AKL_ZZGL_ZZJGDMZ_S"},
            {"税务登记证_国税", "BO_AKL_ZZGL_SWDJZGS_S"},
            {"税务登记证_地税", "BO_AKL_ZZGL_SWDJZDS_S"},
            {"第三方财务公司", "BO_AKL_ZZGL_DSFCWGSXX_S"},
            {"开户行信息", "BO_AKL_ZZGL_KHH_S"},
            {"印章信息", "BO_AKL_ZZGL_YZXX_S"}
    };

    /** 总部到客服. */
    public static final String DFH_JLBS_ZTK = "0";
    /** 客服到总部. */
    public static final String DFH_JLBS_KTZ = "1";

    /** 查询单身. */
    public static final String QUERY_FORM_BODY = "SELECT * FROM BO_AKL_YZSQ_S WHERE BINDID=?";
    /** 查询用章申请单号. */
    public static final String QUERY_FORM_HEAD_YZSQDH = "SELECT YZSQDH FROM BO_AKL_YZSQ_P WHERE BINDID=?";
}
