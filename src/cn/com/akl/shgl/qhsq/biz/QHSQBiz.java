package cn.com.akl.shgl.qhsq.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.qhsq.cnt.QHSQCnt;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

public class QHSQBiz {

	/**
	 * ����ȱ����¼
	 * @param conn
	 * @param rs
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public static void insertHander(Connection conn, ResultSet rs, int bindid, String uid, String kfzx, String kfmc) throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		String xmlb = StrUtil.returnStr(rs.getString("XMLB"));
		String wlbh = StrUtil.returnStr(rs.getString("YCPWLBH"));
		String wlmc = StrUtil.returnStr(rs.getString("CPZWMC"));
		String sqsj = StrUtil.returnStr(rs.getString("SQSJ"));
		String sqly = StrUtil.returnStr(rs.getString("SQLY"));
		String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));
		String yxj = StrUtil.returnStr(rs.getString("YXJ"));
		String xh = StrUtil.returnStr(rs.getString("PN"));
		String sx = StrUtil.returnStr(rs.getString("SX"));
		int sl = rs.getInt("SL");
		
		String sxdh = StrUtil.returnStr(rs.getString("SXDH"));
		String sxcphh = StrUtil.returnStr(rs.getString("SXCPHH"));
		String jfcphh = StrUtil.returnStr(rs.getString("JFCPHH"));
		rec.put("SXDH", sxdh);//���޵���
		rec.put("SXCPHH", sxcphh);//���޲�Ʒ�к�
		rec.put("JFCPHH", jfcphh);//������Ʒ�к�
		
//		rec.put("CLJG", cljg);//������
		rec.put("XMLB", xmlb);//��Ŀ���
		rec.put("WLBH", wlbh);//���ϱ��
		rec.put("WLMC", wlmc);//��������
		rec.put("PN", xh);//�����Ʒ�ͺ�
		rec.put("JFKFBM", kfzx);//�����ⷿ����
		rec.put("JFKFMC", kfmc);//�����ⷿ����
		rec.put("SQSJ", sqsj);//����ʱ��
		rec.put("SQLY", sqly);//��������
		rec.put("SL", String.valueOf(sl));//�����Ʒ����
		rec.put("SX", sx);//����
		rec.put("SFJSTH", sfjsth);//�Ƿ�����滻
		rec.put("YXJ", yxj);//���ȼ�
		rec.put("QHFS", QHSQCnt.bhlx1);//ȱ����ʽ
		rec.put("ZT", QHSQCnt.zt0);//ȱ������״̬
		
		/*
		String sqsj = StrUtil.returnStr(rs.getString("SQSJ"));
		String sqly = StrUtil.returnStr(rs.getString("SQLY"));
		String dqjlsh = StrUtil.returnStr(rs.getString("DQJLSH"));
		String bhyy = StrUtil.returnStr(rs.getString("BHYY"));
		String zbsh = StrUtil.returnStr(rs.getString("ZBSH"));
		String zbkc = StrUtil.returnStr(rs.getString("ZBKC"));
		String khlx = StrUtil.returnStr(rs.getString("KHLX"));
		String yhxm = StrUtil.returnStr(rs.getString("YHXM"));
		String yhdh = StrUtil.returnStr(rs.getString("YHDH"));
		String tjr = StrUtil.returnStr(rs.getString("TJR"));
		String bdkckyz = StrUtil.returnStr(rs.getString("BDKCKYZ"));
		
		rec.put("YHMC", yhxm);//�û�����
		rec.put("DH", yhdh);//�绰
		rec.put("SQSJ", sqsj);//����ʱ��
		rec.put("SQLY", sqly);//��������
		rec.put("KHLX", khlx);//�ͻ�����
		rec.put("BDKCKYZ", bdkckyz);//���ؿ�����ֵ
		rec.put("TJR", tjr);//�ύ��
		rec.put("DQJLSH", dqjlsh);//�����������
		rec.put("BHYY", bhyy);//����ԭ��
		rec.put("ZBSH", zbsh);//�ܲ����
		rec.put("ZBKC", zbkc);//�ܲ����
		rec.put("YCPWLBH", );//ԭ��Ʒ���ϱ��
		rec.put("YCPPN", );//ԭ��Ʒ�ͺ�
		rec.put("YCPZWMC", );//ԭ��Ʒ��������
		rec.put("JCSJ", );//���ʱ��
		rec.put("CLJG", );//������
		rec.put("ZT", );//״̬
		rec.put("BZ", );//��ע
*/
		
		try {
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QHJL", rec, bindid, uid);//����ȱ����¼
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * ȱ����¼״̬����
	 * @param conn
	 * @param sql
	 * @param zt
	 * @param bindid
	 * @throws SQLException
	 */
	public static void setStatus(Connection conn, String sql, final String zt, int bindid) throws SQLException{
		DAOUtil.executeQueryForParser(conn, sql, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String xmlb = StrUtil.returnStr(rs.getString("XMLB"));//��Ŀ���
				String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//���޵���
				String sxcphh = StrUtil.returnStr(rs.getString("SXCPHH"));//���޲�Ʒ�к�
				String jfcphh = StrUtil.returnStr(rs.getString("JFCPHH"));//������Ʒ�к�
				String wlbh = StrUtil.returnStr(rs.getString("SQCPWLBH"));//�����Ʒ���ϱ��
				String sx = StrUtil.returnStr(rs.getString("SX"));//����
				String sqsj = StrUtil.returnStr(rs.getString("SQSJ"));//����ʱ��
				String sqly = StrUtil.returnStr(rs.getString("SQLY"));//��������
				String sfqhsq = StrUtil.returnStr(rs.getString("SFQHSQ"));//�Ƿ�ȱ������
				String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));//�Ƿ�����滻
				String yxj = StrUtil.returnStr(rs.getString("YXJ"));//���ȼ�
				
				if(QHSQCnt.no.equals(sfqhsq)){
					DAOUtil.executeUpdate(conn, QHSQCnt.UPDATE_QHJL_ZT, sqsj, sqly, QHSQCnt.zt2, sfjsth, yxj, xmlb, sxdh, wlbh, sx, sxcphh, jfcphh);
				}else{
					int updateRow = DAOUtil.executeUpdate(conn, QHSQCnt.UPDATE_QHJL_ZT, sqsj, sqly, zt, sfjsth, yxj, xmlb, sxdh, wlbh, sx, sxcphh, jfcphh);
					if(updateRow != 1) throw new RuntimeException("ȱ����¼״̬����ʧ�ܣ�");
				}
				return true;
			}
		}, bindid);
	}
	
	/**
	 * ����״̬����
	 * @param conn
	 * @param bindid
	 * @param bhlx
	 * @param zt
	 * @throws SQLException
	 */
	public static void updateStatus(Connection conn, int bindid, String bhlx, String zt) throws SQLException{
		DAOUtil.executeUpdate(conn, QHSQCnt.UPDATE_QHSQ_P_ZT, zt, bindid);//��ͷ״̬����
		if(QHSQCnt.bhlx1.equals(bhlx)){
			DAOUtil.executeUpdate(conn, QHSQCnt.UPDATE_TSSQ_S_ZT, zt, bindid);//�ӱ�״̬���£����⣩
		}else{
			DAOUtil.executeUpdate(conn, QHSQCnt.UPDATE_QHSQ_S_ZT, zt, bindid);//�ӱ�״̬����
		}
	}
	
}
