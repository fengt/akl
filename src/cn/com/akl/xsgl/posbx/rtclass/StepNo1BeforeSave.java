package cn.com.akl.xsgl.posbx.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	private static final String QUERY_POSBH = "SELECT POSBH FROM BO_AKL_POSBX_P WHERE BINDID=?";
	
	private static final String QUERY_POSBX = "SELECT POSBH,WLBH,XH,POSSL,YSYSL,YJG,POSDJ,YJG-POSDJ AS JJ,YSYSL*POSDJ AS YPPOSZE "
			+ "FROM BO_AKL_WXB_XS_POS_BODY WHERE POSBH=? AND POSSL-YSYSL > =0 AND ZT='032001'";
	
	public StepNo1BeforeSave() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("����POS��Ŵ�����Ҫ���������ݡ�");
		
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		Hashtable<String, String> fromHead = this.getParameter(PARAMETER_FORM_DATA).toHashtable();
		Vector<Hashtable<String, String>> body = BOInstanceAPI.getInstance().getBODatas("BO_AKL_POSBX_S", bindid);
		
		String sqdh = fromHead.get("SQDH") == null ?"":fromHead.get("SQDH").toString();//POS���뵥��
		String posbh = fromHead.get("POSBH") == null ? "" : fromHead.get("POSBH").toString();//POS���
		
		if("BO_AKL_POSBX_P".equals(tablename)){
			Vector<Hashtable<String, String>> receiveBody = new Vector<Hashtable<String, String>>();
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			try{
				conn = DBSql.open();
				if(posbh == null || posbh.trim().length() == 0){
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_POSBX_S", bindid);
					return true;
				}
				
				String posbh2 = DAOUtil.getString(conn, QUERY_POSBH, bindid);
				if(posbh.equals(posbh2)){
					return true;
				}
				
				ps = conn.prepareStatement(QUERY_POSBX);
				rs = DAOUtil.executeFillArgsAndQuery(conn, ps, posbh);
				if(rs != null){
					while(rs.next()){
						String wlbh = rs.getString("WLBH") == null ?"":rs.getString("WLBH").toString();//���ϱ��
						String xh = rs.getString("XH") == null ?"":rs.getString("XH").toString();//�ͺ�
						int possl = rs.getInt("POSSL");//����Ԥ������
						int sqsl = rs.getInt("YSYSL");//��������
						double dj = rs.getDouble("YJG");//����
						double posdj = rs.getDouble("POSDJ");//POS����
						double jj = rs.getDouble("JJ");//����
						double ypposze = rs.getDouble("YPPOSZE");//Ԥ��POS�ܶ�
						
						Hashtable<String, String> rec = new Hashtable<String, String>();
						rec.put("WLBH", wlbh);
						rec.put("SDSKU", xh);
						rec.put("SDYPSL", String.valueOf(possl));
						rec.put("SQSL", String.valueOf(sqsl));
						rec.put("DJ", String.valueOf(dj));
						rec.put("POSPRICE", String.valueOf(posdj));
						rec.put("JJ", String.valueOf(jj));
						rec.put("YPPOSZE", String.valueOf(ypposze));
						rec.put("SQDH", sqdh);
						rec.put("SPPOSZE", String.valueOf(ypposze));
						
						receiveBody.add(rec);
					}
					//ɾ����������
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_POSBX_S", bindid);
					//��������
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_POSBX_S", receiveBody, bindid, this.getUserContext().getUID());
				}else{
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��"+posbh+"���û��Ҫ���������ݣ����飡");
					return false;
				}
				return true;
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				DBSql.close(conn, ps, rs);
			}
		}
		return true;
	}

}
