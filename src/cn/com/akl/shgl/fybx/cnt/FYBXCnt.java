package cn.com.akl.shgl.fybx.cnt;

public class FYBXCnt {

	public static final String zt0 = " ������";
	public static final String zt1 = " �ѱ���";

	public static final String djlx0 = "0";//��������
	public static final String djlx1 = "1";//�ǵ�������

	/** ���ñ���״̬,δ����. */
	public static final String KFFYBXZT_WBX="0";
	/** ���ñ���״̬,�ѱ���. */
	public static final String KFFYBXZT_YBX="1";

	/**
	 * ��������
	 */

	/** ��������. */
	public static final String QUERY_KF_P_DJLX = "SELECT DJLX FROM BO_AKL_FWZXBX_P WHERE BINDID=?";
	/** ��ѯ������ϸ�ӱ�. */
	public static final String QUERY_KF_S = "SELECT * FROM BO_AKL_KDFY_S WHERE BINDID=?";
    /** ��ѯ��Ʊ������Ϣ. */
    public static final String QUERY_KF_FPHZ = "SELECT BXXM, PZLX, XMLB, MIN (FYFSSJ) FYFSSJ, MAX (FYJSSJ) FYJSSJ, SUM (ZS) ZS, SUM (ZE) ZE FROM BO_AKL_FPMX_S WHERE BINDID = ? GROUP BY BXXM, XMLB, PZLX";
    /** ��ͷ״̬����. */
    public static final String UPDATE_KF_P_ZT = "UPDATE BO_AKL_FWZXBX_P SET ZT=? WHERE BINDID=?";
    /** ���·�Ʊ��������. */
    public static final String UPDATE_KF_FPHZ = "UPDATE BO_AKL_FWZXBX_FPHZ_S SET ZS=?, ZE=?, FYFSSJ=?, FYJSSJ=? WHERE BXXM=? AND PZLX=? AND BINDID=? AND XMLB=?";

	/**
	 * �ܲ�
	 */
	/** ��������. */
	public static final String QUERY_ZB_P_DJLX = "SELECT DJLX FROM BO_AKL_ZBFYBXSQ_P WHERE BINDID=?";
	/** ��ѯ������ϸ�ӱ�. */
	public static final String QUERY_ZB_S = "SELECT * FROM BO_AKL_ZBFYBXFY_S WHERE BINDID=?";
	/** ��ͷ״̬����. */
	public static final String UPDATE_ZB_P_ZT = "UPDATE BO_AKL_ZBFYBXSQ_P SET ZT=? WHERE BINDID=?";
	/** ��ѯ�ܲ�����. */
	public static final String QUERY_ZBFY_S = "SELECT * FROM BO_AKL_ZBFYBXFY_S WHERE BINDID=?";


	/**
	 * ��ص��ݣ����޵�����������״̬����
	 */
	/** �������޷���״̬. */
	public static final String UPDATE_SX_FYZT = "UPDATE BO_AKL_SX_P SET FYZT=? WHERE SXDH=?";
	/** ���½�������״̬. */
	public static final String UPDATE_JF_FYZT = "UPDATE BO_AKL_WXJF_P SET FYZT=? WHERE JFDH=?";

}
