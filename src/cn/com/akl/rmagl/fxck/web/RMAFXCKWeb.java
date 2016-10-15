package cn.com.akl.rmagl.fxck.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class RMAFXCKWeb extends ActionsoftWeb {

	public RMAFXCKWeb() {
		super();
	}

	public RMAFXCKWeb(UserContext arg0) {
		super(arg0);
	}

	public String parseHtml(int bindid) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			conn = DBSql.open();
			Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAFX_P", bindid);
			Vector<Hashtable<String, String>> boDatas = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFX_S", bindid);
			StringBuilder sb = new StringBuilder();
			int count = 0;
			if (boDatas != null) {
				for (Hashtable<String, String> hashtable : boDatas) {
					String lx = hashtable.get("LX");
					if(!"坏品返新".equals(lx)){
						continue;
					}
					
					// 获取子表数据
					sb.append("<tr class='subtable_tr'>");
					sb.append(PrintUtil.formatBodyRowRecord(hashtable.get("KHDH")));
					sb.append(PrintUtil.formatBodyRowRecord(hashtable.get("THJBM")));
					sb.append(PrintUtil.formatBodyRowRecord(hashtable.get("KHTHSPBM")));
					sb.append(PrintUtil.formatBodyRowRecord(hashtable.get("THXH")));
					sb.append(PrintUtil.formatBodyRowRecord(hashtable.get("FXKHSPBM")));
					sb.append(PrintUtil.formatBodyRowRecord(hashtable.get("FXXH")));
					sb.append(PrintUtil.formatBodyRowRecord(hashtable.get("BZ")));
					sb.append("</tr>");
					count++;
				}
			}
			boData.put("HJ", String.valueOf(count));
			boData.put("SubReport", sb.toString());
			return getHtmlPage("RMA返新打印单据.html", boData);
		} finally {
			DBSql.close(conn, ps, reset);
		}
	}

}
