package cn.com.akl.shgl.qhbh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.shgl.qhsq.cnt.QHSQCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;

public class StepNo1AfterSave extends WorkFlowStepRTClassA {

	private static final String is = "025000";
	private static final String no = "025001";

	private static final String zt0 = "070247";//�ڿ⣨���״̬��
	private static final String zt1 = "076278";//�����루����״̬��
	
	private static final String bhlx0 = "0";//Ƿ������������
	private static final String bhlx1 = "1";//��ȫ���
	
	private static final String QUERY_SFZDPH = "SELECT SFZDPH FROM BO_AKL_QHBH_P WHERE BINDID=?";
	private static final String QUERY_XMLB = "SELECT XMLB FROM BO_AKL_QHBH_P WHERE BINDID=?";
	private static final String QUERY_FHCKBM = "SELECT FHCKBM FROM BO_AKL_QHBH_P WHERE BINDID=?";
	private static final String QUERY_BHLX = "SELECT BHLX FROM BO_AKL_QHBH_P WHERE BINDID=?";
	
	private static final String QUERY_QHJL = "SELECT a.*, ( SELECT ISNULL(SUM(KWSL), 0) FROM BO_AKL_SHKC_S b WHERE a.XMLB = b.XMLB AND a.WLBH = b.WLBH AND a.SX = b.SX AND b.CKDM = ? AND b.ZT = '"+zt0+"' )-(SELECT ISNULL(SUM(SDSL), 0) FROM BO_AKL_SH_KCSK c WHERE a.XMLB = c.XMLB AND a.WLBH = c.WLBH AND a.SX = c.SX AND c.CKDM = ?) BDKCSL, sx.SXWLMC, sx.SXPN "
			+ "FROM BO_AKL_QHJL a LEFT JOIN ( SELECT p.SXDH AS SXDHM, s.SXCPHH, s.WLMC AS SXWLMC, s.XH AS SXPN FROM BO_AKL_SX_P p, BO_AKL_SX_S s WHERE p.BINDID = s.BINDID ) sx ON sx.SXDHM = a.SXDH AND sx.SXCPHH = a.SXCPHH "
			+ "WHERE a.XMLB = ? AND ((a.QHFS<>'"+QHSQCnt.bhlx3+"' AND 0=?) OR (a.QHFS='"+QHSQCnt.bhlx3+"' AND 1=?)) AND a.ZT = '"+zt1+"' ORDER BY YXJ DESC, SQSJ DESC";
	
	private Connection conn = null;
	public StepNo1AfterSave() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1AfterSave(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("�Զ�����Ƿ����¼��");
		
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		
		if("BO_AKL_QHBH_P".equals(tablename)){
			try{
				
				conn = DBSql.open();
				String sfzdph = DAOUtil.getString(conn, QUERY_SFZDPH, bindid);
				String xmlb = DAOUtil.getString(conn, QUERY_XMLB, bindid);
				String bhlx = DAOUtil.getString(conn, QUERY_BHLX, bindid);
				String flag = bhlx0;
				if(bhlx.equals(bhlx1)){
					flag = bhlx1;
				}
				
				if(sfzdph.equals(no)){
					return true;
				}
				//ɾ����������
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_QHBH_S", bindid);
				service(conn, bindid, xmlb, flag);
				
			}catch (RuntimeException e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), e.getMessage(), true);
				return false;
			} catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��̨�����쳣���������̨", true);
				return false;
			} finally{
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}
	
	public void service(Connection conn, final int bindid, String xmlb, String flag) throws SQLException{
		final String fhckbm = DAOUtil.getString(conn, QUERY_FHCKBM, bindid);
		DAOUtil.executeQueryForParser(conn, QUERY_QHJL, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String jfkfbm = StrUtil.returnStr(rs.getString("JFKFBM"));//�ջ��ⷿ����
				if(fhckbm.equals(jfkfbm)) return true;//�����Լ����Լ�����
				insertHandle(conn, rs, bindid);
				return true;
			}
		}, fhckbm, fhckbm, xmlb, flag, flag);
	}
	
	public void insertHandle(Connection conn, ResultSet rs, int bindid) throws SQLException{
		Hashtable<String, String> body = new Hashtable<String, String>();
		String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//���޵���
		String sqsj = StrUtil.returnStr(rs.getString("SQSJ"));//����ʱ��
		String sqly = StrUtil.returnStr(rs.getString("SQLY"));//��������
		String cljg = StrUtil.returnStr(rs.getString("CLJG"));//������
		String sl = StrUtil.returnStr(rs.getString("SL"));//����
		String sx = StrUtil.returnStr(rs.getString("SX"));//����
		String zt = StrUtil.returnStr(rs.getString("ZT"));//״̬
		String xmlb = StrUtil.returnStr(rs.getString("XMLB"));//��Ŀ���
		String wlbh = StrUtil.returnStr(rs.getString("WLBH"));//���ϱ��
		String wlmc = StrUtil.returnStr(rs.getString("WLMC"));//��������
		String jfkfbm = StrUtil.returnStr(rs.getString("JFKFBM"));//�����ⷿ����
		String jfkfmc = StrUtil.returnStr(rs.getString("JFKFMC"));//�����ⷿ����
		String sxcphh = StrUtil.returnStr(rs.getString("SXCPHH"));//���޲�Ʒ�к�
		String pn = StrUtil.returnStr(rs.getString("PN"));//PN
		String yxj = StrUtil.returnStr(rs.getString("YXJ"));//���ȼ�
		String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));//�Ƿ�����滻
		String qhfs = StrUtil.returnStr(rs.getString("QHFS"));//ȱ����ʽ
		String jfcphh = StrUtil.returnStr(rs.getString("JFCPHH"));//������Ʒ�к�
		
		String sxwlmc = StrUtil.returnStr(rs.getString("SXWLMC"));//���޲�Ʒ����
		String sxpn = StrUtil.returnStr(rs.getString("SXPN"));//����PN
		String bdkcsl = StrUtil.returnStr(rs.getString("BDKCSL"));//���ؿ�����ֵ
		String scwcgphyy = StrUtil.returnStr(rs.getString("SCWCGPHYY"));//�ϴ�δ�ɹ����ԭ��

//		String hh = RuleAPI.getInstance().executeRuleScript("@sequence:(#BO_AKL_QHBH_S)");//�к�
//		body.put("HH", hh);//�к�
		body.put("SXDH", sxdh);//���޵���
		body.put("SQSJ", sqsj);//����ʱ��
		body.put("SQLY", sqly);//��������
		body.put("CLJG", cljg);//������
		body.put("SL", sl);//�����Ʒ����
		body.put("SX", sx);//����
		body.put("ZT", zt);//״̬
		body.put("XMLB", xmlb);//��Ŀ���
		body.put("YCPWLBH", wlbh);//�����Ʒ���ϱ��
		body.put("YCPZWMC", wlmc);//�����Ʒ��������
		body.put("JFKFBM", jfkfbm);//�ջ��ⷿ����
		body.put("JFKFMC", jfkfmc);//�ջ��ⷿ����
		body.put("SXCPHH", sxcphh);//���޲�Ʒ�к�
		body.put("JFCPHH", jfcphh);//������Ʒ�к�
		body.put("YCPPN", pn);//�����ƷPN
		body.put("YXJ", yxj);//���ȼ�
		body.put("SFJSTH", sfjsth);//�Ƿ�����滻
		body.put("PHFS", qhfs);//�����ʽ
		
		body.put("SQCPWLBH", wlbh);//�����Ʒ���ϱ��
		body.put("SQCPSL", sl);//�����Ʒ����
		body.put("SQCPZWMC", wlmc);//�����Ʒ��������

		body.put("SXCPMC", sxwlmc);//���޲�Ʒ����
		body.put("SXCPPN", sxpn);//����PN
		body.put("BDKCKYZ", bdkcsl);//�����Ʒ���ϱ��
		body.put("SCWCGPHYY", scwcgphyy);//�ϴ�δ�ɹ����ԭ��
		
		
		//��������
		try {
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QHBH_S", body, bindid, this.getUserContext().getUID());
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}
	}
}
