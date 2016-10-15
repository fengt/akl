package cn.com.akl.shgl.lcqd;

import java.util.Hashtable;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class JS6 implements IJob{

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		String url="F:/Users/luxiangyu/Desktop/XML1/XML/6(接收)";
		List<String> Filenames=FileOperateDemo.SystemFileName(url);
		for(String name:Filenames){
			cretatEror(name,"6#报文已接收","没有");
			}
		
	}
	/**
	 * 生成日志
	 * @param name 文件名
	 */
	public void cretatEror(String name,String zt,String cwyy){
		 Hashtable<String,String> recordData = new Hashtable<String,String>();
		 recordData.put("WJM", name);
		 recordData.put("RXBM", "");
		 recordData.put("XH", "");
		 recordData.put("SL", "");
		 recordData.put("ZT", zt);
		 recordData.put("hh", "");
		 recordData.put("CWYY", cwyy);
		 try {
			BOInstanceAPI.getInstance().createBOData("BO_AKL_SAP_RZ", recordData, "admin");
		} catch (AWSSDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
