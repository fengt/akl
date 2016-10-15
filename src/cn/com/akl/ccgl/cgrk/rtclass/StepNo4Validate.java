package cn.com.akl.ccgl.cgrk.rtclass;

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
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
/**
 * ���Ľڵ㣬��Ч��У���¼�
 * @author ActionSoft_2013
 *
 */
public class StepNo4Validate extends WorkFlowStepRTClassA{

	private UserContext uc;
	public StepNo4Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("zhangran");
		setDescription("V1.0");
		setDescription("������������Ƿ���ת���������!");
	}
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable pTable = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);//�ɹ���ⵥͷ
		Vector sVector = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName1, bindid);//�ɹ���ⵥ��
		
		/**��������Ƿ���ת���������**/
		return judgePvector(uc,pTable, sVector);
	}
	

	/**
	 * У��ת����������������Ƿ����
	 * @param pTable
	 * @return
	 */
	public static boolean judgePvector(UserContext uc,Hashtable pTable,Vector sVector){
		
		Hashtable rePtable = null;
		Hashtable reStable = new Hashtable();
		for (int i = 0; i < sVector.size(); i++) {
			rePtable = new Hashtable();
			reStable = (Hashtable)sVector.get(i);
			    int bindid = Integer.parseInt(reStable.get("BINDID").toString());
			    String cgddh = reStable.get("CGDDH").toString();
				String pch = reStable.get("PCH").toString();
				String xh = reStable.get("XH").toString();
				String wlbh = reStable.get("WLBH").toString();
				String lydh = reStable.get("LYDH").toString();
				String sql = "select sum(isnull(yssl,0)) yssl from " + CgrkCnt.tableName1 + " " +
						"where bindid = " + bindid + " and pch = '" + pch +"' and wlbh = '" + wlbh + "' and xh = '" + xh + "' and cgddh = '"+cgddh+"'";
				int yssl = DBSql.getInt(sql, "yssl");//��ֺ���ⵥ���Ӧ������
				String sql2 = "select sum(isnull(chsl,0)) chsl from " + CgrkCnt.tableName2 + " where khddh = '"+cgddh+"' and wlbh = '" + wlbh + "' and bindid = " + bindid;
				int chsl = DBSql.getInt(sql2, "chsl");//ת����Ϣ���еĳ�������
				
				/*���ݿͻ������ж��Ƿ��Ӧ������У��*/
				String sql3 = "select top 1 khdm from " + CgrkCnt.tableName2 + " where bindid = " + bindid;
				String khdm = DBSql.getString(sql3, "khdm");
				if(khdm.equals(CgrkCnt.khdm0)){
					if(yssl!=chsl){
						MessageQueue.getInstance().putMessage(uc.getUID(), "�����ϱ�ţ���" + wlbh + "�����ͺţ���"+ xh +"����ת�����������������������˲飡");
						return false;
					}
				}
				
				/*��λ����У��*/
				String str = "select * from " + CgrkCnt.tableName1 + " where wlbh = '"+wlbh+"' and pch = '"+pch+"' and bindid = " + bindid;
				String kwbh = DBSql.getString(str, "KWBH");
				if(kwbh==null || "".equals(kwbh)){
					MessageQueue.getInstance().putMessage(uc.getUID(), "���ϱ��Ϊ��"+wlbh+"���Ļ�λ���Ϊ�գ�");
					return false;
				}else{
					List list = hwbmCheck(lydh,pch,wlbh,bindid);
					if(list.size()>0 && list.size()<15){
						MessageQueue.getInstance().putMessage(uc.getUID(), "������ݲ��ʱ�����ϱ��Ϊ��"+wlbh+"�������ϴ�����ͬ�Ļ�λ��ţ�" + list.toString());
						return false;
					}else if(list.size()>=15){
						MessageQueue.getInstance().putMessage(uc.getUID(), "������ݲ�ֺ󣬲����������ͬ�Ļ�λ��ţ�");
						return false;
					}
				}
		}
		return true;
	}
	
	/**
	 * У����ʱ��λ����Ƿ��ظ�
	 * @param pch
	 * @param wlbh
	 * @param xh
	 * @param bindid
	 * @return
	 */
	public static List hwbmCheck(String lydh,String pch, String wlbh, int bindid){
		Connection conn = DBSql.open();
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> list = new ArrayList<String>();
		String sql = "select kwbh from " + CgrkCnt.tableName1 + " where lydh = '"+lydh+"' and pch = '"+pch+"' and wlbh = '"+wlbh+"' and bindid = "+bindid+" group by kwbh having count(kwbh) >1";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					String kwbh = StrUtil.returnStr(rs.getString("kwbh"));
					if(StrUtil.isNotNull(kwbh)){
						list.add(kwbh);
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
	

}
