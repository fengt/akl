package cn.com.akl.kwgl.cmd;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import cn.com.akl.kwgl.dbcrk.web.DbPrinterWeb;
import cn.com.akl.kwgl.kwtz.web.KwtzPrinterWeb;
import cn.com.akl.pdgl.kcpd.rtclass.PdPrinterWeb;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilString;

public class KwglCmdParser implements BaseSocketCommand {

	private static final String KWTZ = "KWTZ_PRINTER_";
	private static final String DBCR = "DB_PRINTER_";
	private static final String KCPD = "CMD_KCPDPRINTER_";
	
	@Override
	public boolean executeCommand(UserContext uc, Socket mySocket,
			OutputStreamWriter out, Vector vec, UtilString myStr, String socketCmd)
			throws Exception {
		if(socketCmd.startsWith(KWTZ)){
			if(KWTZ.length() == socketCmd.length()){
				out.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring(KWTZ.length());
			out.write(new KwtzPrinterWeb(uc).parseHtml(bindid));
		}else if(socketCmd.startsWith(DBCR)){
			if(DBCR.length() == socketCmd.length()){
				out.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring(DBCR.length());
			out.write(new DbPrinterWeb(uc).parseHtml(bindid));
		}else if(socketCmd.startsWith(KCPD)){
			if(KCPD.length() == socketCmd.length()){
				out.write("NO BINDID");
				return true;
			}
			String bindid = socketCmd.substring(KCPD.length());
			out.write(new PdPrinterWeb(uc).parseHtml(bindid));
		}else{
			return false;
		}
		return true;
	}

}
