package cn.com.akl.shgl.sx.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.shgl.sx.biz.KCBiz;
import cn.com.akl.shgl.sx.biz.SXBiz;
import cn.com.akl.shgl.sx.biz.SXHandle;
import cn.com.akl.shgl.sx.biz.ShipmentsBiz;
import cn.com.akl.shgl.sx.biz.TimeoutBiz;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {
	
	private UserContext uc;
	private Connection conn;
	private SXBiz sxBiz = new SXBiz();
	private KCBiz kcBiz = new KCBiz();
	private SXHandle sxHandle = new SXHandle();
	public StepNo1Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("插入待检品库存或扣减代用品库存。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> hashtable = BOInstanceAPI.getInstance().getBOData("BO_AKL_SX_P", bindid);
		String uid = uc.getUID();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			String sfdyp = DAOUtil.getString(conn, SXCnt.QUERY_isDYP, bindid);
			String sxfs = DAOUtil.getString(conn, SXCnt.QUERY_SXFS, bindid);
			String ywlx = DAOUtil.getString(conn, SXCnt.QUERY_YWLX, bindid);
			
			if(ywlx.equals(SXCnt.ywlx0) || ywlx.equals(SXCnt.ywlx1)){//赠送|销售
				sxHandle.setSXStatus(conn, bindid);//更新送修单状态
				sxHandle.setSXRowNum(conn, bindid, ywlx);//更新送修单行号
			}else{
				/**1、插入库存. **/
				insertInventory(conn, bindid, uid);
				if(sxfs.equals(SXCnt.sxfs)){
					ShipmentsBiz.insertShipments(conn, bindid, uid);//邮寄送修，插入待发货记录
				}
			}
			
			/**2、扣减代用品库存. **/
			if(sfdyp.equals(SXCnt.is)){
				sxBiz.decreaseDYP(conn, bindid);//扣减代用品库存
				DAOUtil.executeUpdate(conn, SXCnt.UPDATE_DYP_SFYKKC, bindid);//更新代用品'是否已扣库存'
			}
			
			conn.commit();
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
		
		/**
		 * 启动送修单超时.
		 */
		try {
            TimeoutBiz.startSXTimeout(bindid, uid, hashtable);
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
        }

        return true;
	}
	
	/**
	 * 库存插入或更新（除库存汇总外）
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void insertInventory(Connection conn, final int bindid, final String uid) throws SQLException{
		final String ckdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_CKDM, bindid));//客服仓库编码
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMLB, bindid));//项目类别
		final String ywlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_YWLX, bindid));//业务类型
		final String pch = RepositoryBiz.getPCH(conn, xmlb);//生成批次号
		sxHandle.setFieldPCH(conn, pch, bindid);//更新送修子表批次号
		
		//1、插入库存汇总
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_SXHZ, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				kcBiz.insertKCHZ(conn, bindid, uid, rs, xmlb, pch);
				return true;
			}
		}, bindid);
		
		//2、插入库存明细(属性为待检品)
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_SXMX, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				Hashtable<String, String> body = kcBiz.getKCMX(conn, rs, ckdm, xmlb, ywlx, pch, SXCnt.sx2);//第一节点入库都为待检品
				insertKCMXForStepOne(conn, bindid, uid, body, xmlb);
				return true;
			}
		}, bindid);
		
	}
	
	/**
	 * 插入库存明细
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param body
	 */
	public void insertKCMXForStepOne(Connection conn, int bindid, String uid, Hashtable<String, String> body, String xmlb) throws SQLException{
		String wlbh = body.get("WLBH");
		String sx = body.get("SX");
		String pch = body.get("PCH");
		String hwdm = body.get("HWDM");
		int sl = Integer.parseInt(body.get("KWSL"));
		try {
			/**1、插入或更新库存明细*/
			int n = DAOUtil.getInt(conn, SXCnt.QUERY_isExistKCMX, xmlb, wlbh, sx, hwdm, pch, SXCnt.zt0);
			if(n == 0){
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", body, bindid, uid);
			}else{
				int updateCount = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCMX_KWSL, sl, xmlb, wlbh, hwdm, pch, sx);
				if(updateCount != 1) throw new RuntimeException("库存明细更新失败！");
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
}
