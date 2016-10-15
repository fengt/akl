package cn.com.akl.hhgl.hhrk.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class DealRkDatasBiz {

	private Connection conn = null;
	public void dealDatas(UserContext uc, Hashtable pTable,Vector sVector,String id){
		
		try {
			conn = DBSql.open();
			conn.setAutoCommit(false);// 手工提交事务

			String uid = uc.getUID();
		int processInstanceId = 0;
		Hashtable dataSet = new Hashtable();//封装主子表数据
		/**写入库存汇总表数据**/
		Vector rePvector = new Vector();
		rePvector = DealHzDatasBiz.getPvector(conn,pTable,sVector);
		//rePvector=getsPvector(conn,pTable,sVector);
		/**写入库存明细子表数据**/
		Vector reSvector = new Vector();
		reSvector = DealHzDatasBiz.getSvector(conn,sVector,rePvector);
		
		
			processInstanceId = WorkflowInstanceAPI.getInstance().createProcessInstance(HHDJConstant.uuid, uid, "入库维护");
			int[] processTaskInstanceIds = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, processInstanceId,  1, uid, "入库维护" ); 
			int[] boIds = BOInstanceAPI.getInstance().createBOData(HHDJConstant.tableName3, rePvector, processInstanceId, uid);
			int[] boIds2 = BOInstanceAPI.getInstance().createBOData(HHDJConstant.tableName4, reSvector, processInstanceId, uid); 
			WorkflowInstanceAPI.getInstance().closeProcessInstance(uid, processInstanceId, processTaskInstanceIds[0]);
			/**第一步：将转仓信息回填至入库单身**/
			if(id.substring(0,1).equals("J"))
			{
				
				dealStatusForKCHZ(id,0);
			}
			if(id.substring(0,1).equals("H"))
			{
				
				dealStatusForKCHZ(id,1);
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}finally{
			DBSql.close(conn, null, null);
		}
	}
	
	public static Vector<String> getsPvector(Connection conn,Hashtable pTable,Vector sVector){
		Vector resPvector = new Vector();
		Hashtable rePtable = null;
		Hashtable reStable = new Hashtable();
		
		for (int i = 0; i < sVector.size(); i++) {
			rePtable = new Hashtable();
			reStable = (Hashtable)sVector.get(i);
			Hashtable resStable = new Hashtable();
			if(resPvector.size()==0)
			{
				
				rePtable.put("PCH", reStable.get("PCH"));
				rePtable.put("WLBH", reStable.get("WLBH"));
				rePtable.put("RKDH", pTable.get("RKDH"));
				rePtable.put("XH", reStable.get("XH"));//型号
				rePtable.put("RKSL", Integer.parseInt(reStable.get("SSSL").toString()));//入库数量
				rePtable.put("PCH", reStable.get("PCH"));//批次号
				rePtable.put("WLBH", reStable.get("WLBH"));//物料编号
				rePtable.put("WLMC", reStable.get("CPMC"));//物料名称
				rePtable.put("DJ", reStable.get("WSJG"));//单价
				rePtable.put("CKSL", 0);//出库数量
				rePtable.put("PCSJ",  Integer.parseInt(reStable.get("SSSL").toString()));//批次数量	
			}
			for(int j=0;j<resPvector.size();j++)
			{
				resStable = (Hashtable)resPvector.get(j);
				String  a=new String ();
				a=reStable.get("WLBH").toString();
				String  b=new String ();
				b=resStable.get("WLBH").toString();
				String  c=new String();
				c=reStable.get("WSJG").toString();
				String  d=new String();
				d=resStable.get("DJ").toString();
			if(a==b && c==d)
			{
			rePtable.put("PCH", reStable.get("PCH"));
			rePtable.put("WLBH", reStable.get("WLBH"));
			rePtable.put("RKDH", pTable.get("RKDH"));
			rePtable.put("XH", reStable.get("XH"));//型号
			rePtable.put("RKSL", Integer.parseInt(reStable.get("SSSL").toString())+ Integer.parseInt(resStable.get("RKSL").toString()));//入库数量
			rePtable.put("PCH", reStable.get("PCH"));//批次号
			rePtable.put("WLBH", reStable.get("WLBH"));//物料编号
			rePtable.put("WLMC", reStable.get("CPMC"));//物料名称
			rePtable.put("DJ", reStable.get("WSJG"));//单价
			rePtable.put("CKSL", 0);//出库数量
			rePtable.put("PCSL",  Integer.parseInt(reStable.get("SSSL").toString())+ Integer.parseInt(resStable.get("PCSL").toString()));//批次数量	
			}
			else
				
			{
				rePtable.put("PCH", reStable.get("PCH"));
				rePtable.put("WLBH", reStable.get("WLBH"));
				rePtable.put("RKDH", pTable.get("RKDH"));
				rePtable.put("XH", reStable.get("XH"));//型号
				rePtable.put("RKSL", Integer.parseInt(reStable.get("SSSL").toString()));//入库数量
				rePtable.put("PCH", reStable.get("PCH"));//批次号
				rePtable.put("WLBH", reStable.get("WLBH"));//物料编号
				rePtable.put("WLMC", reStable.get("CPMC"));//物料名称
				rePtable.put("DJ", reStable.get("WSJG"));//单价
				rePtable.put("CKSL", 0);//出库数量
				rePtable.put("PCSJ",  Integer.parseInt(reStable.get("SSSL").toString()));//批次数量	
			
			}
			}
			resPvector.add(rePtable);
		}
		
		
		return resPvector;
		
	}
	
	
	/**
	 * 更新库存汇总表中的状态为"正常"
	 * @param xh
	 * @param pch
	 */
	private void dealStatusForKCHZ(String id,int t){
		String sql="";
		if(t==0)
		{
		 sql = "update c set jhzt = 5 ,hHZT=CASE WHEN c.jhlb=1 then ( SELECT    CASE WHEN SUM(JHSL) - SUM(CASE WHEN DHSL IS NULL THEN 0 ELSE DHSL END) <> 0 THEN '0' ELSE '1' END AS jhzt FROM      dbo.BO_JHDD_BODY_TOTAL b WHERE     b.JHDH = c.JHDH) else '' end from BO_AKL_JHDD_HEAD c where c.JHDH = '" + id +"' ";
		}
		if(t==1)
		{
		 sql = "update BO_AKL_HHDD_HEAD set zt = 3 where HHDH = '" + id +"' ";
		}
		
		int cnt = DBSql.executeUpdate(sql);
	}
	
	
	
	
	/**
	 * 暂无该逻辑：根据当前入库单身信息，更新借还货出库的信息
	 * @return
	 */
	public static int updateKCHZDatas(String id){
//		
		String sql2 ="";
		if(id.substring(0,1).equals("J"))
		{
			 sql2 = "UPDATE [BO_AKL_JHDD_BODY] SET YHHSL=YHHSL FROM [AKL_BPM].[dbo].[BO_AKL_JHDD_BODY] WHERE HH ='"+id+"'";
		}
		else
			
		{ 
			sql2 = "UPDATE [BO_AKL_JHDD_BODY] SET YHHSL=YHHSL FROM [AKL_BPM].[dbo].[BO_AKL_HHDD_BODY_TOTAL] WHERE HH IN (SELECT JHDHH from dbo.BO_AKL_HHDD_BODY_TOTAL WHERE hh='"+id+"')";
		}
		
		
		//		int cnt = DBSql.executeUpdate(sql);
		int cnt = DBSql.executeUpdate(sql2);
		return cnt;
	}
	
	
}	
