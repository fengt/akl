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
 * ��ʱ����8#����
 * @author luxiangyu
 *
 */
public class Sc8 implements IJob {

	public Sc8() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("������");
		String url="/\\10.10.10.70/SendCS/8";
	    Connection conn= DBSql.open();
	    Statement stmt=null;
	    ResultSet rset=null;
	    ResultSet rset1=null;
	    try {
			stmt=conn.createStatement();
			rset=DBSql.executeQuery(conn, stmt, "select * from BO_AKL_WXJF_P where ISEND=1 and XMLB='061270' and UPDATEDATE  BETWEEN DATEADD(day,-1,getdate()) AND getdate()");
			while(rset.next()){
				int bindid=rset.getInt("BINDID");
				String rx=rset.getString("SQBM");
				rset1=DBSql.executeQuery(conn, stmt, "select COUNT(*) sl from (select DISTINCT XH from BO_AKL_WXJF_S where bindid='"+bindid+"') AS a");
				while(rset1.next()){
					int sl=rset1.getInt("sl");
					String sjjfxh="";
					int sjjfsl=0;
					if(sl==1){
						//�����ӱ���Ϣ
						Vector<Hashtable<String,String>> vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXJF_S", bindid);
						//����
						for(Hashtable<String,String> record:vector){
							sjjfxh=record.get("XH");
							System.out.println("�ͺ�------"+sjjfxh);
							String sx=record.get("SX");//����
							if(sx.equals("066208")){
								sjjfsl++;
							}
						}
						System.out.println("զ����");
						//��ʵ�ʵĽ����ͺ�,���� �� 3#���ĵ��ͺ��������бȽ�
						String jfxh=DBSql.getString("select XH from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"' and BWLX='3#' and ZT='��Ч'", "XH");//Ӧ�����ͺ�
						String jfsl=DBSql.getString("select SL from BO_AKL_SAP_JF_CPXX where RXBM='"+rx+"' and BWLX='3#' and ZT='��Ч'", "SL");//Ӧ��������
						System.out.println(jfxh+jfsl);
						if(jfxh.equals(sjjfxh)&&sjjfsl==Integer.parseInt(jfsl)){
							createXML8(url,bindid);
						}
					}
				}
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		
	}
	 //����8#����
		private void createXML8(String url, int bindid) {
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
			url=url+"/chinabj_SHPCNF_"+dateTime+".xml";//7����·��
			
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
				sender.addElement("REFERENCEID").addText("107410_423");
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
				Element ss=da.addElement("SHOW_SHIPMENT");
				Element sh=ss.addElement("SHIPMENT");
				sh.addElement("SHIPPERID").addText("CARTON");
				sh.addElement("PALLETID");
				//����bindid��ѯ���޵���
				String sxdh=DBSql.getString("select SXDH from BO_AKL_WXJF_P where bindid='"+bindid+"'", "SXDH");
				sh.addElement("TRACKNUM").addText(sxdh);
				sh.addElement("BOL").addText(sxdh);
				sh.addElement("UCC128").addText(sxdh);
				
				Element dt2=sh.addElement("DATETIME").addAttribute("qualifier","SHIP");
				dt2.addElement("YEAR").addText(y.toString());//��
				dt2.addElement("MONTH").addText(m.toString());//��
				dt2.addElement("DAY").addText(d.toString());//��
				dt2.addElement("HOUR").addText(h.toString());//Сʱ
				dt2.addElement("MINUTE").addText(f.toString());//��
			    dt2.addElement("SECOND").addText(s.toString());//��
				dt2.addElement("SUBSECOND").addText(hs.toString());//����
				dt2.addElement("TIMEZONE").addText("+0800");//ʱ��
				
				Element pa=sh.addElement("PARTNER");
				pa.addElement("PARTNRTYPE");
				pa.addElement("PARTNRID").addText("UPSN");
				pa.addElement("SVCLEVEL").addText("STD4");
				
				Element qu=sh.addElement("QUANTITY").addAttribute("qualifier", "WEIGHT");
				qu.addElement("VALUE").addText("0");
				qu.addElement("NUMOFDEC").addText("0");
				qu.addElement("SIGH").addText("+");
				qu.addElement("UOM").addText("LB");
				
				Element op=sh.addElement("OPERAMT").addAttribute("qualifier", "FREIGHT").addAttribute("type","T");
				op.addElement("VALUE");
				op.addElement("SIGH");
				op.addElement("CURRENCY");
				
				Element si=sh.addElement("SHIPITEM");
				Element qt=si.addElement("QUANTITY").addAttribute("qualifier", "ITEM");
				qt.addElement("VALUE").addText("1");
				qt.addElement("NUMOFDEC").addText("0");
				qt.addElement("SIGN").addText("+");
				qt.addElement("UOM").addText("EA");
				//����bindid����ͺ�,����,�к�,����,��Ȩ����
				String xh=DBSql.getString("select XH from BO_AKL_WXJF_S", "XH");
				si.addElement("ITEM").addText(xh);
				String sqbm=DBSql.getString("select SQBM from BO_AKL_WXJF_P where bindid='"+bindid+"'", "SQBM");
				si.addElement("CLIORDNUM").addText(sqbm);
				String hh = DBSql.getString("select HH from BO_AKL_WXJF_S where bindid='"+bindid+"'", "HH");
				si.addElement("LINENUM").addText(hh);
				String jfdh=DBSql.getString("select JFDH from BO_AKL_WXJF_P where bindid='"+bindid+"'", "JFDH");
				si.addElement("MMIORDNUM").addText(jfdh);
				si.addElement("SHIPPERNUM").addText(sqbm);
				si.addElement("MMIINVOICENUM");
				si.addElement("ORIGIN");
				
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
	 
				HelloAWSProcessEventBiz.cretatEror("chinabj_SHPCNF_"+dateTime+".xml","8#�������ɳɹ�","û��");//������־
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

}
