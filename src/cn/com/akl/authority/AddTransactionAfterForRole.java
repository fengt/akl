package cn.com.akl.authority;


import java.util.Hashtable;
import java.util.List;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.ORGAPI;


public class AddTransactionAfterForRole extends WorkFlowStepRTClassA{
	private UserContext uc;
	public AddTransactionAfterForRole(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("zhangr");
		setDescription("V1.0");
		setDescription("按部门授予角色!");
	}

	@Override
	public boolean execute() {
		String boTableName = "BO_BMJSXZ";
		int processInstanceId = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Hashtable boT = (Hashtable) BOInstanceAPI.getInstance().getBOData(
				boTableName, processInstanceId);
		String bmid = boT.get("BMID") == null ? "" : boT.get("BMID").toString();
		int roleID = Integer.parseInt(boT.get("ROLEID").toString());
		List<String> list = DepartmentUtil.getAllUserid(Integer.parseInt(bmid));
		for (int i = 0; i < list.size(); i++) {
			GiveUserRole roleUtil = new GiveUserRole();
			roleUtil.giveRoleName(roleID, list.get(i));
		}
		// 更新缓存用户数据
		ORGAPI.getInstance().reloadOrgCache("user");
		return false;
	}
}
