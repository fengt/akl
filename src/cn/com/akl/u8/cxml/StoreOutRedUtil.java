/**
 * 
 */
package cn.com.akl.u8.cxml;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cn.com.akl.u8.entity.StoreOutEntity;
import cn.com.akl.u8.entity.StoreOutItemEntity;
import cn.com.akl.u8.util.InterfaceUtil;


/**
 * @author hzy
 *
 */
public class StoreOutRedUtil {

	/**
	 * @param head
	 * @param body
	 * @return
	 * @throws Exception
	 * @author hzy
	 * @desc 封装网销RMA退货红字数据
	 */
	public Map<String, Object> StoreOutRedData(Hashtable<String, String> head,Vector<Hashtable<String, String>> body)throws Exception{
		SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");
		//
		String qsrq = sdf.format(sdf.parse(head.get("CREATEDATE")));
		Map<String, Object> map = new HashMap<String, Object>();
		
		StoreOutEntity soe = new StoreOutEntity();
		soe.setId(head.get("CYDDH"));
		soe.setReceiveflag("0");
		soe.setVouchtype("09");
		soe.setBusinesstype("其他出库");
		soe.setSource("库存");
		soe.setDate(qsrq);
		soe.setCode(head.get("CYDDH"));
		soe.setReceivecode("9");
		//TODO 部门
		soe.setDepartmentcode("");
		soe.setTemplatenumber("85");
		soe.setHandler(head.get("CREATEUSER"));
		soe.setMaker(head.get("CREATEUSER"));
		soe.setChandler(head.get("CREATEUSER"));
		soe.setAuditdate(qsrq);
		soe.setIscomplement("0");
		List<StoreOutItemEntity> bodyList =  new ArrayList<StoreOutItemEntity>();
		String warehousecode = "";
		for(Hashtable<String, String> ht : body){
			StoreOutItemEntity soie = new StoreOutItemEntity();
			int num = Integer.parseInt(ht.get("CYSL"));
			if(num<1)
				continue;
			soie.setId(head.get("CYDDH"));
			//15-03-31更改 00100712 的物料编号
			String U8flbh =  InterfaceUtil.getU8Number("006",ht.get("WLH"),"");
			String incentoryCode= null == U8flbh || "".equals(U8flbh)|| U8flbh.isEmpty() ? ht.get("WLH") : U8flbh;
			soie.setInventorycode(incentoryCode);
			String quantity = (new BigDecimal(ht.get("CYSL")).multiply(new BigDecimal("-1"))).toString();
			soie.setQuantity(quantity);
			soie.setCmassunitname("0");
			soie.setMemory("京东RMA退货");
			warehousecode = ht.get("FHKFDM");
			bodyList.add(soie);
		}
		//TODO 库房
		soe.setWarehousecode(warehousecode);
		map.put("head", soe);
		map.put("body", bodyList);
		return map;
	}
	
	/**
	 * @param map
	 * @return
	 * @throws Exception
	 * @author hzy
	 * @desc 封装u8其他出库xml
	 */
	@SuppressWarnings("unchecked")
	public String StoreOutRedXML(Map<String, Object> map)throws Exception{
		
		StoreOutEntity head = (StoreOutEntity) map.get("head");
		List<StoreOutItemEntity> bodyList = (List<StoreOutItemEntity>) map.get("body");
		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
		"<ufinterface sender=\"222\" receiver=\"u8\" roottag=\"storeout\" docid=\"\" proc=\"add\" codeexchanged=\"N\" exportneedexch=\"N\" paginate=\"0\" display=\"出库单\" family=\"库存管理\">"+
		"<storeout>"+
			"<header>"+
				"<id></id>"+
				"<receiveflag>"+head.getReceiveflag()+"</receiveflag>"+
				"<vouchtype>"+head.getVouchtype()+"</vouchtype>"+
				"<businesstype>"+head.getBusinesstype()+"</businesstype>"+
				"<source>"+head.getSource()+"</source>"+
				"<businesscode/>"+
				"<warehousecode>"+head.getWarehousecode()+"</warehousecode>"+
				"<date>"+head.getDate()+"</date>"+
				"<code>"+head.getCode()+"</code>"+
				"<receivecode>"+head.getReceivecode()+"</receivecode>"+
				"<departmentcode>"+head.getDepartmentcode()+"</departmentcode>"+
				"<personcode/>"+
				"<purchasetypecode/>"+
				"<saletypecode/>"+
				"<customercode/>"+
				"<customerccode></customerccode>"+
				"<cacauthcode></cacauthcode>"+
				"<vendorcode/>"+
				"<ordercode/>"+
				"<quantity/>"+
				"<arrivecode/>"+
				"<billcode/>"+
				"<consignmentcode/>"+
				"<arrivedate/>"+
				"<checkcode/>"+
				"<checkdate/>"+
				"<checkperson/>"+
				"<templatenumber>"+head.getTemplatenumber()+"</templatenumber>"+
				"<serial/>"+
				"<handler>"+head.getHandler()+"</handler>"+
				"<memory/>"+
				"<maker>"+head.getMaker()+"</maker>"+
				"<chandler>"+head.getChandler()+"</chandler>"+
				"<define1></define1>"+
				"<define2/>"+
				"<define3/>"+
				"<define4/>"+
				"<define5/>"+
				"<define6/>"+
				"<define7/>"+
				"<define8/>"+
				"<define9/>"+
				"<define10/>"+
				"<define11/>"+
				"<define12/>"+
				"<define13/>"+
				"<define14/>"+
				"<define15/>"+
				"<define16/>"+
				"<auditdate>"+head.getAuditdate()+"</auditdate>"+
				"<taxrate/>"+
				"<exchname></exchname>"+
				"<exchrate/>"+
				"<discounttaxtype/>"+
				"<contact/>"+
				"<phone/>"+
				"<mobile/>"+
				"<address/>"+
				"<conphone/>"+
				"<conmobile/>"+
				"<deliverunit/>"+
				"<contactname/>"+
				"<officephone/>"+
				"<mobilephone/>"+
				"<psnophone/>"+
				"<psnmobilephone/>"+
				"<shipaddress/>"+
				"<addcode/>"+
				"<iscomplement>"+head.getIscomplement()+"</iscomplement>"+
			"</header>"+
			"<body>");
		for(StoreOutItemEntity body : bodyList){
				xml.append("<entry>"+
					"<id></id>"+
					"<barcode/>"+
					"<inventorycode>"+body.getInventorycode()+"</inventorycode>"+
					"<free1/>"+
					"<free2/>"+
					"<free3/>"+
					"<free4/>"+
					"<free5/>"+
					"<free6/>"+
					"<free7/>"+
					"<free8/>"+
					"<free9/>"+
					"<free10/>"+
					"<shouldquantity/>"+
					"<shouldnumber/>"+
					"<quantity>"+body.getQuantity()+"</quantity>"+
					"<cmassunitname>"+body.getCmassunitname()+"</cmassunitname>"+
					"<assitantunit/>"+
					"<assitantunitname/>"+
					"<irate/>"+
					"<number/>"+
					"<price/>"+
					"<cost/>"+
					"<plancost/>"+
					"<planprice/>"+
					"<serial/>"+
					"<makedate/>"+
					"<validdate/>"+
					"<transitionid/>"+
					"<subbillcode/>"+
					"<subpurchaseid/>"+
					"<position/>"+
					"<itemclasscode/>"+
					"<itemclassname/>"+
					"<itemcode/>"+
					"<itemname/>"+
					"<define22/>"+
					"<define23/>"+
					"<define24/>"+
					"<define25/>"+
					"<define26/>"+
					"<define27/>"+
					"<define28/>"+
					"<define29/>"+
					"<define30/>"+
					"<define31/>"+
					"<define32/>"+
					"<define33/>"+
					"<define34/>"+
					"<define35/>"+
					"<define36/>"+
					"<define37/>"+
					"<subconsignmentid/>"+
					"<delegateconsignmentid/>"+
					"<subproducingid/>"+
					"<subcheckid/>"+
					"<cRejectCode/>"+
					"<iRejectIds/>"+
					"<cCheckPersonCode/>"+
					"<dCheckDate/>"+
					"<cCheckCode/>"+
					"<iMassDate/>"+
					"<ioritaxcost/>"+
					"<ioricost/>"+
					"<iorimoney/>"+
					"<ioritaxprice/>"+
					"<iorisum/>"+
					"<taxrate/>"+
					"<taxprice/>"+
					"<isum/>"+
					"<massunit/>"+
					"<vmivencode/>"+
					"<whpersoncode/>"+
					"<whpersonname/>"+
					"<batchproperty1/>"+
					"<batchproperty2/>"+
					"<batchproperty3/>"+
					"<batchproperty4/>"+
					"<batchproperty5/>"+
					"<batchproperty6/>"+
					"<batchproperty7/>"+
					"<batchproperty8/>"+
					"<batchproperty9/>"+
					"<batchproperty10/>"+
					"<iexpiratdatecalcu>"+body.getIexpiratdatecalcu()+"</iexpiratdatecalcu>"+
					"<dexpirationdate/>"+
					"<cexpirationdate/>"+
					"<memory>"+body.getMemory()+"</memory>"+
				"</entry>");
		}
			xml.append("</body>"+
		"</storeout>"+
	"</ufinterface>");
		return xml.toString();
	}
}
