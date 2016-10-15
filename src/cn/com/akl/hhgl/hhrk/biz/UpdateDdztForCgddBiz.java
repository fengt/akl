package cn.com.akl.hhgl.hhrk.biz;

import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;

import com.actionsoft.awf.util.DBSql;

public class UpdateDdztForCgddBiz {
	/***
	 * 根据采购订单号，更新采购订单订单状态为已入库及入库数量
	 * @param aklOrderId
	 * @return
	 */
	public static int updateDatas(String aklOrderId,String lh,String zt,int sssl){
//		String sql = "update " + CgrkConstant.tableName6 + " set ddzt = '" + zt + "' where ddid = '" + aklOrderId + "'";
		String sql2 = "update " + HHDJConstant.tableName7 + " set zt = '" + zt + "',YRKSL = '" + sssl + "' where ddid = '" + aklOrderId + "' and xh = '" + lh + "'";
//		int cnt = DBSql.executeUpdate(sql);
		int cnt = DBSql.executeUpdate(sql2);
		return cnt;
	}
}
