package cn.com.akl.AFS;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.Set;

import cn.com.akl.hhgl.hhsj.rtclass.HHDJConstant;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class Activation extends WorkFlowStepRTClassA{

	private Connection conn = null;
	private UserContext uc;
	public Activation(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("qjc");
		setDescription("V1.0");
		setDescription("办理完毕后，FTP上传文件!");
	}
	
	@Override
	public boolean execute() {	
		
		// 数据库连接
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		conn = DBSql.open();
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();	
		
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
	
		// 同意标记
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "通过");
		
	if(!tyFlag)
	{return true;}	
				
				
				String col="";		
		try {					
			
			col="SOLDTOCODE,SKUPN,CUSTOMERPO,DN,FCODE,BOXCODE1,BOXCODE2,BOXCODE3,BOXCODE4,BOXCODE5,BOXCODE6,BOXCODE7,BOXCODE8,BOXCODE9,BOXCODE10,RESULTS,BIG";
			
			File file = cn.com.akl.util.FileUtil.saveBytesToFile("activate_"+bindid+".csv", new String(col).getBytes());
			
			
			
			/**获取需要激活的数据明细**/
			String sql = "SELECT [SOLDTOCODE]+','+[SKUPN]+','+[CUSTOMERPO]+','+[DN]+','+[FCODE]+','+[BOXCODE1]+','+[BOXCODE2]+','+[BOXCODE3]+','+[BOXCODE4]+','+[BOXCODE5]+','+[BOXCODE6]+','+[BOXCODE7]+','+[BOXCODE8]+','+[BOXCODE9]+','+[BOXCODE10]+','+[RESULTS]+','+[BIG]  as 'vl' FROM [dbo].[BO_AFS_ACTIVATE_DETAIL] where BINDID=" + bindid;
			try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs!=null){
					while(rs.next())
					{	String vl ="\n"+ StrUtil.returnStr(rs.getString("vl"));//还货单号						
						file = cn.com.akl.util.FileUtil.addBytesToFile("activate_"+bindid+".csv", new String(vl).getBytes());						
					}
					
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				DBSql.close(conn, ps, rs);
			}
					cn.com.akl.util.FileUtil.saveFileToFtpServer("10.10.10.221", 2131, "ftp", "akl,.ftp12b11", file);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	
	
	
}
