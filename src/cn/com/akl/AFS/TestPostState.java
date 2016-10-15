package cn.com.akl.AFS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import cn.com.akl.AFS.TestSendPost;


public class TestPostState {

	
public static void TestPostState() throws IOException {  
        
        //URL url = new URL("http://182.92.195.4:8080/smsweb/httpapi/mo.do");  
        URL url = new URL("http://182.92.195.4:8080/smsweb/httpapi/report.do");  
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
        String resultString = cn.com.akl.AFS.TestSendPost.byteArrayToHexString(results);  
        
        out.write("enterpriseID=15783&loginName=admin&password="+resultString.toLowerCase()); // 向页面传递数据。post的关键所在！  
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
        } catch(Exception ex2){  
            ex2.printStackTrace();  
        } 
    }  

public static void main(String[] args) throws IOException {  
	TestPostState();  
}  

	
}
