package cn.com.akl.ccgl.cgrk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import cn.com.akl.util.StrUtil;


public class GetEmailContentUtil {

	public static String GetEmailModel(String emailContent) throws IOException {
		
		/**��ȡHtml��**/
		BufferedReader bs = HtmlParser();
		String info = bs.readLine();
		StringBuffer sb = new StringBuffer();
		while (info != null) {
			if((info.trim()).equals("<!--�ʼ�֪ͨ���ݿ�ʼ-->")){
				sb.append(emailContent);
				info = bs.readLine();
			}else{
				info = bs.readLine();
				sb.append(StrUtil.returnStr(info));
			}
		}
		System.out.println(sb.toString());
		return sb.toString();
	}
	/**��ȡHtml��**/
	public static BufferedReader HtmlParser() throws IOException{
		String path = System.getProperty("user.dir");
		File file = new File(path + File.separator + "emailModels" + File.separator + "emailModels.html");
		System.out.println(file.getAbsolutePath());
		System.out.println(file.getName());
		FileInputStream ism = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(ism, "utf-8");
		BufferedReader bs = new BufferedReader(isr);
		return bs;
	}
}