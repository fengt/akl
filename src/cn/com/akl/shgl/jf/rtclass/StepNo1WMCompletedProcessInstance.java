package cn.com.akl.shgl.jf.rtclass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import cn.com.akl.shgl.lcqd.HelloAWSProcessEventBiz;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowEventClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;

public class StepNo1WMCompletedProcessInstance extends WorkFlowEventClassA{
	

	public StepNo1WMCompletedProcessInstance() {
		super();
	}

	public StepNo1WMCompletedProcessInstance(UserContext arg0) {
		super(arg0);
		setProvider("³����");
		setVersion("1.0.0");
		setDescription("��ò����¼������");
	}

	@Override
	public boolean execute() {
		//����ʵ��id
		int bindid = getProcessCWAD().getCurrentProcessInstanceID();
		//�������л����Ȩ����
		Hashtable<String,String> recordData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXJF_P", bindid); 
	    String zdr=recordData.get("ZDR");//�Ƶ���
	    //ְ��
	    String zw=getProcessCWAD().getCurrentUserContext().getRoleModel().getRoleName();
		String rx=recordData.get("SQBM");//��Ȩ����
		String jfdh=recordData.get("JFDH");//��������
		String sxdh=recordData.get("SXDH");//���޵���
		String khmc=recordData.get("KHMC");//�ͻ�����
		String sjh=recordData.get("SJH");//�ֻ���
		int sjjfsl=0;
		String sjjfxh="";
		Connection conn=DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, "select COUNT(*) sl from (select DISTINCT XH from BO_AKL_WXJF_S where bindid='"+bindid+"') AS a");
			while(rset.next()){
				int sl=Integer.parseInt(rset.getString("sl"));
				System.out.println(sl+"znme hui��");
				if(sl>1){
					//��¼�����������ʼ�����
					String bz="�򽻸��ͺ���������1,�����´���";
					createCYJL(zdr,zw,sxdh,jfdh,rx,khmc,sjh,bz,sjjfxh,sjjfsl);
				}else if(sl==1){
					//�����ӱ���Ϣ
					Vector<Hashtable<String,String>> vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXJF_S", bindid);
					//����
					for(Hashtable<String,String> record:vector){
						sjjfxh=record.get("XH");
						String sx=record.get("SX");//����
						if(sx.equals("066208")){
							sjjfsl++;
						}
					}
					System.out.println("զ����");
					//��ʵ�ʵĽ����ͺ�,���� �� 3#���ĵ��ͺ��������бȽ�
					String jfxh=DBSql.getString("select XH from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"' and BWLX='3#'", "XH");//Ӧ�����ͺ�
					String jfsl=DBSql.getString("select SL from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"' and BWLX='3#'", "SL");//Ӧ��������
					if(!sjjfxh.equals(jfxh)){
						System.out.println("�ͺŲ�һ��");
						String bz="����ԭ��Ϊʵ�ʽ����ͺ�"+sjjfxh+"��3#�ű��Ľ����ͺ�"+jfxh+"��һ��";
						if(sjjfsl!=Integer.parseInt(jfsl)){
							System.out.println("����Ҳ��һ��");
							bz=bz+"ʵ�ʽ�������"+sjjfsl+"��3#���Ľ�������"+jfsl+"��һ��";
						}
						createCYJL(zdr,zw,sxdh,jfdh,rx,khmc,sjh,bz,sjjfxh,sjjfsl);
					}else if(sjjfsl!=Integer.parseInt(jfsl)){
						String bz ="ʵ�ʽ�������"+sjjfsl+"��3#���Ľ�������"+jfsl+"��һ��";
						createCYJL(zdr,zw,sxdh,jfdh,rx,khmc,sjh,bz,sjjfxh,sjjfsl);
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DBSql.close(conn, stmt, rset);
		}
		return false;
				
	}	


    //��¼������Ϣ
	private void createCYJL(String zdr, String zw, String sxdh, String jfdh,
			String rx, String khmc, String sjh, String bz,String sjjfxh,int sjjfsl) {
		Hashtable<String,String> record = new Hashtable<String,String>();
		record.put("ZDR", zdr);
		record.put("ZW", zw);
		record.put("SXDH", sxdh);
		record.put("JFDH", jfdh);
		record.put("SQBM", rx);
		record.put("KHMC", khmc);
		record.put("SJH", sjh);
		record.put("BZ", bz);
		record.put("SJJFXH", sjjfxh);
		record.put("SJJFSL", String.valueOf(sjjfsl));
		try {
			BOInstanceAPI.getInstance().createBOData("BO_AKL_SAP_JFCYJL", record, "admin");
		} catch (AWSSDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

   
		
	

}
