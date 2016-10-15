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
  * 请求数据
 * @throws JSONException 
  * */
 public static String readContentFromPost(int page) throws IOException, JSONException {
	
	 String ul=null;
	 Calendar c = Calendar.getInstance();  
		//获取前一天
	    c.add(Calendar.DAY_OF_MONTH, -1);  
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");  
	    String startTime=formatter.format(c.getTime());  
	 
	    c.add(Calendar.DAY_OF_MONTH, 1);
	   // c.set(Calendar.HOUR_OF_NIGHT, 0);
	   /* //将小时至0  
	    c.set(Calendar.HOUR_OF_DAY, 0);  
	    //将分钟至0  
	    c.set(Calendar.MINUTE, 0);  
	    //将秒至0  
	    c.set(Calendar.SECOND,0);  
	    //将毫秒至0  
	    c.set(Calendar.MILLISECOND, 0);  */
	    //c.set(Calendar.MILLISECOND, -1);
	    String endTime = formatter.format(new Date());	 
	
		//请求字符串
		 String str="{\"StartTime\":\""+startTime.toString()+"\",\"EndTime\":\""+endTime.toString()+"\",\"TradeStatus\":" +
					"\"over_trade\",\"PageNO\":"+page+",\"PageSize\":40}73ae933f5f5c494819672dda3626a4f3";
		
		String s="{\"StartTime\":\""+startTime.toString()+"\",\"EndTime\":\""+endTime.toString()+"\",\"TradeStatus\":" +
				"\"over_trade\",\"PageNO\":"+page+",\"PageSize\":40}";
	
	

	 //BASE64加密
	 String bs=new sun.misc.BASE64Encoder().encode( MD5_Test.MD5(str).getBytes()); //MD5加密

	 
	 try {
		 //URL转码
		 ul=URLEncoder.encode(bs,"UTF-8");
	
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
        // Post请求的url，与get不同的是不需要带参数
        URL postUrl = new URL("http://api.wangdian.cn/stockapi/interface.php");
        // 打开连接
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
     
        // 设置是否向connection输出，因为这个是post请求，参数要放在
        // http正文内，因此需要设为true
        connection.setDoOutput(true);
        // Read from the connection. Default is true.
        connection.setDoInput(true);
        // 默认是 GET方式
        connection.setRequestMethod("POST");
      
        // Post 请求不能使用缓存
        connection.setUseCaches(false);
      
        connection.setInstanceFollowRedirects(true);
      
        // 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
        // 意思是正文是urlencoded编码过的form参数，下面我们可以看到我们对正文内容使用URLEncoder.encode
        // 进行编码
        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
        // 要注意的是connection.getOutputStream会隐含的进行connect。
        connection.connect();
        DataOutputStream out = new DataOutputStream(connection
                .getOutputStream());
        // The URL-encoded contend
        // 正文，正文内容其实跟get的URL中 '? '后的参数字符串一致
        String content = "Method=" + URLEncoder.encode("QueryTradeByMTime", "UTF-8");
        //String content = "Method=" + URLEncoder.encode("QueryTradeByNO", "UTF-8");//根据单号查询
        content +="&SellerID="+URLEncoder.encode("yakun", "UTF-8");
        content +="&InterfaceID="+URLEncoder.encode("yakun_bpm", "UTF-8");
        content +="&Sign="+URLEncoder.encode(ul, "UTF-8");
        content +="&Content="+URLEncoder.encode(s, "UTF-8");
        // DataOutputStream.writeBytes将字符串中的16位的unicode字符以8位的字符形式写到流里面
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

