package cn.com.akl.hhgl.hhrk.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//import cn.com.akl.cggl.cgdd.constant.CgddConstant;
import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class UpdateDdztForHhddStepNo2And3Event extends WorkFlowStepRTClassA{

	private Connection conn = null;
	private UserContext uc;
	public UpdateDdztForHhddStepNo2And3Event(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("qjc");
		setDescription("V1.0");
		setDescription("办理完毕后，更新采购订单的订单状态为：已提货!");
	}
	@Override
	public boolean execute() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		conn = DBSql.open();
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String sql = "select cgddh,xh,sssl from " + HHDJConstant.tableName1 + " where bindid = " + bindid;
		/**更新采购订单订单状态**/
		
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					String aklOrderId = StrUtil.returnStr(rs.getString("cgddh"));
					String xh = StrUtil.returnStr(rs.getString("xh"));
					int sssl = rs.getInt("sssl");
					if(StrUtil.isNotNull(aklOrderId) && StrUtil.isNotNull(xh)){
						updateDatas(conn,aklOrderId,xh,sssl);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, ps, rs);
		}
		return false;
	}

	/***
	 * 根据采购订单号，更新采购订单订单状态为已提货
	 * @param aklOrderId
	 * @return
	 */
	private int updateDatas(Connection conn,String aklOrderId,String xh,int sssl){
		conn = DBSql.open();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select * from " + HHDJConstant.tableName7 + " where ddid = '" + aklOrderId + "' and xh = '" + xh + "'"; 
		String sql2 = "";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					int cgsl = rs.getInt("cgsl");
					if(cgsl==sssl){
						sql2 = "update " + HHDJConstant.tableName1 + " set zt = '" + HHDJConstant.ddzt2 + "' where ddid = '" + aklOrderId + "' and xh = '" + xh +"'";
					}else if(cgsl>sssl){
						sql2 = "update " + HHDJConstant.tableName1 + " set zt = '" + HHDJConstant.ddzt7 + "' where ddid = '" + aklOrderId + "' and xh = '" + xh +"'";
					}else{
						sql2 = "update " + HHDJConstant.tableName1 + " set zt = '" + HHDJConstant.ddzt8 + "' where ddid = '" + aklOrderId + "' and xh = '" + xh +"'";
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(null, ps, rs);
		}
		int cnt = DBSql.executeUpdate(sql2);
		return cnt;
	}
}
