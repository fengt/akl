package cn.com.akl.rmahpfh.rtclass;

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

public class Validate_XDSL extends WorkFlowStepRTClassA {

	public Validate_XDSL() {}

	public Validate_XDSL(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("验证单身数据是否为空，数量与库存数量是否相符，单身型号是否重复");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable recordData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAHPFH_HEAD", bindid);
		String pp = recordData.get("PP") == null ?"":recordData.get("PP").toString();//品牌
		String qsrq = recordData.get("QSRQ") == null ?"":recordData.get("QSRQ").toString();//起始日期
		String jsrq = recordData.get("JSRQ") == null ?"":recordData.get("JSRQ").toString();//结束日期
		Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAHPFH_BODY", bindid);
		if(vc == null){
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "坏品发货单身信息不可为空，请导入！");
			return false;
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = DBSql.open();
		try {
			Iterator t = vc.iterator();
			while(t.hasNext()){
				Hashtable formData = (Hashtable) t.next();
//				String shdh = formData.get("RMASHDH") == null ?"":formData.get("RMASHDH").toString();//收货单号
//				String wlbh = formData.get("WLBH") == null ?"":formData.get("WLBH").toString();//物料编号
				String xh = formData.get("XH") == null ?"":formData.get("XH").toString();//型号
				String sl = formData.get("SL") == null ?"":formData.get("SL").toString();//数量
				int sl1 = Integer.parseInt(sl);//数量
				String sql = "SELECT SUM (KWSL) AS SL FROM BO_AKL_RMA_KCMX a,BO_AKL_WXB_XS_RMASH_P b WHERE a.KWSL != 0 AND b.ISEND = 1 AND LX IN ('坏品返新','返新已处理') AND a.XH = '"+xh+"' AND a.DDH = b.DJBH AND b.PP = '"+pp+"' AND b.UPDATEDATE >= '"+qsrq+"' AND b.UPDATEDATE < '"+jsrq+"'";
				int kwsl = DBSql.getInt(sql, "SL");//库位数量
				/*if(sl1 == 0){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "坏品发货单身坏品数量不可为0，请修正！");
					return false;
				}*/
				if(kwsl != sl1){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "型号【"+xh+"】的数量【"+sl1+"】与库存数量【"+kwsl+"】不符，请检查库存，删除单身重新录入！");
					return false;
				}
			}
			
			//判断坏品发货单身型号是否重复
			String sql = "select XH from BO_AKL_WXB_RMAHPFH_BODY where bindid = '"+bindid+"' group by XH having count(*)>1";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					//String wlbh = rs.getString("WLBH") == null?"":rs.getString("WLBH");
					String xh = rs.getString("XH") == null?"":rs.getString("XH");
					if(!"".equals(xh)){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "坏品发货单身表中存在重复型号"+xh+"，请检查!");
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
