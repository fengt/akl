package cn.com.akl.shgl.sxtz.biz;

import cn.com.akl.shgl.sx.cnt.SXCnt;

/**
 * Created by huangming on 2015/5/8.
 */
public class SXTZConstant {

    /**
     * ԭ��������.
     */
    public static final String NEW_TABLE_MAIN = "BO_AKL_SH_SXTZ_P";
    /**
     * �޸ĵ�����.
     */
    public static final String NEW_TABLE_SUB = "BO_AKL_SH_SXTZ_S";
    /**
     * ԭ�����ӱ�.
     */
    public static final String OLD_TABLE_MAIN = "BO_AKL_SX_P";
    /**
     * �޸ĵ�������.
     */
    public static final String OLD_TABLE_SUB = "BO_AKL_SX_S";
    /**
     * �����ֶ�.
     */
    public static final String FROM_DH = "SXDH";

    /**
     * ץȡ���޵���.
     */
    public static final String QUERY_FORM_SXDH = "SELECT " + FROM_DH + " FROM " + NEW_TABLE_MAIN + " WHERE BINDID=?";
    /**
     * ��ȡԭ���޵�BINDID.
     */
    public static final String QUERY_FORM_PARENTBINDID = "SELECT PARENTBINDID FROM " + NEW_TABLE_MAIN + " WHERE BINDID=?";
    /*
    * ��ȡԭ���޵�BINDID.
    */
    public static final String QUERY_OLD_FORM_BINDID = "SELECT BINDID FROM " + OLD_TABLE_MAIN + " WHERE " + FROM_DH + "=?";
    /**
     * ��ȡ������ʱ��.
     */
    public static final String QUERY_FORM_LASTTIME = "SELECT LASTTIME FROM " + NEW_TABLE_MAIN + " WHERE BINDID=?";
    /**
     * ��ȡ���޵�������޸�ʱ��.
     */
    public static final String QUEYR_OLD_FORM_LASTTIME = "SELECT UPDATEDATE FROM " + OLD_TABLE_MAIN + " WHERE SXDH=?";
    /**
     * ��ѯԭ���޵��ӱ��¼.
     */
    public static final String QUERY_OLD_FORM_BODY = "SELECT * FROM " + OLD_TABLE_SUB + " WHERE BINDID=?";
    /**
     * ��ѯ�����޵��ӱ�����.
     */
    public static final String QUERY_NEW_FORM_BODY = "SELECT * FROM " + NEW_TABLE_SUB + " WHERE BINDID=?";
    /**
     * ��ѯԭ�������ʱ��.
     */
    public static final String QUERY_OLD_FORM_LASTTIME = "SELECT UPDATEDATE FROM " + SXTZConstant.OLD_TABLE_SUB + " WHERE ID=?";

    public static final String QUERY_YSX_S = "SELECT * FROM BO_AKL_SH_YSX_S WHERE BINDID=?";//��ѯԭ���޲�Ʒ������Ϣ
    
    public static final String QUERY_XMLB = "SELECT XMLB FROM BO_AKL_SH_SXTZ_P WHERE BINDID=?";//��ѯ��Ŀ���
	public static final String QUERY_YSXFS = "SELECT YSXFS FROM BO_AKL_SH_SXTZ_P WHERE BINDID=?";//��ѯԭ���޷�ʽ
	public static final String QUERY_SXFS = "SELECT SXFS FROM BO_AKL_SH_SXTZ_P WHERE BINDID=?";//��ѯ���޷�ʽ
	public static final String QUERY_YYWLX = "SELECT YYWLX FROM BO_AKL_SH_SXTZ_P WHERE BINDID=?";//��ѯԭҵ������
	public static final String QUERY_YWLX = "SELECT YWLX FROM BO_AKL_SH_SXTZ_P WHERE BINDID=?";//��ѯҵ������
	public static final String QUERY_XMKF = "SELECT XMKF FROM BO_AKL_SH_SXTZ_P WHERE BINDID=?";//��ѯ��Ŀ�ⷿ
    
    /**
     * ������Ϣ��ѯ
     */
	public static final String QUERY_JF_SFYJ = "SELECT SFYJ FROM BO_AKL_WXJF_P WHERE JFDH=?";//��ѯ�����Ƿ��ʼ�
	public static final String QUERY_JF_BINDID = "SELECT BINDID FROM BO_AKL_WXJF_P WHERE JFDH=?";//��ѯ����BINDID
	
	public static final String QUERY_isExsitJFD = "SELECT JFDH FROM BO_AKL_WXJF_P WHERE ISEND=? AND SXDH=?";//��ѯ�Ƿ���δ��ɵĽ�����
	
	public static final String QUERY_JF_S = "SELECT s.* FROM BO_AKL_WXJF_P p,BO_AKL_WXJF_S s WHERE p.BINDID=s.BINDID AND p.JFDH=?";//��ѯ�������ӱ�
	
	public static final String QUERY_JF_S_PJ = "SELECT s.* FROM BO_AKL_WXJF_P p,BO_AKL_PJCP s WHERE p.BINDID=s.BINDID AND p.JFDH=?";//��ѯ���������
	
	/**
	 * ����ѯ������
	 */
    public static final String QUERY_SXTZ_S_HZ = "SELECT WLBH,WLMC,XH,PCH,SUM(SL)SL FROM BO_AKL_SH_SXTZ_S WHERE BINDID=? GROUP BY WLBH,WLMC,XH,PCH";//��ѯ���޲�Ʒ������Ϣ�����ܣ�
    
    public static final String UPDATE_KCHZ = "UPDATE BO_AKL_SHKC_P SET RKSL=ISNULL(RKSL,0)+?,PCSL=ISNULL(PCSL,0)+? WHERE XMLB=? AND WLBH=? AND PCH=?";//���¿����Ϣ
    
    public static final String UPDATE_KCMX_KWSL = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)+? WHERE XMLB=? AND WLBH=? AND HWDM=? AND PCH=? AND SX=? AND ZT='070247'";//���¿�棨������
    
    public static final String DELETE_XLH = "DELETE FROM BO_AKL_SHKC_XLH_S WHERE GZTM=?";//ɾ�����к�
    
    /**
     * ���������ֶθ���
     */
    public static final String UPDATE_SXTZ_P_ZT = "UPDATE BO_AKL_SH_SXTZ_P SET ZT='"+SXCnt.zt1+"' WHERE BINDID=?";//�������޵�ͷ��״̬��
	public static final String UPDATE_SXTZ_S_ZT = "UPDATE BO_AKL_SH_SXTZ_S SET ZT='"+SXCnt.zt2+"' WHERE BINDID=?";//�������޵���״̬��
    public static final String UPDATE_SXTZ_HHAndHWDMAndSX = "UPDATE BO_AKL_SH_SXTZ_S SET SXCPHH=?,HWDM=?,SX=? WHERE ID=?";//���µ������޵���(�кš���λ���롢����)
    
    /**
     * ɾ����������¼�����޺ͽ�����
     */
    public static final String DELETE_SX_DFH_P = "DELETE FROM BO_AKL_DFH_P WHERE BINDID=(SELECT BINDID FROM BO_AKL_SX_P WHERE SXDH=?)";//ɾ������������
    public static final String DELETE_SX_DFH_S = "DELETE FROM BO_AKL_DFH_S WHERE BINDID=(SELECT BINDID FROM BO_AKL_SX_P WHERE SXDH=?)";//ɾ���������ӱ�
    
    public static final String DELETE_JF_DFH_P = "DELETE FROM BO_AKL_DFH_P WHERE BINDID=(SELECT BINDID FROM BO_AKL_WXJF_P WHERE JFDH=?)";//ɾ������������
    public static final String DELETE_JF_DFH_S = "DELETE FROM BO_AKL_DFH_S WHERE BINDID=(SELECT BINDID FROM BO_AKL_WXJF_P WHERE JFDH=?)";//ɾ���������ӱ�
    
}
