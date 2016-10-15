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
 * 处理导入单身数据唯一
 * @author ActionSoft_2013
 *
 */
public class CheckBiz {

	private static Connection conn = null;
	
	/**
	 * a.校验料号是否存在及不重复;b.根据客户代码判断是否校验转仓信息
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
				MessageQueue.getInstance().putMessage(uc.getUID(), "您导入的转仓信息中客户代码不唯一，请重新导入！");
				return false;
			}else{
				String str2 = "select top 1 khdm from " + CgrkCnt.tableName2 + " where bindid = " + bindid;
				String khdm = DBSql.getString(str2, "khdm");
				String str3 = "select * from " + CgrkCnt.tableName14 + " where gyskhbh = '"+khdm+"'";
				String hzbm = DBSql.getString(str3, "KHBH");
				String str0 = "select count(*) xh from " + CgrkCnt.tableName8 + " where xh = '"+xh+"' and hzbm = '"+hzbm+"'";
				int isXh = DBSql.getInt(str0, "xh");
				/*a.校验物料属性表中是否存在此型号*/
				if(isXh <= 0){
					MessageQueue.getInstance().putMessage(uc.getUID(), "您导入的转仓信息中，型号为【"+xh+"】的物料不在物料属性信息中，请核查！");
					return false;
				}else{
					List list = DealUtil(bindid,cgddh);
					if(list.size()>0 && list.size()<15){
						MessageQueue.getInstance().putMessage(uc.getUID(), "客户订单号【"+cgddh+"】存在重复【料号】信息：" + list.toString());
						return false;
					}else if(list.size()>=15){
						MessageQueue.getInstance().putMessage(uc.getUID(), "客户订单号【"+cgddh+"】存在重复【料号】信息，请去重后重新办理！");
						return false;
					}
				}
				/*b.根据客户代码判断是否进行转仓信息校验*/
				if(khdm.equals(CgrkCnt.khdm0)){
					/*转仓信息与采购订单数据校验*/
					return ZcxxDataCheck(uc,vector);
				}
			}
		}
		return true;
	}
	
	/**
	 * 处理同一采购单的料号是否唯一
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
	 * 导入转仓信息与采购订单数据校验(客户代码为【khdm0】的使用)
	 * @param vector
	 * @return
	 */
	public static boolean ZcxxDataCheck(UserContext uc,Vector vector){
		
		int wrksl = 0; //未入库数量
		for (int i = 0; i < vector.size(); i++) {
			Hashtable rec = (Hashtable)vector.get(i);
			String xh = rec.get("LH").toString();
			String khddh = rec.get("KHDDH").toString();
			int chsl = Integer.parseInt(rec.get("CHSL").toString());
			
			/**根据客户订单号和型号校验**/
			String str1 = "SELECT ZT FROM BO_AKL_CGDD_BODY WHERE DDID='"+khddh+"' AND XH='"+xh+"'";
			String str2 = "SELECT ISNULL(CGSL,0)CGSL,ISNULL(YRKSL,0)YRKSL,(ISNULL(CGSL,0)-ISNULL(YRKSL,0)-ISNULL(ZTSL,0)) SYSL FROM BO_AKL_CGDD_BODY WHERE DDID='"+khddh+"' AND XH='"+xh+"'";
			String zt = StrUtil.returnStr(DBSql.getString(str1, "ZT"));
			if(CgrkCnt.ddzt5.equals(zt) || CgrkCnt.ddzt9.equals(zt)){//未结束、部分入库
				/**校验采购数量和出货数量是否相符**/
				int cgsl = DBSql.getInt(str2, "CGSL");
				int yrksl = DBSql.getInt(str2, "YRKSL");
				wrksl = cgsl - yrksl;
				if(chsl > wrksl){
					MessageQueue.getInstance().putMessage(uc.getUID(), "您导入的转仓信息中，该型号【"+xh+"】的物料不能大于采购的数量，请核查！");
					return false;
				}
			}else if(CgrkCnt.ddzt4.equals(zt)){//已入库
				MessageQueue.getInstance().putMessage(uc.getUID(), "该物料【"+khddh+","+xh+"】已全部入库，请核对转仓出货数！");
				return false;
			}else if("".equals(zt)){
				MessageQueue.getInstance().putMessage(uc.getUID(), "该物料【"+khddh+","+xh+"】没有在采购下单，请核查！");
				return false;
			}else{//该型号在途状态(部分转仓、已转出、部分提货、已提货)
				int sysl = DBSql.getInt(str2, "SYSL");//剩余可转仓数量
				if(chsl > sysl){
					MessageQueue.getInstance().putMessage(uc.getUID(), "该物料【"+khddh+"，"+xh+"】出货数量已超出可转仓的数量，请核查！");
					return false;
				}
				if(chsl == 0){
					MessageQueue.getInstance().putMessage(uc.getUID(), "该物料【"+khddh+"，"+xh+"】出货数量为0，请核查！");
					return false;
				}
			}
		}
		return true;
	}
}
