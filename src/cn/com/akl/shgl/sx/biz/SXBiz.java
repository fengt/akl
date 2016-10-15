package cn.com.akl.shgl.sx.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;
import cn.com.akl.util.StrUtil;

public class SXBiz {

	private KCBiz kcBiz = new KCBiz();
	private SXHandle sxHandle = new SXHandle();
	
	/**
	 * ���¿����ϸ�Ͳ������к���ϸ�������ܵ�һ�ڵ��Ѳ��룩
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ckbm
	 * @param xmlb
	 * @throws SQLException
	 */
	public void insertALL(Connection conn, final int bindid, final String uid, 
			final String ckdm, final String xmlb, final String ywlx)throws SQLException{
		final String sxdh = DAOUtil.getString(conn, SXCnt.QUERY_SXDH, bindid);
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_SXMX, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String hh = sxHandle.getSXRowNum(conn, bindid, sxdh);//�������޲�Ʒ�к�
				String clfs = StrUtil.returnStr(rs.getString("CLFS"));
				String gztm = StrUtil.returnStr(rs.getString("GZTM"));
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				String xh = StrUtil.returnStr(rs.getString("XH"));
				
				String sx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_SRSX, xmlb, clfs, ywlx));
				if("".equals(clfs)){
					throw new RuntimeException("�����ӱ�����ʽ����ȡʧ�ܣ�");
				}else if(clfs.equals(SXCnt.clfs2)||clfs.equals(SXCnt.clfs3)){
					throw new RuntimeException("���ͺ�["+xh+"]����ʽѡ���������飡");
				}else if(SXCnt.clfs4.equals(clfs) || SXCnt.clfs5.equals(clfs)){//���ڡ���������ת��Ϊ�����������
					sx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_HCSX, xmlb, clfs, ywlx));
				}
				//�޷����ݴ���ʽ��ȡ���ԣ���Ϊ��Ʒ
				if("".equals(sx)){
					sx = SXCnt.sx1;
				}
				
				Hashtable<String, String> body = kcBiz.getKCMX(conn, rs, ckdm, xmlb, ywlx, pch, sx);
				@SuppressWarnings("unchecked")
				Hashtable<String, String> xlhBody = (Hashtable<String, String>)body.clone();
				
				@SuppressWarnings("unchecked")//��Ҫ�Ǳ����λ�������ʱ���һ�ڵ㲻ͬ���������»�ȡ
				Hashtable<String, String> tempBody = (Hashtable<String, String>)body.clone();
				int id = Integer.parseInt(tempBody.get("KEYID"));//ΨһID
				String tempSX = tempBody.get("SX");//��ʱ����
				String tempHW = tempBody.get("HWDM");//��ʱ��λ
				
				/**
				 * 1�����¿����ϸ
				 */
				kcBiz.insertKCMX(conn, bindid, uid, body, xmlb, clfs);
				
				/**
				 * 2���������к���ϸ
				 * ����������ʽΪ���»򸴼�����&&�������벻��Ϊ��
				 */
				if(!"".equals(gztm)&&(clfs.equals(SXCnt.clfs0)||clfs.equals(SXCnt.clfs1))){
					kcBiz.insertXLHMX(conn, bindid, uid, xlhBody, xmlb, gztm);
				}
				
				/**
				 * 3��������������ӱ��кš���λ��������ԣ�
				 */
				if(SXCnt.clfs4.equals(clfs) || SXCnt.clfs5.equals(clfs)){//���ڡ�����ά�ޣ�������������
					tempSX = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_SRSX, xmlb, clfs, ywlx));
				}
				sxHandle.setHHAndHWDMAndSX(conn, hh, tempHW, tempSX, id);
				return true;
			}
		}, bindid);
		
	}
	
	/**
	 * ������Ʒ���
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void decreaseDYP(Connection conn, int bindid)throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMLB, bindid));//��Ŀ���
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_DYP_DE, 
				new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				int count1 = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCHZ_DYP_DE, rs.getInt("SL"), rs.getInt("SL"), xmlb, rs.getString("WLBH"), pch);
				int count2 = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCMX_DYP_DE, rs.getInt("SL"), xmlb, rs.getString("WLBH"), pch, rs.getString("HWDM"));
				if(count2 != 1){
					throw new RuntimeException(" ����Ʒ���ۼ�ʧ�ܣ�");
				}
				return true;
			}
		}, bindid);
	}
	
	/**
	 * �Ӵ���Ʒ���
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void increaseDYP(Connection conn, int bindid)throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMLB, bindid));//��Ŀ���
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_DYP_IN, 
				new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCHZ_DYP_IN, rs.getInt("SL"), rs.getInt("SL"), xmlb, rs.getString("WLBH"), rs.getString("PCH"));
				DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCMX_DYP_IN, rs.getInt("SL"), rs.getString("WLBH"), rs.getString("HWDM"), rs.getString("PCH"));
				return true;
			}
		}, bindid);
	}
	
	
}
