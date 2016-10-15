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
			// �����Ƿ��httpUrlConnection���룬Ĭ���������true;
			conn.setDoInput(true);
			// �����Ƿ���httpUrlConnection�������Ϊ�����post���󣬲���Ҫ����http�����ڣ������Ҫ��Ϊtrue,
			// Ĭ���������false;
			conn.setDoOutput(true);
			conn.setAllowUserInteraction(false);
			// Post ������ʹ�û���
			conn.setUseCaches(false);
			// �趨����ķ���Ϊ"POST"��Ĭ����GET
			conn.setRequestMethod("POST");
			// �趨���͵����������ǿ����л���java����(����������,�ڴ������л�����ʱ,��WEB����Ĭ�ϵĲ�����������ʱ������java.io.EOFException)
			conn.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded");
			conn.setConnectTimeout(3000000);// ��������������ʱ
			conn.setReadTimeout(3000000);// ���ô�������ȡ���ݳ�ʱ

			// ��U8����Reuqest��Ϣ
			OutputStream outstream = conn.getOutputStream();
			DataOutputStream dostream = new DataOutputStream(outstream);
			VendorUtil vu = new VendorUtil();
			Map<String, Object> maps = vu.Vendor(head);
			System.out.println(vu.VendorXml(maps));

			String vendxml = vu.VendorXml(maps);
			String yswj = FileDown.fileDown("��Ӧ����Ϣ", ys, vendxml);
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
			String mdwj = FileDown.fileDown("��Ӧ����Ϣ-return", md, responseXml);
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
