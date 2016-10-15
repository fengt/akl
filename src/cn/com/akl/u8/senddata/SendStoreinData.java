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
	   * @Description: TODO(这里用一句话描述这个方法的作用)
	   * @param: @param args   
	   * @return: void   
	   * @throws
	   */
	public static void main(String[] args)
{
    URL url;
    //int instanceID=0; //流程BindID
    //String OKorNot=null;
   // String U8IP=getU8IPAddress(); //从基础数据维护中获取U8IP地址
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
		 //向U8发送Request消息
		 OutputStream out = con.getOutputStream();
		 DataOutputStream dos = new DataOutputStream(out);
		// String sql="SELECT * FROM BO_UNIS_VOUCHER";
//		 			+"BINDID="+instanceID
		 //Vector<Hashtable<String,String>> vLn=queryAsVector(sql);
		// Hashtable<String,String> vHD= queryAsHashtable(sql);
		 dos.write(StoreinUtil.storeinXML().getBytes("UTF-8"));
		//U8返回消息
		 InputStream in =con.getInputStream();
		 BufferedReader br = new BufferedReader(new InputStreamReader(in,"utf-8"));
		 StringBuilder sb = new StringBuilder();
		 String s = null;
		 while ((s = br.readLine()) != null) {
			sb.append(s);
		 }
		 String responseXml = sb.toString();
		 System.out.println("ResponseXml:  "+responseXml);

		// OKorNot=UpdateVoucherLine(instanceID,responseXml); // 将凭证信息反填BPM的凭证明细
//		 if (OKorNot.equals("S")==false){
//			 System.out.println(OKorNot);
//		 }else{
//			 System.out.println("凭证成功传入U8！");
//		 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 }
}
