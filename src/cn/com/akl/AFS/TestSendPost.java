package cn.com.akl.AFS;

import java.io.BufferedReader;  
import java.io.IOException;  
import java.io.InputStream;  
import java.io.InputStreamReader;  
import java.io.OutputStreamWriter;  
import java.net.URL;  
import java.net.URLConnection; 
import java.security.MessageDigest;  
public class TestSendPost {

	public static void TestSendPost() throws IOException {  
        
        URL url = new URL("http://182.92.195.4:8080/smsweb/httpapi/send.do");  
        URLConnection connection = url.openConnection();  
        connection.setDoOutput(true);  
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
        String originString="2jk61e";
        try{  
      //创建具有指定算法名称的信息摘要  
        MessageDigest md = MessageDigest.getInstance("MD5");  
        //使用指定的字节数组对摘要进行最后更新，然后完成摘要计算  
        
        byte[] results = md.digest(originString.getBytes());  
        //将得到的字节数组变成字符串返回  
        String resultString = byteArrayToHexString(results);  
        
        out.write("enterpriseID=15783&loginName=admin&password="+resultString.toLowerCase()+"&mobiles=18510640086&content=【亚昆】尊敬的会员，您所查询的产品为正品"); // 向页面传递数据。post的关键所在！  
        out.flush();  
        out.close();  
        // 一旦发送成功，用以下方法就可以得到服务器的回应：  
        String sCurrentLine;  
        String sTotalString;  
        sCurrentLine = "";  
        sTotalString = "";  
        InputStream l_urlStream;  
        l_urlStream = connection.getInputStream();  
        // 传说中的三层包装阿！  
        BufferedReader l_reader = new BufferedReader(new InputStreamReader(  
                l_urlStream));  
        while ((sCurrentLine = l_reader.readLine()) != null) {  
            sTotalString += sCurrentLine + "\r\n";  
  
        }  
        System.out.println(sTotalString);  
        } catch(Exception ex){  
            ex.printStackTrace();  
        } 
    }  
  
	/**  
     * 转换字节数组为十六进制字符串 
     * @param     字节数组 
     * @return    十六进制字符串 
     */  
	public static String byteArrayToHexString(byte[] b){  
        StringBuffer resultSb = new StringBuffer();  
        for (int i = 0; i < b.length; i++){  
            resultSb.append(byteToHexString(b[i]));  
        }  
        return resultSb.toString();  
    }  
	
    /** 将一个字节转化成十六进制形式的字符串     */  
	public static String byteToHexString(byte b){  
        int n = b;  
        if (n < 0)  
            n = 256 + n;  
        int d1 = n / 16;  
        int d2 = n % 16;  
        return hexDigits[d1] + hexDigits[d2];  
    }  
	
  //十六进制下数字到字符的映射数组  
	public final static String[] hexDigits = {"0", "1", "2", "3", "4",  
        "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"}; 
    
    
    public static void main(String[] args) throws IOException {  
    	TestSendPost();  
    }  
	
	
}
