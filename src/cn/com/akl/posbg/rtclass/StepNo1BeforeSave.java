package cn.com.akl.posbg.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA{
	//��ѯpos��Ŷ�Ӧ�ĵ�����Ϣ
	private static final String queryPOSDH= "select * from BO_AKL_WXB_XS_POS_BODY where POSBH = ?";
	public StepNo1BeforeSave(UserContext uc){
		super(uc);
		setVersion("1.0.0");
		setDescription("����ǰ�¼���ѡ��pos��ź��Զ����뵥������");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		Integer bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		String uid = getUserContext().getUID();
		Hashtable<String,String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		Hashtable<String,String> data = BOInstanceAPI.getInstance().getBOData("BO_AKL_POS_BG_P", bindid);//���ݿ��ȡ��ͷ��Ϣ


		if(tablename.equals("BO_AKL_POS_BG_P")){
			Connection conn = null;
			try {
				conn = DAOUtil.openConnectionTransaction();
				String posbh = hashtable.get("POSBH");
				String posbhdata = data.get("POSBH");
				data.get("");
				if(!posbh.equals(posbhdata)){
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_POS_BG_S", bindid);
					query_insertDS(conn, bindid, uid, posbh);
				}
				conn.commit();
			}catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			} finally {
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}
	
	public void query_insertDS(Connection conn, Integer bindid, String uid, String posbh) throws Exception{
		PreparedStatement ps = conn.prepareStatement(queryPOSDH);
		ResultSet rs = DAOUtil.executeFillArgsAndQuery(conn, ps, posbh);
		try{
			while(rs.next()){
				posbh = parseStringorNull(posbh);// POS���
				String tpm = parseStringorNull(rs.getString("TPM"));// ��Ӧ��POS���(TPM��)
				String wlbh = parseStringorNull(rs.getString("WLBH"));// ���ϱ��
				String wlmc = parseStringorNull(rs.getString("WLMC"));// ��������
				String xh = parseStringorNull(rs.getString("XH"));// �ͺ�
				String gg = parseStringorNull(rs.getString("GG"));// ���
				Integer possl = rs.getInt("POSSL");// POS����
				String currency = parseStringorNull(rs.getString("CURRENCY"));// ����
				Double posdj = rs.getDouble("POSDJ");// POS����
				Double yjg = rs.getDouble("YJG");// ԭ�۸�
				Double zchjjbhs = rs.getDouble("ZCHJJBHS");// ֧�ֺ󾻼�(δ˰)
				Double zchjjhs = rs.getDouble("ZCHJJHS");// ֧�ֺ󾻼�(��˰)
				Double sl = rs.getDouble("SL");// ˰��
				String zt = parseStringorNull(rs.getString("ZT"));// ״̬
				Integer ysysl = rs.getInt("YSYSL");// ��ʹ������
				
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("POSBH",posbh);
				hashtable.put("TPM",tpm);
				hashtable.put("WLBH",wlbh);
				hashtable.put("WLMC",wlmc);
				hashtable.put("XH",xh);
				hashtable.put("GG",gg);
				hashtable.put("POSSL",String.valueOf(possl));
				hashtable.put("CURRENCY",currency);
				hashtable.put("POSDJ",String.valueOf(posdj));
				hashtable.put("YJG",String.valueOf(yjg));
				hashtable.put("ZCHJJBHS",String.valueOf(zchjjbhs));
				hashtable.put("ZCHJJHS",String.valueOf(zchjjhs));
				hashtable.put("SL",String.valueOf(sl));
				hashtable.put("ZT",gg);
				hashtable.put("YSYSL",String.valueOf(ysysl));
				
				// ԭPOS����
				hashtable.put("YPOSDJ",String.valueOf(posdj));
				// ԭ��ʹ������
				hashtable.put("YYSYSL",String.valueOf(ysysl));
				// ԭ����
				hashtable.put("YPOSSL", String.valueOf(possl));
				
				// ��������
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_POS_BG_S", hashtable, bindid, uid);
				
				
			}
		}finally{
			DBSql.close(null, ps, rs);
		}
	}
	public String parseStringorNull(String parse){
		return parse==null?"":parse;
	}
}
