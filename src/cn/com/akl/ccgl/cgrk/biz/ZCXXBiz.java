package cn.com.akl.ccgl.cgrk.biz;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;


public class ZCXXBiz {
	
	private static final String QUERY_KHDM = "SELECT TOP 1 KHDM FROM BO_AKL_CCB_RKD_ZCXX WHERE BINDID =?";//查询客户代码
	private static final String QUERY_CGCK = "SELECT CKID FROM BO_AKL_CGDD_HEAD WHERE DDID=?";//查询采购仓库
	//从库位关系表获取最新货位关系
	private static final String SFYX = "是";//是否有效
	private static final String QUERY_HWDM = "SELECT c.CKDM,c.QDM,c.DDM,c.KWDM,c.KWBH FROM BO_AKL_WLKWGXB c,( SELECT MAX(ID)AS ID FROM BO_AKL_WLKWGXB WHERE SFYX='"+SFYX+"' AND XH=? AND SSKHBM=? AND CKDM=? GROUP BY XH,SSKHBM )b WHERE c.ID=b.ID";
	private static final String QUERY_DH_HWDM = "SELECT c.CKDM,c.QDM,c.DDM,c.KWDM,c.KWBH FROM BO_AKL_WLKWGXB c,( SELECT MAX(ID)AS ID FROM BO_AKL_WLKWGXB WHERE SFYX='"+SFYX+"' AND XH=? AND SSKHBM=? GROUP BY XH,SSKHBM )b WHERE c.ID=b.ID";//大恒物料库位关系
	
	/**
	 * 获取转仓信息
	 * @param pTable主表数据
	 * @param zcVector子表数据
	 * @param rkrq入库日期
	 * @return
	 */
	public Vector<Hashtable<String, String>> getZcxx(Connection conn,
			Hashtable<String, String> pTable,Vector<Hashtable<String, String>> zcVector,Date zcrq) throws SQLException{
		Vector<Hashtable<String, String>> reZcVector = new Vector<Hashtable<String, String>>();//封装后的转仓明细信息
		Hashtable<String, String> reTable = null;//接收遍历后的转仓信息
		Hashtable<String, String> subTable = new Hashtable<String, String>();//遍历的转仓信息
		
		/**根据客户代码判断代管库批次号生成*/
		int bindid = Integer.parseInt(pTable.get("BINDID").toString());
		String khdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QUERY_KHDM, bindid));//获取客户代码
		String xh = "";
		for(int i=0;i<zcVector.size();i++){
			reTable = new Hashtable<String, String>();
			subTable = zcVector.get(i);
			if(subTable!=null){
				//reTable.put("WLBH", subTable.get("WLBH").toString());//物料编号
				//reTable.put("DW", subTable.get("DW").toString());//单位
				String cgdh = subTable.get("KHDDH").toString();
				xh = subTable.get("LH").toString();
				
				reTable.put("CGDDH", cgdh);//客户订单号(AKL)
				reTable.put("XH", xh);//型号
				reTable.put("YSSL", subTable.get("CHSL").toString());//出货数量(应收数量)
				reTable.put("SSSL", subTable.get("CHSL").toString());//出货数量(实收数量，默认，可以修改)
				reTable.put("LYDH", subTable.get("DDH").toString());//订单号
				reTable.put("DDHH", subTable.get("DDHH").toString());//订单行号
				reTable.put("RKDH", pTable.get("RKDH").toString());//入库单号
				
				/**批次号，物料库位关系信息*/
				if("".equals(khdm) || khdm.equals(CgrkCnt.khdm0)){//自营
					reTable.put("PCH", CreatePCHBiz.createPCH(zcrq));//批次号
					String cgck = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QUERY_CGCK, cgdh));//采购单头仓库代码
					Hashtable<String, String> sysTable = getSysDatas(conn,subTable,cgck);
					if(sysTable!=null){
						reTable.put("CKBM", sysTable.get("ckdm").toString());//仓库编码
						reTable.put("KFQBM", sysTable.get("qdm").toString());//库房区编码
						reTable.put("KFDBM", sysTable.get("ddm").toString());//库房道编码
						reTable.put("KFKWDM", sysTable.get("kwdm").toString());//库房库位编码
						reTable.put("KWBH", sysTable.get("kwbh").toString());//库位编号
					}else{//默认采购时的仓库
						reTable.put("CKBM", cgck);//仓库编码
						reTable.put("KWBH", cgck);//库位编号
					}
				}else{//代管
					reTable.put("PCH", CreatePCHBiz.createPCH0(zcrq));//批次号
					Hashtable<String, String> sysTable = getSysDatas(conn,subTable);
					if(sysTable!=null){
						reTable.put("CKBM", sysTable.get("ckdm").toString());//仓库编码
						reTable.put("KFQBM", sysTable.get("qdm").toString());//库房区编码
						reTable.put("KFDBM", sysTable.get("ddm").toString());//库房道编码
						reTable.put("KFKWDM", sysTable.get("kwdm").toString());//库房库位编码
						reTable.put("KWBH", sysTable.get("kwbh").toString());//库位编号
					}
				}
			}
			
			/**根据型号和客户代码，回填物料相关信息**/
			Hashtable<String, String> wlxxTable = getWLXX(conn,pTable,subTable);
			if(wlxxTable!=null){
				String wlbh = wlxxTable.get("wlbh").toString();//物料编码
				reTable.put("WLBH", wlbh);
				reTable.put("CPMC", wlxxTable.get("wlmc").toString());//物料名称
				reTable.put("DW", wlxxTable.get("dw").toString());//单位
				/**根据料号，获取价格管理表中执行日期小于等于转仓日期的最后一次该料号的未税价格和含税价格**/
				Hashtable<String, String> jgglTabel = dealZCJG(conn, pTable, wlbh);
				if(jgglTabel!=null){
					double wsjg = Double.parseDouble(jgglTabel.get("wsjg").toString());//未税价格
					//double hsjg = Double.parseDouble(jgglTabel.get("hsjg").toString());//含税价格
					double sl = Double.parseDouble(jgglTabel.get("sl").toString());//税率
					double hsjg = wsjg*(sl+1.0);
					double wsje = Double.parseDouble(jgglTabel.get("wsjg").toString()) * Integer.parseInt(subTable.get("CHSL").toString());//未税金额
					double hsje = Double.parseDouble(jgglTabel.get("hsjg").toString()) * Integer.parseInt(subTable.get("CHSL").toString());//含税金额
					if(khdm.equals(CgrkCnt.khdm0)&&(hsjg==0.0 || wsjg==0.0)){
						throw new RuntimeException("该物料【"+xh+"】转仓时未能读取到最新价格，请核查该物料的价格表信息是否正确！");
					}
					reTable.put("HSJG", String.valueOf(hsjg));
					reTable.put("WSJG", String.valueOf(wsjg));
					reTable.put("WSJE", String.valueOf(wsje));
					reTable.put("HSJE", String.valueOf(hsje));
					reTable.put("SL",String.valueOf(sl));
				}else{
					 if(khdm.equals(CgrkCnt.khdm0)){
						 throw new RuntimeException("该物料【"+xh+"】转仓时未能读取到最新价格，请核查该物料的价格表信息是否正确！");
					 }
				}
			}
			reZcVector.add(reTable);
		}
		return reZcVector;
	}
	
	/**
	 * 根据型号和客户代码，获取物料相关信息
	 * @param table
	 * @return
	 */
	public Hashtable<String, String> getWLXX(Connection conn,Hashtable<String, String> pTable,Hashtable<String, String> table) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> wlxxTable = null;
		String xh = table.get("LH").toString();
		String khdm = table.get("KHDM").toString();
		String sql = "select * from " + CgrkCnt.tableName14 + " where gyskhbh = '" + khdm + "'";
		String khbh = DBSql.getString(sql, "KHBH");
		String sql2 = "select * from " + CgrkCnt.tableName8 + " where xh = '" + xh + "' and hzbm = '"+khbh+"'";
		try {
			ps = conn.prepareStatement(sql2);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					wlxxTable = new Hashtable<String, String>();
					// 获取物料相关信息
					String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
					String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
					String dw = StrUtil.returnStr(rs.getString("DW"));
					wlxxTable.put("wlbh", wlbh);
					wlxxTable.put("wlmc", wlmc);
					wlxxTable.put("dw", dw);
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return wlxxTable;
	}
	
	/**
	 * 
	 * @param conn
	 * @param pTable 主表信息==>获取转仓日期
	 * @param wlbh 物料编号==>获取价格管理中该物料号所对应的未税价格、含税价格
	 * @return
	 */
	private Hashtable<String, String> dealZCJG(Connection conn,Hashtable<String, String> pTable,String wlbh) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> reTable = null;
		Date zcrq = Date.valueOf(pTable.get("ZCRQ").toString());//转仓日期
		String gysbh = pTable.get("GYSBH").toString();//供应商编号
		/*String sql = "select * from " + CgrkCnt.tableName10 + " where (CONVERT(varchar(100), ZXRQ, 23)) = " +
				"(select max(CONVERT(varchar(100), ZXRQ, 23)) from " + CgrkCnt.tableName10 + " where (CONVERT(varchar(100), ZXRQ, 23)) <= '"+ zcrq +"' and wlbh = '" + wlbh + "' and gysbh = '" + gysbh + "') and wlbh = '" + wlbh + "' and gysbh = '" + gysbh + "'" +
						"AND ID = (SELECT MAX(ID) FROM " + CgrkCnt.tableName10 + " WHERE (CONVERT (VARCHAR(100), ZXRQ, 23)) <= '"+zcrq+"' AND wlbh = '"+wlbh+"' and gysbh = '" + gysbh + "')";
		*/
		String sql = "SELECT * FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) = ( SELECT MAX ( CONVERT (VARCHAR(100), ZXRQ, 23) ) FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) <= '"+ zcrq +"' AND wlbh = '" + wlbh + "' AND gysbh = '" + gysbh + "' ) AND wlbh = '" + wlbh + "' AND gysbh = '" + gysbh + "' AND ID = ( SELECT MAX (ID) FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) <= '"+zcrq+"' AND wlbh = '"+wlbh+"' AND gysbh = '" + gysbh + "' )";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					reTable = new Hashtable<String, String>();
					reTable.put("wsjg", String.valueOf(rs.getDouble("ZDCB")));//未税价格
					reTable.put("hsjg", String.valueOf(rs.getDouble("HSCGJ")));//含税价格
					reTable.put("sl", String.valueOf(rs.getDouble("SL")));//税率
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return reTable;
	}
	
	/**
	 * 获取库位相关信息(网销)
	 * @param table
	 * @return
	 */
	public Hashtable<String, String> getSysDatas(Connection conn,Hashtable<String, String> table,String cgck) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> sysTable = null;
		String xh = table.get("LH").toString();
		String khdm = table.get("KHDM").toString();
		String sql = "select KHBH from " + CgrkCnt.tableName14 + " where gyskhbh = '" + khdm + "'";
		String khbh = DBSql.getString(conn, sql, "KHBH");
		try {
			ps = conn.prepareStatement(QUERY_HWDM);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, xh, khbh, cgck);
			if(rs!=null){
				while(rs.next()){
					sysTable = new Hashtable<String, String>();
					//获取库位相关信息
					String ckdm = StrUtil.returnStr(rs.getString("CKDM"));
					String qdm = StrUtil.returnStr(rs.getString("QDM"));
					String ddm = StrUtil.returnStr(rs.getString("DDM"));
					String kwdm = StrUtil.returnStr(rs.getString("KWDM"));
					String kwbh = StrUtil.returnStr(rs.getString("KWBH"));
					sysTable.put("ckdm", ckdm);
					sysTable.put("qdm", qdm);
					sysTable.put("ddm", ddm);
					sysTable.put("kwdm", kwdm);
					sysTable.put("kwbh", kwbh);
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return sysTable;
	}
	
	/**
	 * 获取库位相关信息(大恒)
	 * @param conn
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> getSysDatas(Connection conn,Hashtable<String, String> table) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> sysTable = null;
		String cgdh = table.get("KHDDH").toString();//采购单号
		String xh = table.get("LH").toString();
		String khdm = table.get("KHDM").toString();
		String sql = "select * from " + CgrkCnt.tableName14 + " where gyskhbh = '" + khdm + "'";
		String khbh = DBSql.getString(sql, "KHBH");
		try {
			ps = conn.prepareStatement(QUERY_DH_HWDM);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, xh, khbh);
			if(rs!=null){
				while(rs.next()){
					sysTable = new Hashtable<String, String>();
					//获取库位相关信息
					String ckdm = StrUtil.returnStr(rs.getString("CKDM"));
					String qdm = StrUtil.returnStr(rs.getString("QDM"));
					String ddm = StrUtil.returnStr(rs.getString("DDM"));
					String kwdm = StrUtil.returnStr(rs.getString("KWDM"));
					String kwbh = StrUtil.returnStr(rs.getString("KWBH"));
					sysTable.put("ckdm", ckdm);
					sysTable.put("qdm", qdm);
					sysTable.put("ddm", ddm);
					sysTable.put("kwdm", kwdm);
					sysTable.put("kwbh", kwbh);
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return sysTable;
	}
}
