package cn.com.akl.util;

import com.actionsoft.application.server.WFS;

/**
*����AWS BPM Server������� 
*/
public class StartUpUtil {

	public static void main(String[] args){ 
		RunbatUtil.runbat2("httpd-startup.bat");
		WFS.startup(args);
	}
}
