package cn.com.akl.shgl.sx.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;

import com.actionsoft.sdk.local.level0.RuleAPI;

public class SXHandle {

	/**
	 * 更新送修单状态
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setSXStatus(Connection conn, int bindid) throws SQLException{
		int updateCount1 = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_SX_P_ZT, bindid);
		int updateCount2 = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_SX_S_ZT, bindid);
		if(updateCount1 != 1) throw new RuntimeException("送修主表状态更新失败！");
		if(updateCount2 <= 0) throw new RuntimeException("送修子表状态更新失败！");
	}
	
	/**
	 * 更新送修子表批次号
	 * @param conn
	 * @param pch
	 * @param id
	 * @throws SQLException
	 */
	public void setFieldPCH(Connection conn, String pch, int bindid)throws SQLException{
		int count = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_SX_PCH, pch, bindid);
		if(count <= 0) throw new RuntimeException("批次号更新失败！");
	}
	
	
	
	/**
	 * 生成送修行号
	 * @param conn
	 * @param bindid
	 * @param sxdh
	 * @return
	 * @throws SQLException
	 */
	public String getSXRowNum(Connection conn, int bindid, String sxdh) throws SQLException {
        Integer rowNum = DAOUtil.getIntOrNull(conn, "SELECT ISNULL(MAX(CONVERT(INT, SUBSTRING(SXCPHH,16,19))),0)+1 FROM BO_AKL_SX_S WHERE BINDID=?", bindid);
        StringBuilder sxrow = new StringBuilder(20);
        if (rowNum == null) {
            return sxrow.append(sxdh).append("-").append(1).toString();
        } else {
            return sxrow.append(sxdh).append("-").append(rowNum).toString();
        }
    }
	
	/**
	 * 更新送修产品行号（赠送和销售时）
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setSXRowNum(Connection conn, final int bindid, final String ywlx) throws SQLException{
		final String sxdh = DAOUtil.getString(conn, SXCnt.QUERY_SXDH, bindid);
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_SXMX, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String id = rs.getString("ID");
				String hh = getSXRowNum(conn, bindid, sxdh);//生成送修产品行号
				String clfs = SXCnt.clfs6;
				if(ywlx.equals(SXCnt.ywlx0)){//赠送
					clfs = SXCnt.clfs7;
				}
				int count = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_SX_S_CLFSAndHH, clfs, hh, id);
				if(count != 1) throw new RuntimeException("送修产品行号更新失败！");
				return true;
			}
		}, bindid);
	}
	
	/**
	 * 更新特批号
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setTPH(Connection conn, int bindid) throws SQLException{
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_SXMX, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String id = rs.getString("ID");
				String sftp = StrUtil.returnStr(rs.getString("SFTP"));
				if(sftp.equals(SXCnt.is)){
					String tph = RuleAPI.getInstance().executeRuleScript(SXCnt.tphScript);
					int tphCount = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_TPH, tph, id);
					if(tphCount != 1) throw new RuntimeException("特批号更新失败！");
				}else{
					DAOUtil.executeUpdate(conn, SXCnt.UPDATE_TPH, SXCnt.empty, id);
				}
				return true;
			}
		}, bindid);
	}
	
	/**
	 * 验证送修子表（行号、属性、状态等值）是否已正确更新
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public void setHHAndHWDMAndSX(Connection conn, String hh, String hwdm, String sx, int id) throws SQLException{
		System.out.println("用于调试该方法无法更新的bug.");
		int count = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_SX_HHAndHWDMAndSX, hh, hwdm, sx, id);
		if(count != 1) throw new RuntimeException("送修子表的属性和货位代码更新失败！");
	}
	
}
