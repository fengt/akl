package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DateUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;

public class Update_CGZT extends WorkFlowStepRTClassA {

	public Update_CGZT() {
	}

	public Update_CGZT(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("�������κţ����²ɹ�������ͷ�Ĳɹ�״̬����������ܱ�");
	}

	@Override
	public boolean execute() {
		//�������κ�
		Date now = new Date();
		String pch1 = DateUtil.dateToLongStrBys2(now);//ǰ׺���磺20140724
		String pch = pch1 + judgeRKRQ(pch1);
				
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		//��ȡ��ⵥͷ��Ϣ
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);
		String rkdh = rkdtData.get("RKDH") == null ?"":rkdtData.get("RKDH").toString();//��ⵥ��
		String ydh = rkdtData.get("YDH") == null ?"":rkdtData.get("YDH").toString();//�ɹ�����
		String rklx = rkdtData.get("RKLX") == null ?"":rkdtData.get("RKLX").toString();//�������
		
		Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);
		Vector v1 = new Vector();//������
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement bindps = null;
		ResultSet bindrs = null;
		Connection conn = DBSql.open();
		try {
			Iterator t = vc.iterator();
			while(t.hasNext()){
				//��ȡ��ⵥ������
				Hashtable formData = (Hashtable) t.next();
				String wlbh = formData.get("WLBH") == null ?"":formData.get("WLBH").toString();//���ϱ��
				String wlmc = formData.get("WLMC") == null ?"":formData.get("WLMC").toString();//��������
				String xh = formData.get("XH") == null ?"":formData.get("XH").toString();//�ͺ�
				String dw = formData.get("DW") == null ?"":formData.get("DW").toString();//��λ
				String yssl = formData.get("YSSL") == null ?"":formData.get("YSSL").toString();//Ӧ������
				String sssl = formData.get("SSSL") == null ?"":formData.get("SSSL").toString();//ʵ������
				int sl1 = Integer.parseInt(yssl);//Ӧ������
				int sl2 = Integer.parseInt(sssl);//ʵ������
				
				//��ӿ���������
				Hashtable recordData1 = new Hashtable();
				recordData1.put("PCH", pch);//���κ�
				recordData1.put("WLBH", wlbh);//���ϱ��
				recordData1.put("WLMC", wlmc);//��������
				recordData1.put("XH", xh);//�ͺ�
				recordData1.put("DW", dw);//��λ
				recordData1.put("RKSL", sl2);//�������
				recordData1.put("PCSL", sl1);//��������
				recordData1.put("RKDH", rkdh);//��ⵥ��
				recordData1.put("ZT", "042023");//״̬��;
				v1.add(recordData1);
				//���������Ͳ���������⣬���²ɹ�����ɹ�״̬
				if(!rklx.equals("�������")){
					String cgssql = "update BO_AKL_DGCG_S set CGZT='�����' where DDBH='"+ydh+"' and xh='"+xh+"' and dw='"+dw+"'";
					DBSql.executeUpdate(conn,cgssql);
				}
			}
			//������ⵥ�����κ�
			String rksql = "update BO_AKL_DGRK_S set PCH='"+pch+"' where bindid='"+bindid+"'";
			DBSql.executeUpdate(conn,rksql);
			//������ܱ�
			BOInstanceAPI.getInstance().removeProcessInstanceBOData("BO_AKL_DGKC_KCHZ_P", bindid);
			BOInstanceAPI.getInstance().createBOData("BO_AKL_DGKC_KCHZ_P", v1, bindid, this.getUserContext().getUID());
			//���������Ͳ���������⣬���²ɹ���ͷ�ɹ�״̬��ɾ�����������������
			if(!rklx.equals("�������")){
				//���²ɹ���ͷ�ɹ�״̬
				String sql = "select CGZT from BO_AKL_DGCG_S where DDBH='"+ydh+"'";
				boolean flag = true;
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						String cgzt = rs.getString("CGZT") == null?"":rs.getString("CGZT");
						if("���ɹ�".equals(cgzt)){
							flag = false;
						}
					}
					if(flag){
						String cgpsql = "update BO_AKL_DGCG_P set CGZT='�����' where DDBH='"+ydh+"'";
						DBSql.executeUpdate(conn,cgpsql);
					}
				}
				//ɾ�����������������
				String bindsql = "select bindid from BO_AKL_DGRK_P where ydh='"+ydh+"' AND ISEND=0";
				bindps = conn.prepareStatement(bindsql);
				bindrs = bindps.executeQuery();
				if(bindrs != null){
					while(bindrs.next()){
						String bind = bindrs.getString("bindid") == null?"":bindrs.getString("bindid");
						int instanceid = Integer.parseInt(bind);
						if(instanceid != bindid){
							WorkflowInstanceAPI.getInstance().removeProcessInstance(instanceid);
						}
					}
				}
			}
			return true;
		} catch (Exception e) {
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "����ʧ�ܣ���֪ͨ����Ա");
			e.printStackTrace(System.err);
			return false;
		} finally {
			DBSql.close(ps, rs);
			DBSql.close(conn, bindps, bindrs);
		}
	}
	
	/**
	 * �ж��Ƿ���ڵ�ǰ������ڵ����κţ���������ܱ����Ѵ��ڵ�ǰ������ڣ������κţ���׺��λ�����ۼӣ����򣬵�ǰ�������+001
	 * @param pch1
	 * @return
	 */
	private  String judgeRKRQ(String pch1){
		String sql = "select max(SUBSTRING(PCH,9,3)) pch2 from BO_AKL_DGKC_KCHZ_P where SUBSTRING(PCH,1,8) = '" + pch1 + "'";
		String pch2 = "";
		pch2 = StrUtil.returnStr(DBSql.getString(sql, "pch2"));
		if(StrUtil.isNotNull(pch2)){
			return String.format("%03d", Integer.parseInt(pch2)+1);
		}
		return "001";
	}
}
