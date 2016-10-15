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
 * ��ȡ3�ű���,���ɲ�Ʒ��Ϣ,�ظ�4�ű���
 * @author luxiangyu
 *
 */
public class Sapjfd implements IJob{
	private static String vers;//������ʽ
	private static String logicalid;//��������
    private static String rx;//��Ȩ����
    private static String shipper;//�ջ�id
    private static String value;//����
    private static String hh;//�к�
    private static String upc;//upc��
    private static String xh;//�ͺ�
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("��ʱ��������");
		String url="/\\10.10.10.70/edics";//3#����·��
		String okurl="/\\10.10.10.70/bak";//�ɹ����պ����·��
		String erurl="/\\10.10.10.70/ErrorList";//��������·��
		String url4="/\\10.10.10.70/SendCS/4";//����4#���ĵ�·��
		List<String> filename=FileOperateDemo.SystemFileName(url);
		for(String name:filename){
			if(!name.contains("ZORDERS_V2")){
				FileOperateDemo.cutGeneralFile(url+"/"+name,erurl);//�ļ�����
				HelloAWSProcessEventBiz.cretatEror(name, "3#����δ�ܳɹ�����", "�ļ�����ʽ����ȷ");//���´���ԭ��
				continue;
			}
			//��ȡ���ű���
			selectXml(url+"/"+name);//��ȡ���ű���
			
			if(!vers.equals("ADD")){
		    	 System.out.println("������ʽΪ"+vers+"����1�ű��ı�׼���Լ���ִ��");
		    	 //���������ʽ��Ϊadd,�������(�����)
		    	 FileOperateDemo.cutGeneralFile(url+"/"+name, erurl);//�ļ�����
		    	 HelloAWSProcessEventBiz.cretatEror(name, "3#����δ�ܳɹ�����", "������ʽ����ȷ");//���´���ԭ��
		    	 return;
		     }
		     if(!logicalid.equals("M030")){
		    	 System.out.println("���շ�Ϊ"+logicalid+"���Լ���ִ��");
		    	 //����ջ���ΪM030R,�������(�����)
		    	 FileOperateDemo.cutGeneralFile(url+"/"+name, erurl);//�ļ�����
		    	 HelloAWSProcessEventBiz.cretatEror(name, "3#����δ�ܳɹ�����", "������������");//���´���ԭ��
		    	 return;
		     }
		     HelloAWSProcessEventBiz.cretatEror(name, "3#���ĳɹ�����", "û��");//���´���ԭ��
		     createXML(url4); 
		   //������4�ű����Ժ�3�ű��ļ��е�ָ���ļ���(�����)
				System.out.println(name);
				FileOperateDemo.cutGeneralFile(url+"/"+name, okurl+"/"+Calendar.getInstance().get(Calendar.YEAR)+"/"+(Calendar.getInstance().get(Calendar.MONTH)+1)+"/3#");//�ļ�����
	     }
	}
		 
	/**
	 * ��ȡxml(3�ű���)
	 * @param url �ļ�·��
	 */
	public void selectXml(String url){
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(new File(url));
			Element node = doc.getRootElement();
	        listNodes(node);//����
		    vers= node.element("CNTROLAREA").element("BSR").elementText("VERB");//������ʽ
		    
			logicalid= node.element("CNTROLAREA").element("SENDER").elementText("LOGICALID");//��������
			
			rx= node.element("DATAAREA").element("ADD_SALESORDER").element("SOHEADER").elementText("SALESORDID");//��Ȩ����
			 
		    Element e33 =(Element)doc.selectSingleNode("//SOHEADER/NOTES[@description='External Order/Shipper ID']");//�ջ���id
		    shipper=e33.getText();
		    Element e = node.element("DATAAREA").element("ADD_SALESORDER").element("SOHEADER");
		    String khmc= e.element("PARTNER").elementText("NAME");//�ͻ�����
		    Element el =e.element("PARTNER").element("ADDRESS"); 
		    Element e58 = (Element)doc.selectSingleNode("//ADDRLINE[@index='1']");
		    String dz=e58.getText();
		    String city= el.elementText("CITY");//����
		    String gj=el.elementText("COUNTRY");//����
		    String yb= el.elementText("POSTALCODE");//�ʱ�
		    String sjh= el.elementText("TELEPHONE");//�绰
		    Element ele = node.element("DATAAREA").element("ADD_SALESORDER").element("SOLINE");
		    value = ele.element("QUANTITY").elementText("VALUE");//����
		    hh= ele.elementText("SOLINENUM");//�к�
		    Element e95 = (Element)doc.selectSingleNode("//SOLINE/NOTES[@description='UPC']");//upc��
		    upc=e95.getText();
		    xh= ele.elementText("ITEM");//�ͺ�
			Hashtable<String,String> recordData = new Hashtable<String,String>();
		    recordData.put("RXBM", rx);
		    recordData.put("BWLX", "3#"); 
		    recordData.put("XH", xh);
		    recordData.put("SL", value);
		    recordData.put("ZT", "��Ч");
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
     * ������ǰ�ڵ�Ԫ�����������(Ԫ�ص�)�ӽڵ� 
     *  
     * @param node ��ǰԪ�ؽڵ�
     */  
	public void listNodes(Element node){
		Iterator<Element> it=node.elementIterator();
		while(it.hasNext()){
			Element e = it.next();
			listNodes(e);
		}
		
	}
	 
	 /**
	  * ����4�ű���
	  * @param url ���ɺ��ļ�·��
	  */
	public void createXML(String url){
		Calendar calendar = Calendar.getInstance();
       Integer y=calendar.get(Calendar.YEAR);//��
       Integer m=calendar.get(Calendar.MONTH)+1;//��
       Integer d=calendar.get(Calendar.DATE);//��
       Integer h=calendar.get(Calendar.HOUR_OF_DAY);//ʱ
       Integer f=calendar.get(Calendar.MINUTE);//��
	   Integer s=calendar.get(Calendar.SECOND);//��
	   Integer hs=calendar.get(Calendar.MILLISECOND);//����
	   SimpleDateFormat dateFm = new SimpleDateFormat("yyyyMMddHHmmSS"); //��ʽ����ǰϵͳ����
	   String dateTime = dateFm.format(new java.util.Date());
	   url=url+"/chinarmabj_ORDER_ACK_"+dateTime+".xml";//4�ű���·��
		
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
			dt.addElement("YEAR").addText(y.toString());//��
			dt.addElement("MONTH").addText(m.toString());//��
			dt.addElement("DAY").addText(d.toString());//��
			dt.addElement("HOUR").addText(h.toString());//Сʱ
			dt.addElement("MINUTE").addText(f.toString());//24
			dt.addElement("SECOND").addText(s.toString());//25
			dt.addElement("SUBSECOND").addText(hs.toString());//26
			dt.addElement("TIMEZONE").addText("+0800");//27
			
	
			Element da=root.addElement("DATAAREA");
			Element msg=da.addElement("MSG");
			Element msgdetails=msg.addElement("MSGDETAILS");
			//����rx�����ý�������
			String jfdh=DBSql.getString("select JFDH from BO_AKL_WXJF_P where SQBM='"+rx+"'", "JFDH");
			msgdetails.addElement("ORDFILEREF").addText(jfdh);//����33
			msgdetails.addElement("ORDERREFNUM").addText(rx);//��Ȩ����34
			msgdetails.addElement("SHIPPERNUM").addText(shipper);//35
			msgdetails.addElement("MMIORD").addText(jfdh);//����36
			msgdetails.addElement("STATUS").addText("00");//
			msgdetails.addElement("DESCRIPTION").addText("SUCCESSFUL");//
			
			Element msgline=da.addElement("MSGLINE");
			msgline.addElement("LINENUM").addText(hh);//�к�42
			msgline.addElement("ITEM").addText(xh);//�ͺ�43
			Element quantity=msgline.addElement("QUANTITY").addAttribute("qualifier", "ITEM");
			quantity.addElement("VALUE").addText(value);//����
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
			
			HelloAWSProcessEventBiz.cretatEror("chinarmabj_ORDER_ACK_"+dateTime+".xml", "4#�������ɳɹ�", "û��");//������־
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
