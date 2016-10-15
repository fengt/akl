package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGRK_DRDS extends WorkFlowStepRTClassA {

	public DGRK_DRDS() {
	}

	public DGRK_DRDS(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("根据单号带入单身数据");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		if(tablename.equals("BO_AKL_DGRK_P")){
			Hashtable rkdtData = this.getParameter(PARAMETER_FORM_DATA).toHashtable();
			Vector v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGRK_S", bindid);
			//读取入库单头信息
			String ydh = rkdtData.get("YDH") == null ?"":rkdtData.get("YDH").toString();//采购单号
			String khbh = rkdtData.get("KHBH") == null ?"":rkdtData.get("KHBH").toString();//客户编号
			String bmbh = rkdtData.get("KHBMBM") == null ?"":rkdtData.get("KHBMBM").toString();//部门编号
			Vector vc =new Vector();
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection conn = DBSql.open();
			try {
				//选单号为空
				if(ydh == null || ydh.trim().length()==0){
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_DGRK_S", bindid); 
					return true;
				}
				//入库单身数据的采购单号
				String ddhsql = "SELECT distinct CGDH FROM BO_AKL_DGRK_S WHERE BINDID="+bindid+"";
				String ddh = DBSql.getString(ddhsql, "CGDH");
				if(ydh.equals(ddh)){
					return true;
				}
				//匹配物料货位代码
//				String sql = "SELECT b.ID,b.DDBH,b.WLBH,b.WLMC,b.GG,b.XH,CGSL,YRKSL,b.DW,b.CGRQ,c.CKDM,c.CKMC,c.QDM,c.DDM,c.KWDM,c.KWBH from (select b.ID,b.DDBH,b.WLBH,b.WLMC,b.GG,b.XH,CGSL,YRKSL,b.DW,a.CGRQ FROM BO_AKL_DGCG_S b,BO_AKL_DGCG_P a where a.DDBH=b.DDBH and a.DDBH='"+ydh+"' and a.CGZT='待采购' and b.CGZT='待采购') b LEFT JOIN(SELECT c.WLBH,c.CKDM,d.CKMC,c.QDM,c.DDM,c.KWDM,c.KWBH FROM BO_AKL_WLKWGXB c,(SELECT MAX(ID) as id, WLBH FROM BO_AKL_WLKWGXB GROUP BY WLBH) b,BO_AKL_CK d WHERE c.id=b.id AND c.WLBH=b.WLBH and c.CKDM=d.CKDM and c.SSKHBM='"+khbh+"') c on b.WLBH=c.WLBH";
				String sql = "SELECT b.ID, b.DDBH, b.WLBH, b.WLMC, b.GG, b.XH, CGSL, YRKSL, b.DW, a.CGRQ FROM BO_AKL_DGCG_S b, BO_AKL_DGCG_P a WHERE a.DDBH = b.DDBH AND a.DDBH = '"+ydh+"' AND a.CGZT = '待采购' AND b.CGZT = '待采购'";
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						Hashtable recordData = new Hashtable();
						//读取采购单身表
//						String hh = rs.getString("HH") == null ?"":rs.getString("HH").toString();//行号
						String cgdh = rs.getString("DDBH") == null ?"":rs.getString("DDBH").toString();//采购单号
						String wlbh = rs.getString("WLBH") == null ?"":rs.getString("WLBH").toString();//物料编号
						String wlmc = rs.getString("WLMC") == null ?"":rs.getString("WLMC").toString();//物料名称
						String gg = rs.getString("GG") == null ?"":rs.getString("GG").toString();//规格
						String xh = rs.getString("XH") == null ?"":rs.getString("XH").toString();//型号
						int cgsl = rs.getInt("CGSL");//采购数量
						int yrksl = rs.getInt("YRKSL");//已入库数量
						String dw = rs.getString("DW") == null ?"":rs.getString("DW").toString();//计量单位
						String cgrq = rs.getString("CGRQ") == null ?"":rs.getString("CGRQ").toString();//采购日期
						
						//插入入库单身表
//						recordData.put("HH", hh);//行号
						recordData.put("CGDH", cgdh);//采购单号
						recordData.put("WLBH", wlbh);//物料编号
						recordData.put("WLMC", wlmc);//物料名称
						recordData.put("GG", gg);//规格
						recordData.put("XH", xh);//型号
						recordData.put("YSSL", cgsl-yrksl);//应收数量
						recordData.put("SSSL", cgsl-yrksl);//实收数量
						recordData.put("DW", dw);//计量单位
						recordData.put("SCHCGRQ", cgrq);//采购日期
						
						//获取库位关系表
						Hashtable<String, String> hwdmTable= getHWDM(conn, wlbh, bmbh);
						if(hwdmTable != null){
							recordData.put("CKDM", hwdmTable.get("ckdm").toString());//仓库代码
							recordData.put("CKMC", hwdmTable.get("ckmc").toString());//仓库名称
							recordData.put("QDM", hwdmTable.get("qdm").toString());//区代码
							recordData.put("DDM", hwdmTable.get("ddm").toString());//道代码
							recordData.put("KWDM", hwdmTable.get("kwdm").toString());//库位代码
							recordData.put("HWDM", hwdmTable.get("kwbh").toString());//货位代码
						}
						recordData.put("SX", "049088");//货位代码
						vc.add(recordData);
					}
					//删除数据
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_DGRK_S", bindid); 
					//插入数据库
					BOInstanceAPI.getInstance().createBOData("BO_AKL_DGRK_S", vc, this.getParameter(PARAMETER_INSTANCE_ID).toInt(),  this.getUserContext().getUID());
				}else{
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "当前采购单为空，请检查!");
					return false;
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBSql.close(conn, ps, rs);
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
	
	
}
