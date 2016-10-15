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
		setDescription("网销审批办理后，更新还货单的状态为：更新还货单单身“已确认”");
	}
	@Override
	public boolean execute() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		conn = DBSql.open();
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();	 
		
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
	
		// 同意标记
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "同意");
		System.out.println(tyFlag);
		if(tyFlag == true){
			/**更新状态：还货单据状态更新为“已确认”**/
			String sql = "SELECT dbo.BO_AKL_HHDD_BODY_TOTAL.HHDH,JHDH,JHDHH,HWLBH,HHSL,HHZXCB,WLBH FROM dbo.BO_AKL_HHDD_BODY_TOTAL INNER JOIN dbo.BO_AKL_HHDD_HEAD ON dbo.BO_AKL_HHDD_BODY_TOTAL.BINDID = dbo.BO_AKL_HHDD_HEAD.BINDID" +
					" WHERE dbo.BO_AKL_HHDD_BODY_TOTAL.HHLB=0 AND dbo.BO_AKL_HHDD_BODY_TOTAL.BINDID=" + bindid;
			try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs!=null){
while(rs.next()){
						
						String HHDH = StrUtil.returnStr(rs.getString("HHDH"));//还货单号
						String JHDHH = StrUtil.returnStr(rs.getString("JHDHH"));//货货单号
						String JHDH = StrUtil.returnStr(rs.getString("JHDH"));//货货单号
						String WLBH = StrUtil.returnStr(rs.getString("WLBH"));//物料编号
						int HHSL =  Integer.parseInt(rs.getString("HHSL"));//批次
						if(StrUtil.isNotNull(HHDH)){
							
							
							//String sql1 = "update "+HHDJConstant.tableName0+"  set ZT = '"+HHDJConstant.hhzt2+"' where HHDH = '" + HHDH + "'";//更新还货单状态
							//String sql2 = "update "+HHDJConstant.tableName4+"  set YHHSL=case when YHHSL is null then 0 else YHHSL end+"+HHSL+",DHSL=JHSL-case when YHHSL is null then 0 else YHHSL end-"+HHSL+",jhzt=1,HHWCSJ=getdate() where WLBH='"+WLBH+"' HH = '" + JHDHH + "' and JHDH='" + JHDH + "'";//更新借货单状态
							
							 //cnt = DBSql.executeUpdate(sql1);
							 //int cnt = DBSql.executeUpdate(sql2);
						}
					}
					String sql1 = "update "+HHDJConstant.tableName0+"  set ZT =1 where bindid = '" + bindid + "'";//更新还货单状态
					
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


