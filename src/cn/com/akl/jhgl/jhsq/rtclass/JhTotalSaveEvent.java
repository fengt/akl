/**
 * @Title: jhTotalSaveEvent.java
 * @Package cn.com.akl.jhgl.jhsq.rtclass
 * @Description: TODO
 * Company:akl
 * @author huzhiyu
 * @date 2014-7-28 ����2:50:33
 * @version V1.0
 */
package cn.com.akl.jhgl.jhsq.rtclass;

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

/**
 * @author hzy
 * 
 */
public class JhTotalSaveEvent extends WorkFlowStepRTClassA {

	public JhTotalSaveEvent(UserContext uc) {
		super(uc);

		this.setVersion("1.0.0");
		this.setDescription("���ݽ������ͳ�ƽ�������ӱ�");
	}

	public JhTotalSaveEvent() {

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionsoft.loader.core.AWFClassLoaderCoreImpl#execute()
	 * 
	 * @desc ���������ܱ�����Զ���ֳɽ���ӱ�����
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() {
		// ���ݿ�����
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		int processInstanceId = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		// ��ý����ͷ������
		Hashtable<String, Object> hht = BOInstanceAPI.getInstance().getBOData("BO_AKL_JHDD_HEAD", processInstanceId);
		//ɾ���뱾���������ص�����������&�������������
		try {
			conn = DBSql.open();
			conn.setAutoCommit(false);// �ֹ��ύ����
			DBSql.executeUpdate(conn,"delete from BO_AKL_JHDD_BODY where JHDH  = '"+hht.get("JHDH")+"'");
			DBSql.executeUpdate(conn, "delete from BO_AKL_KC_SPPCSK where DDH = '"+hht.get("JHDH")+"'");
			// �ж� ��� go| ����return --0:����|1:���
			Vector<Hashtable<String, Object>> tvhts = BOInstanceAPI.getInstance().getBODatas("BO_JHDD_BODY_TOTAL", processInstanceId);
			//�����к�
			for (int i = 0; i < tvhts.size(); i++) {
				String key = (String) tvhts.get(i).get("WLBH");
				tvhts.get(i).put("HH", i+1);
				DBSql.executeUpdate(conn, "update BO_JHDD_BODY_TOTAL set dhsl=jhsl, HH = "+(i+1)+" where bindid = " +processInstanceId +" and wlbh = '" +key+"'");
			}
			//����ִֹͣ��
			if ("0".equals(hht.get("JHLB"))) {
				return true;
			}
			if(tvhts == null || tvhts.size() < 1 ){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�����������в����ڼ�¼",true);
				return false;
			}
			// �����ӱ����ݷ�����&�ͺ�&�������»�ȡ����
			Vector<Hashtable<String, Object>> bvhts = new Vector<Hashtable<String, Object>>();
			// ���·�װ
			Hashtable<String, Object> wlht = new Hashtable<String, Object>();
			for (int i = 0; i < tvhts.size(); i++) {
				String key = (String) tvhts.get(i).get("WLBM");
				// ��֤grid�����ظ�����
				if (wlht.contains(key)) {
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�ֶ����ϱ���ֵ�ڽ����������д����ظ���¼",true);
					return false;
				}
				tvhts.get(i).put("HH", i+1);
				DBSql.executeUpdate(conn, "update BO_JHDD_BODY_TOTAL set dhsl=jhsl,HH = "+(i+1)+" where bindid = " +processInstanceId +" and wlbh = '" +key+"'");
				wlht.put(key, tvhts.get(i));
			}
			int hh = 1;
			for (int i = 0; i < tvhts.size(); i++) {
				Hashtable<String, Object> bht = tvhts.get(i);
				// ���ݷ�װ&����
				Vector<Hashtable<String, Object>> vhts = null;
				
					vhts = this.getData(conn,rs,ps,hht,bht);
					if(vhts == null || vhts.size()< 1){
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���ϱ���ֵΪ"+bht.get("WLBH")+"�޿�����μ�¼��������",true);
						return false;
					}
				for (int j = 0; j < vhts.size(); j++) {
					vhts.get(j).put("HH", hh);
					bvhts.add(vhts.get(j));
					hh++;
				}
			}
			// �������ݷ�װ
			Vector<Hashtable<String, Object>> sks = new Vector<Hashtable<String, Object>>();
			Hashtable<String, Object> sk = null;
			for (int i = 0; i < bvhts.size(); i++) {
				sk = new Hashtable<String, Object>();
				Hashtable<String, Object> temp = bvhts.get(i);
				sk.put("PCH", temp.get("PCH"));
				sk.put("WLBH", temp.get("WLBH"));
				sk.put("DDH", temp.get("JHDH"));
				sk.put("SDSL", temp.get("JHSL"));
				sk.put("CKDM", hht.get("WFJHCKBH"));
				sks.add(sk);
			}
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_JHDD_BODY",bvhts, processInstanceId, this.getUserContext().getUID());
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_KC_SPPCSK",sks, processInstanceId, this.getUserContext().getUID());
			conn.commit();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		} finally {
			DBSql.close(conn, ps, rs);
		}
	}

	/**
	 * @throws SQLException
	 * @Title: getData
	 * @Description: ����������ȡ���ݣ�����װ
	 * @param @return
	 * @return Hashtable<String,Object>
	 * @throws
	 */
	private Vector<Hashtable<String, Object>> getData(Connection conn,ResultSet rs,PreparedStatement ps,
			Hashtable<String, Object> hht,Hashtable<String, Object> ht) throws SQLException {
		String sql = " select c.WLBH,c.WLMC,c.XH,a.PCH,a.KWSL - (case when (select SUM(d.SDSL) from BO_AKL_KC_SPPCSK d where a.WLBH = d.WLBH and a.PCH = d.PCH and a.CKDM = d.CKDM)"+ 
				 " is null "+
				 " then 0 "+
				 " else (select SUM(d.SDSL) from BO_AKL_KC_SPPCSK d where a.WLBH = d.WLBH and a.PCH = d.PCH and a.CKDM = d.CKDM) end) kysl," +
				 " b.DJ," +
				 " (select top 1 b.ZDCB from BO_AKL_JGGL b where a.WLBH = b.WLBH and " +
				 " (case when b.ZXRQ is NULL then '2000-01-01' else b.ZXRQ end) < GETDATE() order by b.CREATEDATE desc) as WSCGJ,c.SL,a.HWDM " +
				 " from BO_AKL_KC_KCMX_S a" +
				 " join BO_AKL_KC_KCHZ_P b on(a.PCH = b.PCH and a.WLBH = b.WLBH) " +
				 " join BO_AKL_WLXX c on(a.WLBH = c.WLBH and c.HZBM = '01065') " +
				//" left join BO_AKL_KC_SPPCSK d on(a.WLBH = d.WLBH and a.PCH = d.PCH and a.CKDM = d.CKDM) "
				 " where a.WLBH = ? and (a.KWSL - (case when (select SUM(d.SDSL) from BO_AKL_KC_SPPCSK d where a.WLBH = d.WLBH and a.PCH = d.PCH and a.CKDM = d.CKDM)"+
				 " is null " +
				 " then 0 "+
				 " else (select SUM(d.SDSL) from BO_AKL_KC_SPPCSK d where a.WLBH = d.WLBH and a.PCH = d.PCH and a.CKDM = d.CKDM) end)) > 0 and a.SX = '049088' and b.ZT ='042022' and a.CKDM = ?  order by a.CREATEDATE ";
		// ���ݿ�����
		Vector<Hashtable<String, Object>> hts = new Vector<Hashtable<String, Object>>();
			ps = conn.prepareStatement(sql);
			ps.setString(1, (String) ht.get("WLBH"));
			ps.setString(2, (String)hht.get("WFJHCKBH"));
			rs = ps.executeQuery();
			// ���ݷ�װ
			int jhsl = Integer.parseInt(ht.get("JHSL").toString());
			int n = jhsl;
			while (rs.next() && n > 0) {
				String pch = rs.getString(4);
				int pcsl = Integer.parseInt(rs.getString(5));
				BigDecimal dj = new BigDecimal(rs.getString(6));
				BigDecimal wscgj = new BigDecimal(rs.getString(7));
				BigDecimal sl = new BigDecimal(rs.getString(8));
				String hwdm = rs.getString(9);
				BigDecimal bg = sl.add(new BigDecimal(1));
				Hashtable<String, Object> htab = new Hashtable<String, Object>();
				htab.put("HH", ht.get("HH"));
				htab.put("JHDH", ht.get("JHDH"));
				htab.put("WLBH", ht.get("WLBH"));
				htab.put("WLMC", ht.get("WLMC"));
				htab.put("WLBM", ht.get("WLBM"));
				htab.put("BZ", ht.get("BZ"));
				// �ж� ��Ҫ�������εĻ�
				if (n - pcsl <= 0) {
					htab.put("JHSL", n);
					htab.put("DHSL", n);
					htab.put("PCSL", n);
					htab.put("JHWSCB", dj.multiply(new BigDecimal(n)));
					htab.put("JHHSZCB", dj.multiply(new BigDecimal(n)).multiply(bg));
					htab.put("PCH", pch);
					htab.put("PCCBJ", dj);
					htab.put("JHZXCB", wscgj);
					htab.put("SL", ht.get("SL"));
					htab.put("HWDM", hwdm);
					hts.add(htab);
					break;
				} else {
					n = n - pcsl;
					htab.put("JHSL", pcsl);
					htab.put("DHSL", pcsl);
					htab.put("PCSL", pcsl);
					htab.put("JHWSCB", dj.multiply(new BigDecimal(pcsl)));
					htab.put("JHHSZCB", dj.multiply(new BigDecimal(pcsl)).multiply(bg));
					htab.put("PCH", pch);
					htab.put("PCCBJ", dj);
					htab.put("JHZXCB", wscgj);
					htab.put("SL", ht.get("SL"));
					htab.put("HWDM", hwdm);
					hts.add(htab);
				}
			}
		return hts;
	}
}
