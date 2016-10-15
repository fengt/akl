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

import cn.com.akl.u8.cxml.VendorUtil;
import cn.com.akl.u8.filedown.FileDown;
import cn.com.akl.u8.util.InterfaceUtil;
import cn.com.akl.u8.util.Parserxml;

public class SendVendorData {

	public void sendVendor(Hashtable<String, String> head) throws Exception {
		URL url;
		String u8IP = InterfaceUtil.getU8IPAddress();
		Map<String, String> map = InterfaceUtil.getFilePosition();
		String ys = map.get("YS");
		String md = map.get("MD");
		try {
			url = new URL("http://" + u8IP + "/u8eai/import.asp");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// 设置是否从httpUrlConnection读入，默认情况下是true;
			conn.setDoInput(true);
			// 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在http正文内，因此需要设为true,
			// 默认情况下是false;
			conn.setDoOutput(true);
			conn.setAllowUserInteraction(false);
			// Post 请求不能使用缓存
			conn.setUseCaches(false);
			// 设定请求的方法为"POST"，默认是GET
			conn.setRequestMethod("POST");
			// 设定传送的内容类型是可序列化的java对象(如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
			conn.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded");
			conn.setConnectTimeout(3000000);// 设置连接主机超时
			conn.setReadTimeout(3000000);// 设置从主机读取数据超时

			// 向U8发送Reuqest信息
			OutputStream outstream = conn.getOutputStream();
			DataOutputStream dostream = new DataOutputStream(outstream);
			VendorUtil vu = new VendorUtil();
			Map<String, Object> maps = vu.Vendor(head);
			System.out.println(vu.VendorXml(maps));

			String vendxml = vu.VendorXml(maps);
			String yswj = FileDown.fileDown("供应商信息", ys, vendxml);
			dostream.write(vu.VendorXml(maps).getBytes("UTF-8"));

			InputStream is = conn.getInputStream();
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));

			String str = null;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			String responseXml = sb.toString();
			String mdwj = FileDown.fileDown("供应商信息-return", md, responseXml);
			System.out.println("ResponseXML:" + responseXml);

			Hashtable<String, String> ht = new Hashtable<String, String>();

			ht.put("YSWJ", yswj);
			ht.put("MDWJ", mdwj);
			ht.put("BZ", responseXml);
			ht.put("SFCG", Parserxml.getDataFromXml(sb.toString()));
			InterfaceUtil.addDate(ht);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
