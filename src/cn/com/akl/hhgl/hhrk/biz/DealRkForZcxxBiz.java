package cn.com.akl.hhgl.hhrk.biz;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.util.DBSql;

import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import cn.com.akl.util.StrUtil;


public class DealRkForZcxxBiz {
	
	private Connection conn = null;
	
	/**
	 * 获取转仓信息
	 * @param pTable主表数据
	 * @param zcVector子表数据
	 * @param rkrq入库日期
	 * @return
	 */
	public Vector getZcxx(Hashtable pTable,Vector zcVector,Date zcrq){
		conn = DBSql.open();
		Vector reZcVector = new Vector();//封装后的转仓明细信息
		Hashtable reTable = null;//接收遍历后的转仓信息
		Hashtable subTable = new Hashtable();//遍历的转仓信息
		for(int i=0;i<zcVector.size();i++){
			reTable = new Hashtable();
			subTable = (Hashtable) zcVector.get(i);
			if(subTable!=null){
				/**生成批次号，并回填至入库单身**/
				reTable.put("PCH", CreatePCHBiz.createPCH(zcrq));//批次号
				
				reTable.put("XH", subTable.get("LH").toString());//料号
				reTable.put("SSSL", subTable.get("CHSL").toString());//出货数量
				reTable.put("DW", subTable.get("DW").toString());//单位
				reTable.put("CGDDH", subTable.get("KHDDH").toString());//客户订单号(AKL)
				reTable.put("LYDH", subTable.get("DDH").toString());//订单号
			}
			
			
			/**根据型号，回填仓库相关信息**/
			Hashtable sysTable = getSysDatas(conn,subTable);
			if(sysTable!=null){
				reTable.put("CKBM", sysTable.get("ckdm").toString());//仓库编码
				reTable.put("KFQBM", sysTable.get("qdm").toString());//库房区编码
				reTable.put("KFDBM", sysTable.get("ddm").toString());//库房道编码
				reTable.put("KFKWDM", sysTable.get("kwdm").toString());//库房库位编码
				reTable.put("KWBH", sysTable.get("kwbh").toString());//库位编号
			}
			
			/**根据型号，回填物料相关信息**/
			Hashtable wlxxTable = getWLXX(conn,pTable,subTable);
			if(wlxxTable!=null){
				reTable.put("WLBH", wlxxTable.get("wlbh").toString());//物料编码
				reTable.put("CPMC", wlxxTable.get("wlmc").toString());//物料名称
				/**根据料号，获取价格管理表中执行日期小于等于转仓日期的最后一次该料号的未税价格和含税价格**/
				Hashtable jgglTabel = dealZCJG(conn, pTable, wlxxTable.get("wlbh").toString());
				if(jgglTabel!=null){
					reTable.put("HSJG", jgglTabel.get("hsjg").toString());//未税价格
					reTable.put("WSJG", jgglTabel.get("wsjg").toString());//含税价格
					reTable.put("WSJE", Double.parseDouble(jgglTabel.get("wsjg").toString()) * Integer.parseInt(subTable.get("SSSL").toString()));//含税金额
					reTable.put("HSJE", Double.parseDouble(jgglTabel.get("hsjg").toString()) * Integer.parseInt(subTable.get("SSSL").toString()));//含税金额
				}
			}
			
			reZcVector.add(reTable);
		}
		//最后关闭数据库连接
		DBSql.close(conn, null, null);
		return reZcVector;
	}
	
	/**
	 * 获取库位相关信息
	 * @param table
	 * @return
	 */
	public Hashtable getSysDatas(Connection conn,Hashtable table){
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable sysTable = null;
		String xh = table.get("LH").toString();
		String sql = "select * from " + HHDJConstant.tableName9 + " where xh = '" + xh + "'";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					sysTable = new Hashtable();
					// 获取库位相关信息
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
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(null, ps, rs);
		}
		return sysTable;
	}
	
	/**
	 * 根据型号，获取物料相关信息
	 * @param table
	 * @return
	 */
	public Hashtable getWLXX(Connection conn,Hashtable pTable,Hashtable table){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable wlxxTable = null;
		String xh = table.get("LH").toString();
		String sql = "select * from " + HHDJConstant.tableName8 + " where xh = '" + xh + "'";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					wlxxTable = new Hashtable();
					// 获取物料相关信息
					String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
					String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
					wlxxTable.put("wlbh", wlbh);
					wlxxTable.put("wlmc", wlmc);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(null, ps, rs);
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
	private Hashtable dealZCJG(Connection conn,Hashtable pTable,String wlbh){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable reTable = null;
		Date zcrq = Date.valueOf(pTable.get("ZCRQ").toString());
		//select * from BO_AKL_JGGL where ZXRQ = (select max(CONVERT(varchar(100), ZXRQ, 23)) from BO_AKL_JGGL where ZXRQ <= '2014-07-21') 
		String sql = "select * from " + HHDJConstant.tableName10 + " where ZXRQ = (select max(CONVERT(varchar(100), ZXRQ, 23)) from " + HHDJConstant.tableName10 + " where ZXRQ <= '"+ zcrq +"') and wlbh = '" + wlbh + "'";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					reTable = new Hashtable();
					double wsjg = rs.getDouble("WSCGJ");//未税价格
					double hsjg = rs.getDouble("HSCGJ");//含税价格
					reTable.put("wsjg", wsjg);
					reTable.put("hsjg", hsjg);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(null, ps, rs);
		}
		return reTable;
	}
}
