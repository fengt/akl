package cn.com.akl.dgrk;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilString;

public class DGRKCmdParser implements BaseSocketCommand {

	@Override
	public boolean executeCommand(UserContext uc, Socket mySocket,
			OutputStreamWriter out, Vector vec, UtilString myStr, String socketCmd)
			throws Exception {
		if(socketCmd.startsWith("CMD_DGYRKPRINTER_")){
			if("CMD_DGYRKPRINTER_".length() == socketCmd.length()){
				out.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring("CMD_DGYRKPRINTER_".length());
//			System.out.println("ÐÐºÅ"+bindid);
			out.write(new DGYRKDPrinterWeb(uc).parseHtml(bindid));
		}else if(socketCmd.startsWith("CMD_DGRKPRINTER_")){
			if("CMD_DGRKPRINTER_".length() == socketCmd.length()){
				out.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring("CMD_DGRKPRINTER_".length());
//			System.out.println("ÐÐºÅ"+bindid);
			out.write(new DGRKDPrinterWeb(uc).parseHtml(bindid));
		}else{
			return false;
		}
		return true;
	}
}
