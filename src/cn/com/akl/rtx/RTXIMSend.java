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
 * @description 此类集成了RTX即时通讯系统，主要实现有代办任务时出发消息提醒操作,详见：notifyTaskMessage方法，其它方法不变
 * @version 1.0
 * @author wangaz
 * @update 2014年4月2日 下午6:19:26
 */
public class RTXIMSend implements IMNotifyInterface {

	public RTXIMSend() {
	}
	public boolean notifyCmsMessage(UserContext sender, String title, String target)
	  {
	    UserModel userModel = (UserModel)UserCache.getModel(target);
	    if (userModel != null) {
	      String c = I18nRes.findValue(sender.getLanguage(),"您有一个[{0}]发布的信息资讯", sender.getUserModel().getUserName());
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
	      	String title = I18nRes.findValue(lang, "您有一封新邮件到达/you have a new mail") + "-" + mailTaskModel._title;
	      	MessageQueue.getInstance().putMessage(userModel.getUID(), title, true);
	    }
	    return true;
	  }
	@Override
	public boolean notifyTaskMessage(UserContext sender,TaskInstanceModel taskIntanceModel, ProcessInstanceModel processInstanceModel) {
				//自己发给自己，不提醒
				if(!taskIntanceModel.getTarget().equals(sender.getUserModel().getUID())){
					String user = taskIntanceModel.getTarget();
					String getUserMailSql="select * from orguser where userid='"+user+"'";
					String usermail=DBSql.getString(getUserMailSql,"EMAIL");
					System.out.println("被提醒账户="+user+"被提醒人的邮箱： "+usermail);
					//=====================
					
					String IPname="10.10.10.153:8012";
					//=====================
					try {
						String addressUrl=AWFConfig._awfServerConf.getPortalHost()+WorkflowTaskInstanceAPI.getInstance().createPublicExecuteUrl(taskIntanceModel.getId());
						//调用消息提醒接口，传参为：（用户名、标题、内容、级别）
						String s = "[MyIM:"+taskIntanceModel.getTarget()+"]有一个["+ sender.getUserModel().getUserName() + "]发来的标题为“"+taskIntanceModel.getTitle()+"”的待办任务，请点击标题审批。"+"|"+addressUrl;
						String ss = s.replace("[", "(");
						String sss = ss.replace("]", ")");
						String str2 = "[MyIM:"+taskIntanceModel.getTarget()+"]有一个["+ sender.getUserModel().getUserName() + "]发来的标题为"+taskIntanceModel.getTitle()+"的待办任务-";
						String str3 = str2.replace("[", "(");
						String str4 = "["+sss+"]";
						String str5 = URLEncoder.encode(str4);
						String str6 = "待办任务提醒";
						//   coco_sendnotify.php    sendnotify.cgi
						String url2 = "http://"+IPname+"/sendnotify.cgi?title="+URLEncoder.encode(str6)+"&msg="+str5+"&receiver="+user+""; 
		//				String url2 = "http://"+IPname+"/coco_sendnotify.php?title="+URLEncoder.encode(str6)+"&msg="+str5+"&receiver="+usermail+""; 
						HttpClient ht = new HttpClient(); 
							GetMethod gt = new GetMethod(url2);
							ht.executeMethod(gt);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("出现异常="+e.getMessage());
					} 
			}
			return true;
	}
}
