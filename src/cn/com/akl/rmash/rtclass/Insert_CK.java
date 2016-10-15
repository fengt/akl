package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import sun.security.jca.GetInstance;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.SubWorkflowEventClassA;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class Insert_CK extends SubWorkflowEventClassA{
	public Insert_CK(){}
	public Insert_CK(UserContext uc){
		super(uc);
		setVersion("RMA返新退货流程v1.0");
		setProvider("刘松");
		setDescription("用于返新退货的返新商品写入出库单");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		String sql = null;
		//获取RMA返新退货申请表的数据
		int bindid = this.getParameter(PARAMETER_PARENT_PROCESS_INSTANCE_ID).toInt();
		Hashtable ha = this.getParameter(PARAMETER_SUB_PROCESS_INSTANCE_ID).toHashtable();
		int subbindid = Integer.parseInt(ha.get(0).toString());
		Hashtable hft = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAFX_P", bindid);
		Vector vft = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFX_S", bindid);
		//打开数据库连接
		Connection conn = DBSql.open();
		Statement stat = null;
		ResultSet rs = null;
		//出库单头所需字段
		String RMAFXDH = hft.get("FHDH").toString();
		String KH = hft.get("KHBH").toString();
		String KHMC = hft.get("KHMC").toString();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
		String CJSJ = sf.format(new Date());
		String CJXM = getUserContext().getUserModel().getUserName();
		String CJID = getUserContext().getUID();
		String BHR = "";
		String FHR = getUserContext().getUserModel().getUserName();
		String WFFZRID = getUserContext().getUID();
		String WFFZR = getUserContext().getUserModel().getUserName();
		String WFDH = getUserContext().getUserModel().getOfficeTel();
		String WFSJ = getUserContext().getUserModel().getMobile();
		String WFEMAIL = getUserContext().getUserModel().getEmail();
		String SHCK = hft.get("SHCK").toString();
		String CK = hft.get("CHCK").toString();
		String CKDM = hft.get("CHCKDM").toString();
		String JHDZ = hft.get("KHSHDMC").toString();
		String KFLXR = "";
		String CKLXRDH = "";
		String CKLXREMAIL = "";
		String XDRQ = hft.get("XDRQ").toString();
		String BZ = hft.get("BZ").toString();
		System.out.println(BZ);
		double YSHJ = 0.0;

		//公用字段
		double TJ = 0;
		double ZL = 0;
		int JS = 0;


		//出库单身所需字段
		String SX = "";
		String WLH = "";
		String WLMC = "";
		String XH = "";
		String FHKFBH = "";
		String FHKFMC = "";
		String KWBH = "";
		String DW = "";
		String PP = hft.get("PP").toString();
		StringBuffer PC = new StringBuffer();
		int KCSL = 0;
		String GG = "";
		String KHCPBM = "";

		String TWLH = "";
		String TXH = "";
		int ID = 0;
		Double XSDJ = 0.0;
		int TKWSL = 0;
		int TSYSL = 0;
		int FXSL = 0;
		int boid = 0;
		String PCH = "";
		int CKSL = 0;
		int PCSL = 0;
		int KWSL = 0;
		int JS1 = 0;
		int JS2 = 0;

		Hashtable h = null;
		Hashtable hckt = new Hashtable();
		Hashtable hck = null;
		Hashtable gck = null;
		Hashtable kchz = null;

		List<Hashtable> KFPCW = null;
		Hashtable KFPCL = null;

		Hashtable HZ = null;

		List<Hashtable> MXW = null;
		Hashtable MXL = null;
		String LX = "";
		int i = 1;
		int a = 0;
		if(vft!=null){
			try {
				conn.setAutoCommit(false);
				stat = conn.createStatement();
				//根据客户名称获取客户编号
				sql = "select KHID, EMAIL from BO_AKL_KH_P where KHID = '"+KH+"'";
				rs = DBSql.executeQuery(conn, stat, sql);
				if(rs!=null){
					while(rs.next()){
						//						KH = rs.getString("KHID");
						CKLXREMAIL = rs.getString("EMAIL");
					}
				}
				//根据库房名称从库房信息中查询库房管理员信息
				sql = "select GLZXM from BO_AKL_CK where CKDM = '"+CKDM+"'";
				BHR = DBSql.getString(conn, sql, "GLZXM");
				//根据收货库房获取客户客户库房联系人和练习电话
				sql = "select LXR1, LXDH1 from BO_AKL_KHCK where KHCKID = '"+hft.get("KHCKID")+"'";
				rs = DBSql.executeQuery(conn, stat, sql);
				if(rs!=null){
					while(rs.next()){
						KFLXR = rs.getString("LXR1");
						CKLXRDH = rs.getString("LXDH1");
					}
				}
				List<Hashtable<String, String>> list = new ArrayList<Hashtable<String,String>>();
				//获取单身数据
				sql = "select WLBH, PCH, SDSL, CKDM from BO_AKL_KC_SPPCSK where DDH = '"+RMAFXDH+"'";
				rs = stat.executeQuery(sql);
				while(rs.next()){
					Hashtable<String, String> hashtable = new Hashtable<String, String>();
					hashtable.put("FXSL", String.valueOf(rs.getInt(3)));
					hashtable.put("WLBH", rs.getString(1)==null?"":rs.getString(1));
					hashtable.put("PCH", rs.getString(2)==null?"":rs.getString(2));
					hashtable.put("CKDM", rs.getString(3)==null?"":rs.getString(3));
					list.add(hashtable);
				}
				for(Hashtable<String, String> hash : list){
					gck = new Hashtable();
					kchz = new Hashtable();
					KFPCW = new ArrayList<Hashtable>();
					JS1 = Integer.parseInt(hash.get("FXSL"));
					WLH = hash.get("WLBH");
					PCH = hash.get("PCH");

					sql = "SELECT ISNULL(s.XSGHJ, 0) XSGHJ FROM BO_AKL_KH_JGGL_P p, BO_AKL_KH_JGGL_S s WHERE s.bindid = p.bindid AND s.ID = ( SELECT MAX (x.ID) FROM BO_AKL_KH_JGGL_S x, BO_AKL_KH_JGGL_P y WHERE x.WLBH = s.WLBH AND x.BINDID = y.BINDID AND y.KHBH = p.KHBH) AND p.KHBH = '"+KH+"' and s.WLBH='"+WLH+"'";
					XSDJ = DBSql.getDouble(conn, sql, "XSGHJ");


					//获取该商品库存总数
					sql = "select sum(KWSL) KWSL from BO_AKL_KC_KCMX_S where WLBH = '"+WLH+"' AND CKDM = '"+CKDM+"'";
					KCSL = DBSql.getInt(conn, sql, "KWSL");
					if(KCSL<JS1){
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "物料号："+WLH+",返新商品本库房库存数量不足！",true);
						return false;
					}
					//根据物料号和型号从物料信息表中获取物料名称，单位，体积，重量，规格等信息
					sql = "select WLMC, XH, ISNULL(TJ,0) TJ, ISNULL(ZL,0) ZL, DW, GG from BO_AKL_WLXX where WLBH = '"+WLH+"'";
					rs = DBSql.executeQuery(conn, stat, sql);
					if(rs!=null){
						while(rs.next()){
							WLMC = rs.getString("WLMC");
							TJ += rs.getDouble("TJ");
							ZL += rs.getDouble("ZL");
							DW = rs.getString("DW");
							GG = rs.getString("GG");
							XH = rs.getString("XH");
						}
					}
					//根据物料编号和型号获取客户产品编号
					sql = "select KHSPSKU from BO_AKL_KHSPBMGL where YKSPSKU = '"+WLH+"' and KHMC = '"+hft.get("KHMC")+"'";
					KHCPBM = DBSql.getString(conn, sql, "KHSPSKU")==null?"":DBSql.getString(conn, sql, "KHSPSKU");

					JS2 = JS1;
					//查询此库房中此批次号排序的数据集合
					sql = "select s.PCH, (ISNULL(sum(s.KWSL), 0)-ISNULL((select sum(SDSL) from BO_AKL_KC_SPPCSK where WLBH = s.WLBH AND PCH = s.PCH AND CKDM = s.CKDM AND DDH<>'"+RMAFXDH+"'), 0)) kysl  from BO_AKL_KC_KCMX_S s where s.WLBH = '"+WLH+"' and s.PCH = '"+PCH+"' and s.CKDM='"+CKDM+"' AND s.SX in ('049088', '049090') AND s.KWSL>0 group by s.WLBH,s.PCH,s.CKDM";
					rs = DBSql.executeQuery(conn, stat, sql);
					while(rs.next()){
						KFPCL = new Hashtable();
						KFPCL.put("PCH", PrintUtil.parseNull(rs.getString(1)));
						KFPCL.put("PCSL", rs.getInt(2));
						KFPCW.add(KFPCL);
					}
					for(Hashtable KF : KFPCW){
						if(Integer.parseInt(KF.get("PCSL").toString())>0){
							if(Integer.parseInt(KF.get("PCSL").toString())>=JS2){
								PCH = KF.get("PCH").toString();
								//							//根据物料号和型号和批次号从库存汇总表中取批次号最早的库位数量,批次号等信息
								//							sql = "select ISNULL(ID,0), PCH, ISNULL(CKSL,0) CKSL, ISNULL(PCSL,0) PCSL from BO_AKL_KC_KCHZ_P where WLBH = '"+WLH+"' and PCH = '"+PCH+"'";					
								//							rs = DBSql.executeQuery(conn, stat, sql);
								//							if(rs!=null){
								//								while(rs.next()){
								//									HZ = new Hashtable();
								//									HZ.put("ID", rs.getInt(1));
								//									HZ.put("PCH", PrintUtil.parseNull(rs.getString(2)));
								//									HZ.put("CKSL", rs.getInt(3));
								//									HZ.put("PCSL", rs.getInt(4));
								//								}
								//							}
								MXW = new ArrayList<Hashtable>();
								//							ID = Integer.parseInt(HZ.get("ID").toString());

								//更新库存汇总表出库数量和批次数量
								//							PCSL = Integer.parseInt(HZ.get("PCSL").toString())-JS2;
								//							CKSL = Integer.parseInt(HZ.get("CKSL").toString())+JS2;
								//							gck.put("PCSL", PCSL);
								//							gck.put("CKSL", CKSL);
								//							BOInstanceAPI.getInstance().updateBOData(conn, "BO_AKL_KC_KCHZ_P", gck, ID);
								//更新库存明细表库位数量
								sql = "select ISNULL(ID, 0) ID, ISNULL(KWSL, 0) KWSL, HWDM, CKDM, CKMC, SX from BO_AKL_KC_KCMX_S where PCH = '"+PCH+"' and WLBH = '"+WLH+"' and CKDM = '"+CKDM+"' AND SX in ('049088', '049090') AND KWSL > 0 order by KWSL";
								rs = DBSql.executeQuery(conn, stat, sql);
								while(rs.next()){
									MXL = new Hashtable();
									MXL.put("ID", rs.getInt(1));
									MXL.put("KWSL", rs.getInt(2));
									MXL.put("HWDM", PrintUtil.parseNull(rs.getString(3)));
									MXL.put("CKDM", PrintUtil.parseNull(rs.getString(4)));
									MXL.put("CKMC", PrintUtil.parseNull(rs.getString(5)));
									MXL.put("SX", PrintUtil.parseNull(rs.getString(6)));
									MXW.add(MXL);
								}
								a = 0;
								for(Hashtable WMX : MXW){
									KWBH = WMX.get("HWDM").toString();
									FHKFBH = WMX.get("CKDM").toString();
									FHKFMC = WMX.get("CKMC").toString();
									ID = Integer.parseInt(WMX.get("ID").toString());
									KWSL = Integer.parseInt(WMX.get("KWSL").toString());
									SX = WMX.get("SX").toString();
									if(JS2<=KWSL){
										//									kchz.put("KWSL", KWSL-JS2);
										//									BOInstanceAPI.getInstance().updateBOData(conn, "BO_AKL_KC_KCMX_S", kchz, ID);
										a = 1;
										//将返新信息写入出库单单身信息表
										hck = new Hashtable();
										hck.put("WLH",PrintUtil.parseNull(WLH));
										hck.put("WLMC", PrintUtil.parseNull(WLMC));
										hck.put("TJ",TJ);
										hck.put("ZL", ZL);
										hck.put("SL", JS2);
										hck.put("SJSL", JS2);
										hck.put("SJTJ", TJ);
										hck.put("SJZL", ZL);
										hck.put("SX", SX);
										hck.put("JLDW", PrintUtil.parseNull(DW));
										hck.put("PP", PrintUtil.parseNull(PP));
										hck.put("KWBH", PrintUtil.parseNull(KWBH));
										hck.put("XH", PrintUtil.parseNull(XH));
										hck.put("FHKFBH", PrintUtil.parseNull(FHKFBH));
										hck.put("FHKFMC", PrintUtil.parseNull(FHKFMC));
										hck.put("KCSL", KWSL);
										hck.put("PC", PrintUtil.parseNull(PCH));
										hck.put("GG", PrintUtil.parseNull(GG));
										hck.put("KHCPBM", PrintUtil.parseNull(KHCPBM));
										hck.put("DJ", XSDJ);
										boid += BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CKD_BODY", hck, subbindid, getUserContext().getUID());
										JS2 = JS2-KWSL;
										break;
									}
									else{
										//									kchz.put("KWSL", 0);
										//									BOInstanceAPI.getInstance().updateBOData(conn, "BO_AKL_KC_KCMX_S", kchz, ID);
										//将返新信息写入出库单单身信息表
										hck = new Hashtable();
										hck.put("WLH",PrintUtil.parseNull(WLH));
										hck.put("WLMC", PrintUtil.parseNull(WLMC));
										hck.put("TJ",TJ);
										hck.put("ZL", ZL);
										hck.put("SL", KWSL);
										hck.put("SJSL", KWSL);
										hck.put("SJTJ", TJ);
										hck.put("SJZL", ZL);
										hck.put("SX", SX);
										hck.put("JLDW", PrintUtil.parseNull(DW));
										hck.put("PP", PrintUtil.parseNull(PP));
										hck.put("KWBH", PrintUtil.parseNull(KWBH));
										hck.put("XH", PrintUtil.parseNull(XH));
										hck.put("FHKFBH", PrintUtil.parseNull(FHKFBH));
										hck.put("FHKFMC", PrintUtil.parseNull(FHKFMC));
										hck.put("KCSL", KWSL);
										hck.put("PC", PrintUtil.parseNull(PCH));
										hck.put("GG", PrintUtil.parseNull(GG));
										hck.put("KHCPBM", PrintUtil.parseNull(KHCPBM));
										hck.put("DJ", XSDJ);
										boid += BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CKD_BODY", hck, subbindid, getUserContext().getUID());

										JS2 = JS2- KWSL;
									}
								}
								if(a==1){
									break;
								}
							}
							else{
								//更新库存汇总表出库数量和批次数量
								//							CKSL = Integer.parseInt(HZ.get("CKSL").toString())+Integer.parseInt(KF.get("PCSL").toString());
								//							gck.put("PCSL", 0);
								//							gck.put("CKSL", CKSL);
								//							BOInstanceAPI.getInstance().updateBOData(conn, "BO_AKL_KC_KCHZ_P", gck, ID);
								//更新库存明细表库位数量
								//更新库存明细表库位数量
								sql = "select ISNULL(ID,0) ID, ISNULL(KWSL,0) KWSL, HWDM, CKDM, CKMC, SX from BO_AKL_KC_KCMX_S where PCH = '"+PCH+"' and WLBH = '"+WLH+"' and CKDM = '"+CKDM+"' AND SX in ('049088', '049090') AND KWSL >0 order by KWSL";
								rs = DBSql.executeQuery(conn, stat, sql);
								while(rs.next()){
									MXL = new Hashtable();
									MXL.put("ID", rs.getInt(1));
									MXL.put("KWSL", rs.getInt(2));
									MXL.put("HWDM", PrintUtil.parseNull(rs.getString(3)));
									MXL.put("CKDM", PrintUtil.parseNull(rs.getString(4)));
									MXL.put("CKMC", PrintUtil.parseNull(rs.getString(5)));
									MXL.put("SX", PrintUtil.parseNull(rs.getString(6)));
									MXW.add(MXL);
								}
								for(Hashtable WMX : MXW){
									KWBH = WMX.get("HWDM").toString();
									FHKFBH = WMX.get("CKDM").toString();
									FHKFMC = WMX.get("CKMC").toString();
									ID = Integer.parseInt(WMX.get("ID").toString());
									KWSL = Integer.parseInt(WMX.get("KWSL").toString());
									SX = WMX.get("SX").toString();
									//								kchz.put("KWSL", 0);
									//								BOInstanceAPI.getInstance().updateBOData(conn, "BO_AKL_KC_KCMX_S", kchz, ID);
									//将返新信息写入出库单单身信息表
									hck = new Hashtable();
									hck.put("WLH",PrintUtil.parseNull(WLH));
									hck.put("WLMC", PrintUtil.parseNull(WLMC));
									hck.put("TJ",TJ);
									hck.put("ZL", ZL);
									hck.put("SL", KWSL);
									hck.put("SJSL", KWSL);
									hck.put("SJTJ", TJ);
									hck.put("SJZL", ZL);
									hck.put("SX", SX);
									hck.put("JLDW", PrintUtil.parseNull(DW));
									hck.put("PP", PrintUtil.parseNull(PP));
									hck.put("KWBH", PrintUtil.parseNull(KWBH));
									hck.put("XH", PrintUtil.parseNull(XH));
									hck.put("FHKFBH", PrintUtil.parseNull(FHKFBH));
									hck.put("FHKFMC", PrintUtil.parseNull(FHKFMC));
									hck.put("KCSL", KWSL);
									hck.put("PC", PrintUtil.parseNull(PCH));
									hck.put("GG", PrintUtil.parseNull(GG));
									hck.put("KHCPBM", PrintUtil.parseNull(KHCPBM));
									hck.put("DJ", XSDJ);
									boid += BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CKD_BODY", hck, subbindid, getUserContext().getUID());
									JS2 = JS2- KWSL;
								}
							}
						}
						JS += JS1; 
						YSHJ += JS1*XSDJ;
					}
				}

				//将返新信息写入出库单头信息表中

				hckt.put("RMAFXDH", PrintUtil.parseNull(RMAFXDH));
				hckt.put("KH", PrintUtil.parseNull(KH));
				hckt.put("KHMC", PrintUtil.parseNull(KHMC));
				hckt.put("BHR", PrintUtil.parseNull(BHR));
				hckt.put("FHR", PrintUtil.parseNull(FHR));
				hckt.put("WFFZRID", PrintUtil.parseNull(WFFZRID));
				hckt.put("WFFZR", PrintUtil.parseNull(WFFZR));
				hckt.put("WFDH", PrintUtil.parseNull(WFDH));
				hckt.put("WFSJ", PrintUtil.parseNull(WFSJ));
				hckt.put("WFEMAIL", PrintUtil.parseNull(WFEMAIL));
				hckt.put("CK", PrintUtil.parseNull(SHCK));
				hckt.put("BZ", PrintUtil.parseNull(BZ));
				hckt.put("JHDZ", PrintUtil.parseNull(JHDZ));
				hckt.put("KFLXR", PrintUtil.parseNull(KFLXR));
				hckt.put("CKLXRDH", PrintUtil.parseNull(CKLXRDH));
				hckt.put("CKLXREMAIL", PrintUtil.parseNull(CKLXREMAIL));
				hckt.put("XDRQ", PrintUtil.parseNull(XDRQ));
				hckt.put("CKLX", PrintUtil.parseNull("2"));
				boid += BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CKD_HEAD", hckt, subbindid, getUserContext().getUID());

				conn.commit();
			} catch (AWSSDKException e) {
				// TODO Auto-generated catch block
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
				return false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
				return false;
			}
			finally{
				DBSql.close(conn, stat, rs);
			}
			if(boid==0){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "数据没正常写入库存");
				return false;
			}
		}
		return true;

	}


}
