package cn.com.akl.shgl.jg.biz;

public class JGConstant {

	/** �ӹ���ʽ�� �����ӹ�. */
	public static final String JGLX_ZCJG = "�����ӹ�";
	/** �ӹ���ʽ�� �������ӹ�. */
	public static final String JGLX_FZCJG = "�������ӹ�";
	/** ���޴���ʽ. */
	public static final String clfs0 = "064286";//����
	public static final String clfs1 = "064289";//���ջ�
	/** ��. */
	public static final String is = "025000";

	/** ����Ʒ�������: �ӡ������Ŀ��. */
	public static final String add = "+";
	public static final String subtract = "-";
	
	/** �ӹ���Ŀ���. */
	public static final String QUERY_XMLB = "SELECT XMLB FROM BO_AKL_SH_JG_P WHERE BINDID=?";
	/** ���޵���. */
	public static final String QUERY_SXDH = "SELECT SXDH FROM BO_AKL_SH_JG_P WHERE BINDID=?";
	/** �ӹ�����. */
	public static final String QUERY_JGLX = "SELECT JGLX FROM BO_AKL_SH_JG_P WHERE BINDID=?";
	/** �ӹ���ɵ���. */
	public static final String QUERY_JGWC = "SELECT * FROM BO_AKL_SH_JGWC_S WHERE BINDID=?";
	/** ���ӹ�. */
	public static final String QUERY_DJG = "SELECT * FROM BO_AKL_SH_DJG_S WHERE BINDID=?";
	/** ���. */
	public static final String QUERY_PJ = "SELECT * FROM BO_AKL_SH_JG_PJXH_S WHERE BINDID=?";
	/** �ӹ�����. */
	public static final String QUERY_JGGZ = "SELECT * FROM BO_AKL_SH_JG_GZ_S WHERE BINDID=?";
	/** �ӹ��������Ĳ�Ʒ. */
	public static final String QUERY_JGGZ_XH = "SELECT * FROM BO_AKL_SH_JGGZ_VIEW WHERE GZBH=?";
	/** �ӹ�������ɲ�Ʒ. */
	public static final String QUERY_JGGZ_WC = "SELECT * FROM BO_AKL_SH_JGSC_VIEW WHERE GZBH=?";
	/** �ӹ����������Ʒ. */
	public static final String QUERY_JGGZ_PJ = "SELECT * FROM BO_AKL_SH_JGPJ_VIEW WHERE GZBH=?";
	/** �ӹ���ɻ���. */
	public static final String QUERY_JGGZ_HZ = "SELECT WLBH,WLMC,XH,GG,CPSX,SUM(SL)SL FROM BO_AKL_SH_JGWC_S WHERE BINDID=? GROUP BY WLBH,WLMC,XH,GG,CPSX";
	/** �������ӹ�ȡ��������. */
	public static final String QUERY_JGLX_FZCJG = "SELECT b.TPH,b.PCH,b.HWDM,b.SL FROM BO_AKL_SX_P a JOIN BO_AKL_SX_S b ON (a.BINDID=b.BINDID AND a.SXDH=?) "
			+ "LEFT JOIN BO_AKL_SHKC_S c ON (b.WLBH=c.WLBH AND b.SX=c.SX AND b.PCH=c.PCH AND b.HWDM=c.HWDM AND c.ZT=?) WHERE b.WLBH=? AND b.SX=? AND (b.CLFS='"+clfs0+"' OR b.CLFS='"+clfs1+"')";
	
	
	/** �������޵��Ƿ��Ѽӹ�. */
	public static final String UPDATE_SX_SFYJG = "UPDATE BO_AKL_SX_S SET SFYJG='"+is+"' WHERE BINDID=(SELECT BINDID FROM BO_AKL_SX_P WHERE SXDH=?) AND WLBH=? AND PCH=? AND HWDM=? AND SX=? AND SFTP='"+is+"' AND TPH=?";

}
