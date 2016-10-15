package cn.com.akl.hhgl.hhrk.biz;

import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;

import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * 向应付信息表中插入应付记录
 * @author ActionSoft_2013
 *
 */
public class DealYsyfInfoBiz {

	public static void getInfoToYf(int bindid){
		Hashtable pTable = BOInstanceAPI.getInstance().getBOData(HHDJConstant.tableName0, bindid);
		Vector sVector = BOInstanceAPI.getInstance().getBODatas(HHDJConstant.tableName1, bindid);
		
		/**向应付信息表中插入应付记录**/
		int id = insertInfoToYf( pTable, sVector);
	}
	
	/**
	 * 向应付信息表中插入应付记录
	 * @param pTable
	 * @param sVector
	 */
	public static int insertInfoToYf(Hashtable pTable,Vector sVector){
		Hashtable recordData = new Hashtable();
		recordData.put("GYSBM", pTable.get("GYSBH").toString());
		recordData.put("GYSMC", pTable.get("GYS").toString());
		recordData.put("RKDH", pTable.get("RKDH").toString());
		double wsje = 0.0000d;
		double hsje = 0.0000d;
		double wsAccount = 0.0000d;
		double hsAccount = 0.0000d;
		Hashtable resTable = null;
		for (int i = 0; i < sVector.size(); i++) {
			recordData = new Hashtable();
			resTable = (Hashtable) sVector.get(i);
			recordData.put("CGDH", resTable.get("CGDDH").toString());
			wsje = Double.parseDouble(resTable.get("WSJE").toString());
			hsje = Double.parseDouble(resTable.get("HSJE").toString());
			wsAccount += wsje;
			hsAccount += hsje;
		}
		recordData.put("WSYFJE", wsAccount);
		recordData.put("HSYFJE", hsAccount);
		int id = 0;
		try {
			id = BOInstanceAPI.getInstance().createBOData(HHDJConstant.tableName11, recordData, "admin");
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}
		return id;
	}
}
