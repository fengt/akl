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
      //��������ָ���㷨���Ƶ���ϢժҪ  
        MessageDigest md = MessageDigest.getInstance("MD5");  
        //ʹ��ָ�����ֽ������ժҪ���������£�Ȼ�����ժҪ����  
        
        byte[] results = md.digest(originString.getBytes());  
        //���õ����ֽ��������ַ�������  
        String resultString = byteArrayToHexString(results);  
        
        out.write("enterpriseID=15783&loginName=admin&password="+resultString.toLowerCase()+"&mobiles=18510640086&content=���������𾴵Ļ�Ա��������ѯ�Ĳ�ƷΪ��Ʒ"); // ��ҳ�洫�����ݡ�post�Ĺؼ����ڣ�  
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
        } catch(Exception ex){  
            ex.printStackTrace();  
        } 
    }  
  
	/**  
     * ת���ֽ�����Ϊʮ�������ַ��� 
     * @param     �ֽ����� 
     * @return    ʮ�������ַ��� 
     */  
	public static String byteArrayToHexString(byte[] b){  
        StringBuffer resultSb = new StringBuffer();  
        for (int i = 0; i < b.length; i++){  
            resultSb.append(byteToHexString(b[i]));  
        }  
        return resultSb.toString();  
    }  
	
    /** ��һ���ֽ�ת����ʮ��������ʽ���ַ���     */  
	public static String byteToHexString(byte b){  
        int n = b;  
        if (n < 0)  
            n = 256 + n;  
        int d1 = n / 16;  
        int d2 = n % 16;  
        return hexDigits[d1] + hexDigits[d2];  
    }  
	
  //ʮ�����������ֵ��ַ���ӳ������  
	public final static String[] hexDigits = {"0", "1", "2", "3", "4",  
        "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"}; 
    
    
    public static void main(String[] args) throws IOException {  
    	TestSendPost();  
    }  
	
	
}
