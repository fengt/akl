package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class Validate_DGCG extends WorkFlowStepRTClassA {

	public Validate_DGCG() {
	}

	public Validate_DGCG(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("校验采购数量不可为0，采购单身数据是否重复");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGCG_S", bindid);
		if(vc == null){
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "采购单身信息不可为空！！！");
			return false;
		}
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select XH,DW from BO_AKL_DGCG_S where bindid = '"+bindid+"' group by XH,DW having count(*) > 1";
		Connection conn = DBSql.open();
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					String xh = rs.getString("XH") == null?"":rs.getString("XH");//型号
					String dw = rs.getString("DW") == null?"":rs.getString("DW");//计量单位
					String dwsql = "select XLMC from BO_AKL_DATA_DICT_S where DLBM='026' and XLBM='"+dw+"'";
					String dwmc = DBSql.getString(dwsql, "XLMC");
					if(!"".equals(xh)){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "采购单身存在相同数据，型号"+xh+"，单位"+dwmc
							+"，请检查!");
						return false;
					}
				}
			}
			Iterator t = vc.iterator();
			while(t.hasNext()){
				//读取入库单身数据
				Hashtable formData = (Hashtable) t.next();
				String xh = formData.get("XH") == null ?"":formData.get("XH").toString();//型号
				String dw = formData.get("DW") == null ?"":formData.get("DW").toString();//计量单位
				String cgsl = formData.get("CGSL") == null ?"":formData.get("CGSL").toString();//采购数量
				int sl = Integer.parseInt(cgsl);//数量
				String dwsql = "select XLMC from BO_AKL_DATA_DICT_S where DLBM='026' and XLBM='"+dw+"'";
				String dwmc = DBSql.getString(dwsql, "XLMC");
				if(sl == 0){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "型号"+xh+"，单位"+dwmc
							+"的采购数量为0,请检查!");
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "校验报错，请通知后台");
			e.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, ps, rs);
		}
		return false;
	}
}
