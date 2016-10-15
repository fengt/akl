package cn.com.akl.shgl.sx.rtclass;

import java.sql.Connection;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("���޲�Ʒ��ϢУ�顣");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		
		Connection conn = null;
		try {
			conn = DBSql.open();
			
			int sxCount = DAOUtil.getInt(conn, SXCnt.QUERY_isSXExist, bindid);
			String xmkf = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMKF, bindid));
			if("".equals(xmkf)){
				MessageQueue.getInstance().putMessage(uid, "���ͷ����ġ���ֵΪ�գ�����ϵ����Ա��");
				return false;
			}
			if(sxCount == 0){
				MessageQueue.getInstance().putMessage(uid, "�����ӱ���Ϣ����Ϊ�գ�");
				return false;
			}
			
			String sfdyp = DAOUtil.getString(conn, SXCnt.QUERY_isDYP, bindid);
			int count = DAOUtil.getInt(conn, SXCnt.QUERY_DYP, bindid);
			if(sfdyp.equals(SXCnt.is) && count == 0){
				MessageQueue.getInstance().putMessage(uid, "����Ʒ�ӱ���Ϣ����Ϊ�գ�");
				return false;
			}else if(sfdyp.equals(SXCnt.no) && count != 0){
				MessageQueue.getInstance().putMessage(uid, "����Ʒ�ӱ���¼����Ϣ����ɾ�������");
				return false;
			}
			
			return true;
		} catch (RuntimeException e) {
 			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
