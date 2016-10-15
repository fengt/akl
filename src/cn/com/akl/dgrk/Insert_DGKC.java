package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class Insert_DGKC extends WorkFlowStepRTClassA {

	public Insert_DGKC() {
	}

	public Insert_DGKC(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("��ⵥ����Ϣ��������ϸ��,���¿����ܱ����²ɹ����ɹ�״̬��������������������к�״̬");
	}

	@Override
	public boolean execute() {
		//������ܿ����ܱ���ϸ��
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		//��ȡ��ⵥͷ��Ϣ
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);
		String rkdh = rkdtData.get("RKDH") == null ?"":rkdtData.get("RKDH").toString();//��ⵥ��
		String ydh = rkdtData.get("YDH") == null ?"":rkdtData.get("YDH").toString();//�ɹ�����
		String rklx = rkdtData.get("RKLX") == null ?"":rkdtData.get("RKLX").toString();//�������
		if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "���")){
			Set<String> wlh = new HashSet<String>();//���ϱ��+��λ����
			Set<String> qswlh = new HashSet<String>();//������ȱ�ٵ����ϱ��+��λ����
			//��ȡ��ⵥ����Ϣ
			Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);
			Vector v2 = new Vector();//�����ϸ
			String pchsql = "select distinct PCH from BO_AKL_DGKC_KCHZ_P where RKDH = '"+rkdh+"'";
			
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection conn = DBSql.open();
			try {
				//��ȡ���κ�
				String pch = DBSql.getString(conn, pchsql, "PCH");
				
				Iterator t = vc.iterator();
				while(t.hasNext()){
					//��ȡ��ⵥ������
					Hashtable formData = (Hashtable) t.next();
					String wlbh = formData.get("WLBH") == null ?"":formData.get("WLBH").toString();//���ϱ��
					String wlmc = formData.get("WLMC") == null ?"":formData.get("WLMC").toString();//��������
					String gg = formData.get("GG") == null ?"":formData.get("GG").toString();//���
					String xh = formData.get("XH") == null ?"":formData.get("XH").toString();//�ͺ�
//					String yssl = formData.get("YSSL") == null ?"":formData.get("YSSL").toString();//Ӧ������
					String sssl = formData.get("SSSL") == null ?"":formData.get("SSSL").toString();//ʵ������
					String ckdm = formData.get("CKDM") == null ?"":formData.get("CKDM").toString();//�ֿ����
					String ckmc = formData.get("CKMC") == null ?"":formData.get("CKMC").toString();//�ֿ�����
					String qdm = formData.get("QDM") == null ?"":formData.get("QDM").toString();//������
					String ddm = formData.get("DDM") == null ?"":formData.get("DDM").toString();//������
					String kwdm = formData.get("KWDM") == null ?"":formData.get("KWDM").toString();//��λ����
					String hwdm = formData.get("HWDM") == null ?"":formData.get("HWDM").toString();//��λ����
					String bzq = formData.get("BZQ") == null ?"":formData.get("BZQ").toString();//������
					String scrq = formData.get("SCHCGRQ") == null ?"":formData.get("SCHCGRQ").toString();//��������
					String dw = formData.get("DW") == null ?"":formData.get("DW").toString();//��λ
					String sx = formData.get("SX") == null ?"":formData.get("SX").toString();//����
//					int sl1 = Integer.parseInt(yssl);//Ӧ������
					int sl2 = Integer.parseInt(sssl);//ʵ������
					
					//��ǰ�������Ϻ�+��λ����
					wlh.add(wlbh+dw);
					//��ӿ����ϸ����
					Hashtable recordData2 = new Hashtable();
					recordData2.put("WLBH", wlbh);//���ϱ��
					recordData2.put("WLMC", wlmc);//��������
					recordData2.put("GG", gg);//���
					recordData2.put("XH", xh);//�ͺ�
					recordData2.put("PCH", pch);//���κ�
					recordData2.put("CKDM", ckdm);//�ֿ����
					recordData2.put("CKMC", ckmc);//�ֿ�����
					recordData2.put("QDM", qdm);//������
					recordData2.put("DDM", ddm);//������
					recordData2.put("KWDM", kwdm);//��λ����
					recordData2.put("HWDM", hwdm);//��λ����
					recordData2.put("KWSL", sl2);//��λ����
					recordData2.put("BZQ", bzq);//������
					recordData2.put("SCHCGRQ", scrq);//��������
					recordData2.put("JLDW", dw);//������λ
					recordData2.put("SX", sx);//����
					v2.add(recordData2);
					
					//��ֺ�ʵ������
					String cfssslsql = "select sum(SSSL) as sl from BO_AKL_DGRK_S where bindid = '"+bindid+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					int cfsssl = DBSql.getInt(conn, cfssslsql, "sl");
					//��ֺ�Ӧ������
					String cfysslsql = "select sum(YSSL) as sl from BO_AKL_DGRK_S where bindid = '"+bindid+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					int cfyssl = DBSql.getInt(conn, cfysslsql, "sl");
					if(!rklx.equals("�������")){
						//�ɹ��������������
						String yrksql = "select YRKSL from BO_AKL_DGCG_S where DDBH='"+ydh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
						int yrk = DBSql.getInt(yrksql, "YRKSL");
						//ʵ�������ж�
						if(cfyssl > cfsssl){					
							//���²ɹ��������������
							yrk = yrk + sl2;
							String cgslsql = "update BO_AKL_DGCG_S set YRKSL="+yrk+" where DDBH='"+ydh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,cgslsql);
							
							//���²ɹ�����ɹ�״̬
							String cgssql = "update BO_AKL_DGCG_S set CGZT='���ɹ�' where DDBH='"+ydh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,cgssql);
							
							//���¿������������
							String rkslsql = "update BO_AKL_DGKC_KCHZ_P set RKSL="+cfsssl+" where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,rkslsql);
							
							//���¿�������������
							String pcslsql = "update BO_AKL_DGKC_KCHZ_P set PCSL="+cfsssl+" where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,pcslsql);
							
						}else if(cfyssl == cfsssl){					
							//���²ɹ��������������
							yrk = yrk + sl2;
							String cgslsql = "update BO_AKL_DGCG_S set YRKSL="+yrk+" where DDBH='"+ydh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,cgslsql);
						}
					}else if(rklx.equals("�������")){
						//ʵ�������ж�
						if(cfyssl > cfsssl){
							//���¿������������
							String rkslsql = "update BO_AKL_DGKC_KCHZ_P set RKSL="+cfsssl+" where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,rkslsql);
							//���¿�������������
							String pcslsql = "update BO_AKL_DGKC_KCHZ_P set PCSL="+cfsssl+" where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,pcslsql);
						}
					}
					
					//���¿�����������ڡ�״̬����
					Date date = new Date();
					SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
					String dateString = format.format(date);
					String rkrqsql = "update BO_AKL_DGKC_KCHZ_P set RKRQ='"+dateString+"',ZT='042022' where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					DBSql.executeUpdate(conn,rkrqsql);				
					
					//��ѯ�ɹ�����ɹ�״̬
					if(!rklx.equals("�������")){
						String cgztsql = "select CGZT from BO_AKL_DGCG_S where DDBH='"+ydh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
						String cgzt = DBSql.getString(cgztsql, "CGZT");
						if("�����".equals(cgzt)){
							//���²ɹ�����ɹ�״̬
							String cgssql = "update BO_AKL_DGCG_S set CGZT='�����' where DDBH='"+ydh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,cgssql);
						}
					}
				}
				
				//�жϵ������Ϻ��Ƿ�ȱ��,ȱ�ٵ��ϺŸ��Ĳɹ�״̬
				String wlsql = "select WLBH,DW from BO_AKL_DGKC_KCHZ_P where RKDH = '"+rkdh+"'";
				ps = conn.prepareStatement(wlsql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						boolean wlflag = false;
						String wl = rs.getString("WLBH") == null?"":rs.getString("WLBH");
						String dw = rs.getString("DW") == null?"":rs.getString("DW");
						
						for(Iterator<String> it=wlh.iterator();it.hasNext();){
							String s = it.next();
							if(s.equals(wl+dw)){
								wlflag = true;
							}
						}
						if(!wlflag){
							//ȱ�ٵ����Ϻ�+��λ���뼯��
							qswlh.add(wl+dw);
							if(!rklx.equals("�������")){
								//���²ɹ�����ɹ�״̬
								String cgssql = "update BO_AKL_DGCG_S set CGZT='���ɹ�' where DDBH='"+ydh+"' and wlbh='"+wl+"' and dw='"+dw+"'";
								DBSql.executeUpdate(conn,cgssql);
							}
						}
					}
				}
				
				//ɾ�������ܵ�����ɾ�������Ϻ�
				for(Iterator<String> it=qswlh.iterator();it.hasNext();){
					String s = it.next();
					String cgssql = "delete from BO_AKL_DGKC_KCHZ_P where RKDH='"+rkdh+"' and WLBH+DW='"+s+"'";
					DBSql.executeUpdate(conn,cgssql);
				}
				
				//��������ϸ��
				BOInstanceAPI.getInstance().removeProcessInstanceBOData("BO_AKL_DGKC_KCMX_S", bindid);
				BOInstanceAPI.getInstance().createBOData("BO_AKL_DGKC_KCMX_S", v2, bindid, this.getUserContext().getUID());
				if(!rklx.equals("�������")){
					//���²ɹ���ͷ�ɹ�״̬(���»ش��ɹ�)
					String cgsql = "select CGZT from BO_AKL_DGCG_S where DDBH='"+ydh+"'";
					boolean cgflag = false;
					ps = conn.prepareStatement(cgsql);
					rs = ps.executeQuery();
					if(rs != null){
						while(rs.next()){
							String cgzt = rs.getString("CGZT") == null?"":rs.getString("CGZT");
							if("���ɹ�".equals(cgzt)){
								cgflag = true;
							}
						}
						if(cgflag){
							String cgpsql = "update BO_AKL_DGCG_P set CGZT='���ɹ�' where DDBH='"+ydh+"'";
							DBSql.executeUpdate(conn,cgpsql);
						}
					}
					//���²ɹ���ͷ�ɹ�״̬(����Ϊ�����)
					String sql = "select CGZT from BO_AKL_DGCG_S where DDBH='"+ydh+"' group by CGZT";
					boolean flag = true;
					ps = conn.prepareStatement(sql);
					rs = ps.executeQuery();
					if(rs != null){
						while(rs.next()){
							String cgzt = rs.getString("CGZT") == null?"":rs.getString("CGZT");
							if(!"�����".equals(cgzt)){
								flag = false;
							}
						}
						if(flag){
							String cgpsql = "update BO_AKL_DGCG_P set CGZT='�����' where DDBH='"+ydh+"'";
							DBSql.executeUpdate(conn,cgpsql);
						}
					}
					//�������к�״̬
					String cgpsql = "update BO_AKL_CCB_RKD_XLH_S set ZT='�ڿ�' where bindid='"+bindid+"'";
					DBSql.executeUpdate(conn,cgpsql);
				}
				return true;
			} catch (Exception e) {
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "д����ʧ�ܣ���֪ͨ����Ա");
				e.printStackTrace(System.err);
				return false;
			} finally {
				DBSql.close(conn, ps, rs);
			}
		}else if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "�˻�")){
			Connection conn = DBSql.open();
			try {
				int currentStepNo = 3;
				int stepno = new WorkFlowUtil().getPreviousStepNo(conn, bindid, currentStepNo);
				if(stepno == 1){
					//ɾ��������
					String hzsql = "delete from BO_AKL_DGKC_KCHZ_P where RKDH='"+rkdh+"'";
					DBSql.executeUpdate(hzsql);
					//���������Ͳ����������
					if(!rklx.equals("�������")){
						//���²ɹ�����ɹ�״̬
						String cgdssql = "update BO_AKL_DGCG_S set CGZT='���ɹ�' where DDBH='"+ydh+"'";
						DBSql.executeUpdate(cgdssql);
						//���²ɹ���ͷ
						String cgdtsql = "update BO_AKL_DGCG_P set CGZT='���ɹ�' where DDBH='"+ydh+"'";
						DBSql.executeUpdate(cgdtsql);
					}
					//ɾ����ⵥ�����κ�
					String pchsql = "update BO_AKL_DGRK_S set PCH='' where bindid='"+bindid+"'";
					DBSql.executeUpdate(pchsql);
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				e.printStackTrace(System.err);
				return false;
			}finally {
				DBSql.close(conn, null, null);
			}
		}
		return false;
	}
}
