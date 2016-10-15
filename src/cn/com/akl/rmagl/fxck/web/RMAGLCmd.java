package cn.com.akl.rmagl.fxck.web;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilString;

public class RMAGLCmd implements BaseSocketCommand {

	private static final String RMA_FXCK_FX = "CMD_RMA_FXCK_FXPrinter_";
	private static final String RMA_FXCK_TH = "CMD_RMA_FXCK_THPrinter_";

	@Override
	public boolean executeCommand(UserContext me, Socket myProSocket, OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr, String socketCmd)
			throws Exception {
		if (socketCmd.startsWith(RMA_FXCK_FX)) {
			if (RMA_FXCK_FX.length() == socketCmd.length()) {
				myOut.write("NO BINDID");
				return true;
			}
			String bindidStr = socketCmd.substring(RMA_FXCK_FX.length());
			int bindid = Integer.parseInt(bindidStr);
			myOut.write(new RMAFXCKWeb(me).parseHtml(bindid));
			return true;
		} else if (socketCmd.startsWith(RMA_FXCK_TH)) {
			if (RMA_FXCK_TH.length() == socketCmd.length()) {
				myOut.write("NO BINDID");
				return true;
			}
			String bindidStr = socketCmd.substring(RMA_FXCK_TH.length());
			int bindid = Integer.parseInt(bindidStr);
			myOut.write(new RMAFXTHWeb(me).parseHtml(bindid));
			return true;
		} else {
			return false;
		}
		
	}

}
