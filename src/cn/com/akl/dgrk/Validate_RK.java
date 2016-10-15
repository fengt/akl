package cn.com.akl.dgrk;

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
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

/**
 * 该方法已废弃，被StepNo3Validate取代
 * @author Administrator
 *
 */
public class Validate_RK extends WorkFlowStepRTClassA {

	public Validate_RK() {
	}

	public Validate_RK(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("校验拆分是否正确，校验是否需要序列号及序列号是否已全部正确导入");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		
		if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "入库")){
			Set<String> wlh = new HashSet<String>();//当前单身物料编号集合
			boolean flag = false;//是否需要序列号
			Set<String> wlxlh = new HashSet<String>();//需要序列号的物料编号
			Set<String> wlxlh2 = new HashSet<String>();//需要并且有序列号的物料编号
			Hashtable wlsl = new Hashtable();//物料号的数量信息
			
			//读取入库单头信息
			Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);
			String rkdh = rkdtData.get("RKDH") == null ?"":rkdtData.get("RKDH").toString();//入库单号
			String khbh = rkdtData.get("KHBH") == null ?"":rkdtData.get("KHBH").toString();//客户编号
			String rklx = rkdtData.get("RKLX") == null ?"":rkdtData.get("RKLX").toString();//入库类型
			//读取单身信息
			Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);
			if(vc == null){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "入库单身信息不可为空！！！");
				return false;
			}
			
			PreparedStatement ps = null;
			ResultSet rs = null;
			
			Connection conn = DBSql.open();
			try {
				
				Iterator t = vc.iterator();
				while(t.hasNext()){
					//读取入库单身数据
					Hashtable formData = (Hashtable) t.next();
					String wlbh = formData.get("WLBH") == null ?"":formData.get("WLBH").toString();//物料编号
					//String wlmc = formData.get("WLMC") == null ?"":formData.get("WLMC").toString();//物料名称
					String xh = formData.get("XH") == null ?"":formData.get("XH").toString();//型号
					String dw = formData.get("DW") == null ?"":formData.get("DW").toString();//单位
					String hwdm = formData.get("HWDM") == null ?"":formData.get("HWDM").toString();//货位代码

					String dwsql = "select XLMC from BO_AKL_DATA_DICT_S where DLBM='026' and XLBM='"+dw+"'";
					String dwmc = DBSql.getString(dwsql, "XLMC");
					//当前单身物料号集合
					wlh.add(wlbh);
					//入库数量
					String kcslsql = "select RKSL from BO_AKL_DGKC_KCHZ_P where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					int kcsl = DBSql.getInt(conn, kcslsql, "RKSL");
					//批次数量
					String pcsql = "select PCSL from BO_AKL_DGKC_KCHZ_P where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					int pcsl = DBSql.getInt(conn, pcsql, "PCSL");
					//拆分后实收总数
					String cfssslsql = "select sum(SSSL) as sl from BO_AKL_DGRK_S where bindid = '"+bindid+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					int sssl = DBSql.getInt(conn, cfssslsql, "sl");
					//拆分后应收总数
					String cfysslsql = "select sum(YSSL) as sl from BO_AKL_DGRK_S where bindid = '"+bindid+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					int yssl = DBSql.getInt(conn, cfysslsql, "sl");
					//实收数量校验
					if(kcsl < sssl){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "型号"+xh+",单位"+dwmc+",拆分后实收总数量"+sssl+"多于实收数量"+kcsl+"，请检查!");
						return false;
					}
					//应收数量校验
					if(pcsl < yssl){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "型号"+xh+",单位"+dwmc+",拆分后应收总数量"+yssl+"多于应收数量"+pcsl+"，请检查!");
						return false;
					}else if(pcsl > yssl){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "型号"+xh+",单位"+dwmc+",拆分后应收总数量"+yssl+"少于应收数量"+pcsl+"，请检查!");
						return false;
					}
					//货位代码是否为空的校验
					if("".equals(hwdm)){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "型号"+xh+",单位"+dwmc+",未选择库位，请选择!");
						return false;
					}
					//如果入库类型不是其它入库，判断是否需要序列号
					if(!rklx.equals("其它入库")){
						String sql2 = "select SFXLH from BO_AKL_WLXX where WLBH= '"+wlbh+"' and HZBM='"+khbh+"'";
						String sfxlh = DBSql.getString(sql2, "SFXLH");
						if("1".equals(sfxlh)){
							wlxlh.add(xh);
							wlsl.put(xh, sssl);
							flag = true;
						}
					}
				}
				
				//判断单身物料号是否缺少
				if(rklx.equals("其它入库")){
					String wlsql = "select WLBH from BO_AKL_DGKC_KCHZ_P where RKDH = '"+rkdh+"'";
					ps = conn.prepareStatement(wlsql);
					rs = ps.executeQuery();
					if(rs != null){
						while(rs.next()){
							boolean wlflag = false;
							String wl = rs.getString("WLBH") == null?"":rs.getString("WLBH");
							for(Iterator<String> it=wlh.iterator();it.hasNext();){
								String s = it.next();
								if(s.equals(wl)){
									wlflag = true;
								}
							}
							if(!wlflag){
								MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "拆分后单身缺少该物料"+wl+"，请检查!");
								return false;
							}
						}
					}
				}
				
				//判断单身数据货位是否重复
				String sql = "select XH,DW,HWDM from BO_AKL_DGRK_S where bindid = '"+bindid+"' group by XH,DW,HWDM having count(*) > 1";
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						String xh = rs.getString("XH") == null?"":rs.getString("XH");
						String dw = rs.getString("DW") == null?"":rs.getString("DW");
						String hwdm = rs.getString("HWDM") == null?"":rs.getString("HWDM");
						String dwsql = "select XLMC from BO_AKL_DATA_DICT_S where DLBM='026' and XLBM='"+dw+"'";
						String dwmc = DBSql.getString(dwsql, "XLMC");
						if(!"".equals(xh) ){
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "入库单身同一物料存在重复货位数据，型号"+xh
									+",单位"+dwmc+",货位编码"+hwdm+"，请检查!");
							return false;
						}
					}
				}
				
				//序列号校验
				if(flag){
					//序列号集合
//					Vector vc1 = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CCB_RKD_XLH_S", bindid);//录入的序列号表
					Vector<Hashtable<String, String>> vc1 = XLHBody(conn, bindid);
					if(vc1 == null){
						StringBuffer xlhstr = new StringBuffer();
						//需要序列号的物料号
						for(Iterator<String> it=wlxlh.iterator();it.hasNext();){
							String s = it.next();
							xlhstr.append(s);
							xlhstr.append(",");
						}
						String str = xlhstr.substring(0, xlhstr.length()-1);
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "物料"+str+"需要序列号！");
						return false;
					}
					
					
					//读取需要序列号的物料编号集合
					for(Iterator<String> it=wlxlh.iterator();it.hasNext();){
						boolean f = true;
						String s = it.next();
						int c = (Integer) wlsl.get(s);//获取该物料实收数量
						int count = 0;
						Iterator t1 = vc1.iterator();
						while(t1.hasNext()){
							Hashtable xlhData = (Hashtable) t1.next();
							String xh = xlhData.get("XH") == null ?"":xlhData.get("XH").toString();//型号
							String xlh = xlhData.get("XLH") == null ?"":xlhData.get("XLH").toString();//序列号
							//判断序列号数据是否重复
							String xlhsql = "select XH,XLH from BO_AKL_CCB_RKD_XLH_S where XH='"+xh+"' AND XLH='"+xlh+"' AND ZT='在库' group by XH,XLH having count(*) > 1";
							ps = conn.prepareStatement(xlhsql);
							rs = ps.executeQuery();
							if(rs != null){
								while(rs.next()){
									String xlhbxh = rs.getString("XH") == null?"":rs.getString("XH");
									String xlhbxlh = rs.getString("XLH") == null?"":rs.getString("XLH");
									if(!"".equals(xlhbxlh)){
										MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "该型号："+xlhbxh+"的序列号："+xlhbxlh+"产品已在库，请检查!");
										return false;
									}
								}
							}
							if(s.equals(xh)){
								wlxlh2.add(xh);
								count =count + 1;
								f=false;
							}
						}
						//校验该物料号是否有序列号
						if(f){
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "型号"+s+"没有序列号，请检查！");
							return false;
						}
						//校验物料号的序列号数量是否正确
						if(count < c){
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "型号"+s+"的序列号数量"+count+"少于实收数量"+c+"，请检查！");
							return false;
						}else if(count > c){
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "型号"+s+"的序列号数量"+count+"多于实收数量"+c+"，请检查！");
							return false;
						}
					}
					
					//校验是否有多余物料
					if(!wlxlh2.isEmpty()){
						Iterator t1 = vc1.iterator();
						while(t1.hasNext()){
							boolean ff = true;
							Hashtable xlhData = (Hashtable) t1.next();
							String xh = xlhData.get("XH") == null ?"":xlhData.get("XH").toString();//型号
							for(Iterator<String> it=wlxlh.iterator();it.hasNext();){
								String s = it.next();
								if(s.equals(xh)){
//									wlxlh2.add(xh);
									ff=false;
								}
							}
							if(ff){
								MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "序列号表中存在多余的物料"+xh+"，请删除！");
								return false;
							}
						}
					}else{
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "需要序列号的物料尚未录入序列号，请录入！");
						return false;
					}
				}
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBSql.close(conn, ps, rs);
			}
		}else if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回")){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 序列号单身数据封装
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public Vector<Hashtable<String,String>> XLHBody(Connection conn, int bindid) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector<Hashtable<String, String>> vec = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		String query_xlh = "SELECT XH,XLH FROM BO_AKL_CCB_RKD_XLH_S WHERE BINDID="+bindid;
		try {
			ps = conn.prepareStatement(query_xlh);
			rs = ps.executeQuery();
			while(rs.next()){
				rec = new Hashtable<String, String>();
				String xh = rs.getString("XH") == null ?"":rs.getString("XH");//型号
				String xlh = rs.getString("XLH") == null ?"":rs.getString("XLH");//序列号
				rec.put("XH", xh);
				rec.put("XLH", xlh);
				vec.add(rec);
			}
		} finally{
			DBSql.close(ps, rs);
		}
		return vec;
	}
}
