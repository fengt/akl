package cn.com.akl.dgkgl.xsck.qscy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.SubWorkflowEventClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGCK_qscy_Sub extends SubWorkflowEventClassA{
	//��ѯǩ������¼�뵥��
	private static final String QUERY_DGQSD_S = "select a.WLH, a.YSSL, a.SSSL, a.XH, b.TJ from BO_AKL_DGCK_QSSL_S a left join BO_AKL_WLXX b on a.WLH = b.WLBH where a.bindid=? AND a.YSSL<>a.SSSL";
	
	public DGCK_qscy_Sub(UserContext uc){
		super(uc);
		setProvider("liusong");
		setVersion("1.0.0");
		setDescription("�������������������̱���������");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_PARENT_PROCESS_INSTANCE_ID).toInt();// ����bindid
		Hashtable hbindid = getParameter(PARAMETER_SUB_PROCESS_INSTANCE_ID).toHashtable();
		int subBindid =  Integer.parseInt(hbindid.get(0).toString());// ����bindid
		Connection conn = null;
		Statement stat = null;
		ResultSet rs = null;
		PreparedStatement qsbodyPs = null;
		ResultSet qsbobyReset = null;
		String sql = null;
		try {
			conn = DBSql.open();
			conn.setAutoCommit(false);
			stat = conn.createStatement();
			Hashtable<String, String> hashtablep = new Hashtable<String, String>();
			// ��ѯ���쵥�ĳ��ⵥ��
			sql = "select top 1 CKDH from BO_AKL_DGCK_QSSL_S where bindid="+bindid;
			String CKDH = DBSql.getString(sql, "CKDH");// ���ⵥ��
			// ��ѯ�˵���ͷ��ȡǩ�ղ��쵥��ͷ��Ϣ
			sql = "SELECT SHDW, SHR, SHRDH, SFZ, DZ, CYS, LXR, LXFS, YSFS, DDH, CKDH FROM BO_AKL_YD_P WHERE CKDH ='"+CKDH+"'";
			rs = stat.executeQuery(sql);
			if(rs.next()){
				CKDH = rs.getString("CKDH") == null?"":rs.getString("CKDH");// ���ⵥ��
				String DDH = rs.getString("DDH") == null?"":rs.getString("DDH");// ���۶�����
				String SHDW = rs.getString("SHDW") == null?"":rs.getString("SHDW");// �ջ���λ
				String SHR = rs.getString("SHR") == null?"":rs.getString("SHR");// �ջ�������
				String SHRLXFS = rs.getString("SHRDH") == null?"":rs.getString("SHRDH");// �ջ��˵绰
				String SFZ = rs.getString("SFZ") == null?"":rs.getString("SFZ");// ʼ��վ
				String MDZ = rs.getString("DZ") == null?"":rs.getString("DZ");// Ŀ��վ
				String CYS = rs.getString("CYS") == null?"":rs.getString("CYS");// ������
				String CYSLXR = rs.getString("LXR") == null?"":rs.getString("LXR");// ��������ϵ��
				String CYSLXFS = rs.getString("LXFS") == null?"":rs.getString("LXFS");// ��������ϵ��ʽ
				String YSFS = rs.getString("YSFS") == null?"":rs.getString("YSFS");// ���䷽ʽ
				
				hashtablep.put("CKDH", CKDH);
				hashtablep.put("DDH", DDH);
				hashtablep.put("SHDW", SHDW);
				hashtablep.put("SHR", SHR);
				hashtablep.put("SHRLXFS", SHRLXFS);
				hashtablep.put("SFZ", SFZ);
				hashtablep.put("MDZ", MDZ);
				hashtablep.put("CYS", CYS);
				hashtablep.put("CYSLXR", CYSLXR);
				hashtablep.put("CYSLXFS", CYSLXFS);
				hashtablep.put("YSFS", YSFS);
			}
			else{
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�˳��ⵥ���˵���Ϣ", true);
				return false;
			}
			// ��ǩ�յ���ѯ��������
			sql = "select SHRQ from BO_AKL_QSD_P where CKDH = '"+CKDH+"'";
			String FHRQ = DBSql.getString(sql, "SHRQ")==null?"":DBSql.getString(sql, "SHRQ");// ��������
			hashtablep.put("FHRQ", FHRQ);
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_DGCK_QSCY_P", subBindid);
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DGCK_QSCY_P", hashtablep, subBindid, getUserContext().getUID());
			
			//���뵥����Ϣ
			qsbodyPs = conn.prepareStatement(QUERY_DGQSD_S);
			qsbobyReset = DAOUtil.executeFillArgsAndQuery(conn, qsbodyPs, bindid);
			while(qsbobyReset.next()){
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("CYXH", PrintUtil.parseNull(qsbobyReset.getString("XH")));
				hashtable.put("WLH", PrintUtil.parseNull(qsbobyReset.getString("WLH")));
				hashtable.put("SL", String.valueOf(qsbobyReset.getInt("YSSL")));
				hashtable.put("QSSL", String.valueOf(qsbobyReset.getInt("SSSL")));
				hashtable.put("TJ", String.valueOf(qsbobyReset.getInt("TJ")));
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DGCK_QSCY_S", hashtable, subBindid, getUserContext().getUID());
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
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���޷����⣬�������̨", true);
			return false;
		} finally {
			DBSql.close(conn, stat, rs);
			DBSql.close(null, qsbodyPs, qsbobyReset);
		}
		
		return true;
	}

}
