package cn.com.akl.util;

import java.io.IOException;
import java.io.InputStream;

public class RunbatUtil {
	
	/**
	 * ��ͨ��bat�ű�
	 * @param path
	 */
	public static void runbat(final String path){
		new Thread(new Runnable(){
			public void run(){
				try {
					Process process = Runtime.getRuntime().exec("cmd /c \""+path+"\"");
					InputStream inputStream = process.getInputStream();
					byte[] buffer = new byte[1024];
					int length = inputStream.read(buffer);
					if(length != -1)
					System.out.println(new String(buffer, 0, length));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	
	
	/**
	 * ��̵�bat�ű�
	 * @param path
	 * @param daemon
	 */
	public static void runbat2(final String path){
		new Thread(new Runnable(){
			public void run(){
				try {
					Process process = Runtime.getRuntime().exec("cmd /c start "+path);
					InputStream inputStream = process.getInputStream();
					byte[] buffer = new byte[1024];
					int length = inputStream.read(buffer);
					if(length != -1)
					System.out.println(new String(buffer, 0, length));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
}
