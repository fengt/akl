package cn.com.akl.ccgl.cmd;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.web.RKDPrinterWeb;
import cn.com.akl.ccgl.wply.web.WPLYPrinter;
import cn.com.akl.ccgl.xsck.web.BHDPrinterWeb;
import cn.com.akl.ccgl.xsck.web.CKDExcelPrinterWeb;
import cn.com.akl.ccgl.xsck.web.CKDPrinterWeb;
import cn.com.akl.ccgl.xsck.web.FXBHDPrinterWeb;
import cn.com.akl.ccgl.xsck.web.FXCKDPrinterWeb;
import cn.com.akl.ccgl.xsck.web.QSDPrinterWeb;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilString;

public class XSCKCmdParser implements BaseSocketCommand {
	private static final String XSCK_QSD = "CMD_XSCK_QSDPrinter_";
	private static final String XSCK_CKD = "CMD_XSCK_CKDPrinter_";
	private static final String FXCK_CKD = "CMD_FXCK_CKDPrinter_";
	private static final String XSCK_BHD = "CMD_XSCK_BHDPrinter_";
	private static final String FXCK_BHD = "CMD_FXCK_BHDPrinter_";
	private static final String CGRK_CKD = "CMD_RKPRINTER_";
	private static final String XSCK_CKD_EXCEL_DOWN = "CMD_XSCK_EXCEL_DOWN_";
	private static final String WPLY = "WPLYPrinter_";//��Ʒ����

	@Override
	public boolean executeCommand(UserContext me, Socket myProSocket, OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr, String socketCmd)
			throws Exception {

		myProSocket.getInputStream();

		if (socketCmd.startsWith(XSCK_QSD)) {
			// �������۳���ǩ�յ�
			if (XSCK_QSD.length() == socketCmd.length()) {
				myOut.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring(XSCK_QSD.length());
			myOut.write(new QSDPrinterWeb(me).paserHtml(bindid));
		} else if (socketCmd.startsWith(XSCK_CKD)) {
			// �������۳�����ⵥ
			if (XSCK_CKD.length() == socketCmd.length()) {
				myOut.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring(XSCK_CKD.length());
			myOut.write(new CKDPrinterWeb(me).paserHtml(bindid));
		} else if (socketCmd.startsWith(FXCK_CKD)) {
			// RMA���³�����ⵥ
			if (FXCK_CKD.length() == socketCmd.length()) {
				myOut.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring(FXCK_CKD.length());
			myOut.write(new FXCKDPrinterWeb(me).paserHtml(bindid));
		} else if (socketCmd.startsWith(XSCK_BHD)) {
			// �������۳��ⱸ����
			if (XSCK_BHD.length() == socketCmd.length()) {
				myOut.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring(XSCK_BHD.length());
			myOut.write(new BHDPrinterWeb(me).paserHtml(bindid));
		} else if (socketCmd.startsWith(FXCK_BHD)) {
			// ���³��ⱸ����
			if (FXCK_BHD.length() == socketCmd.length()) {
				myOut.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring(FXCK_BHD.length());
			myOut.write(new FXBHDPrinterWeb(me).paserHtml(bindid));
		} else if(socketCmd.startsWith(CGRK_CKD)){
			// ������ⵥ
			if(CGRK_CKD.length() == socketCmd.length()){
				myOut.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring(CGRK_CKD.length());
			myOut.write(new RKDPrinterWeb(me).parseHtml(bindid));
		} else if (socketCmd.startsWith(XSCK_CKD_EXCEL_DOWN)) {
			if (XSCK_CKD_EXCEL_DOWN.length() == socketCmd.length()) {
				myOut.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring(XSCK_CKD_EXCEL_DOWN.length());
			myOut.write(new CKDExcelPrinterWeb(me).parseHtml(bindid));
			return true;
		} else if(socketCmd.startsWith(WPLY)){
			if(WPLY.length() == socketCmd.length()){
				myOut.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring(WPLY.length());
			myOut.write(new WPLYPrinter(me).parseHtml(bindid));
			return true;
		}else {
			return false;
		}

		return true;
	}
}
