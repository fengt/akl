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
		// dom4j��ȡxml  
		File xml = new File("D:\\XML\\jdom.xml");   
		Document doc = new SAXReader().read(xml);   
		// doc = DocumentFactory.getInstance().createDocument();//��ͷ�� ��  
		// dom4j Ԫ�ر���   
		for (Iterator it = doc.getRootElement().elementIterator(); it.hasNext();) 
		{   
			Element el = (Element) it.next();  
			System.out.println(el.getName());
			// Ԫ������   
			System.out.println(el.attributeValue("attr"));// Ԫ������  
			System.out.println(el.getTextTrim());// Ԫ������  
			  
		}
	
		  // dom4j xpath����   
	Element el = (Element) doc.selectSingleNode("//DATAAREA//CHANGE_SALESORDER"); 
	el.setText("D:\\XML\\jdom.xml");  
		  // dom4j���xml   
	OutputFormat format = OutputFormat.createPrettyPrint(); 
	format.setNewlines(true); 
	format.setIndent(true);  
	format.setIndentSize(4); 
	XMLWriter writer = new XMLWriter(new FileWriter(xml), format);  
	writer.write(doc);   writer.close();  
	}
	
	}
	
 	

 