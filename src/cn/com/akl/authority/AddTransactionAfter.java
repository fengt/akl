package cn.com.akl.authority;

import java.util.Hashtable;
import java.util.List;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * @ClassName: AddTransactionAfter
 * @Description: 部门权限添加，给部门下的员工账户授权
 * @author zhaijw
 * @date 2014-2-26 下午01:50:12
 */
public class AddTransactionAfter extends WorkFlowStepRTClassA {
	public AddTransactionAfter(UserContext uc) {
		super(uc);
	}

	
	@Override
	public boolean execute() {
		String boTableName = "BO_BMQXXZ";
		int processInstanceId = getParameter(this.PARAMETER_INSTANCE_ID)
				.toInt();
//		int processTaskInstanceId = getParameter(this.PARAMETER_TASK_ID)
//				.toInt();
		Hashtable boT = (Hashtable) BOInstanceAPI.getInstance().getBOData(
				boTableName, processInstanceId);
		String bmid = boT.get("BMID") == null ? "" : boT.get("BMID").toString();
		String qxzmc = boT.get("QXZMC") == null ? "" : boT.get("QXZMC")
				.toString();

		int securityGroupId = DBSql.getInt(
				"select id from sys_securitygroup where groupname='" + qxzmc
						+ "'", "id");
		List<String> list = DepartmentUtil.getAllUserid(Integer.parseInt(bmid));
		for (int i = 0; i < list.size(); i++) {
			GiveUserAuthority gua = new GiveUserAuthority();
			gua.givePower(securityGroupId, list.get(i));
		}
		return false;
	}

}
