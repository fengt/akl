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
 * ��ȡ1#����,������������,����2#����
 * @author ³����
 *
 */
public class HelloAWSProcessEventBiz implements IJob{
	private static String salesordid;//��Ȩ����
	private static String shipper;//����
	private static String khmc;//�ͻ�����
	private static String addrline;//��ϸ��ַ
	private static String city;//����
	private static String country;//����
	private static String postalcode;//�ʱ�
	private static String telephone;//�绰
	private static int value;//����
	private static String solinenum;//�к�
	private static String upc;//upc��
	private static String item;//�ͺ�
	private static String sxdh;//���޵���
	private static String sheng;//ʡ
	private static String shi;//��
	private static String qx;//����
	private static String wlbh;//���ϱ��
	private static String wlmc;//��������
	private static String jg;//���ϼ۸�
	private static String sfdc;//�����Ƿ����
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException{
		String url ="/\\10.10.10.70/edics";//1#���Ľ���·��
		String url2="/\\10.10.10.70/SendCS/2";//2�ű�������·��
		String erurl="/\\10.10.10.70/ErrorList";//����ʱ����·��
		String okurl="/\\10.10.10.70/bak";//1#���ĳɹ������·��
		List<String> Filenames=FileOperateDemo.SystemFileName(url);
		for(String name:Filenames){
			//�ж��Ƿ�Ϊ3#
			if(name.contains("ZORDERS_V2")){
				//��ȡ
				if(selectXml(url,name,erurl)){
					//��¼��־
					cretatEror(name,"3#���Ľ��ճɹ�","û��");
					//��ѯ��Ȩ����(û������������)
					if(!selectRX(salesordid,item,telephone)){
						selectSSQ(city,qx);//��ѯʡ����
						if(sheng.contains("̨��")){
							//��ѯ����
						    if(selectWl(item)){
						    	Generation(name);//��������,�������ޱ�
						    }else{
						    	FileOperateDemo.cutGeneralFile(url,erurl);//�ļ�����
								cretatEror(name,"��������ʧ��","���ϲ�����");//������־
						    }
						}
					}
				}
				
			}else if(!name.contains("ZRMAORDERS")){
				FileOperateDemo.cutGeneralFile(url+"/"+name, erurl);//�ļ�����
				cretatEror(name,"1#����δ�ܳɹ�����","�ļ�����ʽ����ȷ");//������־
				continue;
			}else if(selectXml(url,name,erurl)){
				cretatEror(name,"1#���Ľ��ճɹ�","û��");
				//��¼1#����
				
				//��ѯ�Ƿ������Ȩ����
				if(selectRX(salesordid,item,telephone)){
					//��ѯ���޵�
					
					createXML(url2,name);//����2#����  
				}else{
					selectSSQ(city,qx);//��ѯʡ����
					//��ѯ����
				    if(selectWl(item)){
				    	Generation(name);//��������,�������ޱ�
						createXML(url2,name);//����2#����  
						//������2�ű����Ժ�һ�ű��ļ��е�ָ���ļ���(�����)
						System.out.println(name);
						System.out.println("��ǰ�·�"+Calendar.getInstance().get(Calendar.MONTH));
						FileOperateDemo.cutGeneralFile(url+"/"+name,okurl+"/"+Calendar.getInstance().get(Calendar.YEAR)+"/"+(Calendar.getInstance().get(Calendar.MONTH)+1)+"/1#");//�ļ�����
				    }else{
				    	FileOperateDemo.cutGeneralFile(url,erurl);//�ļ�����
						cretatEror(name,"��������ʧ��","���ϲ�����");//������־
				    }
				}
				
			}
	    }
	}
	//��ѯ��Ȩ�����Ƿ��Ѵ���
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
	 * ��ȡxml(һ�ű���)
	 * @param url �ļ�·��
	 */
	public boolean selectXml(String url,String name,String erurl){
		url =url+"/"+name;
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(new File(url));
			Element node = doc.getRootElement();
	        listNodes(node);//����
		    
		    String vers =node.element("CNTROLAREA").element("BSR").elementText("VERB");//������ʽ
			String logicalid = node.element("CNTROLAREA").element("SENDER").elementText("LOGICALID");//���շ�ʽ
			Element e = node.element("DATAAREA").element("ADD_SALESORDER").element("SOHEADER");
			salesordid = e.elementText("SALESORDID");//��Ȩ����
		    String salesorg = e.elementText("SALESORG");//�ջ���
		    Element e33 =(Element)doc.selectSingleNode("//SOHEADER/NOTES[@description='External Order/Shipper ID']");
		    shipper = e33.getText();//����id
		    khmc= e.element("PARTNER").elementText("NAME");//�ͻ�����
		    Element el =e.element("PARTNER").element("ADDRESS"); 
		    Element e57 = (Element)doc.selectSingleNode("//ADDRLINE[@index='2']");
		    Element e58 = (Element)doc.selectSingleNode("//ADDRLINE[@index='3']");
		    qx=e58.getText();//����
		    addrline=el.elementText("ADDRLINE")+e57.getText()+qx;//��ַ
		    city= el.elementText("CITY");//����
		    country=el.elementText("COUNTRY");//����
		    postalcode= el.elementText("POSTALCODE");//�ʱ�
		    telephone= el.elementText("TELEPHONE");//�绰
		    Element ele = node.element("DATAAREA").element("ADD_SALESORDER").element("SOLINE");
		    value=Integer.parseInt(ele.element("QUANTITY").elementText("VALUE"));//����
		    solinenum= ele.elementText("SOLINENUM");//�к�
		    Element e95 = (Element)doc.selectSingleNode("//SOLINE/NOTES[@description='UPC']");
		    upc= e95.getText();//upc��
		    item=ele.elementText("ITEM");//�����ͺ�
		    
		    if(!vers.equals("ADD")){
		    	 FileOperateDemo.cutGeneralFile(url,erurl);//�ļ�����
		    	 cretatEror(name,"���Ľ���ʧ��","������ʽ������");
		    	 return false;
		     }
		     if(!logicalid.contains("M030")){
		    	 //����ջ���ΪM030R,�������(�����)
		    	 FileOperateDemo.cutGeneralFile(url,erurl);//�ļ�����
		    	 cretatEror(name,"���Ľ���ʧ��","������������");
		    	 return false;
		     }
		     if(!salesorg.equals("M030")){
		    	 //����ջ�����ΪM030,
		    	 FileOperateDemo.cutGeneralFile(url,erurl);//�ļ�����
		    	 cretatEror(name,"���Ľ���ʧ��","�ջ�����������");
		    	 return false;
		     }
		     //��¼1#����
		     Hashtable<String,String> recordData = new Hashtable<String,String>();
		        recordData.put("BWLX", "1#");
			    recordData.put("RXBM", salesordid);//��Ȩ����
			    recordData.put("XH", item);//�ͺ�
			    recordData.put("SL", String.valueOf(value));//����
			    recordData.put("ZT", "��Ч");
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
	  * ����2�ű���
	  * @param url ���ɺ��ļ�·��
	  */
	public void createXML(String url,String name){
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
		url=url+"/chinarmabj_ORDER_ACK_"+dateTime+".xml";//2�ű���·��
		
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
			//�����д�
			System.out.println("���޵���,���޵���,���޵���"+sxdh);
			msgdetails.addElement("ORDFILEREF").addText(sxdh);//����33
			msgdetails.addElement("ORDERREFNUM").addText(salesordid);//��Ȩ����34
			msgdetails.addElement("SHIPPERNUM").addText(shipper);//35---------------------------------
			msgdetails.addElement("MMIORD").addText(sxdh);//����36
			msgdetails.addElement("STATUS").addText("00");//
			msgdetails.addElement("DESCRIPTION").addText("SUCCESSFUL");//
			
			Element msgline=da.addElement("MSGLINE");
			msgline.addElement("LINENUM").addText(solinenum);//�к�42
			msgline.addElement("ITEM").addText(item);
			Element quantity=msgline.addElement("QUANTITY").addAttribute("qualifier", "ITEM");
			quantity.addElement("VALUE").addText(String.valueOf(value));//����
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
			
			cretatEror("chinarmabj_ORDER_ACK_"+dateTime+".xml","2#�������ɳɹ�","û��");//������־
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * ��������
	 * ���ɱ�
	 */
	public void Generation(String name){
		try {
			//����������ʵ��
			int processInstanceId = WorkflowInstanceAPI.getInstance().createProcessInstance("298b3069c9749c4e34c258bf80f2b931","101003","³��������޵�");
			//��������ʵ��
			int [] processTaskInstanceIds = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance("101003", processInstanceId,1,"101003","³��������޵�");
			selectKhbyinsert(processInstanceId);//���ҿͻ���Ϣ,û���򴴽�
			//��ʼ��bo����
			Hashtable<String,String> recordData = new Hashtable<String,String>();
			sxdh=RuleAPI.getInstance().executeRuleScript("SX@replace(@date,-)@formatZero(4,@sequencefordateandkey(BO_AKL_SX_P))");
			recordData.put("SXDH",sxdh);//���޵���
			recordData.put("XMLB","061270");//��Ŀ���
			recordData.put("YWLX","083330");//ҵ������
			recordData.put("SXFS","0");//���޷�ʽ
			recordData.put("SQBM", salesordid);//��Ȩ����
			recordData.put("RXBMHQFS","��ǰ��ȡ");//RX�����ȡ��ʽ
			recordData.put("SXYY","��");//����ԭ��
			recordData.put("KHMC",khmc);//�ͻ�����
			recordData.put("KHLXMC","����");//�ͻ�����
			 
			recordData.put("SJH",telephone);//�ֻ���
			recordData.put("YB",postalcode);//�ʱ�
			String c = DBSql.getString("SELECT COUNTRY FROM BO_AKL_SH_COUNTRY WHERE COUNTRYID ='"+country+"'", "COUNTRY");
			recordData.put("GJ",c);//����
			recordData.put("S",sheng);//ʡ
			recordData.put("SHI",shi);//��
			recordData.put("QX",qx);//��/��
			recordData.put("XXDZ", sheng+shi+addrline);
			recordData.put("SFYDYP","025001");//�Ƿ��д���Ʒ
			recordData.put("SFSJ","025001");//�Ƿ�����
		    int boId = BOInstanceAPI.getInstance().createBOData("BO_AKL_SX_P", recordData, processInstanceId, "101003");
		    
			Vector<Hashtable<String,String>> zibiao = new Vector<Hashtable<String,String>>();//�ӱ�
			for(int i=0;i<value;i++){
				Hashtable<String,String> s1 = new Hashtable<String,String>();
				s1.put("WLBH",wlbh);//�������ϱ��
				s1.put("XH",item);//���������ͺ�
				s1.put("CCPN", item);//���ñ���pn
				s1.put("WLMC",wlmc);//������������
				s1.put("SL","1");//������������
				s1.put("SXCPHH",solinenum);//�������ϲ�Ʒ�к�
				s1.put("JG", jg);//�������ϼ۸�
				s1.put("SFDC",sfdc);//���������Ƿ����
				zibiao.add(s1);
			}
			
			int[] boIds = BOInstanceAPI.getInstance().createBOData("BO_AKL_SX_S",zibiao,processInstanceId,"101003");
			
			
		} catch (AWSSDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * �����û��Ƿ��Ͽͻ�,�������½��ͻ���Ϣ
	 * @param name �ͻ�����
	 * @param telephone �ͻ��绰
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
					recordData.put("KHBH",RuleAPI.getInstance().executeRuleScript("KH@replace(@date,-)@formatZero(4,@sequencefordateandkey(BO_AKL_SH_KH))"));//���޵���
					recordData.put("KHMC",khmc);//�ͻ�����
					recordData.put("KHLX", "062277");//�ͻ�����,Ĭ��Ϊ����
					recordData.put("XMLB","061270");//��Ŀ���,Ĭ��Ϊ����
					recordData.put("SJH", telephone);//�ֻ���
					recordData.put("S", sheng);//ʡ
					recordData.put("SHI", shi);//��
					recordData.put("QX", qx);//����
					recordData.put("DZ", addrline);//��ַ
					recordData.put("GJ", country);//����
					recordData.put("YB", postalcode);//�ʱ�
					recordData.put("KHJB", "086335");//�ͻ�����Ĭ��ΪA��
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
	 * ��ѯ����
	 * @param wlxh �����ͺ�
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
	 * ��ѯʡ �� ��
	 */
	public void selectSSQ(String city,String xian){
		if(!city.contains("|")){
			city="";
		}
		if(city.equals("")){
			city="������|��Ͻ��";
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
				sheng="������";
				shi="��Ͻ��";
				 
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}finally{
			DBSql.close(conn, stmt, rset);
		}
		
	}
	/**
	 * ������־
	 * @param name �ļ���
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

