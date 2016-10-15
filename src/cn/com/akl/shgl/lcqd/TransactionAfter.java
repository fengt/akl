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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
/**
 * ��ȡ�������,����5#����
 * @author luxiangyu
 *
 */
public class TransactionAfter extends WorkFlowStepRTClassA{
	
	public TransactionAfter() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TransactionAfter(UserContext arg0) {
		super(arg0);
		setProvider("³����");
		setVersion("1.0.0");
		setDescription("��ȡ�������,����5#����");
	}

	@Override
	public boolean execute() {
		//����ʵ��id
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		//��ѯ��������Ч�Ĳ���
		seletYxCy(bindid);
		
		return false;
	}

	private void seletYxCy(int bindid) {
		Connection conn=DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset=DBSql.executeQuery(conn, stmt, "select * from BO_AKL_JF_CYSP_S where CYSFZC='��������' and bindid='"+bindid+"'");
			while(rset.next()){
				
				String sjjfsl=rset.getString("SJJFSL");//ʵ�ʽ�������
				String sjjfxh=rset.getString("SJJFXH");//ʵ�ʽ����ͺ�
				String rx=rset.getString("SQBM");//��Ȩ����
				System.out.println("����5"+sjjfxh+sjjfsl);
				//��������,ͨ���Ĳ���,����5#����
				createXML(sjjfsl,sjjfxh,rx);
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DBSql.close(conn,stmt,rset);
		}
	}
    /**
     * ����5#����
     * @param sjjfsl
     * @param sjjfxh
     * @param sxdh
     */
	private void createXML(String sjjfsl, String sjjfxh, String rx) {
		Calendar calendar = Calendar.getInstance();
        Integer y=calendar.get(Calendar.YEAR);//��
        Integer m=calendar.get(Calendar.MONTH)+1;//��
        Integer d=calendar.get(Calendar.DATE);//��
        Integer h=calendar.get(Calendar.HOUR_OF_DAY);//ʱ
        Integer f=calendar.get(Calendar.MINUTE);//��
	    Integer s=calendar.get(Calendar.SECOND);//��
	    SimpleDateFormat dateFm = new SimpleDateFormat("yyyyMMddHHmmSS"); //��ʽ����ǰϵͳ����
	    String dateTime = dateFm.format(new java.util.Date());
	    
	    String url="/\\10.10.10.70/SendCS/5/ZORDCHG_V2_"+dateTime+".xml";//����·��
		
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("CHANGE_SALESORDER_008");//3
			Element cn =root.addElement("CNTROLAREA");//4/29
			
			Element bsr =cn.addElement("BSR");//5/9
			bsr.addElement("VERB").addText("SALESORDER");//6
			bsr.addElement("NOUN").addText("ORDER");//7
			bsr.addElement("REVISION").addText("008");//8
			
			Element sender=cn.addElement("SENDER");//10/19
			sender.addElement("LOGICALID").addText("M030");//11
			sender.addElement("COMPONENT").addText("M030");//12
			sender.addElement("TASK").addText("GIS");//13
			sender.addElement("REFERENCEID").addText("142730513");//14
			sender.addElement("CONFIRMATION").addText("0");//15
			sender.addElement("LANGUAGE").addText("EN");//16
			sender.addElement("CODEPAGE").addText("iso8859-1");//17
			sender.addElement("AUTHID").addText("GIS");//18
			
			Element dt=cn.addElement("DATETIME").addAttribute("qualifier", "CREATION");//20/28
			dt.addElement("YEAR").addText(y.toString());//21
			dt.addElement("MONTH").addText(m.toString());//22
			dt.addElement("DAY").addText(d.toString());//23
			dt.addElement("HOUR").addText(h.toString());//24
			dt.addElement("MINUTE").addText(f.toString());//25
			dt.addElement("SECOND").addText(s.toString());//26
			dt.addElement("TIMEZONE").addText("+0800");//27
			
	
			Element da=root.addElement("DATAAREA");//30/125
			Element cs=da.addElement("CHANGE_SALESORDER");//31/124
			Element soheader=cs.addElement("SOHEADER");//32/87
		    soheader.addElement("SALESORDID").addText(rx);//33��Ȩ����
			soheader.addElement("SALESORG").addText("M030");//34
			String rxid=DBSql.getString("select RXBMID from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"'", "RXBMID");
			
			soheader.addElement("NOTES").addAttribute("index","1").addAttribute("description","External Order/Shipper ID").addText(rxid);//35
		    soheader.addElement("NOTES").addAttribute("index", "2").addAttribute("description", "Priority Code").addText(" ");//
			soheader.addElement("NOTES").addAttribute("index", "3").addAttribute("description", "Label Type").addText(" ");
			soheader.addElement("NOTES").addAttribute("index", "4").addAttribute("description", "Department Number").addText(" ");
			soheader.addElement("NOTES").addAttribute("index", "5").addAttribute("description", "Pack Information").addText(" ");
			soheader.addElement("NOTES").addAttribute("index", "6").addAttribute("description", "Store Number").addText("0000000");//39
			soheader.addElement("NOTES").addAttribute("index", "7").addAttribute("description", "Partial Allowed").addText("yes");//40
			soheader.addElement("NOTES").addAttribute("index", "8").addAttribute("description", "Store Type").addText(" ");
			soheader.addElement("NOTES").addAttribute("index", "9").addAttribute("description", "PO Type").addText(" ");
			soheader.addElement("NOTES").addAttribute("index", "10").addAttribute("description", "Order Type").addText("ZRA");//43
			soheader.addElement("POID").addText("2527209");
			soheader.addElement("SHIPNOTES").addText("SRGA");
			soheader.addElement("SVCLEVEL").addText("STD4");
			soheader.addElement("FOB").addText("DDP");
			soheader.addElement("NAMEDPLACE").addText("Destination");
			soheader.addElement("FRTRMS").addText("PREPAID");
			soheader.addElement("FRACCNUM");
			
			Element partner = soheader.addElement("PARTNER").addAttribute("type", "ShipTo");
			String khmc=DBSql.getString("select KHMC from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"'", "KHMC");
			partner.addElement("NAME").addAttribute("index", "1").addText(khmc);//��д
			partner.addElement("NAME").addAttribute("index", "2").addText("AKL Logistics");
			partner.addElement("CUSTNUM").addText("OT00210");
			Element address = partner.addElement("ADDRESS");
			String dz=DBSql.getString("select DZ from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"'", "DZ");
			address.addElement("ADDRLINE").addAttribute("index", "1").addText(dz);//��ϸ��ַ��д
			address.addElement("ADDRLINE").addAttribute("index", "2").addText(" ");
			address.addElement("ADDRLINE").addAttribute("index","3").addText(" ");
			String city=DBSql.getString("select CITY from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"'", "CITY");
			address.addElement("CITY").addText(city);//ʡ�д�д
			String gj=DBSql.getString("select GJ from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"'", "GJ");
			address.addElement("COUNTRY").addText(gj);//���Ҵ�д
			String yb=DBSql.getString("select YB from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"'", "YB");
			address.addElement("POSTALCODE").addText(yb);//�ʱ��д
			address.addElement("STATEPROVN");
			String sjh=DBSql.getString("select SJH from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"'", "SJH");
			address.addElement("TELEPHONE").addAttribute("index", "1").addText(sjh);//�绰��д
			
			
			Element partner2 = soheader.addElement("PARTNER").addAttribute("type", "BillTo");
			partner2.addElement("NAME").addAttribute("index", "1").addText(khmc);//
			partner2.addElement("NAME").addAttribute("index", "2").addText("AKL Logistics");
			partner2.addElement("CUSTNUM").addText("OT00210");
			Element address2 = partner2.addElement("ADDRESS");
			address2.addElement("ADDRLINE").addAttribute("index", "1").addText(dz);
			address2.addElement("ADDRLINE").addAttribute("index", "2").addText("AKL logistics");
			address2.addElement("ADDRLINE").addAttribute("index","3");
			address2.addElement("CITY").addText(city);
			address2.addElement("COUNTRY").addText(gj);
			address2.addElement("POSTALCODE").addText(yb);
			address2.addElement("STATEPROVN");
			address2.addElement("TELEPHONE").addAttribute("index", "1").addText(sjh);
			
			Element soterms = soheader.addElement("SOTERMS");
			soterms.addElement("TERMID").addText("No Charge");
			soterms.addElement("VATNUM");
			soterms.addElement("SDVATNUM");
			
			Element soline = cs.addElement("SOLINE");
			Element quantity=soline.addElement("QUANTITY").addAttribute("qualifier", "ORDERED");
			quantity.addElement("VALUE").addText(sjjfsl);//����
			quantity.addElement("UOM").addText("EA");
			 
			soline.addElement("SOLINENUM").addText("");//�к�
			soline.addElement("NOTES").addAttribute("index", "1").addAttribute("description","Required Ship Date").addText(dateTime);
			soline.addElement("NOTES").addAttribute("index", "2").addAttribute("description", "Customer Item Number").addText(" ");
			soline.addElement("NOTES").addAttribute("index", "3").addAttribute("description", "UPC").addText("619659077730");
			soline.addElement("NOTES").addAttribute("index", "4").addAttribute("description", "Required Delivery Date (Start)").addText(" ");
			soline.addElement("NOTES").addAttribute("index", "5").addAttribute("description", "Required Delivery Date (End)").addText(" ");
			soline.addElement("NOTES").addAttribute("index", "6").addAttribute("description", "Partial Allowed").addText("no");
			soline.addElement("NOTES").addAttribute("index", "7").addAttribute("description", "StdPackQty").addText(" ");
			soline.addElement("NOTES").addAttribute("index", "8").addAttribute("description", "Priority").addText("0");
			soline.addElement("NOTES").addAttribute("index", "9").addAttribute("description", "CustomsDesc").addText("Memory Storage Card");
			soline.addElement("NOTES").addAttribute("index", "10").addAttribute("description", "Tarif Code").addText(" ");
			soline.addElement("NOTES").addAttribute("index", "11").addAttribute("description", "ECCN Code").addText(" ");
		    soline.addElement("NOTES").addAttribute("index", "12").addAttribute("description", "Customer Required Date").addText(dateTime);
			soline.addElement("NOTES").addAttribute("index", "13").addAttribute("description", "Change Revision Number").addText("01");
			soline.addElement("NOTES").addAttribute("index", "14").addAttribute("description", "Line Status").addText("RELEASED");
			soline.addElement("NOTES").addAttribute("index", "15").addAttribute("description", "Line Cancelled").addText("yes");
            soline.addElement("NOTES").addAttribute("index", "16").addAttribute("description", "Line ShipVia").addText("SRGA");
			soline.addElement("NOTES").addAttribute("index", "17").addAttribute("description", "Line Freight Terms").addText(" ");
		    soline.addElement("NOTES").addAttribute("index", "18").addAttribute("description", "Customer Requested Ship Date").addText(dateTime);
			soline.addElement("NOTES").addAttribute("index", "19").addAttribute("description", "Line Service Level").addText("STD4");
			soline.addElement("ITEM").addText(sjjfxh);//�ͺŴ�д
	
			Element charge = soline.addElement("CHARGE");
			Element operamt=charge.addElement("OPERAMT").addAttribute("type", "T").addAttribute("qualifier", "UNIT");
			operamt.addElement("VALUE").addText("15.99");
			operamt.addElement("SIGN").addText("+");
			operamt.addElement("CURRENCY").addText("USD");
			operamt.addElement("UOMVALUE").addText("99.07");
			operamt.addElement("ORDCURRCODE").addText("CNY");
			
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("us-ascii");
			format.setIndent(true);
		 
			XMLWriter writer = new XMLWriter(new FileWriter(new File(url)),format);
			doc.normalize();
			writer.write(doc);
			writer.flush();
			writer.close();
			HelloAWSProcessEventBiz.cretatEror("ZORDCHG_V2_"+y+m+d+h+f+s+".xml","5#���ĳɹ�����","û��");//������־
			//����3#������Ϣ
			updateXML3(sjjfxh,sjjfsl,rx);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    //����3#���Ĳ����Ĳ�Ʒ��Ϣ
	private void updateXML3(String sjjfxh, String sjjfsl, String rx) {
		System.out.println("���ڸ���");
        Connection conn = DBSql.open();
        Statement stmt=null;
        ResultSet rset=null;
        try {
			stmt = conn.createStatement();
			rset=DBSql.executeQuery(conn, stmt, "select * from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"' and ZT='��Ч' and BWLX='3#'");
			while(rset.next()){
				String sqbm=rset.getString("RXBM");
				String xh=rset.getString("XH");
				String sl=rset.getString("SL");
				String sqbmid=rset.getString("RXBMID");
				String hh=rset.getString("HH");
				String khmc=rset.getString("KHMC");
				String gj=rset.getString("GJ");
				String dz=rset.getString("DZ");
				String yb=rset.getString("YB");
				String sjh=rset.getString("SJH");
				String city=rset.getString("CITY");
				System.out.println("���ϸ���"+rx);
				DBSql.executeUpdate(conn,"update BO_AKL_SAP_JF_CPXX set ZT='��Ч' where RXBM='"+rx+"' and BWLX='3#' and ZT='��Ч'");
				Hashtable<String,String> re=new Hashtable<String,String>();
				re.put("RXBM", sqbm);
				re.put("XH", sjjfxh);
				re.put("SL", sjjfsl);
				re.put("RXBMID", sqbmid);
				re.put("HH", hh);
				re.put("KHMC",khmc );
				re.put("GJ", gj);
				re.put("DZ", dz);
				re.put("YB", yb);
				re.put("SJH", sjh);
				re.put("CITY", city);
				re.put("ZT", "��Ч");
				re.put("BWLX", "3#");
				BOInstanceAPI.getInstance().createBOData("BO_AKL_SAP_JF_CPXX", re, "admin");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AWSSDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DBSql.close(conn,stmt,rset);
		}
		
	}

}
