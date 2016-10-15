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
		setDescription("更新导入型号的实收数量、单位、属性");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		//读取入库单身
		Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);
		Iterator t = vc.iterator();
		while(t.hasNext()){
			//读取采购单身数据
			Hashtable formData = (Hashtable) t.next();
			String xh = formData.get("XH") == null ?"":formData.get("XH").toString().trim();//型号
			String yssl = formData.get("YSSL") == null ?"":formData.get("YSSL").toString().trim();//应收数量
			String dw = formData.get("DW") == null ?"":formData.get("DW").toString().trim();//单位
			String sx = formData.get("SX") == null ?"":formData.get("SX").toString().trim();//属性
			int ysl = Integer.parseInt(yssl);
			if("".equals(xh)){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "型号存在空值，请检查");
			}
			if(0 == ysl){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "产品"+xh+"应收数量为0，请检查");
			}
			if("".equals(dw)){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "产品"+xh+"单位为空，请检查");
			}
			if("".equals(sx)){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "产品"+xh+"属性为空，请检查");
			}
			if(!dw.matches("[0-9]+")){
				//获取单位编码
				String dwsql = "select XLBM from BO_AKL_DATA_DICT_S where DLBM='026' and XLMC='"+dw+"'";
				dw = DBSql.getString(dwsql, "XLBM");
			}
			if(!sx.matches("[0-9]+")){
				//获取属性编码
				String sxsql = "select XLBM from BO_AKL_DATA_DICT_S where DLBM='049' and XLMC='"+sx+"'";
				sx = DBSql.getString(sxsql, "XLBM");
			}
			//更新入库单身信息
			String updatewl = "update BO_AKL_DGRK_S set SSSL='"+ysl+"',DW='"+dw+"',SX='"+sx+"' where bindid = '"+bindid+"' and XH='"+xh+"'";
			DBSql.executeUpdate(updatewl);
		}
		return null;
	}

}
