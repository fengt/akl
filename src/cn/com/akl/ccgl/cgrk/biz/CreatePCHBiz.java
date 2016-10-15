package cn.com.akl.ccgl.cgrk.biz;

import java.sql.Date;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DateUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;

public class CreatePCHBiz {

	/**
	 * 1、自营库存批次号生成规则
	 **/
	public static String createPCH(Date rkrq){
		//生成批次号规则，当前入库日期2014-07-24转换为20140724 + 001序列号
		String pch1 = DateUtil.dateToLongStrBys2(rkrq);//前缀，如：20140724
		String pch = pch1 + judgeRKRQ(pch1);
		return pch;
	}
	
	/**
	 * 判断是否存在当前入库日期的批次号，如果库存汇总表中已存在当前入库日期，则批次号，后缀三位进行累加，否则，当前入库日期+001
	 * @param pch1
	 * @return
	 */
	private static String judgeRKRQ(String pch1){
		String sql = "select max(SUBSTRING(pch,9,3)) pch2 from " + CgrkCnt.tableName3 + " where SUBSTRING(pch,1,8) = '" + pch1 + "'";
		String pch2 = "";
		pch2 = StrUtil.returnStr(DBSql.getString(sql, "pch2"));
		if(StrUtil.isNotNull(pch2)){
			return String.format("%03d", Integer.parseInt(pch2)+1);
		}
		return "001";
	}
	
	
	
	/**
	 * 2、代管库批次号生成规则
	 **/
	public static String createPCH0(Date rkrq){
		//生成批次号规则，当前入库日期2014-07-24转换为20140724 + 001序列号
		String pch1 = DateUtil.dateToLongStrBys2(rkrq);//前缀，如：20140724
		String pch = pch1 + judgeRKRQ0(pch1);
		return pch;
	}
	
	private static String judgeRKRQ0(String pch1){
		String sql = "select max(SUBSTRING(pch,9,3)) pch2 from " + CgrkCnt.tableName12 + " where SUBSTRING(pch,1,8) = '" + pch1 + "'";
		String pch2 = "";
		pch2 = StrUtil.returnStr(DBSql.getString(sql, "pch2"));
		if(StrUtil.isNotNull(pch2)){
			return String.format("%03d", Integer.parseInt(pch2)+1);
		}
		return "001";
	}
}
