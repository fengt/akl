package cn.com.akl.util;

import com.actionsoft.application.server.WFS;

/**
*启动AWS BPM Server调试入口 
*/
public class StartUpUtil {

	public static void main(String[] args){ 
		RunbatUtil.runbat2("httpd-startup.bat");
		WFS.startup(args);
	}
}
