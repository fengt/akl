package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.SalesOrderBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	public StepNo1BeforeSave() {
		super();
	}

	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("第一个节点的校验事件，检测客户订单号重复，并给予提示。");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		String table = getParameter(PARAMETER_TABLE_NAME).toString();

		if (table.equals("BO_AKL_WXB_XSDD_HEAD")) {
			Connection conn = null;
			try {
				conn = DAOUtil.openConnection();

				// 查询与此流程相同的客户采购号
				String khcgdh = hashtable.get("KHCGDH");
				// 未填写客户采购单号不进行判断
				if (khcgdh == null || "".equals(khcgdh)) {
					return true;
				}
				// 查询相似的采购单号
				SalesOrderBiz biz = new SalesOrderBiz();
				int count = biz.getKHCGDDHRepeatCount(conn, bindid, khcgdh);
				if (count > 0) {
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "系统中存在相同的客户采购编号，您仍然可以继续办理。", true);
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			} finally {
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}

}
