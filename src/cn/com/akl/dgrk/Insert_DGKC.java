package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class Insert_DGKC extends WorkFlowStepRTClassA {

	public Insert_DGKC() {
	}

	public Insert_DGKC(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("入库单身信息插入库存明细表,更新库存汇总表，更新采购单采购状态、已入库数量，更新序列号状态");
	}

	@Override
	public boolean execute() {
		//插入代管库存汇总表、明细表
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		//读取入库单头信息
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);
		String rkdh = rkdtData.get("RKDH") == null ?"":rkdtData.get("RKDH").toString();//入库单号
		String ydh = rkdtData.get("YDH") == null ?"":rkdtData.get("YDH").toString();//采购单号
		String rklx = rkdtData.get("RKLX") == null ?"":rkdtData.get("RKLX").toString();//入库类型
		if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "入库")){
			Set<String> wlh = new HashSet<String>();//物料编号+单位集合
			Set<String> qswlh = new HashSet<String>();//单身中缺少的物料编号+单位集合
			//读取入库单身信息
			Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);
			Vector v2 = new Vector();//库存明细
			String pchsql = "select distinct PCH from BO_AKL_DGKC_KCHZ_P where RKDH = '"+rkdh+"'";
			
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection conn = DBSql.open();
			try {
				//获取批次号
				String pch = DBSql.getString(conn, pchsql, "PCH");
				
				Iterator t = vc.iterator();
				while(t.hasNext()){
					//读取入库单身数据
					Hashtable formData = (Hashtable) t.next();
					String wlbh = formData.get("WLBH") == null ?"":formData.get("WLBH").toString();//物料编号
					String wlmc = formData.get("WLMC") == null ?"":formData.get("WLMC").toString();//物料名称
					String gg = formData.get("GG") == null ?"":formData.get("GG").toString();//规格
					String xh = formData.get("XH") == null ?"":formData.get("XH").toString();//型号
//					String yssl = formData.get("YSSL") == null ?"":formData.get("YSSL").toString();//应收数量
					String sssl = formData.get("SSSL") == null ?"":formData.get("SSSL").toString();//实收数量
					String ckdm = formData.get("CKDM") == null ?"":formData.get("CKDM").toString();//仓库代码
					String ckmc = formData.get("CKMC") == null ?"":formData.get("CKMC").toString();//仓库名称
					String qdm = formData.get("QDM") == null ?"":formData.get("QDM").toString();//区代码
					String ddm = formData.get("DDM") == null ?"":formData.get("DDM").toString();//道代码
					String kwdm = formData.get("KWDM") == null ?"":formData.get("KWDM").toString();//库位代码
					String hwdm = formData.get("HWDM") == null ?"":formData.get("HWDM").toString();//货位代码
					String bzq = formData.get("BZQ") == null ?"":formData.get("BZQ").toString();//保质期
					String scrq = formData.get("SCHCGRQ") == null ?"":formData.get("SCHCGRQ").toString();//生产日期
					String dw = formData.get("DW") == null ?"":formData.get("DW").toString();//单位
					String sx = formData.get("SX") == null ?"":formData.get("SX").toString();//属性
//					int sl1 = Integer.parseInt(yssl);//应收数量
					int sl2 = Integer.parseInt(sssl);//实收数量
					
					//当前单身物料号+单位集合
					wlh.add(wlbh+dw);
					//添加库存明细数据
					Hashtable recordData2 = new Hashtable();
					recordData2.put("WLBH", wlbh);//物料编号
					recordData2.put("WLMC", wlmc);//物料名称
					recordData2.put("GG", gg);//规格
					recordData2.put("XH", xh);//型号
					recordData2.put("PCH", pch);//批次号
					recordData2.put("CKDM", ckdm);//仓库代码
					recordData2.put("CKMC", ckmc);//仓库名称
					recordData2.put("QDM", qdm);//区代码
					recordData2.put("DDM", ddm);//道代码
					recordData2.put("KWDM", kwdm);//库位代码
					recordData2.put("HWDM", hwdm);//货位代码
					recordData2.put("KWSL", sl2);//货位数量
					recordData2.put("BZQ", bzq);//保质期
					recordData2.put("SCHCGRQ", scrq);//生产日期
					recordData2.put("JLDW", dw);//计量单位
					recordData2.put("SX", sx);//属性
					v2.add(recordData2);
					
					//拆分后实收总数
					String cfssslsql = "select sum(SSSL) as sl from BO_AKL_DGRK_S where bindid = '"+bindid+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					int cfsssl = DBSql.getInt(conn, cfssslsql, "sl");
					//拆分后应收总数
					String cfysslsql = "select sum(YSSL) as sl from BO_AKL_DGRK_S where bindid = '"+bindid+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					int cfyssl = DBSql.getInt(conn, cfysslsql, "sl");
					if(!rklx.equals("其它入库")){
						//采购单身已入库数量
						String yrksql = "select YRKSL from BO_AKL_DGCG_S where DDBH='"+ydh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
						int yrk = DBSql.getInt(yrksql, "YRKSL");
						//实收数量判断
						if(cfyssl > cfsssl){					
							//更新采购单身已入库数量
							yrk = yrk + sl2;
							String cgslsql = "update BO_AKL_DGCG_S set YRKSL="+yrk+" where DDBH='"+ydh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,cgslsql);
							
							//更新采购单身采购状态
							String cgssql = "update BO_AKL_DGCG_S set CGZT='待采购' where DDBH='"+ydh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,cgssql);
							
							//更新库存汇总入库数量
							String rkslsql = "update BO_AKL_DGKC_KCHZ_P set RKSL="+cfsssl+" where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,rkslsql);
							
							//更新库存汇总批次数量
							String pcslsql = "update BO_AKL_DGKC_KCHZ_P set PCSL="+cfsssl+" where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,pcslsql);
							
						}else if(cfyssl == cfsssl){					
							//更新采购单身已入库数量
							yrk = yrk + sl2;
							String cgslsql = "update BO_AKL_DGCG_S set YRKSL="+yrk+" where DDBH='"+ydh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,cgslsql);
						}
					}else if(rklx.equals("其它入库")){
						//实收数量判断
						if(cfyssl > cfsssl){
							//更新库存汇总入库数量
							String rkslsql = "update BO_AKL_DGKC_KCHZ_P set RKSL="+cfsssl+" where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,rkslsql);
							//更新库存汇总批次数量
							String pcslsql = "update BO_AKL_DGKC_KCHZ_P set PCSL="+cfsssl+" where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,pcslsql);
						}
					}
					
					//更新库存汇总入库日期、状态正常
					Date date = new Date();
					SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
					String dateString = format.format(date);
					String rkrqsql = "update BO_AKL_DGKC_KCHZ_P set RKRQ='"+dateString+"',ZT='042022' where RKDH = '"+rkdh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
					DBSql.executeUpdate(conn,rkrqsql);				
					
					//查询采购单身采购状态
					if(!rklx.equals("其它入库")){
						String cgztsql = "select CGZT from BO_AKL_DGCG_S where DDBH='"+ydh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
						String cgzt = DBSql.getString(cgztsql, "CGZT");
						if("待入库".equals(cgzt)){
							//更新采购单身采购状态
							String cgssql = "update BO_AKL_DGCG_S set CGZT='已入库' where DDBH='"+ydh+"' and XH = '"+xh+"' and DW = '"+dw+"'";
							DBSql.executeUpdate(conn,cgssql);
						}
					}
				}
				
				//判断单身物料号是否缺少,缺少的料号更改采购状态
				String wlsql = "select WLBH,DW from BO_AKL_DGKC_KCHZ_P where RKDH = '"+rkdh+"'";
				ps = conn.prepareStatement(wlsql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						boolean wlflag = false;
						String wl = rs.getString("WLBH") == null?"":rs.getString("WLBH");
						String dw = rs.getString("DW") == null?"":rs.getString("DW");
						
						for(Iterator<String> it=wlh.iterator();it.hasNext();){
							String s = it.next();
							if(s.equals(wl+dw)){
								wlflag = true;
							}
						}
						if(!wlflag){
							//缺少的物料号+单位放入集合
							qswlh.add(wl+dw);
							if(!rklx.equals("其它入库")){
								//更新采购单身采购状态
								String cgssql = "update BO_AKL_DGCG_S set CGZT='待采购' where DDBH='"+ydh+"' and wlbh='"+wl+"' and dw='"+dw+"'";
								DBSql.executeUpdate(conn,cgssql);
							}
						}
					}
				}
				
				//删除库存汇总单身已删除的物料号
				for(Iterator<String> it=qswlh.iterator();it.hasNext();){
					String s = it.next();
					String cgssql = "delete from BO_AKL_DGKC_KCHZ_P where RKDH='"+rkdh+"' and WLBH+DW='"+s+"'";
					DBSql.executeUpdate(conn,cgssql);
				}
				
				//插入库存明细表
				BOInstanceAPI.getInstance().removeProcessInstanceBOData("BO_AKL_DGKC_KCMX_S", bindid);
				BOInstanceAPI.getInstance().createBOData("BO_AKL_DGKC_KCMX_S", v2, bindid, this.getUserContext().getUID());
				if(!rklx.equals("其它入库")){
					//更新采购单头采购状态(更新回待采购)
					String cgsql = "select CGZT from BO_AKL_DGCG_S where DDBH='"+ydh+"'";
					boolean cgflag = false;
					ps = conn.prepareStatement(cgsql);
					rs = ps.executeQuery();
					if(rs != null){
						while(rs.next()){
							String cgzt = rs.getString("CGZT") == null?"":rs.getString("CGZT");
							if("待采购".equals(cgzt)){
								cgflag = true;
							}
						}
						if(cgflag){
							String cgpsql = "update BO_AKL_DGCG_P set CGZT='待采购' where DDBH='"+ydh+"'";
							DBSql.executeUpdate(conn,cgpsql);
						}
					}
					//更新采购单头采购状态(更新为已入库)
					String sql = "select CGZT from BO_AKL_DGCG_S where DDBH='"+ydh+"' group by CGZT";
					boolean flag = true;
					ps = conn.prepareStatement(sql);
					rs = ps.executeQuery();
					if(rs != null){
						while(rs.next()){
							String cgzt = rs.getString("CGZT") == null?"":rs.getString("CGZT");
							if(!"已入库".equals(cgzt)){
								flag = false;
							}
						}
						if(flag){
							String cgpsql = "update BO_AKL_DGCG_P set CGZT='已入库' where DDBH='"+ydh+"'";
							DBSql.executeUpdate(conn,cgpsql);
						}
					}
					//更新序列号状态
					String cgpsql = "update BO_AKL_CCB_RKD_XLH_S set ZT='在库' where bindid='"+bindid+"'";
					DBSql.executeUpdate(conn,cgpsql);
				}
				return true;
			} catch (Exception e) {
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "写入库存失败，请通知管理员");
				e.printStackTrace(System.err);
				return false;
			} finally {
				DBSql.close(conn, ps, rs);
			}
		}else if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回")){
			Connection conn = DBSql.open();
			try {
				int currentStepNo = 3;
				int stepno = new WorkFlowUtil().getPreviousStepNo(conn, bindid, currentStepNo);
				if(stepno == 1){
					//删除库存汇总
					String hzsql = "delete from BO_AKL_DGKC_KCHZ_P where RKDH='"+rkdh+"'";
					DBSql.executeUpdate(hzsql);
					//如果入库类型不是其它入库
					if(!rklx.equals("其它入库")){
						//更新采购单身采购状态
						String cgdssql = "update BO_AKL_DGCG_S set CGZT='待采购' where DDBH='"+ydh+"'";
						DBSql.executeUpdate(cgdssql);
						//更新采购单头
						String cgdtsql = "update BO_AKL_DGCG_P set CGZT='待采购' where DDBH='"+ydh+"'";
						DBSql.executeUpdate(cgdtsql);
					}
					//删除入库单身批次号
					String pchsql = "update BO_AKL_DGRK_S set PCH='' where bindid='"+bindid+"'";
					DBSql.executeUpdate(pchsql);
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				e.printStackTrace(System.err);
				return false;
			}finally {
				DBSql.close(conn, null, null);
			}
		}
		return false;
	}
}
