package cn.com.akl.shgl.lcqd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
/**
 * ��ʱ����7#����
 * @author luxiangyu
 *
 */
public class Sc7 implements IJob {

	public Sc7() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		//��ѯһ�����ѽ��������̵�����bindid
		 selectLC();
		 
	    
	}
	private void selectLC() {
		Connection conn=DBSql.open();
		Statement stmt=null;
		ResultSet rset=null;
		try {
			stmt=conn.createStatement();
			rset=DBSql.executeQuery(conn, stmt, "select * from BO_AKL_SX_P where ISEND=1 and XMLB='061270' and UPDATEDATE  BETWEEN DATEADD(day,-1,getdate()) AND getdate()");
			while(rset.next()){
				int bindid=rset.getInt("BINDID");
				String sqbm=rset.getString("SQBM");
				//��������
				Hashtable<String,String> record=BOInstanceAPI.getInstance().getBOData("BO_AKL_SX_P", bindid);
				String sjh=record.get("SJH");
				//�ӱ�����
				Hashtable<String,String> recordData=BOInstanceAPI.getInstance().getBOData("BO_AKL_SX_S", bindid);
				String pn=recordData.get("XH");
				String ccpn=recordData.get("CCPN");
				//�ж��ͺ������Ƿ�һ��
				//��ѯ1#��������
				String sl =DBSql.getString("SELECT SL FROM BO_AKL_SAP_JF_CPXX WHERE RXBM='"+sqbm+"' AND XH='"+pn+"' AND SJH='"+sjh+"' and BWLX='1#'", "SL");
				//��ѯʵ������
				String sjsl=selectSl(bindid,pn);
				System.out.println(pn+"iii"+ccpn+"kiiik"+sl+"kiekieeke"+sjsl);
				//���ʵ�ʽ����ͺ�,��Ʒ������1#����һ��
				if(pn.equals(ccpn)&&Integer.parseInt(sl)==Integer.parseInt(sjsl)){
					//����7#����
					String url="/\\10.10.10.70/SendCS/7";//����·��
					createXML(url,record,recordData);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
    //��ѯʵ�����޵�����
	private String selectSl(int bindid, String pn) {
		String sjsl="";
    	Connection conn=DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, "select SUM(SL) as SL from BO_AKL_SX_S "
					+ "WHERE BINDID='"+bindid+"' AND XH='"+pn+"' AND SX='066206' ");
			if(rset!=null){
				while(rset.next()){
					 sjsl=rset.getString("SL");
					 System.out.println("ʵ�������Ƕ���"+sjsl);
					 return sjsl;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}finally{
			DBSql.close(conn, stmt, rset);
		}
		return sjsl;
	}

		//����7#����
		private void createXML(String url, Hashtable<String, String> record, Hashtable<String, String> recordData) {
			Calendar calendar = Calendar.getInstance();
	        Integer y=calendar.get(Calendar.YEAR);//��
	        Integer m=calendar.get(Calendar.MONTH)+1;//��
	        Integer d=calendar.get(Calendar.DATE);//��
	        Integer h=calendar.get(Calendar.HOUR_OF_DAY);//ʱ
	        Integer f=calendar.get(Calendar.MINUTE);//��
		    Integer s=calendar.get(Calendar.SECOND);//��
		    Integer hs=calendar.get(Calendar.MILLISECOND);//����
		    SimpleDateFormat dateFm = new SimpleDateFormat("yyyyMMddHHmmSS"); //��ʽ����ǰϵͳ����
		    String dateTime = dateFm.format(new java.util.Date());
			url=url+"/chinarmabj_SHPCNF_INB_"+dateTime+".xml";//7����·��
			
			try {
				Document doc = DocumentHelper.createDocument();
				Element root = doc.addElement("SHOW_SHIPMENT_003");
				Element cn =root.addElement("CNTROLAREA");
				
				Element bsr =cn.addElement("BSR");
				bsr.addElement("VERB").addText("SHOW");
				bsr.addElement("NOUN").addText("SHIPMENT");
				bsr.addElement("REVISION").addText("003");
				
				Element sender=cn.addElement("SENDER");
				sender.addElement("LOGICALID").addText("M030R");
				sender.addElement("COMPONENT").addText("M030R");
				sender.addElement("TASK").addText("EA");
				sender.addElement("REFERENCEID").addText("211252_54");
				sender.addElement("CONFIRMATION").addText("0");
				sender.addElement("LANGUAGE").addText("EN");
				sender.addElement("CODEPAGE");
				sender.addElement("AUTHID").addText("Prod");
				
				Element dt=cn.addElement("DATETIME").addAttribute("qualifier", "CREATION");
				dt.addElement("YEAR").addText(y.toString());//��
				dt.addElement("MONTH").addText(m.toString());//��
				dt.addElement("DAY").addText(d.toString());//��
				dt.addElement("HOUR").addText(h.toString());//Сʱ
				dt.addElement("MINUTE").addText(f.toString());//24
			    dt.addElement("SECOND").addText(s.toString());//25
				dt.addElement("SUBSECOND").addText(hs.toString());//26
				dt.addElement("TIMEZONE").addText("+0800");//27
				
		
				Element da=root.addElement("DATAAREA");
				Element msg=da.addElement("SHOW_SHIPMENT");
				Element msgdetails=msg.addElement("SHIPMENT");
				String sxdh=record.get("SXDH");//���޵���
				msgdetails.addElement("SHIPPERID").addText(sxdh);//����33
				String sqbm =record.get("SQBM");//��Ȩ����
				msgdetails.addElement("TRACKNUM").addText(sqbm); 
				
				Element dt2=msgdetails.addElement("DATETIME").addAttribute("qualifier", "RECEIPT");
				dt2.addElement("YEAR").addText(y.toString());//��
				dt2.addElement("MONTH").addText(m.toString());//��
				dt2.addElement("DAY").addText(d.toString());//��
				dt2.addElement("HOUR").addText(h.toString());//Сʱ
				dt2.addElement("MINUTE").addText(f.toString());//��
			    dt2.addElement("SECOND").addText(s.toString());//��
				dt2.addElement("SUBSECOND").addText(hs.toString());//����
				dt2.addElement("TIMEZONE").addText("+0800");//ʱ��
				
				Element shipitem=msgdetails.addElement("SHIPITEM");
				Element qt=shipitem.addElement("QUANTITY").addAttribute("qualifier", "ITEM");
				qt.addElement("VALUE").addText("1");
				qt.addElement("NUMOFDEC").addText("0");
				qt.addElement("SIGN").addText("+");
				qt.addElement("UOM").addText("EA");
				String xh = recordData.get("XH");
				shipitem.addElement("ITEM").addText(xh);
				shipitem.addElement("CLIORDNUM").addText(sqbm);
				String hh=recordData.get("SXCPHH");//���޲�Ʒ�к�
				shipitem.addElement("LINENUM").addText(hh);
				shipitem.addElement("MMIORDNUM").addText(sxdh);
				shipitem.addElement("SHIPPERNUM").addText(sqbm);
				shipitem.addElement("MMIINVOICENUM");
				shipitem.addElement("ORIGIN");
				
				Element us=da.addElement("USERAREA");
				Element re=us.addElement("RECORDCOUNT");
				re.addElement("TYPE").addText("RECORDS");
				re.addElement("COUNT").addText("1");
				
				
				OutputFormat format = OutputFormat.createPrettyPrint();
				format.setEncoding("us-ascii");
				format.setIndent(true);
			 
				XMLWriter writer = new XMLWriter(new FileWriter(new File(url)),format);
				doc.normalize();
				writer.write(doc);
				writer.flush();
				writer.close();
	 
				HelloAWSProcessEventBiz.cretatEror("chinarmabj_SHPCNF_INB_"+dateTime+".xml","7#�������ɳɹ�","û��");//������־
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


}
