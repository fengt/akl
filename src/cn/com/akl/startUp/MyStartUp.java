package cn.com.akl.startUp;

import com.actionsoft.application.server.WFS;

/**
*启动AWS BPM Server调试入口 
*/
public class MyStartUp {
	public static void main(String[] args){ 
		WFS.startup(args);
	}
}
