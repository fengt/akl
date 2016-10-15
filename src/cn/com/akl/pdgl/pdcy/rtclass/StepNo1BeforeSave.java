package cn.com.akl.pdgl.pdcy.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.dict.util.DictionaryUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	public StepNo1BeforeSave() {
	}

	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("根据盘点单号带入差异单身数据");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		Hashtable pdcydtData = this.getParameter(PARAMETER_FORM_DATA).toHashtable();
		Vector v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_PDCYCL_P", bindid);
		//读取单头信息
		String cydh = pdcydtData.get("CYDH") == null ?"":pdcydtData.get("CYDH").toString();//差异单号
		String pddh = pdcydtData.get("PDDH") == null ?"":pdcydtData.get("PDDH").toString();//盘点单号
		if(tablename.equals("BO_AKL_PDCYCL_P")){
			Vector vc =new Vector();
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection conn = DBSql.open();
			try {
				//盘点单号为空
				if(pddh == null || pddh.trim().length()==0){
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_PDCYCL_S", bindid); 
					return true;
				}
				//单身数据的盘点单号
				String pddhsql = "SELECT distinct PDDH FROM BO_AKL_PDCYCL_S WHERE BINDID="+bindid+"";
				String pddh1 = DBSql.getString(pddhsql, "PDDH");
				if(pddh.equals(pddh1)){
					return true;
				}
				//查询盘点单头品牌编码
				String ppsql = "select PPBH from BO_AKL_KCPD_P where PDDH='"+pddh+"'";
				String ppbh_tmp = DBSql.getString(conn, ppsql, "PPBH");
				String ppbh = DictionaryUtil.parsePPToName(ppbh_tmp);
				//读取盘点单身反馈表信息
				String sql = "select * from BO_AKL_KCPD_FK_S where PDDH='"+pddh+"'";
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						String wlbh = rs.getString("WLBH") == null ?"":rs.getString("WLBH").toString();//物料编号
						String wlmc = rs.getString("WLMC") == null ?"":rs.getString("WLMC").toString();//物料名称
						String xh = rs.getString("XH") == null ?"":rs.getString("XH").toString();//型号
						String sx = rs.getString("SX") == null ?"":rs.getString("SX").toString();//属性
						String pc = rs.getString("PC") == null ?"":rs.getString("PC").toString();//批次
						int kwsl = rs.getInt("KWSL");//库位数量
						int pksjsl = rs.getInt("PKSJSL");//盘库实际数量
						String cyyy = rs.getString("CYYY") == null ?"":rs.getString("CYYY").toString();//差异原因
						String hwdm = rs.getString("HWDM") == null ?"":rs.getString("HWDM").toString();//货位代码
						//插入入库单身表
						Hashtable recordData = new Hashtable();
						recordData.put("PDDH", pddh);//盘点单号
						recordData.put("CYDH", cydh);//差异单号
						recordData.put("WLBH", wlbh);//物料编号
						recordData.put("WLMC", wlmc);//物料名称
						recordData.put("PPBH", ppbh);//品牌编号
						recordData.put("XH", xh);//型号
						recordData.put("SX", sx);//属性
						recordData.put("PC", pc);//批次
						recordData.put("KWSL", kwsl);//库位数量
						recordData.put("PKSJSL", pksjsl);//盘库实际数量
						recordData.put("CYSL", pksjsl-kwsl);//差异数量
						recordData.put("CYYY", cyyy);//差异原因
						recordData.put("KWBM", hwdm);//货位代码
						if(pksjsl-kwsl != 0){
							vc.add(recordData);
						}
					}
					//删除数据
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_PDCYCL_S", bindid); 
					//插入数据库
					BOInstanceAPI.getInstance().createBOData("BO_AKL_PDCYCL_S", vc, this.getParameter(PARAMETER_INSTANCE_ID).toInt(),  this.getUserContext().getUID());
				}else{
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "当前盘点单反馈表为空，请检查!");
					return false;
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBSql.close(conn, ps, rs);
			}
		}
		return true;
	}

}
