package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGRK_FormLoad extends WorkFlowStepRTClassA {

	public DGRK_FormLoad() {
	}

	public DGRK_FormLoad(UserContext arg0) {
		super(arg0);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("�����غ�ƥ�䵥��������Ϣ�����������ݿ�");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable rkdtData = this.getParameter(PARAMETER_FORM_DATA).toHashtable();//����ȡ��ͷ��Ϣ
		Hashtable rkData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);//���ݿ��ȡ��ͷ��Ϣ
		Vector v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);//���ݿ��ȡ������Ϣ
		//�ӱ���ȡ��ⵥͷ��Ϣ
		String rq = rkdtData.get("RQ") == null ?"":rkdtData.get("RQ").toString();//����
		rq = rq.substring(rq.length()-10);
		String rkdh = rkdtData.get("RKDH") == null ?"":rkdtData.get("RKDH").toString();//��ⵥ��
		rkdh = rkdh.substring(rkdh.length()-15);
//		String ydh = rkdtData.get("YDH") == null ?"":rkdtData.get("YDH").toString();//�ɹ�����
//		ydh = ydh.substring(ydh.indexOf("value="), ydh.indexOf("value=")+22);
//		ydh = ydh.substring(ydh.length()-15);
//		String khbh = rkdtData.get("KHBH") == null ?"":rkdtData.get("KHBH").toString();//�ͻ����
//		khbh = khbh.substring(khbh.indexOf("value="),khbh.indexOf("value=")+12);
//		khbh = khbh.substring(khbh.length()-5);
//		System.out.println(khbh);
//		System.out.println(ydh);
//		System.out.println(rq);
//		System.out.println(rkdh);
		//�����ݿ��ȡ��ͷ��Ϣ
		String rkdh2 = rkData.get("RKDH") == null ?"":rkData.get("RKDH").toString();//��ⵥ��
		if(rkdh2.equals("")){
			if(v != null){
				Connection conn = DBSql.open();
				try {
					//���ݿ��ѯ�����̲ɹ����š��ͻ����
					String sql = "select ydh,khbh,khbmbm from BO_AKL_DGRK_P where bindid='"+bindid+"'";
					String ydh = DBSql.getString(sql, "ydh"); //�ɹ�����
					String khbh = DBSql.getString(sql, "khbh"); //�ͻ����
					String bmbh = DBSql.getString(sql, "khbmbm"); //�ͻ����ű��
					//ѡ����Ϊ��
					if(ydh == null || ydh.trim().length()==0){
						return true;
					}
					//���ݿ��ѯ�ɹ������ϼ�
					String sql2 = "select slhj from BO_AKL_DGCG_P where ddbh='"+ydh+"'";
					int ysslhj = DBSql.getInt(sql2, "slhj"); //�ɹ������ϼ�
					rkdtData.put("SSSLHJ", ysslhj);//�ɹ������ϼ���ӵ�ʵ�������ϼ�
					//���µ�ͷ��ⵥ�š�����
					String dtsql = "update BO_AKL_DGRK_P set RKDH='"+rkdh+"',RQ='"+rq+"' where bindid='"+bindid+"' and YDH='"+ydh+"'";
					DBSql.executeUpdate(conn,dtsql);
					//���µ���ʵ�����������ԡ�������ɹ����ڡ���λ
					Iterator t = v.iterator();
					while(t.hasNext()){
						Hashtable formData = (Hashtable) t.next();
						String wlbh = formData.get("WLBH") == null ?"":formData.get("WLBH").toString().trim();//���ϱ��
						String yssl = formData.get("YSSL") == null ?"":formData.get("YSSL").toString().trim();//Ӧ������
						
						//��ȡ��λ��ϵ��
						String ckdm = "";
						String ckmc = "";
						String qdm = "";
						String ddm = "";
						String kwdm = "";
						String hwdm = "";
						Hashtable<String, String> hwdmTable= getHWDM(conn, wlbh, bmbh);
						if(hwdmTable != null){
							ckdm = hwdmTable.get("ckdm").toString();//�ֿ����
							ckmc = hwdmTable.get("ckmc").toString();//�ֿ�����
							qdm = hwdmTable.get("qdm").toString();//������
							ddm = hwdmTable.get("ddm").toString();//������
							kwdm = hwdmTable.get("kwdm").toString();//��λ����
							hwdm = hwdmTable.get("kwbh").toString();//��λ����
						}
						String dssql = "update BO_AKL_DGRK_S set SSSL='"+yssl+"',SX='049088',SCHCGRQ='"+rq+"',CKDM='"+ckdm+"',CKMC='"+ckmc+"',QDM='"+qdm+"',DDM='"+ddm+"',KWDM='"+kwdm+"',HWDM='"+hwdm+"' WHERE BINDID='"+bindid+"' AND CGDH='"+ydh+"' AND WLBH='"+wlbh+"'";
						DBSql.executeUpdate(conn, dssql);
						/*//��ѯ���϶�Ӧ��λ����
						String hwsql = "SELECT c.CKDM,d.CKMC,c.QDM,c.DDM,c.KWDM,c.KWBH FROM BO_AKL_WLKWGXB c,(SELECT MAX(ID) as id, WLBH FROM BO_AKL_WLKWGXB GROUP BY WLBH) b,BO_AKL_CK d WHERE c.id=b.id AND c.WLBH=b.WLBH and c.CKDM=d.CKDM and c.SSKHBM='"+khbh+"' and c.WLBH='"+wlbh+"'";
						String ckdm = parseNull(DBSql.getString(hwsql, "CKDM"));//�ֿ����
						String ckmc = parseNull(DBSql.getString(hwsql, "CKMC"));//�ֿ�����
						String qdm = parseNull(DBSql.getString(hwsql, "QDM"));//������
						String ddm = parseNull(DBSql.getString(hwsql, "DDM"));//������
						String kwdm = parseNull(DBSql.getString(hwsql, "KWDM"));//��λ����
						String hwdm = parseNull(DBSql.getString(hwsql, "KWBH"));//��λ����
*/						//���µ���ʵ�����������ԡ�������ɹ����ڡ���λ
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					DBSql.close(conn, null, null);
				}
			}
		}
		return true;
	}

	/**
	 * ��ȡ��λ��Ϣ���в��ŵ����ȣ�
	 * @param conn
	 * @param wlbh
	 * @param bmbh
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> getHWDM(Connection conn, String wlbh, String bmbh) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> rec = null;
		String sql = "SELECT c.CKDM, d.CKMC, c.QDM, c.DDM, c.KWDM, c.KWBH FROM BO_AKL_WLKWGXB c, ( SELECT MAX (ID) AS ID FROM BO_AKL_WLKWGXB WHERE SFYX = '��' AND WLBH='"+wlbh+"' AND BMBH ='"+bmbh+"' GROUP BY WLBH ) b, BO_AKL_CK d WHERE c.ID = b.ID AND c.CKDM = d.CKDM";
		String sql2 = "SELECT c.CKDM, d.CKMC, c.QDM, c.DDM, c.KWDM, c.KWBH FROM BO_AKL_WLKWGXB c, ( SELECT MAX (ID) AS ID FROM BO_AKL_WLKWGXB WHERE SFYX = '��' AND WLBH='"+wlbh+"' GROUP BY WLBH ) b, BO_AKL_CK d WHERE c.ID = b.ID AND c.CKDM = d.CKDM";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs.next()){
				rec = new Hashtable<String, String>();
				String ckdm = StrUtil.returnStr(rs.getString("CKDM"));
				String ckmc = StrUtil.returnStr(rs.getString("CKMC"));
				String qdm = StrUtil.returnStr(rs.getString("QDM"));
				String ddm = StrUtil.returnStr(rs.getString("DDM"));
				String kwdm = StrUtil.returnStr(rs.getString("KWDM"));
				String kwbh = StrUtil.returnStr(rs.getString("KWBH"));
				rec.put("ckdm", ckdm);
				rec.put("ckmc", ckmc);
				rec.put("qdm", qdm);
				rec.put("ddm", ddm);
				rec.put("kwdm", kwdm);
				rec.put("kwbh", kwbh);
			}else{
				ps = conn.prepareStatement(sql2);
				rs = ps.executeQuery();
				while(rs.next()){
					rec = new Hashtable<String, String>();
					String ckdm = StrUtil.returnStr(rs.getString("CKDM"));
					String ckmc = StrUtil.returnStr(rs.getString("CKMC"));
					String qdm = StrUtil.returnStr(rs.getString("QDM"));
					String ddm = StrUtil.returnStr(rs.getString("DDM"));
					String kwdm = StrUtil.returnStr(rs.getString("KWDM"));
					String kwbh = StrUtil.returnStr(rs.getString("KWBH"));
					rec.put("ckdm", ckdm);
					rec.put("ckmc", ckmc);
					rec.put("qdm", qdm);
					rec.put("ddm", ddm);
					rec.put("kwdm", kwdm);
					rec.put("kwbh", kwbh);
				}
			}
		} finally{
			DBSql.close(ps, rs);
		}
		return rec;
	}
	
	public String parseNull(String str){
		return str == null?"":str;
	}
}
