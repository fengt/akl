package cn.com.akl.hhgl.hhrk.biz;

import java.sql.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.hssf.record.formula.functions.Today;

import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;

import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DealHhrkBodyDatasBiz {

	public static void dealDatas(int bindid){
		///**第一步：将转仓明细插入至入库单身**/
		Vector zcVector = BOInstanceAPI.getInstance().getBODatas(HHDJConstant.tableName2, bindid);
		Hashtable pTable = BOInstanceAPI.getInstance().getBOData(HHDJConstant.tableName0, bindid);
		
		DealRkForZcxxBiz zxUtil = new DealRkForZcxxBiz();
		Date zcrq = Date.valueOf(pTable.get("ZCRQ").toString());
		String code2=pTable.get("UPDATEUSER").toString();
	       
		/**第二步：封装入库单身数据并写入**/
		Vector reZcVector = zxUtil.getZcxx(pTable,zcVector,zcrq);
		try {
			int ids[] = BOInstanceAPI.getInstance().createBOData(HHDJConstant.tableName1, reZcVector, bindid, code2);
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}
	}
}
