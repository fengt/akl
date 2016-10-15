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
import cn.com.akl.u8.entity.CunsignmentEntity;
import cn.com.akl.u8.entity.CunsignmentItemEntity;
import cn.com.akl.u8.util.InterfaceUtil;


/**
 * @author hzy
 *
 */
public class CunsignmentUtil {

	
	/**
	 * @param head
	 * @param body
	 * @return
	 * @throws Exception
	 * @author hzy
	 * @desc 获取BPM签收差异数据
	 */
	public Map<String, Object> CunsignmentData(Hashtable<String,String> head, Vector<Hashtable<String,String>> body)throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		
		CunsignmentEntity cn = new CunsignmentEntity();
		SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");
		//签收日期
		String qsrq = sdf.format(sdf.parse(head.get("CWSHSJ")));
		cn.setId(head.get("CYDDH"));
		cn.setCode(head.get("CYDDH"));
		cn.setVoucherType("05");
		cn.setSaleType("1");
		cn.setDate(qsrq);
		//TODO 部门、客户编号
		cn.setDeptCode("020201");
		cn.setCustCode(head.get("KHBM"));
		cn.setCurrencyName("人民币");
		cn.setCurrencyRate("1");
		cn.setTaxRate("17");
		cn.setBeginFlag("0");
		cn.setReturnFlag("1");
		cn.setRemark("退货");
		cn.setDefine1(head.get("CKDH"));
		cn.setDefine2(head.get("DDH"));
		cn.setMarker(head.get("CREATEUSER"));
		cn.setSaleConsFlag("0");
		cn.setRetailCustName(head.get("SHDW"));
		cn.setOperationType("普通销售");
		cn.setVerifyDate(qsrq);
		cn.setCverifier(head.get("CREATEUSER"));
		cn.setBcredit("否");
		List<CunsignmentItemEntity> cieList = new ArrayList<CunsignmentItemEntity>();
		for(Hashtable<String,String> ht : body){
			int num = Integer.parseInt(ht.get("CYSL"));
			if(num <1)
				continue;
			CunsignmentItemEntity cie = new CunsignmentItemEntity();
			cie.setHeadId(head.get("CYDDH"));
			//TODO 库房
			cie.setWareHouseCode(ht.get("FHKFDM"));
			//15-03-31更改 00100712 的物料编号
			String U8flbh =  InterfaceUtil.getU8Number("006",ht.get("WLH"),"");
			String incentoryCode= null == U8flbh || "".equals(U8flbh)|| U8flbh.isEmpty() ? ht.get("WLH") : U8flbh;
			cie.setInventoryCode(incentoryCode);
			String quantity = (new BigDecimal(ht.get("CYSL")).multiply(new BigDecimal("-1"))).toString();
			cie.setQuantity(quantity);
			cie.setCcomunitCode("01");
			cie.setCinvmUnit("片");
			cie.setQuotedPrice(ht.get("XSDJ"));
			//TODO 税率
			String shuil = (ht.get("SHUIL").isEmpty() || "0".equals(Double.parseDouble(ht.get("SHUIL")))? "0.17" : ht.get("SHUIL"));
			String price = (new BigDecimal(ht.get("XSDJ")).divide(new BigDecimal("1").add(new BigDecimal(shuil)),6,BigDecimal.ROUND_HALF_UP)).toString();
			cie.setPrice(price);
			cie.setTaxPrice(ht.get("XSDJ"));
			String money  = (new BigDecimal(price).multiply(new BigDecimal(quantity))).toString();
			cie.setMoney(money);
			String tax = (new BigDecimal(money).multiply(new BigDecimal(shuil))).toString();
			cie.setTax(tax);
			String sum = (new BigDecimal(ht.get("XSDJ")).multiply(new BigDecimal(quantity))).toString();
			cie.setSum(sum);
			cie.setDisCount("0");
			cie.setNatPrice(price);
			cie.setNatMoney(money);
			cie.setNatTax(tax);
			cie.setNatSum(sum);
			cie.setNatDiscount("0");
			cie.setBackFlag("正常");
			cie.setBackQuantity(quantity);
			cie.setDisCount1("100");
			cie.setDisCount2("100");
			cie.setInventoryPrintName(ht.get("CPMC"));
			String taxRate = (new BigDecimal(shuil).multiply(new BigDecimal("100"))).toString();
			cie.setTaxRate(taxRate);
			cie.setRetailPrice("0");
			cie.setRetailMoney("0");
			cie.setDefine22(ht.get("KHCGDH"));
			cie.setBqaneedCheck("否");
			cie.setBqaurgency("否");
			cie.setIrowno("1");
			cie.setExpiratDateCalcu("0");
			cie.setBsalePrice("1");
			cie.setBgift("0");
			cie.setFcusminPrice("0");
			cieList.add(cie);
		}
		map.put("head", cn);
		map.put("body", cieList);
		return map;
	}
	
	/**
	 * @param map
	 * @return
	 * @throws Exception
	 * @author hzy
	 * @desc 封装u8发货单xml
	 */
	@SuppressWarnings("unchecked")
	public String cunsignmentXML(Map<String, Object> map)throws Exception{
		
		CunsignmentEntity ce  = (CunsignmentEntity) map.get("head");
		
		List<CunsignmentItemEntity> cieList = (List<CunsignmentItemEntity>) map.get("body");
		
		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+"<ufinterface sender=\"222\" receiver=\"u8\" roottag=\"consignment\" docid=\"\" proc=\"add\" codeexchanged=\"N\" exportneedexch=\"N\" display=\"销售发货单\" family=\"销售管理\">"
			+"<consignment>"
			    +"<header>"
			      +"<id></id>"
			      +"<outid/>"
			      +"<code>"+ce.getCode()+"</code>"
			      +"<vouchertype>"+ce.getVoucherType()+"</vouchertype>"
			      +"<saletype>"+ce.getSaleType()+"</saletype>"
			      +"<date>"+ce.getDate()+"</date>"
			      +"<deptcode>"+ce.getDeptCode()+"</deptcode>"
			      +"<personcode/>"
			      +"<custcode>"+ce.getCustCode()+"</custcode>"
			      +"<paycondition_code/>"
			      +"<shippingchoice/>"
			      +"<address/>"
			      +"<currency_name>"+ce.getCurrencyName()+"</currency_name>"
			      +"<currency_rate>"+ce.getCurrencyRate()+"</currency_rate>"
			      +"<taxrate>"+ce.getTaxRate()+"</taxrate>"
			      +"<beginflag>"+ce.getBeginFlag()+"</beginflag>"
			      +"<returnflag>"+ce.getReturnFlag()+"</returnflag>"
			      +"<remark>"+ce.getRemark()+"</remark>"
			      +"<define1>"+ce.getDefine1()+"</define1>"
			      +"<define2>"+ce.getDefine2()+"</define2>"
			      +"<define3></define3>"
			      +"<define4/>"
			      +"<define5/>"
			      +"<define6/>"
			      +"<define7/>"
			      +"<define8/>"
			      +"<define9/>"
			      +"<define10/>"
			      +"<define11/>"
			      +"<define12/>"
			      +"<define13/>"
			      +"<define14/>"
			      +"<define15/>"
			      +"<define16/>"
			      +"<maker>"+ce.getMarker()+"</maker>"
			      +"<sale_cons_flag>"+ce.getSaleConsFlag()+"</sale_cons_flag>"
			      +"<retail_custname>"+ce.getRetailCustName()+"</retail_custname>"
			      +"<operation_type>"+ce.getOperationType()+"</operation_type>"
			      +"<verifydate>"+ce.getVerifyDate()+"</verifydate>"
			      +"<modifydate/>"
			      +"<caddcode/>"
			      +"<cverifier>"+ce.getCverifier()+"</cverifier>"
			      +"<cdeliverunit/>"
			      +"<cdeliveradd/>"
			      +"<ccontactname/>"
			      +"<cofficephone/>"
			      +"<cmobilephone/>"
			      +"<cgatheringplan/>"
			      +"<dcreditstart/>"
			      +"<icreditdays/>"
			      +"<dgatheringdate/>"
			      +"<bcredit>"+ce.getBcredit()+"</bcredit>"
			      +"<cbooktype/>"
			      +"<cbookdepcode/>"
			      +"<ccuspersoncode/>"
			      +"<ccusperson/>"
			    +"</header>"
			    +"<body>");
		for(CunsignmentItemEntity cie : cieList){
			      xml.append("<entry>"
			        +"<headid></headid>"
			        +"<body_outid/>"
			        +"<warehouse_code>"+cie.getWareHouseCode()+"</warehouse_code>"
			        +"<inventory_code>"+cie.getInventoryCode()+"</inventory_code>"
			        +"<quantity>"+cie.getQuantity()+"</quantity>"
			        +"<num/>"
			        +"<ccomunitcode>"+cie.getCcomunitCode()+"</ccomunitcode>"
			        +"<cinvm_unit>"+cie.getCinvmUnit()+"</cinvm_unit>"
			        +"<cinva_unit/>"
			        +"<quotedprice>"+cie.getQuotedPrice()+"</quotedprice>"
			        +"<price>"+cie.getPrice()+"</price>"
			        +"<taxprice>"+cie.getTaxPrice()+"</taxprice>"
			        +"<money>"+cie.getMoney()+"</money>"
			        +"<tax>"+cie.getTax()+"</tax>"
			        +"<sum>"+cie.getSum()+"</sum>"
			        +"<discount>"+cie.getDisCount()+"</discount>"
			        +"<natprice>"+cie.getNatPrice()+"</natprice>"
			        +"<natmoney>"+cie.getNatMoney()+"</natmoney>"
			        +"<nattax>"+cie.getNatTax()+"</nattax>"
			        +"<natsum>"+cie.getNatSum()+"</natsum>"
			        +"<natdiscount>"+cie.getNatDiscount()+"</natdiscount>"
			        +"<batch/>"
			        +"<remark/>"
			        +"<backflag>"+cie.getBackFlag()+"</backflag>"
			        +"<overdate/>"
			        +"<backquantity>"+cie.getBackQuantity()+"</backquantity>"
			        +"<backnum/>"
			        +"<discount1>"+cie.getDisCount1()+"</discount1>"
			        +"<discount2>"+cie.getDisCount2()+"</discount2>"
			        +"<inventory_printname>"+cie.getInventoryPrintName()+"</inventory_printname>"
			        +"<taxrate>"+cie.getTaxRate()+"</taxrate>"
			        +"<item_class/>"
			        +"<item_classname/>"
			        +"<item_code/>"
			        +"<item_name/>"
			        +"<retail_price>"+cie.getRetailPrice()+"</retail_price>"
			        +"<retail_money>"+cie.getRetailMoney()+"</retail_money>"
			        +"<vendor_name/>"
			        +"<unitrate/>"
			        +"<unit_code/>"
			        +"<free1/>"
			        +"<free2/>"
			        +"<free3/>"
			        +"<free4/>"
			        +"<free5/>"
			        +"<free6/>"
			        +"<free7/>"
			        +"<free8/>"
			        +"<free9/>"
			        +"<free10/>"
			        +"<define22>"+cie.getDefine22()+"</define22>"
			        +"<define23/>"
			        +"<define24/>"
			        +"<define25/>"
			        +"<define26/>"
			        +"<define27/>"
			        +"<define28/>"
			        +"<define29/>"
			        +"<define30/>"
			        +"<define31/>"
			        +"<define32/>"
			        +"<define33/>"
			        +"<define34/>"
			        +"<define35/>"
			        +"<define36/>"
			        +"<define37/>"
			        +"<batchproperty1/>"
			        +"<batchproperty2/>"
			        +"<batchproperty3/>"
			        +"<batchproperty4/>"
			        +"<batchproperty5/>"
			        +"<batchproperty6/>"
			        +"<batchproperty7/>"
			        +"<batchproperty8/>"
			        +"<batchproperty9/>"
			        +"<batchproperty10/>"
			        +"<ccorcode/>"
			        +"<ccusinvcode/>"
			        +"<ccusinvname/>"
			        +"<ippartseqid/>"
			        +"<ippartqty/>"
			        +"<ippartid/>"
			        +"<bqaneedcheck>"+cie.getBqaneedCheck()+"</bqaneedcheck>"
			        +"<bqaurgency>"+cie.getBqaurgency()+"</bqaurgency>"
			        +"<cmassunit/>"
			        +"<imassdate/>"
			        +"<dmdate/>"
			        +"<cordercode/>"
			        +"<iorderrowno/>"
			        +"<cvmivencode/>"
			        +"<irowno>"+cie.getIrowno()+"</irowno>"
			        +"<ExpirationDate/>"
			        +"<ExpiratDateCalcu>"+cie.getExpiratDateCalcu()+"</ExpiratDateCalcu>"
			        +"<ExpirationItem/>"
			        +"<ReasonCode/>"
			        +"<cposition/>"
			        +"<cbookwhcode/>"
			        +"<retailpredate/>"
			        +"<retailpromode/>"
			        +"<bsaleprice>"+cie.getBsalePrice()+"</bsaleprice>"
			        +"<bgift>"+cie.getBgift()+"</bgift>"
			        +"<fcusminprice>"+cie.getFcusminPrice()+"</fcusminprice>"
			      +"</entry>");
		}
			    xml.append("</body>"
			  +"</consignment>"
			+"</ufinterface>");
		return xml.toString();
	}
}
