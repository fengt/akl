/**
 * 修改为保存后事件，已在aws后台捆绑
 * 2014/10/09 15:54
 * hzy
 */
package cn.com.akl.jhhgl.ck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * @author hzy
 *
 */
public class StepNo1FormAfterSave extends WorkFlowStepRTClassA{

	public StepNo1FormAfterSave() {
		super();
	}

	public StepNo1FormAfterSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("借还货申请单身保存后事件: 选择单号，自动加载单身信息");
	}
	/* (non-Javadoc)
	 * @see com.actionsoft.loader.core.AWFClassLoaderCoreImpl#execute()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() {
		String tb = getParameter(PARAMETER_TABLE_NAME).toString();
		if(!tb.equals("BO_AKL_CKD_HEAD")){
			return false;
		}
		
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		//getParameter(PARAMETER_FORM_DATA).toHashtable();
		Connection conn = null;
		PreparedStatement ps =null;
		ResultSet rs = null;
		try{
			conn= DBSql.open();
			//删除已存在的单身数据
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_CKD_BODY", bindid);
			Hashtable<String, Object> ht = getParameter(PARAMETER_FORM_DATA).toHashtable();
			String jhhdh = ht.get("JHHDH").toString();
			Vector<Hashtable<String,Object>> vh = this.packageDSDatas(conn, ps, rs, jhhdh);
			BOInstanceAPI.getInstance().createBOData("BO_AKL_CKD_BODY", vh, bindid, this.getUserContext().getUID());
		}catch(Exception ex){
			ex.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "系统后台出错，请联系管理员!", true);
			return false;
		}finally{
			DBSql.close(conn, ps, rs);
		}
		return true;
	}
	/**
	 * @param conn
	 * @param ps
	 * @param rs
	 * @param jhhdh
	 * @return
	 * @throws Exception
	 * @author hzy
	 * @desc 从后台获取数据，自动填充
	 */
	private Vector<Hashtable<String,Object>> packageDSDatas(Connection conn,PreparedStatement ps,ResultSet rs,String jhhdh)throws Exception{
		Vector<Hashtable<String,Object>> vh = new Vector<Hashtable<String,Object>>();
		String sql = "select *  from (select a.id ,a.JHDH as sign,a.WLBH,a.WLMC,a.WLBM,b.WFJHCKBH,b.WFJHCK,c.DW,c.PPID,a.PCH,a.JHSL,  ((select d.PCSL from BO_AKL_KC_KCHZ_P d where a.PCH = d.PCH and d.WLBH = a.WLBH)-  ISNULL((SELECT sum(case when e.SDSL is null then 0 else e.SDSL end) FROM BO_AKL_KC_SPPCSK e  where e.WLBH = a.WLBH and e.pch = a.PCH and e.DDH<>b.JHDH and e.WLBH <>a.WLBH group by e.WLBH,e.pch),0)) as KCSL,  a.HWDM from BO_AKL_JHDD_BODY a JOIN BO_AKL_JHDD_HEAD b ON(a.jhdh = b.jhdh)  join BO_AKL_WLXX c on(a.WLBH = c.WLBH)  UNION ALL  SELECT a.id,a.HHDH as sign,a.WLBH,a.WLMC,a.WLBM,b.WFCKBH,b.WFCKMZ,c.DW,c.PPID,a.PCH,a.HHSL,  ((select d.PCSL from BO_AKL_KC_KCHZ_P d where a.PCH = d.PCH and d.WLBH = a.WLBH)-  ISNULL((SELECT sum(case when e.SDSL is null then 0 else e.SDSL end) FROM BO_AKL_KC_SPPCSK e  where e.WLBH = a.WLBH and e.pch = a.PCH and e.DDH<>b.JHDH and e.WLBH <>a.WLBH group by e.WLBH,e.pch),0)) as KCSL,  a.HWBM  FROM BO_AKL_HHDD_BODY a JOIN BO_AKL_HHDD_HEAD b ON(a.hhdh = b.hhdh)  join BO_AKL_WLXX c on(a.WLBH = c.WLBH)) t  WHERE 1=1 AND t.sign= ? ";
		ps = conn.prepareStatement(sql);
		ps.setString(1,jhhdh);
		rs = ps.executeQuery();
		while(rs.next()){
			//获取数据
			String sign = rs.getString("sign");
			String wlbh = rs.getString("WLBH");
			String wlmc = rs.getString("WLMC");
			String wlbm = rs.getString("WLBM");
			String wfjhckbh = rs.getString("WFJHCKBH");
			String wfjhck = rs.getString("WFJHCK");
			String dw = rs.getString("DW");
			String ppid = rs.getString("PPID");
			String pch = rs.getString("PCH");
			String jhsl = rs.getString("JHSL");
			String kcsl = rs.getString("KCSL");
			String hwdm = rs.getString("HWDM");
			
			//重新封装
			Hashtable<String,Object> h = new Hashtable<String, Object>();
			h.put("JHHDH", sign);
			h.put("WLH", wlbh);
			h.put("WLMC", wlmc);
			h.put("XH", wlbm);
			h.put("FHKFBH", wfjhckbh);
			h.put("FHKFMC", wfjhck);
			h.put("JLDW", dw);
			h.put("PP", ppid);
			h.put("PC", pch);
			h.put("SL", jhsl);
			h.put("KCSL", kcsl);
			h.put("KWBH", hwdm);
			//-----------------------
			h.put("SJSL",jhsl);
			vh.add(h);
		}
		return vh;
	}	
}

