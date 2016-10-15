package cn.com.akl.EDI; 

import java.io.File; 
import java.io.IOException; 
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer; 
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath; 
import javax.xml.xpath.XPathConstants; 
import javax.xml.xpath.XPathExpression; 
import javax.xml.xpath.XPathExpressionException; 
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document; 
import org.w3c.dom.NodeList; 
import org.xml.sax.SAXException;  

public class jaxp {
	
	public static void main(String[] args) 
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerException 
			{  
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();   
		Document doc = parser.parse(new File("D:\\XML\\ZRMAORDERS_20150605105312_052_141850787.xml")); 
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile("//CHANGE_SALESORDER_008/text()");
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);  
		for (int i = 0; i < nodes.getLength(); i++) 
		{    String aa=nodes.item(i).getNodeValue();
		String ab=nodes.item(i).getNodeName();
			System.out.println(nodes.item(i).getNodeValue());  
			}  
	  TransformerFactory xformFactory = TransformerFactory.newInstance(); 
	  Transformer transformer = xformFactory.newTransformer();   
	  transformer.transform(new DOMSource(doc), new StreamResult(new File("D:\\XML\\jdom.xml")));
	  }  
	
	}
	
 	

 