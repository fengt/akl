package cn.com.akl.EDI; 

import java.io.File; 
import java.io.FileWriter; 
import java.io.IOException;   

import java.util.Iterator; 
import org.dom4j.Document; 
import org.dom4j.DocumentException;
import org.dom4j.Element; 
import org.dom4j.io.OutputFormat; 
import org.dom4j.io.SAXReader; 

import org.dom4j.io.XMLWriter;


public class dom4j {
	
	public static void main(String[] args) 
			throws DocumentException, IOException {   
		// dom4j读取xml  
		File xml = new File("D:\\XML\\jdom.xml");   
		Document doc = new SAXReader().read(xml);   
		// doc = DocumentFactory.getInstance().createDocument();//从头创 建  
		// dom4j 元素遍历   
		for (Iterator it = doc.getRootElement().elementIterator(); it.hasNext();) 
		{   
			Element el = (Element) it.next();  
			System.out.println(el.getName());
			// 元素名称   
			System.out.println(el.attributeValue("attr"));// 元素属性  
			System.out.println(el.getTextTrim());// 元素内容  
			  
		}
	
		  // dom4j xpath查找   
	Element el = (Element) doc.selectSingleNode("//DATAAREA//CHANGE_SALESORDER"); 
	el.setText("D:\\XML\\jdom.xml");  
		  // dom4j输出xml   
	OutputFormat format = OutputFormat.createPrettyPrint(); 
	format.setNewlines(true); 
	format.setIndent(true);  
	format.setIndentSize(4); 
	XMLWriter writer = new XMLWriter(new FileWriter(xml), format);  
	writer.write(doc);   writer.close();  
	}
	
	}
	
 	

 