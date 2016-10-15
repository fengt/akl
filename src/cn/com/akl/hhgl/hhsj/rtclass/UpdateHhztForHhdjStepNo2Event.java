package cn.com.akl.hhgl.hhsj.rtclass;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.hhgl.hhsj.rtclass.HHDJConstant;
import cn.com.akl.util.StrUtil;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;


public class UpdateHhztForHhdjStepNo2Event extends WorkFlowStepRTClassA{

	private Connection conn = null;

	public UpdateHhztForHhdjStepNo2Event(UserContext uc) {
		super(uc);
	
		setProvider("QJC");
		setDescription("V1.0");
		setDescription("������������󣬸��»�������״̬Ϊ�����»�����������ȷ�ϡ�");
	}
	@Override
	public boolean execute() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		conn = DBSql.open();
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();	 
		
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
	
		// ͬ����
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "ͬ��");
		System.out.println(tyFlag);
		if(tyFlag == true){
			/**����״̬����������״̬����Ϊ����ȷ�ϡ�**/
			String sql = "SELECT dbo.BO_AKL_HHDD_BODY_TOTAL.HHDH,JHDH,JHDHH,HWLBH,HHSL,HHZXCB,WLBH FROM dbo.BO_AKL_HHDD_BODY_TOTAL INNER JOIN dbo.BO_AKL_HHDD_HEAD ON dbo.BO_AKL_HHDD_BODY_TOTAL.BINDID = dbo.BO_AKL_HHDD_HEAD.BINDID" +
					" WHERE dbo.BO_AKL_HHDD_BODY_TOTAL.HHLB=0 AND dbo.BO_AKL_HHDD_BODY_TOTAL.BINDID=" + bindid;
			try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs!=null){
while(rs.next()){
						
						String HHDH = StrUtil.returnStr(rs.getString("HHDH"));//��������
						String JHDHH = StrUtil.returnStr(rs.getString("JHDHH"));//��������
						String JHDH = StrUtil.returnStr(rs.getString("JHDH"));//��������
						String WLBH = StrUtil.returnStr(rs.getString("WLBH"));//���ϱ��
						int HHSL =  Integer.parseInt(rs.getString("HHSL"));//����
						if(StrUtil.isNotNull(HHDH)){
							
							
							//String sql1 = "update "+HHDJConstant.tableName0+"  set ZT = '"+HHDJConstant.hhzt2+"' where HHDH = '" + HHDH + "'";//���»�����״̬
							//String sql2 = "update "+HHDJConstant.tableName4+"  set YHHSL=case when YHHSL is null then 0 else YHHSL end+"+HHSL+",DHSL=JHSL-case when YHHSL is null then 0 else YHHSL end-"+HHSL+",jhzt=1,HHWCSJ=getdate() where WLBH='"+WLBH+"' HH = '" + JHDHH + "' and JHDH='" + JHDH + "'";//���½����״̬
							
							 //cnt = DBSql.executeUpdate(sql1);
							 //int cnt = DBSql.executeUpdate(sql2);
						}
					}
					String sql1 = "update "+HHDJConstant.tableName0+"  set ZT =1 where bindid = '" + bindid + "'";//���»�����״̬
					
					 //cnt = DBSql.executeUpdate(sql1);
					 int cnt2 = DBSql.executeUpdate(sql1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				DBSql.close(conn, ps, rs);
			}
		}
		
		return false;
	}

	
	}


