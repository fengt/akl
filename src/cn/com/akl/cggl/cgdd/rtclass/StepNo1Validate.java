package cn.com.akl.cggl.cgdd.rtclass;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import cn.com.akl.cggl.cgdd.constant.CgddConstant;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Validate extends WorkFlowStepRTClassA{

	private UserContext uc;
	public StepNo1Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("zhangran");
		setDescription("V1.0");
		setDescription("处理导入单身数据唯一且不为空!");
	}
	
	private Connection conn = null;
	private String boTableName = CgddConstant.tableName1;
	
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable head = BOInstanceAPI.getInstance().getBOData(CgddConstant.tableName0, bindid);
		Vector vector = BOInstanceAPI.getInstance().getBODatas(boTableName, bindid);
		
		if(vector == null){
			MessageQueue.getInstance().putMessage(uc.getUID(), "您的单身信息为空，请输入！");
			return false;
		}else{
			return checkField(vector,bindid,head);
		}
	}
	
	/**
	 * 判断部分字段是否唯一或为空
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public boolean checkField(Vector vector, int bindid, Hashtable head){
		
		/**1、判断导入订单编号需唯一**/
		String sql2 = "select COUNT(DISTINCT(ddid)) cnt from " + boTableName + " where bindid = " + bindid ;
		int cnt = DBSql.getInt(sql2, "cnt");
		if(cnt > 1){
			MessageQueue.getInstance().putMessage(uc.getUID(), "导入【订单编号】须唯一！");
			return false;
		}
		
		/**
		 * 2、判断采购数量和要求到货日期不能为空，物料型号是否存在
		 * 3、闪迪采购/BG采购，需要校验是否已导入该供应商价格
		 */
		String cgdb = head.get("DBID").toString();//采购单别
		String gysbh = head.get("GYSID").toString();//供应商编号
		Date cgrq = Date.valueOf(head.get("CGRQ").toString());//采购日期
		double zero = 0.000d;
		for (int i = 0; i < vector.size(); i++) {
			Hashtable rec = (Hashtable)vector.get(i);
			String xh = rec.get("XH").toString();//型号
			String wlbh = rec.get("WLBH").toString();//物料编号
			double cgdj = Double.parseDouble(rec.get("CGDJ").toString());//采购单价 
			double sl = Double.parseDouble(rec.get("SL").toString());//税率
			
			//--2
			String sql3 = "select count(*) n from " + CgddConstant.tableName3 + " where xh = '"+xh+"'";
			int n = DBSql.getInt(sql3, "n");
			if(n <= 0){
				MessageQueue.getInstance().putMessage(uc.getUID(), "单身数据中型号为【"+xh+"】的物料不在物料属性信息中，请核查！");
				return false;
			}else{//采购数量和要求到货日校验
				int cgsl = Integer.parseInt(rec.get("CGSL").toString());
				String yqdhrq = rec.get("YQDHRQ").toString();
				if(cgsl == 0 || cgsl < 0){
					MessageQueue.getInstance().putMessage(uc.getUID(), "型号为【"+xh+"】的采购数量不能为0或为负，请重新输入！");
					return false;
				}else if(yqdhrq.equals("")){
					MessageQueue.getInstance().putMessage(uc.getUID(), "型号为【"+xh+"】的要求到货日期不能为空，请输入！");
					return false;
				}
			}
			
			//--3
			String sql4 = "SELECT * FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) = ( SELECT MAX ( CONVERT (VARCHAR(100), ZXRQ, 23) ) FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) <= '"+ cgrq +"' AND wlbh = '" + wlbh + "' AND gysbh = '" + gysbh + "' ) AND wlbh = '" + wlbh + "' AND gysbh = '" + gysbh + "' AND ID = ( SELECT MAX (ID) FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) <= '"+cgrq+"' AND wlbh = '"+wlbh+"' AND gysbh = '" + gysbh + "' )";
			double zdcb = DBSql.getDouble(sql4, "ZDCB");//总代成本
			double tax = DBSql.getDouble(sql4, "SL");//税率
			//String gysmc = DBSql.getString("SELECT GYSMC FROM BO_AKL_GYS_P WHERE GYSBH='"+gysbh+"'", "GYSMC");//供应商名称
			if(cgdj == zero){//此处修改：20150519去除税率为零校验|| sl == zero
				MessageQueue.getInstance().putMessage(uc.getUID(), "该物料【"+wlbh+"】的采购单价或税率不能为零！");
				return false;
			}else{
				if(cgdb.equals(CgddConstant.dbid0) || cgdb.equals(CgddConstant.dbid3)){//闪迪采购，BG采购
					if(zdcb == zero || sl == zero){
						MessageQueue.getInstance().putMessage(uc.getUID(), "在价格表中没有找到该采购单中的供应商价格，请先维护！");
						return false;
					}else if(cgdj != zdcb || sl != tax){
						MessageQueue.getInstance().putMessage(uc.getUID(), "该物料【"+wlbh+"】的采购单价或税率与价格表中不符，请核查！");
						return false;
					}
				}
			}
			
		}
		
		/**4、判断单身数据中的型号需唯一**/
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> list = new ArrayList<String>();
		
		String sql = "select xh from " + boTableName + " where bindid = " + bindid + " group by xh having count(xh)>1 ";
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					String xh = StrUtil.returnStr(rs.getString("XH"));
					if(StrUtil.isNotNull(xh)){
						list.add(xh);
					}
				}
			}
			if(list.size()>0 && list.size()<15){
				MessageQueue.getInstance().putMessage(uc.getUID(), "导入订单不允许存在重复【型号】信息：" + list.toString());
				return false;
			}else if(list.size()>=15){
				MessageQueue.getInstance().putMessage(uc.getUID(), "导入订单不允许存在重复【型号】信息，请去重后重新办理！");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, ps, rs);
		}
		
		return true;
	}
	
}
