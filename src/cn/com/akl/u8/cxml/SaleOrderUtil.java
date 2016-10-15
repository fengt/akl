/**
 * 
 */
package cn.com.akl.u8.cxml;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cn.com.akl.u8.entity.SaleOrderEntity;
import cn.com.akl.u8.entity.SaleOrderItemEntity;
import cn.com.akl.u8.util.InterfaceUtil;


/**
 * @author hzy
 *
 */
public class SaleOrderUtil {

	/**
	 * @param head
	 * @param body
	 * @return
	 * @throws GeneralSecurityException
	 * @author hzy
	 * @throws ParseException 
	 * @desc 封装数据
	 */
	public Map<String, Object> saleOrderData(Hashtable<String,String> head,Vector<Hashtable<String,String>> body)throws GeneralSecurityException, ParseException{
		Map<String, Object> map = new HashMap<String, Object>();
		SaleOrderEntity soe = new SaleOrderEntity();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//日期
		String fhrq = sdf.format(sdf.parse(head.get("QSRQ")));
		List<SaleOrderItemEntity> soieList = new ArrayList<SaleOrderItemEntity>();
		soe.setId(head.get("CKDH"));
		soe.setTypeCode("1");
		soe.setDate(fhrq);
		soe.setCode(head.get("CKDH"));
		soe.setCustCode(head.get("KH"));
		//TODO 部门
		soe.setDeptCode("020299");
		soe.setCurrency("人民币");
		soe.setCurrencyRate("1");
		soe.setTaxRate("17");
		soe.setMarker(head.get("CREATEUSER"));
		soe.setBusinessType("普通销售");
		soe.setDisFlag("0");
		soe.setDefine1(head.get("XSDDH"));
		//soe.setDefine2(head.get("KHCGDH"));
		soe.setCusName(head.get("KHMC"));
		soe.setDparedatebt(fhrq);
		soe.setDpremodatebt(fhrq);
		soe.setBmustBook("0");
		for(Hashtable<String,String> ht : body){
			SaleOrderItemEntity soie = new SaleOrderItemEntity();
			soie.setId(head.get("CKDH"));
			//15-03-31更改 00100712 的物料编号
			String U8flbh =  InterfaceUtil.getU8Number("006",ht.get("WLH"),"");
			String incentoryCode= null == U8flbh || "".equals(U8flbh)|| U8flbh.isEmpty() ? ht.get("WLH") : U8flbh;
			
			soie.setInventoryCode(incentoryCode);
			soie.setPrepareDate(fhrq);
			soie.setQuantity(ht.get("SJSL"));
			soie.setQuotedPrice(ht.get("DJ"));
			//TODO 税率
			String shuil = (ht.get("SHUIL").isEmpty() || "0".equals(Double.parseDouble(ht.get("SHUIL")))? "0.17" : ht.get("SHUIL"));
			String unitPrice = (new BigDecimal(ht.get("DJ")).divide((new BigDecimal(shuil).add(new BigDecimal("1"))),6,BigDecimal.ROUND_HALF_UP)).toString();
			soie.setUnitPrice(unitPrice);
			soie.setTaxunitPrice(ht.get("DJ"));
			String money = (new BigDecimal(unitPrice).multiply(new BigDecimal(ht.get("SJSL")))).toString();
			soie.setMoney(money);
			String tax = (new BigDecimal(money).multiply(new BigDecimal(shuil))).toString();
			soie.setTax(tax);
			String sum = (new BigDecimal(ht.get("DJ")).multiply(new BigDecimal(ht.get("SJSL")))).toString();
			soie.setSum(sum);
			soie.setNatunitPrice(unitPrice);
			soie.setNatMoney(money);
			soie.setNatTax(tax);
			soie.setNatSum(sum);
			soie.setMid(head.get("CKDH"));
			soie.setDiscounTrade("100");
			soie.setDiscounTrade2("100");
			String taxRate = (new BigDecimal(shuil).multiply(new BigDecimal(100))).toString();
			soie.setTaxRate(taxRate);
			soie.setDfine22(head.get("KHCGDH"));
			soie.setIrowno("1");
			soie.setDpreDate(fhrq);
			soie.setDparemoDate(fhrq);
			soie.setBsalePrice("1");
			soie.setBgift("0");
			soieList.add(soie);
		}
		map.put("head", soe);
		map.put("bodyList", soieList);
		return map;
	}
	
	/**
	 * @param map
	 * @return
	 * @throws GeneralSecurityException
	 * @author hzy
	 * @desc 填充xml
	 */
	@SuppressWarnings("unchecked")
	public String saleOrderXML(Map<String, Object> map)throws GeneralSecurityException{
		
		SaleOrderEntity head = (SaleOrderEntity) map.get("head");
		List<SaleOrderItemEntity> bodyList = (List<SaleOrderItemEntity>) map.get("bodyList");
		StringBuffer xml = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
				"<ufinterface sender=\"222\" receiver=\"u8\" roottag=\"saleorder\" docid=\"\" proc=\"add\" codeexchanged=\"N\" exportneedexch=\"N\" display=\"销售订单\" family=\"销售管理\">"+
				  "<saleorder>"+
				    "<header>"+
				      "<id></id>"+
				      "<outid/>"+
				      "<typecode>"+head.getTypeCode()+"</typecode>"+
				      "<date>"+head.getDate()+"</date>"+
				      "<code></code>"+
				      "<custcode>"+head.getCustCode()+"</custcode>"+
				      "<deptcode>"+head.getDeptCode()+"</deptcode>"+
				      "<personcode/>"+
				      "<sendcode/>"+
				      "<sendaddress/>"+
				      "<ccusperson/>"+
				      "<ccuspersoncode/>"+
				      "<paycondition_code/>"+
				      "<currency>"+head.getCurrency()+"</currency>"+
				      "<currencyrate>"+head.getCurrencyRate()+"</currencyrate>"+
				      "<taxrate>"+head.getTaxRate()+"</taxrate>"+
				      "<earnest/>"+
				      "<memo/>"+
				      "<maker>"+head.getMarker()+"</maker>"+
				      "<businesstype>"+head.getBusinessType()+"</businesstype>"+
				      "<disflag>"+head.getDisFlag()+"</disflag>"+
				      "<define1>"+head.getDefine1()+"</define1>"+
				      "<define2>"+head.getCode()+"</define2>"+
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
				      "<cusname>"+head.getCusName()+"</cusname>"+
				      "<caddcode/>"+
				      "<cgatheringplan/>"+
				      "<dpredatebt>"+head.getDpremodatebt()+"</dpredatebt>"+
				      "<dpremodatebt>"+head.getDpremodatebt()+"</dpremodatebt>"+
				      "<bmustbook>"+head.getBmustBook()+"</bmustbook>"+
				      "<fbookratio/>"+
				      "<fbooksum/>"+
				      "<fbooknatsum/>"+
				      "<retaildates/>"+
				    "</header>"+
				    "<body>");
				    for(SaleOrderItemEntity body : bodyList){
				     xml.append("<entry>"+
				        "<id></id>"+
				        "<body_outid/>"+
				        "<inventorycode>"+body.getInventoryCode()+"</inventorycode>"+
				        "<preparedate>"+body.getPrepareDate()+"</preparedate>"+
				        "<quantity>"+body.getQuantity()+"</quantity>"+
				        "<num/>"+
				        "<quotedprice>"+body.getQuotedPrice()+"</quotedprice>"+
				        "<unitprice>"+body.getUnitPrice()+"</unitprice>"+
				        "<taxunitprice>"+body.getTaxunitPrice()+"</taxunitprice>"+
				        "<money>"+body.getMoney()+"</money>"+
				        "<tax>"+body.getTax()+"</tax>"+
				        "<sum>"+body.getSum()+"</sum>"+
				        "<assistantunit/>"+
				        "<discount/>"+
				        "<natunitprice>"+body.getNatunitPrice()+"</natunitprice>"+
				        "<natmoney>"+body.getNatMoney()+"</natmoney>"+
				        "<nattax>"+body.getNatTax()+"</nattax>"+
				        "<natsum>"+body.getNatSum()+"</natsum>"+
				        "<natdiscount/>"+
				        "<memo/>"+
				        "<mid></mid>"+
				        "<discountrate>"+body.getDiscounTrade()+"</discountrate>"+
				        "<discountrate2>"+body.getDiscounTrade2()+"</discountrate2>"+
				        "<taxrate>"+body.getTaxRate()+"</taxrate>"+
				        "<define22>"+body.getDfine22()+"</define22>"+
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
				        "<itemcode/>"+
				        "<item_class/>"+
				        "<itemname/>"+
				        "<itemclass_name/>"+
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
				        "<irowno>"+body.getIrowno()+"</irowno>"+
				        "<unitrate/>"+
				        "<unitcode/>"+
				        "<dreleasedate/>"+
				        "<dpredate>"+body.getDpreDate()+"</dpredate>"+
				        "<dpremodate>"+body.getDparemoDate()+"</dpremodate>"+
				        "<demandtype/>"+
				        "<demandcode/>"+
				        "<demandmemo/>"+
				        "<cdetailsdemandcode/>"+
				        "<cdetailsdemandmemo/>"+
				        "<retailxsquantity/>"+
				        "<retailxsmoney/>"+
				        "<retailkcquantity/>"+
				        "<retailyhmoney/>"+
				        "<ccusinvcode/>"+
				        "<ccusinvname/>"+
				        "<bsaleprice>"+body.getBsalePrice()+"</bsaleprice>"+
				        "<bgift>"+body.getBgift()+"</bgift>"+
				        "<fcusminprice/>"+
				      "</entry>");
				    }
				xml.append(
				    "</body>"+
				  "</saleorder>"+
				"</ufinterface>	"
				);
		
		return xml.toString();
	}
}
