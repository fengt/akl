package cn.com.akl.shgl.sx.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.sx.biz.DateBiz;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DateUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1AfterSave extends WorkFlowStepRTClassA{

	Connection conn = null;
	private UserContext uc;
	DateBiz dateBiz = new DateBiz();
	public StepNo1AfterSave(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("������水ť�������ʱ���ֹ����!");
	}
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		
		if(tablename.equals("BO_AKL_SX_P")){
			try {
				conn = DAOUtil.openConnectionTransaction();
				
				/**1��������úϼ�*/
				DAOUtil.executeUpdate(conn, SXCnt.UPDATE_SX_P_YSYJ, bindid);//Ԥ��Ѻ��
				DAOUtil.executeUpdate(conn, SXCnt.UPDATE_SX_P_SUM, bindid);//�����ܼ�
				
				/**2�������ʱ���ֹ����*/
				setDeadline(conn, bindid);
				
				conn.commit();
				return true;
			} catch (RuntimeException e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage(), true);
				return false;
			} catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(uc.getUID(), "��̨�����쳣���������̨", true);
				return false;
			} finally {
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}
	
	/**
	 * �ʱ���ֹ����
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setDeadline(Connection conn, int bindid) throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMLB, bindid));//��Ŀ���
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_SXMX,
				new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));//���ϱ��
				String sn = StrUtil.returnStr(rs.getString("SN"));//SN
				String pn = StrUtil.returnStr(rs.getString("XH"));//PN
				String zblx = StrUtil.returnStr(rs.getString("ZBLX"));//�ʱ�����
				String gmsj = StrUtil.returnStr(rs.getString("GMRQ"));//��������
				
				String deadline = "";
				String yesOrNo = "";
				String zbjzrq = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_ZBJZRQ, wlbh, pn));//�����е��ʱ���ֹ����
				if("".equals(zbjzrq)){
					yesOrNo = SXCnt.is;
				}else{
					yesOrNo = SXCnt.no;
					deadline = zbjzrq;
				}
				int warranty = DAOUtil.getIntOrNull(conn, SXCnt.QUERY_ZBNX, xmlb, wlbh, pn) == null ? -1 : DAOUtil.getIntOrNull(conn, SXCnt.QUERY_ZBNX, xmlb, wlbh, pn);
				if(yesOrNo.equals(SXCnt.is) && zblx.equals(SXCnt.zblx1)){//�״��ʱ�&&�ʱ�����PID
					if(warranty == -1) throw new RuntimeException("��PN��"+pn+"���޷���ȡ�ʱ����ޣ�����ά����");
					int[] Yweeks = dateBiz.convertStr(sn);
					deadline = dateBiz.getDeadline(warranty, Yweeks);
				}else if(SXCnt.zblx0.equals(zblx) || SXCnt.zblx2.equals(zblx)){
					if(warranty == -1) throw new RuntimeException("��PN��"+pn+"���޷���ȡ�ʱ����ޣ�����ά����");
					deadline = DateUtil.dateToStrBy(dateAddYear(gmsj, warranty));
				}
				DAOUtil.executeUpdate(conn, SXCnt.UPDATE_ZBJZRQ, yesOrNo, deadline, rs.getInt("ID"));
				return true;
			}
		}, bindid);
	}
	
	/**
	 * ���������
	 * @param str
	 * @param years
	 * @return
	 */
	public Date dateAddYear(String str, int years){
		Calendar c = null;
		try {
			Date date = DateUtil.strToShortDate(str);
			c = Calendar.getInstance();
			c.setTime(date);
			c.add(c.YEAR, years);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return c.getTime();
	}
	
}
