package cn.com.akl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

public class XMLUtil {
	
	public static byte[] parseUfinterface(Hashtable<String, String> hashtable, Vector<Hashtable<String, String>> vector, Map<String, String> mFieldMap, Map<String, String> sFieldMap) throws IOException{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		buffer.write("<ufinterface >".getBytes());
		// sender="33" receiver="u8" roottag="storein" 
		// docid="345821559" proc="Query" codeexchanged="N" 
		// exportneedexch="N" display="入库单" family="库存管理" 
		// timestamp="0x00000000001D47D5"
		
		// 日期要有 - 
		// 计量单位 编号
		// 商品属性中要开批次，才能填写批次号
		// 货位不需要传入到u8
		// 单据日期 <date> 单据日期必须大于启用日期
		
		buffer.write("<storein>".getBytes());
		buffer.write("<head>".getBytes());
		buffer.write(parseHashtableToXML(hashtable, mFieldMap));
		buffer.write("</head>".getBytes());
		buffer.write("<body>".getBytes());
		for (Hashtable<String, String> hashtable2 : vector) {
			buffer.write("<entry>".getBytes());
			buffer.write(parseHashtableToXML(hashtable2, sFieldMap));
			buffer.write("</entry>".getBytes());
		}
		buffer.write("</body>".getBytes());
		buffer.write("</storein>".getBytes());
		buffer.write("<ufinterface>".getBytes());
		return buffer.toByteArray();
	}
	
	/**
	 * 将记录转换成InputStream
	 * @param hashtable
	 * @param mapField
	 * @return
	 */
	public static byte[] parseHashtableToXML(Hashtable<String, String> hashtable, Map<String, String> mapField){
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(outputStream);
		
		Set<Entry<String, String>> entrySet = hashtable.entrySet();
		for (Entry<String, String> entry : entrySet) {
			StringBuilder sb = new StringBuilder();
			sb.append("<").append(entry.getKey()).append(">");
			sb.append("</").append(entry.getValue()).append(">");
			writer.write(sb.toString());
		}
		
		writer.flush();
		return outputStream.toByteArray();
	}
	
	/**
	 * 转换数据库原始字段成映射字段
	 * @param hashtable 数据库原始Hashtable
	 * @param mapField 映射字段
	 * @return
	 */
	public static Hashtable<String, String> parseMap(Hashtable<String, String> hashtable, Map<String, String> mapField){
		Hashtable<String, String> parser = new Hashtable<String, String>();
		Set<Entry<String, String>> entrySet = hashtable.entrySet();
		for (Entry<String, String> entry : entrySet) {
			String value = hashtable.get(entry.getKey());
			String key = mapField.get(entry.getKey());
			if(key == null)
				parser.put(entry.getKey(), value);
			else
				parser.put(key, value);
		}
		return parser;
	}
	
	/**
	 * 将集合转换成byte数组
	 * @param vector
	 * @param mapField
	 * @return
	 * @throws IOException 
	 */
	public static byte[] parseVectorToXML(Vector<Hashtable<String, String>> vector, Map<String, String> mapField) throws IOException{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		for (Hashtable<String, String> hashtable : vector) {
			outputStream.write(parseHashtableToXML(hashtable, mapField));
		}
		return 	outputStream.toByteArray();
	}

}
