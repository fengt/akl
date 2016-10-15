/**
 * @Probject Name: COCO
 * @Path: cn.com.coco.utilWorkflowTaskUtil.java
 * @Create By hongliang.gao
 * @Create In 2014-4-30 ����2:53:29
 * TODO
 */
package cn.com.akl.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

/**
 * ���������񹤾���
 * @Class Name WorkflowTaskUtil
 * @Author zhangran
 * @Create In 2014-4-30
 */
public class WorkflowTaskUtil {

    /**
     * 
     */
    public WorkflowTaskUtil() {
        // TODO Auto-generated constructor stub
    }

    /**
     * ��������ID ��ȡ���̵��˹���˲˵�
     * @Methods Name getAuditMenusChecked
     * @Create In 2014-5-13 By zhangran
     * @param taskId
     * @return
     */
    public static String getAuditMenusChecked(int taskId){
        String audit_name = "";
        String menus = null;
        try {
            menus = WorkflowTaskInstanceAPI.getInstance().getAuditMenus(taskId);
            JSONArray arr = JSONArray.fromObject(menus);
            for (int i = 0; i < arr.size(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if(obj.getBoolean("isChecked")){  // �Ƿ�ѡ��
                    audit_name = obj.getString("menuName");
                    break;
                }
            }
      } catch (AWSSDKException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return audit_name;
    }
}
