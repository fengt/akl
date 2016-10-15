package cn.com.akl.shgl.qhbh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.shgl.qhsq.cnt.QHSQCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;

public class StepNo1AfterSave extends WorkFlowStepRTClassA {

	private static final String is = "025000";
	private static final String no = "025001";

	private static final String zt0 = "070247";//在库（库存状态）
	private static final String zt1 = "076278";//已申请（补货状态）
	
	private static final String bhlx0 = "0";//欠货及特殊申请
	private static final String bhlx1 = "1";//安全库存
	
	private static final String QUERY_SFZDPH = "SELECT SFZDPH FROM BO_AKL_QHBH_P WHERE BINDID=?";
	private static final String QUERY_XMLB = "SELECT XMLB FROM BO_AKL_QHBH_P WHERE BINDID=?";
	private static final String QUERY_FHCKBM = "SELECT FHCKBM FROM BO_AKL_QHBH_P WHERE BINDID=?";
	private static final String QUERY_BHLX = "SELECT BHLX FROM BO_AKL_QHBH_P WHERE BINDID=?";
	
	private static final String QUERY_QHJL = "SELECT a.*, ( SELECT ISNULL(SUM(KWSL), 0) FROM BO_AKL_SHKC_S b WHERE a.XMLB = b.XMLB AND a.WLBH = b.WLBH AND a.SX = b.SX AND b.CKDM = ? AND b.ZT = '"+zt0+"' )-(SELECT ISNULL(SUM(SDSL), 0) FROM BO_AKL_SH_KCSK c WHERE a.XMLB = c.XMLB AND a.WLBH = c.WLBH AND a.SX = c.SX AND c.CKDM = ?) BDKCSL, sx.SXWLMC, sx.SXPN "
			+ "FROM BO_AKL_QHJL a LEFT JOIN ( SELECT p.SXDH AS SXDHM, s.SXCPHH, s.WLMC AS SXWLMC, s.XH AS SXPN FROM BO_AKL_SX_P p, BO_AKL_SX_S s WHERE p.BINDID = s.BINDID ) sx ON sx.SXDHM = a.SXDH AND sx.SXCPHH = a.SXCPHH "
			+ "WHERE a.XMLB = ? AND ((a.QHFS<>'"+QHSQCnt.bhlx3+"' AND 0=?) OR (a.QHFS='"+QHSQCnt.bhlx3+"' AND 1=?)) AND a.ZT = '"+zt1+"' ORDER BY YXJ DESC, SQSJ DESC";
	
	private Connection conn = null;
	public StepNo1AfterSave() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1AfterSave(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("自动带入欠货记录。");
		
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		
		if("BO_AKL_QHBH_P".equals(tablename)){
			try{
				
				conn = DBSql.open();
				String sfzdph = DAOUtil.getString(conn, QUERY_SFZDPH, bindid);
				String xmlb = DAOUtil.getString(conn, QUERY_XMLB, bindid);
				String bhlx = DAOUtil.getString(conn, QUERY_BHLX, bindid);
				String flag = bhlx0;
				if(bhlx.equals(bhlx1)){
					flag = bhlx1;
				}
				
				if(sfzdph.equals(no)){
					return true;
				}
				//删除已有数据
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_QHBH_S", bindid);
				service(conn, bindid, xmlb, flag);
				
			}catch (RuntimeException e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), e.getMessage(), true);
				return false;
			} catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "后台出现异常，请检查控制台", true);
				return false;
			} finally{
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}
	
	public void service(Connection conn, final int bindid, String xmlb, String flag) throws SQLException{
		final String fhckbm = DAOUtil.getString(conn, QUERY_FHCKBM, bindid);
		DAOUtil.executeQueryForParser(conn, QUERY_QHJL, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String jfkfbm = StrUtil.returnStr(rs.getString("JFKFBM"));//收货库房编码
				if(fhckbm.equals(jfkfbm)) return true;//过滤自己给自己补货
				insertHandle(conn, rs, bindid);
				return true;
			}
		}, fhckbm, fhckbm, xmlb, flag, flag);
	}
	
	public void insertHandle(Connection conn, ResultSet rs, int bindid) throws SQLException{
		Hashtable<String, String> body = new Hashtable<String, String>();
		String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//送修单号
		String sqsj = StrUtil.returnStr(rs.getString("SQSJ"));//申请时间
		String sqly = StrUtil.returnStr(rs.getString("SQLY"));//申请理由
		String cljg = StrUtil.returnStr(rs.getString("CLJG"));//处理结果
		String sl = StrUtil.returnStr(rs.getString("SL"));//数量
		String sx = StrUtil.returnStr(rs.getString("SX"));//属性
		String zt = StrUtil.returnStr(rs.getString("ZT"));//状态
		String xmlb = StrUtil.returnStr(rs.getString("XMLB"));//项目类别
		String wlbh = StrUtil.returnStr(rs.getString("WLBH"));//物料编号
		String wlmc = StrUtil.returnStr(rs.getString("WLMC"));//物料名称
		String jfkfbm = StrUtil.returnStr(rs.getString("JFKFBM"));//交付库房编码
		String jfkfmc = StrUtil.returnStr(rs.getString("JFKFMC"));//交付库房名称
		String sxcphh = StrUtil.returnStr(rs.getString("SXCPHH"));//送修产品行号
		String pn = StrUtil.returnStr(rs.getString("PN"));//PN
		String yxj = StrUtil.returnStr(rs.getString("YXJ"));//优先级
		String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));//是否接受替换
		String qhfs = StrUtil.returnStr(rs.getString("QHFS"));//缺货方式
		String jfcphh = StrUtil.returnStr(rs.getString("JFCPHH"));//交付产品行号
		
		String sxwlmc = StrUtil.returnStr(rs.getString("SXWLMC"));//送修产品名称
		String sxpn = StrUtil.returnStr(rs.getString("SXPN"));//送修PN
		String bdkcsl = StrUtil.returnStr(rs.getString("BDKCSL"));//本地库存可用值
		String scwcgphyy = StrUtil.returnStr(rs.getString("SCWCGPHYY"));//上次未成功配货原因

//		String hh = RuleAPI.getInstance().executeRuleScript("@sequence:(#BO_AKL_QHBH_S)");//行号
//		body.put("HH", hh);//行号
		body.put("SXDH", sxdh);//送修单号
		body.put("SQSJ", sqsj);//申请时间
		body.put("SQLY", sqly);//申请理由
		body.put("CLJG", cljg);//处理结果
		body.put("SL", sl);//申请产品数量
		body.put("SX", sx);//属性
		body.put("ZT", zt);//状态
		body.put("XMLB", xmlb);//项目类别
		body.put("YCPWLBH", wlbh);//申请产品物料编号
		body.put("YCPZWMC", wlmc);//申请产品中文名称
		body.put("JFKFBM", jfkfbm);//收货库房编码
		body.put("JFKFMC", jfkfmc);//收货库房名称
		body.put("SXCPHH", sxcphh);//送修产品行号
		body.put("JFCPHH", jfcphh);//交付产品行号
		body.put("YCPPN", pn);//申请产品PN
		body.put("YXJ", yxj);//优先级
		body.put("SFJSTH", sfjsth);//是否接受替换
		body.put("PHFS", qhfs);//配货方式
		
		body.put("SQCPWLBH", wlbh);//配货产品物料编号
		body.put("SQCPSL", sl);//配货产品数量
		body.put("SQCPZWMC", wlmc);//配货产品中文名称

		body.put("SXCPMC", sxwlmc);//送修产品名称
		body.put("SXCPPN", sxpn);//送修PN
		body.put("BDKCKYZ", bdkcsl);//配货产品物料编号
		body.put("SCWCGPHYY", scwcgphyy);//上次未成功配货原因
		
		
		//插入数据
		try {
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QHBH_S", body, bindid, this.getUserContext().getUID());
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}
	}
}
