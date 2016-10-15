package cn.com.akl.dgkgl.xsck.qscy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA{
	// 查询签收差异单身信息
	private static final String QUERY_DGQSCY_S = "select CYSL, WLH from BO_AKL_DGCK_QSCY_S where bindid=?";
	// 查询签收差异父流程出库单身
	private static final String QUERY_DGCK_S = "select s.HWDM, s.WLBH, s.PCH, s.SFSL, s.SX, s.HWKYSL, s.DW, s.KCSL, s.YFSL, s.XH, s.CKMC, s.CKDM, s.QDM, s.DDM, s.KWDM, s.GG, s.WLMC, s.TJ, s.ZL, s.DDH, s.BZ, s.TBSL, s.bindid  from BO_BO_AKL_DGCK_S s, BO_BO_AKL_DGCK_P p where p.CKDH=? AND s.WLBH=? AND p.bindid = s.bindid";
	// 更新明细表库存
	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("第三节点办理事件，出现差异，将差异数量回写库存");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();
//		Hashtable<String,String> cyData = getParameter(PARAMETER_FORM_DATA).toHashtable();
		Hashtable<String,String> cyData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGCK_QSCY_P", bindid);//数据库读取单头信息

		int superbindid = getParameter(PARAMETER_PARENT_WORKFLOW_INSTANCE_ID).toInt();
		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "退回");
		String CYLX = cyData.get("CYLX");
		if(!th&&!CYLX.equals("052131")){
			Connection conn = null;
			PreparedStatement cybodyPs = null;
			PreparedStatement ckbodyPs = null;
			ResultSet cybobyReset = null;
			ResultSet ckbobyReset = null;
			String WLBH = "";
			int CYSL = 0;
			int SFSL = 0;
			List<Hashtable<String, String>> list = new ArrayList<Hashtable<String, String>>();
			try {
				conn = DAOUtil.openConnectionTransaction();
				cybodyPs = conn.prepareStatement(QUERY_DGQSCY_S);
				cybobyReset = DAOUtil.executeFillArgsAndQuery(conn, cybodyPs, bindid);
				while(cybobyReset.next()){
					WLBH = cybobyReset.getString("WLH")==null?"":cybobyReset.getString("WLH");
					CYSL = cybobyReset.getInt("CYSL");

					Hashtable<String, String> hashtable = new Hashtable<String, String>();
					hashtable.put("CYSL", String.valueOf(CYSL));
					hashtable.put("WLBH", WLBH);
					list.add(hashtable);

				}
				for(Hashtable<String, String> h : list){
					int cysl = Integer.parseInt(h.get("CYSL"));
					ckbodyPs = conn.prepareStatement(QUERY_DGCK_S);
					ckbobyReset = DAOUtil.executeFillArgsAndQuery(conn, ckbodyPs, cyData.get("CKDH"),h.get("WLBH"));
					boolean overFlag = true;
					WLBH = h.get("WLBH");
					while(overFlag&&ckbobyReset.next()){
						
						int ckbindid = ckbobyReset.getInt("bindid");
						SFSL = ckbobyReset.getInt("SFSL");
						cysl-=SFSL;
						String PCH = ckbobyReset.getString("PCH")==null?"":ckbobyReset.getString("PCH");
						String HWDM = ckbobyReset.getString("HWDM")==null?"":ckbobyReset.getString("HWDM");
						String SX = ckbobyReset.getString("SX")==null?"":ckbobyReset.getString("SX");
						// 预备转存入出库单身中
						Hashtable<String, String> ckhashtable = new Hashtable<String, String>();
//						ckhashtable.put("ISEND", "1");
						ckhashtable.put("CKDM", PrintUtil.parseNull(ckbobyReset.getString("CKDM")));
						ckhashtable.put("CKMC", PrintUtil.parseNull(ckbobyReset.getString("CKMC")));
						ckhashtable.put("QDM", PrintUtil.parseNull(ckbobyReset.getString("QDM")));
						ckhashtable.put("DDM", PrintUtil.parseNull(ckbobyReset.getString("DDM")));
						ckhashtable.put("KWDM", PrintUtil.parseNull(ckbobyReset.getString("KWDM")));
						ckhashtable.put("HWDM", PrintUtil.parseNull(ckbobyReset.getString("HWDM")));
						ckhashtable.put("PCH", PCH);
						ckhashtable.put("WLBH", h.get("WLBH"));
						ckhashtable.put("XH", PrintUtil.parseNull(ckbobyReset.getString("XH")));
						ckhashtable.put("GG", PrintUtil.parseNull(ckbobyReset.getString("GG")));
						ckhashtable.put("WLMC", PrintUtil.parseNull(ckbobyReset.getString("WLMC")));
						ckhashtable.put("DW", PrintUtil.parseNull(ckbobyReset.getString("DW")));
						ckhashtable.put("HWKYSL", String.valueOf(ckbobyReset.getInt("HWKYSL")));
						ckhashtable.put("KCSL", String.valueOf(ckbobyReset.getInt("KCSL")));
						ckhashtable.put("SX", SX);
						ckhashtable.put("TJ", String.valueOf(ckbobyReset.getInt("TJ")));
						ckhashtable.put("ZL", String.valueOf(ckbobyReset.getInt("ZL")));
						ckhashtable.put("YFSL", String.valueOf(ckbobyReset.getInt("YFSL")));
						ckhashtable.put("TBSL", String.valueOf(ckbobyReset.getInt("TBSL")));
						ckhashtable.put("BZ", PrintUtil.parseNull(ckbobyReset.getString("BZ")));
						ckhashtable.put("DDH", PrintUtil.parseNull(ckbobyReset.getString("DDH")));
						if(cysl<=0){
							// 更新库存明细
							if(0==DAOUtil.executeUpdate(conn, "Update BO_AKL_DGKC_KCMX_S Set KWSL=KWSL+? WHERE WLBH=? AND PCH=? AND HWDM=? AND SX=?", SFSL+cysl, h.get("WLBH"), PCH, HWDM, SX)){
								MessageQueue.getInstance().putMessage(getUserContext().getUID(), "批次号："+PCH+"物料号："+h.get("WLBH")+"货位代码："+HWDM+"反更新明细库存失败", true);
								return false;
							}
							// 更新库存汇总
							if(0==DAOUtil.executeUpdate(conn, "Update BO_AKL_DGKC_KCHZ_P Set PCSL=PCSL+?, CKSL=CKSL-? WHERE WLBH=? AND PCH=? AND ZT='042022' AND CKSL>=?", SFSL+cysl, SFSL+cysl, h.get("WLBH"), PCH, SFSL+cysl)){
								MessageQueue.getInstance().putMessage(getUserContext().getUID(), "批次号："+PCH+"物料号："+h.get("WLBH")+"反更新汇总库存失败", true);
								return false;
							}

							ckhashtable.put("SFSL", String.valueOf(0-(SFSL+cysl)));
							// 插入数据
							BOInstanceAPI.getInstance().createBOData(conn, "BO_BO_AKL_DGCK_S", ckhashtable, ckbindid, uid);
							//结束
							overFlag = false;
						}
						else{
							// 更新库存明细
							if(0==DAOUtil.executeUpdate(conn, "Update BO_AKL_DGKC_KCMX_S Set KWSL=KWSL+? WHERE WLBH=? AND PCH=? AND HWDM=? AND SX=?", SFSL, h.get("WLBH"), PCH, HWDM, SX)){
								MessageQueue.getInstance().putMessage(getUserContext().getUID(), "批次号："+PCH+"物料号："+h.get("WLBH")+"货位代码："+HWDM+"反更新明细库存失败", true);
								return false;
							}
							// 更新库存汇总
							if(0==DAOUtil.executeUpdate(conn, "Update BO_AKL_DGKC_KCHZ_P Set PCSL=PCSL+?, CKSL=CKSL-? WHERE WLBH=? AND PCH=? AND ZT='042022' AND CKSL>=?", SFSL, SFSL, h.get("WLBH"), PCH, SFSL)){
								MessageQueue.getInstance().putMessage(getUserContext().getUID(), "批次号："+PCH+"物料号："+h.get("WLBH")+"反更新汇总库存失败", true);
								return false;
							}
							
							ckhashtable.put("SFSL", String.valueOf(0-(SFSL)));
							// 插入数据
							BOInstanceAPI.getInstance().createBOData(conn, "BO_BO_AKL_DGCK_S", ckhashtable, ckbindid, uid);
						}
					}
				}
				conn.commit();
			} catch(RuntimeException e){
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			} catch(Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，无法出库，请检查控制台", true);
				return false;
			} finally {
				DBSql.close(conn, cybodyPs, cybobyReset);
				DBSql.close(conn, ckbodyPs, ckbobyReset);
			}

		}
		return true;
	}

}
