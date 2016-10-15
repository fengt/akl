package cn.com.akl.shgl.qhbh.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.jf.biz.ReplacementRuleBiz;
import cn.com.akl.shgl.qhbh.cnt.QHBHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class SecurityBiz {

	/**
	 * ��ȫ����Զ����������
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void securityAllocationAndLockMaterial(Connection conn, final int bindid, final String uid) throws SQLException{
		final String ydh = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_QHBHDH, bindid));//��������
		final String bhck = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_FHCKBM, bindid));//�����ֿ�
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_XMLB, bindid));//��Ŀ���
		
		final List<String> list = new ArrayList<String>();
		DAOUtil.executeQueryForParser(conn, QHBHCnt.QUERY_S,
				new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("YCPWLBH"));//�����Ʒ���ϱ��
				String xh = StrUtil.returnStr(rs.getString("YCPPN"));//�����ƷPN
				String sx = StrUtil.returnStr(rs.getString("SX"));//����
				String original_sx = StrUtil.returnStr(rs.getString("SX"));//����
				String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));//�Ƿ�����滻
				String jfkfbm = StrUtil.returnStr(rs.getString("JFKFBM"));//�����ⷿ����
				String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//���޵���
				String sxcphh = StrUtil.returnStr(rs.getString("SXCPHH"));//���޲�Ʒ�к�
				String jfcphh = StrUtil.returnStr(rs.getString("JFCPHH"));//������Ʒ�к�
				int sl = rs.getInt("SL");//����
				int id = rs.getInt("ID");//ID
				
				String sfbgph = StrUtil.returnStr(rs.getString("SFBGPH"));//�Ƿ������
				String bgwlbh = StrUtil.returnStr(rs.getString("BGWLBH"));//������ϱ��
				String bgpn = StrUtil.returnStr(rs.getString("BGPN"));//���PN
				String bgsx = StrUtil.returnStr(rs.getString("BGSX"));//�������
				
				String pn8L = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_PN8L, wlbh));//�ͺ�8L
				String pn9L = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_PN9L, wlbh));//�ͺ�9L
				String pn9L_wlbh = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_PN9L_WLBH, pn9L));//�ͺ�9L��Ϊ8L�����ϱ��
				
				StringBuffer failMessage = new StringBuffer(QHBHCnt.failMessage0);//δ�ɹ����ԭ��
				
				/**
				 * һ��������
				 * �Ƿ������Ϊ��ʱ�����ݱ���ϺŽ��������������ʧ�ܣ�����������
				 */
				boolean flag = false;
				if(QHBHCnt.is.equals(sfbgph)){
					flag = setMaterialHander(conn, bindid, uid, rs, ydh, xmlb, bgwlbh, bgpn, bgsx, bhck, sl, id);
					if(!flag) throw new RuntimeException("������ʧ�ܣ�������ѡ�������ϣ�");
				}
				
				/**
				 * �����������
				 * 1������������ϵĿ��
				 * 2�������治�㣬������Ƿ�����滻����������
				 * 3���������ٴβ��㣬������Ŀ�����ʧ�ܡ��޼���Ŀʱ��ħ�����ƣ���
				 * 		a��RMA��Ʒ������ݣ�9L�ͺ�+DOA��Ʒ�����
				 * 		b����a��治�㣬������Ƿ��滻������У�9L�ͺ�+DOA��Ʒ�����
				 * 4�������治�㣬�����ʧ��
				 * 
				 * finalNum�����ݲ�������ȷ���������������
				 * flag=false�����ʧ��
				 */
				
				int finalNum = getFinalNum(conn, bindid, xmlb, wlbh, sx, bhck, sl).intValue();//���������
				if(!flag){
					flag = setMaterialHander(conn, bindid, uid, rs, ydh, xmlb, wlbh, pn8L, sx, bhck, finalNum, id);
				}
				
				if(!flag && sfjsth.equals(QHBHCnt.is)){//�����滻����������
					flag = setReplaceMaterialHander(conn, bindid, uid, ydh, rs, xmlb, wlbh, pn8L, xh, sx, bhck, finalNum, id, failMessage);
				}
				
				/**
				 * �޼���RMA-->DOA
				 */
				if(!flag && QHBHCnt.xmlb0.equals(xmlb) && QHBHCnt.sx0.equals(sx) && !"".equals(pn9L)){
					sx = QHBHCnt.sx1;
					flag = setMaterialHander(conn, bindid, uid, rs, ydh, xmlb, pn9L_wlbh, pn9L, sx, bhck, finalNum, id);
					if(!flag && QHBHCnt.is.equals(sfjsth)){
						flag = setReplaceMaterialHander(conn, bindid, uid, ydh, rs, xmlb, pn9L_wlbh, pn9L, xh, sx, bhck, finalNum, id, failMessage);
					}
				}
				
				/**
				 * ħ����FGER-->FG
				 */
				if(!flag && QHBHCnt.xmlb1.equals(xmlb) && QHBHCnt.sx2.equals(sx) && !"".equals(pn9L)){
					sx = QHBHCnt.sx3;
					flag = setMaterialHander(conn, bindid, uid, rs, ydh, xmlb, pn9L_wlbh, pn9L, sx, bhck, finalNum, id);
					if(!flag && QHBHCnt.is.equals(sfjsth)){
						flag = setReplaceMaterialHander(conn, bindid, uid, ydh, rs, xmlb, pn9L_wlbh, pn9L, xh, sx, bhck, finalNum, id, failMessage);
					}
				}
				
				
				/**
				 * ����������������
				 * ����ɹ�������ȱ����¼״̬
				 * ���ʧ�ܣ�����ʧ��ԭ�򣬹���ʧ�ܼ�¼
				 */
				if(flag && finalNum != 0){
					int count = DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_QHJL_ZT, QHBHCnt.qhzt0, xmlb, wlbh, original_sx, jfkfbm, sfjsth, QHBHCnt.qhzt2, sxdh, sxcphh, jfcphh);//����ȱ����¼��״̬
					if(count != 1) throw new RuntimeException("ȱ����¼״̬����ʧ�ܣ�");
				}else{
					list.add(xh);
					DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_QHJL_SCWCGPHYY, failMessage.toString(), xmlb, wlbh, original_sx, jfkfbm, sfjsth, QHBHCnt.qhzt2);//����δ�ɹ����ԭ��
					DAOUtil.executeUpdate(conn, QHBHCnt.DELETE_QHBH_S, id);//ɾ��δ�ɹ������¼
//					throw new RuntimeException("�ò�Ʒ��"+xh+"���޿�棬���޷������");
				}
				return true;
			}
			public void destory(Connection conn) throws SQLException{
				Integer count = DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_S_count, bindid);
				if(count == null || count == 0){
					throw new RuntimeException("�õ�û�п�����ɹ������ϣ����޷�����");
				}
				if(list.size() > 0)
					MessageQueue.getInstance().putMessage(uid, "�ͺ����£�"+list.toString()+"δ����ɹ����ѱ�ϵͳ�Զ����ˣ��ش����ѣ�");
			}
		}, bindid);
	}
	
	/**
	 * ���ݲ��������жϲ�������
	 * @param conn
	 * @param bindid
	 * @param xmlb
	 * @param wlbh
	 * @param sx
	 * @param bhck
	 * @param sl
	 * @return
	 * @throws SQLException
	 */
	public BigDecimal getFinalNum(Connection conn, int bindid, String xmlb, String wlbh, String sx, String bhck, int sl) throws SQLException{
		String bhlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_BHLX, bindid));//��������
		int kczl = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_Sum, xmlb, wlbh, sx, bhck));//������������
		int lockAllNum = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_SK_All_SUM, xmlb, wlbh, bhck, sx));//��������
		int kcxx = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_LimtInventory, xmlb, bhck, wlbh, sx));//����������С��
		int allShortage = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_All_Shortage, bindid, xmlb, wlbh, sx));//��������ȱ����
		
		BigDecimal bd1 = new BigDecimal(sl);
		BigDecimal bd2 = new BigDecimal(allShortage);
		BigDecimal finalNum;
		if(QHBHCnt.bhlx0.equals(bhlx)){
			finalNum = bd1;
		}else{
			int kyz = kczl - lockAllNum - kcxx;//������ֵ = ������� - �������� - �������
			BigDecimal bd3 = new BigDecimal(kyz);
			if(allShortage > kyz && kyz > 0){//��治��ʱ����������������ͷ�
				finalNum = bd1.divide(bd2, 2, BigDecimal.ROUND_HALF_EVEN).multiply(bd3);//ʵ��������� = ȱ������ / ��ȱ���� * ������ֵ
			}else{//�����ʱ���������
				finalNum = bd1;
			}
		}
		return finalNum;
	}
	
	
	/**
	 * �����滻�������������
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ydh
	 * @param xmlb
	 * @param wlbh
	 * @param sx
	 * @param sqcpsl
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public boolean setReplaceMaterialHander(Connection conn, int bindid, String uid, String ydh, ResultSet rest,
			String xmlb, String wlbh, String pn, String xh, String sx, String ckdm, int sl, int id, StringBuffer failMessage)
			throws SQLException{
		boolean mark = false;
		ReplacementRuleBiz ruleBiz = new ReplacementRuleBiz();
		List<String> replaceWlbhList = ruleBiz.replaceMaterial(conn, xmlb, wlbh, sx);

		if (replaceWlbhList.size() != 0) {
			// ƴ�Ӵ���������.
			StringBuilder replaceWlbhSb = new StringBuilder(50);
			replaceWlbhSb.append(replaceWlbhList.get(0));
			for (int i = 1; i < replaceWlbhList.size(); i++) {
				replaceWlbhSb.append(",");
				replaceWlbhSb.append(replaceWlbhList.get(i));
			}
		} else {
//			throw new RuntimeException("���ͺš�"+xh+"��û�п��滻��Ϣ��");
			failMessage = failMessage.replace(0, failMessage.length(), QHBHCnt.failMessage1);
			return mark;
		}

		// �ҵ��п�������.
		for (String reWlbh : replaceWlbhList) {
			boolean flag = setMaterialHander(conn, bindid, uid, rest, ydh, xmlb, reWlbh, pn, sx, ckdm, sl, id);//���flag=false�����ʧ��
			if(flag){
				mark = true;
			}else{
				failMessage = failMessage.replace(0, failMessage.length(), QHBHCnt.failMessage2);
			}
		}
		return mark;
	}
	
	
	/**
	 * ���滻�������������
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ydh
	 * @param xmlb
	 * @param wlbh
	 * @param sx
	 * @param sqcpsl
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public boolean setMaterialHander(Connection conn, int bindid, String uid, ResultSet rest, String ydh, 
			String xmlb, String wlbh, String pn, String sx, String ckdm, int sl, int id) throws SQLException{
		String wlmc = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_WLMC, wlbh));//��������
		String xh = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_PN8L, wlbh));//LPN8
		PreparedStatement ps = null;
		ResultSet rs = null;
		int total = sl;
		int count = 0;
		try {
			ps = conn.prepareStatement(QHBHCnt.QUERY_KCMX_PCHAndHWDM);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, wlbh, sx, ckdm);
			while(total > 0 && rs.next()){
				String pch = rs.getString("PCH");
				String hwdm = rs.getString("HWDM");
				int kwsl = rs.getInt("KWSL");
				int lockNum = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_SK_SUM, xmlb, wlbh, pch, hwdm, sx));//��������
				if(kwsl > lockNum){
					int useNum = kwsl - lockNum;
					if(total > useNum){
						allocation(conn, bindid, uid, ydh, rest, xmlb, wlbh, wlmc, xh, pch, ckdm, hwdm, sx, useNum, id, count);
						total -= useNum;
					}else{
						allocation(conn, bindid, uid, ydh, rest, xmlb, wlbh, wlmc, xh, pch, ckdm, hwdm, sx, total, id, count);
						total = 0;
					}
					
				}
				count ++;
			}
		} finally{
			DBSql.close(null, ps, rs);
		}
		return total == 0;
	}
	
	/**
	 * ���ݿ�����ʵ�ʲ�������
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ydh
	 * @param rest
	 * @param xmlb
	 * @param wlbh
	 * @param wlmc
	 * @param pn
	 * @param pch
	 * @param hwdm
	 * @param sx
	 * @param sl
	 * @param id
	 * @param count
	 * @throws SQLException
	 */
	public void allocation(Connection conn, int bindid, String uid, String ydh, ResultSet rest,
			String xmlb, String wlbh, String wlmc, String pn, String pch, String ckdm, String hwdm, String sx, int sl, int id, int count) throws SQLException{
		
		/**
		 * 1���������κͻ�λ��
		 * count=0�ǵ�һ�������a.���»�λ����;b.����
		 * count!=0ʱ��a.����һ���¼�¼;b.����
		 */
		if(count == 0){
			int updateCount = DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_S_PCAndHWDM, wlbh, pn, wlmc, sl, sx, pch, hwdm, QHBHCnt.qhzt0, id);
			if(updateCount != 1) throw new RuntimeException("���λ��λ����ʧ�ܣ�");
		}else{
			copyRecord(conn, bindid, uid, rest, pch, hwdm, sl, sx);
		}
		
		/**
		 * 2��������������
		 */
		int isLockExsit = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.isLockExsit, ydh, xmlb, wlbh, pch, hwdm, sx));
		if(isLockExsit >= 1){
			int n = DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_LockNum, sl, ydh, xmlb, wlbh, pch, hwdm, sx);
			if(n != 1) throw new RuntimeException("����������ʧ�ܣ�");
		}else{
			try {
				LockBiz.insertSK(conn, bindid, uid, ydh, xmlb, wlbh, pn, pch, ckdm, hwdm, sx, sl);
			} catch (AWSSDKException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * ���Ʋ�ֵĲ�����¼
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param rs
	 * @param pch
	 * @param hwdm
	 * @param sl
	 * @throws SQLException
	 */
	public void copyRecord(Connection conn, int bindid, String uid, ResultSet rs, String pch, String hwdm, int sl, String phsx) throws SQLException{
		Hashtable<String, String> body = new Hashtable<String, String>();
		String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//���޵���
		String sqsj = StrUtil.returnStr(rs.getString("SQSJ"));//����ʱ��
		String sqly = StrUtil.returnStr(rs.getString("SQLY"));//��������
		String cljg = StrUtil.returnStr(rs.getString("CLJG"));//������
		String sqcpsl = StrUtil.returnStr(rs.getString("SL"));//�����Ʒ����
		String sx = StrUtil.returnStr(rs.getString("SX"));//����
		String zt = StrUtil.returnStr(rs.getString("ZT"));//״̬
		String bz = StrUtil.returnStr(rs.getString("BZ"));//��ע
		String xmlb = StrUtil.returnStr(rs.getString("XMLB"));//��Ŀ���
		String wlbh = StrUtil.returnStr(rs.getString("YCPWLBH"));//���ϱ��
		String wlmc = StrUtil.returnStr(rs.getString("YCPZWMC"));//��������
		String jfkfbm = StrUtil.returnStr(rs.getString("JFKFBM"));//�����ⷿ����
		String jfkfmc = StrUtil.returnStr(rs.getString("JFKFMC"));//�����ⷿ����
		String sxcphh = StrUtil.returnStr(rs.getString("SXCPHH"));//���޲�Ʒ�к�
		String pn = StrUtil.returnStr(rs.getString("YCPPN"));//PN
		String yxj = StrUtil.returnStr(rs.getString("YXJ"));//���ȼ�
		String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));//�Ƿ�����滻
		String qhfs = StrUtil.returnStr(rs.getString("PHFS"));//ȱ����ʽ
		
		String phcpwlbh = StrUtil.returnStr(rs.getString("SQCPWLBH"));//�����Ʒ���ϱ��
		String phcppn = StrUtil.returnStr(rs.getString("SQCPPN"));//�����ƷPN
		String phcpmc = StrUtil.returnStr(rs.getString("SQCPZWMC"));//�����Ʒ��������
		
		String sxwlmc = StrUtil.returnStr(rs.getString("SXCPMC"));//���޲�Ʒ����
		String sxpn = StrUtil.returnStr(rs.getString("SXCPPN"));//����PN
		String bdkcsl = StrUtil.returnStr(rs.getString("BDKCKYZ"));//���ؿ�����ֵ
		
		String sfbgph = StrUtil.returnStr(rs.getString("SFBGPH"));//�Ƿ������
		String bgwlbh = StrUtil.returnStr(rs.getString("BGWLBH"));//������ϱ��
		String bgwlmc = StrUtil.returnStr(rs.getString("BGWLMC"));//�����������
		String bgpn = StrUtil.returnStr(rs.getString("BGPN"));//���PN
		String bgsx = StrUtil.returnStr(rs.getString("BGSX"));//�������
		
//		String phcpsl = StrUtil.returnStr(rs.getString("SQCPSL"));//�����Ʒ����
//		String jfcphh = StrUtil.returnStr(rs.getString("JFCPHH"));//������Ʒ�к�
//		String hh = RuleAPI.getInstance().executeRuleScript("@sequence:(#BO_AKL_QHBH_S)");//�к�
		
		
//		body.put("HH", hh);//�к�
		body.put("SXDH", sxdh);//���޵���
		body.put("SQSJ", sqsj);//����ʱ��
		body.put("SQLY", sqly);//��������
		body.put("CLJG", cljg);//������
		body.put("SL", sqcpsl);//�����Ʒ����
		body.put("SX", sx);//����
		body.put("ZT", zt);//״̬
		body.put("BZ", bz);//��ע
		body.put("XMLB", xmlb);//��Ŀ���
		body.put("YCPWLBH", wlbh);//�����Ʒ���ϱ��
		body.put("YCPZWMC", wlmc);//�����Ʒ��������
		body.put("JFKFBM", jfkfbm);//�ջ��ⷿ����
		body.put("JFKFMC", jfkfmc);//�ջ��ⷿ����
		body.put("SXCPHH", sxcphh);//���޲�Ʒ�к�
		body.put("YCPPN", pn);//�����ƷPN
		body.put("YXJ", yxj);//���ȼ�
		body.put("SFJSTH", sfjsth);//�Ƿ�����滻
		body.put("PHFS", qhfs);//�����ʽ
		
		body.put("SQCPWLBH", phcpwlbh);//�����Ʒ���ϱ��
		body.put("SQCPPN", phcppn);//�����ƷPN
		body.put("SQCPZWMC", phcpmc);//�����Ʒ��������

		body.put("SXCPMC", sxwlmc);//���޲�Ʒ����
		body.put("SXCPPN", sxpn);//����PN
		body.put("BDKCKYZ", bdkcsl);//���ؿ�����ֵ
		
		body.put("SQCPSL", String.valueOf(sl));//�����Ʒ����
		body.put("PHSX", phsx);//�������
		body.put("PCH", pch);//���κ�
		body.put("HWDM", hwdm);//��λ����
		
		body.put("SFBGPH", sfbgph);//�Ƿ������
		body.put("BGWLBH", bgwlbh);//������ϱ��
		body.put("BGWLMC", bgwlmc);//�����������
		body.put("BGPN", bgpn);//���PN
		body.put("BGSX", bgsx);//�������
		
		//��������
		try {
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QHBH_S", body, bindid, uid);
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}
	}
	
}
