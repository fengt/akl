package cn.com.akl.util;



public class ShutdownUtil {
	
	public static void main(String[] args) {
		RunbatUtil.runbat("httpd-shutdown.bat");
		RunbatUtil.runbat("shutdown.bat");
	}
}
