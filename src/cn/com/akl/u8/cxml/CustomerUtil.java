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
			String cjrq = sdf.format(sdf.parse(head.get("CJRQ")));// ��������
			String ggrq = sdf.format(sdf.parse(head.get("UPDATEDATE")));// ��������

			ce.setCode(head.get("KHID"));// �͑�id
			ce.setName(head.get("NAME"));// �͑�����
			ce.setAbbrname(head.get("NAME"));// �ͻ����
			//ce.setSort_code(khlb.get(ht.get("LBID")));// �ͻ�������룿
			//String u8flbm = DBSql.getString("SELECT U8FLBH FROM BO_AKL_U8_NUMBER_R WHERE BPMFLBH ='"+ht.get("LBID")+"'","U8FLBH");
			String u8flbm=InterfaceUtil.getU8Number("001",head.get("LBID"),"");
			ce.setSort_code(u8flbm);
			ce.setSeed_date(cjrq);// ��չ����?
			ce.setDiscount_rate(head.get("0"));// ����?
			ce.setCredit_amount(head.get("XYJE"));// �����~��
			ce.setCredit_deadline(head.get("0"));// ��������?
			ce.setHead_corp_code(head.get("KHID"));// �ͻ��ܹ�˾����?
			ce.setAr_rest(head.get("0"));// Ӧ����YSJE
			ce.setLast_tr_amount(head.get("0"));// ���������?
			ce.setLast_rec_amount(head.get("0"));// ����տ���?
			ce.setTr_frequency(head.get("0"));// ʹ��Ƶ��?
			ce.setPricegrade(head.get("0"));// �۸񼶱�?----
			ce.setCreatePerson(head.get("CREATEUSER"));// ������
			ce.setModifyPerson(head.get("UPDATEUSER"));// �����
			ce.setModifyDate(ggrq);// �������
			ce.setAuth_class(head.get("KHID"));// ����Ȩ����?
			ce.setInvoiceCompany(head.get("KHID"));// ��Ʊ��λ?
			ce.setCredit(head.get("0"));// �Ƿ�������ö��?
			ce.setCreditByHead(head.get("0"));// ?
			ce.setCreditDate(head.get("0"));// �Ƿ���ƶ������?
			ce.setLicenceDate(head.get("0"));// ?
			ce.setBusinessDate(head.get("0"));// ?
			ce.setbLimitSale(head.get("0"));// �Ƿ�����������
			ce.setbHomeBranch(head.get("0"));// �Ƿ��з�֧����
			ce.setbCusState(head.get("0"));// �Ƿ�ɽ�
			ce.setCcusexch_name(head.get("�����"));// ����
			ce.setBshop(head.get("0"));// �Ƿ��ŵ�----
			ce.setbOnGPinStore(head.get("0"));// ?
			ce.setbOnGPinStore(head.get("0"));// ?
			ce.setBcusdomestic(head.get("1"));// ����
			ce.setBcusoverseas(head.get("0"));// ����
			ce.setBserviceattribute(head.get("0"));// ?
			ce.setCcuscreditcompany(head.get("KHID"));// ���õ�λ ?
			ce.setCcusmngtypecode(head.get("999"));// ?
			ce.setBrequestsign(head.get("0"));//
			ce.setCcuscode(head.get("KHID"));// ?
			ce.setCinvoicecompany(head.get("KHID"));// ?
			ce.setAutoid(head.get("1"));// ?
			ce.setBdefault(head.get("True"));// ?
			//customerList.add(ce);
			ce.setBank_open(head.get("KHH"));// �_����
			ce.setBank_acc_number(head.get("YHZH"));// �y���~̖
			ce.setContact(head.get("LXR"));// �M��
			ce.setPhone(head.get("LXDH"));// �M�Ԓ
			ce.setAddress(head.get("LXDZ"));// ��ַ
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
					+ "<ufinterface sender=\"222\" receiver=\"u8\" roottag=\"customer\" docid=\"\" proc=\"add\" codeexchanged=\"N\" exportneedexch=\"N\" paginate=\"0\" display=\"�ͻ�����\" family=\"��������\" >"
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
					+ "<ccusexch_name>�����</ccusexch_name>"
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
