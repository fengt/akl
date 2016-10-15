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
		setDescription("申请办理后，更新还货单的状态为：更新还货单单身“还货申请中”，更新借货单单身“还货中”");
	}
	@Override
	public boolean execute() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		conn = DBSql.open();
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector<Hashtable<String, Object>> tvhts = BOInstanceAPI.getInstance().getBODatas("BO_AKL_HHDD_BODY_TOTAL", bindid);
		if(tvhts == null || tvhts.size() < 1 ){
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "还货单身汇总中不存在记录",true);
			return false;
		}
		/**更新状态：还货单申请点击办理后，借货单据状态更新为“还货中”，还货单状态更新为：“还货申请中”**/
		String sql = "SELECT dbo.BO_AKL_HHDD_BODY_TOTAL.HHDH,JHDH,JHDHH,HWLBH,HHSL,HHZXCB,WLBH,[dbo].[BO_AKL_HHDD_HEAD].[HHLB] FROM dbo.BO_AKL_HHDD_BODY_TOTAL INNER JOIN dbo.BO_AKL_HHDD_HEAD ON dbo.BO_AKL_HHDD_BODY_TOTAL.BINDID = dbo.BO_AKL_HHDD_HEAD.BINDID" +
				" WHERE dbo.BO_AKL_HHDD_BODY_TOTAL.BINDID=" + bindid;

		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					
					String HHDH = StrUtil.returnStr(rs.getString("HHDH"));//还货单号
					String JHDHH = StrUtil.returnStr(rs.getString("JHDHH"));//货货单号
					String JHDH = StrUtil.returnStr(rs.getString("JHDH"));//货货单号
					String WLBH = StrUtil.returnStr(rs.getString("WLBH"));//物料编号
					int HHLB = Integer.parseInt(rs.getString("HHLB"));//物料编号
					int HHSL =  Integer.parseInt(rs.getString("HHSL"));//批次
					if(StrUtil.isNotNull(HHDH)){
						
						
						
						String sql2 = "update "+HHDJConstant.tableName4+" set YHHSL=case when YHHSL is null then 0 else YHHSL end+"+HHSL+",DHSL=JHSL-case when YHHSL is null then 0 else YHHSL end-"+HHSL+",HHWCSJ=getdate() where WLBH='"+WLBH+"' and JHDH='" + JHDH + "'";//更新借货单状态
						 int cnt = DBSql.executeUpdate(sql2);
						if(HHLB==0)
						{
							String sql1 = "update "+HHDJConstant.tableName3+"  set YHHSL=case when YHHSL is null then 0 else YHHSL end+"+HHSL+",DHSL=JHSL-case when YHHSL is null then 0 else YHHSL end-"+HHSL+",HHWCSJ=getdate(),hhzt=case when JHSL-case when YHHSL is null then 0 else YHHSL end-"+HHSL+">0 then 1 else 2 end where HH='"+JHDHH+"' and WLBH='"+WLBH+"' and JHDH='" + JHDH + "'";//更新借货单状态
							cnt = DBSql.executeUpdate(sql1);
						}							
					}
				}
				String sql1 = "update "+HHDJConstant.tableName0+"  set ZT =0 where bindid = '" + bindid + "'";//更新还货单状态
				
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
