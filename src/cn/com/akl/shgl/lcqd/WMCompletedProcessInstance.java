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

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowEventClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
/** 
 * ������Ϣ�������̽���������9#����
 * @author luxiangyu
 *
 */
public class WMCompletedProcessInstance extends WorkFlowEventClassA {
	private static int sxbindid;
	private static int jfbindid;
	public WMCompletedProcessInstance() {
		super();
		// TODO Auto-generated constructor stub
	}

	public WMCompletedProcessInstance(UserContext arg0) {
		super(arg0);
		setProvider("³����");
		setVersion("1.0.0");
		setDescription("������Ϣ�������̽�����,����9#����");
	}

	@Override
	public boolean execute() {
		System.out.println("�������������");
		//����ʵ��id
		int bindid=getProcessCWAD().getCurrentProcessInstanceID();
		System.out.println("���bindid��ʲô"+bindid);
		String url="/\\10.10.10.70/SendCS/9";
        //����bindiid������޵�bindid
		selectSXbindid(bindid);
		
		creatXml9(sxbindid,url);
		
	    
		
		
		return false; 
	}
	//����9#����(������)
	private void creatXml91(int bindid, String url) {
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
		url=url+"/chinamabj_ORDER_"+dateTime+".xml(����)";//9����·��
		
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("SYNC_INVENTORY_005");
			Element cn =root.addElement("CNTROLAREA");
			
			Element bsr =cn.addElement("BSR");
			bsr.addElement("VERB").addText("SYNC");
			bsr.addElement("NOUN").addText("INVENTORY");
			bsr.addElement("REVISION").addText("005");
			
			Element sender=cn.addElement("SENDER");
			sender.addElement("LOGICALID").addText("M030");
			sender.addElement("COMPONENT").addText("M030");
			sender.addElement("TASK");
			sender.addElement("REFERENCEID").addText("5155027_2");
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
			dt.addElement("TIMEZONE").addText("+0800");//
            //����bindid��ѯ���޵�����
			Vector<Hashtable<String,String>> vector=BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXJF_S", bindid);
			Connection conn=DBSql.open();
			for(Hashtable<String,String> re:vector){
				String sl=re.get("SL");
				String xh=re.get("XH");
				String xlbm=re.get("SX");
				String hh = re.get("HH");
				Element da = root.addElement("DATAAREA");
	        	Element si = da.addElement("SYNC_INVENTORY");
	        	Element inven  = si.addElement("INVENTORY");
	        	inven.addElement("ACTTYPE").addText("TRANS");
	        	String jfdh=DBSql.getString("select JFDH from BO_AKL_WXJF_P where bindid='"+bindid+"'", "JFDH");
	        	System.out.println("��������"+jfdh);
	        	inven.addElement("TRANSID").addText(jfdh);//���޵���
	        	Element dt1 = inven.addElement("DATETIME").addAttribute("qualifier", "EFFECTIVE");
	        	dt1.addElement("YEAR").addText(y.toString());//��
	        	dt1.addElement("MONTH").addText(m.toString());//��
	        	dt1.addElement("DAY").addText(d.toString());//��
	        	dt1.addElement("HOUR").addText(h.toString());//ʱ
	        	dt1.addElement("MINUTE").addText(f.toString());//��
	        	dt1.addElement("SECOND").addText(s.toString());//��
	        	dt1.addElement("TIMEZONE").addText("+0800");//ʱ��
	        	Element qa=inven.addElement("QUANTITY").addAttribute("QUANTITY", "ITEM");
	        	qa.addElement("VALUE").addText(sl);//����
	        	qa.addElement("SIGH").addText("-");//��
	        	qa.addElement("UOM").addText("EACH");
	        	inven.addElement("ITEM").addText(xh);//�ͺ�
	        	inven.addElement("SITELEVEL").addAttribute("index","1").addText("M030");
	        	inven.addElement("ITEMDESC").addText("SDSDQUAN-128G,Mobile Ultra uSD,48MB/s,C1");
	        	inven.addElement("ITEMSTATUS");
	        	inven.addElement("ORDNUM").addText(" ");
	        	inven.addElement("TRANSACTION").addText("STO");
	        	inven.addElement("RELNUM").addText(hh);//�к�
	        	inven.addElement("REASONCODE");
	        	inven.addElement("REASONDESC");
	        	String sx =DBSql.getString("SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE xlbm='"+xlbm+"'", "XLMC");
	        	inven.addElement("LOCNUM").addText(sx);//����
				
			}
        	DBSql.close(conn,null,null);
        	
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("us-ascii");
			format.setIndent(true);
		 
			XMLWriter writer = new XMLWriter(new FileWriter(new File(url)),format);
			doc.normalize();
			writer.write(doc);
			writer.flush();
			writer.close();
            System.out.println("��ô�������� ��������");
			HelloAWSProcessEventBiz.cretatEror("chinabj_SHPCNF_"+y+m+d+h+f+s+".xml","9#�������ɳɹ�","û��");//������־
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
		
		
	}

	//��ѯ��������bindid
    private int selectJFbindid(int bindid) {
		// TODO Auto-generated method stub
		return 0;
	}

	//�������޵���bindid
	private void selectSXbindid(int bindid) {
		System.out.println("���ڲ�ѯ");
		Connection conn =DBSql.open();
		Statement stmt=null;
		ResultSet rset=null;
		try {
			stmt = conn.createStatement();
			rset=DBSql.executeQuery(conn, stmt, "select * from BO_AKL_SAP_QDXXSP where BINDID='"+bindid+"' and SPJG='ͨ��'");
			if(rset!=null){
				while(rset.next()){
					sxbindid=rset.getInt("SXBINDID");
					jfbindid=rset.getInt("JFBINDID");
				}
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DBSql.close(conn,stmt,rset);
		}
	}
    //����9#����
	private void creatXml9(int bindid,String url) {
		Calendar calendar = Calendar.getInstance();
        Integer y=calendar.get(Calendar.YEAR);//��
        Integer m=calendar.get(Calendar.MONTH)+1;//��
        Integer d=calendar.get(Calendar.DATE);//��
        Integer h=calendar.get(Calendar.HOUR_OF_DAY);//ʱ
        Integer f=calendar.get(Calendar.MINUTE);//��
	    Integer s=calendar.get(Calendar.SECOND);//��
	    Integer hs=calendar.get(Calendar.MILLISECOND);//����
		String urll=url+"/chinabj_SHPCNF_"+y+m+d+h+f+s+".xml(����)";//9����·��
		
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("SYNC_INVENTORY_005");
			Element cn =root.addElement("CNTROLAREA");
			
			Element bsr =cn.addElement("BSR");
			bsr.addElement("VERB").addText("SYNC");
			bsr.addElement("NOUN").addText("INVENTORY");
			bsr.addElement("REVISION").addText("005");
			
			Element sender=cn.addElement("SENDER");
			sender.addElement("LOGICALID").addText("M030");
			sender.addElement("COMPONENT").addText("M030");
			sender.addElement("TASK");
			sender.addElement("REFERENCEID").addText("5155027_2");
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
			dt.addElement("TIMEZONE").addText("+0800");//
            //����bindid��ѯ���޵�����
			Vector<Hashtable<String,String>> vector=BOInstanceAPI.getInstance().getBODatas("BO_AKL_SX_S", bindid);
			Connection conn=DBSql.open();
			for(Hashtable<String,String> re:vector){
				String sl=re.get("SL");
				String xh=re.get("XH");
				String xlbm=re.get("SX");
				String hh=re.get("SXCPHH");
				Element da = root.addElement("DATAAREA");
	        	Element si = da.addElement("SYNC_INVENTORY");
	        	Element inven  = si.addElement("INVENTORY");
	        	inven.addElement("ACTTYPE").addText("TRANS");
	        	String sxdh=DBSql.getString("select SXDH from BO_AKL_SX_P where bindid='"+bindid+"'", "SXDH");
	        	System.out.println("���޵���"+sxdh);
	        	inven.addElement("TRANSID").addText(sxdh);//���޵���
	        	Element dt1 = inven.addElement("DATETIME").addAttribute("qualifier", "EFFECTIVE");
	        	dt1.addElement("YEAR").addText(y.toString());//��
	        	dt1.addElement("MONTH").addText(m.toString());//��
	        	dt1.addElement("DAY").addText(d.toString());//��
	        	dt1.addElement("HOUR").addText(h.toString());//ʱ
	        	dt1.addElement("MINUTE").addText(f.toString());//��
	        	dt1.addElement("SECOND").addText(s.toString());//��
	        	dt1.addElement("TIMEZONE").addText("+0800");//ʱ��
	        	Element qa=inven.addElement("QUANTITY").addAttribute("QUANTITY", "ITEM");
	        	qa.addElement("VALUE").addText(sl);//����
	        	qa.addElement("SIGH").addText("+");//��
	        	qa.addElement("UOM").addText("EACH");
	        	inven.addElement("ITEM").addText(xh);//�ͺ�
	        	inven.addElement("SITELEVEL").addAttribute("index","1").addText("M030");
	        	inven.addElement("ITEMDESC").addText("SDSDQUAN-128G,Mobile Ultra uSD,48MB/s,C1");
	        	inven.addElement("ITEMSTATUS");
	        	inven.addElement("ORDNUM").addText(" ");
	        	inven.addElement("TRANSACTION").addText("STO");
	        	inven.addElement("RELNUM").addText(hh);//�к�
	        	inven.addElement("REASONCODE");
	        	inven.addElement("REASONDESC");
	        	String sx =DBSql.getString("SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE xlbm='"+xlbm+"'", "XLMC");
	        	inven.addElement("LOCNUM").addText(sx);//����
				
			}
        	DBSql.close(conn,null,null);
        	
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("us-ascii");
			format.setIndent(true);
		 
			XMLWriter writer = new XMLWriter(new FileWriter(new File(urll)),format);
			doc.normalize();
			writer.write(doc);
			writer.flush();
			writer.close();
			HelloAWSProcessEventBiz.cretatEror("chinabj_SHPCNF_"+y+m+d+h+f+s+".xml","9#�������ɳɹ�","û��");//������־
			creatXml91(jfbindid,url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
		
	}

}
