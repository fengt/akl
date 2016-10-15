package cn.com.akl.ccgl.cgrk.biz;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * 向应付信息表中插入应付记录
 * @author ActionSoft_2013
 *
 */
public class YFBiz {
	
	/**
	 * 向应付信息表中插入应付记录
	 * @param pTable
	 * @param sVector
	 */
	public static void insertInfoToYf(Connection conn, int bindid,UserContext uc,
			Hashtable<String, String> pTable,Vector<Hashtable<String, String>> sVector) throws SQLException, AWSSDKException{
		
		Hashtable<String, String> recordData = new Hashtable<String, String>();
		recordData.put("GYSBM", pTable.get("GYSBH").toString());
		recordData.put("GYSMC", pTable.get("GYSMC").toString());
		recordData.put("RKDH", pTable.get("RKDH").toString());
		recordData.put("ZCRQ", pTable.get("ZCRQ").toString());
		
		String QuerySFYF = "SELECT SFYF FROM BO_AKL_CGDD_HEAD WHERE DDID=?";
		
		double wsje = 0.0000d;
		double hsje = 0.0000d;
		double wsAccount = 0.0000d;
		double hsAccount = 0.0000d;
		Hashtable<String, String> resTable = null;
		for (int i = 0; i < sVector.size(); i++) {
			resTable = sVector.get(i);
			String cgddh = resTable.get("CGDDH").toString();//采购订单号
			String sfyf = DAOUtil.getString(conn, QuerySFYF, cgddh);//是否已预付
			if(sfyf.equals(CgrkCnt.sfyf)){
				continue;
			}
			double sl = Double.parseDouble(resTable.get("SL").toString());
			wsje = Double.parseDouble(resTable.get("WSJE").toString());
			//hsje = Double.parseDouble(resTable.get("HSJE").toString());
			hsje = wsje*(sl+1.0);
			wsAccount += wsje;
			hsAccount += hsje;
		}
		recordData.put("WSYFJE", String.valueOf(wsAccount));
		recordData.put("HSYFJE", String.valueOf(hsAccount));
		recordData.put("ZT",CgrkCnt.zt);
		recordData.put("LB",CgrkCnt.lb1);
		if(wsAccount != 0.0){
			BOInstanceAPI.getInstance().createBOData(conn, CgrkCnt.tableName11, recordData, bindid, uc.getUID());
		}
	}
}
