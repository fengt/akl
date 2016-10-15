package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class Validate_SHYS extends WorkFlowStepRTClassA {

	public Validate_SHYS() {
	}

	public Validate_SHYS(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("У��ʵ�������Ƿ�С�ڵ���Ӧ��������У�����������Ƿ��ظ���У�鵥�������Ƿ��Ѱ���");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		//��ȡ��ͷ��Ϣ
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);
		String cgdh = rkdtData.get("YDH") == null ?"":rkdtData.get("YDH").toString();//�ɹ�����
		String rklx = rkdtData.get("RKLX") == null ?"":rkdtData.get("RKLX").toString();//�������
		//������Ͳ���������⣬ѡ���Ų���Ϊ��
		if(!rklx.equals("�������")){
			if(cgdh.equals("")){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "ѡ����Ϊ�գ���ѡ�񵥺�!");
				return false;
			}
		}
		//��ȡ������Ϣ
		Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);
		if(vc == null){
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��ⵥ����Ϣ����Ϊ�գ�����");
			return false;
		}
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = DBSql.open();
		try {
			Iterator t = vc.iterator();
			while(t.hasNext()){
				//��ȡ��ⵥ������
				Hashtable formData = (Hashtable) t.next();
//				String cgdh = formData.get("CGDH") == null ?"":formData.get("CGDH").toString();//�ɹ�����
				String wlbh = formData.get("WLBH") == null ?"":formData.get("WLBH").toString();//���ϱ��
				String wlmc = formData.get("WLMC") == null ?"":formData.get("WLMC").toString();//��������
				String xh = formData.get("XH") == null ?"":formData.get("XH").toString();//�ͺ�
				String dw = formData.get("DW") == null ?"":formData.get("DW").toString();//��λ
				String yssl = formData.get("YSSL") == null ?"":formData.get("YSSL").toString();//Ӧ������
				String sssl = formData.get("SSSL") == null ?"":formData.get("SSSL").toString();//ʵ������
				int sl1 = Integer.parseInt(yssl);//Ӧ������
				int sl2 = Integer.parseInt(sssl);//ʵ������
				String dwsql = "select XLMC from BO_AKL_DATA_DICT_S where DLBM='026' and XLBM='"+dw+"'";
				String dwmc = DBSql.getString(dwsql, "XLMC");
				if(sl1 != sl2){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�ͺ�"+xh+"����λ"+dwmc
							+"��ʵ������"+sl2+"��Ӧ������"+sl1+"�����,����!");
					return false;
				}
			}
			//�жϵ������������Ƿ��ظ�
			String sql = "select XH,DW from BO_AKL_DGRK_S where bindid = '"+bindid+"' group by XH,DW having count(*) > 1";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					String xh = rs.getString("XH") == null?"":rs.getString("XH");
					String dw = rs.getString("DW") == null?"":rs.getString("DW");
					String dwsql = "select XLMC from BO_AKL_DATA_DICT_S where DLBM='026' and XLBM='"+dw+"'";
					String dwmc = DBSql.getString(dwsql, "XLMC");
					if(!"".equals(xh) ){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��ⵥ������ظ��������ݣ��ͺ�"+xh
								+",��λ"+dwmc+"������!");
						return false;
					}
				}
			}
			//���������Ͳ���������⣬�жϵ��������Ƿ��Ѱ���
			if(!rklx.equals("�������")){
				String ztsql = "select CGZT from BO_AKL_DGCG_P where DDBH='"+cgdh+"'";
				String cgzt = DBSql.getString(ztsql, "CGZT");
				if(!cgzt.equals("���ɹ�")){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��ⵥ�������Ѱ���!");
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, ps, rs);
		}
		return false;
	}
}
