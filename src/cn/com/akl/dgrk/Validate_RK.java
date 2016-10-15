package cn.com.akl.dgrk;

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
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

/**
 * �÷����ѷ�������StepNo3Validateȡ��
 * @author Administrator
 *
 */
public class Validate_RK extends WorkFlowStepRTClassA {

	public Validate_RK() {
	}

	public Validate_RK(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("У�����Ƿ���ȷ��У���Ƿ���Ҫ���кż����к��Ƿ���ȫ����ȷ����");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		
		if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "���")){
			Set<String> wlh = new HashSet<String>();//��ǰ�������ϱ�ż���
			boolean flag = false;//�Ƿ���Ҫ���к�
			Set<String> wlxlh = new HashSet<String>();//��Ҫ���кŵ����ϱ��
			Set<String> wlxlh2 = new HashSet<String>();//��Ҫ���������кŵ����ϱ��
			Hashtable wlsl = new Hashtable();//���Ϻŵ�������Ϣ
			
			//��ȡ��ⵥͷ��Ϣ
			Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);
			String rkdh = rkdtData.get("RKDH") == null ?"":rkdtData.get("RKDH").toString();//��ⵥ��
			String khbh = rkdtData.get("KHBH") == null ?"":rkdtData.get("KHBH").toString();//�ͻ����
			String rklx = rkdtData.get("RKLX") == null ?"":rkdtData.get("RKLX").toString();//�������
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
					String wlbh = formData.get("WLBH") == null ?"":formData.get("WLBH").toString();//���ϱ��
					//String wlmc = formData.get("WLMC") == null ?"":formData.get("WLMC").toString();//��������
					String xh = formData.get("XH") == null ?"":formData.get("XH").toString();//�ͺ�
					String dw = formData.get("DW") == null ?"":formData.get("DW").toString();//��λ
					String hwdm = formData.get("HWDM") == null ?"":formData.get("HWDM").toString();//��λ����

					String dwsql = "select XLMC from BO_AKL_DATA_DICT_S where DLBM='026' and XLBM='"+dw+"'";
					String dwmc = DBSql.getString(dwsql, "XLMC");
					//��ǰ�������Ϻż���
					wlh.add(wlbh);
					//�������
					String kcslsql = "select RKSL from BO_AKL_DGKC_KCHZ_P where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					int kcsl = DBSql.getInt(conn, kcslsql, "RKSL");
					//��������
					String pcsql = "select PCSL from BO_AKL_DGKC_KCHZ_P where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					int pcsl = DBSql.getInt(conn, pcsql, "PCSL");
					//��ֺ�ʵ������
					String cfssslsql = "select sum(SSSL) as sl from BO_AKL_DGRK_S where bindid = '"+bindid+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					int sssl = DBSql.getInt(conn, cfssslsql, "sl");
					//��ֺ�Ӧ������
					String cfysslsql = "select sum(YSSL) as sl from BO_AKL_DGRK_S where bindid = '"+bindid+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					int yssl = DBSql.getInt(conn, cfysslsql, "sl");
					//ʵ������У��
					if(kcsl < sssl){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�ͺ�"+xh+",��λ"+dwmc+",��ֺ�ʵ��������"+sssl+"����ʵ������"+kcsl+"������!");
						return false;
					}
					//Ӧ������У��
					if(pcsl < yssl){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�ͺ�"+xh+",��λ"+dwmc+",��ֺ�Ӧ��������"+yssl+"����Ӧ������"+pcsl+"������!");
						return false;
					}else if(pcsl > yssl){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�ͺ�"+xh+",��λ"+dwmc+",��ֺ�Ӧ��������"+yssl+"����Ӧ������"+pcsl+"������!");
						return false;
					}
					//��λ�����Ƿ�Ϊ�յ�У��
					if("".equals(hwdm)){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�ͺ�"+xh+",��λ"+dwmc+",δѡ���λ����ѡ��!");
						return false;
					}
					//���������Ͳ���������⣬�ж��Ƿ���Ҫ���к�
					if(!rklx.equals("�������")){
						String sql2 = "select SFXLH from BO_AKL_WLXX where WLBH= '"+wlbh+"' and HZBM='"+khbh+"'";
						String sfxlh = DBSql.getString(sql2, "SFXLH");
						if("1".equals(sfxlh)){
							wlxlh.add(xh);
							wlsl.put(xh, sssl);
							flag = true;
						}
					}
				}
				
				//�жϵ������Ϻ��Ƿ�ȱ��
				if(rklx.equals("�������")){
					String wlsql = "select WLBH from BO_AKL_DGKC_KCHZ_P where RKDH = '"+rkdh+"'";
					ps = conn.prepareStatement(wlsql);
					rs = ps.executeQuery();
					if(rs != null){
						while(rs.next()){
							boolean wlflag = false;
							String wl = rs.getString("WLBH") == null?"":rs.getString("WLBH");
							for(Iterator<String> it=wlh.iterator();it.hasNext();){
								String s = it.next();
								if(s.equals(wl)){
									wlflag = true;
								}
							}
							if(!wlflag){
								MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��ֺ���ȱ�ٸ�����"+wl+"������!");
								return false;
							}
						}
					}
				}
				
				//�жϵ������ݻ�λ�Ƿ��ظ�
				String sql = "select XH,DW,HWDM from BO_AKL_DGRK_S where bindid = '"+bindid+"' group by XH,DW,HWDM having count(*) > 1";
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						String xh = rs.getString("XH") == null?"":rs.getString("XH");
						String dw = rs.getString("DW") == null?"":rs.getString("DW");
						String hwdm = rs.getString("HWDM") == null?"":rs.getString("HWDM");
						String dwsql = "select XLMC from BO_AKL_DATA_DICT_S where DLBM='026' and XLBM='"+dw+"'";
						String dwmc = DBSql.getString(dwsql, "XLMC");
						if(!"".equals(xh) ){
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��ⵥ��ͬһ���ϴ����ظ���λ���ݣ��ͺ�"+xh
									+",��λ"+dwmc+",��λ����"+hwdm+"������!");
							return false;
						}
					}
				}
				
				//���к�У��
				if(flag){
					//���кż���
//					Vector vc1 = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CCB_RKD_XLH_S", bindid);//¼������кű�
					Vector<Hashtable<String, String>> vc1 = XLHBody(conn, bindid);
					if(vc1 == null){
						StringBuffer xlhstr = new StringBuffer();
						//��Ҫ���кŵ����Ϻ�
						for(Iterator<String> it=wlxlh.iterator();it.hasNext();){
							String s = it.next();
							xlhstr.append(s);
							xlhstr.append(",");
						}
						String str = xlhstr.substring(0, xlhstr.length()-1);
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "����"+str+"��Ҫ���кţ�");
						return false;
					}
					
					
					//��ȡ��Ҫ���кŵ����ϱ�ż���
					for(Iterator<String> it=wlxlh.iterator();it.hasNext();){
						boolean f = true;
						String s = it.next();
						int c = (Integer) wlsl.get(s);//��ȡ������ʵ������
						int count = 0;
						Iterator t1 = vc1.iterator();
						while(t1.hasNext()){
							Hashtable xlhData = (Hashtable) t1.next();
							String xh = xlhData.get("XH") == null ?"":xlhData.get("XH").toString();//�ͺ�
							String xlh = xlhData.get("XLH") == null ?"":xlhData.get("XLH").toString();//���к�
							//�ж����к������Ƿ��ظ�
							String xlhsql = "select XH,XLH from BO_AKL_CCB_RKD_XLH_S where XH='"+xh+"' AND XLH='"+xlh+"' AND ZT='�ڿ�' group by XH,XLH having count(*) > 1";
							ps = conn.prepareStatement(xlhsql);
							rs = ps.executeQuery();
							if(rs != null){
								while(rs.next()){
									String xlhbxh = rs.getString("XH") == null?"":rs.getString("XH");
									String xlhbxlh = rs.getString("XLH") == null?"":rs.getString("XLH");
									if(!"".equals(xlhbxlh)){
										MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "���ͺţ�"+xlhbxh+"�����кţ�"+xlhbxlh+"��Ʒ���ڿ⣬����!");
										return false;
									}
								}
							}
							if(s.equals(xh)){
								wlxlh2.add(xh);
								count =count + 1;
								f=false;
							}
						}
						//У������Ϻ��Ƿ������к�
						if(f){
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�ͺ�"+s+"û�����кţ����飡");
							return false;
						}
						//У�����Ϻŵ����к������Ƿ���ȷ
						if(count < c){
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�ͺ�"+s+"�����к�����"+count+"����ʵ������"+c+"�����飡");
							return false;
						}else if(count > c){
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�ͺ�"+s+"�����к�����"+count+"����ʵ������"+c+"�����飡");
							return false;
						}
					}
					
					//У���Ƿ��ж�������
					if(!wlxlh2.isEmpty()){
						Iterator t1 = vc1.iterator();
						while(t1.hasNext()){
							boolean ff = true;
							Hashtable xlhData = (Hashtable) t1.next();
							String xh = xlhData.get("XH") == null ?"":xlhData.get("XH").toString();//�ͺ�
							for(Iterator<String> it=wlxlh.iterator();it.hasNext();){
								String s = it.next();
								if(s.equals(xh)){
//									wlxlh2.add(xh);
									ff=false;
								}
							}
							if(ff){
								MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "���кű��д��ڶ��������"+xh+"����ɾ����");
								return false;
							}
						}
					}else{
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��Ҫ���кŵ�������δ¼�����кţ���¼�룡");
						return false;
					}
				}
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBSql.close(conn, ps, rs);
			}
		}else if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "�˻�")){
			return true;
		}
		return false;
	}
	
	
	/**
	 * ���кŵ������ݷ�װ
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public Vector<Hashtable<String,String>> XLHBody(Connection conn, int bindid) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector<Hashtable<String, String>> vec = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		String query_xlh = "SELECT XH,XLH FROM BO_AKL_CCB_RKD_XLH_S WHERE BINDID="+bindid;
		try {
			ps = conn.prepareStatement(query_xlh);
			rs = ps.executeQuery();
			while(rs.next()){
				rec = new Hashtable<String, String>();
				String xh = rs.getString("XH") == null ?"":rs.getString("XH");//�ͺ�
				String xlh = rs.getString("XLH") == null ?"":rs.getString("XLH");//���к�
				rec.put("XH", xh);
				rec.put("XLH", xlh);
				vec.add(rec);
			}
		} finally{
			DBSql.close(ps, rs);
		}
		return vec;
	}
}
