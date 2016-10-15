package cn.com.akl.shgl.db.biz;

public class DBConstant {

	/** ��ѯ������ͷ��Ϣ. */
	public static final String QUERY_DB_FORM_HEAD = "SELECT * FROM BO_AKL_DB_P WHERE BINDID=?";
	/** ��ѯ�������к���Ϣ. */
	public static final String QUERY_DB_FORM_XLH = "SELECT * FROM BO_AKL_DB_XLH_S WHERE BINDID=?";
	/** ��ѯ����������Ϣ */
	public static final String QUERY_DB_FORM_BODY = "SELECT * FROM BO_AKL_DB_S WHERE BINDID=?";
	/** ��ѯ�������������Ϣ */
	public static final String QUERY_DB_FORM_HZ_BODY = "SELECT * FROM BO_AKL_DB_HZ_S WHERE BINDID=?";
	/** ��ѯ����������Ŀ���� */
	public static final String QUERY_DB_FORM_XMLX = "SELECT XMLX FROM BO_AKL_DB_P WHERE BINDID=?";
	/** ��ѯ���������ջ��ֿ���� */
	public static final String QUERY_DB_FORM_SHCKBM = "SELECT SHKFCKBM FROM BO_AKL_DB_P WHERE BINDID=?";
	/** ��ѯ�������ķ����ֿ���� */
	public static final String QUERY_DB_FORM_FHCKBM = "SELECT FHKFCKBM FROM BO_AKL_DB_P WHERE BINDID=?";
	/** ��ѯ��������. */
	public static final String QUERY_DB_FORM_DBDH = "SELECT DBDH FROM BO_AKL_DB_P WHERE BINDID=?";
    /** ��ѯ��������. */
    public static final String QUERY_DB_FORM_DBLX = "SELECT DBLX FROM BO_AKL_DB_P WHERE BINDID=?";
	/** ��ѯ����װ�䷽ʽ. */
	public static final String QUERY_DB_FORM_SFGJPCHZX = "SELECT SFGJPCHZX FROM BO_AKL_DB_P WHERE BINDID=?";
	/** ��ѯ�����������ڲ���װ�䵥.*/
	public static final String QUERY_DB_FORM_BODY_GROUP_ZXD = "SELECT WLBH,WLMC CPMC,CPSX,'' as PCH,SUM(SJCKSL) SL,CPLX,SUM(SJCKSL) ZXSL FROM BO_AKL_DB_S WHERE BINDID=? GROUP BY WLBH,WLMC,CPSX,CPLX";
	/** ��ѯ�����������ڲ���װ�䵥.�����PCH��*/
	public static final String QUERY_DB_FORM_BODY_GROUP_ZXD_PCH = "SELECT WLBH,WLMC CPMC,CPSX,PCH,SUM(SJCKSL) SL,CPLX,SUM(SJCKSL) ZXSL FROM BO_AKL_DB_S WHERE BINDID=? GROUP BY PCH,WLBH,WLMC,CPSX,CPLX";
	/** �������Ϻź����κŽ��з���. */
	public static final String QUERY_ZXD_GROUP_WLHPCH = "SELECT WLBH, CPMC, PCH, SUM(ISNULL(ZXSL, 0)) SL FROM BO_AKL_ZXD_S WHERE BINDID=? GROUP BY WLBH,PCH,CPMC";
	/** �������Ϻź����κŶ�װ�䵥���з���. */
	public static final String QUERY_DB_FORM_BODY_GROUP_WLH = "SELECT WLBH, WLMC CPMC, '' AS PCH, SUM(SJCKSL) SL FROM BO_AKL_DB_S WHERE BINDID=? GROUP BY WLBH,WLMC";
	/** �������ϺŶ�װ�䵥���з���. */
	public static final String QUERY_DB_FORM_BODY_GROUP_WLHPCH = "SELECT WLBH, WLMC CPMC, PCH, SUM(SJCKSL) SL FROM BO_AKL_DB_S WHERE BINDID=? GROUP BY PCH,WLBH,WLMC";

	/** ���µ�����������ջ��ֿ���� */
	public static final String UPDATE_DB_FORM_SHCKBM_AND_RKSL = "UPDATE BO_AKL_DB_S SET RKCKDM=?, RKCKMC=?, RKHWDM=?, RKSL=?, SJRKSL=? WHERE ID=?";

	/** �������ͣ����ȷ���. */
	public static final String DBLX_JDFJ = "���ȷ���";

}
