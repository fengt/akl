/**
 * 还货出库时，根据还货单身汇总表（BO_AKL_HHDD_BODY_TOTAL）保存后自动拆分成还货单单身表（BO_AKL_HHDD_BODY）并保存
 * @author Qjc
 *v1.0
 */
package cn.com.akl.hhgl.hhsj.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;


import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;


public class HhTotalSaveEvent extends WorkFlowStepRTClassA {

	public HhTotalSaveEvent(UserContext uc) {
		super(uc);

		this.setVersion("1.0.0");
		this.setDescription("根据还货汇总统计还货批次子表");
	}

	public HhTotalSaveEvent() {

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionsoft.loader.core.AWFClassLoaderCoreImpl#execute()
	 * 
	 * @desc 还货单身汇总表保存后自动拆分成还货子表并保存
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() {
		// 数据库连接
		Connection conn = null;
		ResultSet rs=null;
		PreparedStatement ps = null;
		int processInstanceId = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		// 获得还货单头表数据
		Hashtable<String, Object> hht = BOInstanceAPI.getInstance().getBOData("BO_AKL_HHDD_HEAD", processInstanceId);
		String ckdm = (String) hht.get("WFCKBH");
		try {
		// 判断 借出 go| 借入return --0:入|1:借出
			if ("1".equals(hht.get("HHLB"))) {
				//删掉与本还货单号相关单身批次数据&库存锁定表数据
				
					conn = DBSql.open();
					conn.setAutoCommit(false);// 手工提交事务
					DBSql.executeUpdate(conn,"delete from BO_AKL_HHDD_BODY where HHDH  = '"+hht.get("HHDH")+"'");
					DBSql.executeUpdate(conn, "delete from BO_AKL_KC_SPPCSK where DDH = '"+hht.get("HHDH+")+"'");
				// 判断 还出 go| 还入return --0:还入|1:还出
				
			}else if ("0".equals(hht.get("HHLB"))) {
				return true;
			}
			Vector<Hashtable<String, Object>> tvhts = BOInstanceAPI.getInstance().getBODatas("BO_AKL_HHDD_BODY_TOTAL", processInstanceId);
			 
			if(tvhts == null || tvhts.size() < 1 ){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "还货单单身中不存在记录",true);
				return false;
			}
			
			
			// 根据子表数据分批次&型号&数量重新获取数据
			Vector<Hashtable<String, Object>> bvhts = new Vector<Hashtable<String, Object>>();
			// 重新封装
			Hashtable<String, Object> wlht = new Hashtable<String, Object>();
			for (int i = 0; i < tvhts.size(); i++) {
				String key = (String) tvhts.get(i).get("HWLBM");
				// 验证grid中无重复物料
				if (wlht.contains(key)) {
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "字段物料编码值在还货单身汇总中存在重复记录",true);
					return false;
				}
				wlht.put(key, tvhts.get(i));
			}
			for (int i = 0; i < tvhts.size(); i++) {
				Hashtable<String, Object> bht = tvhts.get(i);
				// 数据封装&保存
				if("0".equals(bht.get("HHLB")))
				{
				Vector<Hashtable<String, Object>> vhts = null;
					vhts = this.getData(conn,rs,ps,ckdm,bht);
					if(vhts == null || vhts.size()< 1){
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "物料编码值为"+bht.get("WLBH")+"无库存批次记录，请检查库存",true);
						return false;
					}
				for (int j = 0; j < vhts.size(); j++) {
					bvhts.add(vhts.get(j));
				}}
			}
			// 锁库数据封装
			Vector<Hashtable<String, Object>> sks = new Vector<Hashtable<String, Object>>();
			Hashtable<String, Object> sk = null;
			for (int i = 0; i < bvhts.size(); i++) {
				sk = new Hashtable<String, Object>();
				Hashtable<String, Object> temp = bvhts.get(i);
				if("0".equals(temp.get("HHLB")))
				{
				sk.put("PCH", temp.get("PCH"));
				sk.put("WLBH", temp.get("WLBH"));
				sk.put("DDH", temp.get("HHDH"));
				sk.put("SDSL", temp.get("HHSL"));
				sk.put("CKDM", hht.get("WFCKBH"));
				sks.add(sk);}
			}
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_KC_SPPCSK",sks, processInstanceId, this.getUserContext().getUID());
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_HHDD_BODY",bvhts, processInstanceId, this.getUserContext().getUID());
				conn.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally {
			DBSql.close(conn, ps, rs);
		}

		return true;

	}

	/**
	 * @throws SQLException
	 * @Title: getData
	 * @Description: 根据条件获取数据，并封装
	 * @param @return
	 * @return Hashtable<String,Object>
	 * @throws
	 */
	private Vector<Hashtable<String, Object>> getData(Connection conn,ResultSet rs,PreparedStatement ps,String ckdm,
			Hashtable<String, Object> ht) throws SQLException {
		String sql = " select c.WLBH,c.WLMC,c.XH,a.PCH,a.KWSL - (case when (select SUM(d.SDSL) from BO_AKL_KC_SPPCSK d where a.WLBH = d.WLBH and a.PCH = d.PCH and a.CKDM = d.CKDM)"+ 
				 " is null "+
				 " then 0 "+
				 " else (select SUM(d.SDSL) from BO_AKL_KC_SPPCSK d where a.WLBH = d.WLBH and a.PCH = d.PCH and a.CKDM = d.CKDM) end) pcsl," +
				 " b.DJ," +
				 "  ISNULL((select top 1 b.ZDCB from BO_AKL_JGGL b where a.WLBH = b.WLBH and " +
				 " (case when b.ZXRQ is NULL then '2000-01-01' else b.ZXRQ end) < GETDATE() order by b.CREATEDATE desc),0) as WSCGJ,c.SL,a.HWDM " +
				 " from BO_AKL_KC_KCMX_S a" +
				 " join BO_AKL_KC_KCHZ_P b on(a.PCH = b.PCH and a.WLBH = b.WLBH) " +
				 " join BO_AKL_WLXX c on(a.WLBH = c.WLBH and c.HZBM = '01065') " +
				//" left join BO_AKL_KC_SPPCSK d on(a.WLBH = d.WLBH and a.PCH = d.PCH and a.CKDM = d.CKDM) "
				 " where a.WLBH = ? and a.KWSL > 0 AND [SX]='049088' and b.ZT ='042022' and a.ckdm='"+ckdm+"' order by a.CREATEDATE ";
		Vector<Hashtable<String, Object>> hts = new Vector<Hashtable<String, Object>>();
			conn = DBSql.open();
			ps = conn.prepareStatement(sql);
			ps.setString(1, (String) ht.get("HWLBH"));
			rs = ps.executeQuery();
			// 数据封装
			int hhsl = Integer.parseInt(ht.get("HHSL").toString());
			int n = hhsl;
			while (rs.next() && n > 0) {
				String pch = rs.getString("pch");//批次号
				int pcsl = Integer.parseInt(rs.getString("pcsl"));//批次
				BigDecimal dj = new BigDecimal(rs.getString("dj"));//批次价
				BigDecimal wscgj = new BigDecimal(rs.getString("wscgj"));
				String hwdm =rs.getString("HWDM");
				BigDecimal sl = new BigDecimal(rs.getString("sl"));
				BigDecimal bg = sl.add(new BigDecimal(1));
				Hashtable<String, Object> htab = new Hashtable<String, Object>();
				htab.put("HH", ht.get("HH"));
				htab.put("JHDHH", ht.get("JHDHH"));
				htab.put("HHDH", ht.get("HHDH"));
				htab.put("WLBH", ht.get("WLBH"));
				htab.put("WLMC", ht.get("WLMC"));
				htab.put("WLBM", ht.get("WLBM"));
				htab.put("HHLB", ht.get("HHLB"));
				//htab.put("HHZT", ht.get("HHZT"));
				htab.put("BZ", ht.get("BZ"));
				// 判断 需要几个批次的货
				if (n - pcsl < 0) {
					htab.put("PCH", pch);
					htab.put("PCCBJ", ht.get("PCCBJ"));
					htab.put("JHZXCB", ht.get("JHZXCB"));
					htab.put("JHSL", ht.get("JHSL"));
					htab.put("YHHSL", ht.get("YHHSL"));
					htab.put("HHZXCB",ht.get("HHZXCB") );
					htab.put("RKCB",  new BigDecimal(ht.get("PCCBJ").toString()).subtract(new BigDecimal(ht.get("SJCE").toString())));
					htab.put("HHSL", n);
					htab.put("CE",ht.get("CE"));
					htab.put("SJCE", ht.get("SJCE"));
					htab.put("HHWSJE", wscgj.multiply(new BigDecimal(n)) );
					htab.put("HHHSJE", wscgj.multiply(new BigDecimal(n)).multiply(bg));
					htab.put("CJZE",new BigDecimal(n).multiply(new BigDecimal(ht.get("SJCE").toString())));	
					htab.put("SL", ht.get("SL"));
					htab.put("HWBM", hwdm);
					hts.add(htab);
					break;
				} else {
					n = n - pcsl;
					htab.put("PCH", pch);
					htab.put("PCCBJ", ht.get("PCCBJ"));
					htab.put("JHZXCB", ht.get("JHZXCB"));
					htab.put("JHSL", ht.get("JHSL"));
					htab.put("YHHSL", ht.get("YHHSL"));
					htab.put("HHZXCB",ht.get("HHZXCB") );
					htab.put("RKCB", new BigDecimal(ht.get("PCCBJ").toString()).subtract(new BigDecimal(ht.get("SJCE").toString())));
					htab.put("HHSL", pcsl);
					htab.put("CE", ht.get("CE"));
					htab.put("SJCE",ht.get("SJCE"));
					htab.put("HHWSJE",wscgj.multiply(new BigDecimal(pcsl)) );
					htab.put("HHHSJE", wscgj.multiply(new BigDecimal(pcsl)).multiply(bg));
					htab.put("CJZE",new BigDecimal(pcsl).multiply(new BigDecimal(ht.get("SJCE").toString())));	
					htab.put("SL", ht.get("SL"));
					htab.put("HWBM", hwdm);
					hts.add(htab);
				}
			}
	
		return hts;
	}
}
