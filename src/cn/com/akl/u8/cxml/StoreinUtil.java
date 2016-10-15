package cn.com.akl.u8.cxml;


public class StoreinUtil {

	
	public static String storeinXML()throws Exception{
		String str = "";
		str = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
		+"<ufinterface sender=\"999\" receiver=\"u8\" roottag=\"storein\" docid=\"345821559\" proc=\"add\" codeexchanged=\"N\" exportneedexch=\"N\" display=\"入库单\" family=\"库存管理\" timestamp=\"0x00000000001D47D5\">"
		  +"<storein>"
		    +"<header>"
		      +"<id>1</id>"
		      +"<receiveflag>1</receiveflag>"
		      +"<vouchtype>01</vouchtype>"
		      +"<businesstype>普通采购</businesstype>"
		      +"<source>库存</source>"
		      +"<businesscode />"
		      +"<warehousecode>11</warehousecode>"
		      +"<date>2014-7-28</date>"
		      +"<code>0000000001</code>"
		      +"<receivecode />"
		      +"<departmentcode />"
		      +"<personcode />"
		      +"<purchasetypecode />"
		      +"<saletypecode />"
		      +"<customercode />"
		      +"<vendorcode>DJK12213</vendorcode>"
		      +"<ordercode />"
		      +"<quantity />"
		      +"<arrivecode />"
		      +"<billcode />"
		      +"<consignmentcode />"
		      +"<arrivedate />"
		      +"<checkcode />"
		      +"<checkdate />"
		      +"<checkperson />"
		      +"<templatenumber>27</templatenumber>"
		      +"<serial />"
		      +"<handler>demo</handler>"
		      +"<memory />"
		      +"<maker>demo</maker>"
		      +"<chandler>demo</chandler>"
		      +"<define1 />"
		      +"<define2 />"
		      +"<define3 />"
		      +"<define4 />"
		      +"<define5 />"
		      +"<define6 />"
		      +"<define7>0</define7>"
		      +"<define8 />"
		      +"<define9 />"
		      +"<define10 />"
		      +"<define11 />"
		      +"<define12 />"
		      +"<define13 />"
		      +"<define14 />"
		      +"<define15 />"
		      +"<define16 />"
		      +"<auditdate>2014-7-28</auditdate>"
		      +"<taxrate />"
		      +"<exchname>人民币</exchname>"
		      +"<exchrate>1</exchrate>"
		      +"<contact />"
		      +"<phone />"
		      +"<mobile />"
		      +"<address />"
		      +"<conphone />"
		      +"<conmobile />"
		      +"<deliverunit />"
		      +"<contactname />"
		      +"<officephone />"
		      +"<mobilephone />"
		      +"<psnophone />"
		      +"<psnmobilephone />"
		      +"<shipaddress />"
		      +"<addcode />"
		    +"</header>"
		    +"<body>"
		      +"<entry>"
		        +"<id>1</id>"
		        +"<autoid>1</autoid>"
		        +"<barcode />"
		        +"<inventorycode>11111</inventorycode>"
		        +"<free1 />"
		        +"<free2 />"
		        +"<free3 />"
		        +"<free4 />"
		        +"<free5 />"
		        +"<free6 />"
		        +"<free7 />"
		        +"<free8 />"
		        +"<free9 />"
		        +"<free10 />"
		        +"<shouldquantity />"
		        +"<shouldnumber />"
		        +"<quantity>150</quantity>"
		        +"<cmassunitname>公斤</cmassunitname>"
		        +"<assitantunit />"
		        +"<assitantunitname />"
		        +"<irate />"
		        +"<number />"
		        +"<price>100</price>"
		        +"<cost>15000</cost>"
		        +"<plancost />"
		        +"<planprice />"
		        +"<serial />"
		        +"<makedate />"
		        +"<validdate />"
		        +"<transitionid />"
		        +"<subbillcode />"
		        +"<subpurchaseid />"
		        +"<position>"
		        +"</position>"
		        +"<itemclasscode />"
		        +"<itemclassname />"
		        +"<itemcode />"
		        +"<itemname />"
		        +"<define22 />"
		        +"<define23 />"
		        +"<define24 />"
		        +"<define25 />"
		        +"<define26 />"
		        +"<define27 />"
		        +"<define28 />"
		        +"<define29 />"
		        +"<define30 />"
		        +"<define31 />"
		        +"<define32 />"
		        +"<define33 />"
		        +"<define34 />"
		        +"<define35 />"
		        +"<define36 />"
		        +"<define37 />"
		        +"<subconsignmentid />"
		        +"<delegateconsignmentid />"
		        +"<subproducingid />"
		        +"<subcheckid />"
		        +"<cRejectCode />"
		        +"<iRejectIds />"
		        +"<cCheckPersonCode />"
		        +"<dCheckDate />"
		        +"<cCheckCode />"
		        +"<iMassDate />"
		        +"<ioritaxcost>117</ioritaxcost>"
		        +"<ioricost>100</ioricost>"
		        +"<iorimoney>15000</iorimoney>"
		        +"<ioritaxprice>2550</ioritaxprice>"
		        +"<iorisum>17550</iorisum>"
		        +"<taxrate>17</taxrate>"
		        +"<taxprice>2550</taxprice>"
		        +"<isum>17550</isum>"
		        +"<massunit />"
		      +"</entry>"
		    +"</body>"
		  +"</storein>"
		  +"</ufinterface>";
		return str;
		
	}
}
