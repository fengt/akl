package cn.com.akl.EDI; 

import java.io.File; 
import java.io.FileWriter; 
import java.io.IOException;
import java.util.Iterator; 
import java.util.List; 
import org.jdom.Attribute;
import org.jdom.Document; 
import org.jdom.Element; 
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder; 
import org.jdom.output.Format; 
import org.jdom.output.XMLOutputter; 
import org.jdom.xpath.XPath;  


public class jdom {
	
	public static void main(String[] args) 
			throws JDOMException, IOException 
			{  
		File xml = new File("D:\\XML\\ZRMAORDERS_20150605105312_052_141850787.xml");   
		SAXBuilder sax = new SAXBuilder(); 
		Document doc = sax.build(xml);      
		// jdom属性   
		List attributes = doc.getRootElement().getAttributes();  
		for (int i = 0; i < attributes.size(); i++) 
		{   
Attribute attr = (Attribute) attributes.get(i); 
System.out.println(attr.getName() + ":" + attr.getValue()); 
}  
			
	  // jdom元素  
		List children = doc.getRootElement().getChildren();
		for (int i = 0; i < children.size(); i++) 
		{    
			if (children.get(i) instanceof Element) 
			{    
				Element el = (Element) children.get(i); 
				System.out.println(el.getName());     
				System.out.println(el.getText());   
				}  
			}   
		// jdom查找  
		XPath xpath = XPath.newInstance("//CNTROLAREA/BSR/VERB"); 
		List list = xpath.selectNodes(doc);   
		Iterator iter = list.iterator();   
		while (iter.hasNext()) 
		{  
			Element item = (Element) iter.next();  
			System.err.println(item.getText());//(010)62345678  
		}     
		// jdom输出  
		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat()); 
		FileWriter fw = new FileWriter(xml); 
		xo.output(doc, fw);   fw.close();  
		
		
		}
	
	}
	
 	

 