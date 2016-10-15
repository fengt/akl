package cn.com.akl.dgrk;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class Update_DGCGWL extends ExcelDownFilterRTClassA {

	public Update_DGCGWL(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("ƥ�䵼���ͺŵĶ����š����Ϻš����ơ���񡢿������");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGCG_P", bindid);
		String ddbh = rkdtData.get("DDBH") == null ?"":rkdtData.get("DDBH").toString();//�������
		String khbh = rkdtData.get("KHBH") == null ?"":rkdtData.get("KHBH").toString();//�ͻ����
		//String gysbh = rkdtData.get("GYSBH") == null ?"":rkdtData.get("GYSBH").toString();//��Ӧ�̱��
		if(ddbh.equals("") || khbh.equals("")){
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��ͷ��Ϣ��ȫ������");
		}
		
		Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGCG_S", bindid);
		
		Connection conn = DBSql.open();
		try {
			Iterator t = vc.iterator();
			while(t.hasNext()){
				//��ȡ�ɹ���������
				Hashtable formData = (Hashtable) t.next();
				String xh = formData.get("XH") == null ?"":formData.get("XH").toString().trim();//�ͺ�
//				String gg = formData.get("GG") == null ?"":formData.get("GG").toString().trim();//���
				String dw = formData.get("DW") == null ?"":formData.get("DW").toString().trim();//������λ
				if("".equals(xh)){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�ͺŴ��ڿ�ֵ������");
				}
				if("".equals(dw)){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��λ���ڿ�ֵ������");
				}
				if(!dw.matches("[0-9]+")){
					//��ȡ��λ����
					String dwsql = "select XLBM from BO_AKL_DATA_DICT_S where DLBM='026' and XLMC='"+dw+"'";
					dw = DBSql.getString(dwsql, "XLBM");
				}
				//ƥ��ɹ��������Ϻš����ơ���񡢿������
				String wlbhsql = "SELECT A.WLBH,WLMC,GG,B.KWSL FROM (SELECT WLBH,WLMC,GG FROM BO_AKL_WLXX WHERE XH='"+xh+"' AND HZBM = '"+khbh+"') A LEFT JOIN (SELECT WLBH,JLDW,SUM(KWSL) AS KWSL FROM BO_AKL_DGKC_KCMX_S GROUP BY WLBH,JLDW) B ON A.WLBH = B.WLBH";
				String wlbh = DBSql.getString(wlbhsql, "WLBH");//���ϱ��
				String wlmc = DBSql.getString(wlbhsql, "WLMC");//��������
				String gg = DBSql.getString(wlbhsql, "GG");//���
				int kwsl = DBSql.getInt(wlbhsql, "KWSL");//�������
				String updatewl = "update BO_AKL_DGCG_S set DDBH='"+ddbh+"',WLBH='"+wlbh+"',WLMC='"+wlmc+"',GG='"+gg+"',DW='"+dw+"',KCSL='"+kwsl+"',CGZT='���ɹ�' where bindid = '"+bindid+"' and XH='"+xh+"'";
				DBSql.executeUpdate(updatewl);
			}
		} catch (Exception e) {
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "ƥ��ʧ�ܣ���֪ͨ��̨");
			e.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, null, null);
		}
		return null;
	}
}
