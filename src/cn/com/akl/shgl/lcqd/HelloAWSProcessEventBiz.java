package cn.com.akl.shgl.lcqd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
 
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;
/**
 * 读取1#报文,启动送修流程,生成2#报文
 * @author 鲁祥宇
 *
 */
public class HelloAWSProcessEventBiz implements IJob{
	private static String salesordid;//授权编码
	private static String shipper;//发货
	private static String khmc;//客户名称
	private static String addrline;//详细地址
	private static String city;//城市
	private static String country;//国家
	private static String postalcode;//邮编
	private static String telephone;//电话
	private static int value;//数量
	private static String solinenum;//行号
	private static String upc;//upc码
	private static String item;//型号
	private static String sxdh;//送修单号
	private static String sheng;//省
	private static String shi;//市
	private static String qx;//区县
	private static String wlbh;//物料编号
	private static String wlmc;//物料名称
	private static String jg;//物料价格
	private static String sfdc;//物料是否调查
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException{
		String url ="/\\10.10.10.70/edics";//1#报文接收路径
		String url2="/\\10.10.10.70/SendCS/2";//2号报文生成路径
		String erurl="/\\10.10.10.70/ErrorList";//错误时剪切路径
		String okurl="/\\10.10.10.70/bak";//1#报文成功后剪切路径
		List<String> Filenames=FileOperateDemo.SystemFileName(url);
		for(String name:Filenames){
			//判断是否为3#
			if(name.contains("ZORDERS_V2")){
				//读取
				if(selectXml(url,name,erurl)){
					//记录日志
					cretatEror(name,"3#报文接收成功","没有");
					//查询授权编码(没有则启动流程)
					if(!selectRX(salesordid,item,telephone)){
						selectSSQ(city,qx);//查询省市区
						if(sheng.contains("台湾")){
							//查询物料
						    if(selectWl(item)){
						    	Generation(name);//启动流程,生成送修表单
						    }else{
						    	FileOperateDemo.cutGeneralFile(url,erurl);//文件剪切
								cretatEror(name,"流程启动失败","物料不存在");//生成日志
						    }
						}
					}
				}
				
			}else if(!name.contains("ZRMAORDERS")){
				FileOperateDemo.cutGeneralFile(url+"/"+name, erurl);//文件剪切
				cretatEror(name,"1#报文未能成功接收","文件名格式不正确");//生成日志
				continue;
			}else if(selectXml(url,name,erurl)){
				cretatEror(name,"1#报文接收成功","没有");
				//记录1#报文
				
				//查询是否存在授权编码
				if(selectRX(salesordid,item,telephone)){
					//查询送修单
					
					createXML(url2,name);//生成2#报文  
				}else{
					selectSSQ(city,qx);//查询省市区
					//查询物料
				    if(selectWl(item)){
				    	Generation(name);//启动流程,生成送修表单
						createXML(url2,name);//生成2#报文  
						//生成完2号报文以后将一号报文剪切到指定文件夹(待完成)
						System.out.println(name);
						System.out.println("当前月份"+Calendar.getInstance().get(Calendar.MONTH));
						FileOperateDemo.cutGeneralFile(url+"/"+name,okurl+"/"+Calendar.getInstance().get(Calendar.YEAR)+"/"+(Calendar.getInstance().get(Calendar.MONTH)+1)+"/1#");//文件剪切
				    }else{
				    	FileOperateDemo.cutGeneralFile(url,erurl);//文件剪切
						cretatEror(name,"流程启动失败","物料不存在");//生成日志
				    }
				}
				
			}
	    }
	}
	//查询授权编码是否已存在
	private boolean selectRX(String salesordid,String xh,String sjh) {
		Connection conn=DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, "select p.sqbm,* from BO_AKL_SX_P p LEFT JOIN BO_AKL_SX_S s ON p.BINDID=s.BINDID "
					+ "WHERE p.SJH='"+sjh+"' AND s.XH='"+xh+"'" );
			if(rset!=null){
				while(rset.next()){
					 if(salesordid.equals(rset.getString("SQBM"))){
						 sxdh=rset.getString("SXDH");
						 return true; 
					 }
					
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}finally{
			DBSql.close(conn, stmt, rset);
		}
		return false;
	}
	/**
	 * 读取xml(一号报文)
	 * @param url 文件路径
	 */
	public boolean selectXml(String url,String name,String erurl){
		url =url+"/"+name;
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(new File(url));
			Element node = doc.getRootElement();
	        listNodes(node);//遍历
		    
		    String vers =node.element("CNTROLAREA").element("BSR").elementText("VERB");//操作方式
			String logicalid = node.element("CNTROLAREA").element("SENDER").elementText("LOGICALID");//接收方式
			Element e = node.element("DATAAREA").element("ADD_SALESORDER").element("SOHEADER");
			salesordid = e.elementText("SALESORDID");//授权编码
		    String salesorg = e.elementText("SALESORG");//收货方
		    Element e33 =(Element)doc.selectSingleNode("//SOHEADER/NOTES[@description='External Order/Shipper ID']");
		    shipper = e33.getText();//发货id
		    khmc= e.element("PARTNER").elementText("NAME");//客户姓名
		    Element el =e.element("PARTNER").element("ADDRESS"); 
		    Element e57 = (Element)doc.selectSingleNode("//ADDRLINE[@index='2']");
		    Element e58 = (Element)doc.selectSingleNode("//ADDRLINE[@index='3']");
		    qx=e58.getText();//区县
		    addrline=el.elementText("ADDRLINE")+e57.getText()+qx;//地址
		    city= el.elementText("CITY");//城市
		    country=el.elementText("COUNTRY");//国家
		    postalcode= el.elementText("POSTALCODE");//邮编
		    telephone= el.elementText("TELEPHONE");//电话
		    Element ele = node.element("DATAAREA").element("ADD_SALESORDER").element("SOLINE");
		    value=Integer.parseInt(ele.element("QUANTITY").elementText("VALUE"));//数量
		    solinenum= ele.elementText("SOLINENUM");//行号
		    Element e95 = (Element)doc.selectSingleNode("//SOLINE/NOTES[@description='UPC']");
		    upc= e95.getText();//upc码
		    item=ele.elementText("ITEM");//物料型号
		    
		    if(!vers.equals("ADD")){
		    	 FileOperateDemo.cutGeneralFile(url,erurl);//文件剪切
		    	 cretatEror(name,"报文接收失败","操作方式不符合");
		    	 return false;
		     }
		     if(!logicalid.contains("M030")){
		    	 //如果收货不为M030R,则需剪切(待完成)
		    	 FileOperateDemo.cutGeneralFile(url,erurl);//文件剪切
		    	 cretatEror(name,"报文接收失败","不是亚昆接收");
		    	 return false;
		     }
		     if(!salesorg.equals("M030")){
		    	 //如果收货方不为M030,
		    	 FileOperateDemo.cutGeneralFile(url,erurl);//文件剪切
		    	 cretatEror(name,"报文接收失败","收货方不是亚昆");
		    	 return false;
		     }
		     //记录1#报文
		     Hashtable<String,String> recordData = new Hashtable<String,String>();
		        recordData.put("BWLX", "1#");
			    recordData.put("RXBM", salesordid);//授权编码
			    recordData.put("XH", item);//型号
			    recordData.put("SL", String.valueOf(value));//数量
			    recordData.put("ZT", "有效");
			    recordData.put("RXBMID", shipper);
			    recordData.put("HH", solinenum);
			    recordData.put("KHMC", khmc);
			    recordData.put("GJ",country);
			    recordData.put("SJH", telephone);
			    recordData.put("CITY", city);
			    recordData.put("YB", postalcode);
			    recordData.put("DZ", addrline);
			    BOInstanceAPI.getInstance().createBOData("BO_AKL_SAP_JF_CPXX", recordData, "admin");
			 
		} catch (DocumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace(); 
		} catch (AWSSDKException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	  return true;
		
	}
	 
	/** 
     * 遍历当前节点元素下面的所有(元素的)子节点 
     *  
     * @param node 当前元素节点
     */  
	public void listNodes(Element node){
		Iterator<Element> it=node.elementIterator();
		while(it.hasNext()){
			Element e = it.next();
			listNodes(e);
		}
		
	}
	 /**
	  * 生成2号报文
	  * @param url 生成后文件路径
	  */
	public void createXML(String url,String name){
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
		url=url+"/chinarmabj_ORDER_ACK_"+dateTime+".xml";//2号报文路径
		
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("ORDER_ACK");
			Element cn =root.addElement("CNTROLAREA");
			
			Element bsr =cn.addElement("BSR");
			bsr.addElement("VERB").addText("ACK");
			bsr.addElement("NOUN").addText("ORDER");
			bsr.addElement("REVISION").addText("001");
			
			Element sender=cn.addElement("SENDER");
			sender.addElement("LOGICALID").addText("M030R");
			sender.addElement("COMPONENT").addText("M030R");
			sender.addElement("TASK");
			sender.addElement("REFERENCEID").addText("19162157_336");
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
			dt.addElement("TIMEZONE").addText("+0800");//27
			
	
			Element da=root.addElement("DATAAREA");
			Element msg=da.addElement("MSG");
			Element msgdetails=msg.addElement("MSGDETAILS");
			//单号有错
			System.out.println("送修单号,送修单号,送修单号"+sxdh);
			msgdetails.addElement("ORDFILEREF").addText(sxdh);//单号33
			msgdetails.addElement("ORDERREFNUM").addText(salesordid);//授权编码34
			msgdetails.addElement("SHIPPERNUM").addText(shipper);//35---------------------------------
			msgdetails.addElement("MMIORD").addText(sxdh);//单号36
			msgdetails.addElement("STATUS").addText("00");//
			msgdetails.addElement("DESCRIPTION").addText("SUCCESSFUL");//
			
			Element msgline=da.addElement("MSGLINE");
			msgline.addElement("LINENUM").addText(solinenum);//行号42
			msgline.addElement("ITEM").addText(item);
			Element quantity=msgline.addElement("QUANTITY").addAttribute("qualifier", "ITEM");
			quantity.addElement("VALUE").addText(String.valueOf(value));//数量
			quantity.addElement("UOM").addText("EA");
			
			msgline.addElement("STATUS").addText("00");
			OutputFormat format = OutputFormat.createCompactFormat();
			format.setEncoding("us-ascii");
			format.setIndent(true);
			format.setNewlines(true);
		 
			XMLWriter writer = new XMLWriter(new FileWriter(new File(url)),format);
			doc.normalize();
			writer.write(doc);
			writer.flush();
			writer.close();
			
			cretatEror("chinarmabj_ORDER_ACK_"+dateTime+".xml","2#报文生成成功","没有");//生成日志
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 启动流程
	 * 生成表单
	 */
	public void Generation(String name){
		try {
			//创建工作流实例
			int processInstanceId = WorkflowInstanceAPI.getInstance().createProcessInstance("298b3069c9749c4e34c258bf80f2b931","101003","鲁祥宇的送修单");
			//创建任务实例
			int [] processTaskInstanceIds = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance("101003", processInstanceId,1,"101003","鲁祥宇的送修单");
			selectKhbyinsert(processInstanceId);//查找客户信息,没有则创建
			//初始化bo数据
			Hashtable<String,String> recordData = new Hashtable<String,String>();
			sxdh=RuleAPI.getInstance().executeRuleScript("SX@replace(@date,-)@formatZero(4,@sequencefordateandkey(BO_AKL_SX_P))");
			recordData.put("SXDH",sxdh);//送修单号
			recordData.put("XMLB","061270");//项目类别
			recordData.put("YWLX","083330");//业务类型
			recordData.put("SXFS","0");//送修方式
			recordData.put("SQBM", salesordid);//授权编码
			recordData.put("RXBMHQFS","提前获取");//RX编码获取方式
			recordData.put("SXYY","无");//送修原因
			recordData.put("KHMC",khmc);//客户名称
			recordData.put("KHLXMC","个人");//客户类型
			 
			recordData.put("SJH",telephone);//手机号
			recordData.put("YB",postalcode);//邮编
			String c = DBSql.getString("SELECT COUNTRY FROM BO_AKL_SH_COUNTRY WHERE COUNTRYID ='"+country+"'", "COUNTRY");
			recordData.put("GJ",c);//国家
			recordData.put("S",sheng);//省
			recordData.put("SHI",shi);//市
			recordData.put("QX",qx);//区/县
			recordData.put("XXDZ", sheng+shi+addrline);
			recordData.put("SFYDYP","025001");//是否有代用品
			recordData.put("SFSJ","025001");//是否升级
		    int boId = BOInstanceAPI.getInstance().createBOData("BO_AKL_SX_P", recordData, processInstanceId, "101003");
		    
			Vector<Hashtable<String,String>> zibiao = new Vector<Hashtable<String,String>>();//子表
			for(int i=0;i<value;i++){
				Hashtable<String,String> s1 = new Hashtable<String,String>();
				s1.put("WLBH",wlbh);//设置物料编号
				s1.put("XH",item);//设置物料型号
				s1.put("CCPN", item);//设置北京pn
				s1.put("WLMC",wlmc);//设置物料名称
				s1.put("SL","1");//设置物料数量
				s1.put("SXCPHH",solinenum);//设置物料产品行号
				s1.put("JG", jg);//设置物料价格
				s1.put("SFDC",sfdc);//设置物料是否调查
				zibiao.add(s1);
			}
			
			int[] boIds = BOInstanceAPI.getInstance().createBOData("BO_AKL_SX_S",zibiao,processInstanceId,"101003");
			
			
		} catch (AWSSDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * 查找用户是否老客户,不是则新建客户信息
	 * @param name 客户姓名
	 * @param telephone 客户电话
	 */
	public void selectKhbyinsert(int processInstanceId){
		String[] xm= khmc.split(" ");
		khmc=xm[1]+xm[0];
		Connection conn = DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt,"select * from BO_AKL_SH_KH where KHMC='"+khmc+"' and SJH='"+telephone+"'");
			if(rset!=null){
				while(rset.next()){
					return;
					}
			   try {
					Hashtable<String,String> recordData = new Hashtable<String,String>();
					recordData.put("KHBH",RuleAPI.getInstance().executeRuleScript("KH@replace(@date,-)@formatZero(4,@sequencefordateandkey(BO_AKL_SH_KH))"));//送修单号
					recordData.put("KHMC",khmc);//客户名称
					recordData.put("KHLX", "062277");//客户类型,默认为个人
					recordData.put("XMLB","061270");//项目类别,默认为闪迪
					recordData.put("SJH", telephone);//手机号
					recordData.put("S", sheng);//省
					recordData.put("SHI", shi);//市
					recordData.put("QX", qx);//区县
					recordData.put("DZ", addrline);//地址
					recordData.put("GJ", country);//国家
					recordData.put("YB", postalcode);//邮编
					recordData.put("KHJB", "086335");//客户级别默认为A级
				    BOInstanceAPI.getInstance().createBOData("BO_AKL_SH_KH", recordData, processInstanceId, "admin");
				
			       } catch (AWSSDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace(System.err);
			       }
			}
			 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}finally{
			DBSql.close(conn, stmt, rset);
		}
		
	}
	/**
	 * 查询物料
	 * @param wlxh 物料型号
	 */
	public boolean selectWl(String xh){
		Connection conn=DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, "select * from BO_AKL_CPXX where LPN8='"+xh+"'");
			if(rset!=null){
				while(rset.next()){
					 wlbh=rset.getString("WLBH");
					 wlmc=rset.getString("WLMC");
					 jg=rset.getString("JG");
					 sfdc=rset.getString("SFDC");
					
					 return true;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}finally{
			DBSql.close(conn, stmt, rset);
		}
		return false;
	}
	/**
	 * 查询省 市 区
	 */
	public void selectSSQ(String city,String xian){
		if(!city.contains("|")){
			city="";
		}
		if(city.equals("")){
			city="北京市|市辖区";
		}
		String[] ss= city.split("\\|");
	    sheng=ss[0];
		shi=ss[1];
		Connection conn=DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, "SELECT TOP 1 r.REGIONNAME,r3.shi,r3.quxian FROM BO_BASE_REGION AS r "
					+ "LEFT JOIN (SELECT r1.REGIONNAME AS shi, r2.REGIONNAME AS quxian,r1.PARENTID AS a FROM BO_BASE_REGION AS r1 "
					+ "LEFT JOIN BO_BASE_REGION r2 ON r1.REGIONID = r2.PARENTID WHERE r1.REGIONLEVEL = 2) AS r3 "
					+ "ON r.REGIONID = r3.a WHERE r.regionlevel = 1 AND r.regionname LIKE '%"+sheng+"%' AND r3.shi LIKE '%"+shi+"%' AND r3.quxian like '%"+xian+"%'");
			if(rset!=null){
				while(rset.next()){
					  sheng=rset.getString("REGIONNAME");
					  shi=rset.getString("shi");
					  qx=rset.getString("quxian");
					  return;
				}
				sheng="北京市";
				shi="市辖区";
				 
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}finally{
			DBSql.close(conn, stmt, rset);
		}
		
	}
	/**
	 * 生成日志
	 * @param name 文件名
	 */
	public static void cretatEror(String name,String zt,String cwyy){
		 Hashtable<String,String> recordData = new Hashtable<String,String>();
		 recordData.put("WJM", name);
		  
		 
		 recordData.put("ZT", zt);
		 recordData.put("CWYY", cwyy);
		 try {
			BOInstanceAPI.getInstance().createBOData("BO_AKL_SAP_RZ", recordData, "admin");
		} catch (AWSSDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

