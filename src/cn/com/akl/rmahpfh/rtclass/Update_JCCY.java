package cn.com.akl.rmahpfh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class Update_JCCY extends WorkFlowStepRTClassA {

	public Update_JCCY() {
	}

	public Update_JCCY(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("校验反馈信息型号、数量是否有差异，有差异并记录");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		if(tablename.equals("BO_AKL_WXB_RMAHPFH_HEAD")){
			//反馈信息
			Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFK_BODY", bindid);
			Hashtable<String,Integer> xhsl = new Hashtable<String,Integer>();//存放反馈单身型号数量
			Set<String> xhlist = new HashSet<String>();//存放坏品单身型号
			Set<String> fkxhlist = new HashSet<String>();//存放反馈单身型号
			
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection conn = DBSql.open();
			//判断型号数量是否有差异、有差异记录反馈差异
			String sql = "select XH,sum(SL) as ZS from BO_AKL_WXB_RMAHPFH_BODY where bindid = '"+bindid+"' group by XH";
			try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						String xh = rs.getString("XH") == null?"":rs.getString("XH");
						String zs = rs.getString("ZS") == null?"":rs.getString("ZS");
						boolean flag = false;
						int sl = Integer.parseInt(zs);
						xhlist.add(xh);//坏品单身型号集合
						Iterator it=vc.iterator();
						while(it.hasNext()){
							//读取入库单身数据
							Hashtable formData = (Hashtable) it.next();
							String fkxh = formData.get("XH") == null ?"":formData.get("XH").toString();//型号
							String fksl = formData.get("SL") == null ?"":formData.get("SL").toString();//数量
							int fsl = Integer.parseInt(fksl);
							fkxhlist.add(fkxh);//反馈型号集合
							xhsl.put(fkxh, fsl);//反馈型号数量集合
							if(xh.equals(fkxh)){
								flag = true;
								//匹配反馈表物料号
								int t = fsl - sl;//差异数
								String wlsql = "select WLBH from BO_AKL_WLXX where XH='"+xh+"' and HZBM='01065'";
								String wlh = DBSql.getString(wlsql, "WLBH");
								//更新差异数
								String updatewl = "update BO_AKL_WXB_RMAFK_BODY set WLBH='"+wlh+"',CY="+t+" where bindid = '"+bindid+"' and XH='"+xh+"'";
								DBSql.executeUpdate(updatewl);
								//更新修订数量
								String updatexd = "update BO_AKL_WXB_RMAHPFH_BODY set XDSL="+fsl+" where bindid = '"+bindid+"' and XH='"+xh+"'";
								DBSql.executeUpdate(updatexd);
							}
						}
						if(!flag){
							//更新修订数量
							String updatexd = "update BO_AKL_WXB_RMAHPFH_BODY set XDSL=0 where bindid = '"+bindid+"' and XH='"+xh+"'";
							DBSql.executeUpdate(updatexd);
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "反馈表缺少该型号"+xh+"的产品，请检查!");
						}
					}
				}
				
				//判断反馈型号是否多余（反馈表中没有物料号的型号为反馈多出的型号，此处代码测试无效）
				if(!fkxhlist.isEmpty()){
					Iterator<String> t = fkxhlist.iterator();
					while(t.hasNext()){
						String s1 = t.next();
						boolean flag = false;
						Iterator<String> it=xhlist.iterator();
						while(it.hasNext()){
							String s = it.next();
							if(s1.equals(s)){
								flag = true;
							}
						}
						if(!flag){
							//匹配反馈表物料号
							String wlsql = "select WLBH from BO_AKL_WLXX where XH='"+s1+"'";
							String wlh = DBSql.getString(wlsql, "WLBH");
							//更新差异数
							String updatewl = "update BO_AKL_WXB_RMAFK_BODY set WLBH='"+wlh+"',CY="+xhsl.get(s1)+" where bindid = '"+bindid+"' and XH='"+s1+"'";
							DBSql.executeUpdate(updatewl);
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "反馈表多出该型号"+s1+"的产品，请检查!");
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				DBSql.close(conn, ps, rs);
			}
		}
		return true;
	}
}
