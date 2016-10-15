package cn.com.akl.cggl.cgdd.route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.cggl.cgdd.constant.CgddConstant;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.awf.workflow.execute.route.extend.ParticipantRouteInterface;


public class StepNo2Participant implements ParticipantRouteInterface{

	@Override
	public String getDescription() {
		return "网销经理\\网销副总";
	}

	@Override
	public String routeUser(UserContext uc, ProcessInstanceModel instanceModel,
			DepartmentModel localDepartmentModel, int ownerDepartmentModel,
			WorkFlowStepModel workFlowStepModel, int taskId) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder sb = new StringBuilder();
		String userid = "";
		String sql = "select userid,username from orguser u,orgrole r where u.roleid = r.id and r.rolename in ('" + CgddConstant.reloName0+ "','" + CgddConstant.reloName1 + "')";
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					userid = StrUtil.returnStr(rs.getString("userid"));
					sb.append(userid + " ");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, ps, rs);
		}
		return sb.toString();
	}
}
