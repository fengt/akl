package cn.com.akl.cggl.cgdd.rtclass;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import cn.com.akl.cggl.cgdd.constant.CgddConstant;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Validate extends WorkFlowStepRTClassA{

	private UserContext uc;
	public StepNo1Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("zhangran");
		setDescription("V1.0");
		setDescription("�����뵥������Ψһ�Ҳ�Ϊ��!");
	}
	
	private Connection conn = null;
	private String boTableName = CgddConstant.tableName1;
	
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable head = BOInstanceAPI.getInstance().getBOData(CgddConstant.tableName0, bindid);
		Vector vector = BOInstanceAPI.getInstance().getBODatas(boTableName, bindid);
		
		if(vector == null){
			MessageQueue.getInstance().putMessage(uc.getUID(), "���ĵ�����ϢΪ�գ������룡");
			return false;
		}else{
			return checkField(vector,bindid,head);
		}
	}
	
	/**
	 * �жϲ����ֶ��Ƿ�Ψһ��Ϊ��
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public boolean checkField(Vector vector, int bindid, Hashtable head){
		
		/**1���жϵ��붩�������Ψһ**/
		String sql2 = "select COUNT(DISTINCT(ddid)) cnt from " + boTableName + " where bindid = " + bindid ;
		int cnt = DBSql.getInt(sql2, "cnt");
		if(cnt > 1){
			MessageQueue.getInstance().putMessage(uc.getUID(), "���롾������š���Ψһ��");
			return false;
		}
		
		/**
		 * 2���жϲɹ�������Ҫ�󵽻����ڲ���Ϊ�գ������ͺ��Ƿ����
		 * 3�����ϲɹ�/BG�ɹ�����ҪУ���Ƿ��ѵ���ù�Ӧ�̼۸�
		 */
		String cgdb = head.get("DBID").toString();//�ɹ�����
		String gysbh = head.get("GYSID").toString();//��Ӧ�̱��
		Date cgrq = Date.valueOf(head.get("CGRQ").toString());//�ɹ�����
		double zero = 0.000d;
		for (int i = 0; i < vector.size(); i++) {
			Hashtable rec = (Hashtable)vector.get(i);
			String xh = rec.get("XH").toString();//�ͺ�
			String wlbh = rec.get("WLBH").toString();//���ϱ��
			double cgdj = Double.parseDouble(rec.get("CGDJ").toString());//�ɹ����� 
			double sl = Double.parseDouble(rec.get("SL").toString());//˰��
			
			//--2
			String sql3 = "select count(*) n from " + CgddConstant.tableName3 + " where xh = '"+xh+"'";
			int n = DBSql.getInt(sql3, "n");
			if(n <= 0){
				MessageQueue.getInstance().putMessage(uc.getUID(), "�����������ͺ�Ϊ��"+xh+"�������ϲ�������������Ϣ�У���˲飡");
				return false;
			}else{//�ɹ�������Ҫ�󵽻���У��
				int cgsl = Integer.parseInt(rec.get("CGSL").toString());
				String yqdhrq = rec.get("YQDHRQ").toString();
				if(cgsl == 0 || cgsl < 0){
					MessageQueue.getInstance().putMessage(uc.getUID(), "�ͺ�Ϊ��"+xh+"���Ĳɹ���������Ϊ0��Ϊ�������������룡");
					return false;
				}else if(yqdhrq.equals("")){
					MessageQueue.getInstance().putMessage(uc.getUID(), "�ͺ�Ϊ��"+xh+"����Ҫ�󵽻����ڲ���Ϊ�գ������룡");
					return false;
				}
			}
			
			//--3
			String sql4 = "SELECT * FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) = ( SELECT MAX ( CONVERT (VARCHAR(100), ZXRQ, 23) ) FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) <= '"+ cgrq +"' AND wlbh = '" + wlbh + "' AND gysbh = '" + gysbh + "' ) AND wlbh = '" + wlbh + "' AND gysbh = '" + gysbh + "' AND ID = ( SELECT MAX (ID) FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) <= '"+cgrq+"' AND wlbh = '"+wlbh+"' AND gysbh = '" + gysbh + "' )";
			double zdcb = DBSql.getDouble(sql4, "ZDCB");//�ܴ��ɱ�
			double tax = DBSql.getDouble(sql4, "SL");//˰��
			//String gysmc = DBSql.getString("SELECT GYSMC FROM BO_AKL_GYS_P WHERE GYSBH='"+gysbh+"'", "GYSMC");//��Ӧ������
			if(cgdj == zero){//�˴��޸ģ�20150519ȥ��˰��Ϊ��У��|| sl == zero
				MessageQueue.getInstance().putMessage(uc.getUID(), "�����ϡ�"+wlbh+"���Ĳɹ����ۻ�˰�ʲ���Ϊ�㣡");
				return false;
			}else{
				if(cgdb.equals(CgddConstant.dbid0) || cgdb.equals(CgddConstant.dbid3)){//���ϲɹ���BG�ɹ�
					if(zdcb == zero || sl == zero){
						MessageQueue.getInstance().putMessage(uc.getUID(), "�ڼ۸����û���ҵ��òɹ����еĹ�Ӧ�̼۸�����ά����");
						return false;
					}else if(cgdj != zdcb || sl != tax){
						MessageQueue.getInstance().putMessage(uc.getUID(), "�����ϡ�"+wlbh+"���Ĳɹ����ۻ�˰����۸���в�������˲飡");
						return false;
					}
				}
			}
			
		}
		
		/**4���жϵ��������е��ͺ���Ψһ**/
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> list = new ArrayList<String>();
		
		String sql = "select xh from " + boTableName + " where bindid = " + bindid + " group by xh having count(xh)>1 ";
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					String xh = StrUtil.returnStr(rs.getString("XH"));
					if(StrUtil.isNotNull(xh)){
						list.add(xh);
					}
				}
			}
			if(list.size()>0 && list.size()<15){
				MessageQueue.getInstance().putMessage(uc.getUID(), "���붩������������ظ����ͺš���Ϣ��" + list.toString());
				return false;
			}else if(list.size()>=15){
				MessageQueue.getInstance().putMessage(uc.getUID(), "���붩������������ظ����ͺš���Ϣ����ȥ�غ����°���");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, ps, rs);
		}
		
		return true;
	}
	
}
