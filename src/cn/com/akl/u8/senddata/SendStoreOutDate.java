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

import cn.com.akl.u8.cxml.StoreOutUtil;
import cn.com.akl.u8.filedown.FileDown;
import cn.com.akl.u8.util.InterfaceUtil;
import cn.com.akl.u8.util.Parserxml;

public class SendStoreOutDate {
	public void sendData(Hashtable<String, String> head,
			Vector<Hashtable<String, String>> body) throws Exception {

		URL url;
		String u8IP = InterfaceUtil.getU8IPAddress();
		Map<String, String> mapB = InterfaceUtil.getFilePosition();
		String ys = mapB.get("YS");
		String md = mapB.get("MD");
		try {
			url = new URL("http://" + u8IP + "/u8eai/import.asp");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(3000000);
			con.setReadTimeout(3000000);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setAllowUserInteraction(false);
			con.setUseCaches(false);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded");

			// 向U8发送Request消息
			OutputStream outstream = con.getOutputStream();
			DataOutputStream dotstream = new DataOutputStream(outstream);
			StoreOutUtil sou=new StoreOutUtil();
			Map<String, Object> map = sou.Storeout(head, body);

			// 文件下载
			String storeoutxml = sou.storeinoutXML(map);
			String yswj = FileDown.fileDown("RMA返新", ys, storeoutxml);
			dotstream.write(sou.storeinoutXML(map).getBytes("UTF-8"));

			// 返回信息
			InputStream is = con.getInputStream();
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			String str = null;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}

			String responseXml = sb.toString();
			String mdwj = FileDown.fileDown("RMA返新-return", md, responseXml);
			System.out.println("ResponseXml:  " + responseXml);

			// 添加数据
			Hashtable<String, String> ht = new Hashtable<String, String>();
			ht.put("YSWJ", yswj);
			ht.put("MDWJ", mdwj);
			ht.put("BZ", responseXml);
			ht.put("SFCG", Parserxml.getDataFromXml(sb.toString()));
			InterfaceUtil.addDate(ht);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
