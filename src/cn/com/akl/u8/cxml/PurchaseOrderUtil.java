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

import cn.com.akl.u8.entity.PurchaseOrderEntity;
import cn.com.akl.u8.entity.PurchaseOrderItemEntity;
import cn.com.akl.u8.util.InterfaceUtil;



/**
 * @author hzy
 *
 */
public class PurchaseOrderUtil {

	public Map<String, Object> purchaseOrderDate(Hashtable<String, String> head,Vector<Hashtable<String, String>> body)throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		//单头
		PurchaseOrderEntity poe = new PurchaseOrderEntity();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//入库日期
		String rkrq = sdf.format(sdf.parse(head.get("RKRQ")));
		poe.setCode(head.get("RKDH"));
		poe.setDate(rkrq);
		poe.setVendorCode(head.get("GYSBH"));
		poe.setPurchaseTypeCode("1");
		poe.setOperationTypeCode("普通采购");
		poe.setIdiscounttaxType("0");
		poe.setCurrencyName("人民币");
		poe.setCurrencyRate("1");
		poe.setTaxRate("17");
		poe.setTrafficMoney("0");
		poe.setBargain("0");
		poe.setMark(head.get("UPDATEUSER"));
		map.put("head", poe);
		//单身
		List<PurchaseOrderItemEntity> itemList = new ArrayList<PurchaseOrderItemEntity>();
		for(Hashtable<String, String> ht : body){
			PurchaseOrderItemEntity poie = new PurchaseOrderItemEntity();
			//15-03-31更改 00100712 的物料编号
			String U8flbh =  InterfaceUtil.getU8Number("006",ht.get("WLBH"),"");
			String incentoryCode=null == U8flbh || "".equals(U8flbh)|| U8flbh.isEmpty() ? ht.get("WLBH") : U8flbh;
			
			poie.setInventoryCode(incentoryCode);
			poie.setCheckFlag("0");
			poie.setQuantity(ht.get("SSSL"));
			poie.setPrice(ht.get("WSJG"));
			poie.setTaxPrice(ht.get("HSJG"));
			poie.setMoney(ht.get("WSJE"));
			//税额
			String tax = (new BigDecimal(ht.get("HSJE")).subtract(new BigDecimal(ht.get("WSJE")))).toString();
			poie.setTax(tax);
			poie.setSum(ht.get("HSJE"));
			poie.setNatPrice(ht.get("WSJG"));
			poie.setNatMoney(ht.get("WSJE"));
			poie.setNatTax(tax);
			poie.setNatSum(ht.get("HSJE"));
			poie.setTaxRate(ht.get("SL"));
			poie.setArriveDate(rkrq);
			poie.setIvouchrowNo("1");
			poie.setBtaxCost("1");
			poie.setDefine22(ht.get("CGDDH"));
			poie.setDefine23(ht.get("DDH"));
			poie.setDefine24(ht.get("DDHH"));
			poie.setDefine25(ht.get("LYDH"));
			itemList.add(poie);
		}
		map.put("body", itemList);
		return map;
	}
	
	/**
	 * @param map
	 * @return
	 * @throws Exception
	 * @author hzy
	 * @desc 封装xml数据
	 */
	@SuppressWarnings("unchecked")
	public String purchaseOrderXML(Map<String, Object> map)throws Exception{
		PurchaseOrderEntity head = (PurchaseOrderEntity) map.get("head");
		List<PurchaseOrderItemEntity> itemList = (List<PurchaseOrderItemEntity>) map.get("body");
		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
		"<ufinterface sender=\"222\" receiver=\"u8\" roottag=\"purchaseorder\" docid=\"\" proc=\"add\" codeexchanged=\"N\" exportneedexch=\"N\" display=\"采购订单\" family=\"采购管理\">"+
		  "<purchaseorder>"+
		    "<header>"+
		      "<code>"+head.getCode()+"</code>"+
		      "<date>"+head.getDate()+"</date>"+
		      "<vendorcode>"+head.getVendorCode()+"</vendorcode>"+
		      "<deptcode/>"+
		      "<personcode/>"+
		      "<purchase_type_code>1</purchase_type_code>"+
		      "<operation_type_code>普通采购</operation_type_code>"+
		      "<address/>"+
		      "<idiscounttaxtype>0</idiscounttaxtype>"+
		      "<recsend_type/>"+
		      "<currency_name>人民币</currency_name>"+
		      "<currency_rate>1</currency_rate>"+
		      "<tax_rate>17</tax_rate>"+
		      "<paycondition_code/>"+
		      "<traffic_money>0</traffic_money>"+
		      "<bargain>0</bargain>"+
		      "<remark/>"+
		      "<period/>"+
		      "<maker>"+head.getRemark()+"</maker>"+
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
		      "<cvenpuomprotocol/>"+
		      "<ccontactcode/>"+
		      "<cvenperson/>"+
		      "<cvenbank/>"+
		      "<cvenaccount/>"+
		    "</header>"+
		    "<body>");
		for(PurchaseOrderItemEntity body : itemList){
		xml.append(
		      "<entry>"+
		        "<inventorycode>"+body.getInventoryCode()+"</inventorycode>"+
		        "<checkflag>"+body.getCheckFlag()+"</checkflag>"+
		        "<unitcode/>"+
		        "<quantity>"+body.getQuantity()+"</quantity>"+
		        "<num/>"+
		        "<quotedprice/>"+
		        "<price>"+body.getPrice()+"</price>"+
		        "<taxprice>"+body.getTaxPrice()+"</taxprice>"+
		        "<money>"+body.getMoney()+"</money>"+
		        "<tax>"+body.getTax()+"</tax>"+
		        "<sum>"+body.getSum()+"</sum>"+
		        "<discount/>"+
		        "<natprice>"+body.getNatPrice()+"</natprice>"+
		        "<natmoney>"+body.getNatMoney()+"</natmoney>"+
		        "<assistantunit/>"+
		        "<nattax>"+body.getNatTax()+"</nattax>"+
		        "<natsum>"+body.getNatSum()+"</natsum>"+
		        "<natdiscount/>"+
		        "<taxrate>17</taxrate>"+
		        "<item_class/>"+
		        "<item_code/>"+
		        "<item_name/>"+
		        "<arrivedate>"+body.getArriveDate()+"</arrivedate>"+
		        "<define22>"+body.getDefine22()+"</define22>"+
		        "<define23>"+body.getDefine23()+"</define23>"+
		        "<define24>"+body.getDefine24()+"</define24>"+
		        "<define25>"+body.getDefine25()+"</define25>"+
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
		        "<ivouchrowno>1</ivouchrowno>"+
		        "<btaxcost>1</btaxcost>"+
		      "</entry>");
		}
		xml.append(
		    "</body>"+
		  "</purchaseorder>"+
		"</ufinterface>");
		return xml.toString();
	}
}
