package cn.com.akl.shgl.dwrk.biz;

public class DWRKConstant {

	/** ��ѯ��Ŀ����. */
	public static final String QUERY_DWRK_XMLX = "SELECT XMLX FROM BO_AKL_SH_DWRK_P WHERE BINDID=?";
	/** �ջ��ֿ����. */
	public static final String QUERY_DWRK_SHKFCKBM = "SELECT SHKFCKBM FROM BO_AKL_SH_DWRK_P WHERE BINDID=?";
	/** ������ⵥͷ. */
	public static final String QUERY_DWRK_HEAD = "SELECT * FROM BO_AKL_SH_DWRK_P WHERE BINDID=?";
	/** ������ⵥ��. */
	public static final String QUERY_DWRK_BODY = "SELECT * FROM BO_AKL_SH_DWRK_S WHERE BINDID=?";

	/** ���µ�����������ջ��ֿ���� */
	public static final String UPDATE_DB_FORM_SHCKBM_AND_RKSL = "UPDATE BO_AKL_SH_DWRK_S SET RKHWDM=?, RKCKDM=?, RKCKMC=?, PCH=?, RKSL=?, SJRKSL=? WHERE ID=?";
}
