package cn.com.akl.rmahpfh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class Validate_XDSL extends WorkFlowStepRTClassA {

	public Validate_XDSL() {}

	public Validate_XDSL(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("��֤���������Ƿ�Ϊ�գ��������������Ƿ�����������ͺ��Ƿ��ظ�");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable recordData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAHPFH_HEAD", bindid);
		String pp = recordData.get("PP") == null ?"":recordData.get("PP").toString();//Ʒ��
		String qsrq = recordData.get("QSRQ") == null ?"":recordData.get("QSRQ").toString();//��ʼ����
		String jsrq = recordData.get("JSRQ") == null ?"":recordData.get("JSRQ").toString();//��������
		Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAHPFH_BODY", bindid);
		if(vc == null){
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��Ʒ����������Ϣ����Ϊ�գ��뵼�룡");
			return false;
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = DBSql.open();
		try {
			Iterator t = vc.iterator();
			while(t.hasNext()){
				Hashtable formData = (Hashtable) t.next();
//				String shdh = formData.get("RMASHDH") == null ?"":formData.get("RMASHDH").toString();//�ջ�����
//				String wlbh = formData.get("WLBH") == null ?"":formData.get("WLBH").toString();//���ϱ��
				String xh = formData.get("XH") == null ?"":formData.get("XH").toString();//�ͺ�
				String sl = formData.get("SL") == null ?"":formData.get("SL").toString();//����
				int sl1 = Integer.parseInt(sl);//����
				String sql = "SELECT SUM (KWSL) AS SL FROM BO_AKL_RMA_KCMX a,BO_AKL_WXB_XS_RMASH_P b WHERE a.KWSL != 0 AND b.ISEND = 1 AND LX IN ('��Ʒ����','�����Ѵ���') AND a.XH = '"+xh+"' AND a.DDH = b.DJBH AND b.PP = '"+pp+"' AND b.UPDATEDATE >= '"+qsrq+"' AND b.UPDATEDATE < '"+jsrq+"'";
				int kwsl = DBSql.getInt(sql, "SL");//��λ����
				/*if(sl1 == 0){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��Ʒ��������Ʒ��������Ϊ0����������");
					return false;
				}*/
				if(kwsl != sl1){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�ͺš�"+xh+"����������"+sl1+"������������"+kwsl+"�������������棬ɾ����������¼�룡");
					return false;
				}
			}
			
			//�жϻ�Ʒ���������ͺ��Ƿ��ظ�
			String sql = "select XH from BO_AKL_WXB_RMAHPFH_BODY where bindid = '"+bindid+"' group by XH having count(*)>1";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					//String wlbh = rs.getString("WLBH") == null?"":rs.getString("WLBH");
					String xh = rs.getString("XH") == null?"":rs.getString("XH");
					if(!"".equals(xh)){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��Ʒ����������д����ظ��ͺ�"+xh+"������!");
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, ps, rs);
		}
		return true;
	}
}
