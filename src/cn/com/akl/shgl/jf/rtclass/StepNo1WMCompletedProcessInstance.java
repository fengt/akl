package cn.com.akl.shgl.jf.rtclass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import cn.com.akl.shgl.lcqd.HelloAWSProcessEventBiz;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowEventClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;

public class StepNo1WMCompletedProcessInstance extends WorkFlowEventClassA{
	

	public StepNo1WMCompletedProcessInstance() {
		super();
	}

	public StepNo1WMCompletedProcessInstance(UserContext arg0) {
		super(arg0);
		setProvider("鲁祥宇");
		setVersion("1.0.0");
		setDescription("获得差异记录并保存");
	}

	@Override
	public boolean execute() {
		//流程实例id
		int bindid = getProcessCWAD().getCurrentProcessInstanceID();
		//在主表中获得授权编码
		Hashtable<String,String> recordData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXJF_P", bindid); 
	    String zdr=recordData.get("ZDR");//制单人
	    //职务
	    String zw=getProcessCWAD().getCurrentUserContext().getRoleModel().getRoleName();
		String rx=recordData.get("SQBM");//授权编码
		String jfdh=recordData.get("JFDH");//交付单号
		String sxdh=recordData.get("SXDH");//送修单号
		String khmc=recordData.get("KHMC");//客户名称
		String sjh=recordData.get("SJH");//手机号
		int sjjfsl=0;
		String sjjfxh="";
		Connection conn=DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, "select COUNT(*) sl from (select DISTINCT XH from BO_AKL_WXJF_S where bindid='"+bindid+"') AS a");
			while(rset.next()){
				int sl=Integer.parseInt(rset.getString("sl"));
				System.out.println(sl+"znme hui是");
				if(sl>1){
					//记录差异走线下邮件处理
					String bz="因交付型号数量大于1,须线下处理";
					createCYJL(zdr,zw,sxdh,jfdh,rx,khmc,sjh,bz,sjjfxh,sjjfsl);
				}else if(sl==1){
					//交付子表信息
					Vector<Hashtable<String,String>> vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXJF_S", bindid);
					//遍历
					for(Hashtable<String,String> record:vector){
						sjjfxh=record.get("XH");
						String sx=record.get("SX");//属性
						if(sx.equals("066208")){
							sjjfsl++;
						}
					}
					System.out.println("咋回事");
					//把实际的交付型号,数量 和 3#报文的型号数量进行比较
					String jfxh=DBSql.getString("select XH from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"' and BWLX='3#'", "XH");//应交付型号
					String jfsl=DBSql.getString("select SL from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"' and BWLX='3#'", "SL");//应交付数量
					if(!sjjfxh.equals(jfxh)){
						System.out.println("型号不一致");
						String bz="差异原因为实际交付型号"+sjjfxh+"与3#号报文交付型号"+jfxh+"不一致";
						if(sjjfsl!=Integer.parseInt(jfsl)){
							System.out.println("数量也不一致");
							bz=bz+"实际交付数量"+sjjfsl+"与3#报文交付数量"+jfsl+"不一致";
						}
						createCYJL(zdr,zw,sxdh,jfdh,rx,khmc,sjh,bz,sjjfxh,sjjfsl);
					}else if(sjjfsl!=Integer.parseInt(jfsl)){
						String bz ="实际交付数量"+sjjfsl+"与3#报文交付数量"+jfsl+"不一致";
						createCYJL(zdr,zw,sxdh,jfdh,rx,khmc,sjh,bz,sjjfxh,sjjfsl);
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DBSql.close(conn, stmt, rset);
		}
		return false;
				
	}	


    //记录差异信息
	private void createCYJL(String zdr, String zw, String sxdh, String jfdh,
			String rx, String khmc, String sjh, String bz,String sjjfxh,int sjjfsl) {
		Hashtable<String,String> record = new Hashtable<String,String>();
		record.put("ZDR", zdr);
		record.put("ZW", zw);
		record.put("SXDH", sxdh);
		record.put("JFDH", jfdh);
		record.put("SQBM", rx);
		record.put("KHMC", khmc);
		record.put("SJH", sjh);
		record.put("BZ", bz);
		record.put("SJJFXH", sjjfxh);
		record.put("SJJFSL", String.valueOf(sjjfsl));
		try {
			BOInstanceAPI.getInstance().createBOData("BO_AKL_SAP_JFCYJL", record, "admin");
		} catch (AWSSDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

   
		
	

}
