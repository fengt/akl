package cn.com.akl.rmahpfh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

public class Update_JCCY extends WorkFlowStepRTClassA {

	public Update_JCCY() {
	}

	public Update_JCCY(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("У�鷴����Ϣ�ͺš������Ƿ��в��죬�в��첢��¼");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		if(tablename.equals("BO_AKL_WXB_RMAHPFH_HEAD")){
			//������Ϣ
			Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFK_BODY", bindid);
			Hashtable<String,Integer> xhsl = new Hashtable<String,Integer>();//��ŷ��������ͺ�����
			Set<String> xhlist = new HashSet<String>();//��Ż�Ʒ�����ͺ�
			Set<String> fkxhlist = new HashSet<String>();//��ŷ��������ͺ�
			
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection conn = DBSql.open();
			//�ж��ͺ������Ƿ��в��졢�в����¼��������
			String sql = "select XH,sum(SL) as ZS from BO_AKL_WXB_RMAHPFH_BODY where bindid = '"+bindid+"' group by XH";
			try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						String xh = rs.getString("XH") == null?"":rs.getString("XH");
						String zs = rs.getString("ZS") == null?"":rs.getString("ZS");
						boolean flag = false;
						int sl = Integer.parseInt(zs);
						xhlist.add(xh);//��Ʒ�����ͺż���
						Iterator it=vc.iterator();
						while(it.hasNext()){
							//��ȡ��ⵥ������
							Hashtable formData = (Hashtable) it.next();
							String fkxh = formData.get("XH") == null ?"":formData.get("XH").toString();//�ͺ�
							String fksl = formData.get("SL") == null ?"":formData.get("SL").toString();//����
							int fsl = Integer.parseInt(fksl);
							fkxhlist.add(fkxh);//�����ͺż���
							xhsl.put(fkxh, fsl);//�����ͺ���������
							if(xh.equals(fkxh)){
								flag = true;
								//ƥ�䷴�������Ϻ�
								int t = fsl - sl;//������
								String wlsql = "select WLBH from BO_AKL_WLXX where XH='"+xh+"' and HZBM='01065'";
								String wlh = DBSql.getString(wlsql, "WLBH");
								//���²�����
								String updatewl = "update BO_AKL_WXB_RMAFK_BODY set WLBH='"+wlh+"',CY="+t+" where bindid = '"+bindid+"' and XH='"+xh+"'";
								DBSql.executeUpdate(updatewl);
								//�����޶�����
								String updatexd = "update BO_AKL_WXB_RMAHPFH_BODY set XDSL="+fsl+" where bindid = '"+bindid+"' and XH='"+xh+"'";
								DBSql.executeUpdate(updatexd);
							}
						}
						if(!flag){
							//�����޶�����
							String updatexd = "update BO_AKL_WXB_RMAHPFH_BODY set XDSL=0 where bindid = '"+bindid+"' and XH='"+xh+"'";
							DBSql.executeUpdate(updatexd);
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "������ȱ�ٸ��ͺ�"+xh+"�Ĳ�Ʒ������!");
						}
					}
				}
				
				//�жϷ����ͺ��Ƿ���ࣨ��������û�����Ϻŵ��ͺ�Ϊ����������ͺţ��˴����������Ч��
				if(!fkxhlist.isEmpty()){
					Iterator<String> t = fkxhlist.iterator();
					while(t.hasNext()){
						String s1 = t.next();
						boolean flag = false;
						Iterator<String> it=xhlist.iterator();
						while(it.hasNext()){
							String s = it.next();
							if(s1.equals(s)){
								flag = true;
							}
						}
						if(!flag){
							//ƥ�䷴�������Ϻ�
							String wlsql = "select WLBH from BO_AKL_WLXX where XH='"+s1+"'";
							String wlh = DBSql.getString(wlsql, "WLBH");
							//���²�����
							String updatewl = "update BO_AKL_WXB_RMAFK_BODY set WLBH='"+wlh+"',CY="+xhsl.get(s1)+" where bindid = '"+bindid+"' and XH='"+s1+"'";
							DBSql.executeUpdate(updatewl);
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�����������ͺ�"+s1+"�Ĳ�Ʒ������!");
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				DBSql.close(conn, ps, rs);
			}
		}
		return true;
	}
}
