package cn.com.akl.u8.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.xml.sax.SAXException;

public class Parserxml {


	 
//  public static void main(String[] args) throws Exception{
//  	String Strxml="<?xml version=\"1.0\"?><ufinterface roottag=\"voucheraddreturn\"" +
//  			" docid=\"\" proc=\"add\" sender=\"u8\" receiver=\"701\" request-roottag=\"voucher\">" +
//  			"<item accounting_period=\"7\" voucher_type=\"记\" voucher_id=\"\" entry_id=\"\"" +
//  			" succeed=\"0\" dsc=\"凭证导入成功\" u8accounting_period=\"7\" u8voucher_id=\"18\">" +
//				"</item></ufinterface>";
//      getDataFromXml(Strxml);
//  }
   
	/**
	 * 用于获取U8凭证回执中的信息，参数xmlStr：U8凭证回执XML字符串
	 */
  @SuppressWarnings("unchecked")
public static String getDataFromXml(String xmlStr)
          throws ParserConfigurationException, SAXException, IOException {
  	  Document document;
  	  StringBuffer Sb = new StringBuffer(); 
		try {
			  document = DocumentHelper.parseText(xmlStr);
			  Element root = document.getRootElement();
	    	  List<Element> elements = root.elements();
	    	  for (Iterator<Element> it = elements.iterator(); it.hasNext();) {
		    	   Element element = it.next();
		    	   List<Attribute> attributes = element.attributes();
		    	   for (int i = 0; i < attributes.size(); i++) {
		    	    Attribute attribute = attributes.get(i);
		    	    if(("succeed").equals(attribute.getName())){
		    	    	Sb.append(attribute.getStringValue()).append(" ");	
		    	    }
		    	    if(("dsc").equals(attribute.getName())){
		    	    	Sb.append(attribute.getStringValue()).append(" ");
		    	    }
		    	  }
	    	  }
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("=============="+ Sb.toString());
		return Sb.toString();
  } 

}
