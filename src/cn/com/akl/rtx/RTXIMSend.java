package cn.com.akl.rtx;

import java.net.URLEncoder;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.awf.workflow.execute.model.TaskInstanceModel;
import com.actionsoft.eai.im.IMNotifyInterface;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.plugs.email.model.MailTaskModel;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;
/**
 * @description ���༯����RTX��ʱͨѶϵͳ����Ҫʵ���д�������ʱ������Ϣ���Ѳ���,�����notifyTaskMessage������������������
 * @version 1.0
 * @author wangaz
 * @update 2014��4��2�� ����6:19:26
 */
public class RTXIMSend implements IMNotifyInterface {

	public RTXIMSend() {
	}
	public boolean notifyCmsMessage(UserContext sender, String title, String target)
	  {
	    UserModel userModel = (UserModel)UserCache.getModel(target);
	    if (userModel != null) {
	      String c = I18nRes.findValue(sender.getLanguage(),"����һ��[{0}]��������Ϣ��Ѷ", sender.getUserModel().getUserName());
	      String content = c + "[" + title + "]";
	      MessageQueue.getInstance().putMessage(userModel.getUID(), content, true);
	    }
	    return true;
	  }
	@Override
	public boolean notifyMailMessage(String sender, int targetUserId, MailTaskModel mailTaskModel)
	  {
	    UserModel userModel = (UserModel)UserCache.getModel(targetUserId);
	    if (userModel != null) { String threadName = Thread.currentThread().getName();
	      String lang;
	      try { lang = threadName.split("--")[3];
	      }
	      catch (Exception e)
	      {
	        lang = "cn";
	      }
	      	String title = I18nRes.findValue(lang, "����һ�����ʼ�����/you have a new mail") + "-" + mailTaskModel._title;
	      	MessageQueue.getInstance().putMessage(userModel.getUID(), title, true);
	    }
	    return true;
	  }
	@Override
	public boolean notifyTaskMessage(UserContext sender,TaskInstanceModel taskIntanceModel, ProcessInstanceModel processInstanceModel) {
				//�Լ������Լ���������
				if(!taskIntanceModel.getTarget().equals(sender.getUserModel().getUID())){
					String user = taskIntanceModel.getTarget();
					String getUserMailSql="select * from orguser where userid='"+user+"'";
					String usermail=DBSql.getString(getUserMailSql,"EMAIL");
					System.out.println("�������˻�="+user+"�������˵����䣺 "+usermail);
					//=====================
					
					String IPname="10.10.10.153:8012";
					//=====================
					try {
						String addressUrl=AWFConfig._awfServerConf.getPortalHost()+WorkflowTaskInstanceAPI.getInstance().createPublicExecuteUrl(taskIntanceModel.getId());
						//������Ϣ���ѽӿڣ�����Ϊ�����û��������⡢���ݡ�����
						String s = "[MyIM:"+taskIntanceModel.getTarget()+"]��һ��["+ sender.getUserModel().getUserName() + "]�����ı���Ϊ��"+taskIntanceModel.getTitle()+"���Ĵ���������������������"+"|"+addressUrl;
						String ss = s.replace("[", "(");
						String sss = ss.replace("]", ")");
						String str2 = "[MyIM:"+taskIntanceModel.getTarget()+"]��һ��["+ sender.getUserModel().getUserName() + "]�����ı���Ϊ"+taskIntanceModel.getTitle()+"�Ĵ�������-";
						String str3 = str2.replace("[", "(");
						String str4 = "["+sss+"]";
						String str5 = URLEncoder.encode(str4);
						String str6 = "������������";
						//   coco_sendnotify.php    sendnotify.cgi
						String url2 = "http://"+IPname+"/sendnotify.cgi?title="+URLEncoder.encode(str6)+"&msg="+str5+"&receiver="+user+""; 
		//				String url2 = "http://"+IPname+"/coco_sendnotify.php?title="+URLEncoder.encode(str6)+"&msg="+str5+"&receiver="+usermail+""; 
						HttpClient ht = new HttpClient(); 
							GetMethod gt = new GetMethod(url2);
							ht.executeMethod(gt);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("�����쳣="+e.getMessage());
					} 
			}
			return true;
	}
}
