package cn.com.akl.shgl.qhsq.cnt;

public class QHSQCnt {

	/**
	 * ȱ����¼״̬
	 */
	public static final String zt0 = "076277";//������
	public static final String zt1 = "076278";//������
	public static final String zt2 = "076349";//������
	public static final String zt3 = "076356";//ȱ����
	
	/**
	 * �����ʽ
	 */
	public static final String bhlx0 = "073263";//������������
	public static final String bhlx1 = "073264";//�������벹��
	public static final String bhlx3 = "073265";//��ȫ��油��
	
	/**
	 * ����
	 */
	public static final String low = "0";//��
	public static final String high = "1";//��
	public static final String is = "025000";//��
	public static final String no = "025001";//��
	
	
	public static final String QUERY_QHSQ_P_BHLX = "SELECT BHLX FROM BO_AKL_QHSQ_P WHERE BINDID=?";//��������
	public static final String QUERY_QHSQ_P_KFZX = "SELECT KFZX FROM BO_AKL_QHSQ_P WHERE BINDID=?";//�ͷ�����
	public static final String QUERY_KFCKMC = "SELECT KFCKMC FROM BO_AKL_KFCK WHERE KFCKBM=?";//�ͷ�����
	
	
	public static final String QUERY_HIGH = "SELECT 1 FROM BO_AKL_QHSQ_S WHERE BINDID=? AND YXJ='"+high+"'";//���ȼ�����
	public static final String QUERY_SFZCP = "SELECT 1 FROM BO_AKL_TSSQ_S WHERE BINDID=? AND SFZCP='"+is+"'";//�Ƿ�����Ʒ����
	
	public static final String QUERY_QHSQ_S = "SELECT * FROM BO_AKL_QHSQ_S WHERE BINDID=?";//��ѯȱ�������ӱ�
	public static final String QUERY_TSSQ_S = "SELECT * FROM BO_AKL_TSSQ_S WHERE BINDID=?";//��ѯ���������ӱ�
	
	public static final String UPDATE_QHJL_ZT = "UPDATE BO_AKL_QHJL SET SQSJ=?,SQLY=?,ZT=?,SFJSTH=?,YXJ=? WHERE XMLB=? AND SXDH=? AND WLBH=? AND SX=? AND SXCPHH=? AND JFCPHH=?";//����ȱ����¼��״̬
	public static final String UPDATE_QHJL_ZT2 = "UPDATE BO_AKL_QHJL SET ZT=? WHERE BINDID=?";//����(��������)ȱ����¼״̬
	public static final String DELETE_QHJL = "DELETE FROM BO_AKL_QHJL WHERE BINDID=?";//ɾ��(��������)��ȱ����¼
	
	public static final String UPDATE_QHSQ_P_ZT = "UPDATE BO_AKL_QHSQ_P SET ZT=? WHERE BINDID=?";//����ȱ����¼����״̬
	public static final String UPDATE_QHSQ_S_ZT = "UPDATE BO_AKL_QHSQ_S SET ZT=? WHERE BINDID=?";//����ȱ����¼�ӱ�״̬
	public static final String UPDATE_TSSQ_S_ZT = "UPDATE BO_AKL_TSSQ_S SET ZT=? WHERE BINDID=?";//�������������ӱ�״̬
}
