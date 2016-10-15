package cn.com.akl.shgl.fjfj.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjfj.biz.FJFJBiz;
import cn.com.akl.shgl.fjfj.biz.ShipmentsBiz;
import cn.com.akl.shgl.fjfj.cnt.FJFJCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo7Transaction extends WorkFlowStepRTClassA {

	
	private Connection conn = null;
	private UserContext uc;
	private ShipmentsBiz shipBiz = new ShipmentsBiz();
	public StepNo7Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo7Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("扣减总部检测库房的库存。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		int stepNo = 7;//定义该节点号
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			/**1、扣减总部检测库存*/
			FJFJBiz.decreaseKCXX(conn, bindid, stepNo);
			
			/**2、更新库存和插入待发货记录*/
			increaseKCXXAndShipments(conn, bindid, uid);
			
			/**3、更新送修单处理方式和检测子表"是否返货"值*/
			setSXDealAndIsBack(conn, bindid);
			
			/**4、更新单据状态*/
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_P_ZT, FJFJCnt.djzt1, bindid);
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_S_ZT, FJFJCnt.djzt1, bindid);
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * 加库存和插入待发货记录（客服）
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void increaseKCXXAndShipments(Connection conn, final int bindid, final String uid)throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_XMLB, bindid));//项目类别
		final String ckdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_JCKF, bindid));//寄出库房
		
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//物料编号
				String wlmc = StrUtil.returnStr(rs.getString("CPZWMC"));//物料名称
				String xh = StrUtil.returnStr(rs.getString("PN"));//型号
				String gztm = StrUtil.returnStr(rs.getString("KFGZDM"));//故障条码
				String sx = StrUtil.returnStr(rs.getString("SX"));//属性
				String pch = StrUtil.returnStr(rs.getString("PCH"));//批次号
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));//货位代码
				String ejjl = StrUtil.returnStr(rs.getString("EJJL"));//二检结论
				String sjjl = StrUtil.returnStr(rs.getString("SJJL"));//三检结论
				int sl = rs.getInt("SL");//数量
				
//				String pch = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_PCH, xmlb, wlbh, hwdm, sx, FJFJCnt.zt4));//获取该物料库存批次号
				
				/**1、有故障或三检被收入的，则该产品不返回给客服*/
				if(ejjl.equals(FJFJCnt.jcjg0)){
					return true;
				}else if(ejjl.equals(FJFJCnt.jcjg2) && !sjjl.equals(FJFJCnt.jcjg5)){
					return true;
				}
				
				/**2、更新故障明细信息(库房和状态)*/
				Hashtable<String, String> rec = getKFHWXX(conn, xmlb, wlbh, sx, pch, hwdm);
				int updateCount;
				if(rec == null){
					throw new RuntimeException("客服故障明细信息更新失败，请联系管理员！");
				}else{
					updateCount = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_GZMX,
							rec.get("CKDM"), rec.get("CKMC"), rec.get("QDM"), rec.get("DDM"), rec.get("KWDM"), hwdm, xmlb, wlbh, pch, gztm);
				}
				
				/**3、更新库存信息*/
				int n = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX_IN, sl, xmlb, wlbh, sx, pch, hwdm);
				if(n != 1 || updateCount != 1){
					throw new RuntimeException("库存更新失败，请联系管理员！");
				}
				
				/**4、插入待发货记录（子表）*/
				Hashtable<String, String> body = new Hashtable<String, String>();
				body.put("WLBH", wlbh);
				body.put("WLMC", wlmc);
				body.put("XH", xh);
				body.put("SX", sx);
				body.put("PCH", pch);
				body.put("SL", String.valueOf(sl));
				body.put("QSSL", String.valueOf(sl));
				body.put("HWDM", hwdm);
				body.put("CKDM", ckdm);
				body.put("JLBZ", FJFJCnt.jlbz1);//记录标识
				
				try {
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_S", body, bindid, uid);
				} catch (AWSSDKException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return true;
			}
		}, bindid);
		
		
		/**5、插入主表记录*/
		shipBiz.insertHead(conn, bindid, uid, FJFJCnt.jlbz1);
	}
	
	
	/**
	 * 获取客服货位信息
	 * @param conn
	 * @param xmlb
	 * @param wlbh
	 * @param sx
	 * @param hwdm
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> getKFHWXX(Connection conn, String xmlb, String wlbh, String sx, String pch, String hwdm) throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(FJFJCnt.QUERY_HWXX);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, wlbh, sx, pch, hwdm);
			while(rs.next()){
				rec.put("CKDM", StrUtil.returnStr(rs.getString(1)));
				rec.put("CKMC", StrUtil.returnStr(rs.getString(2)));
				rec.put("QDM", StrUtil.returnStr(rs.getString(3)));
				rec.put("DDM", StrUtil.returnStr(rs.getString(4)));
				rec.put("KWDM", StrUtil.returnStr(rs.getString(5)));
			}
		} finally{
			DBSql.close(null, ps, rs);
		}
		return rec;
	}
	
	
	/**
	 * 更新送修处理方式和属性
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setSXDealAndIsBack(Connection conn, final int bindid) throws SQLException{
		final int sxBindid = DAOUtil.getInt(conn, FJFJCnt.QUERY_SX_BINDID, bindid);//送修BINDID
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_XMLB, bindid));//项目类别
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//物料编号
				String gztm = StrUtil.returnStr(rs.getString("KFGZDM"));//故障条码
				String sx = StrUtil.returnStr(rs.getString("SX"));//属性
				String pch = StrUtil.returnStr(rs.getString("PCH"));//批次号
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));//货位代码
				String hwdm2 = StrUtil.returnStr(rs.getString("HWDM2"));//检测货位代码
				String ejjl = StrUtil.returnStr(rs.getString("EJJL"));//二检结论
				String sjjl = StrUtil.returnStr(rs.getString("SJJL"));//三检结论
				int id = rs.getInt("ID");//子表ID
				
				String clfs = "";
				String sx2 = "";//检测结果影响属性
				if(ejjl.equals(FJFJCnt.jcjg0)){//有故障
					clfs = FJFJCnt.clfs0;
					sx2 = FJFJCnt.sx0;
				}else if(ejjl.equals(FJFJCnt.jcjg1)){//无故障
					clfs = FJFJCnt.clfs1;
					sx2 = FJFJCnt.sx1;
				}else if(ejjl.equals(FJFJCnt.jcjg2) && sjjl.equals(FJFJCnt.jcjg5)){//工厂检测 && 无故障退回(有故障收入 || 无故障收入)
					clfs = FJFJCnt.clfs1;
					sx2 = FJFJCnt.sx1;
				}else if(ejjl.equals(FJFJCnt.jcjg2) && !sjjl.equals(FJFJCnt.jcjg5)){//工厂检测 && (有故障收入 || 无故障收入)
					clfs = FJFJCnt.clfs0;
					sx2 = FJFJCnt.sx0;
				}
				
				/**1、更新检测子表（是否返货）.*/
				if(clfs.equals(FJFJCnt.clfs1)){
					int m = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_S_SFFH, bindid, wlbh, gztm, sx);
					if(m != 1) throw new RuntimeException("检测子表【是否返货】更新失败！");
				}
				
				FJFJBiz.setAttribute(conn, sxBindid, clfs, sx, sx2, xmlb, wlbh, pch, hwdm, hwdm2, gztm, id, false);
				return true;
			}
		}, bindid);
	}
}



