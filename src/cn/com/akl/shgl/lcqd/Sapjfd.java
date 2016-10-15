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
import java.util.Iterator;
import java.util.List;
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
 * 读取3号报文,生成产品信息,回复4号报文
 * @author luxiangyu
 *
 */
public class Sapjfd implements IJob{
	private static String vers;//操作方式
	private static String logicalid;//亚昆接收
    private static String rx;//授权编码
    private static String shipper;//收货id
    private static String value;//数量
    private static String hh;//行号
    private static String upc;//upc码
    private static String xh;//型号
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("定时器启动了");
		String url="/\\10.10.10.70/edics";//3#报文路径
		String okurl="/\\10.10.10.70/bak";//成功接收后剪切路径
		String erurl="/\\10.10.10.70/ErrorList";//错误后剪切路径
		String url4="/\\10.10.10.70/SendCS/4";//生成4#报文的路径
		List<String> filename=FileOperateDemo.SystemFileName(url);
		for(String name:filename){
			if(!name.contains("ZORDERS_V2")){
				FileOperateDemo.cutGeneralFile(url+"/"+name,erurl);//文件剪切
				HelloAWSProcessEventBiz.cretatEror(name, "3#报文未能成功接收", "文件名格式不正确");//更新错误原因
				continue;
			}
			//读取三号报文
			selectXml(url+"/"+name);//读取三号报文
			
			if(!vers.equals("ADD")){
		    	 System.out.println("操作方式为"+vers+"符合1号报文标准可以继续执行");
		    	 //如果操作方式不为add,则需剪切(待完成)
		    	 FileOperateDemo.cutGeneralFile(url+"/"+name, erurl);//文件剪切
		    	 HelloAWSProcessEventBiz.cretatEror(name, "3#报文未能成功接收", "操作方式不正确");//更新错误原因
		    	 return;
		     }
		     if(!logicalid.equals("M030")){
		    	 System.out.println("接收方为"+logicalid+"可以继续执行");
		    	 //如果收货不为M030R,则需剪切(待完成)
		    	 FileOperateDemo.cutGeneralFile(url+"/"+name, erurl);//文件剪切
		    	 HelloAWSProcessEventBiz.cretatEror(name, "3#报文未能成功接收", "不是亚昆接收");//更新错误原因
		    	 return;
		     }
		     HelloAWSProcessEventBiz.cretatEror(name, "3#报文成功接收", "没有");//更新错误原因
		     createXML(url4); 
		   //生成完4号报文以后将3号报文剪切到指定文件夹(待完成)
				System.out.println(name);
				FileOperateDemo.cutGeneralFile(url+"/"+name, okurl+"/"+Calendar.getInstance().get(Calendar.YEAR)+"/"+(Calendar.getInstance().get(Calendar.MONTH)+1)+"/3#");//文件剪切
	     }
	}
		 
	/**
	 * 读取xml(3号报文)
	 * @param url 文件路径
	 */
	public void selectXml(String url){
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(new File(url));
			Element node = doc.getRootElement();
	        listNodes(node);//遍历
		    vers= node.element("CNTROLAREA").element("BSR").elementText("VERB");//操作方式
		    
			logicalid= node.element("CNTROLAREA").element("SENDER").elementText("LOGICALID");//亚昆接收
			
			rx= node.element("DATAAREA").element("ADD_SALESORDER").element("SOHEADER").elementText("SALESORDID");//授权编码
			 
		    Element e33 =(Element)doc.selectSingleNode("//SOHEADER/NOTES[@description='External Order/Shipper ID']");//收货方id
		    shipper=e33.getText();
		    Element e = node.element("DATAAREA").element("ADD_SALESORDER").element("SOHEADER");
		    String khmc= e.element("PARTNER").elementText("NAME");//客户姓名
		    Element el =e.element("PARTNER").element("ADDRESS"); 
		    Element e58 = (Element)doc.selectSingleNode("//ADDRLINE[@index='1']");
		    String dz=e58.getText();
		    String city= el.elementText("CITY");//城市
		    String gj=el.elementText("COUNTRY");//国家
		    String yb= el.elementText("POSTALCODE");//邮编
		    String sjh= el.elementText("TELEPHONE");//电话
		    Element ele = node.element("DATAAREA").element("ADD_SALESORDER").element("SOLINE");
		    value = ele.element("QUANTITY").elementText("VALUE");//数量
		    hh= ele.elementText("SOLINENUM");//行号
		    Element e95 = (Element)doc.selectSingleNode("//SOLINE/NOTES[@description='UPC']");//upc码
		    upc=e95.getText();
		    xh= ele.elementText("ITEM");//型号
			Hashtable<String,String> recordData = new Hashtable<String,String>();
		    recordData.put("RXBM", rx);
		    recordData.put("BWLX", "3#"); 
		    recordData.put("XH", xh);
		    recordData.put("SL", value);
		    recordData.put("ZT", "有效");
		    recordData.put("RXBMID", shipper);
		    recordData.put("HH", hh);
		    recordData.put("KHMC", khmc);
		    recordData.put("GJ", gj);
		    recordData.put("SJH", sjh);
		    recordData.put("CITY", city);
		    recordData.put("YB", yb);
		    recordData.put("DZ", dz);
		    BOInstanceAPI.getInstance().createBOData("BO_AKL_SAP_JF_CPXX", recordData, "admin");
		 
		} catch (AWSSDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
		}catch (DocumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
 
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
	  * 生成4号报文
	  * @param url 生成后文件路径
	  */
	public void createXML(String url){
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
	   url=url+"/chinarmabj_ORDER_ACK_"+dateTime+".xml";//4号报文路径
		
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
		    sender.addElement("REFERENCEID").addText("1074034_173");
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
			//根据rx编码获得交付单号
			String jfdh=DBSql.getString("select JFDH from BO_AKL_WXJF_P where SQBM='"+rx+"'", "JFDH");
			msgdetails.addElement("ORDFILEREF").addText(jfdh);//单号33
			msgdetails.addElement("ORDERREFNUM").addText(rx);//授权编码34
			msgdetails.addElement("SHIPPERNUM").addText(shipper);//35
			msgdetails.addElement("MMIORD").addText(jfdh);//单号36
			msgdetails.addElement("STATUS").addText("00");//
			msgdetails.addElement("DESCRIPTION").addText("SUCCESSFUL");//
			
			Element msgline=da.addElement("MSGLINE");
			msgline.addElement("LINENUM").addText(hh);//行号42
			msgline.addElement("ITEM").addText(xh);//型号43
			Element quantity=msgline.addElement("QUANTITY").addAttribute("qualifier", "ITEM");
			quantity.addElement("VALUE").addText(value);//数量
			quantity.addElement("UOM").addText("EA");
			msgline.addElement("STATUS").addText("00");
			
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("us-ascii");
			format.setIndent(true);
		 
			XMLWriter writer = new XMLWriter(new FileWriter(new File(url)),format);
			doc.normalize();
			writer.write(doc);
			writer.flush();
			writer.close();
			
			HelloAWSProcessEventBiz.cretatEror("chinarmabj_ORDER_ACK_"+dateTime+".xml", "4#报文生成成功", "没有");//生成日志
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
