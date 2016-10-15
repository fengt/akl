package cn.com.akl.ccgl.cgrk.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
/**
 * �����뵥������Ψһ
 * @author ActionSoft_2013
 *
 */
public class CheckBiz {

	private static Connection conn = null;
	
	/**
	 * a.У���Ϻ��Ƿ���ڼ����ظ�;b.���ݿͻ������ж��Ƿ�У��ת����Ϣ
	 * @param uc
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public static boolean XHCheck(UserContext uc,Vector vector,int bindid){
		for (int i = 0; i < vector.size(); i++) {
			Hashtable rec = (Hashtable)vector.get(i);
			String xh = rec.get("LH").toString();
			String cgddh = rec.get("KHDDH").toString();
			
			String str1 = "select count(distinct(KHDM)) n from " + CgrkCnt.tableName2 + " where bindid = " + bindid;
			int n = DBSql.getInt(str1, "n");
			if(n > 1){
				MessageQueue.getInstance().putMessage(uc.getUID(), "�������ת����Ϣ�пͻ����벻Ψһ�������µ��룡");
				return false;
			}else{
				String str2 = "select top 1 khdm from " + CgrkCnt.tableName2 + " where bindid = " + bindid;
				String khdm = DBSql.getString(str2, "khdm");
				String str3 = "select * from " + CgrkCnt.tableName14 + " where gyskhbh = '"+khdm+"'";
				String hzbm = DBSql.getString(str3, "KHBH");
				String str0 = "select count(*) xh from " + CgrkCnt.tableName8 + " where xh = '"+xh+"' and hzbm = '"+hzbm+"'";
				int isXh = DBSql.getInt(str0, "xh");
				/*a.У���������Ա����Ƿ���ڴ��ͺ�*/
				if(isXh <= 0){
					MessageQueue.getInstance().putMessage(uc.getUID(), "�������ת����Ϣ�У��ͺ�Ϊ��"+xh+"�������ϲ�������������Ϣ�У���˲飡");
					return false;
				}else{
					List list = DealUtil(bindid,cgddh);
					if(list.size()>0 && list.size()<15){
						MessageQueue.getInstance().putMessage(uc.getUID(), "�ͻ������š�"+cgddh+"�������ظ����Ϻš���Ϣ��" + list.toString());
						return false;
					}else if(list.size()>=15){
						MessageQueue.getInstance().putMessage(uc.getUID(), "�ͻ������š�"+cgddh+"�������ظ����Ϻš���Ϣ����ȥ�غ����°���");
						return false;
					}
				}
				/*b.���ݿͻ������ж��Ƿ����ת����ϢУ��*/
				if(khdm.equals(CgrkCnt.khdm0)){
					/*ת����Ϣ��ɹ���������У��*/
					return ZcxxDataCheck(uc,vector);
				}
			}
		}
		return true;
	}
	
	/**
	 * ����ͬһ�ɹ������Ϻ��Ƿ�Ψһ
	 * @param bindid
	 * @return
	 */
	public static List DealUtil(int bindid,String cgddh){
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> list = new ArrayList<String>();
		String sql = "select lh from " + CgrkCnt.tableName2 + " where bindid = " + bindid + " and khddh = '"+cgddh+"' group by lh having count(lh)>1 ";
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					String lh = StrUtil.returnStr(rs.getString("lh"));
					if(StrUtil.isNotNull(lh)){
						list.add(lh);
					}
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, ps, rs);
		}
		return list;
	}
	
	/**
	 * ����ת����Ϣ��ɹ���������У��(�ͻ�����Ϊ��khdm0����ʹ��)
	 * @param vector
	 * @return
	 */
	public static boolean ZcxxDataCheck(UserContext uc,Vector vector){
		
		int wrksl = 0; //δ�������
		for (int i = 0; i < vector.size(); i++) {
			Hashtable rec = (Hashtable)vector.get(i);
			String xh = rec.get("LH").toString();
			String khddh = rec.get("KHDDH").toString();
			int chsl = Integer.parseInt(rec.get("CHSL").toString());
			
			/**���ݿͻ������ź��ͺ�У��**/
			String str1 = "SELECT ZT FROM BO_AKL_CGDD_BODY WHERE DDID='"+khddh+"' AND XH='"+xh+"'";
			String str2 = "SELECT ISNULL(CGSL,0)CGSL,ISNULL(YRKSL,0)YRKSL,(ISNULL(CGSL,0)-ISNULL(YRKSL,0)-ISNULL(ZTSL,0)) SYSL FROM BO_AKL_CGDD_BODY WHERE DDID='"+khddh+"' AND XH='"+xh+"'";
			String zt = StrUtil.returnStr(DBSql.getString(str1, "ZT"));
			if(CgrkCnt.ddzt5.equals(zt) || CgrkCnt.ddzt9.equals(zt)){//δ�������������
				/**У��ɹ������ͳ��������Ƿ����**/
				int cgsl = DBSql.getInt(str2, "CGSL");
				int yrksl = DBSql.getInt(str2, "YRKSL");
				wrksl = cgsl - yrksl;
				if(chsl > wrksl){
					MessageQueue.getInstance().putMessage(uc.getUID(), "�������ת����Ϣ�У����ͺš�"+xh+"�������ϲ��ܴ��ڲɹ�����������˲飡");
					return false;
				}
			}else if(CgrkCnt.ddzt4.equals(zt)){//�����
				MessageQueue.getInstance().putMessage(uc.getUID(), "�����ϡ�"+khddh+","+xh+"����ȫ����⣬��˶�ת�ֳ�������");
				return false;
			}else if("".equals(zt)){
				MessageQueue.getInstance().putMessage(uc.getUID(), "�����ϡ�"+khddh+","+xh+"��û���ڲɹ��µ�����˲飡");
				return false;
			}else{//���ͺ���;״̬(����ת�֡���ת������������������)
				int sysl = DBSql.getInt(str2, "SYSL");//ʣ���ת������
				if(chsl > sysl){
					MessageQueue.getInstance().putMessage(uc.getUID(), "�����ϡ�"+khddh+"��"+xh+"�����������ѳ�����ת�ֵ���������˲飡");
					return false;
				}
				if(chsl == 0){
					MessageQueue.getInstance().putMessage(uc.getUID(), "�����ϡ�"+khddh+"��"+xh+"����������Ϊ0����˲飡");
					return false;
				}
			}
		}
		return true;
	}
}
