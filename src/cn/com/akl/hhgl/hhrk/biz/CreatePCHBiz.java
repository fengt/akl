package cn.com.akl.hhgl.hhrk.biz;

import java.sql.Date;

import com.actionsoft.awf.util.DBSql;

import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import cn.com.akl.util.DateUtil;
import cn.com.akl.util.StrUtil;

public class CreatePCHBiz {

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
		String sql = "select SUBSTRING(pch,8,3) pch2 from " + HHDJConstant.tableName3 + " where SUBSTRING(pch,0,8) = '" + pch1 + "'";
		String pch2 = "";
		pch2 = StrUtil.returnStr(DBSql.getString(sql, "pch2"));
		if(StrUtil.isNotNull(pch2)){
			return String.format("%03d", Integer.parseInt(pch2)+1);
		}
		return "001";
	}
}
