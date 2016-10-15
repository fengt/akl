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
	 * �������޵�״̬
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setSXStatus(Connection conn, int bindid) throws SQLException{
		int updateCount1 = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_SX_P_ZT, bindid);
		int updateCount2 = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_SX_S_ZT, bindid);
		if(updateCount1 != 1) throw new RuntimeException("��������״̬����ʧ�ܣ�");
		if(updateCount2 <= 0) throw new RuntimeException("�����ӱ�״̬����ʧ�ܣ�");
	}
	
	/**
	 * ���������ӱ����κ�
	 * @param conn
	 * @param pch
	 * @param id
	 * @throws SQLException
	 */
	public void setFieldPCH(Connection conn, String pch, int bindid)throws SQLException{
		int count = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_SX_PCH, pch, bindid);
		if(count <= 0) throw new RuntimeException("���κŸ���ʧ�ܣ�");
	}
	
	
	
	/**
	 * ���������к�
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
	 * �������޲�Ʒ�кţ����ͺ�����ʱ��
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setSXRowNum(Connection conn, final int bindid, final String ywlx) throws SQLException{
		final String sxdh = DAOUtil.getString(conn, SXCnt.QUERY_SXDH, bindid);
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_SXMX, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String id = rs.getString("ID");
				String hh = getSXRowNum(conn, bindid, sxdh);//�������޲�Ʒ�к�
				String clfs = SXCnt.clfs6;
				if(ywlx.equals(SXCnt.ywlx0)){//����
					clfs = SXCnt.clfs7;
				}
				int count = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_SX_S_CLFSAndHH, clfs, hh, id);
				if(count != 1) throw new RuntimeException("���޲�Ʒ�кŸ���ʧ�ܣ�");
				return true;
			}
		}, bindid);
	}
	
	/**
	 * ����������
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
					if(tphCount != 1) throw new RuntimeException("�����Ÿ���ʧ�ܣ�");
				}else{
					DAOUtil.executeUpdate(conn, SXCnt.UPDATE_TPH, SXCnt.empty, id);
				}
				return true;
			}
		}, bindid);
	}
	
	/**
	 * ��֤�����ӱ��кš����ԡ�״̬��ֵ���Ƿ�����ȷ����
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public void setHHAndHWDMAndSX(Connection conn, String hh, String hwdm, String sx, int id) throws SQLException{
		System.out.println("���ڵ��Ը÷����޷����µ�bug.");
		int count = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_SX_HHAndHWDMAndSX, hh, hwdm, sx, id);
		if(count != 1) throw new RuntimeException("�����ӱ�����Ժͻ�λ�������ʧ�ܣ�");
	}
	
}
