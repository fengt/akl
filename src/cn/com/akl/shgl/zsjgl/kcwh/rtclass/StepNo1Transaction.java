package cn.com.akl.shgl.zsjgl.kcwh.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

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
	
	private static final String QUERY_KCHZ = "SELECT * FROM BO_AKL_SHKC_P_TEMP WHERE BINDID=?";
	private static final String QUERY_KCMX = "SELECT * FROM BO_AKL_SHKC_S_TEMP WHERE BINDID=?";
	private static final String UPDATE_KCMX_KWSL = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)+? WHERE XMLB=? AND WLBH=? AND HWDM=? AND PCH=? AND SX=? AND ZT=?";//更新库存（数量）
	
	private UserContext uc;
	private Connection conn;
	public StepNo1Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("插入或更新库存明细和汇总。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			insertInventory(conn, bindid, uid);
			
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
        return true;
	}
	
	/**
	 * 库存插入或更新
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void insertInventory(Connection conn, final int bindid, final String uid) throws SQLException{
		
		//1、插入库存汇总
		DAOUtil.executeQueryForParser(conn, QUERY_KCHZ, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				insertKCHZ(conn, bindid, uid, rs);
				return true;
			}
		}, bindid);
		
		//2、插入库存明细
		DAOUtil.executeQueryForParser(conn, QUERY_KCMX, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				Hashtable<String, String> rec = new Hashtable<String, String>();
				String xmlb = StrUtil.returnStr(rs.getString("XMLB"));
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
				String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
				String gg = StrUtil.returnStr(rs.getString("GG"));
				String xh = StrUtil.returnStr(rs.getString("XH"));
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				String sx = StrUtil.returnStr(rs.getString("SX"));
				String ckdm = StrUtil.returnStr(rs.getString("CKDM"));
				String ckmc = StrUtil.returnStr(rs.getString("CKMC"));
				String qdm = StrUtil.returnStr(rs.getString("QDM"));
				String ddm = StrUtil.returnStr(rs.getString("DDM"));
				String kwdm = StrUtil.returnStr(rs.getString("KWDM"));
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));
				int kwsl = rs.getInt("KWSL");
				String zjm = StrUtil.returnStr(rs.getString("ZJM"));
				double dj = rs.getDouble("DJ");
				String bzq = StrUtil.returnStr(rs.getString("BZQ"));
				String fzsx = StrUtil.returnStr(rs.getString("FZSX"));
				String scrq = StrUtil.returnStr(rs.getString("SCRQ"));
				String jldw = StrUtil.returnStr(rs.getString("JLDW"));
				String zt = StrUtil.returnStr(rs.getString("ZT"));
				
				rec.put("XMLB", xmlb);//项目类别
				rec.put("WLBH", wlbh);//物料编号
				rec.put("WLMC", wlmc);//物料名称
				rec.put("GG", gg);
				rec.put("ZJM", zjm);
				rec.put("BZQ", bzq);
				rec.put("FZSX", fzsx);
				rec.put("SCRQ", scrq);
				rec.put("JLDW", jldw);
				rec.put("XH", xh);//型号
				rec.put("SX", sx);//属性
				rec.put("PCH", pch);//批次号
				rec.put("ZT", zt);//状态
				rec.put("KWSL", String.valueOf(kwsl));//库位数量
				rec.put("DJ", String.valueOf(dj));//单价
				
				rec.put("CKDM", ckdm);//客服仓库编码
				rec.put("CKMC", ckmc);//客服仓库名称
				rec.put("QDM", qdm);//区代码
				rec.put("DDM", ddm);//道代码
				rec.put("KWDM", kwdm);//库位代码
				rec.put("HWDM", hwdm);//货位代码
				
				try {
					/**1、插入或更新库存明细*/
					int n = DAOUtil.getInt(conn, SXCnt.QUERY_isExistKCMX, xmlb, wlbh, sx, hwdm, pch, zt);
					if(n == 0){
						BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", rec, bindid, uid);
					}else{
						int updateCount = DAOUtil.executeUpdate(conn, UPDATE_KCMX_KWSL, kwsl, xmlb, wlbh, hwdm, pch, sx, zt);
						if(updateCount != 1) throw new RuntimeException("库存明细更新失败！");
					}
				} catch (AWSSDKException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return true;
			}
		}, bindid);
		
	}
	
	public void insertKCHZ(Connection conn, int bindid, String uid, ResultSet rs) throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		String xmlb = StrUtil.returnStr(rs.getString("XMLB"));
		String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
		String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
		String gg = StrUtil.returnStr(rs.getString("GG"));
		String xh = StrUtil.returnStr(rs.getString("XH"));
		String pch = StrUtil.returnStr(rs.getString("PCH"));
		int rksl = rs.getInt("RKSL");
		rec.put("XMLB", xmlb);//项目类别
		rec.put("WLBH", wlbh);//物料编号
		rec.put("WLMC", wlmc);//物料名称
		rec.put("GG", gg);//规格
		rec.put("XH", xh);//型号
		rec.put("PCH", pch);//批次号
		rec.put("RKSL", String.valueOf(rksl));//入库数量
		rec.put("PCSL", String.valueOf(rksl));//批次数量
		
		try {
			int n = DAOUtil.getInt(conn, SXCnt.QUERY_isExistKCHZ, xmlb, wlbh, pch);
			if(n == 0){
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_P", rec, bindid, uid);//插入库存汇总
			}else{
				int updateCount = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCHZ, rksl, rksl, xmlb, wlbh, xh, pch);//更新库存汇总
				if(updateCount != 1) throw new RuntimeException("库存汇总更新失败！"); 
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
}
