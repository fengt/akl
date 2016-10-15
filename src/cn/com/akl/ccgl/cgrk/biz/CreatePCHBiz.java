package cn.com.akl.ccgl.cgrk.biz;

import java.sql.Date;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DateUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;

public class CreatePCHBiz {

	/**
	 * 1����Ӫ������κ����ɹ���
	 **/
	public static String createPCH(Date rkrq){
		//�������κŹ��򣬵�ǰ�������2014-07-24ת��Ϊ20140724 + 001���к�
		String pch1 = DateUtil.dateToLongStrBys2(rkrq);//ǰ׺���磺20140724
		String pch = pch1 + judgeRKRQ(pch1);
		return pch;
	}
	
	/**
	 * �ж��Ƿ���ڵ�ǰ������ڵ����κţ���������ܱ����Ѵ��ڵ�ǰ������ڣ������κţ���׺��λ�����ۼӣ����򣬵�ǰ�������+001
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
	 * 2�����ܿ����κ����ɹ���
	 **/
	public static String createPCH0(Date rkrq){
		//�������κŹ��򣬵�ǰ�������2014-07-24ת��Ϊ20140724 + 001���к�
		String pch1 = DateUtil.dateToLongStrBys2(rkrq);//ǰ׺���磺20140724
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
