/*
 * Copyright(C)2001-2012 Actionsoft Co.,Ltd
 * AWS(Actionsoft workflow suite) BPM(Business Process Management) PLATFORM Source code 
 * AWS is a application middleware for BPM System

  
 * 本软件工程编译的二进制文件及源码版权归北京炎黄盈动科技发展有限责任公司所有，
 * 受中国国家版权局备案及相关法律保护，未经书面法律许可，任何个人或组织都不得泄漏、
 * 传播此源码文件的全部或部分文件，不得对编译文件进行逆向工程，违者必究。

 * $$本源码是炎黄盈动最高保密级别的文件$$
 * 
 * http://www.actionsoft.com.cn
 * 
 */

package com.actionsoft.application.schedule.system;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.application.server.LICENSE;
import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.awf.bo.cache.MetaDataCache;
import com.actionsoft.awf.bo.model.MetaDataModel;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.organization.cache.CompanyCache;
import com.actionsoft.awf.organization.cache.DepartmentCache;
import com.actionsoft.awf.organization.cache.RoleCache;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.CompanyModel;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.organization.model.RoleModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.rule.JumpActivityRuleEngine;
import com.actionsoft.awf.rule.ProcessRuleEngine;
import com.actionsoft.awf.util.ClassReflect;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.awf.workflow.calendar.util.CalWorkTimeImp;
import com.actionsoft.awf.workflow.constant.ActivityDefinitionConst;
import com.actionsoft.awf.workflow.constant.TaskRuntimeConst;
import com.actionsoft.awf.workflow.constant.UserTaskDefinitionConst;
import com.actionsoft.awf.workflow.constant.UserTaskRuntimeConst;
import com.actionsoft.awf.workflow.design.cache.WorkFlowCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCostCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepCostModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.design.util.WFFlexDesignEmailTemplateUtil;
import com.actionsoft.awf.workflow.execute.PriorityType;
import com.actionsoft.awf.workflow.execute.SynType;
import com.actionsoft.awf.workflow.execute.WorkflowException;
import com.actionsoft.awf.workflow.execute.dao.ProcessInstance;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.awf.workflow.execute.dao.TaskInstance;
import com.actionsoft.awf.workflow.execute.engine.WorkflowEngine;
import com.actionsoft.awf.workflow.execute.engine.WorkflowTaskEngine;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.awf.workflow.execute.model.TaskInstanceModel;
import com.actionsoft.awf.workflow.execute.route.impl.RouteAbst;
import com.actionsoft.awf.workflow.execute.route.impl.RouteFactory;
import com.actionsoft.awf.workflow.execute.worklist.web.UserTaskExecuteWeb;
import com.actionsoft.coe.bpa.etl.collector.WorkFlowTaskTimeoutCollectorImp;
import com.actionsoft.eai.shortmessage.SMSContext;
import com.actionsoft.eai.shortmessage.SendSMSUtil;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.loader.core.TaskTimeOutEventA;
import com.actionsoft.plugs.email.util.AWSMailUtil;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.IMAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

/**
 * 1.节点超时策略触发器实现类 2.系统提醒类任务，超期3d自动清除
 * 
 */
public class ProcessTaskCostCalculationJob implements IJob {
	// 是否当前AWS平台存在记录超时策略及扣分所需的BO元数据
	// 若没有就不执行记录的操作
	private static boolean isExsitLogTable = false;

	static {
		MetaDataModel metaDataModel = (MetaDataModel) MetaDataCache.getModel("BO_AWS_RT_COSTLOG");
		if (metaDataModel != null)
			isExsitLogTable = true;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		long beginTime = System.currentTimeMillis();
		executeAction(context);
		long endTime = System.currentTimeMillis();
		System.out.println("信息: [" + UtilDate.datetimeFormat(new Date(beginTime)) + "]AWS Process cost calculation:[" + ((endTime - beginTime) / 1000) + "s]");
	}

	/**
	 * 执行一次任务超时检测,检测频率由定时器配置
	 */
	private void executeAction(JobExecutionContext context) {
		java.sql.Connection conn = DBSql.open();
		java.sql.Statement stmt = null;
		java.sql.ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			Hashtable workingTask = ProcessRuntimeDaoFactory.createTaskInstance().getAllActiveTaskListOfStatus(UserTaskRuntimeConst.STATE_TYPE_TRANSACT);
			for (int i = 0; i < workingTask.size(); i++) {
				TaskInstanceModel taskInstanceModel = (TaskInstanceModel) workingTask.get(new Integer(i));
				// 正常的待办任务
				if (taskInstanceModel.getStatus() == 1) {
					// 获得当前任务的Workflow Instance
					rset = DBSql.executeQuery(conn, stmt, "select * from wf_messagedata where id = "+taskInstanceModel.getProcessInstanceId());
					ProcessInstanceModel processInstanceModel = null;
					if (rset != null) {
						if (rset.next()) {
							processInstanceModel =new ProcessInstance().record2Model(rset);
						}
					}
					if (processInstanceModel == null) {
						System.out.println("警告：ID为[" + taskInstanceModel.getId() + "]标题为[" + taskInstanceModel.getTitle() + "]的任务已丢失流程实例，超时策略未执行!");
						continue;
					}

					// 获得该节点的成本定义列表
					WorkFlowStepModel stepModel = (WorkFlowStepModel) WorkFlowStepCache.getModelOfStepNo(processInstanceModel.getProcessDefinitionId(), processInstanceModel.getActivityDefinitionNo());
					if (stepModel == null) {
						System.out.println("警告：ID为[" + taskInstanceModel.getId() + "]标题为[" + taskInstanceModel.getTitle() + "]的任务所在流程节点模型未找到(bindid=" + processInstanceModel.getId() + ",wfId=" + processInstanceModel.getProcessDefinitionId() + ",wfsNo=" + processInstanceModel.getActivityDefinitionNo() + ")，超时策略未执行!");
						continue;
					}

					// 获取待办人的组织身份信息，为扣分人身份信息作准备
					// 该变量在扣分循环中仅初始化一次，并放入for循环内，目的是
					// 如果该节点未定义限时策略，不会提取这些准备信息，降低性能损耗
					UserModel targetUserModel = null;
					DepartmentModel targetDepartmentModel = null;
					CompanyModel targetCompanyModel = null;
					RoleModel targetRoleModel = null;

					// 获取该节点的成本策略
					Hashtable costList = WorkFlowStepCostCache.getListOfWorkFlowStep(stepModel._id);
					for (int ii = 0; ii < costList.size(); ii++) {
						WorkFlowStepCostModel costModel = (WorkFlowStepCostModel) costList.get(new Integer(ii));
						// 是否执行节点成本策略
						boolean isExcuteCost = false;
						long taskCostNow = 0;
						// 如果计算方式是从阅读任务开始计算，并且还没有阅读任务，忽略
						if (costModel._calcType == 1 && taskInstanceModel.getReadTime() == null) {
							continue;
						}
						// 计算可定频率成本策略 可重复执行
						if (costModel._calcType == 3) {
							Calendar c = Calendar.getInstance();
							c.setTime(new Date(taskInstanceModel.getBeginTime().getTime()));
							taskCostNow = CalWorkTimeImp.getInstanceByUid(taskInstanceModel.getTarget()).calcWorkingTime(c);
							isExcuteCost = !isExcutedCostTrigger(taskInstanceModel, costModel);
						} else {
							// 计算常规超市策略
							// 如果该策略已经在该任务触发过一次，则不再继续执行
							if (DBSql.getInt(conn,"select count(*) as c from BO_AWS_RT_COSTLOG where TASKID=" + taskInstanceModel.getId() + " and COSTID=" + costModel._id, "c") > 0) {
								continue;
							}

							// 节点成本设置时的单位是小时
							double taskCostTimes = costModel._cost;
							if (taskCostTimes == -100d) {// 超过合理期限时
								if (stepModel._duration == 0) {
									System.err.println("时限监控-[警告][合理期限=0][处理结果=忽略执行该策略][Task=" + taskInstanceModel.getTitle() + "]");
									continue;// 未设置期限
								}
								// taskCostTimes =
								// Double.parseDouble(Integer.toString(stepModel._duration))
								// / 1000d / 60d ;// 折合成分钟
								taskCostTimes = Double.parseDouble(Long.toString(stepModel._duration)) / 1000d / 60d / 60d;// 折合成小时
								if (taskCostTimes == 0d) {
									System.err.println("时限监控-[警告][合理期限=数值太小][处理结果=忽略执行该策略][Task=" + taskInstanceModel.getTitle() + "]");
									continue;//	
								}
							} else if (taskCostTimes == -101d) {// 超过宽延期限时
								if (stepModel._durationWarning == 0) {
									System.err.println("时限监控-[警告][宽延期限=0][处理结果=忽略执行该策略][Task=" + taskInstanceModel.getTitle() + "]");
									continue;// 未设置期限
								}
								// taskCostTimes =
								// Double.parseDouble(Integer.toString(stepModel._durationWarning))
								// / 1000d / 60d; // 折合成分钟
								taskCostTimes = Double.parseDouble(Integer.toString(stepModel._durationWarning)) / 1000d / 60d / 60d; // 折合成小时
								if (taskCostTimes == 0d) {
									System.err.println("时限监控-[警告][宽延期限=数值太小][处理结果=忽略执行该策略][Task=" + taskInstanceModel.getTitle() + "]");
									continue;//
								}
							}

							// 当前节点的成本小时
							Calendar c = Calendar.getInstance();
							if (costModel._calcType == 1) {// 从阅读时间计算
								c.setTime(new Date(taskInstanceModel.getReadTime().getTime()));
							} else if (costModel._calcType == 0) {// 从任务到达时间开始计算
								c.setTime(new Date(taskInstanceModel.getBeginTime().getTime()));
							} else if (costModel._calcType == 2) {// 从@公式中提取，必须符合年月日、小时分格式
								String timeValueStr = costModel._bizTime;
								if (timeValueStr.indexOf("@") > -1) {// 如果真的包含@命令
									try {
										UserContext targetContext = new UserContext(taskInstanceModel.getTarget());
										RuntimeFormManager rfm = new RuntimeFormManager(targetContext, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId(), 0, 0);
										timeValueStr = rfm.convertMacrosValue(costModel._bizTime);
									} catch (Exception e) {
										e.printStackTrace(System.err);
									}
								}
								// 将时间串转换成times
								long beginTime = new UtilDate().getTimes(timeValueStr, "yyyy-MM-dd HH:mm:ss");
								if (beginTime == 0) {
									beginTime = new UtilDate().getTimes(timeValueStr, "yyyy-MM-dd");
									if (beginTime == 0) {
										System.err.println("时限监控-[警告][业务参数=" + timeValueStr + "][可能是非合法的yyyy-MM-dd HH:mm:ss格式][处理结果=忽略执行该策略][Task=" + taskInstanceModel.getTitle() + "]");
										continue;// 忽略该策略的执行
									}
								}
								c.setTime(new Date(beginTime));
							}

							// 使用当前办理者的工作日历计算，精确到分钟(calcWorkingTimeOfSecond可精确到秒）
							taskCostNow = CalWorkTimeImp.getInstanceByUid(taskInstanceModel.getTarget()).calcWorkingTime(c);
							isExcuteCost = taskCostNow > taskCostTimes * 60;
						}
						String sysAutoTitle = "无";

						if (isExcuteCost) {
							// 如果设置了业务规则检查点，判断规则表达式是否成立
							String bizRuleStr = costModel._bizRule;
							if (bizRuleStr.indexOf("@") > -1) {// 如果真的包含@命令
								try {
									UserContext targetContext = new UserContext(taskInstanceModel.getTarget());
									RuntimeFormManager rfm = new RuntimeFormManager(targetContext, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId(), 0, 0);
									bizRuleStr = rfm.convertMacrosValue(costModel._bizRule);
								} catch (Exception e) {
									e.printStackTrace(System.err);
								}
							}
							if (bizRuleStr.trim().length() > 0) {
								bizRuleStr = bizRuleStr.trim().toLowerCase();
								if (bizRuleStr.equals("true") || bizRuleStr.equals("yes") || bizRuleStr.equals("是") || bizRuleStr.equals("1") || bizRuleStr.equals("on")) {
									// 条件成立
								} else {
									System.err.println("时限监控-[信息][业务条件=FALSE][处理结果=忽略执行该策略][Task=" + taskInstanceModel.getTitle() + "]");
									continue;// 不符合业务条件，忽略该策略的执行
								}
							}

							// 再次校验下当前任务是否已经被用户执行了
							// 这个校验是考虑到成本计算需要运行一段时间，可能恰巧轮到计算该任务时，这个任务已经被用户处理完毕了
							// 如果上述条件发生，则忽略成本计算
							TaskInstanceModel tmpModel = null;
							rset = DBSql.executeQuery(conn, stmt, "SELECT * FROM WF_TASK WHERE ID=" + taskInstanceModel.getId());
							if (rset != null) {
								if (rset.next()) {
									tmpModel =new TaskInstance().record2TaskModel(rset);
								}
							}
							if (tmpModel == null)
								continue;
							// 将超时信息记录到BO_AWS_RT_COSTLOG日志
							int timeOutLogBoId = 0;
							int timeOutLogWorkflowInstanceId = 0;
							if (isExsitLogTable) {
								if (targetUserModel == null)
									targetUserModel = (UserModel) UserCache.getModel(taskInstanceModel.getTarget());
								if (targetUserModel == null)
									continue;
								if (targetDepartmentModel == null)
									targetDepartmentModel = (DepartmentModel) DepartmentCache.getModel(targetUserModel.getDepartmentId());
								if (targetDepartmentModel == null)
									continue;
								if (targetCompanyModel == null)
									targetCompanyModel = (CompanyModel) CompanyCache.getModel(targetDepartmentModel.getCompanyId());
								if (targetCompanyModel == null)
									continue;
								if (targetRoleModel == null)
									targetRoleModel = (RoleModel) RoleCache.getModel(targetUserModel.getRoleId());
								if (targetRoleModel == null)
									continue;

								if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_TASK_MAIL_INNER) { // 给当任务起草者发送电子邮件
									sysAutoTitle = "发送内网邮件通知";
								} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_TASK_MAIL_OUTTER) { // 给当前办理者发送电子邮件
									sysAutoTitle = "发送外网邮件通知";
								} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_AUTO_TASK) { // 自动执行该任务
									sysAutoTitle = "自动向下执行";
								} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_ROLLBACK) { // 自动将该任务回退给上个节点
									sysAutoTitle = "将该任务退回";
								} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_SHORT_MESSAGE) { // 自动发送催办短信
									sysAutoTitle = "给当任务办理者发送了催办短信";
								} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_NORMAL) { // 什么都不做
								} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_CLAZZ) {
									sysAutoTitle = "自定义的超时动作，未知";
								}

								try {
									WorkFlowModel flowModel = (WorkFlowModel) WorkFlowCache.getModel(WorkflowEngine.getInstance().getWorkflowDefId("ff85be74cca5a04c52d9249c0778b446"));
									if (flowModel._workFlowType == 1) {// 仅存储
										timeOutLogWorkflowInstanceId = WorkflowInstanceAPI.getInstance().createBOInstance("ff85be74cca5a04c52d9249c0778b446", "admin", "[" + targetUserModel.getUserName() + "]在[" + UtilDate.datetimeFormat(new Date()) + "]"+I18nRes.findValue("的流程绩效超时记录"));
									} else {// 流程
										timeOutLogWorkflowInstanceId = WorkflowInstanceAPI.getInstance().createProcessInstance("ff85be74cca5a04c52d9249c0778b446", "admin", "[" + targetUserModel.getUserName() + "]在[" + UtilDate.datetimeFormat(new Date()) + "]"+I18nRes.findValue("的流程绩效超时记录"));
										int timeOutLogTaskInstanceId = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance("admin", timeOutLogWorkflowInstanceId, 0, 1, 1, "admin", "[" + targetUserModel.getUserName() + "]在[" + UtilDate.datetimeFormat(new Date()) + "]"+I18nRes.findValue("的流程绩效超时记录"), false, 0)[0];
									}
									if (timeOutLogWorkflowInstanceId > 0) {
										Hashtable rowData = new com.actionsoft.awf.util.UnsyncHashtable();
										rowData.put("YEAR", Integer.toString(UtilDate.getYear(new Date())));
										rowData.put("MONTH", Integer.toString(UtilDate.getMonth(new Date())));
										rowData.put("DAY", Integer.toString(UtilDate.getDay(new Date())));
										rowData.put("COMPANYNAME", targetCompanyModel.getCompanyName());
										rowData.put("DEPTID", Integer.toString(targetDepartmentModel.getId()));
										rowData.put("DEPTNAME", targetDepartmentModel.getDepartmentName());
										rowData.put("AWSID", targetUserModel.getUID());
										rowData.put("USERNAME", targetUserModel.getUserName());
										rowData.put("ROLEID", Integer.toString(targetRoleModel.getId()));
										rowData.put("ROLENAME", targetRoleModel.getGroupName() + "/" + targetRoleModel.getRoleName());
										rowData.put("INSTANCEID", Integer.toString(taskInstanceModel.getProcessInstanceId()));
										rowData.put("TASKID", Integer.toString(taskInstanceModel.getId()));
										rowData.put("TITLE", taskInstanceModel.getTitle());
										rowData.put("COSTPOINT", Integer.toString(costModel._costPoint));
										rowData.put("COSTID", Integer.toString(costModel._id));
										rowData.put("COSTTIME", Long.toString(taskCostNow));
										rowData.put("FLOWPOINTER", flowModel._id);
										rowData.put("STATUS", "待核定");
										rowData.put("SYSAUTO", sysAutoTitle);
										timeOutLogBoId = BOInstanceAPI.getInstance().createBOData("BO_AWS_RT_COSTLOG", rowData, timeOutLogWorkflowInstanceId, "admin");
										if (timeOutLogBoId > 0) {
											if (LICENSE.isBPA()) {
												if (timeOutLogBoId > 0) {
													WorkFlowTaskTimeoutCollectorImp taskStartImp = new WorkFlowTaskTimeoutCollectorImp();
													taskStartImp.setModel(taskInstanceModel);
													taskStartImp.collectorData();
												}
											}
											// 更新相关时间
											DBSql.executeUpdate(conn,"UPDATE BO_AWS_RT_COSTLOG SET LOGDATE=" + DBSql.getDateDefaultValue() + ",BEGINDATE=" + DBSql.convertLongDate(UtilDate.datetimeFormat(taskInstanceModel.getBeginTime())) + " WHERE ID=" + timeOutLogBoId);
										}
									}
								} catch (Exception e) {
									System.err.println("超时策略计算[wf_task.id=" + taskInstanceModel.getId() + "][" + taskInstanceModel.getTitle() + "]时发生错误");
									e.printStackTrace(System.err);
								}
							} else {
								System.out.println("超时策略执行完，但未记录到COSTLOG日志中!");
							}

							// 执行策略
							if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_TASK_MAIL_INNER) { // 内部邮件提醒
								executeTaskInnerMail(taskInstanceModel, costModel);
							} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_TASK_MAIL_OUTTER) { // 外网邮件提醒
								executeTaskOuterMail(taskInstanceModel, costModel);
							} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_AUTO_TASK) { // 自动执行该任务
								executeTask(taskInstanceModel, processInstanceModel);
							} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_ROLLBACK) { // 自动将该任务回退给上个节点
								rollbackTask(taskInstanceModel, processInstanceModel);
							} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_SHORT_MESSAGE) { // 自动发送催办短信
								sendShortMessage(taskInstanceModel, processInstanceModel);
							} else if (costModel._costPolicy == ActivityDefinitionConst.TIMER_COST_POLICY_CLAZZ) { // 执行一个Java类
								boolean isExecute = executeClazz(taskCostNow, taskInstanceModel, processInstanceModel, stepModel, costModel);
								if (!isExecute && timeOutLogWorkflowInstanceId > 0) {// 删除超时日志，等同该限时操作未执行
									try {
										WorkflowEngine.getInstance().removeProcessInstance(timeOutLogWorkflowInstanceId);
									} catch (Exception e) {
										e.printStackTrace(System.err);
									}
								}
							} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_NORMAL) { // 什么都不做
							}
							// 标记发出此任务的工作流实例发生过超时
							ProcessRuntimeDaoFactory.createProcessInstance().setOvertime(taskInstanceModel.getProcessInstanceId(), true);
						}
					} // end for
				}
			}

			// 系统提醒类任务，设置保留天数为 1 天
			AWFConfig._awfServerConf.setTaskNoticeDays("1");
			// 系统提醒类任务,只保留3天，超过自动清除
			String days = AWFConfig._awfServerConf.getTaskNoticeDays();
			// 配置0不做清除
			if (!days.equals("0")) {
				Hashtable notifyTask = ProcessRuntimeDaoFactory.createTaskInstance().getAllActiveTaskListOfStatus(UserTaskRuntimeConst.STATE_TYPE_SYSTEM_NOTIFY);
				for (int i = 0; i < notifyTask.size(); i++) {
					TaskInstanceModel taskInstanceModel = (TaskInstanceModel) notifyTask.get(new Integer(i));
					if (System.currentTimeMillis() > (taskInstanceModel.getBeginTime().getTime() + (Long.parseLong(days) * 24 * 60 * 60 * 1000))) {
						System.out.println("[通知]已超过三天,被自动清除。阅读者[" + taskInstanceModel.getTarget() + "]标题[" + taskInstanceModel.getTitle() + "]");
						DBSql.executeUpdate(conn,"delete from wf_task where id=" + taskInstanceModel.getId());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, stmt, rset);
		}
	}

	private boolean executeClazz(long taskCost, TaskInstanceModel taskInstanceModel, ProcessInstanceModel processInstanceModel, WorkFlowStepModel stepModel, WorkFlowStepCostModel costModel) {
		try {
			// 获取构造器
			// 类构造器
			Constructor cons = null;
			// StepRT类的 构造方法参数类型
			Class[] parameterTypes = {};
			cons = ClassReflect.getConstructor(costModel._bizClazz, parameterTypes);
			if (cons != null) {
				// 构造器参数值
				Object[] initargets = {};
				TaskTimeOutEventA superClass = (TaskTimeOutEventA) cons.newInstance(initargets);
				if (superClass != null) {
					return superClass.taskTimeOut(taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId(), taskInstanceModel, processInstanceModel, taskCost, stepModel);
				}
			}
		} catch (Exception e) {
			System.err.println("节点限时触发的自定义Java类【" + costModel._bizClazz + "】没有在超时计算时正确执行!");
			e.printStackTrace(System.err);
		}
		return false;
	}

	/**
	 * 是否已经执行过该 固定频率 成本
	 * 
	 * @param conn
	 * @param taskInstanceModel
	 * @param wfsCostModel
	 * @return false 没执行 true 执行完毕了
	 * @author Melting-PC
	 */
	private boolean isExcutedCostTrigger(TaskInstanceModel taskInstanceModel, WorkFlowStepCostModel wfsCostModel) {
		if (wfsCostModel._costText == null) {
			return true;
		}
		String[] cost = wfsCostModel._costText.split(":");
		int rule = Integer.parseInt(cost[0]);
		int year = UtilDate.getYear(new Date());
		int month = UtilDate.getMonth(new Date());
		int day = UtilDate.getDay(new Date());
		int hour = UtilDate.getHour(new Date());
		int minutes = UtilDate.getMinute(new Date());
		int week = UtilDate.getDayOfWeek(year, month, day);
		java.sql.Timestamp taskBeginTime;
		switch (rule) {
		// 每日
		case 0: {
			String h = cost[1];
			String m = cost[2];
			taskBeginTime = taskInstanceModel.getBeginTime();
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h));
			calendar.set(Calendar.MINUTE, Integer.parseInt(m));
			if(!hasCostExcuteed(hour, h, minutes, m, year, month, day, taskInstanceModel.getId(), wfsCostModel._id, calendar.getTime(), taskBeginTime)){
				return false;
			}
			break;
		}
			// 每周
		case 1: {
			String h = cost[2];
			String m = cost[3];
			String[] weeks = cost[1].split(",");
			for (int i = 0; i < weeks.length; i++) {
				if (week == Integer.parseInt(weeks[i])) {
					taskBeginTime = taskInstanceModel.getBeginTime();
					Calendar calendar = UtilDate.getCalendarByDayOfCurrentWeek(week);
					calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h));
					calendar.set(Calendar.MINUTE, Integer.parseInt(m));
					if(!hasCostExcuteed(hour, h, minutes, m, year, month, day, taskInstanceModel.getId(), wfsCostModel._id, calendar.getTime(), taskBeginTime)){
						return false;
					}
				}
			}
			break;
		}
			// 每月
		case 2: {
			String h = cost[2];
			String m = cost[3];
			String[] days = cost[1].split(",");
			for (int i = 0; i < days.length; i++) {
				if (day == Integer.parseInt(days[i])) {
					taskBeginTime = taskInstanceModel.getBeginTime();
					Calendar calendar = UtilDate.getCalendarByDayOfCurrentMonth(day);
					calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h));
					calendar.set(Calendar.MINUTE, Integer.parseInt(m));
					if(!hasCostExcuteed(hour, h, minutes, m, year, month, day, taskInstanceModel.getId(), wfsCostModel._id, calendar.getTime(), taskBeginTime)){
						return false;
					}
				}
			}
			break;
		}
			// 每季度
		case 3: {
			String h = cost[3];
			String m = cost[4];
			if ((month % 3 + 1) == Integer.parseInt(cost[1]) && day == Integer.parseInt(cost[2])) {
				taskBeginTime = taskInstanceModel.getBeginTime();
				Calendar calendar = UtilDate.getCalendarByCurrentQuarter((month % 3 + 1), day);
				calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h));
				calendar.set(Calendar.MINUTE, Integer.parseInt(m));
				if(!hasCostExcuteed(hour, h, minutes, m, year, month, day, taskInstanceModel.getId(), wfsCostModel._id, calendar.getTime(), taskBeginTime)){
					return false;
				}
			}
			break;
		}
			// 每年
		case 4: {
			String h = cost[3];
			String m = cost[4];
			if (month == Integer.parseInt(cost[1]) && day == Integer.parseInt(cost[2])) {
				taskBeginTime = taskInstanceModel.getBeginTime();
				Calendar calendar = UtilDate.getCalendarByCurrentYear(month, day);
				calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h));
				calendar.set(Calendar.MINUTE, Integer.parseInt(m));
				if(!hasCostExcuteed(hour, h, minutes, m, year, month, day, taskInstanceModel.getId(), wfsCostModel._id, calendar.getTime(), taskBeginTime)){
					return false;
				}
			}
			break;
		}
		default:
			break;
		}
		return true;
	}

	private boolean hasCostExcuteed(int hour,String h,int minutes,String m,int year,int month,int day,int taskId,int costId,Date date,java.sql.Timestamp taskBeginTime){
		if (hour > Integer.parseInt(h) && date.after(taskBeginTime)) {
			if (!this.isCostExcuteed(year, month, day, taskId, costId)) {
				return false;
			}
		} else if (hour == Integer.parseInt(h) && minutes >= Integer.parseInt(m) && date.after(taskBeginTime)) {
			if (!this.isCostExcuteed(year, month, day, taskId, costId)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 判断 是否已经执行过 固定频率的 成本了
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @param taskid
	 * @param costId
	 * @param conn
	 * @return
	 * @author Melting-PC
	 */
	private boolean isCostExcuteed(int year, int month, int day, int taskid, int costId) {
		String sql = "select count(id) as c from BO_AWS_RT_COSTLOG where YEAR=" + year + " and MONTH=" + month + " and DAY=" + day + " and TASKID=" + taskid + " and COSTID=" + costId;
		return DBSql.getInt(sql, "c") > 0;
	}

	/**
	 * 
	 * @param taskInstanceModel
	 * @param processInstanceModel
	 */
	private void sendShortMessage(TaskInstanceModel taskInstanceModel, ProcessInstanceModel processInstanceModel) {
		UserContext owner = null;
		try {
			owner = new UserContext(taskInstanceModel.getTarget());
		} catch (Exception e) {
		}

		if (owner != null) {
			SMSContext sms = new SMSContext();
			sms.setCompanyName(owner.getCompanyModel().getCompanyName());
			sms.setDepartmentName(owner.getDepartmentModel().getDepartmentName());
			sms.setUid(owner.getUID());
			sms.setUserName(owner.getUserModel().getUserName());

			UserModel model = (UserModel) UserCache.getModel(taskInstanceModel.getTarget());
			if ((model != null) && (model.getSMid() != null) && !model.getSMid().equals("")) {
				sms.setMobileCode(model.getSMid().trim());
			} else if ((model != null) && (model.getMobile() != null) && !model.getMobile().equals("")) {
				sms.setMobileCode(model.getMobile().trim());
			}
			sms.setMobileID(model.getUID().trim());
			sms.setMobileUserName(model.getUserName());
			SendSMSUtil send = new SendSMSUtil(sms);
			send.send("超时待办提醒:" + taskInstanceModel.getTitle());
		}
	}

	/**
	 * 回滚任务到上一个办理者
	 * 
	 * @param taskInstanceModel
	 * @param processInstanceModel
	 */
	private void rollbackTask(TaskInstanceModel taskInstanceModel, ProcessInstanceModel processInstanceModel) {
		// 寻找上一个办理者的节点ID
		int wfsId = DBSql.getInt("select WFSID from wf_task_log where bind_id=" + processInstanceModel.getId() + " and status=1 order by id desc", "WFSID");
		if (wfsId > 0) {// 发现上个办理者所在的节点
			WorkFlowStepModel stepModel = (WorkFlowStepModel) WorkFlowStepCache.getModel(wfsId);
			if (stepModel != null) {
				String owner = DBSql.getString("select owner from wf_task_log where bind_id=" + processInstanceModel.getId() + " and status=1 order by id desc", "owner");
				String target = DBSql.getString("select target from wf_task_log where bind_id=" + processInstanceModel.getId() + " and status=1 order by id desc", "target");
				String title = DBSql.getString("select title from wf_task_log where bind_id=" + processInstanceModel.getId() + " and status=1 order by id desc", "title");
				try {
					int[] taskId = WorkflowTaskEngine.getInstance().createProcessTaskInstance(owner, processInstanceModel.getId(), SynType.synchronous, PriorityType.normal, 1, stepModel._stepNo, target, "(超时未办回退)[" + title, false, 0);
					if (taskId[0] > 0) {
						DBSql.executeUpdate("delete from wf_task where id=" + taskInstanceModel.getId());// 清除待办
						DBSql.executeUpdate("delete from wf_task_log where id=" + taskInstanceModel.getId());// 清除已待办
					}
				} catch (WorkflowException we) {
					we.printStackTrace(System.err);
				}
			}
		}
	}

	private void executeTask(TaskInstanceModel taskInstanceModel, ProcessInstanceModel processInstanceModel) {
		// 仿真当前任务的办理者
		UserContext uc = null;

		try {
			uc = new UserContext(taskInstanceModel.getTarget());
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		// 判断当前节点是否已经办理完毕
		String returnCode = "";
		try {
			returnCode = WorkflowTaskEngine.getInstance().assignComplexProcessTaskInstance(uc, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId());
			// 在当前审批节点，增加超时处理信息
			WorkflowTaskInstanceAPI.getInstance().appendOpinionHistory(taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId(), "超时", "<font color=red>超时系统自动处理</font>");
			// System.out.println(taskModel._title+"----"+returnCode);
			if (returnCode.equals(TaskRuntimeConst.SINGLE_LEAVE_STATUS_PROCESSEND)) {// workflow
																						// end
				WorkFlowModel workFlowModel = (WorkFlowModel) WorkFlowCache.getModel(processInstanceModel.getProcessDefinitionId());
				if (workFlowModel._isAutoArchives) { // 自动归档,案卷id为-xxxx年
					new UserTaskExecuteWeb().toArchives(uc, taskInstanceModel.getProcessInstanceId(), -Integer.parseInt(UtilDate.yearFormat(new java.sql.Timestamp(System.currentTimeMillis()))), taskInstanceModel.getId()); // 自动归档,案卷id为-xxxx年
				} else { // 手工归档
					System.out.println("节点超时策略执行提醒：任务[" + taskInstanceModel.getTitle() + "]所依赖的工作流被指定为手工归档，系统无法自动完成，超时策略执行失败！");
					return;
				}

				// remove this task
				WorkflowTaskEngine.getInstance().closeProcessTaskInstance(uc, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId());
			} else if (returnCode.equals(TaskRuntimeConst.SINGLE_LEAVE_STATUS_ASSIGN)) {// old:open
																						// send
																						// window
				JumpActivityRuleEngine jumpEngine = ProcessRuleEngine.getInstance().jumpActivityRuleEngine(uc, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId());
				int r = jumpEngine.getNextActivityNo();// 获得下一个步骤号
				if (r == -1|| r == 9999) {//增加9999的判断，直接归档 modify by zhanghf 2013.03.26
					// HeadMessageModel headMessageModel = (HeadMessageModel)
					// HeadMessageDaoFactory.createHeadMessage().getInstance(id);
					WorkFlowModel workFlowModel = (WorkFlowModel) WorkFlowCache.getModel(processInstanceModel.getProcessDefinitionId());
					if (workFlowModel._isAutoArchives) { // 自动归档,案卷id为-xxxx年
						new UserTaskExecuteWeb().toArchives(uc, taskInstanceModel.getProcessInstanceId(), -Integer.parseInt(UtilDate.yearFormat(new java.sql.Timestamp(System.currentTimeMillis()))), taskInstanceModel.getId()); // 自动归档,案卷id为-xxxx年
					} else { // 手工归档
						System.out.println("节点超时策略执行警告：任务[" + taskInstanceModel.getTitle() + "]所依赖的工作流被指定为手工归档，系统无法自动完成，超时策略执行失败！");
						return;
					}
				} else { // 向下流转
					WorkFlowStepModel stepModel = (WorkFlowStepModel) WorkFlowStepCache.getModelOfStepNo(processInstanceModel.getProcessDefinitionId(), r);

					// 下一个节点，必须 是被指定了固定办理者办理
					// add by jackliu 200408010 凌晨3:40
					// 寻找当前办理者所在部门（需要根据任务所有者来推算）
					DepartmentModel localDepartmentModel = uc.getDepartmentModel();
					int ownerDepartmentId = taskInstanceModel.getOwnerDepartmentId();

					if (ownerDepartmentId > 0) {
						// 从任务发起者的部门起向上追溯2级
						// UserModel userModel1 = (UserModel)
						// UserCache.getModel(taskModel._owner);
						// 判断当前办理者是否隶属于前一节点办理者部门
						if (UserCache.isExistInDepartment(taskInstanceModel.getOwnerDepartmentId(), uc.getID())) {
							localDepartmentModel = (DepartmentModel) DepartmentCache.getModel(taskInstanceModel.getOwnerDepartmentId());
						} else { // 上级部门
							DepartmentModel tmpDepartmentModel = (DepartmentModel) DepartmentCache.getModel(taskInstanceModel.getOwnerDepartmentId());
							// 判断当前办理者是否隶属于前一节点办理者的上级部门
							if (UserCache.isExistInDepartment(tmpDepartmentModel.getParentDepartmentId(), uc.getID())) {
								localDepartmentModel = (DepartmentModel) DepartmentCache.getModel(tmpDepartmentModel.getParentDepartmentId());
							}
						}
						// modify by jackliu
						// 如果当时的部门已经被删除了，查询当前owner所在的部门
						if (localDepartmentModel == null) {
							localDepartmentModel = (DepartmentModel) DepartmentCache.getModel(((UserModel) UserCache.getModel(taskInstanceModel.getOwner())).getDepartmentId());
						}
					}

					String workMan = stepModel._stepUser;
					Object o = RouteFactory.getInstance(uc, processInstanceModel, localDepartmentModel, ownerDepartmentId, stepModel._routeType);

					if (o != null) { // 找到了处理相关路由方式的类
						workMan = ((RouteAbst) o).getTargetUserAddress(stepModel, taskInstanceModel.getId());
					} else {
						System.err.println("没有找到相关的路由处理类!");
						return;
						// 没有找到路由处理类，不做任何操作;
					}

					if ((workMan == null) || workMan.equals("")) {
						System.err.println("节点超时策略执行提醒：任务[" + taskInstanceModel.getTitle() + "]的下一个节点是[" + stepModel._stepName + "]，但是该节点没有指定固定的办理者，系统将此办理任务发送给该流程管理员！");
						WorkFlowModel workFlowModel = (WorkFlowModel) WorkFlowCache.getModel(processInstanceModel.getProcessDefinitionId());
						workMan = workFlowModel._flowMaster;
						if ((workMan == null) || workMan.equals("")) {
							System.err.println("节点超时策略执行提醒：任务[" + taskInstanceModel.getTitle() + "]的下一个节点是[" + stepModel._stepName + "]，但是该节点没有指定固定的办理者，也没有发现流程管理员，系统无法自动完成，超时策略执行失败！");
							return;
						}
					}

					// 更新当前任务标题，标记为潮湿办理的
					DBSql.executeUpdate("update wf_task set title='**超时自动处理:" + taskInstanceModel.getTitle() + "' where id=" + taskInstanceModel.getId());

					// insert a new task!
					try {
						int[] taskId = WorkflowTaskEngine.getInstance().createProcessTaskInstance(uc, taskInstanceModel.getProcessInstanceId(), new SynType(stepModel._routePointType), PriorityType.normal, 1, r, workMan, "(超时自动)(" + stepModel._stepName + ")" + processInstanceModel.getTitle(), false, 0);
						WorkflowTaskEngine.getInstance().closeProcessTaskInstance(uc, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId());
					} catch (WorkflowException we) {
						we.printStackTrace(System.err);
					}
					return;
				}
			} else if (returnCode.equals("task break")) {
				System.err.println("节点超时策略执行警告：任务[" + taskInstanceModel.getTitle() + "]所依赖的工作流节点被指定了RTClass,该类在执行时不允许结束此任务，系统无法自动完成，超时策略执行失败！");
				return;
			} else if (returnCode.equals("task end")) {
			} else {
				System.err.println("节点超时策略执行警告：任务[" + taskInstanceModel.getTitle() + "]被执行时返回的状态码无法识别：" + returnCode);
				return;
			}

		} catch (Exception we) {
			we.printStackTrace(System.err);
		}

	}

	/**
	 * 超时后，给当前办理者发送电子邮件
	 * 
	 * @param taskInstanceModel
	 *            超时任务
	 */
	private void executeTaskOuterMail(TaskInstanceModel taskInstanceModel, WorkFlowStepCostModel costModel) {
		String[] emailTemplete = null;
		String mailNo = costModel._mailNo;
		try {
			if (mailNo == null || mailNo.trim().length() == 0) {

				emailTemplete = IMAPI.getInstance().getMailDefaultTemplateByGroupName(WFFlexDesignEmailTemplateUtil.MAIL_GROUP_TIMEOUT);

			} else {
				emailTemplete = IMAPI.getInstance().getMailTemplate(mailNo);
			}

		} catch (AWSSDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
		try {
			emailTemplete = emailTemplete == null ? IMAPI.getInstance().getMailDefaultTemplateByGroupName(WFFlexDesignEmailTemplateUtil.MAIL_GROUP_TIMEOUT) : emailTemplete;
		} catch (AWSSDKException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace(System.err);
		}
		if (emailTemplete == null) {
			System.err.println("不存在邮件模板[" + costModel._mailNo + "]发送超时提醒失败");
		} else {
			UserContext uc = null;
			try {
				uc = new UserContext(taskInstanceModel.getTarget());
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
			UserContext sender = null;
			try {
				sender = new UserContext(RuleAPI.getInstance().executeRuleScript(emailTemplete[0], uc, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId()));
			} catch (Exception e) {
				try {
					sender = new UserContext("admin");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
				}
				e.printStackTrace(System.err);
			}
			// TO 获取账户外网邮箱地址
			String mailTo = RuleAPI.getInstance().executeRuleScript(costModel._mailTo, uc, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId());
			mailTo = mailTo.replaceAll("%CURRENT_CONTEXT%", taskInstanceModel.getTarget());
			Hashtable innerAccountTo = new com.actionsoft.awf.util.UnsyncHashtable();
			Hashtable wwwAccountTo = new com.actionsoft.awf.util.UnsyncHashtable();
			AWSMailUtil.getInstance().getAddressList(sender.getCompanyModel().getId(), innerAccountTo, wwwAccountTo, mailTo, "TO");
			mailTo = this.getOuterMailAddress(innerAccountTo, wwwAccountTo);
			Hashtable param = new com.actionsoft.awf.util.UnsyncHashtable();
			param.put("%CURRENT_CONTEXT%", taskInstanceModel.getTarget());
			try {
				IMAPI.getInstance().sendMailByModel(emailTemplete[4], taskInstanceModel.getTarget(), mailTo, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId(), param);
			} catch (AWSSDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(System.err);
			}
			// String
			// mailCC=RuleAPI.getInstance().executeRuleScript(emailTemplete[1],
			// uc, taskModel._bindId, taskModel._id);
			// Hashtable innerAccountCC = new com.actionsoft.awf.util.UnsyncHashtable();
			// Hashtable wwwAccountCC = new com.actionsoft.awf.util.UnsyncHashtable();
			// AWSMailUtil.getInstance().getAddressList(sender.getCompanyModel()._id,
			// innerAccountCC, wwwAccountCC, mailTo, "CC");
			// mailCC=this.getOuterMailAddress(innerAccountCC, wwwAccountCC);
			//			
			// MailModel mailModel = new MailModel();
			// mailModel._content =
			// RuleAPI.getInstance().executeRuleScript(emailTemplete[3], uc,
			// taskModel._bindId, taskModel._id);
			// mailModel._createUser =
			// RuleAPI.getInstance().executeRuleScript(emailTemplete[0], uc,
			// taskModel._bindId, taskModel._id);
			// mailModel._isImportant = true;
			// mailModel._cc = mailCC;
			// mailModel._title =
			// RuleAPI.getInstance().executeRuleScript(emailTemplete[2], uc,
			// taskModel._bindId, taskModel._id);
			// mailModel._to = mailTo;
			// mailModel._mailType = MailTypeConst.Mail_TYPE_INNER_MAIL;
			// AWSMailUtil.getInstance().SendMail(sender, 0, mailModel);
		}
	}

	private String getOuterMailAddress(Hashtable innerAccount, Hashtable wwwAccount) {
		StringBuilder addressList = new StringBuilder();
		for (int i = 0; i < innerAccount.size(); i++) {
			String uid = (String) innerAccount.get(new Integer(i));
			if (uid != null && uid.trim().length() > 0) {
				UserModel userModel = (UserModel) UserCache.getModel(uid);
				if (userModel != null && userModel.getEmail() != null && userModel.getEmail().trim().length() > 0) {
					addressList.append(userModel.getEmail()).append(" ");
				} else {
					addressList.append(uid).append(" ");
				}
			}
		}
		for (int i = 0; i < wwwAccount.size(); i++) {
			String email = (String) wwwAccount.get(new Integer(i));
			if (email != null && email.indexOf(":") != -1) {
				String[] wwwEMail = email.split(":");
				addressList.append(wwwEMail[1]).append(" ");
			}
		}
		return addressList.toString().trim();
	}

	/**
	 * 超时后，给当任务起草者发送电子邮件
	 * 
	 * @param taskInstanceModel
	 *            超时任务
	 * @param headMessageModel
	 *            工作流实例
	 */
	private void executeTaskInnerMail(TaskInstanceModel taskInstanceModel, WorkFlowStepCostModel costModel) {

		String[] emailTemplete = null;
		String mailNo = costModel._mailNo;
		try {
			if (mailNo == null || mailNo.trim().length() == 0) {

				emailTemplete = IMAPI.getInstance().getMailDefaultTemplateByGroupName(WFFlexDesignEmailTemplateUtil.MAIL_GROUP_TIMEOUT);

			} else {
				emailTemplete = IMAPI.getInstance().getMailTemplate(mailNo);
			}
		} catch (AWSSDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
		try {
			emailTemplete = emailTemplete == null ? IMAPI.getInstance().getMailDefaultTemplateByGroupName(WFFlexDesignEmailTemplateUtil.MAIL_GROUP_TIMEOUT) : emailTemplete;
		} catch (AWSSDKException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace(System.err);
		}
		if (emailTemplete == null) {
			System.err.println("不存在邮件模板[" + costModel._mailNo + "]发送超时提醒失败");
		} else {
			UserContext uc = null;
			try {
				uc = new UserContext(taskInstanceModel.getTarget());
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
			String mailTo = RuleAPI.getInstance().executeRuleScript(costModel._mailTo, uc, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId());
			mailTo = mailTo.replaceAll("%CURRENT_CONTEXT%", taskInstanceModel.getTarget());
			Hashtable param = new com.actionsoft.awf.util.UnsyncHashtable();
			param.put("%CURRENT_CONTEXT%", taskInstanceModel.getTarget());
			try {
				IMAPI.getInstance().sendMailByModel(emailTemplete[4], taskInstanceModel.getTarget(), mailTo, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId(), param);
			} catch (AWSSDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(System.err);
			}
		}
	}

}
