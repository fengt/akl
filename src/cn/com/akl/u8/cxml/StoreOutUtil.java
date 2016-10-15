package cn.com.akl.u8.cxml;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cn.com.akl.u8.entity.StoreOutEntity;
import cn.com.akl.u8.entity.StoreOutItemEntity;
import cn.com.akl.u8.util.InterfaceUtil;

/**
 * 
 * @author wjj
 * 
 */
public class StoreOutUtil {
	public Map<String, Object> Storeout(Hashtable<String, String> head,
			Vector<Hashtable<String, String>> body) throws Exception {
		Map<String, Object> map = new Hashtable<String, Object>();

		StoreOutEntity soe = new StoreOutEntity();
		List<StoreOutItemEntity> soieList = new ArrayList<StoreOutItemEntity>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String ckrq = sdf.format(sdf.parse(head.get("QSRQ")));// 发货日期=出库日期
		// String cjrq=sdf.format(sdf.parse(head.get("CJSJ")));//创建日期
		// String xdrq=sdf.format(sdf.parse(head.get("XDRQ")));//下单日期

		soe.setId(head.get("RMAFXDH"));
		soe.setReceivecode(head.get("0"));
		soe.setVouchtype(head.get("09"));// 其他出库
		soe.setBusinesstype(head.get("其他出库"));
		soe.setSource(head.get("库存"));
		soe.setWarehousecode(head.get("SH01"));// 仓库编码
		soe.setCode(head.get("CKDH"));// 出库单号！！！！！！！！
		soe.setDate(ckrq);
		soe.setReceivecode(head.get("9"));// 收发类别（返新出库）
		soe.setDepartmentcode(head.get("020299"));// 部门编码----------------
		// soe.setTemplatenumber(head.get(""));//模板号-----------------

		soe.setHandler(head.get("唐小丽"));// 经手人？？？
		soe.setMemory(head.get("BZ"));// 备注
		soe.setChandler(head.get("唐小丽"));//
		soe.setMaker(head.get("CJXM"));// 制单人
		soe.setAuditdate(ckrq);// 审核日期？、出库日期
		soe.setIscomplement(head.get("0"));
		soe.setVendorcode(head.get("KH"));// 客户编码
		// soe.setDefine1(head.get("KHMC"));//客户名称
		// soe.setDefine2(head.get("TJ"));
		// soe.setDefine3(head.get("ZL"));
		// soe.setDefine4(cjrq);
		// soe.setDefine5(xdrq);
		soe.setDefine6(head.get("CK"));// 仓库
		soe.setDefine7(head.get("JHDZ"));// 交货地址
		soe.setDefine8(head.get("CYZT"));
		soe.setConsignmentcode(head.get("XSDDH"));// 销售订单号（发货单号）
		soe.setOrdercode(head.get("KHCGDH"));// 客户采购单号
		soe.setIscomplement(head.get("0"));
		// 单身
		for(Hashtable<String, String> ht : body){
			StoreOutItemEntity soie = new StoreOutItemEntity();
		//15-03-31更改 00100712 的物料编号
			String U8flbh =  InterfaceUtil.getU8Number("006",ht.get("WLH"),"");
			String incentoryCode=null == U8flbh ||"".equals(U8flbh)|| U8flbh.isEmpty() ? ht.get("WLH") : U8flbh;
			soie.setInventorycode(incentoryCode);
			String wldwbh = InterfaceUtil.getU8Number("005", head.get("DW"), "");
			soie.setCmassunitname(wldwbh);// 物料计量单位编码
			soie.setIexpiratdatecalcu(ht.get("0"));
			soie.setQuantity(ht.get("SJSL"));
			soie.setCmassunitname(ht.get("GG"));//规格
			//soie.setShouldquantity(ht.get("SJSL"));// 实际数量。。应发数量
			soie.setUnitcost(ht.get("DJ"));// 单价
			// String unitcost = (new BigDecimal(head.get("DJ")).divide(new
			// BigDecimal(body.get("SHUIL")).add(new BigDecimal("1")))).toString();
			// soie.setUnitcost(unitcost);//(单价*税率)
			String price = (new BigDecimal(ht.get("DJ")).multiply(new BigDecimal(
					ht.get("SJSL")))).toString();
			soie.setPrice(price);// 金额 （单价*数量）
			// soie.setFree1((new BigDecimal(body.get("SHUIL")).multiply(new
			// BigDecimal("100")).toString()));//税率
			// soie.setFree2(body.get("PCH"));
			// soie.setFree3(body.get("SX"));
			// soie.setFree4(body.get("PP"));
			soie.setId(ht.get("ID"));
			soie.setMemory("RMA返新");
			soieList.add(soie);
		}
		map.put("head", soe);
		map.put("body", soieList);
		return map;

	}

	@SuppressWarnings("unchecked")
	public String storeinoutXML(Map<String,Object> map)throws Exception{
		StoreOutEntity storeout=(StoreOutEntity)map.get("head");
		List<StoreOutItemEntity> soieList=(List<StoreOutItemEntity>)map.get("body");
		StringBuffer sb=new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
				"<ufinterface sender=\"222\" receiver=\"u8\" roottag=\"storeout\" docid=\"\" proc=\"add\" codeexchanged=\"N\" exportneedexch=\"N\" paginate=\"0\" display=\"出库单\" family=\"库存管理\">"+
				"<storeout>"+
				"<header>"+
				"<id>"+storeout.getId()+"</id>"+
				"<receiveflag>0</receiveflag>"+
				"<vouchtype>09</vouchtype>"+
				"<businesstype>其他出库</businesstype>"+
				"<source>库存</source>"+
				"<businesscode/>"+
				"<warehousecode>SH01</warehousecode>"+
				"<date>"+storeout.getDate()+"</date>"+
				"<code>"+storeout.getCode()+"</code>"+
				"<receivecode>9</receivecode>"+
				"<departmentcode>020299</departmentcode>"+
				"<personcode/>"+
				"<purchasetypecode/>"+
				"<saletypecode/>"+
				"<customercode/>"+
				"<customerccode/>"+
				"<cacauthcode/>"+
				"<vendorcode>"+storeout.getVendorcode()+"</vendorcode>"+
				"<ordercode>"+storeout.getOrdercode()+"</ordercode>"+
				"<quantity/>"+
				"<arrivecode/>"+
				"<billcode/>"+
				"<consignmentcode>"+storeout.getConsignmentcode()+"</consignmentcode>"+
				"<arrivedate/>"+
				"<checkcode/>"+
				"<checkdate/>"+
				"<checkperson/>"+
				"<templatenumber>85</templatenumber>"+
				"<serial/>"+
				"<handler>唐小立</handler>"+
				"<memory>"+storeout.getMemory()+"</memory>"+
				"<maker>"+storeout.getMaker()+"</maker>"+
				"<chandler>唐小立</chandler>"+
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
				"<auditdate>"+storeout.getAuditdate()+"</auditdate>"+
				"<taxrate/>"+
				"<exchname/>"+
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
				"<iscomplement>0</iscomplement>"+
				"</header>"+
				"<body>");
				for(StoreOutItemEntity storeitemout : soieList){
					sb.append("<entry>"+
					"<id>"+storeitemout.getId()+"</id>"+
					"<barcode/>"+
					"<inventorycode>"+storeitemout.getInventorycode()+"</inventorycode>"+
	//				"<free1>"+storeitemout.getFree1()+"</free1>"+
	//				"<free2>"+storeitemout.getFree2()+"</free2>"+
	//				"<free3>"+storeitemout.getFree3()+"</free3>"+
	//				"<free4>"+storeitemout.getFree4()+"</free4>"+
					"<free5/>"+
					"<free6/>"+
					"<free7/>"+
					"<free8/>"+
					"<free9/>"+
					"<free10/>"+
					"<shouldquantity/>"+
					"<shouldnumber/>"+
					"<quantity>"+storeitemout.getQuantity()+"</quantity>"+
					"<cmassunitname>"+storeitemout.getCmassunitname()+"</cmassunitname>"+
					"<assitantunit/>"+
					"<assitantunitname/>"+
					"<irate/>"+
					"<number/>"+
					"<price>"+storeitemout.getPrice()+"</price>"+
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
					"<iexpiratdatecalcu>0</iexpiratdatecalcu>"+
					"<dexpirationdate/>"+
					"<cexpirationdate/>"+
					"<memory>"+storeitemout.getMemory()+"</memory>"+
					"</entry>");
				}
				sb.append("</body>"+
				"</storeout>"+
				"</ufinterface>"
				);
		return sb.toString();
		
	}
}
