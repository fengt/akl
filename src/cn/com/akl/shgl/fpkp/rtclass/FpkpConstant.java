package cn.com.akl.shgl.fpkp.rtclass;

public class FpkpConstant {

	/** ��Ʊ״̬�����뿪Ʊ. */
	public static final String FPZT_SQKP = "078288";
	/** ��Ʊ״̬��δ��Ʊ. */
	public static final String FPZT_WKP = "078289";
	/** ��Ʊ״̬���ѿ�Ʊ. */
	public static final String FPZT_YKP = "078290";
	/** ��Ʊ״̬����ǩ��. */
	public static final String FPZT_YQS = "078291";
	/** ��Ʊ״̬����ʧ. */
	public static final String FPZT_DS = "078292";
	/** ��Ʊ״̬����;. */
	public static final String FPZT_ZT = "078293";

	/** ��Ʊǩ��״̬����ǩ�� */
	public static final String QSZT_QS = "��ǩ��";
	/** ��Ʊǩ��״̬���Ѷ�ʧ */
	public static final String QSZT_DS = "��ʧ";

	/** ��Ʊ���ط�ʽ���ڲ��������ͷ� */
	public static final String FPFHFS_NBWL = "079293";
	/** ��Ʊ���ط�ʽ��ƽ�����ͻ� */
	public static final String FPFHFS_PY = "079294";
	/** ��Ʊ���ط�ʽ��������ͻ� */
	public static final String FPFHFS_KD = "079295";

	/** ��ѯ��Ʊ��Ʊ�ĵ���. */
	public static final String QUERY_FPKP_FORM_BODY = "SELECT * FROM BO_AKL_FPKP_S WHERE BINDID=?";
	/** ��ѯ�����к��д˷�Ʊ���뵥�ŵ�����. */
	public static final String QUERY_FPKP_FORM_BODY_FPSQDH = "SELECT BINDID FROM BO_AKL_FPKP_S WHERE FPSQDH=? AND ISEND=0 AND BINDID<>?";
	/** ��Ʊ״̬. */
	public static final String QUERY_FPSQ_FPZT = "SELECT FPZT FROM BO_AKL_FPSQ WHERE FPSQDH=?";
	/** �������޵���Ʊ״̬. */
	public static final String UPDATE_SXD_KPZT = "UPDATE BO_AKL_SX_P SET SFKP=? WHERE SXDH=?";
	/** ���¿�Ʊ�����״̬. */
	public static final String UPDATE_KPSQ_KPZT = "UPDATE BO_AKL_FPSQ SET FPZT=? WHERE FPSQDH=?";

}
