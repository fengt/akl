package cn.com.akl.u8.cxml;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


import cn.com.akl.u8.entity.CustomerEntity;
import cn.com.akl.u8.util.InterfaceUtil;

/**
 * 
 * @author wjj
 * 
 */
public class CustomerUtil {
	public Map<String, Object> Customer(Hashtable<String, String> head) throws Exception {
		//Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		//Map<String, String> khlb = new Hashtable<String, String>();
		//khlb.put("00901", "01");
		//khlb.put("00902", "02");
		//khlb.put("00903", "03");
		
		Map<String, Object> map = new HashMap<String, Object>();
		//List<CustomerEntity> customerList = new ArrayList<CustomerEntity>();
		//for (Hashtable<String, String> ht : head) {
			CustomerEntity ce = new CustomerEntity();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String cjrq = sdf.format(sdf.parse(head.get("CJRQ")));// 创建日期
			String ggrq = sdf.format(sdf.parse(head.get("UPDATEDATE")));// 更改日期

			ce.setCode(head.get("KHID"));// 客戶id
			ce.setName(head.get("NAME"));// 客戶姓名
			ce.setAbbrname(head.get("NAME"));// 客户简称
			//ce.setSort_code(khlb.get(ht.get("LBID")));// 客户分类编码？
			//String u8flbm = DBSql.getString("SELECT U8FLBH FROM BO_AKL_U8_NUMBER_R WHERE BPMFLBH ='"+ht.get("LBID")+"'","U8FLBH");
			String u8flbm=InterfaceUtil.getU8Number("001",head.get("LBID"),"");
			ce.setSort_code(u8flbm);
			ce.setSeed_date(cjrq);// 发展日期?
			ce.setDiscount_rate(head.get("0"));// 扣率?
			ce.setCredit_amount(head.get("XYJE"));// 信用額度
			ce.setCredit_deadline(head.get("0"));// 信用期限?
			ce.setHead_corp_code(head.get("KHID"));// 客户总公司编码?
			ce.setAr_rest(head.get("0"));// 应收余额？YSJE
			ce.setLast_tr_amount(head.get("0"));// 最后交易日期?
			ce.setLast_rec_amount(head.get("0"));// 最后收款金额?
			ce.setTr_frequency(head.get("0"));// 使用频度?
			ce.setPricegrade(head.get("0"));// 价格级别?----
			ce.setCreatePerson(head.get("CREATEUSER"));// 建档人
			ce.setModifyPerson(head.get("UPDATEUSER"));// 变更人
			ce.setModifyDate(ggrq);// 变更日期
			ce.setAuth_class(head.get("KHID"));// 所属权限组?
			ce.setInvoiceCompany(head.get("KHID"));// 开票单位?
			ce.setCredit(head.get("0"));// 是否控制信用额度?
			ce.setCreditByHead(head.get("0"));// ?
			ce.setCreditDate(head.get("0"));// 是否控制额度期限?
			ce.setLicenceDate(head.get("0"));// ?
			ce.setBusinessDate(head.get("0"));// ?
			ce.setbLimitSale(head.get("0"));// 是否允限销控制
			ce.setbHomeBranch(head.get("0"));// 是否有分支机构
			ce.setbCusState(head.get("0"));// 是否成交
			ce.setCcusexch_name(head.get("人民币"));// 币种
			ce.setBshop(head.get("0"));// 是否门店----
			ce.setbOnGPinStore(head.get("0"));// ?
			ce.setbOnGPinStore(head.get("0"));// ?
			ce.setBcusdomestic(head.get("1"));// 国内
			ce.setBcusoverseas(head.get("0"));// 国外
			ce.setBserviceattribute(head.get("0"));// ?
			ce.setCcuscreditcompany(head.get("KHID"));// 信用单位 ?
			ce.setCcusmngtypecode(head.get("999"));// ?
			ce.setBrequestsign(head.get("0"));//
			ce.setCcuscode(head.get("KHID"));// ?
			ce.setCinvoicecompany(head.get("KHID"));// ?
			ce.setAutoid(head.get("1"));// ?
			ce.setBdefault(head.get("True"));// ?
			//customerList.add(ce);
			ce.setBank_open(head.get("KHH"));// 開戶行
			ce.setBank_acc_number(head.get("YHZH"));// 銀行賬號
			ce.setContact(head.get("LXR"));// 聯繫人
			ce.setPhone(head.get("LXDH"));// 聯繫電話
			ce.setAddress(head.get("LXDZ"));// 地址
			ce.setEmail(head.get("EMAIL"));// email
			ce.setSelf_define1(head.get("SSKHBH"));
			
		//}
		map.put("head", ce);
		return map;
	}


	/**
	 * 
	 * @param map
	 * @return
	 * @throws Exception
	 * @author wjj
	 */
	//@SuppressWarnings("unchecked")
	public String customerXML(Map<String, Object> map) throws Exception {
		CustomerEntity head = (CustomerEntity) map.get("head");
		StringBuffer sb = new StringBuffer();
		//List<CustomerEntity> customerList = new ArrayList<CustomerEntity>();
		//for(CustomerEntity ce: head){
			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
					+ "<ufinterface sender=\"222\" receiver=\"u8\" roottag=\"customer\" docid=\"\" proc=\"add\" codeexchanged=\"N\" exportneedexch=\"N\" paginate=\"0\" display=\"客户档案\" family=\"基础档案\" >"
					+ "<customer>" 
					+ "<code>"+ head.getCode()+ "</code>"
					+ "<name>"+ head.getName()+ "</name>"
					+ "<abbrname>"+ head.getAbbrname()+ "</abbrname>"
					+ "<cCusMnemCode/>"
					+ "<sort_code>"+ head.getSort_code()+ "</sort_code>"
					+ "<domain_code/>"
					+ "<industry/>"
					+ "<address>"+head.getAddress()+"</address>"
					+ "<postcode/>"
					+ "<tax_reg_code/>"
					+ "<bank_open>"+head.getBank_open()+"</bank_open>"
					+ "<bank_acc_number>"+head.getBank_acc_number()+"</bank_acc_number>"
					+ "<seed_date>"+ head.getSeed_date()+ "</seed_date>"
					+ "<legal_man/>"
					+ "<email>"+head.getEmail()+"</email>"	
					+ "<contact>"+head.getContact()+"</contact>"
					+ "<phone>"+head.getPay_condition()+"</phone>"	
					+ "<fax/>"
					+ "<bp/>"
					+ "<mobile/>"
					+ "<spec_operator/>"
					+ "<discount_rate>0</discount_rate>"
					+ "<credit_rank/>"
					+ "<credit_amount>"+ head.getCredit_amount()+ "</credit_amount>"
					+ "<credit_deadline>0</credit_deadline>"
					+ "<pay_condition/>"
					+ "<devliver_site/>"
					+ "<deliver_mode/>"
					+ "<head_corp_code>"+head.getHead_corp_code()+ "</head_corp_code>"
					+ "<deli_warehouse/>"
					+ "<super_dept/>"
					+ "<ar_rest>0</ar_rest>"
					+ "<last_tr_date/>"
					+ "<last_tr_amount>0</last_tr_amount>"
					+ "<last_rec_date/>"
					+ "<last_rec_amount>0</last_rec_amount>"
					+ "<end_date/>"
					+ "<tr_frequency>0</tr_frequency>"
					+ "<self_define1>"+head.getSelf_define1()+"</self_define1>"
					+ "<self_define2/>"
					+ "<self_define3/>"
					+ "<pricegrade>-1</pricegrade>"
					+ "<CreatePerson>"+ head.getCreatePerson()+ "</CreatePerson>"
					+ "<ModifyPerson>"+ head.getModifyPerson()+ "</ModifyPerson>"
					+ "<ModifyDate>"+ head.getModifyDate()+ "</ModifyDate>"
					+ "<auth_class>"+ head.getAuth_class()+ "</auth_class>"				
					+ "<self_define4/>"
					+ "<self_define5/>"
					+ "<self_define6/>"
					+ "<self_define7/>"
					+ "<self_define8/>"
					+ "<self_define9/>"
					+ "<self_define10/>"
					+ "<self_define11/>"
					+ "<self_define12/>"
					+ "<self_define13/>"
					+ "<self_define14/>"
					+ "<self_define15/>"
					+ "<self_define16/>"
					+ "<InvoiceCompany>"+ head.getInvoiceCompany()+ "</InvoiceCompany>"
					+ "<Credit>0</Credit>"
					+ "<CreditByHead>0</CreditByHead>"
					+ "<CreditDate>0</CreditDate>"
					+ "<LicenceDate>0</LicenceDate>"
					+ "<LicenceSDate/>"
					+ "<LicenceEDate/>"
					+ "<LicenceADays/>"
					+ "<BusinessDate>0</BusinessDate>"
					+ "<BusinessSDate/>"
					+ "<BusinessEDate/>"
					+ "<BusinessADays/>"
					+ "<Proxy>0</Proxy>"
					+ "<ProxySDate/>"
					+ "<ProxyEDate/>"
					+ "<ProxyADays/>"
					+ "<Memo/>"
					+ "<bLimitSale>0</bLimitSale>"
					+ "<cCusCountryCode/>"
					+ "<cCusEnName/>"
					+ "<cCusEnAddr1/>"
					+ "<cCusEnAddr2/>"
					+ "<cCusEnAddr3/>"
					+ "<cCusEnAddr4/>"
					+ "<cCusPortCode/>"
					+ "<cPrimaryVen/>"
					+ "<fCommisionRate/>"
					+ "<fInsueRate/>"
					+ "<bHomeBranch>0</bHomeBranch>"
					+ "<cBranchAddr/>"
					+ "<cBranchPhone/>"
					+ "<cBranchPerson/>"
					+ "<cCusTradeCCode/>"
					+ "<bCusState>0</bCusState>"
					+ "<ccusbankcode/>"
					+ "<cRelVendor/>"
					+ "<ccusexch_name>人民币</ccusexch_name>"
					+ "<bshop>0</bshop>"
					+ "<bOnGPinStore>0</bOnGPinStore>"
					+ "<bcusdomestic>1</bcusdomestic>"
					+ "<bcusoverseas>0</bcusoverseas>"
					+ "<bserviceattribute>0</bserviceattribute>"
					+ "<ccuscreditcompany>"+ head.getCcuscreditcompany()+ "</ccuscreditcompany>"
					+ "<ccussaprotocol/>"
					+ "<ccusexprotocol/>"
					+ "<ccusotherprotocol/>"
					+ "<ccusimagentprotocol/>"
					+ "<fcusdiscountrate/>"
					+ "<ccussscode/>"
					+ "<ccusmngtypecode>999</ccusmngtypecode>"
					+ "<brequestsign>0</brequestsign>"
					+ "<fExpense/>"
					+ "<fApprovedExpense/>"
					+ "<dTouchedTime/>"
					+ "<dRecentlyInvoiceTime/>"
					+ "<dRecentlyQuoteTime/>"
					+ "<dRecentlyActivityTime/>"
					+ "<dRecentlyChanceTime/>"
					+ "<dRecentlyContractTime/>"
					+ "<cLtcCustomerCode/>"
					+ "<bTransFlag/>"
					+ "<cLtcPerson/>"
					+ "<dLtcDate/>"
					+ "<cLocationSite/>"
					+ "<sa_invoicecustomersall>"
					+ "<sa_invoicecustomers>"
					+ "<ccuscode>"+ head.getCcuscode()+ "</ccuscode>"
					+ "<cinvoicecompany>"+ head.getCinvoicecompany()+ "</cinvoicecompany>"
					+ "<autoid>1</autoid>"
					+ "<bdefault>True</bdefault>"
					+ "</sa_invoicecustomers>"
					+ "</sa_invoicecustomersall>"
					+ "</customer>"
					+ "</ufinterface>");		
		//}
		
		return sb.toString();

	}

}
