package cn.com.akl.shgl.yzsq.biz;

/**
 * Created by huangming on 2015/4/28.
 */
public class YZSQConstant {

    public static final String[][] TABLE_MAP = new String[][]{
            {"Ӫҵִ��", "BO_AKL_ZZGL_YYZZ_S"},
            {"��֯��������֤", "BO_AKL_ZZGL_ZZJGDMZ_S"},
            {"˰��Ǽ�֤_��˰", "BO_AKL_ZZGL_SWDJZGS_S"},
            {"˰��Ǽ�֤_��˰", "BO_AKL_ZZGL_SWDJZDS_S"},
            {"����������˾", "BO_AKL_ZZGL_DSFCWGSXX_S"},
            {"��������Ϣ", "BO_AKL_ZZGL_KHH_S"},
            {"ӡ����Ϣ", "BO_AKL_ZZGL_YZXX_S"}
    };

    /** �ܲ����ͷ�. */
    public static final String DFH_JLBS_ZTK = "0";
    /** �ͷ����ܲ�. */
    public static final String DFH_JLBS_KTZ = "1";

    /** ��ѯ����. */
    public static final String QUERY_FORM_BODY = "SELECT * FROM BO_AKL_YZSQ_S WHERE BINDID=?";
    /** ��ѯ�������뵥��. */
    public static final String QUERY_FORM_HEAD_YZSQDH = "SELECT YZSQDH FROM BO_AKL_YZSQ_P WHERE BINDID=?";
}
