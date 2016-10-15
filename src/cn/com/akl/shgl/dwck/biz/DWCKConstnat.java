package cn.com.akl.shgl.dwck.biz;

public class DWCKConstnat {

	/** ��ѯ��Ŀ����. */
	public static final String QUERY_DWCK_XMLX = "SELECT XMLX FROM BO_AKL_SH_DWCK_P WHERE BINDID=?";
	/** ��ѯ�Ƿ�������κ�װ��. */
	public static final String QUERY_DWCK_SFGJPCHZX = "SELECT SFGJPCHZX FROM BO_AKL_SH_DWCK_P WHERE BINDID=?";
	/** ������ⵥͷ. */
	public static final String QUERY_DWCK_HEAD = "SELECT * FROM BO_AKL_SH_DWCK_P WHERE BINDID=?";
	/** ������ⵥ��. */
	public static final String QUERY_DWCK_BODY = "SELECT * FROM BO_AKL_SH_DWCK_S WHERE BINDID=?";
	/** ������ⵥ�����. */
	public static final String QUERY_DWCK_HZ_BODY = "SELECT * FROM BO_AKL_SH_DWCK_HZ_S WHERE BINDID=?";
	/** ���ܳ��ⵥ�����װ�䵥. */
	public static final String QUERY_DWCK_BODY_GROUP_ZXD_PCH = "SELECT WLBH,WLMC CPMC,CPSX,PCH,SUM(SJCKSL) SL,CPLX,SUM(SJCKSL) ZXSL FROM BO_AKL_SH_DWCK_S WHERE BINDID=? GROUP BY WLBH,WLMC,CPSX,CPLX,PCH";
	/** ���ܳ��ⵥ�����װ�䵥.(������κ�.) */
	public static final String QUERY_DWCK_BODY_GROUP_ZXD = "SELECT WLBH,WLMC CPMC,CPSX,'' AS PCH,SUM(SJCKSL) SL,CPLX,SUM(SJCKSL) ZXSL FROM BO_AKL_SH_DWCK_S WHERE BINDID=? GROUP BY WLBH,WLMC,CPSX,CPLX";

	/** ��ѯ������ⵥ�����Ϻź����κŻ���. */
	public static final String QUERY_DWCK_GROUP_WLH = "SELECT WLBH, WLMC CPMC, '' AS PCH, SUM(SJCKSL) SL FROM BO_AKL_SH_DWCK_S WHERE BINDID=? GROUP BY WLBH,WLMC";
	/** ��ѯ������ⵥ�����ϺŻ���. */
	public static final String QUERY_DWCK_GROUP_WLHPCH = "SELECT WLBH, PCH, WLMC CPMC, SUM(SJCKSL) SL FROM BO_AKL_SH_DWCK_S WHERE BINDID=? GROUP BY WLBH,PCH,WLMC";
	/** ��ѯװ�䵥�����Ϻź����κŻ���. */
	public static final String QUERY_ZXD_GROUP_WLHPCH = "SELECT WLBH, CPMC, PCH, SUM(ISNULL(ZXSL, 0)) SL FROM BO_AKL_ZXD_S WHERE BINDID=? GROUP BY WLBH,PCH,CPMC";
}
