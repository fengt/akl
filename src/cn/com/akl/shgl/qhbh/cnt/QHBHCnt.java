package cn.com.akl.shgl.qhbh.cnt;

public class QHBHCnt {

	public static final String subTitle = "������������";
	public static final String dblx = "��Ʒ���";//��������
	public static final String uuid = "42e5d26e5959793b50b23042933f2964";//��������UUID
	
	public static final String failMessage0 = "��治�����ʧ��";
	public static final String failMessage1 = "���滻�������ʧ��";
	public static final String failMessage2 = "�滻�����治�����ʧ��";
	
	public static final String is = "025000";//��
	public static final String no = "025001";//��
	
	public static final String zt0 = "070247";//�ڿ�
	
	public static final String qhzt2 = "076278";//������
	public static final String qhzt0 = "076279";//�����
	public static final String qhzt1 = "076280";//�����
	
	public static final String bhlx0 = "0";//Ƿ������������
	public static final String bhlx1 = "1";//��ȫ���
	
	public static final String phfs = "073265";//�����ʽ����ȫ���
	
	public static final String xmlb0 = "061271";//�޼�
	public static final String xmlb1 = "061272";//ħ��
	
	public static final String sx0 = "066215";//RMA��Ʒ
	public static final String sx1 = "066216";//DOA��Ʒ
	
	public static final String sx2 = "066208";//FGER
	public static final String sx3 = "066209";//FG
	
	/**
	 * �����ֶβ�ѯ(BO_AKL_QHBH_P)
	 */
	public static final String QUERY_P_QHBHDH = "SELECT QHBHDH FROM BO_AKL_QHBH_P WHERE BINDID=?";//��������
	public static final String QUERY_P_XMLB = "SELECT XMLB FROM BO_AKL_QHBH_P WHERE BINDID=?";//��Ŀ���
	public static final String QUERY_P_BHLX = "SELECT BHLX FROM BO_AKL_QHBH_P WHERE BINDID=?";//��������
	public static final String QUERY_P_FHCKBM = "SELECT FHCKBM FROM BO_AKL_QHBH_P WHERE BINDID=?";//�����ͷ�����
	public static final String QUERY_WLMC = "SELECT WLMC FROM BO_AKL_CPXX WHERE WLBH=?";//��������
	public static final String QUERY_PN8L = "SELECT LPN8 FROM BO_AKL_CPXX WHERE WLBH=?";//�ͺ�8L
	public static final String QUERY_PN9L = "SELECT LPN9 FROM BO_AKL_CPXX WHERE WLBH=?";//�ͺ�9L
	public static final String QUERY_PN9L_WLBH = "SELECT WLBH FROM BO_AKL_CPXX WHERE LPN8=?";//�ͺ�9L�����ϱ��
	
	public static final String QUERY_USERNAME = "SELECT USERNAME FROM ORGUSER WHERE USERID=?";//�û���
	public static final String QUERY_USERID = "SELECT USERID FROM ORGUSER WHERE USERID=?";//�û����˺�
	public static final String QUERY_KFCKMC = "SELECT KFCKMC FROM BO_AKL_KFCK WHERE KFCKBM=?";//�ֿ�����
	
	
	/**
	 * �ӱ���Ϣ��ѯ(BO_AKL_QHBH_S)
	 */
	public static final String QUERY_S = "SELECT * FROM BO_AKL_QHBH_S WHERE BINDID=? ORDER BY YXJ DESC,SQSJ,SL";//�����ӱ�����
	public static final String QUERY_S_count = "SELECT COUNT(1) FROM BO_AKL_QHBH_S WHERE BINDID=?";//ͳ�Ʋ����ӱ��¼
	public static final String QUERY_S_HZ = "SELECT XMLB,SQCPWLBH,SQCPPN,PCH,HWDM,PHSX,SUM(SQCPSL)SL,JFKFBM FROM BO_AKL_QHBH_S WHERE BINDID=? AND JFKFBM=? GROUP BY XMLB,SQCPWLBH,SQCPPN,PCH,HWDM,PHSX,JFKFBM";//ÿ���ͷ��Ĳ����ӱ��������
	
	public static final String QUERY_DB_DETAIL = "SELECT * FROM BO_AKL_QHBH_S WHERE BINDID=? AND JFKFBM=?";//���ͷ������Ƶ������ݣ���ϸ��
	public static final String QUERY_DB_GATHER = "SELECT SQCPWLBH,SQCPPN,SQCPZWMC,PHSX,JFKFBM,JFKFMC,SUM(SQCPSL)CKSL FROM BO_AKL_QHBH_S WHERE BINDID=? AND JFKFBM=? GROUP BY SQCPWLBH,SQCPPN,SQCPZWMC,PHSX,JFKFBM,JFKFMC";//���ͷ������Ƶ������ݣ����ܣ�
	public static final String QUERY_DB_GATHER_NEW = "SELECT SQCPWLBH, SQCPPN, SQCPZWMC, PHSX, JFKFBM, JFKFMC, cp.GG, cp.SJLH, cp.CPFL, SUM (ISNULL(kcsk.KCKYZ, 0)) KCKYZ, SUM (SQCPSL) CKSL FROM BO_AKL_QHBH_S bh LEFT JOIN ( SELECT kc.XMLB, kc.WLBH, kc.PCH, kc.SX, kc.CKDM, SUM (ISNULL(kc.KWSL, 0)) - SUM (ISNULL(sk.SDSL, 0)) KCKYZ FROM BO_AKL_SHKC_S kc LEFT JOIN ( SELECT XMLB, WLBH, PCH, SX, CKDM, SUM (SDSL) SDSL FROM BO_AKL_SH_KCSK GROUP BY XMLB, WLBH, PCH, SX, CKDM ) sk ON kc.XMLB = sk.XMLB AND kc.WLBH = sk.WLBH AND kc.PCH = sk.PCH AND kc.SX = sk.SX AND kc.CKDM = sk.CKDM AND kc.ZT = '070247' GROUP BY kc.XMLB, kc.WLBH, kc.PCH, kc.SX, kc.CKDM ) kcsk ON bh.XMLB = kcsk.XMLB AND bh.SQCPWLBH = kcsk.WLBH AND bh.PCH = kcsk.PCH AND bh.PHSX = kcsk.SX AND kcsk.CKDM =? LEFT JOIN BO_AKL_CPXX cp ON bh.SQCPWLBH = cp.WLBH WHERE bh.JFKFBM=? AND bh.BINDID =? GROUP BY SQCPWLBH, SQCPPN, SQCPZWMC, PHSX, JFKFBM, JFKFMC, cp.GG, cp.SJLH, cp.CPFL";//���ͷ������Ƶ������ݣ����ܣ�
	
	public static final String QUERY_KFFZR = "SELECT a.JFKFBM,b.KFCKMC,b.KFFZR FROM BO_AKL_QHBH_S a, BO_AKL_KFCK b WHERE a.JFKFBM=b.KFCKBM AND a.BINDID=? GROUP BY a.JFKFBM,b.KFCKMC,b.KFFZR";//�ͷ��ֿ⸺����
	
	public static final String QUERY_S_WLBH = "SELECT SQCPWLBH,SX,SUM(SQCPSL)SQZSL FROM BO_AKL_QHBH_S WHERE BINDID=? GROUP BY SQCPWLBH,SX";//������Ʒ����
	
	/**
	 * ����ѯ(BO_AKL_SHKC_S)
	 */
	public static final String QUERY_KCMX_SUM = "SELECT SUM(KWSL)KWSL FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=?";//��λ����
	
	public static final String QUERY_SK_SUM = "SELECT SUM(SDSL)SDSL FROM BO_AKL_SH_KCSK WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=?";//��������
	public static final String QUERY_SK_All_SUM = "SELECT SUM(SDSL)SDSL FROM BO_AKL_SH_KCSK WHERE XMLB=? AND WLBH=? AND SX=? AND CKDM=?";//��������
	
	public static final String isLockExsit = "SELECT COUNT(1)NUM FROM BO_AKL_SH_KCSK WHERE YDH=? AND XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=?";//�Ƿ��������
	
	public static final String QUERY_KCMX_PCHAndHWDM = "SELECT PCH,HWDM,KWSL FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND SX=? AND KWSL>0 AND CKDM=? AND ZT='"+zt0+"' ORDER BY PCH,KWSL DESC";//������Ŀ,���ϱ��,�����Զ�ƥ�������κͻ�λ
	
	public static final String QUERY_LimtInventory = "SELECT KCXX FROM BO_AKL_SH_ZXKCL WHERE XMLB=? AND CKBM=? AND WLBH=? AND SXID=?";//�ܲ��������
	
	public static final String QUERY_All_Shortage = "SELECT SUM(ISNULL(SL,0))QHZL FROM BO_AKL_QHBH_S WHERE BINDID=? AND PHFS='"+phfs+"' AND XMLB=? AND YCPWLBH=? AND SX=?";//��������ȱ����
	
	public static final String QUERY_Sum = "SELECT SUM(ISNULL(KWSL,0))KCZL FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND SX=? AND CKDM=? AND ZT='"+zt0+"'";//�������ܲ������
	
	/**
	 * ���²���
	 */
	public static final String UPDATE_S_PCAndHWDM = "UPDATE BO_AKL_QHBH_S SET SQCPWLBH=?,SQCPPN=?,SQCPZWMC=?,SQCPSL=?,PHSX=?,PCH=?,HWDM=?,ZT=? WHERE ID=?";//����(�����Ʒ���ϱ��/�����Ʒ�ͺ�/�����Ʒ��������/�����Ʒ����/����/��λ)
	
	public static final String UPDATE_S_PCAndHWDM_TH = "UPDATE BO_AKL_QHBH_S SET PCH=NULL,HWDM=NULL WHERE BINDID=?";//���κͻ�λ(�ڵ��˻�ʱ)
	
	public static final String UPDATE_LockNum = "UPDATE BO_AKL_SH_KCSK SET SDSL=ISNULL(SDSL,0)+? WHERE YDH=? AND XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=?";// ������������
	
	public static final String UPDATE_S_ZT = "UPDATE BO_AKL_QHBH_S SET ZT=? WHERE BINDID=?";//���²����ӱ�--״̬
	public static final String UPDATE_QHJL_ZT = "UPDATE BO_AKL_QHJL SET ZT=? WHERE XMLB=? AND WLBH=? AND SX=? AND JFKFBM=? AND SFJSTH=? AND ZT=? AND SXDH=? AND SXCPHH=? AND JFCPHH=?";//����ȱ����¼��--״̬
	public static final String UPDATE_QHJL_SCWCGPHYY = "UPDATE BO_AKL_QHJL SET SCWCGPHYY=? WHERE XMLB=? AND WLBH=? AND SX=? AND JFKFBM=? AND SFJSTH=? AND ZT=?";//����ȱ����¼��--�ϴ�δ�ɹ����ԭ��
	
	/**
	 * ɾ������δ�ɹ��ļ�¼
	 */
	public static final String DELETE_QHBH_S = "DELETE FROM BO_AKL_QHBH_S WHERE ID=?";
	
	
}
