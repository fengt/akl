package cn.com.akl.hhgl.hhsj.rtclass;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.hhgl.hhsj.rtclass.HHDJConstant;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class UpdateHhztForHhdjStepNo1Event extends WorkFlowStepRTClassA{

	private Connection conn = null;

	public UpdateHhztForHhdjStepNo1Event(UserContext uc) {
		super(uc);
	
		setProvider("QJC");
		setDescription("V1.0");
		setDescription("�������󣬸��»�������״̬Ϊ�����»������������������С������½�������������С�");
	}
	@Override
	public boolean execute() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		conn = DBSql.open();
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector<Hashtable<String, Object>> tvhts = BOInstanceAPI.getInstance().getBODatas("BO_AKL_HHDD_BODY_TOTAL", bindid);
		if(tvhts == null || tvhts.size() < 1 ){
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "������������в����ڼ�¼",true);
			return false;
		}
		/**����״̬������������������󣬽������״̬����Ϊ�������С���������״̬����Ϊ�������������С�**/
		String sql = "SELECT dbo.BO_AKL_HHDD_BODY_TOTAL.HHDH,JHDH,JHDHH,HWLBH,HHSL,HHZXCB,WLBH,[dbo].[BO_AKL_HHDD_HEAD].[HHLB] FROM dbo.BO_AKL_HHDD_BODY_TOTAL INNER JOIN dbo.BO_AKL_HHDD_HEAD ON dbo.BO_AKL_HHDD_BODY_TOTAL.BINDID = dbo.BO_AKL_HHDD_HEAD.BINDID" +
				" WHERE dbo.BO_AKL_HHDD_BODY_TOTAL.BINDID=" + bindid;

		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					
					String HHDH = StrUtil.returnStr(rs.getString("HHDH"));//��������
					String JHDHH = StrUtil.returnStr(rs.getString("JHDHH"));//��������
					String JHDH = StrUtil.returnStr(rs.getString("JHDH"));//��������
					String WLBH = StrUtil.returnStr(rs.getString("WLBH"));//���ϱ��
					int HHLB = Integer.parseInt(rs.getString("HHLB"));//���ϱ��
					int HHSL =  Integer.parseInt(rs.getString("HHSL"));//����
					if(StrUtil.isNotNull(HHDH)){
						
						
						
						String sql2 = "update "+HHDJConstant.tableName4+" set YHHSL=case when YHHSL is null then 0 else YHHSL end+"+HHSL+",DHSL=JHSL-case when YHHSL is null then 0 else YHHSL end-"+HHSL+",HHWCSJ=getdate() where WLBH='"+WLBH+"' and JHDH='" + JHDH + "'";//���½����״̬
						 int cnt = DBSql.executeUpdate(sql2);
						if(HHLB==0)
						{
							String sql1 = "update "+HHDJConstant.tableName3+"  set YHHSL=case when YHHSL is null then 0 else YHHSL end+"+HHSL+",DHSL=JHSL-case when YHHSL is null then 0 else YHHSL end-"+HHSL+",HHWCSJ=getdate(),hhzt=case when JHSL-case when YHHSL is null then 0 else YHHSL end-"+HHSL+">0 then 1 else 2 end where HH='"+JHDHH+"' and WLBH='"+WLBH+"' and JHDH='" + JHDH + "'";//���½����״̬
							cnt = DBSql.executeUpdate(sql1);
						}							
					}
				}
				String sql1 = "update "+HHDJConstant.tableName0+"  set ZT =0 where bindid = '" + bindid + "'";//���»�����״̬
				
				 //cnt = DBSql.executeUpdate(sql1);
				 int cnt2 = DBSql.executeUpdate(sql1);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, ps, rs);
		}
		return false;
	}

	
	}
