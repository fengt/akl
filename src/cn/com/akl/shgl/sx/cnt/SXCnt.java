package cn.com.akl.shgl.sx.cnt;

/**
 * �������޳���
 * @author fengtao
 *
 */
public class SXCnt {

	public static final String is = "025000";//��
	public static final String no = "025001";//��
	public static final String empty = "";//��
	
	public static final String sx0 = "066317";//����Ʒ
	public static final String sx1 = "066354";//��Ʒ
	public static final String sx2 = "066326";//����Ʒ
	
	public static final String zt0 = "070247";//�ڿ�
	public static final String zt1 = "069239";//������(��ͷ)
	public static final String zt2 = "069243";//�Ѽ��(����)
	
	public static final String zblx0 = "063281";//��Ʊ//�ʱ�����
	public static final String zblx1 = "063282";//PID//�ʱ�����
	public static final String zblx2 = "063283";//�վ�//�ʱ�����
	
	public static final String djlx = "���޵�";//��������¼����������
	public static final String wlzt = "071254";//��ǩ��
	public static final String wlzt1 = "071252";//������
	
	public static final String sxfs = "1";//���޷�ʽ���ʼ����ޣ�
	
	public static final String ywlx0 = "083323";//����
	public static final String ywlx1 = "083324";//����
	
	public static final String stepNO = "1";//��һ�ڵ�
	public static final String stepNO2 = "0";//�����ڵ�
	
	public static final String clfs0 = "064286";//����
	public static final String clfs1 = "064220";//��������
	public static final String clfs2 = "064221";//��������
	public static final String clfs3 = "064222";//�����˻�
	public static final String clfs4 = "064287";//����ά��
	public static final String clfs5 = "064223";//����ά��
	public static final String clfs6 = "064737";//����
	public static final String clfs7 = "064738";//����
	public static final String clfs8 = "064290";//��ʵ�����
	
	public static final String tphScript = "TPH@replace(@date,-)@formatZero(3,@sequencefordateandkey(SX-TPH))";//���������ɹ���
	
	/**
	 * ����Ʒ
	 */
	public static final String QUERY_isDYP = "SELECT SFYDYP FROM BO_AKL_SX_P WHERE BINDID=?";//��ѯ'�Ƿ��д���Ʒ'
	public static final String UPDATE_DYP_SFYKKC = "UPDATE BO_AKL_DYPXX SET SFYKKC='"+is+"' WHERE BINDID=?";//���´���Ʒ'�Ƿ��ѿۿ��'
	public static final String QUERY_DYP_SFYX = "SELECT * FROM BO_AKL_DYPXX WHERE SFYX='"+no+"' AND BINDID=?";//��ѯ����Ʒ'�Ƿ���Ч'
	
	public static final String QUERY_DYP = "SELECT COUNT(1)n FROM BO_AKL_DYPXX WHERE BINDID=?";//��ѯ����Ʒ����
	public static final String QUERY_DYP_DE = "SELECT * FROM BO_AKL_DYPXX WHERE SFYX='"+is+"' AND SFYKKC='"+no+"' AND BINDID=?";//��ѯ����Ʒ������Ϣ
	public static final String QUERY_DYP_IN = "SELECT * FROM BO_AKL_DYPXX WHERE SFYX='"+no+"' AND SFYKKC='"+is+"' AND BINDID=?";//��ѯ����Ʒ������Ϣ
	
	public static final String UPDATE_KCHZ_DYP_DE = "UPDATE BO_AKL_SHKC_P SET CKSL=ISNULL(CKSL,0)+?,PCSL=ISNULL(PCSL,0)-? WHERE XMLB=? AND WLBH=? AND PCH=?";//���´���Ʒ�����ܣ���
	public static final String UPDATE_KCMX_DYP_DE = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)-? WHERE SX='"+sx0+"' AND ZT='"+zt0+"' AND XMLB=? AND WLBH=? AND PCH=? AND HWDM=?";//���´���Ʒ�����ϸ����
	public static final String UPDATE_KCHZ_DYP_IN = "UPDATE BO_AKL_SHKC_P SET RKSL=ISNULL(RKSL,0)+?,PCSL=ISNULL(PCSL,0)+? WHERE XMLB=? AND WLBH=? AND PCH=?";//���´���Ʒ�����ܣ���
	public static final String UPDATE_KCMX_DYP_IN = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)+? WHERE SX='"+sx0+"' AND ZT='"+zt0+"' AND WLBH=? AND HWDM=? AND XMLB=? AND PCH=?";//���´���Ʒ�����ϸ����
	
	
	/**
	 * ���޲�Ʒ
	 */
	public static final String QUERY_CKDM = "SELECT KFCKBM FROM BO_AKL_SH_BMCKGX a,"
			+ "(SELECT a.ID FROM ORGDEPARTMENT a LEFT JOIN ORGUSER b ON a.ID=b.DEPARTMENTID WHERE b.USERID=(SELECT CREATEUSER FROM BO_AKL_SX_P WHERE BINDID=?))b "
			+ "WHERE a.BMBH=b.ID";//��ѯ�ֿ����
	
	public static final String QUERY_HWXX = "SELECT a.QDM,a.DDM,a.KWDM,a.HWDM FROM BO_AKL_SH_WLKWGX a,"//��ѯ��λ��Ϣ
			+ "(SELECT MAX(ID)ID FROM BO_AKL_SH_WLKWGX WHERE XMLB=? AND WLBH=? AND CKDM=? AND SFYX='"+is+"' GROUP BY XMLB,WLBH,CKDM)c WHERE a.ID=c.ID";
	
	public static final String QUERY_CKMC = "SELECT KFCKMC FROM BO_AKL_KFCK WHERE KFCKBM=?";//��ѯ�ֿ�����
	public static final String QUERY_SXDH = "SELECT SXDH FROM BO_AKL_SX_P WHERE BINDID=?";//��ѯ���޵���
	public static final String QUERY_XMLB = "SELECT XMLB FROM BO_AKL_SX_P WHERE BINDID=?";//��ѯ��Ŀ���
	public static final String QUERY_SXFS = "SELECT SXFS FROM BO_AKL_SX_P WHERE BINDID=?";//��ѯ���޷�ʽ
	public static final String QUERY_YWLX = "SELECT YWLX FROM BO_AKL_SX_P WHERE BINDID=?";//��ѯҵ������
	public static final String QUERY_XMKF = "SELECT XMKF FROM BO_AKL_SX_P WHERE BINDID=?";//��ѯ��Ŀ�ⷿ
	public static final String QUERY_SRSX = "SELECT SRSX FROM BO_AKL_SH_YWSXGX WHERE XMLB=? AND CLFS=? AND YWLX=?";//��ѯ������������
	public static final String QUERY_HCSX = "SELECT HCSX FROM BO_AKL_SH_YWSXGX WHERE XMLB=? AND CLFS=? AND YWLX=?";//��ѯ���ϻ�������
	public static final String QUERY_ZBNX = "SELECT ZBNX FROM BO_AKL_CPXX WHERE XMLB=? AND WLBH=? AND LPN8=?";//��ѯ�ʱ�����
	public static final String QUERY_ZBJZRQ = "SELECT ZBJZRQ FROM BO_AKL_WXJF_S WHERE WLBH=? AND SN=? AND ISEND=1";//��ѯ�����ʱ���ֹ����
	
	public static final String QUERY_SX_P = "SELECT * FROM BO_AKL_SX_P WHERE BINDID=?";//��ѯ���޵�ͷ��Ϣ
	public static final String QUERY_SXHZ = "SELECT WLBH,WLMC,XH,SUM(SL)SL FROM BO_AKL_SX_S WHERE BINDID=? GROUP BY WLBH,WLMC,XH";//��ѯ���޲�Ʒ������Ϣ�����ܣ�
	public static final String QUERY_SXMX = "SELECT * FROM BO_AKL_SX_S WHERE BINDID=?";//��ѯ���޲�Ʒ������Ϣ����ϸ��
	public static final String QUERY_isSXExist = "SELECT COUNT(1)N FROM BO_AKL_SX_S WHERE BINDID=?";//��ѯ�Ƿ������޲�Ʒ
	
	public static final String QUERY_isSFSJ = "SELECT COUNT(1)n FROM BO_AKL_SX_S WHERE SFSJ='"+is+"' AND BINDID=?";//��ѯ���޲�Ʒ���Ƿ���������¼
	public static final String QUERY_isSFZCSJ = "SELECT COUNT(1)n FROM BO_AKL_SX_S WHERE SFZCSJ='"+is+"' AND BINDID=?";//��ѯ���޲�Ʒ���Ƿ����ٴ�������¼
	public static final String QUERY_isExistKCHZ = "SELECT COUNT(1) n FROM BO_AKL_SHKC_P WHERE XMLB=? AND WLBH=? AND PCH=?";//��ѯ�Ƿ��Ѵ��ڿ��
	public static final String QUERY_isExistKCMX = "SELECT COUNT(1) n FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND SX=? AND HWDM=? AND PCH=? AND ZT=?";//��ѯ�Ƿ��Ѵ��ڿ��
	public static final String QUERY_isExistGZMX = "SELECT COUNT(1) n FROM BO_AKL_SHKC_XLH_S WHERE XMLB=? AND WLBH=? AND SX=? AND HWDM=? AND PCH=? AND ZT=? AND GZTM=?";//��ѯ�Ƿ��Ѵ��ڹ�������
	
	/**
	 * ���ݼ�������
	 */
	public static final String UPDATE_KCHZ = "UPDATE BO_AKL_SHKC_P SET RKSL=ISNULL(RKSL,0)+?,PCSL=ISNULL(PCSL,0)+? WHERE XMLB=? AND WLBH=? AND XH=? AND PCH=?";//���¿����Ϣ
	public static final String UPDATE_KCMX_KWSL = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)+? WHERE XMLB=? AND WLBH=? AND HWDM=? AND PCH=? AND SX=? AND ZT='"+zt0+"'";//���¿�棨������
	public static final String UPDATE_KCMX_SX = "UPDATE BO_AKL_SHKC_S SET SX=? WHERE XMLB=? AND WLBH=? AND HWDM=? AND PCH=? AND SX='"+sx2+"' AND ZT='"+zt0+"'";//���¿�棨���ԣ�

	public static final String UPDATE_SX_P_SUM = "UPDATE BO_AKL_SX_P SET YSFYHJ=ISNULL(YSPJCB,0)+ISNULL(YSYJF,0)+ISNULL(YSWXF,0)+ISNULL(YSQTF,0)+ISNULL(YSYJ,0) WHERE BINDID=?";//�������޵�ͷ(Ԥ�շ��úϼ�)
	public static final String UPDATE_SX_P_YSYJ = "UPDATE BO_AKL_SX_P SET YSYJ=(SELECT ISNULL(SUM(ISNULL(YJ,0)),0)ZYJ FROM BO_AKL_DYPXX WHERE BINDID=BO_AKL_SX_P.BINDID AND SFSYJ='025000') WHERE BINDID=?";//�������޵�ͷ(Ԥ��Ѻ��)
	public static final String UPDATE_SX_P_ZT = "UPDATE BO_AKL_SX_P SET ZT='"+zt1+"' WHERE BINDID=?";//�������޵�ͷ��״̬��
	public static final String UPDATE_SX_S_ZT = "UPDATE BO_AKL_SX_S SET ZT='"+zt2+"' WHERE BINDID=?";//�������޵���״̬��
	public static final String UPDATE_SX_S_CLFSAndHH = "UPDATE BO_AKL_SX_S SET CLFS=?,SXCPHH=? WHERE ID=?";//�������޵�������ʽ���кţ�����������|����
	public static final String UPDATE_ZBJZRQ = "UPDATE BO_AKL_SX_S SET SFSCZB=?,ZBJZRQ=? WHERE ID=?";//�����ʱ���ֹ����
	public static final String UPDATE_TPH = "UPDATE BO_AKL_SX_S SET TPH=? WHERE ID=?";//����������
	
	public static final String UPDATE_SX_PCH = "UPDATE BO_AKL_SX_S SET PCH=? WHERE BINDID=?";//�������޵���(���κ�)
	public static final String UPDATE_SX_HHAndHWDMAndSX = "UPDATE BO_AKL_SX_S SET SXCPHH=?,HWDM=?,SX=? WHERE ID=?";//�������޵���(�кš���λ���롢����)
	
}
