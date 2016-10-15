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

public class Update_DGRKWL extends ExcelDownFilterRTClassA {

	public Update_DGRKWL(UserContext arg0) {
		super(arg0);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("���µ����ͺŵ�ʵ����������λ������");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		//��ȡ��ⵥ��
		Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);
		Iterator t = vc.iterator();
		while(t.hasNext()){
			//��ȡ�ɹ���������
			Hashtable formData = (Hashtable) t.next();
			String xh = formData.get("XH") == null ?"":formData.get("XH").toString().trim();//�ͺ�
			String yssl = formData.get("YSSL") == null ?"":formData.get("YSSL").toString().trim();//Ӧ������
			String dw = formData.get("DW") == null ?"":formData.get("DW").toString().trim();//��λ
			String sx = formData.get("SX") == null ?"":formData.get("SX").toString().trim();//����
			int ysl = Integer.parseInt(yssl);
			if("".equals(xh)){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�ͺŴ��ڿ�ֵ������");
			}
			if(0 == ysl){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��Ʒ"+xh+"Ӧ������Ϊ0������");
			}
			if("".equals(dw)){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��Ʒ"+xh+"��λΪ�գ�����");
			}
			if("".equals(sx)){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��Ʒ"+xh+"����Ϊ�գ�����");
			}
			if(!dw.matches("[0-9]+")){
				//��ȡ��λ����
				String dwsql = "select XLBM from BO_AKL_DATA_DICT_S where DLBM='026' and XLMC='"+dw+"'";
				dw = DBSql.getString(dwsql, "XLBM");
			}
			if(!sx.matches("[0-9]+")){
				//��ȡ���Ա���
				String sxsql = "select XLBM from BO_AKL_DATA_DICT_S where DLBM='049' and XLMC='"+sx+"'";
				sx = DBSql.getString(sxsql, "XLBM");
			}
			//������ⵥ����Ϣ
			String updatewl = "update BO_AKL_DGRK_S set SSSL='"+ysl+"',DW='"+dw+"',SX='"+sx+"' where bindid = '"+bindid+"' and XH='"+xh+"'";
			DBSql.executeUpdate(updatewl);
		}
		return null;
	}

}
