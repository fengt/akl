package cn.com.akl.xsgl.xsdd.rtclass;

import java.util.Hashtable;

import cn.com.akl.xsgl.xsdd.biz.ComputeBiz;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

/**
 * �ڶ��ڵ㱣��ǰ�¼�.
 * 
 * @author huangming
 *
 */
public class StepNo2BeforeSave extends WorkFlowStepRTClassA {

	public StepNo2BeforeSave() {
		super();
	}

	public StepNo2BeforeSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("����ǰ̨�ļ���˶�.");
	}

	@Override
	public boolean execute() {
		String tableName = getParameter(PARAMETER_TABLE_NAME).toString();
		Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		if ("BO_AKL_WXB_XSDD_BODY".equals(tableName)) {
			ComputeBiz fillbiz = new ComputeBiz();
			fillbiz.computePOS(hashtable);
			fillbiz.computeFL(hashtable);
			fillbiz.computeChengben(hashtable);
		}
		return true;
	}

}
