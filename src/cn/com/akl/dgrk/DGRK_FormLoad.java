package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGRK_FormLoad extends WorkFlowStepRTClassA {

	public DGRK_FormLoad() {
	}

	public DGRK_FormLoad(UserContext arg0) {
		super(arg0);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("表单加载后，匹配单身物料信息，更新入数据库");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable rkdtData = this.getParameter(PARAMETER_FORM_DATA).toHashtable();//表单读取单头信息
		Hashtable rkData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);//数据库读取单头信息
		Vector v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);//数据库读取单身信息
		//从表单读取入库单头信息
		String rq = rkdtData.get("RQ") == null ?"":rkdtData.get("RQ").toString();//日期
		rq = rq.substring(rq.length()-10);
		String rkdh = rkdtData.get("RKDH") == null ?"":rkdtData.get("RKDH").toString();//入库单号
		rkdh = rkdh.substring(rkdh.length()-15);
//		String ydh = rkdtData.get("YDH") == null ?"":rkdtData.get("YDH").toString();//采购单号
//		ydh = ydh.substring(ydh.indexOf("value="), ydh.indexOf("value=")+22);
//		ydh = ydh.substring(ydh.length()-15);
//		String khbh = rkdtData.get("KHBH") == null ?"":rkdtData.get("KHBH").toString();//客户编号
//		khbh = khbh.substring(khbh.indexOf("value="),khbh.indexOf("value=")+12);
//		khbh = khbh.substring(khbh.length()-5);
//		System.out.println(khbh);
//		System.out.println(ydh);
//		System.out.println(rq);
//		System.out.println(rkdh);
		//从数据库读取单头信息
		String rkdh2 = rkData.get("RKDH") == null ?"":rkData.get("RKDH").toString();//入库单号
		if(rkdh2.equals("")){
			if(v != null){
				Connection conn = DBSql.open();
				try {
					//数据库查询该流程采购单号、客户编号
					String sql = "select ydh,khbh,khbmbm from BO_AKL_DGRK_P where bindid='"+bindid+"'";
					String ydh = DBSql.getString(sql, "ydh"); //采购单号
					String khbh = DBSql.getString(sql, "khbh"); //客户编号
					String bmbh = DBSql.getString(sql, "khbmbm"); //客户部门编号
					//选单号为空
					if(ydh == null || ydh.trim().length()==0){
						return true;
					}
					//数据库查询采购数量合计
					String sql2 = "select slhj from BO_AKL_DGCG_P where ddbh='"+ydh+"'";
					int ysslhj = DBSql.getInt(sql2, "slhj"); //采购数量合计
					rkdtData.put("SSSLHJ", ysslhj);//采购数量合计添加到实收数量合计
					//更新单头入库单号、日期
					String dtsql = "update BO_AKL_DGRK_P set RKDH='"+rkdh+"',RQ='"+rq+"' where bindid='"+bindid+"' and YDH='"+ydh+"'";
					DBSql.executeUpdate(conn,dtsql);
					//更新单身实收数量、属性、生产或采购日期、库位
					Iterator t = v.iterator();
					while(t.hasNext()){
						Hashtable formData = (Hashtable) t.next();
						String wlbh = formData.get("WLBH") == null ?"":formData.get("WLBH").toString().trim();//物料编号
						String yssl = formData.get("YSSL") == null ?"":formData.get("YSSL").toString().trim();//应收数量
						
						//获取库位关系表
						String ckdm = "";
						String ckmc = "";
						String qdm = "";
						String ddm = "";
						String kwdm = "";
						String hwdm = "";
						Hashtable<String, String> hwdmTable= getHWDM(conn, wlbh, bmbh);
						if(hwdmTable != null){
							ckdm = hwdmTable.get("ckdm").toString();//仓库代码
							ckmc = hwdmTable.get("ckmc").toString();//仓库名称
							qdm = hwdmTable.get("qdm").toString();//区代码
							ddm = hwdmTable.get("ddm").toString();//道代码
							kwdm = hwdmTable.get("kwdm").toString();//库位代码
							hwdm = hwdmTable.get("kwbh").toString();//货位代码
						}
						String dssql = "update BO_AKL_DGRK_S set SSSL='"+yssl+"',SX='049088',SCHCGRQ='"+rq+"',CKDM='"+ckdm+"',CKMC='"+ckmc+"',QDM='"+qdm+"',DDM='"+ddm+"',KWDM='"+kwdm+"',HWDM='"+hwdm+"' WHERE BINDID='"+bindid+"' AND CGDH='"+ydh+"' AND WLBH='"+wlbh+"'";
						DBSql.executeUpdate(conn, dssql);
						/*//查询物料对应货位代码
						String hwsql = "SELECT c.CKDM,d.CKMC,c.QDM,c.DDM,c.KWDM,c.KWBH FROM BO_AKL_WLKWGXB c,(SELECT MAX(ID) as id, WLBH FROM BO_AKL_WLKWGXB GROUP BY WLBH) b,BO_AKL_CK d WHERE c.id=b.id AND c.WLBH=b.WLBH and c.CKDM=d.CKDM and c.SSKHBM='"+khbh+"' and c.WLBH='"+wlbh+"'";
						String ckdm = parseNull(DBSql.getString(hwsql, "CKDM"));//仓库代码
						String ckmc = parseNull(DBSql.getString(hwsql, "CKMC"));//仓库名称
						String qdm = parseNull(DBSql.getString(hwsql, "QDM"));//区代码
						String ddm = parseNull(DBSql.getString(hwsql, "DDM"));//道代码
						String kwdm = parseNull(DBSql.getString(hwsql, "KWDM"));//库位代码
						String hwdm = parseNull(DBSql.getString(hwsql, "KWBH"));//货位代码
*/						//更新单身实收数量、属性、生产或采购日期、库位
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					DBSql.close(conn, null, null);
				}
			}
		}
		return true;
	}

	/**
	 * 获取库位信息（有部门的优先）
	 * @param conn
	 * @param wlbh
	 * @param bmbh
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> getHWDM(Connection conn, String wlbh, String bmbh) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> rec = null;
		String sql = "SELECT c.CKDM, d.CKMC, c.QDM, c.DDM, c.KWDM, c.KWBH FROM BO_AKL_WLKWGXB c, ( SELECT MAX (ID) AS ID FROM BO_AKL_WLKWGXB WHERE SFYX = '是' AND WLBH='"+wlbh+"' AND BMBH ='"+bmbh+"' GROUP BY WLBH ) b, BO_AKL_CK d WHERE c.ID = b.ID AND c.CKDM = d.CKDM";
		String sql2 = "SELECT c.CKDM, d.CKMC, c.QDM, c.DDM, c.KWDM, c.KWBH FROM BO_AKL_WLKWGXB c, ( SELECT MAX (ID) AS ID FROM BO_AKL_WLKWGXB WHERE SFYX = '是' AND WLBH='"+wlbh+"' GROUP BY WLBH ) b, BO_AKL_CK d WHERE c.ID = b.ID AND c.CKDM = d.CKDM";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs.next()){
				rec = new Hashtable<String, String>();
				String ckdm = StrUtil.returnStr(rs.getString("CKDM"));
				String ckmc = StrUtil.returnStr(rs.getString("CKMC"));
				String qdm = StrUtil.returnStr(rs.getString("QDM"));
				String ddm = StrUtil.returnStr(rs.getString("DDM"));
				String kwdm = StrUtil.returnStr(rs.getString("KWDM"));
				String kwbh = StrUtil.returnStr(rs.getString("KWBH"));
				rec.put("ckdm", ckdm);
				rec.put("ckmc", ckmc);
				rec.put("qdm", qdm);
				rec.put("ddm", ddm);
				rec.put("kwdm", kwdm);
				rec.put("kwbh", kwbh);
			}else{
				ps = conn.prepareStatement(sql2);
				rs = ps.executeQuery();
				while(rs.next()){
					rec = new Hashtable<String, String>();
					String ckdm = StrUtil.returnStr(rs.getString("CKDM"));
					String ckmc = StrUtil.returnStr(rs.getString("CKMC"));
					String qdm = StrUtil.returnStr(rs.getString("QDM"));
					String ddm = StrUtil.returnStr(rs.getString("DDM"));
					String kwdm = StrUtil.returnStr(rs.getString("KWDM"));
					String kwbh = StrUtil.returnStr(rs.getString("KWBH"));
					rec.put("ckdm", ckdm);
					rec.put("ckmc", ckmc);
					rec.put("qdm", qdm);
					rec.put("ddm", ddm);
					rec.put("kwdm", kwdm);
					rec.put("kwbh", kwbh);
				}
			}
		} finally{
			DBSql.close(ps, rs);
		}
		return rec;
	}
	
	public String parseNull(String str){
		return str == null?"":str;
	}
}
