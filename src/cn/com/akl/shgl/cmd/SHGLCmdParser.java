package cn.com.akl.shgl.cmd;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import cn.com.akl.shgl.sx.web.SXDPrinterWeb;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilString;

public class SHGLCmdParser implements BaseSocketCommand {
	private static final String SX_SXD = "SX_SXD_QSDPrinter_";//ËÍÐÞ´«²Î×Ö·û

	@Override
	public boolean executeCommand(UserContext me, Socket myProSocket, OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr, String socketCmd)
			throws Exception {
		myProSocket.getInputStream();

		if (socketCmd.startsWith(SX_SXD)) {
			//ËÍÐÞµ¥
			if (SX_SXD.length() == socketCmd.length()) {
				myOut.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring(SX_SXD.length());
			myOut.write(new SXDPrinterWeb(me).parseHtml(bindid));
		} else {
			return false;
		}
		return true;
	}
}
