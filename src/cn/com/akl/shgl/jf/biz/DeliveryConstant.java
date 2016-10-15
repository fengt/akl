package cn.com.akl.shgl.jf.biz;

import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

public class DeliveryConstant {
	/** �������ԣ���Ʒ. */
	public static final String WLSX_XP = "066216";

	/** �������ͣ��滻����.*/
	public static final String SJLX_THFA = "089346";
	/** �������ͣ�����.*/
	public static final String SJLX_ZS = "089347";
	/** �������ͣ�һ����.*/
	public static final String SJlX_YHD = "089348";

	/** ҵ������: ����.*/
	public static final String YWLX_XS = "083324";
	/** ҵ������: ����.*/
	public static final String YWLX_ZS = "083323";

	/** ��˽��. */
	public static final String SHJG_TY = "ͬ��";
	public static final String SHJG_BH = "����";

	/** ���� */
	public static final String CLFS_HX = "064286";
	/** ����ά�� */
	public static final String CLFS_BNWX = "064287";
	/** ����ά��.*/
	public static final String CLFS_BWWX = "064223";
	/** �˻� */
	public static final String CLFS_TH = "064288";
	/** ���ջ� */
	public static final String CLFS_DSH = "064289";
	/** ��ʵ����� */
	public static final String CLFS_WSWGH = "064290";
	/** ���컻��. */
	public static final String CLFS_FJHX = "064222";
	/** �����˻�. */
	public static final String CLFS_SJTH = "064221";
	/** ����. */
	public static final String CLFS_XS = "064738";
	/** ����. */
	public static final String CLFS_ZS = "064737";

	/** ���޵�ͷ״̬��������. */
	public static final String SX_H_ZT_DJF = "069239";
	/** ���޵�ͷ״̬���ѽ���. */
	public static final String SX_H_ZT_YJF = "069240";
	/** ���޵���״̬���Ѽ��. */
	public static final String SX_B_ZT_YJC = "069243";
	/** ���޵���״̬���ѽ���. */
	public static final String SX_B_ZT_YJF = "069244";

	/** ȱ���ȴ�. */
	public static final String JF_JLZT_QHDD = "090351";
	/** ��֪ͨ. */
	public static final String JF_JLZT_YTZ = "090352";
	/** �ѽ���. */
	public static final String JF_JLZT_YJF = "090353";
	/** ������. */
	public static final String JF_JLZT_DJF = "090350";

	/** ¼����Ϣ. */
	public static final int STEP_LRXX = 1;
	/** ��ؽ������. */
	public static final int STEP_YDJFSH = 2;
	/** �滻������������. */
	public static final int STEP_SJCL = 3;
	/** �滻������������ȷ��. */
	public static final int STEP_SJCLKFQR = 4;
	/** ȱ������. */
	public static final int STEP_QHSQ = 5;
	/** ֪ͨ�ͻ�ȡ��. */
	public static final int STEP_TZKHQH = 7;
	/** ����. */
	public static final int STEP_JF = 7;
	/** ����. */
	public static final int STEP_ZPSH = 8;

	/** ��ѯ�����滻�����Ӧ���滻����. */
	public static final String QUERY_REPLACE_WLBH = "SELECT thgz.WLBH FROM BO_AKL_THGZ thgz WHERE thgz.HCKJ=? AND THGZ=? AND ISNULL(YXJFZ,0)>=ISNULL(?,0) AND SX=? ORDER BY THYXJ DESC";
	/** ��ѯ�滻�������. */
	public static final String QUERY_THGZ = "SELECT DISTINCT THGZ FROM BO_AKL_THGZ WHERE XMLB=? AND SRKJ=? AND WLBH=? AND SX=?";
	/** ��ѯ�滻���ȼ�. */
	public static final String QUERY_YXJFZ = "SELECT YXJFZ FROM BO_AKL_THGZ WHERE XMLB=? AND SRKJ=? AND WLBH=? AND THGZ=? AND SX=?";
	/** ��ѯ��Ŀ���. */
	public static final String QUERY_XMLB = "SELECT XMLB FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** ��ѯ���޵���. */
	public static final String QUERY_SXDH = "SELECT SXDH FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** ��ѯ�Ƿ��ʼ�. */
	public static final String QUERY_JFDH = "SELECT JFDH FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** ��ѯ��������. */
	public static final String QUERY_SFYJ = "SELECT SFYJ FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** ��ѯ�Ƿ���ؽ���. */
	public static final String QUERY_SFYDJF = "SELECT SFYDJF FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** ��ѯ�⳥�� */
	public static final String QUERY_PCJ = "SELECT PCJ FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** ��ѯ��ǰ�ͷ��ֿ�. */
	public static final String QUERY_BDCKDM = "SELECT BDKFCKBM FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** ��ѯ������޿ͷ�. */
	public static final String QUERY_YDCKDM = "SELECT YDJFKFBM FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** ��ѯ��ǰҵ������. */
	public static final String QUERY_KHLX = "SELECT KHLX FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** ��ѯ��ǰ�ͻ�����. */
	public static final String QUERY_YWLX = "SELECT YWLX FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** ��ѯ�������޵���. */
	public static final String QUERY_JF_SXDS = "SELECT * FROM BO_AKL_WXJF_SX_S WHERE BINDID=?";
	/** ��ѯ������������. */
	public static final String QUERY_JF_JFDS = "SELECT * FROM BO_AKL_WXJF_S WHERE BINDID=?";
	/** ��ѯ�����������. */
	public static final String QUERY_JF_PJDS = "SELECT * FROM BO_AKL_PJCP WHERE BINDID=?";
	/** ��ѯ��������Ʒ����. */
	public static final String QUERY_JF_DYPDS = "SELECT * FROM BO_AKL_WXJF_DYP_S WHERE BINDID=?";
	/** ��ѯ������Ӧ�������Ϣ. */
	public static final String QUERY_JF_PJXX = "SELECT PJCPBH, PJCPXH, PJCPMC, PJXHSL FROM BO_AKL_SH_WXPJGX WHERE WXCPBH=? AND WXBW=?";
	/** ��ѯ���ϼ۸�. */
	public static final String QUERY_WL_JG = "SELECT JG FROM BO_AKL_SH_JGGL WHERE XLMB=? AND WLBH=?";
	/** ��ѯ�����Ϣ�ظ���¼. */
	public static final String QUERY_PJXX_CFJL = "SELECT COUNT(*) FROM BO_AKL_PJCP WHERE BINDID=? AND WLBH=? AND CKDM=? AND HWDM=? AND PCH=?";
	/** ���½�����¼��״̬. */
	public static final String UPDATE_JFJL_ZT = "UPDATE BO_AKL_WXJF_S SET ZT=? WHERE BINDID=?";

	/** ��ѯ����ȱ�������¼. */
	public static final String QUERY_JF_QHSQJL2 = "SELECT * FROM BO_AKL_WXJF_P p LEFT JOIN BO_AKL_WXJF_S s ON p.bindid=s.bindid WHERE p.BINDID=? AND SFQHSQ=?";
	/** ���´���Ʒ���. */
	public static final String UPDATE_DYP_KC = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)-? WHERE SX='049304' AND WLBH=? AND HWDM=?";
	/** �������޵�ͷ״̬. */
	public static final String UPDATE_SXDT_ZT = "UPDATE BO_AKL_SX_P SET ZT=? WHERE SXDH=?";
	/** �������޼�¼״̬. */
	public static final String UPDATE_SXDS_ZT = "UPDATE BO_AKL_SX_S SET ZT=? WHERE ID=?";
	/** ���¹�������״̬. */
	public static final String UPDATE_GZTM_ZT = "UPDATE BO_AKL_SHKC_XLH_S SET ZT=? WHERE GZTM=?";
	/** ���������Ϣ���� */
	public static final String UPDATE_PJXX_SL = "UPDATE BO_AKL_PJCP SET SL=ISNULL(SL,0)+? WHERE BINDID=? AND WLBH=? AND CKDM=? AND HWDM=? AND PCH=?";

	/** �Զ�ƥ���к� */
	public static final String AUTO_MATCH_ROWNUM_SXDS = "SELECT MIN (rownum) FROM ( SELECT rownum FROM ( SELECT row_number () OVER (ORDER BY id) rownum FROM BO_AKL_WXJF_SX_S WHERE bindid = ? ) rowt WHERE rownum NOT IN ( SELECT ISNULL(HH,0) FROM BO_AKL_WXJF_SX_S WHERE bindid = ? ) UNION SELECT COUNT (*) + 1 rownum FROM BO_AKL_WXJF_SX_S WHERE bindid = ? ) a";
	public static final String AUTO_MATCH_ROWNUM_JFDS = "SELECT MIN (rownum) FROM ( SELECT rownum FROM ( SELECT row_number () OVER (ORDER BY id) rownum FROM BO_AKL_WXJF_S WHERE bindid = ? ) rowt WHERE rownum NOT IN ( SELECT ISNULL(HH,0) FROM BO_AKL_WXJF_S WHERE bindid = ? ) UNION SELECT COUNT (*) + 1 rownum FROM BO_AKL_WXJF_S WHERE bindid = ? ) a";

}
