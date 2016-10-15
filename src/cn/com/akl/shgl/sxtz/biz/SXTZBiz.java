package cn.com.akl.shgl.sxtz.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.sx.biz.KCBiz;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;
import cn.com.akl.util.StrUtil;

import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;

public class SXTZBiz {

	KCBiz kcBiz = new KCBiz();
	/**
     * �����ܣ���
     * �����ϸ����
     * ���кţ�ɾ��
     * �¼�¼���������̲���
     * 
     * �������������ѽ������򽻸����ӻؿ�棻��δ����������������޵����ݣ�����ʾɾ�������ٵ���������
     */
	
	public void repositoryHandle(Connection conn, int bindid, String uid, 
			String parentBindid, final String xmlb) throws SQLException{
		String ckbm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_XMKF, bindid));//�ͷ��ֿ����
		String ywlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_YWLX, bindid));//ҵ������
		
		DAOUtil.executeQueryForParser(conn, SXTZConstant.QUERY_YSX_S, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
				String sx = StrUtil.returnStr(rs.getString("SX"));
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));
				String gztm = StrUtil.returnStr(rs.getString("GZTM"));
				String clfs = StrUtil.returnStr(rs.getString("CLFS"));
				int sl = rs.getInt("SL");
				if(SXCnt.clfs8.equals(clfs)) return true;
				int updateCount1 = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_KCHZ, -sl, -sl, xmlb, wlbh, pch);
				int updateCount2 = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_KCMX_KWSL, -sl, xmlb, wlbh, hwdm, pch, sx);
				if(updateCount1 != 1 || updateCount2 != 1) throw new RuntimeException("���޵���������ʧ�ܣ�");
				DAOUtil.executeUpdate(conn, SXTZConstant.DELETE_XLH, gztm);//ɾ�����к�
				return true;
			}
		}, bindid);
		
		repositoryInsert(conn, bindid, uid, parentBindid, xmlb, ywlx, ckbm);
	}
	
	/**
	 * �����ܺ���ϸ����
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param parentBindid
	 * @param xmlb
	 * @param ywlx
	 * @param ckdm
	 * @throws SQLException
	 */
	public void repositoryInsert(Connection conn, final int bindid, final String uid, 
			final String parentBindid, final String xmlb, final String ywlx, final String ckdm) throws SQLException{
		final String sxdh = DAOUtil.getString(conn, SXTZConstant.QUERY_FORM_SXDH, bindid);
		
		//1�����������
		DAOUtil.executeQueryForParser(conn, SXTZConstant.QUERY_SXTZ_S_HZ, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				kcBiz.insertKCHZ(conn, Integer.parseInt(parentBindid), uid, rs, xmlb, pch);
				return true;
			}
		}, bindid);
		
		//2����������ϸ
		DAOUtil.executeQueryForParser(conn, SXTZConstant.QUERY_NEW_FORM_BODY, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String hh = getSXRowNum(conn, bindid, sxdh);//�������޲�Ʒ�к�
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
				insertKCMX(conn, Integer.parseInt(parentBindid), uid, body, xmlb, clfs);
				
				/**
				 * 2���������к���ϸ
				 * ����������ʽΪ���»򸴼�����&&�������벻��Ϊ��
				 */
				if(!"".equals(gztm)&&(clfs.equals(SXCnt.clfs0)||clfs.equals(SXCnt.clfs1))){
					kcBiz.insertXLHMX(conn, Integer.parseInt(parentBindid), uid, xlhBody, xmlb, gztm);
				}
				
				/**
				 * 3��������������ӱ��кš���λ��������ԣ�
				 */
				if(SXCnt.clfs4.equals(clfs) || SXCnt.clfs5.equals(clfs)){//���ڡ�����ά�ޣ�������������
					tempSX = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_SRSX, xmlb, clfs, ywlx));
				}
				
				int count = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_SXTZ_HHAndHWDMAndSX, hh, tempHW, tempSX, id);
				if(count != 1) throw new RuntimeException("�����ӱ�����Ժͻ�λ�������ʧ�ܣ�");
				return true;
			}
		}, bindid);
	}
	
	/**
	 * ��������ϸ
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param body
	 * @param xmlb
	 * @throws SQLException
	 */
	public void insertKCMX(Connection conn, int bindid, String uid, Hashtable<String, String> body, String xmlb, String clfs) throws SQLException{
		String wlbh = body.get("WLBH");
		String sx = body.get("SX");
		String pch = body.get("PCH");
		String hwdm = body.get("HWDM");
		int sl = Integer.parseInt(body.get("KWSL"));
		try {
			/**1���������¿����ϸ*/
			int n = DAOUtil.getInt(conn, SXCnt.QUERY_isExistKCMX, xmlb, wlbh, sx, hwdm, pch, SXCnt.zt0);
			if(!SXCnt.clfs8.equals(clfs)){
				if(n == 0){
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", body, bindid, uid);
				}else{
					int updateCount = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCMX_KWSL, sl, xmlb, wlbh, hwdm, pch, sx);
					if(updateCount != 1) throw new RuntimeException("���޵��ݵ��������ϸ����ʧ�ܣ�");
				}
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * �������޵����Ĳ�Ʒ�к�
	 * @param conn
	 * @param bindid
	 * @param sxdh
	 * @return
	 * @throws SQLException
	 */
	public String getSXRowNum(Connection conn, int bindid, String sxdh) throws SQLException {
        Integer rowNum = DAOUtil.getIntOrNull(conn, "SELECT ISNULL(MAX(CONVERT(INT, SUBSTRING(SXCPHH,16,19))),0)+1 FROM BO_AKL_SH_SXTZ_S WHERE BINDID=?", bindid);
        StringBuilder sxrow = new StringBuilder(20);
        if (rowNum == null) {
            return sxrow.append(sxdh).append("-").append(1).toString();
        } else {
            return sxrow.append(sxdh).append("-").append(rowNum).toString();
        }
    }
	
	
	/**==========================================================================================================================*/
	/**
	 * �����������ӿ��|�����ɾ�ʼġ�ɾ������������
	 * @param conn
	 * @param bindid
	 * @param jfdh
	 * @param xmlb
	 * @throws SQLException
	 */
	public void jfdHandle(Connection conn, int bindid, String jfdh, final String xmlb) throws SQLException{
		/**
		 * 1���ӿ��
		 */
		DAOUtil.executeQueryForParser(conn, SXTZConstant.QUERY_JF_S, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
				String sx = StrUtil.returnStr(rs.getString("SX"));
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));
				int sl = rs.getInt("SL");
				int updateCount1 = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_KCHZ, sl, sl, xmlb, wlbh, pch);
				int updateCount2 = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_KCMX_KWSL, sl, xmlb, wlbh, hwdm, pch, sx);
				if(updateCount1 != 1 || updateCount2 != 1) throw new RuntimeException("���޵���������ʧ�ܣ�");
				return true;
			}
		}, jfdh);
		
		/**
		 * 2�������
		 */
		DAOUtil.executeQueryForParser(conn, SXTZConstant.QUERY_JF_S_PJ, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
				String sx = StrUtil.returnStr(rs.getString("SX"));
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));
				int sl = rs.getInt("SL");
				int updateCount1 = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_KCHZ, sl, xmlb, wlbh, pch);
				int updateCount2 = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_KCMX_KWSL, sl, xmlb, wlbh, hwdm, pch, sx);
				if(updateCount1 != 1 || updateCount2 != 1) throw new RuntimeException("���޵���������ʧ�ܣ�");
				return true;
			}
		}, jfdh);
		
		/**
		 * 3��ɾ�ʼ���Ϣ
		 */
		String sfyj = DAOUtil.getString(conn, SXTZConstant.QUERY_JF_SFYJ, jfdh);
		if(XSDDConstant.YES.equals(sfyj)){
			DAOUtil.executeUpdate(conn, SXTZConstant.DELETE_JF_DFH_P, jfdh);
			DAOUtil.executeUpdate(conn, SXTZConstant.DELETE_JF_DFH_S, jfdh);
		}
		
		/**
		 * 4��ɾ������������
		 */
		
		try {
			int jfBindid = DAOUtil.getInt(conn, SXTZConstant.QUERY_JF_BINDID, jfdh);
			WorkflowInstanceAPI.getInstance().removeProcessInstance(jfBindid);
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(" ��������ɾ��ʧ�ܣ�");
		}
	}
	
	
}
