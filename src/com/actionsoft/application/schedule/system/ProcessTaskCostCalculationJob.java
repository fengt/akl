/*
 * Copyright(C)2001-2012 Actionsoft Co.,Ltd
 * AWS(Actionsoft workflow suite) BPM(Business Process Management) PLATFORM Source code 
 * AWS is a application middleware for BPM System

  
 * ��������̱���Ķ������ļ���Դ���Ȩ�鱱���׻�ӯ���Ƽ���չ�������ι�˾���У�
 * ���й����Ұ�Ȩ�ֱ�������ط��ɱ�����δ�����淨����ɣ��κθ��˻���֯������й©��
 * ������Դ���ļ���ȫ���򲿷��ļ������öԱ����ļ��������򹤳̣�Υ�߱ؾ���

 * $$��Դ�����׻�ӯ����߱��ܼ�����ļ�$$
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
 * 1.�ڵ㳬ʱ���Դ�����ʵ���� 2.ϵͳ���������񣬳���3d�Զ����
 * 
 */
public class ProcessTaskCostCalculationJob implements IJob {
	// �Ƿ�ǰAWSƽ̨���ڼ�¼��ʱ���Լ��۷������BOԪ����
	// ��û�оͲ�ִ�м�¼�Ĳ���
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
		System.out.println("��Ϣ: [" + UtilDate.datetimeFormat(new Date(beginTime)) + "]AWS Process cost calculation:[" + ((endTime - beginTime) / 1000) + "s]");
	}

	/**
	 * ִ��һ������ʱ���,���Ƶ���ɶ�ʱ������
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
				// �����Ĵ�������
				if (taskInstanceModel.getStatus() == 1) {
					// ��õ�ǰ�����Workflow Instance
					rset = DBSql.executeQuery(conn, stmt, "select * from wf_messagedata where id = "+taskInstanceModel.getProcessInstanceId());
					ProcessInstanceModel processInstanceModel = null;
					if (rset != null) {
						if (rset.next()) {
							processInstanceModel =new ProcessInstance().record2Model(rset);
						}
					}
					if (processInstanceModel == null) {
						System.out.println("���棺IDΪ[" + taskInstanceModel.getId() + "]����Ϊ[" + taskInstanceModel.getTitle() + "]�������Ѷ�ʧ����ʵ������ʱ����δִ��!");
						continue;
					}

					// ��øýڵ�ĳɱ������б�
					WorkFlowStepModel stepModel = (WorkFlowStepModel) WorkFlowStepCache.getModelOfStepNo(processInstanceModel.getProcessDefinitionId(), processInstanceModel.getActivityDefinitionNo());
					if (stepModel == null) {
						System.out.println("���棺IDΪ[" + taskInstanceModel.getId() + "]����Ϊ[" + taskInstanceModel.getTitle() + "]�������������̽ڵ�ģ��δ�ҵ�(bindid=" + processInstanceModel.getId() + ",wfId=" + processInstanceModel.getProcessDefinitionId() + ",wfsNo=" + processInstanceModel.getActivityDefinitionNo() + ")����ʱ����δִ��!");
						continue;
					}

					// ��ȡ�����˵���֯�����Ϣ��Ϊ�۷��������Ϣ��׼��
					// �ñ����ڿ۷�ѭ���н���ʼ��һ�Σ�������forѭ���ڣ�Ŀ����
					// ����ýڵ�δ������ʱ���ԣ�������ȡ��Щ׼����Ϣ�������������
					UserModel targetUserModel = null;
					DepartmentModel targetDepartmentModel = null;
					CompanyModel targetCompanyModel = null;
					RoleModel targetRoleModel = null;

					// ��ȡ�ýڵ�ĳɱ�����
					Hashtable costList = WorkFlowStepCostCache.getListOfWorkFlowStep(stepModel._id);
					for (int ii = 0; ii < costList.size(); ii++) {
						WorkFlowStepCostModel costModel = (WorkFlowStepCostModel) costList.get(new Integer(ii));
						// �Ƿ�ִ�нڵ�ɱ�����
						boolean isExcuteCost = false;
						long taskCostNow = 0;
						// ������㷽ʽ�Ǵ��Ķ�����ʼ���㣬���һ�û���Ķ����񣬺���
						if (costModel._calcType == 1 && taskInstanceModel.getReadTime() == null) {
							continue;
						}
						// ����ɶ�Ƶ�ʳɱ����� ���ظ�ִ��
						if (costModel._calcType == 3) {
							Calendar c = Calendar.getInstance();
							c.setTime(new Date(taskInstanceModel.getBeginTime().getTime()));
							taskCostNow = CalWorkTimeImp.getInstanceByUid(taskInstanceModel.getTarget()).calcWorkingTime(c);
							isExcuteCost = !isExcutedCostTrigger(taskInstanceModel, costModel);
						} else {
							// ���㳣�泬�в���
							// ����ò����Ѿ��ڸ����񴥷���һ�Σ����ټ���ִ��
							if (DBSql.getInt(conn,"select count(*) as c from BO_AWS_RT_COSTLOG where TASKID=" + taskInstanceModel.getId() + " and COSTID=" + costModel._id, "c") > 0) {
								continue;
							}

							// �ڵ�ɱ�����ʱ�ĵ�λ��Сʱ
							double taskCostTimes = costModel._cost;
							if (taskCostTimes == -100d) {// ������������ʱ
								if (stepModel._duration == 0) {
									System.err.println("ʱ�޼��-[����][��������=0][������=����ִ�иò���][Task=" + taskInstanceModel.getTitle() + "]");
									continue;// δ��������
								}
								// taskCostTimes =
								// Double.parseDouble(Integer.toString(stepModel._duration))
								// / 1000d / 60d ;// �ۺϳɷ���
								taskCostTimes = Double.parseDouble(Long.toString(stepModel._duration)) / 1000d / 60d / 60d;// �ۺϳ�Сʱ
								if (taskCostTimes == 0d) {
									System.err.println("ʱ�޼��-[����][��������=��ֵ̫С][������=����ִ�иò���][Task=" + taskInstanceModel.getTitle() + "]");
									continue;//	
								}
							} else if (taskCostTimes == -101d) {// ������������ʱ
								if (stepModel._durationWarning == 0) {
									System.err.println("ʱ�޼��-[����][��������=0][������=����ִ�иò���][Task=" + taskInstanceModel.getTitle() + "]");
									continue;// δ��������
								}
								// taskCostTimes =
								// Double.parseDouble(Integer.toString(stepModel._durationWarning))
								// / 1000d / 60d; // �ۺϳɷ���
								taskCostTimes = Double.parseDouble(Integer.toString(stepModel._durationWarning)) / 1000d / 60d / 60d; // �ۺϳ�Сʱ
								if (taskCostTimes == 0d) {
									System.err.println("ʱ�޼��-[����][��������=��ֵ̫С][������=����ִ�иò���][Task=" + taskInstanceModel.getTitle() + "]");
									continue;//
								}
							}

							// ��ǰ�ڵ�ĳɱ�Сʱ
							Calendar c = Calendar.getInstance();
							if (costModel._calcType == 1) {// ���Ķ�ʱ�����
								c.setTime(new Date(taskInstanceModel.getReadTime().getTime()));
							} else if (costModel._calcType == 0) {// �����񵽴�ʱ�俪ʼ����
								c.setTime(new Date(taskInstanceModel.getBeginTime().getTime()));
							} else if (costModel._calcType == 2) {// ��@��ʽ����ȡ��������������ա�Сʱ�ָ�ʽ
								String timeValueStr = costModel._bizTime;
								if (timeValueStr.indexOf("@") > -1) {// �����İ���@����
									try {
										UserContext targetContext = new UserContext(taskInstanceModel.getTarget());
										RuntimeFormManager rfm = new RuntimeFormManager(targetContext, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId(), 0, 0);
										timeValueStr = rfm.convertMacrosValue(costModel._bizTime);
									} catch (Exception e) {
										e.printStackTrace(System.err);
									}
								}
								// ��ʱ�䴮ת����times
								long beginTime = new UtilDate().getTimes(timeValueStr, "yyyy-MM-dd HH:mm:ss");
								if (beginTime == 0) {
									beginTime = new UtilDate().getTimes(timeValueStr, "yyyy-MM-dd");
									if (beginTime == 0) {
										System.err.println("ʱ�޼��-[����][ҵ�����=" + timeValueStr + "][�����ǷǺϷ���yyyy-MM-dd HH:mm:ss��ʽ][������=����ִ�иò���][Task=" + taskInstanceModel.getTitle() + "]");
										continue;// ���Ըò��Ե�ִ��
									}
								}
								c.setTime(new Date(beginTime));
							}

							// ʹ�õ�ǰ�����ߵĹ����������㣬��ȷ������(calcWorkingTimeOfSecond�ɾ�ȷ���룩
							taskCostNow = CalWorkTimeImp.getInstanceByUid(taskInstanceModel.getTarget()).calcWorkingTime(c);
							isExcuteCost = taskCostNow > taskCostTimes * 60;
						}
						String sysAutoTitle = "��";

						if (isExcuteCost) {
							// ���������ҵ�������㣬�жϹ�����ʽ�Ƿ����
							String bizRuleStr = costModel._bizRule;
							if (bizRuleStr.indexOf("@") > -1) {// �����İ���@����
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
								if (bizRuleStr.equals("true") || bizRuleStr.equals("yes") || bizRuleStr.equals("��") || bizRuleStr.equals("1") || bizRuleStr.equals("on")) {
									// ��������
								} else {
									System.err.println("ʱ�޼��-[��Ϣ][ҵ������=FALSE][������=����ִ�иò���][Task=" + taskInstanceModel.getTitle() + "]");
									continue;// ������ҵ�����������Ըò��Ե�ִ��
								}
							}

							// �ٴ�У���µ�ǰ�����Ƿ��Ѿ����û�ִ����
							// ���У���ǿ��ǵ��ɱ�������Ҫ����һ��ʱ�䣬����ǡ���ֵ����������ʱ����������Ѿ����û����������
							// ���������������������Գɱ�����
							TaskInstanceModel tmpModel = null;
							rset = DBSql.executeQuery(conn, stmt, "SELECT * FROM WF_TASK WHERE ID=" + taskInstanceModel.getId());
							if (rset != null) {
								if (rset.next()) {
									tmpModel =new TaskInstance().record2TaskModel(rset);
								}
							}
							if (tmpModel == null)
								continue;
							// ����ʱ��Ϣ��¼��BO_AWS_RT_COSTLOG��־
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

								if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_TASK_MAIL_INNER) { // ������������߷��͵����ʼ�
									sysAutoTitle = "���������ʼ�֪ͨ";
								} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_TASK_MAIL_OUTTER) { // ����ǰ�����߷��͵����ʼ�
									sysAutoTitle = "���������ʼ�֪ͨ";
								} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_AUTO_TASK) { // �Զ�ִ�и�����
									sysAutoTitle = "�Զ�����ִ��";
								} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_ROLLBACK) { // �Զ�����������˸��ϸ��ڵ�
									sysAutoTitle = "���������˻�";
								} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_SHORT_MESSAGE) { // �Զ����ʹ߰����
									sysAutoTitle = "������������߷����˴߰����";
								} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_NORMAL) { // ʲô������
								} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_CLAZZ) {
									sysAutoTitle = "�Զ���ĳ�ʱ������δ֪";
								}

								try {
									WorkFlowModel flowModel = (WorkFlowModel) WorkFlowCache.getModel(WorkflowEngine.getInstance().getWorkflowDefId("ff85be74cca5a04c52d9249c0778b446"));
									if (flowModel._workFlowType == 1) {// ���洢
										timeOutLogWorkflowInstanceId = WorkflowInstanceAPI.getInstance().createBOInstance("ff85be74cca5a04c52d9249c0778b446", "admin", "[" + targetUserModel.getUserName() + "]��[" + UtilDate.datetimeFormat(new Date()) + "]"+I18nRes.findValue("�����̼�Ч��ʱ��¼"));
									} else {// ����
										timeOutLogWorkflowInstanceId = WorkflowInstanceAPI.getInstance().createProcessInstance("ff85be74cca5a04c52d9249c0778b446", "admin", "[" + targetUserModel.getUserName() + "]��[" + UtilDate.datetimeFormat(new Date()) + "]"+I18nRes.findValue("�����̼�Ч��ʱ��¼"));
										int timeOutLogTaskInstanceId = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance("admin", timeOutLogWorkflowInstanceId, 0, 1, 1, "admin", "[" + targetUserModel.getUserName() + "]��[" + UtilDate.datetimeFormat(new Date()) + "]"+I18nRes.findValue("�����̼�Ч��ʱ��¼"), false, 0)[0];
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
										rowData.put("STATUS", "���˶�");
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
											// �������ʱ��
											DBSql.executeUpdate(conn,"UPDATE BO_AWS_RT_COSTLOG SET LOGDATE=" + DBSql.getDateDefaultValue() + ",BEGINDATE=" + DBSql.convertLongDate(UtilDate.datetimeFormat(taskInstanceModel.getBeginTime())) + " WHERE ID=" + timeOutLogBoId);
										}
									}
								} catch (Exception e) {
									System.err.println("��ʱ���Լ���[wf_task.id=" + taskInstanceModel.getId() + "][" + taskInstanceModel.getTitle() + "]ʱ��������");
									e.printStackTrace(System.err);
								}
							} else {
								System.out.println("��ʱ����ִ���꣬��δ��¼��COSTLOG��־��!");
							}

							// ִ�в���
							if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_TASK_MAIL_INNER) { // �ڲ��ʼ�����
								executeTaskInnerMail(taskInstanceModel, costModel);
							} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_TASK_MAIL_OUTTER) { // �����ʼ�����
								executeTaskOuterMail(taskInstanceModel, costModel);
							} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_AUTO_TASK) { // �Զ�ִ�и�����
								executeTask(taskInstanceModel, processInstanceModel);
							} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_ROLLBACK) { // �Զ�����������˸��ϸ��ڵ�
								rollbackTask(taskInstanceModel, processInstanceModel);
							} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_SHORT_MESSAGE) { // �Զ����ʹ߰����
								sendShortMessage(taskInstanceModel, processInstanceModel);
							} else if (costModel._costPolicy == ActivityDefinitionConst.TIMER_COST_POLICY_CLAZZ) { // ִ��һ��Java��
								boolean isExecute = executeClazz(taskCostNow, taskInstanceModel, processInstanceModel, stepModel, costModel);
								if (!isExecute && timeOutLogWorkflowInstanceId > 0) {// ɾ����ʱ��־����ͬ����ʱ����δִ��
									try {
										WorkflowEngine.getInstance().removeProcessInstance(timeOutLogWorkflowInstanceId);
									} catch (Exception e) {
										e.printStackTrace(System.err);
									}
								}
							} else if (costModel._costPolicy == UserTaskDefinitionConst.TIMER_COST_POLICY_NORMAL) { // ʲô������
							}
							// ��Ƿ���������Ĺ�����ʵ����������ʱ
							ProcessRuntimeDaoFactory.createProcessInstance().setOvertime(taskInstanceModel.getProcessInstanceId(), true);
						}
					} // end for
				}
			}

			// ϵͳ�������������ñ�������Ϊ 1 ��
			AWFConfig._awfServerConf.setTaskNoticeDays("1");
			// ϵͳ����������,ֻ����3�죬�����Զ����
			String days = AWFConfig._awfServerConf.getTaskNoticeDays();
			// ����0�������
			if (!days.equals("0")) {
				Hashtable notifyTask = ProcessRuntimeDaoFactory.createTaskInstance().getAllActiveTaskListOfStatus(UserTaskRuntimeConst.STATE_TYPE_SYSTEM_NOTIFY);
				for (int i = 0; i < notifyTask.size(); i++) {
					TaskInstanceModel taskInstanceModel = (TaskInstanceModel) notifyTask.get(new Integer(i));
					if (System.currentTimeMillis() > (taskInstanceModel.getBeginTime().getTime() + (Long.parseLong(days) * 24 * 60 * 60 * 1000))) {
						System.out.println("[֪ͨ]�ѳ�������,���Զ�������Ķ���[" + taskInstanceModel.getTarget() + "]����[" + taskInstanceModel.getTitle() + "]");
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
			// ��ȡ������
			// �๹����
			Constructor cons = null;
			// StepRT��� ���췽����������
			Class[] parameterTypes = {};
			cons = ClassReflect.getConstructor(costModel._bizClazz, parameterTypes);
			if (cons != null) {
				// ����������ֵ
				Object[] initargets = {};
				TaskTimeOutEventA superClass = (TaskTimeOutEventA) cons.newInstance(initargets);
				if (superClass != null) {
					return superClass.taskTimeOut(taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId(), taskInstanceModel, processInstanceModel, taskCost, stepModel);
				}
			}
		} catch (Exception e) {
			System.err.println("�ڵ���ʱ�������Զ���Java�ࡾ" + costModel._bizClazz + "��û���ڳ�ʱ����ʱ��ȷִ��!");
			e.printStackTrace(System.err);
		}
		return false;
	}

	/**
	 * �Ƿ��Ѿ�ִ�й��� �̶�Ƶ�� �ɱ�
	 * 
	 * @param conn
	 * @param taskInstanceModel
	 * @param wfsCostModel
	 * @return false ûִ�� true ִ�������
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
		// ÿ��
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
			// ÿ��
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
			// ÿ��
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
			// ÿ����
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
			// ÿ��
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
	 * �ж� �Ƿ��Ѿ�ִ�й� �̶�Ƶ�ʵ� �ɱ���
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
			send.send("��ʱ��������:" + taskInstanceModel.getTitle());
		}
	}

	/**
	 * �ع�������һ��������
	 * 
	 * @param taskInstanceModel
	 * @param processInstanceModel
	 */
	private void rollbackTask(TaskInstanceModel taskInstanceModel, ProcessInstanceModel processInstanceModel) {
		// Ѱ����һ�������ߵĽڵ�ID
		int wfsId = DBSql.getInt("select WFSID from wf_task_log where bind_id=" + processInstanceModel.getId() + " and status=1 order by id desc", "WFSID");
		if (wfsId > 0) {// �����ϸ����������ڵĽڵ�
			WorkFlowStepModel stepModel = (WorkFlowStepModel) WorkFlowStepCache.getModel(wfsId);
			if (stepModel != null) {
				String owner = DBSql.getString("select owner from wf_task_log where bind_id=" + processInstanceModel.getId() + " and status=1 order by id desc", "owner");
				String target = DBSql.getString("select target from wf_task_log where bind_id=" + processInstanceModel.getId() + " and status=1 order by id desc", "target");
				String title = DBSql.getString("select title from wf_task_log where bind_id=" + processInstanceModel.getId() + " and status=1 order by id desc", "title");
				try {
					int[] taskId = WorkflowTaskEngine.getInstance().createProcessTaskInstance(owner, processInstanceModel.getId(), SynType.synchronous, PriorityType.normal, 1, stepModel._stepNo, target, "(��ʱδ�����)[" + title, false, 0);
					if (taskId[0] > 0) {
						DBSql.executeUpdate("delete from wf_task where id=" + taskInstanceModel.getId());// �������
						DBSql.executeUpdate("delete from wf_task_log where id=" + taskInstanceModel.getId());// ����Ѵ���
					}
				} catch (WorkflowException we) {
					we.printStackTrace(System.err);
				}
			}
		}
	}

	private void executeTask(TaskInstanceModel taskInstanceModel, ProcessInstanceModel processInstanceModel) {
		// ���浱ǰ����İ�����
		UserContext uc = null;

		try {
			uc = new UserContext(taskInstanceModel.getTarget());
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		// �жϵ�ǰ�ڵ��Ƿ��Ѿ��������
		String returnCode = "";
		try {
			returnCode = WorkflowTaskEngine.getInstance().assignComplexProcessTaskInstance(uc, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId());
			// �ڵ�ǰ�����ڵ㣬���ӳ�ʱ������Ϣ
			WorkflowTaskInstanceAPI.getInstance().appendOpinionHistory(taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId(), "��ʱ", "<font color=red>��ʱϵͳ�Զ�����</font>");
			// System.out.println(taskModel._title+"----"+returnCode);
			if (returnCode.equals(TaskRuntimeConst.SINGLE_LEAVE_STATUS_PROCESSEND)) {// workflow
																						// end
				WorkFlowModel workFlowModel = (WorkFlowModel) WorkFlowCache.getModel(processInstanceModel.getProcessDefinitionId());
				if (workFlowModel._isAutoArchives) { // �Զ��鵵,����idΪ-xxxx��
					new UserTaskExecuteWeb().toArchives(uc, taskInstanceModel.getProcessInstanceId(), -Integer.parseInt(UtilDate.yearFormat(new java.sql.Timestamp(System.currentTimeMillis()))), taskInstanceModel.getId()); // �Զ��鵵,����idΪ-xxxx��
				} else { // �ֹ��鵵
					System.out.println("�ڵ㳬ʱ����ִ�����ѣ�����[" + taskInstanceModel.getTitle() + "]�������Ĺ�������ָ��Ϊ�ֹ��鵵��ϵͳ�޷��Զ���ɣ���ʱ����ִ��ʧ�ܣ�");
					return;
				}

				// remove this task
				WorkflowTaskEngine.getInstance().closeProcessTaskInstance(uc, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId());
			} else if (returnCode.equals(TaskRuntimeConst.SINGLE_LEAVE_STATUS_ASSIGN)) {// old:open
																						// send
																						// window
				JumpActivityRuleEngine jumpEngine = ProcessRuleEngine.getInstance().jumpActivityRuleEngine(uc, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId());
				int r = jumpEngine.getNextActivityNo();// �����һ�������
				if (r == -1|| r == 9999) {//����9999���жϣ�ֱ�ӹ鵵 modify by zhanghf 2013.03.26
					// HeadMessageModel headMessageModel = (HeadMessageModel)
					// HeadMessageDaoFactory.createHeadMessage().getInstance(id);
					WorkFlowModel workFlowModel = (WorkFlowModel) WorkFlowCache.getModel(processInstanceModel.getProcessDefinitionId());
					if (workFlowModel._isAutoArchives) { // �Զ��鵵,����idΪ-xxxx��
						new UserTaskExecuteWeb().toArchives(uc, taskInstanceModel.getProcessInstanceId(), -Integer.parseInt(UtilDate.yearFormat(new java.sql.Timestamp(System.currentTimeMillis()))), taskInstanceModel.getId()); // �Զ��鵵,����idΪ-xxxx��
					} else { // �ֹ��鵵
						System.out.println("�ڵ㳬ʱ����ִ�о��棺����[" + taskInstanceModel.getTitle() + "]�������Ĺ�������ָ��Ϊ�ֹ��鵵��ϵͳ�޷��Զ���ɣ���ʱ����ִ��ʧ�ܣ�");
						return;
					}
				} else { // ������ת
					WorkFlowStepModel stepModel = (WorkFlowStepModel) WorkFlowStepCache.getModelOfStepNo(processInstanceModel.getProcessDefinitionId(), r);

					// ��һ���ڵ㣬���� �Ǳ�ָ���˹̶������߰���
					// add by jackliu 200408010 �賿3:40
					// Ѱ�ҵ�ǰ���������ڲ��ţ���Ҫ�������������������㣩
					DepartmentModel localDepartmentModel = uc.getDepartmentModel();
					int ownerDepartmentId = taskInstanceModel.getOwnerDepartmentId();

					if (ownerDepartmentId > 0) {
						// ���������ߵĲ���������׷��2��
						// UserModel userModel1 = (UserModel)
						// UserCache.getModel(taskModel._owner);
						// �жϵ�ǰ�������Ƿ�������ǰһ�ڵ�����߲���
						if (UserCache.isExistInDepartment(taskInstanceModel.getOwnerDepartmentId(), uc.getID())) {
							localDepartmentModel = (DepartmentModel) DepartmentCache.getModel(taskInstanceModel.getOwnerDepartmentId());
						} else { // �ϼ�����
							DepartmentModel tmpDepartmentModel = (DepartmentModel) DepartmentCache.getModel(taskInstanceModel.getOwnerDepartmentId());
							// �жϵ�ǰ�������Ƿ�������ǰһ�ڵ�����ߵ��ϼ�����
							if (UserCache.isExistInDepartment(tmpDepartmentModel.getParentDepartmentId(), uc.getID())) {
								localDepartmentModel = (DepartmentModel) DepartmentCache.getModel(tmpDepartmentModel.getParentDepartmentId());
							}
						}
						// modify by jackliu
						// �����ʱ�Ĳ����Ѿ���ɾ���ˣ���ѯ��ǰowner���ڵĲ���
						if (localDepartmentModel == null) {
							localDepartmentModel = (DepartmentModel) DepartmentCache.getModel(((UserModel) UserCache.getModel(taskInstanceModel.getOwner())).getDepartmentId());
						}
					}

					String workMan = stepModel._stepUser;
					Object o = RouteFactory.getInstance(uc, processInstanceModel, localDepartmentModel, ownerDepartmentId, stepModel._routeType);

					if (o != null) { // �ҵ��˴������·�ɷ�ʽ����
						workMan = ((RouteAbst) o).getTargetUserAddress(stepModel, taskInstanceModel.getId());
					} else {
						System.err.println("û���ҵ���ص�·�ɴ�����!");
						return;
						// û���ҵ�·�ɴ����࣬�����κβ���;
					}

					if ((workMan == null) || workMan.equals("")) {
						System.err.println("�ڵ㳬ʱ����ִ�����ѣ�����[" + taskInstanceModel.getTitle() + "]����һ���ڵ���[" + stepModel._stepName + "]�����Ǹýڵ�û��ָ���̶��İ����ߣ�ϵͳ���˰��������͸������̹���Ա��");
						WorkFlowModel workFlowModel = (WorkFlowModel) WorkFlowCache.getModel(processInstanceModel.getProcessDefinitionId());
						workMan = workFlowModel._flowMaster;
						if ((workMan == null) || workMan.equals("")) {
							System.err.println("�ڵ㳬ʱ����ִ�����ѣ�����[" + taskInstanceModel.getTitle() + "]����һ���ڵ���[" + stepModel._stepName + "]�����Ǹýڵ�û��ָ���̶��İ����ߣ�Ҳû�з������̹���Ա��ϵͳ�޷��Զ���ɣ���ʱ����ִ��ʧ�ܣ�");
							return;
						}
					}

					// ���µ�ǰ������⣬���Ϊ��ʪ�����
					DBSql.executeUpdate("update wf_task set title='**��ʱ�Զ�����:" + taskInstanceModel.getTitle() + "' where id=" + taskInstanceModel.getId());

					// insert a new task!
					try {
						int[] taskId = WorkflowTaskEngine.getInstance().createProcessTaskInstance(uc, taskInstanceModel.getProcessInstanceId(), new SynType(stepModel._routePointType), PriorityType.normal, 1, r, workMan, "(��ʱ�Զ�)(" + stepModel._stepName + ")" + processInstanceModel.getTitle(), false, 0);
						WorkflowTaskEngine.getInstance().closeProcessTaskInstance(uc, taskInstanceModel.getProcessInstanceId(), taskInstanceModel.getId());
					} catch (WorkflowException we) {
						we.printStackTrace(System.err);
					}
					return;
				}
			} else if (returnCode.equals("task break")) {
				System.err.println("�ڵ㳬ʱ����ִ�о��棺����[" + taskInstanceModel.getTitle() + "]�������Ĺ������ڵ㱻ָ����RTClass,������ִ��ʱ���������������ϵͳ�޷��Զ���ɣ���ʱ����ִ��ʧ�ܣ�");
				return;
			} else if (returnCode.equals("task end")) {
			} else {
				System.err.println("�ڵ㳬ʱ����ִ�о��棺����[" + taskInstanceModel.getTitle() + "]��ִ��ʱ���ص�״̬���޷�ʶ��" + returnCode);
				return;
			}

		} catch (Exception we) {
			we.printStackTrace(System.err);
		}

	}

	/**
	 * ��ʱ�󣬸���ǰ�����߷��͵����ʼ�
	 * 
	 * @param taskInstanceModel
	 *            ��ʱ����
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
			System.err.println("�������ʼ�ģ��[" + costModel._mailNo + "]���ͳ�ʱ����ʧ��");
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
			// TO ��ȡ�˻����������ַ
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
	 * ��ʱ�󣬸�����������߷��͵����ʼ�
	 * 
	 * @param taskInstanceModel
	 *            ��ʱ����
	 * @param headMessageModel
	 *            ������ʵ��
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
			System.err.println("�������ʼ�ģ��[" + costModel._mailNo + "]���ͳ�ʱ����ʧ��");
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
