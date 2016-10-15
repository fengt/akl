package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class Validate_XH extends WorkFlowStepRTClassA {

	public Validate_XH() {
	}

	public Validate_XH(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("校验同一批次型号是否有重复");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select XH from BO_AKL_DGRK_S where bindid = '"+bindid+"' group by XH having count(XH) > 1";
		Connection conn = DBSql.open();
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					String xh = StrUtil.returnStr(rs.getString("XH"));
					if(!"".equals(xh)){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "入库单身存在相同型号"+xh+",请检查!");
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, ps, rs);
		}
		return true;
	}

}
