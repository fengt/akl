package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class Validate_SHYS extends WorkFlowStepRTClassA {

	public Validate_SHYS() {
	}

	public Validate_SHYS(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("校验实收数量是否小于等于应收数量，校验物料数据是否重复，校验单身数据是否已办理");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		//读取单头信息
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);
		String cgdh = rkdtData.get("YDH") == null ?"":rkdtData.get("YDH").toString();//采购单号
		String rklx = rkdtData.get("RKLX") == null ?"":rkdtData.get("RKLX").toString();//入库类型
		//入库类型不是其它入库，选单号不可为空
		if(!rklx.equals("其它入库")){
			if(cgdh.equals("")){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "选单号为空，请选择单号!");
				return false;
			}
		}
		//读取单身信息
		Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);
		if(vc == null){
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "入库单身信息不可为空！！！");
			return false;
		}
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = DBSql.open();
		try {
			Iterator t = vc.iterator();
			while(t.hasNext()){
				//读取入库单身数据
				Hashtable formData = (Hashtable) t.next();
//				String cgdh = formData.get("CGDH") == null ?"":formData.get("CGDH").toString();//采购单号
				String wlbh = formData.get("WLBH") == null ?"":formData.get("WLBH").toString();//物料编号
				String wlmc = formData.get("WLMC") == null ?"":formData.get("WLMC").toString();//物料名称
				String xh = formData.get("XH") == null ?"":formData.get("XH").toString();//型号
				String dw = formData.get("DW") == null ?"":formData.get("DW").toString();//单位
				String yssl = formData.get("YSSL") == null ?"":formData.get("YSSL").toString();//应收数量
				String sssl = formData.get("SSSL") == null ?"":formData.get("SSSL").toString();//实收数量
				int sl1 = Integer.parseInt(yssl);//应收数量
				int sl2 = Integer.parseInt(sssl);//实收数量
				String dwsql = "select XLMC from BO_AKL_DATA_DICT_S where DLBM='026' and XLBM='"+dw+"'";
				String dwmc = DBSql.getString(dwsql, "XLMC");
				if(sl1 != sl2){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "型号"+xh+"，单位"+dwmc
							+"的实收数量"+sl2+"和应收数量"+sl1+"不相等,请检查!");
					return false;
				}
			}
			//判断单身数据物料是否重复
			String sql = "select XH,DW from BO_AKL_DGRK_S where bindid = '"+bindid+"' group by XH,DW having count(*) > 1";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					String xh = rs.getString("XH") == null?"":rs.getString("XH");
					String dw = rs.getString("DW") == null?"":rs.getString("DW");
					String dwsql = "select XLMC from BO_AKL_DATA_DICT_S where DLBM='026' and XLBM='"+dw+"'";
					String dwmc = DBSql.getString(dwsql, "XLMC");
					if(!"".equals(xh) ){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "入库单身存在重复物料数据，型号"+xh
								+",单位"+dwmc+"，请检查!");
						return false;
					}
				}
			}
			//如果入库类型不是其它入库，判断单身数据是否已办理
			if(!rklx.equals("其它入库")){
				String ztsql = "select CGZT from BO_AKL_DGCG_P where DDBH='"+cgdh+"'";
				String cgzt = DBSql.getString(ztsql, "CGZT");
				if(!cgzt.equals("待采购")){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "入库单身数据已办理!");
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, ps, rs);
		}
		return false;
	}
}
