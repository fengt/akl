package cn.com.akl.u8.senddata;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.com.akl.u8.cxml.StoreinUtil;

public class SendStoreinData {

	 /**
	   * @DateTime:     Aug 1, 20145:12:12 PM
	   * @Author:      Administrator
	   * @Title: main
	   * @Description: TODO(������һ�仰�����������������)
	   * @param: @param args   
	   * @return: void   
	   * @throws
	   */
	public static void main(String[] args)
{
    URL url;
    //int instanceID=0; //����BindID
    //String OKorNot=null;
   // String U8IP=getU8IPAddress(); //�ӻ�������ά���л�ȡU8IP��ַ
		try {
			url = new URL("http://10.10.10.201/u8eai/import.asp");
			//url = new URL("http://"+U8IP+"/u8eai/import.asp");
			  HttpURLConnection con = (HttpURLConnection)url.openConnection();
	           con.setConnectTimeout(3000000);
	           con.setReadTimeout(3000000);
	           con.setDoInput(true);
	           con.setDoOutput(true);
	           con.setAllowUserInteraction(false);
	           con.setUseCaches(false);
	           con.setRequestMethod("POST");
	           con.setRequestProperty("Content-type","application/x-www-form-urlencoded");
		 //��U8����Request��Ϣ
		 OutputStream out = con.getOutputStream();
		 DataOutputStream dos = new DataOutputStream(out);
		// String sql="SELECT * FROM BO_UNIS_VOUCHER";
//		 			+"BINDID="+instanceID
		 //Vector<Hashtable<String,String>> vLn=queryAsVector(sql);
		// Hashtable<String,String> vHD= queryAsHashtable(sql);
		 dos.write(StoreinUtil.storeinXML().getBytes("UTF-8"));
		//U8������Ϣ
		 InputStream in =con.getInputStream();
		 BufferedReader br = new BufferedReader(new InputStreamReader(in,"utf-8"));
		 StringBuilder sb = new StringBuilder();
		 String s = null;
		 while ((s = br.readLine()) != null) {
			sb.append(s);
		 }
		 String responseXml = sb.toString();
		 System.out.println("ResponseXml:  "+responseXml);

		// OKorNot=UpdateVoucherLine(instanceID,responseXml); // ��ƾ֤��Ϣ����BPM��ƾ֤��ϸ
//		 if (OKorNot.equals("S")==false){
//			 System.out.println(OKorNot);
//		 }else{
//			 System.out.println("ƾ֤�ɹ�����U8��");
//		 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 }
}
