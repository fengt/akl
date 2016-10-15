package cn.com.akl.dgkgl.cmd;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import cn.com.akl.dgkgl.xsck.web.DGBHDPrinterWeb;
import cn.com.akl.dgkgl.xsck.web.DGCKDExcelPrinterWeb;
import cn.com.akl.dgkgl.xsck.web.DGCKDPrinterWeb;
import cn.com.akl.dgkgl.xsck.web.DGQSDPrinterWeb;
import cn.com.akl.dgkgl.xsck.web.PCDPrinterWeb;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilString;

public class DGXSCKCmdParser implements BaseSocketCommand {
	private static final String DG_XSCK_CKD = "CMD_DGK_XSCK_CKDPrinter_";
	private static final String DG_XSCK_QSD = "CMD_DGK_XSCK_QSDPrinter_";
	private static final String DG_XSCK_BHD = "CMD_DGK_XSCK_BHDPrinter_";
	private static final String DG_XSCK_PCD = "CMD_DGK_XSCK_PCDPrinter_";
	private static final String DG_XSCK_CKD_EXCEL_DOWN = "CMD_DGK_XSCK_EXCEL_DOWN_";
	
	@Override
	public boolean executeCommand(UserContext me, Socket myProSocket,
			OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr, String socketCmd)
			throws Exception {
		if(socketCmd.startsWith(DG_XSCK_CKD)){
			// 网销销售出库备货单
			if(DG_XSCK_CKD.length() == socketCmd.length()){
				 myOut.write("NO BINDID");
				 return true;
			}
			String bindid = socketCmd.substring(DG_XSCK_CKD.length());
			myOut.write(new DGCKDPrinterWeb(me).paserHtml(bindid));
		} else if(socketCmd.startsWith(DG_XSCK_QSD)){
			// 网销销售出库备货单
			if(DG_XSCK_QSD.length() == socketCmd.length()){
				 myOut.write("NO BINDID");
				 return true;
			}
			String bindid = socketCmd.substring(DG_XSCK_QSD.length());
			myOut.write(new DGQSDPrinterWeb(me).paserHtml(bindid));
		} else if(socketCmd.startsWith(DG_XSCK_BHD)){
			// 网销销售出库备货单
			if(DG_XSCK_BHD.length() == socketCmd.length()){
				 myOut.write("NO BINDID");
				 return true;
			}
			String bindid = socketCmd.substring(DG_XSCK_BHD.length());
			myOut.write(new DGBHDPrinterWeb(me).paserHtml(bindid));
		} else if(socketCmd.startsWith(DG_XSCK_PCD)){
			// 网销销售出库备货单
			if(DG_XSCK_PCD.length() == socketCmd.length()){
				 myOut.write("NO BINDID");
				 return true;
			}
			String bindid = socketCmd.substring(DG_XSCK_PCD.length());
			myOut.write(new PCDPrinterWeb(me).paserHtml(bindid));
		} else if(socketCmd.startsWith(DG_XSCK_CKD_EXCEL_DOWN)){
			// 网销销售出库备货单
			if(DG_XSCK_CKD_EXCEL_DOWN.length() == socketCmd.length()){
				 myOut.write("NO BINDID");
				 return true;
			}
			String bindid = socketCmd.substring(DG_XSCK_CKD_EXCEL_DOWN.length());
			myOut.write(new DGCKDExcelPrinterWeb(me).parseHtml(bindid));
		} else {
			return false;
		}
		
		return true;
	}
	
}
