package cn.com.akl.shgl.fjfj.cnt;

public class FJFJCnt {

	/**
	 * ������������״̬
	 */
	public static final String wlzt = "071254";//��ǩ��
	public static final String wlzt1 = "071252";//������
	public static final String zt1 = "069239";//������(��ͷ)
	public static final String zt2 = "069243";//�Ѽ��(����)
	
	public static final String djlx = "����";//�������ͣ���������
	
	/**
	 * ��¼��ʶ�����ڴ��������β����¼ʱ��������
	 */
	public static final String jlbz0 = "0";//���ͷ�--��⣩
	public static final String jlbz1 = "1";//�����--�ͷ���
	
	public static final String is = "025000";//��
	
	/**
	 * ��������
	 */
	public static final String sx0 = "066354";//��Ʒ
	public static final String sx1 = "066358";//��ȡƷ
	
	/**
	 * ��桢���к���ϸ״̬
	 */
	public static final String zt3 = "070245";//��;
	public static final String zt4 = "070247";//�ڿ�
	
	/**
	 * ���췵������״̬��5��6�ڵ�״̬���޸��´��룬����Ҫʱ��ӣ�
	 */
	public static final String djzt0 = "069237";//������
	public static final String djzt1 = "069255";//������
	public static final String djzt2 = "069256";//���ջ�
	public static final String djzt3 = "069259";//�ѷ���
//	public static final String djzt4 = "069257";//�Ѷ���
//	public static final String djzt5 = "069258";//������
	
	/**
	 * ��⴦�������������&&������ۣ�
	 */
	public static final String jcjg0 = "067319";//�й���
	public static final String jcjg1 = "067320";//�޹���
	public static final String jcjg2 = "067322";//�������
	public static final String jcjg3 = "088343";//�й�������
	public static final String jcjg4 = "088344";//�޹�������
	public static final String jcjg5 = "088345";//�޹����˻�
	
	/**
	 * ���޴���ʽ
	 */
	public static final String clfs0 = "064221";//���컻��
	public static final String clfs1 = "064222";//�����˻�
	
	/**
	 * ������Ϣ
	 */
	public static final String QUERY_XMLB = "SELECT SSXM FROM BO_AKL_FJFJ_P WHERE BINDID=?";//��Ŀ���
	public static final String QUERY_SRKF = "SELECT SRKF FROM BO_AKL_FJFJ_P WHERE BINDID=?";//����ⷿ
	public static final String QUERY_JCKF = "SELECT JCKF FROM BO_AKL_FJFJ_P WHERE BINDID=?";//�ĳ��ⷿ
	public static final String QUERY_SX_BINDID = "SELECT a.BINDID FROM BO_AKL_SX_P a,BO_AKL_FJFJ_P b WHERE a.SXDH=b.SXDH AND b.BINDID=?";//����BINDID
	public static final String QUERY_WLZT = "SELECT WLZT FROM BO_AKL_DFH_P a,BO_AKL_FJFJ_P b WHERE a.DH=b.HPJCDH AND b.BINDID=? AND a.JLBZ=? AND b.ZT=?";//����״̬
	
	public static final String QUERY_PCH = "SELECT PCH FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND HWDM=? AND SX=? AND ZT=?";//������κ�
	
	public static final String QUERY_WGZ = "SELECT COUNT(1) N FROM BO_AKL_FJFJ_S WHERE BINDID=? AND EJJL='"+jcjg1+"'";//�޹���
	public static final String QUERY_GCJC = "SELECT COUNT(1) N FROM BO_AKL_FJFJ_S WHERE BINDID=? AND EJJL='"+jcjg2+"'";//�������

	public static final String QUERY_HWXX = "SELECT CKDM,CKMC,QDM,DDM,KWDM FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND SX=? AND PCH=? AND HWDM=?";//��ѯ��λ��Ϣ
	public static final String QUERY_FJFJ_P = "SELECT * FROM BO_AKL_FJFJ_P WHERE BINDID=?";//��ѯ������Ϣ
	public static final String QUERY_FJFJ = "SELECT * FROM BO_AKL_FJFJ_S WHERE BINDID=?";//��ѯ�ӱ���Ϣ
	
	public static final String QUERY_isExistKCMX = "SELECT COUNT(1) n FROM BO_AKL_SHKC_S WHERE WLBH=? AND SX=? AND HWDM=? AND PCH=? AND ZT=?";//��ѯ�Ƿ��Ѵ��ڿ��
	
	/**
	 * ���쵥��
	 */
	public static final String UPDATE_FJFJ_P_ZT = "UPDATE BO_AKL_FJFJ_P SET ZT=? WHERE BINDID=?";//���¸�������״̬
	public static final String UPDATE_FJFJ_S_ZT = "UPDATE BO_AKL_FJFJ_S SET ZT=? WHERE BINDID=?";//���¸����ӱ�״̬
	public static final String UPDATE_FJFJ_S_HWDM2 = "UPDATE BO_AKL_FJFJ_S SET HWDM2=? WHERE BINDID=? AND CPLH=? AND KFGZDM=?";//���·��������ӱ�(����λ����)
	public static final String UPDATE_FJFJ_S_SFFH = "UPDATE BO_AKL_FJFJ_S SET SFFH='"+is+"' WHERE BINDID=? AND CPLH=? AND KFGZDM=? AND SX=?";//���·��������ӱ�(�Ƿ񷵻�)
	
	/**
	 * �����ϸ
	 */
	public static final String UPDATE_KCMX_DE = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)-? WHERE XMLB=? AND WLBH=? AND SX=? AND PCH=? AND HWDM=?";//�������ϸ
	public static final String UPDATE_KCMX_IN = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)+?,ZT='"+zt3+"' WHERE XMLB=? AND WLBH=? AND SX=? AND PCH=? AND HWDM=?";//�ӿ����ϸ
	public static final String UPDATE_KCMX_ZT = "UPDATE BO_AKL_SHKC_S SET ZT='"+zt4+"' WHERE XMLB=? AND WLBH=? AND SX=? AND PCH=? AND HWDM=?";//���¿����ϸ״̬
	
	public static final String UPDATE_KCMX = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)+? WHERE WLBH=? AND SX=? AND HWDM=? AND PCH=? AND ZT=?";//���¿����Ϣ
	public static final String DELETE_KCMX = "DELETE FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND SX=? AND HWDM=? AND PCH=? AND ZT=?";//ɾ����;����
	
	/**
	 * ������ϸ
	 */
	public static final String UPDATE_GZMX_ZT = "UPDATE BO_AKL_SHKC_XLH_S SET ZT=? WHERE XMLB=? AND WLBH=? AND PCH=? AND GZTM=?";//���¹�����ϸ״̬
	public static final String UPDATE_GZMX = "UPDATE BO_AKL_SHKC_XLH_S SET CKDM=?,CKMC=?,QDM=?,DDM=?,KWDM=?,HWDM=?,ZT='"+zt3+"' WHERE XMLB=? AND WLBH=? AND PCH=? AND GZTM=?";//���¹�����ϸ�ֿ��״̬
	
	/**
	 * ���޵�
	 */
	public static final String UPDATE_SX_CLFS = "UPDATE BO_AKL_SX_S SET CLFS=?,SX=? WHERE BINDID=? AND WLBH=? AND GZTM=? AND PCH=? AND HWDM=? AND SX=?";//���������ӱ�(����ʽ)
	
	/**
	 * �����ϸ�����������ӱ����кţ����Ը���
	 */
	public static final String UPDATE_KCMX_SX = "UPDATE BO_AKL_SHKC_S SET SX=? WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=?";
	public static final String UPDATE_FJFJ_SX = "UPDATE BO_AKL_FJFJ_S SET SX=? WHERE ID=?";
	public static final String UPDATE_GZMX_SX = "UPDATE BO_AKL_SHKC_XLH_S SET SX=? WHERE XMLB=? AND WLBH=? AND PCH=? AND GZTM=? AND SX=?";
	
}
