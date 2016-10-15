package cn.com.akl.shgl.lcqd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowEventClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
/** 
 * 渠道信息审批流程结束后生成9#报文
 * @author luxiangyu
 *
 */
public class WMCompletedProcessInstance extends WorkFlowEventClassA {
	private static int sxbindid;
	private static int jfbindid;
	public WMCompletedProcessInstance() {
		super();
		// TODO Auto-generated constructor stub
	}

	public WMCompletedProcessInstance(UserContext arg0) {
		super(arg0);
		setProvider("鲁祥宇");
		setVersion("1.0.0");
		setDescription("渠道信息审批流程结束后,生成9#报文");
	}

	@Override
	public boolean execute() {
		System.out.println("启动了这个流程");
		//流程实例id
		int bindid=getProcessCWAD().getCurrentProcessInstanceID();
		System.out.println("这个bindid是什么"+bindid);
		String url="/\\10.10.10.70/SendCS/9";
        //根据bindiid获得送修的bindid
		selectSXbindid(bindid);
		
		creatXml9(sxbindid,url);
		
	    
		
		
		return false; 
	}
	//生成9#报文(交付单)
	private void creatXml91(int bindid, String url) {
		Calendar calendar = Calendar.getInstance();
        Integer y=calendar.get(Calendar.YEAR);//年
        Integer m=calendar.get(Calendar.MONTH)+1;//月
        Integer d=calendar.get(Calendar.DATE);//日
        Integer h=calendar.get(Calendar.HOUR_OF_DAY);//时
        Integer f=calendar.get(Calendar.MINUTE);//分
	    Integer s=calendar.get(Calendar.SECOND);//秒
	    Integer hs=calendar.get(Calendar.MILLISECOND);//毫秒
	    SimpleDateFormat dateFm = new SimpleDateFormat("yyyyMMddHHmmSS"); //格式化当前系统日期
	    String dateTime = dateFm.format(new java.util.Date());
		url=url+"/chinamabj_ORDER_"+dateTime+".xml(交付)";//9报文路径
		
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("SYNC_INVENTORY_005");
			Element cn =root.addElement("CNTROLAREA");
			
			Element bsr =cn.addElement("BSR");
			bsr.addElement("VERB").addText("SYNC");
			bsr.addElement("NOUN").addText("INVENTORY");
			bsr.addElement("REVISION").addText("005");
			
			Element sender=cn.addElement("SENDER");
			sender.addElement("LOGICALID").addText("M030");
			sender.addElement("COMPONENT").addText("M030");
			sender.addElement("TASK");
			sender.addElement("REFERENCEID").addText("5155027_2");
			sender.addElement("CONFIRMATION").addText("0");
			sender.addElement("LANGUAGE").addText("EN");
			sender.addElement("CODEPAGE");
			sender.addElement("AUTHID").addText("Prod");
			
			Element dt=cn.addElement("DATETIME").addAttribute("qualifier", "CREATION");
			dt.addElement("YEAR").addText(y.toString());//年
			dt.addElement("MONTH").addText(m.toString());//月
			dt.addElement("DAY").addText(d.toString());//天
			dt.addElement("HOUR").addText(h.toString());//小时
			dt.addElement("MINUTE").addText(f.toString());//24
		    dt.addElement("SECOND").addText(s.toString());//25
			dt.addElement("SUBSECOND").addText(hs.toString());//26
			dt.addElement("TIMEZONE").addText("+0800");//
            //根据bindid查询送修单数据
			Vector<Hashtable<String,String>> vector=BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXJF_S", bindid);
			Connection conn=DBSql.open();
			for(Hashtable<String,String> re:vector){
				String sl=re.get("SL");
				String xh=re.get("XH");
				String xlbm=re.get("SX");
				String hh = re.get("HH");
				Element da = root.addElement("DATAAREA");
	        	Element si = da.addElement("SYNC_INVENTORY");
	        	Element inven  = si.addElement("INVENTORY");
	        	inven.addElement("ACTTYPE").addText("TRANS");
	        	String jfdh=DBSql.getString("select JFDH from BO_AKL_WXJF_P where bindid='"+bindid+"'", "JFDH");
	        	System.out.println("交付单号"+jfdh);
	        	inven.addElement("TRANSID").addText(jfdh);//送修单号
	        	Element dt1 = inven.addElement("DATETIME").addAttribute("qualifier", "EFFECTIVE");
	        	dt1.addElement("YEAR").addText(y.toString());//年
	        	dt1.addElement("MONTH").addText(m.toString());//月
	        	dt1.addElement("DAY").addText(d.toString());//日
	        	dt1.addElement("HOUR").addText(h.toString());//时
	        	dt1.addElement("MINUTE").addText(f.toString());//分
	        	dt1.addElement("SECOND").addText(s.toString());//秒
	        	dt1.addElement("TIMEZONE").addText("+0800");//时区
	        	Element qa=inven.addElement("QUANTITY").addAttribute("QUANTITY", "ITEM");
	        	qa.addElement("VALUE").addText(sl);//数量
	        	qa.addElement("SIGH").addText("-");//减
	        	qa.addElement("UOM").addText("EACH");
	        	inven.addElement("ITEM").addText(xh);//型号
	        	inven.addElement("SITELEVEL").addAttribute("index","1").addText("M030");
	        	inven.addElement("ITEMDESC").addText("SDSDQUAN-128G,Mobile Ultra uSD,48MB/s,C1");
	        	inven.addElement("ITEMSTATUS");
	        	inven.addElement("ORDNUM").addText(" ");
	        	inven.addElement("TRANSACTION").addText("STO");
	        	inven.addElement("RELNUM").addText(hh);//行号
	        	inven.addElement("REASONCODE");
	        	inven.addElement("REASONDESC");
	        	String sx =DBSql.getString("SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE xlbm='"+xlbm+"'", "XLMC");
	        	inven.addElement("LOCNUM").addText(sx);//属性
				
			}
        	DBSql.close(conn,null,null);
        	
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("us-ascii");
			format.setIndent(true);
		 
			XMLWriter writer = new XMLWriter(new FileWriter(new File(url)),format);
			doc.normalize();
			writer.write(doc);
			writer.flush();
			writer.close();
            System.out.println("怎么这样啊啊 啊啊啊啊");
			HelloAWSProcessEventBiz.cretatEror("chinabj_SHPCNF_"+y+m+d+h+f+s+".xml","9#报文生成成功","没有");//生成日志
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
		
		
	}

	//查询交付单的bindid
    private int selectJFbindid(int bindid) {
		// TODO Auto-generated method stub
		return 0;
	}

	//生成送修单的bindid
	private void selectSXbindid(int bindid) {
		System.out.println("正在查询");
		Connection conn =DBSql.open();
		Statement stmt=null;
		ResultSet rset=null;
		try {
			stmt = conn.createStatement();
			rset=DBSql.executeQuery(conn, stmt, "select * from BO_AKL_SAP_QDXXSP where BINDID='"+bindid+"' and SPJG='通过'");
			if(rset!=null){
				while(rset.next()){
					sxbindid=rset.getInt("SXBINDID");
					jfbindid=rset.getInt("JFBINDID");
				}
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DBSql.close(conn,stmt,rset);
		}
	}
    //生成9#报文
	private void creatXml9(int bindid,String url) {
		Calendar calendar = Calendar.getInstance();
        Integer y=calendar.get(Calendar.YEAR);//年
        Integer m=calendar.get(Calendar.MONTH)+1;//月
        Integer d=calendar.get(Calendar.DATE);//日
        Integer h=calendar.get(Calendar.HOUR_OF_DAY);//时
        Integer f=calendar.get(Calendar.MINUTE);//分
	    Integer s=calendar.get(Calendar.SECOND);//秒
	    Integer hs=calendar.get(Calendar.MILLISECOND);//毫秒
		String urll=url+"/chinabj_SHPCNF_"+y+m+d+h+f+s+".xml(送修)";//9报文路径
		
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("SYNC_INVENTORY_005");
			Element cn =root.addElement("CNTROLAREA");
			
			Element bsr =cn.addElement("BSR");
			bsr.addElement("VERB").addText("SYNC");
			bsr.addElement("NOUN").addText("INVENTORY");
			bsr.addElement("REVISION").addText("005");
			
			Element sender=cn.addElement("SENDER");
			sender.addElement("LOGICALID").addText("M030");
			sender.addElement("COMPONENT").addText("M030");
			sender.addElement("TASK");
			sender.addElement("REFERENCEID").addText("5155027_2");
			sender.addElement("CONFIRMATION").addText("0");
			sender.addElement("LANGUAGE").addText("EN");
			sender.addElement("CODEPAGE");
			sender.addElement("AUTHID").addText("Prod");
			
			Element dt=cn.addElement("DATETIME").addAttribute("qualifier", "CREATION");
			dt.addElement("YEAR").addText(y.toString());//年
			dt.addElement("MONTH").addText(m.toString());//月
			dt.addElement("DAY").addText(d.toString());//天
			dt.addElement("HOUR").addText(h.toString());//小时
			dt.addElement("MINUTE").addText(f.toString());//24
		    dt.addElement("SECOND").addText(s.toString());//25
			dt.addElement("SUBSECOND").addText(hs.toString());//26
			dt.addElement("TIMEZONE").addText("+0800");//
            //根据bindid查询送修单数据
			Vector<Hashtable<String,String>> vector=BOInstanceAPI.getInstance().getBODatas("BO_AKL_SX_S", bindid);
			Connection conn=DBSql.open();
			for(Hashtable<String,String> re:vector){
				String sl=re.get("SL");
				String xh=re.get("XH");
				String xlbm=re.get("SX");
				String hh=re.get("SXCPHH");
				Element da = root.addElement("DATAAREA");
	        	Element si = da.addElement("SYNC_INVENTORY");
	        	Element inven  = si.addElement("INVENTORY");
	        	inven.addElement("ACTTYPE").addText("TRANS");
	        	String sxdh=DBSql.getString("select SXDH from BO_AKL_SX_P where bindid='"+bindid+"'", "SXDH");
	        	System.out.println("送修单号"+sxdh);
	        	inven.addElement("TRANSID").addText(sxdh);//送修单号
	        	Element dt1 = inven.addElement("DATETIME").addAttribute("qualifier", "EFFECTIVE");
	        	dt1.addElement("YEAR").addText(y.toString());//年
	        	dt1.addElement("MONTH").addText(m.toString());//月
	        	dt1.addElement("DAY").addText(d.toString());//日
	        	dt1.addElement("HOUR").addText(h.toString());//时
	        	dt1.addElement("MINUTE").addText(f.toString());//分
	        	dt1.addElement("SECOND").addText(s.toString());//秒
	        	dt1.addElement("TIMEZONE").addText("+0800");//时区
	        	Element qa=inven.addElement("QUANTITY").addAttribute("QUANTITY", "ITEM");
	        	qa.addElement("VALUE").addText(sl);//数量
	        	qa.addElement("SIGH").addText("+");//加
	        	qa.addElement("UOM").addText("EACH");
	        	inven.addElement("ITEM").addText(xh);//型号
	        	inven.addElement("SITELEVEL").addAttribute("index","1").addText("M030");
	        	inven.addElement("ITEMDESC").addText("SDSDQUAN-128G,Mobile Ultra uSD,48MB/s,C1");
	        	inven.addElement("ITEMSTATUS");
	        	inven.addElement("ORDNUM").addText(" ");
	        	inven.addElement("TRANSACTION").addText("STO");
	        	inven.addElement("RELNUM").addText(hh);//行号
	        	inven.addElement("REASONCODE");
	        	inven.addElement("REASONDESC");
	        	String sx =DBSql.getString("SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE xlbm='"+xlbm+"'", "XLMC");
	        	inven.addElement("LOCNUM").addText(sx);//属性
				
			}
        	DBSql.close(conn,null,null);
        	
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("us-ascii");
			format.setIndent(true);
		 
			XMLWriter writer = new XMLWriter(new FileWriter(new File(urll)),format);
			doc.normalize();
			writer.write(doc);
			writer.flush();
			writer.close();
			HelloAWSProcessEventBiz.cretatEror("chinabj_SHPCNF_"+y+m+d+h+f+s+".xml","9#报文生成成功","没有");//生成日志
			creatXml91(jfbindid,url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
		
	}

}
