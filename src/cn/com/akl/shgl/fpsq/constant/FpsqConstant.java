package cn.com.akl.shgl.fpsq.constant;

/**
 * Created by huangming on 2015/4/29.
 */
public class FpsqConstant {

    /** ��ֵ˰��Ʊ. */
    public static final String FPLX_ZZSFP = "��ֵ˰��Ʊ";
    /** ��ͨ��Ʊ. */
    public static final String FPLX_PTFP = "��ͨ��Ʊ";
    /** �ڲ��������ͻ�. */
    public static final String FPFHFS_PYZKF = "079293";
    /** ƽ�����ͻ�. */
    public static final String FPFHFS_KDZKH = "079294";
    /** ������ͻ�.     */
    public static final String FPFHFS_NBWLZKF = "079295";

    /** ��ѯ��Ʊ����ĵ���. */
    public static final String QUEYR_FPSQ_DH = "SELECT * FROM BO_AKL_FPSQ_S WHERE BINDID=?";
    /** ���½�����Ʒ�Ŀ�Ʊ��Ϣ.*/
    public static final String UPDATE_JFCP_KPXX = "UPDATE BO_AKL_WXJF_S SET SFYKP=? WHERE HH=? AND (SFYKP IS NULL Or SFYKP=?)";

}
