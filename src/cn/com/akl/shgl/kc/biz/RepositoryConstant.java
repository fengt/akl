package cn.com.akl.shgl.kc.biz;

public class RepositoryConstant {

	/** ��������_״̬_��; */
	public static final String GZTM_ZT_ZT = "070245";
	/** ��������_״̬_��ʧ */
	public static final String GZTM_ZT_DS = "070246";
	/** ��������_״̬_�ڿ� */
	public static final String GZTM_ZT_ZK = "070247";
	/** ��������_״̬_�ѳ� */
	public static final String GZTM_ZT_YC = "070248";
	/** ����_״̬_��; */
	public static final String WL_ZT_ZT = "070245";// "070249";
	/** ����_״̬_�ڿ� */
	public static final String WL_ZT_ZK = "070247";// "070250";

	/** �������ϵ�ʣ������. */
	public static final String UPDATE_MATERIAL_REMAINING_NUMBER = "UPDATE BO_AKL_SHKC_S SET KWSL=KWSL+? WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=? AND ZT=? AND ISNULL(KWSL,0)+?>=0";
	/** ��ѯ�����ڻ�λ������. */
	public static final String QUERY_MATERIAL_REMAINING_NUMBER = "SELECT SUM(KWSL) FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=? AND ZT=?";
	/** ��ѯ�����ڻ�λ����������. */
	public static final String QUERY_MATERIAL_REMAINING_NUMBER_LOCK = "SELECT SUM(SDSL) FROM BO_AKL_SH_KCSK WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=?";
	/** ��ѯ�����ڿͷ��ֿ��ʣ������. */
	public static final String QUERY_MATERIAL_REMAINING_NUMBER_INKF = "SELECT SUM(KWSL) FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND CKDM=? AND SX=? AND ZT=?";
	/** ��ѯ�����ڿͷ��ֿ����������. */
	public static final String QUERY_MATERIAL_REMAINING_NUMBER_INKF_LOCK = "SELECT SUM(SDSL) FROM BO_AKL_SH_KCSK WHERE XMLB=? AND WLBH=? AND CKDM=? AND SX=?";
	/** ��ѯ���ϵ���������. */
	public static final String QUERY_MATERIAL_REMAINING_NUMBER_ALL = "SELECT KWSL FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND HWDM=? AND SX=?";
	/** ��ѯ�Ƿ����ظ���¼. */
	public static final String QUERY_RECORD_COUNT = "SELECT COUNT(*) FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=? AND ZT=?";
	/** ��ѯ���к�. */
	public static final String QUERY_XLH_COUNT = "SELECT COUNT(*) FROM BO_AKL_SHKC_XLH_S WHERE XMLB=? AND WLBH=? AND PCH=? AND CKDM=? AND SX=? AND ZT=? AND GZTM=?";
	/** ��ѯ���к�ͨ���ͺ�. */
	public static final String QUERY_XLH_COUNT_XH = "SELECT COUNT(*) FROM BO_AKL_SHKC_XLH_S WHERE XMLB=? AND XH=? AND GZTM=?";

	public static final String QUERY_KYWL = "SELECT * FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND CKDM=? AND SX=? AND ZT=?";
}
