package cn.com.akl.wdt.api.test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lowagie.text.pdf.hyphenation.TernaryTree.Iterator;

import cn.com.akl.wdt.api.test.empty.DetailList;
import cn.com.akl.wdt.api.test.empty.ResultDesc;
import cn.com.akl.wdt.api.test.empty.TradeListJ;



public class HttpURLConnectionPost {
	

 /**
  * ��������
 * @throws JSONException 
  * */
 public static String readContentFromPost(int page) throws IOException, JSONException {
	
	 String ul=null;
	 Calendar c = Calendar.getInstance();  
		//��ȡǰһ��
	    c.add(Calendar.DAY_OF_MONTH, -1);  
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");  
	    String startTime=formatter.format(c.getTime());  
	 
	    c.add(Calendar.DAY_OF_MONTH, 1);
	   // c.set(Calendar.HOUR_OF_NIGHT, 0);
	   /* //��Сʱ��0  
	    c.set(Calendar.HOUR_OF_DAY, 0);  
	    //��������0  
	    c.set(Calendar.MINUTE, 0);  
	    //������0  
	    c.set(Calendar.SECOND,0);  
	    //��������0  
	    c.set(Calendar.MILLISECOND, 0);  */
	    //c.set(Calendar.MILLISECOND, -1);
	    String endTime = formatter.format(new Date());	 
	
		//�����ַ���
		 String str="{\"StartTime\":\""+startTime.toString()+"\",\"EndTime\":\""+endTime.toString()+"\",\"TradeStatus\":" +
					"\"over_trade\",\"PageNO\":"+page+",\"PageSize\":40}73ae933f5f5c494819672dda3626a4f3";
		
		String s="{\"StartTime\":\""+startTime.toString()+"\",\"EndTime\":\""+endTime.toString()+"\",\"TradeStatus\":" +
				"\"over_trade\",\"PageNO\":"+page+",\"PageSize\":40}";
	
	

	 //BASE64����
	 String bs=new sun.misc.BASE64Encoder().encode( MD5_Test.MD5(str).getBytes()); //MD5����

	 
	 try {
		 //URLת��
		 ul=URLEncoder.encode(bs,"UTF-8");
	
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
        // Post�����url����get��ͬ���ǲ���Ҫ������
        URL postUrl = new URL("http://api.wangdian.cn/stockapi/interface.php");
        // ������
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
     
        // �����Ƿ���connection�������Ϊ�����post���󣬲���Ҫ����
        // http�����ڣ������Ҫ��Ϊtrue
        connection.setDoOutput(true);
        // Read from the connection. Default is true.
        connection.setDoInput(true);
        // Ĭ���� GET��ʽ
        connection.setRequestMethod("POST");
      
        // Post ������ʹ�û���
        connection.setUseCaches(false);
      
        connection.setInstanceFollowRedirects(true);
      
        // ���ñ������ӵ�Content-type������Ϊapplication/x-www-form-urlencoded��
        // ��˼��������urlencoded�������form�������������ǿ��Կ������Ƕ���������ʹ��URLEncoder.encode
        // ���б���
        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        // ���ӣ���postUrl.openConnection()���˵����ñ���Ҫ��connect֮ǰ��ɣ�
        // Ҫע�����connection.getOutputStream�������Ľ���connect��
        connection.connect();
        DataOutputStream out = new DataOutputStream(connection
                .getOutputStream());
        // The URL-encoded contend
        // ���ģ�����������ʵ��get��URL�� '? '��Ĳ����ַ���һ��
        String content = "Method=" + URLEncoder.encode("QueryTradeByMTime", "UTF-8");
        //String content = "Method=" + URLEncoder.encode("QueryTradeByNO", "UTF-8");//���ݵ��Ų�ѯ
        content +="&SellerID="+URLEncoder.encode("yakun", "UTF-8");
        content +="&InterfaceID="+URLEncoder.encode("yakun_bpm", "UTF-8");
        content +="&Sign="+URLEncoder.encode(ul, "UTF-8");
        content +="&Content="+URLEncoder.encode(s, "UTF-8");
        // DataOutputStream.writeBytes���ַ����е�16λ��unicode�ַ���8λ���ַ���ʽд��������
        out.writeBytes(content);

        out.flush();
        out.close();
        StringBuffer sb=new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
       
        while ((line = reader.readLine()) != null){
        	sb.append(line);
            
        }
     
        reader.close();
        connection.disconnect();
        String result=sb.toString();
        return result;
}

}

