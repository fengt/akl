package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DateUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;

public class Update_CGZT extends WorkFlowStepRTClassA {

	public Update_CGZT() {
	}

	public Update_CGZT(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("生成批次号；更新采购单身、单头的采购状态；插入库存汇总表");
	}

	@Override
	public boolean execute() {
		//生成批次号
		Date now = new Date();
		String pch1 = DateUtil.dateToLongStrBys2(now);//前缀，如：20140724
		String pch = pch1 + judgeRKRQ(pch1);
				
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		//读取入库单头信息
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);
		String rkdh = rkdtData.get("RKDH") == null ?"":rkdtData.get("RKDH").toString();//入库单号
		String ydh = rkdtData.get("YDH") == null ?"":rkdtData.get("YDH").toString();//采购单号
		String rklx = rkdtData.get("RKLX") == null ?"":rkdtData.get("RKLX").toString();//入库类型
		
		Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);
		Vector v1 = new Vector();//库存汇总
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement bindps = null;
		ResultSet bindrs = null;
		Connection conn = DBSql.open();
		try {
			Iterator t = vc.iterator();
			while(t.hasNext()){
				//读取入库单身数据
				Hashtable formData = (Hashtable) t.next();
				String wlbh = formData.get("WLBH") == null ?"":formData.get("WLBH").toString();//物料编号
				String wlmc = formData.get("WLMC") == null ?"":formData.get("WLMC").toString();//物料名称
				String xh = formData.get("XH") == null ?"":formData.get("XH").toString();//型号
				String dw = formData.get("DW") == null ?"":formData.get("DW").toString();//单位
				String yssl = formData.get("YSSL") == null ?"":formData.get("YSSL").toString();//应收数量
				String sssl = formData.get("SSSL") == null ?"":formData.get("SSSL").toString();//实收数量
				int sl1 = Integer.parseInt(yssl);//应收数量
				int sl2 = Integer.parseInt(sssl);//实收数量
				
				//添加库存汇总数据
				Hashtable recordData1 = new Hashtable();
				recordData1.put("PCH", pch);//批次号
				recordData1.put("WLBH", wlbh);//物料编号
				recordData1.put("WLMC", wlmc);//物料名称
				recordData1.put("XH", xh);//型号
				recordData1.put("DW", dw);//单位
				recordData1.put("RKSL", sl2);//入库数量
				recordData1.put("PCSL", sl1);//批次数量
				recordData1.put("RKDH", rkdh);//入库单号
				recordData1.put("ZT", "042023");//状态在途
				v1.add(recordData1);
				//如果入库类型不是其它入库，更新采购单身采购状态
				if(!rklx.equals("其它入库")){
					String cgssql = "update BO_AKL_DGCG_S set CGZT='待入库' where DDBH='"+ydh+"' and xh='"+xh+"' and dw='"+dw+"'";
					DBSql.executeUpdate(conn,cgssql);
				}
			}
			//更新入库单身批次号
			String rksql = "update BO_AKL_DGRK_S set PCH='"+pch+"' where bindid='"+bindid+"'";
			DBSql.executeUpdate(conn,rksql);
			//插入汇总表
			BOInstanceAPI.getInstance().removeProcessInstanceBOData("BO_AKL_DGKC_KCHZ_P", bindid);
			BOInstanceAPI.getInstance().createBOData("BO_AKL_DGKC_KCHZ_P", v1, bindid, this.getUserContext().getUID());
			//如果入库类型不是其它入库，更新采购单头采购状态，删除代管入库冗余流程
			if(!rklx.equals("其它入库")){
				//更新采购单头采购状态
				String sql = "select CGZT from BO_AKL_DGCG_S where DDBH='"+ydh+"'";
				boolean flag = true;
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						String cgzt = rs.getString("CGZT") == null?"":rs.getString("CGZT");
						if("待采购".equals(cgzt)){
							flag = false;
						}
					}
					if(flag){
						String cgpsql = "update BO_AKL_DGCG_P set CGZT='待入库' where DDBH='"+ydh+"'";
						DBSql.executeUpdate(conn,cgpsql);
					}
				}
				//删除代管入库冗余流程
				String bindsql = "select bindid from BO_AKL_DGRK_P where ydh='"+ydh+"' AND ISEND=0";
				bindps = conn.prepareStatement(bindsql);
				bindrs = bindps.executeQuery();
				if(bindrs != null){
					while(bindrs.next()){
						String bind = bindrs.getString("bindid") == null?"":bindrs.getString("bindid");
						int instanceid = Integer.parseInt(bind);
						if(instanceid != bindid){
							WorkflowInstanceAPI.getInstance().removeProcessInstance(instanceid);
						}
					}
				}
			}
			return true;
		} catch (Exception e) {
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "更新失败，请通知管理员");
			e.printStackTrace(System.err);
			return false;
		} finally {
			DBSql.close(ps, rs);
			DBSql.close(conn, bindps, bindrs);
		}
	}
	
	/**
	 * 判断是否存在当前入库日期的批次号，如果库存汇总表中已存在当前入库日期，则批次号，后缀三位进行累加，否则，当前入库日期+001
	 * @param pch1
	 * @return
	 */
	private  String judgeRKRQ(String pch1){
		String sql = "select max(SUBSTRING(PCH,9,3)) pch2 from BO_AKL_DGKC_KCHZ_P where SUBSTRING(PCH,1,8) = '" + pch1 + "'";
		String pch2 = "";
		pch2 = StrUtil.returnStr(DBSql.getString(sql, "pch2"));
		if(StrUtil.isNotNull(pch2)){
			return String.format("%03d", Integer.parseInt(pch2)+1);
		}
		return "001";
	}
}
