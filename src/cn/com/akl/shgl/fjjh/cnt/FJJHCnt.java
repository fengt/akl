package cn.com.akl.shgl.fjjh.cnt;

public class FJJHCnt {
	
	public static final String dblx0 = "���ȷ���";
	public static final String dblx1 = "�������";
	public static final String direction0  = "FORWARD";//������ --> ����
	public static final String direction1  = "BACK";//������ <-- ����
	
	public static final String fjlx0 = "093689";//�ض��������ĸ���
	public static final String fjlx1 = "093690";//��������
	public static final String fjlx2 = "093691";//�²�Ʒ���죨���飩
	public static final String fjlx3 = "093692";//�ض��ͺŸ��죨���飩
	public static final String fjlx4 = "093693";//�������죨���飩
	
	/** ��ѯ�ֶ���Ϣ. */
	public static final String QUERY_FJJH_P_XMLB = "SELECT XMLB FROM BO_AKL_FJJH_P WHERE BINDID=?";//��������
	public static final String QUERY_FJJH_P_FJLX = "SELECT FJLX FROM BO_AKL_FJJH_P WHERE BINDID=?";//��������
	public static final String QUERY_FJJH_P_KFCKBM = "SELECT KFCKBM FROM BO_AKL_FJJH_P WHERE BINDID=?";//�ͷ��ֿ���루���ϣ�
	public static final String QUERY_FJJH_P_KFFZ = "SELECT KFFZ FROM BO_AKL_FJJH_P WHERE BINDID=?";//�ͷ�����
	public static final String QUERY_FJJH_P_FHCKBM = "SELECT FHCKBM FROM BO_AKL_FJJH_P WHERE BINDID=?";//�����ֿ�
	public static final String QUERY_FJJH_P_JCCKBM = "SELECT JCCKBM FROM BO_AKL_FJJH_P WHERE BINDID=?";//���ֿ�
	public static final String QUERY_WLBH = "SELECT WLBH FROM BO_AKL_JHWLXX WHERE BINDID=?";//�������ϱ��
	
	
	/** ��ѯ�ͷ��ֿ�*/
	public static final String QUERY_KFCK = "SELECT * FROM BO_AKL_KFCK WHERE KFCKBM=?";
	
	/** ��ѯ����ƻ����ӱ���Ϣ */
	public static final String QUERY_FJJH_P = "SELECT * FROM BO_AKL_FJJH_P WHERE BINDID=?";
	public static final String QUERY_FJJH_S = "SELECT * FROM BO_AKL_FJJH_S WHERE BINDID=?";
	
	public static final String QUERY_FJJH_S_DBDH = "SELECT DBDH FROM BO_AKL_FJJH_S WHERE BINDID=? GROUP BY DBDH";//�鿴�����ӱ������
	public static final String QUERY_DB_P_ISEND = "SELECT ISEND FROM BO_AKL_DB_P WHERE DBDH=?";//�鿴ÿ���������Ƿ������
	
	/** ��ѯ�������. */
	public static final String QUERY_JHWLBH = "SELECT WLBH FROM BO_AKL_JHWLXX WHERE BINDID=?";
	
	/**
	 * �����Ϣ
	 */
	public static final String QUERY_DB_S_SJCKSL = "SELECT SJCKSL FROM BO_AKL_DB_S WHERE BINDID=? AND WLBH=? AND CPSX=?";//ʵ�ʳ�������
	public static final String QUERY_DB_S_FJSL = "SELECT FJSL FROM BO_AKL_DB_S WHERE BINDID=? AND WLBH=? AND CPSX=?";//��������
	
	/**���������Ϣ.*/
	public static final String QUERY_JHWLXX = "SELECT * FROM BO_AKL_JHWLXX WHERE BINDID=?";
	
	/**��ѯ�����ĵ�������BINDID. */
	public static final String QUERY_DB_ALL_BINDID = "SELECT BINDID FROM BO_AKL_DB_P p WHERE p.XMLX =? AND p.DBLX = '"+dblx0+"' AND p.ISEND = 0 AND p.SHKFCKBM =? AND p.FHKFCKBM IN (( SELECT KFCKBM FROM BO_AKL_KFCK WHERE 1 =? AND ? LIKE '%' + KFCKBM + '%' ) UNION ( SELECT KFCKBM FROM BO_AKL_KFCK WHERE 0 =? AND DQBM =? )) AND EXISTS ( SELECT 1 FROM SYSFLOWSTEP WHERE ID=(SELECT TOP 1 WFSID FROM WF_TASK WHERE BIND_ID=p.BINDID ORDER BY ENDTIME DESC) AND STEPNO > 5 )";
	
	public static final String QUERY_DB_P_S = "SELECT p.DBDH,p.FHKFCKBM,p.FHKFCKMC,s.* FROM BO_AKL_DB_P p,BO_AKL_DB_S s WHERE p.BINDID=s.BINDID AND p.BINDID=? AND s.WLBH=? AND s.CPSX=?";
	
	/**��������ʱ����ѯ���������������趨ֵ�Ŀͷ�����. */
	public static final String QUERY_UNFINISHED = "SELECT KFCKBM, KFCKMC, SDZ, ISNULL(FJZL, 0) FJZL FROM "
			+ "( SELECT p.XMLB, s.KFCKBM, s.KFCKMC, s.SDZ FROM BO_AKL_FJYDJH_P p, BO_AKL_FJYDJH_S s WHERE p.BINDID = s.BINDID AND s.KFCKBM IN ( SELECT KFCKBM FROM BO_AKL_KFCK WHERE DQBM =? )) jh "
			+ "LEFT JOIN ( SELECT p.XMLX, p.FHKFCKBM, SUM (s.FJSL) FJZL FROM BO_AKL_DB_P p, BO_AKL_DB_S s WHERE p.BINDID = s.BINDID AND p.DBLX = '"+dblx0+"' AND p.ISEND = 0 AND p.SHKFCKBM =? AND s.FJSL > 0 AND p.FHKFCKBM IN ( SELECT KFCKBM FROM BO_AKL_KFCK WHERE DQBM =? ) AND EXISTS ( SELECT 1 FROM SYSFLOWSTEP WHERE ID = ( SELECT TOP 1 WFSID FROM WF_TASK WHERE BIND_ID = p.BINDID ORDER BY ENDTIME DESC ) AND STEPNO > 5 ) GROUP BY p.XMLX, p.FHKFCKBM ) db "
					+ "ON db.XMLX = jh.XMLB AND db.FHKFCKBM = jh.KFCKBM WHERE db.FJZL < jh.SDZ OR db.FJZL IS NULL";
	
	
	/** ���¸���ƻ��ĸ�������. */
	public static final String UPDATE_FJSL = "UPDATE BO_AKL_FJJH_S SET FJJHSL=? WHERE BINDID=? AND WLBH=? AND DBDH=? AND SX=?";
	
	public static final String UPDATE_FJSLToNull = "UPDATE BO_AKL_DB_S SET FJSL=0 WHERE WLBH=? AND CPSX=? AND BINDID=(SELECT BINDID FROM BO_AKL_DB_P WHERE DBDH=?)";
	
	public static final String DELETE_FJJH_S = "DELETE FROM BO_AKL_FJJH_S WHERE BINDID=?";
	
	/** ���µ������ĸ�������. */
	public static final String UPDATE_DB_S_FJSL = "UPDATE BO_AKL_DB_S SET FJSL=ISNULL(FJSL,0)+1 WHERE BINDID=? AND WLBH=? AND CPSX=?";
	
	
	
	
}
