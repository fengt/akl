package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGRK_DRDS extends WorkFlowStepRTClassA {

	public DGRK_DRDS() {
	}

	public DGRK_DRDS(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("���ݵ��Ŵ��뵥������");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		if(tablename.equals("BO_AKL_DGRK_P")){
			Hashtable rkdtData = this.getParameter(PARAMETER_FORM_DATA).toHashtable();
			Vector v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);
			//��ȡ��ⵥͷ��Ϣ
			String ydh = rkdtData.get("YDH") == null ?"":rkdtData.get("YDH").toString();//�ɹ�����
			String khbh = rkdtData.get("KHBH") == null ?"":rkdtData.get("KHBH").toString();//�ͻ����
			String bmbh = rkdtData.get("KHBMBM") == null ?"":rkdtData.get("KHBMBM").toString();//���ű��
			Vector vc =new Vector();
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection conn = DBSql.open();
			try {
				//ѡ����Ϊ��
				if(ydh == null || ydh.trim().length()==0){
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_DGRK_S", bindid); 
					return true;
				}
				//��ⵥ�����ݵĲɹ�����
				String ddhsql = "SELECT distinct CGDH FROM BO_AKL_DGRK_S WHERE BINDID="+bindid+"";
				String ddh = DBSql.getString(ddhsql, "CGDH");
				if(ydh.equals(ddh)){
					return true;
				}
				//ƥ�����ϻ�λ����
//				String sql = "SELECT b.ID,b.DDBH,b.WLBH,b.WLMC,b.GG,b.XH,CGSL,YRKSL,b.DW,b.CGRQ,c.CKDM,c.CKMC,c.QDM,c.DDM,c.KWDM,c.KWBH from (select b.ID,b.DDBH,b.WLBH,b.WLMC,b.GG,b.XH,CGSL,YRKSL,b.DW,a.CGRQ FROM BO_AKL_DGCG_S b,BO_AKL_DGCG_P a where a.DDBH=b.DDBH and a.DDBH='"+ydh+"' and a.CGZT='���ɹ�' and b.CGZT='���ɹ�') b LEFT JOIN(SELECT c.WLBH,c.CKDM,d.CKMC,c.QDM,c.DDM,c.KWDM,c.KWBH FROM BO_AKL_WLKWGXB c,(SELECT MAX(ID) as id, WLBH FROM BO_AKL_WLKWGXB GROUP BY WLBH) b,BO_AKL_CK d WHERE c.id=b.id AND c.WLBH=b.WLBH and c.CKDM=d.CKDM and c.SSKHBM='"+khbh+"') c on b.WLBH=c.WLBH";
				String sql = "SELECT b.ID, b.DDBH, b.WLBH, b.WLMC, b.GG, b.XH, CGSL, YRKSL, b.DW, a.CGRQ FROM BO_AKL_DGCG_S b, BO_AKL_DGCG_P a WHERE a.DDBH = b.DDBH AND a.DDBH = '"+ydh+"' AND a.CGZT = '���ɹ�' AND b.CGZT = '���ɹ�'";
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						Hashtable recordData = new Hashtable();
						//��ȡ�ɹ������
//						String hh = rs.getString("HH") == null ?"":rs.getString("HH").toString();//�к�
						String cgdh = rs.getString("DDBH") == null ?"":rs.getString("DDBH").toString();//�ɹ�����
						String wlbh = rs.getString("WLBH") == null ?"":rs.getString("WLBH").toString();//���ϱ��
						String wlmc = rs.getString("WLMC") == null ?"":rs.getString("WLMC").toString();//��������
						String gg = rs.getString("GG") == null ?"":rs.getString("GG").toString();//���
						String xh = rs.getString("XH") == null ?"":rs.getString("XH").toString();//�ͺ�
						int cgsl = rs.getInt("CGSL");//�ɹ�����
						int yrksl = rs.getInt("YRKSL");//���������
						String dw = rs.getString("DW") == null ?"":rs.getString("DW").toString();//������λ
						String cgrq = rs.getString("CGRQ") == null ?"":rs.getString("CGRQ").toString();//�ɹ�����
						
						//������ⵥ���
//						recordData.put("HH", hh);//�к�
						recordData.put("CGDH", cgdh);//�ɹ�����
						recordData.put("WLBH", wlbh);//���ϱ��
						recordData.put("WLMC", wlmc);//��������
						recordData.put("GG", gg);//���
						recordData.put("XH", xh);//�ͺ�
						recordData.put("YSSL", cgsl-yrksl);//Ӧ������
						recordData.put("SSSL", cgsl-yrksl);//ʵ������
						recordData.put("DW", dw);//������λ
						recordData.put("SCHCGRQ", cgrq);//�ɹ�����
						
						//��ȡ��λ��ϵ��
						Hashtable<String, String> hwdmTable= getHWDM(conn, wlbh, bmbh);
						if(hwdmTable != null){
							recordData.put("CKDM", hwdmTable.get("ckdm").toString());//�ֿ����
							recordData.put("CKMC", hwdmTable.get("ckmc").toString());//�ֿ�����
							recordData.put("QDM", hwdmTable.get("qdm").toString());//������
							recordData.put("DDM", hwdmTable.get("ddm").toString());//������
							recordData.put("KWDM", hwdmTable.get("kwdm").toString());//��λ����
							recordData.put("HWDM", hwdmTable.get("kwbh").toString());//��λ����
						}
						recordData.put("SX", "049088");//��λ����
						vc.add(recordData);
					}
					//ɾ������
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_DGRK_S", bindid); 
					//�������ݿ�
					BOInstanceAPI.getInstance().createBOData("BO_AKL_DGRK_S", vc, this.getParameter(PARAMETER_INSTANCE_ID).toInt(),  this.getUserContext().getUID());
				}else{
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��ǰ�ɹ���Ϊ�գ�����!");
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
	
	
}
