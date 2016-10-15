package cn.com.akl.xsgl.khjxc.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.schedule.IJob;

public class UpdateCKZTSchedule implements IJob {

	/**
	 * 1、定时读取财务系统中的状态，更新出库表中的“出库状态（已签收、已开票）、收款状态（未收款|已收款）”
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
	}

}
