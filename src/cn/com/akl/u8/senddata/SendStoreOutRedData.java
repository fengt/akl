/**
 * 
 */
package cn.com.akl.u8.senddata;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import cn.com.akl.u8.cxml.StoreOutRedUtil;
import cn.com.akl.u8.filedown.FileDown;
import cn.com.akl.u8.util.InterfaceUtil;
import cn.com.akl.u8.util.Parserxml;

/**
 * @author hzy
 *
 */
public class SendStoreOutRedData {

	/**
	 * @param head
	 * @param body
	 * @throws Exception
	 * @author hzy
	 * @desc 向u8发送数据并接收返回值
	 */
	public void sendData(Hashtable<String, String> head,Vector<Hashtable<String, String>> body) throws Exception{
		URL url;
		String u8IP = InterfaceUtil.getU8IPAddress();
		Map<String, String> mapB = InterfaceUtil.getFilePosition();
		String ys = mapB.get("YS");
		String md = mapB.get("MD");
			try {
				url = new URL("http://"+u8IP+"/u8eai/import.asp");
				  HttpURLConnection con = (HttpURLConnection)url.openConnection();
		           con.setConnectTimeout(3000000);
		           con.setReadTimeout(3000000);
		           con.setDoInput(true);
		           con.setDoOutput(true);
		           con.setAllowUserInteraction(false);
		           con.setUseCaches(false);
		           con.setRequestMethod("POST");
		           con.setRequestProperty("Content-type","application/x-www-form-urlencoded");
			 //向U8发送Request消息
			 OutputStream out = con.getOutputStream();
			 DataOutputStream dos = new DataOutputStream(out);
			StoreOutRedUtil soru = new StoreOutRedUtil();
			 Map<String, Object> map = soru.StoreOutRedData(head, body);
			 //文件下载
			 String xml = soru.StoreOutRedXML(map);
			 String yswj = FileDown.fileDown("其他出库-Red",ys,xml);
			 dos.write(xml.getBytes("UTF-8"));
			//U8返回消息
			 InputStream in =con.getInputStream();
			 BufferedReader br = new BufferedReader(new InputStreamReader(in,"utf-8"));
			 StringBuilder sb = new StringBuilder();
			 String s = null;
			 while ((s = br.readLine()) != null) {
				sb.append(s);
			 }
			 String responseXml = sb.toString();
			 String mdwj = FileDown.fileDown("其他出库-Red-return",md, responseXml);
			 System.out.println();
			 System.out.println("ResponseXml:  "+responseXml);
			 //向结果表中添加数据
			 Hashtable<String, String> ht  = new Hashtable<String, String>();
			 ht.put("YSWJ", yswj);
			 ht.put("MDWJ", mdwj);
			 ht.put("SFCG", Parserxml.getDataFromXml(sb.toString()));
			 ht.put("BZ", responseXml);
			 InterfaceUtil.addDate(ht);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
