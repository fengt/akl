package cn.com.akl.u8.cxml;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import cn.com.akl.u8.entity.VendorEntity;
import cn.com.akl.u8.util.InterfaceUtil;

/**
 * 
 * @author wjj
 *
 */
public class VendorUtil {

	public Map<String,Object> Vendor(Hashtable<String,String> head)throws Exception{
		Map<String,Object> map = new HashMap<String, Object>();
		//List<VendorEntity> vedorList=new ArrayList<VendorEntity>();
		
		//for(Hashtable<String,String> ht : head){
		SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
		String seeddate=sdf.format(sdf.parse(head.get("CREATEDATE")));//创建时间
		String alterdate=sdf.format(sdf.parse(head.get("UPDATEDATE")));//变更时间
		
		VendorEntity ve=new VendorEntity();
		ve.setCode(head.get("GYSBH"));//供应商编码 
		ve.setName(head.get("GYSMC"));//供应商名称
		ve.setAbbrname(head.get("GYSMC"));//供应商简称?
		String gysbm=InterfaceUtil.getU8Number("002",head.get("LBID"),"");
		ve.setSort_code(gysbm);//供应商分类编码
		ve.setSeed_date(seeddate);//创建日期
		ve.setDiscount_rate(head.get("0"));//扣率
		ve.setCredit_amount(head.get("0"));//信用额度
		ve.setCredit_deadline(head.get("0"));//信用期限
		ve.setHead_corp_code(head.get("GYSBH"));//供应商总公司编码
		ve.setAp_rest(head.get("0"));//应付余额
		ve.setLast_tr_money(head.get("0"));//最后交易金额
		ve.setLast_pay_amount(head.get("0"));//最后付款金额
		ve.setTr_frequency(head.get("0"));//使用频度
		ve.setTax_in_price_flag(head.get("1"));//单价是否含税 
		ve.setCreatePerson(head.get("CREATEUSER"));//创建人
		ve.setModifyPerson(head.get("UPDATEUSER"));//变更人
		ve.setModifyDate(alterdate);//变更日期
		ve.setGradeABC(head.get("-1"));//ABC等级
		ve.setLicenceDate(head.get("0"));
		ve.setBusinessDate(head.get("0"));
		ve.setProxyDate(head.get("0"));
		ve.setPassGMP(head.get("0"));
		ve.setBvencargo(head.get("1"));//是否货物
		ve.setBproxyforeign(head.get("0"));//是否委外
		ve.setBvenservice(head.get("1"));//是否服务
		ve.setCvenexch_name(head.get("人民币"));//币种
		ve.setIvengsptype(head.get("0"));//企业类型
		ve.setIvengspauth(head.get("-1"));//GMP/GSP认证情况??
		ve.setBvenoverseas(head.get("0"));//是否国外
		ve.setBvenaccperiodmng(head.get("0"));//账期管理
		ve.setBvenhomebranch(head.get("0"));//是否有分支机构 
		ve.setDvencreatedatetime(alterdate);//?
		ve.setContact(head.get("LXR"));
		ve.setPhone(head.get("LXDH"));
		ve.setEmail(head.get("EMAIL"));
		ve.setAddress(head.get("LXDZ"));
		ve.setSelf_define1(head.get("SSGYSBH"));
		//vedorList.add(ve);
		
		map.put("head",ve);
		return map;
		
	}
	
	public String VendorXml(Map<String,Object> map)throws Exception{
		
		VendorEntity head =(VendorEntity)map.get("head");
		StringBuffer sb= new StringBuffer();
		//VendorEntity ve=(VendorEntity)map.get("head");
		//for(VendorEntity ve : head){
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
		"<ufinterface sender=\"222\" receiver=\"u8\" roottag=\"vendor\" docid=\"\" proc=\"add\" codeexchanged=\"N\" exportneedexch=\"N\" paginate=\"0\" display=\"供应商档案\" family=\"基础档案\">"+ 
	    "<vendor>"+
	    "<code>"+head.getCode()+"</code>"+
	    "<name>"+head.getName()+"</name>"+
	    "<abbrname>"+head.getAbbrname()+"</abbrname>"+
	    "<sort_code>"+head.getSort_code()+"</sort_code>"+
	    "<domain_code/>"+
	    "<industry/>"+
	    "<address>"+head.getAddress()+"</address>"+
	    "<postcode/>"+
	    "<tax_reg_code/>"+
	    "<bank_open/>"+
	    "<bank_acc_number/>"+
	    "<seed_date>"+head.getSeed_date()+"</seed_date>"+
	    "<legal_man/>"+
	    "<phone>"+head.getPhone()+"</phone>"+
	    "<fax/>"+
	    "<email>"+head.getEmail()+"</email>"+
	    "<contact>"+head.getContact()+"</contact>"+
	    "<bp/>"+
	    "<mobile/>"+
	    "<spec_operator/>"+
	    "<discount_rate></discount_rate>"+
	    "<credit_rank/>"+
	    "<credit_amount>0</credit_amount>"+
	    "<credit_deadline>0</credit_deadline>"+
	    "<pay_condition/>"+
	    "<receive_site/>"+
	    "<receive_mode/>"+
	    "<head_corp_code>"+head.getHead_corp_code()+"</head_corp_code>"+
	    "<rec_warehouse/>"+
	    "<super_dept/>"+
	    "<ap_rest>0</ap_rest>"+
	    "<last_tr_date/>"+
	    "<last_tr_money>0</last_tr_money>"+
	    "<last_pay_date/>"+
	    "<last_pay_amount>0</last_pay_amount>"+
	    "<end_date/>"+
	    "<tr_frequency>0</tr_frequency>"+
	    "<tax_in_price_flag>1</tax_in_price_flag>"+
	    "<CreatePerson>"+head.getCreatePerson()+"</CreatePerson>"+
	    "<ModifyPerson>"+head.getModifyPerson()+"</ModifyPerson>"+
	    "<ModifyDate>"+head.getModifyDate()+"</ModifyDate>"+
	    "<auth_class/>"+
	    "<barcode/>"+
	    "<self_define1>"+head.getSelf_define1()+"</self_define1>"+
	    "<self_define2/>"+
	    "<self_define3/>"+
	    "<self_define4/>"+
	    "<self_define5/>"+
	    "<self_define6/>"+
	    "<self_define7/>"+
	    "<self_define8/>"+
	    "<self_define9/>"+
	    "<self_define10/>"+
	    "<self_define11/>"+
	    "<self_define12/>"+
	    "<self_define13/>"+
	    "<self_define14/>"+
	    "<self_define15/>"+
	    "<self_define16/>"+
	    "<RegistFund/>"+
	    "<EmployeeNum/>"+
	    "<GradeABC>-1</GradeABC>"+
	    "<Memo/>"+
	    "<LicenceDate>0</LicenceDate>"+
	    "<LicenceSDate/>"+
	    "<LicenceEDate/>"+
	    "<LicenceADays/>"+
	    "<BusinessDate>0</BusinessDate>"+
	    "<BusinessSDate/>"+
	    "<BusinessEDate/>"+
	    "<BusinessADays/>"+
	    "<ProxyDate>0</ProxyDate>"+
	    "<ProxySDate/>"+
	    "<ProxyEDate/>"+
	    "<ProxyADays/>"+
	    "<PassGMP>0</PassGMP>"+
	    "<bvencargo>1</bvencargo>"+
	    "<bproxyforeign>0</bproxyforeign>"+
	    "<bvenservice>1</bvenservice>"+
	    "<cVenTradeCCode/>"+
	    "<cvenbankcode/>"+
	    "<cRelCustomer/>"+
	    "<cvenexch_name>人民币</cvenexch_name>"+
	    "<ivengsptype>0</ivengsptype>"+
	    "<ivengspauth>-1</ivengspauth>"+
	    "<cvengspauthno/>"+
	    "<cvenbusinessno/>"+
	    "<cvenlicenceno/>"+
	    "<bvenoverseas>0</bvenoverseas>"+
	    "<bvenaccperiodmng>0</bvenaccperiodmng>"+
	    "<cvenpuomprotocol/>"+
	    "<cvenotherprotocol/>"+
	    "<cvencountrycode/>"+
	    "<cvenenname/>"+
	    "<cvenenaddr1/>"+
	    "<cvenenaddr2/>"+
	    "<cvenenaddr3/>"+
	    "<cvenenaddr4/>"+
	    "<cvenportcode/>"+
	    "<cvenprimaryven/>"+
	    "<fvencommisionrate/>"+
	    "<fveninsuerate/>"+
	    "<bvenhomebranch>0</bvenhomebranch>"+
	    "<cvenbranchaddr/>"+
	    "<cvenbranchphone/>"+
	    "<cvenbranchperson/>"+
	    "<cvensscode/>"+
	    "<comwhcode/>"+
	    "<cvencmprotocol/>"+
	    "<cvenimprotocol/>"+
	    "<iventaxrate/>"+
	    "<dvencreatedatetime>"+head.getDvencreatedatetime()+"</dvencreatedatetime>"+
	    "<cVenMnemCode/>"+
	    "<cvenbankall/>"+
	 	"</vendor>"+
	  	"</ufinterface>");
		return sb.toString();
		
	}
	
	
	
	
}
