package cn.com.akl.shgl.qhbh.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.qhbh.cnt.QHBHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class LockBiz {

	/**
	 * ��֤����Ƿ����.
	 */
	public static ResultPaserAbs checkInvetoryPaser() {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet rs) throws SQLException {
				String xmlb = StrUtil.returnStr(rs.getString("XMLB"));//��Ŀ���
				String wlbh = StrUtil.returnStr(rs.getString("SQCPWLBH"));//���ϱ��
				String pch = StrUtil.returnStr(rs.getString("PCH"));//���κ�
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));//��λ����
				String sx = StrUtil.returnStr(rs.getString("PHSX"));//�������
				
				Integer sdsl = DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_SK_SUM, xmlb, wlbh, pch, hwdm, sx);//��������
				Integer kwsl = DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_KCMX_SUM, xmlb, wlbh, pch, hwdm, sx);//��λ����
				int sl = rs.getInt("SL");
				if (kwsl - sdsl < sl) {
					throw new RuntimeException("���Ϻ�:" + rs.getString("WLBH") + "�� �ͺ�:" + rs.getString("XH") + "�� ���κ�:" + rs.getString("PCH")
							+ "���������㣬���ܿ���ѱ�������");
				}
				return true;
			}
		};
	}
	
	
	/**
	 * ���������¼..
	 */
	public static ResultPaserAbs insertLockPaser(final int bindid, final String uid, final String dbdh) {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet rs) throws SQLException {
				try {
					insertSK(conn, bindid, uid, dbdh, rs.getString("XMLB"), rs.getString("SQCPWLBH"), rs.getString("SQCPPN"), rs.getString("PCH"), rs.getString("JFKFBM"), rs.getString("HWDM"), rs.getString("PHSX"), rs.getInt("SL"));
				} catch (AWSSDKException e) {
					throw new RuntimeException(e);
				}
				return true;
			}
		};
	}
	
	
	/**
	 * ���������¼.
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ddh
	 * @param pch
	 * @param wlbh
	 * @param ckdm
	 * @param ddsl
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public static void insertSK(Connection conn, int bindid, String uid, String ydh, String xmlb, String wlbh, String xh, String pch, String ckdm, String hwdm, String sx, int ddsl) 
			throws SQLException, AWSSDKException {
		// �����¼
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		hashtable.put("YDH", ydh);
		hashtable.put("XMLB", xmlb);
		hashtable.put("WLBH", wlbh);
		hashtable.put("XH", xh);
		hashtable.put("PCH", pch);
		hashtable.put("CKDM", ckdm);
		hashtable.put("HWDM", hwdm);
		hashtable.put("SX", sx);
		hashtable.put("SDSL", String.valueOf(ddsl));
		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SH_KCSK", hashtable, bindid, uid);
	}
	
	public static int nullParse(Integer i){
		return i == null ? 0 : i.intValue();
	}
}
