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
      //��������ָ���㷨���Ƶ���ϢժҪ  
        MessageDigest md = MessageDigest.getInstance("MD5");  
        //ʹ��ָ�����ֽ������ժҪ���������£�Ȼ�����ժҪ����  
        
        byte[] results = md.digest(originString.getBytes());  
        //���õ����ֽ��������ַ�������  
        String resultString = cn.com.akl.AFS.TestSendPost.byteArrayToHexString(results);  
        
        out.write("enterpriseID=15783&loginName=admin&password="+resultString.toLowerCase()); // ��ҳ�洫�����ݡ�post�Ĺؼ����ڣ�  
        out.flush();  
        out.close();  
        // һ�����ͳɹ��������·����Ϳ��Եõ��������Ļ�Ӧ��  
        String sCurrentLine;  
        String sTotalString;  
        sCurrentLine = "";  
        sTotalString = "";  
        InputStream l_urlStream;  
        l_urlStream = connection.getInputStream();  
        // ��˵�е������װ����  
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
