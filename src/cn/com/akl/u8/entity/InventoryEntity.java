package cn.com.akl.u8.entity;
/**
 * 
 * @author wjj
 *
 */
public class InventoryEntity {

	private String code;
	private String name;
	private String InvAddCode;
	private String specs;
	private String sort_code;
	private String main_supplier;
	private String main_measure;
	private String switch_item;
	private String inv_position;
	private String sale_flag;
	private String purchase_flag;
	private String selfmake_flag;
	private String prod_consu_flag;
	private String in_making_flag;
	private String tax_serv_flag;
	private String suit_flag;
	private String tax_rate;
	private String unit_weight;
	private String unit_volume;
	private String pro_sale_price;
	private String ref_cost;
	private String ref_sale_price;
	private String bottom_sale_price;
	private String new_cost;
	private String advance_period;
	private String ecnomic_batch;
	private String safe_stock;
	private String top_stock;
	private String bottom_stock;
	private String backlog;
	private String ABC_type;
	private String qlty_guarantee_flag;
	private String batch_flag;
	private String entrust_flag;
	private String backlog_flag;
	private String start_date;
	private String end_date;
	private String free_item1;
	private String free_item2;
	private String free_item3;
	private String free_item4;
	private String free_item5;
	private String free_item6;
	private String free_item7;
	private String free_item8;
	private String free_item9;
	private String free_item10;
	private String self_define1;
	private String self_define2;
	private String discount_flag;
	private String top_source_price;
	private String quality;
	private String retailprice;
	private String CreatePerson;
	private String ModifyPerson;
	private String ModifyDate;
	private String subscribe_point;
	private String avgquantity;
	private String pricetype;
	private String bfixunit;
	private String outline;
	private String inline;
	private String overdate;
	private String warndays;
	private String expense_rate;
	private String btrack;
	private String bserial;
	private String bbarcode;
	private String barcode;
	private String auth_class;
	private String unitgroup_type;
	private String unitgroup_code;
	private String puunit_code;
	private String saunit_code;
	private String stunit_code;
	private String caunit_code;
	private String unitgroup_name;
	private String ccomunitname;
	private String puunit_name;
	private String saunit_name;
	private String stunit_name;
	private String caunit_name;
	private String puunit_ichangrate;
	private String saunit_ichangrate;
	private String stunit_ichangrate;
	private String caunit_ichangrate;
	private String check_frequency;
	private String frequency;
	private String check_day;
	private String lastcheck_date;
	private String wastage;
	private String solitude;
	private String enterprise;
	private String address;
	private String file;
	private String brand;
	private String checkout_no;
	private String licence;
	private String specialties;
	private String defwarehouse;
	private String salerate;
	private String advanceDate;
	private String currencyName;
	private String ProduceAddress;
	private String produceNation;
	private String RegisterNo;
	private String EnterNo;
	private String PackingType;
	private String EnglishName;
	private String PropertyCheck;
	private String PreparationType;
	private String Commodity;
	private String RecipeBatch;
	private String NotPatentName;
	private String cAssComunitCode;
	private String ROPMethod;
	private String SubscribePoint;
	private String BatchRule;
	private String AssureProvideDays;
	private String VagQuantity;
	private String TestStyle;
	private String DTMethod;
	private String DTRate;
	private String DTNum;
	private String DTUnit;
	private String DTStyle;
	private String QTMethod;
	private String bPlanInv;
	private String bProxyForeign;
	private String bATOModel;
	private String bCheckItem;
	private String bPTOModel;
	private String bequipment;
	private String cProductUnit;
	private String fOrderUpLimit;
	private String cMassUnit;
	private String fRetailPrice;
	private String cInvDepCode;
	private String iAlterAdvance;
	private String fAlterBaseNum;
	private String cPlanMethod;
	private String bMPS;
	private String bROP;
	private String bRePlan;
	private String cSRPolicy;
	private String bBillUnite;
	private String iSupplyDay;
	private String fSupplyMulti;
	private String fMinSupply;
	private String bCutMantissa;
	private String cInvPersonCode;
	private String iInvTfId;
	private String cEngineerFigNo;
	private String bInTotalCost;
	private String iSupplyType;
	private String bConfigFree1;
	private String bConfigFree2;
	private String bConfigFree3;
	private String bConfigFree4;
	private String bConfigFree5;
	private String bConfigFree6;
	private String bConfigFree7;
	private String bConfigFree8;
	private String bConfigFree9;
	private String bConfigFree10;
	
	private String iDTLevel;
	private String cDTAQL;
	private String bOutInvDT;
	private String bPeriodDT;
	private String cDTPeriod;
	private String bBackInvDT;
	private String iEndDTStyle;
	private String bDTWarnInv;
	private String fBackTaxRate;
	private String cCIQCode;
	private String cWGroupCode;
	private String cWUnit;
	private String fGrossW;
	private String cVGroupCode;
	private String cVUnit;
	private String fLength;
	private String fWidth;
	private String fHeight;
	private String cpurpersoncode;
	private String iBigMonth;
	private String iBigDay;
	private String iSmallMonth;
	private String iSmallDay;
	private String cshopunit;
	private String bimportmedicine;
	private String bfirstbusimedicine;
	private String bforeexpland;
	private String cinvplancode;
	private String fconvertrate;
	private String dreplacedate;
	private String binvmodel;
	private String iimptaxrate;
	private String iexptaxrate;
	private String bexpsale;
	private String idrawbatch;
	private String bcheckbsatp;
	private String cinvprojectcode;
	private String itestrule;
	private String crulecode;
	private String bcheckfree1;
	private String bcheckfree2;
	private String bcheckfree3;
	private String bcheckfree4;
	private String bcheckfree5;
	private String bcheckfree6;
	private String bcheckfree7;
	private String bcheckfree8;
	private String bcheckfree9;
	private String bcheckfree10;
	private String bbommain;
	private String bbomsub;
	private String bproductbill;
	private String icheckatp;
	private String iplantfday;
	private String ioverlapday;
	private String iinvatpid;
	private String fmaxsupply;
	private String bpiece;
	private String bsrvitem;
	private String bsrvfittings;
	private String fminsplit;
	private String bspecialorder;
	private String btracksalebill;
	private String fbuyexcess;
	private String isurenesstype;
	private String idatetype;
	private String idatesum;
	private String idynamicsurenesstype;
	private String ibestrowsum;
	private String ipercentumsum;
	private String binbyprocheck;
	private String irequiretrackstyle;
	private String ibomexpandunittype;
	private String iexpiratdatecalcu;
	private String bpurpricefree1;
	private String bpurpricefree2;
	private String bpurpricefree3;
	private String bpurpricefree4;
	private String bpurpricefree5;
	private String bpurpricefree6;
	private String bpurpricefree7;
	private String bpurpricefree8;
	private String bpurpricefree9;
	private String bpurpricefree10;
	private String bompricefree1;
	private String bompricefree2;
	private String bompricefree3;
	private String bompricefree4;
	private String bompricefree5;
	private String bompricefree6;
	private String bompricefree7;
	private String bompricefree8;
	private String bompricefree9;
	private String bompricefree10;
	private String bsalepricefree1;
	private String bsalepricefree2;
	private String bsalepricefree3;
	private String bsalepricefree4;
	private String bsalepricefree5;
	private String bsalepricefree6;
	private String bsalepricefree7;
	private String bsalepricefree8;
	private String bsalepricefree9;
	private String bsalepricefree10;
	private String finvoutuplimit;
	private String bbondedinv;
	private String bbatchcreate;
	private String bbatchproperty1;
	private String bbatchproperty2;
	private String bbatchproperty3;
	private String bbatchproperty4;
	private String bbatchproperty5;
	private String bbatchproperty6;
	private String bbatchproperty7;
	private String bbatchproperty8;
	private String bbatchproperty9;
	private String bbatchproperty10;
	
	private String bcontrolfreerange1;
	private String bcontrolfreerange2;
	private String bcontrolfreerange3;
	private String bcontrolfreerange4;
	private String bcontrolfreerange5;
	private String bcontrolfreerange6;
	private String bcontrolfreerange7;
	private String bcontrolfreerange8;
	private String bcontrolfreerange9;
	private String bcontrolfreerange10;
	
	private String finvciqexch;
	private String iwarrantyperiod;
	private String iwarrantyunit;
	private String binvkeypart;
	private String iacceptearlydays;
	private String fcurllaborcost;
	private String fcurlvarmanucost;
	private String fcurlfixmanucost;
	private String fcurlomcost;
	private String fnextllaborcost;
	private String fnextlvarmanucost;
	private String fnextlfixmanucost;
	private String fnextlomcost;
	private String dinvcreatedatetime;
	private String bpuquota;
	private String binvrohs;
	private String fprjmatlimit;
	private String bprjmat;
	private String binvasset;
	private String bsrvproduct;
	private String iacceptdelaydays;
	private String cInvMnemCode;
	private String iPlanCheckDay;
	private String iMaterialsCycle;
	private String idrawtype;
	private String bSCkeyProjections;
	private String iSupplyPeriodType;
	private String iTimeBucketId;
	private String iAvailabilityDate;
	private String fmaterialcost;
	private String inearrejectdays;
	private String bimport;
	private String bsuitretail;
	private String froundfactor;
	private String bchecksubitemcost;
	private String bconsiderfreestock;
	private String breceiptbydt;
	private String bkccutmantissa;
	
	
	
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInvAddCode() {
		return InvAddCode;
	}
	public void setInvAddCode(String invAddCode) {
		InvAddCode = invAddCode;
	}
	public String getSpecs() {
		return specs;
	}
	public void setSpecs(String specs) {
		this.specs = specs;
	}
	public String getSort_code() {
		return sort_code;
	}
	public void setSort_code(String sort_code) {
		this.sort_code = sort_code;
	}
	public String getMain_supplier() {
		return main_supplier;
	}
	public void setMain_supplier(String main_supplier) {
		this.main_supplier = main_supplier;
	}
	public String getMain_measure() {
		return main_measure;
	}
	public void setMain_measure(String main_measure) {
		this.main_measure = main_measure;
	}
	public String getSwitch_item() {
		return switch_item;
	}
	public void setSwitch_item(String switch_item) {
		this.switch_item = switch_item;
	}
	public String getInv_position() {
		return inv_position;
	}
	public void setInv_position(String inv_position) {
		this.inv_position = inv_position;
	}
	public String getSale_flag() {
		return sale_flag;
	}
	public void setSale_flag(String sale_flag) {
		this.sale_flag = sale_flag;
	}
	public String getPurchase_flag() {
		return purchase_flag;
	}
	public void setPurchase_flag(String purchase_flag) {
		this.purchase_flag = purchase_flag;
	}
	public String getSelfmake_flag() {
		return selfmake_flag;
	}
	public void setSelfmake_flag(String selfmake_flag) {
		this.selfmake_flag = selfmake_flag;
	}
	public String getProd_consu_flag() {
		return prod_consu_flag;
	}
	public void setProd_consu_flag(String prod_consu_flag) {
		this.prod_consu_flag = prod_consu_flag;
	}
	public String getIn_making_flag() {
		return in_making_flag;
	}
	public void setIn_making_flag(String in_making_flag) {
		this.in_making_flag = in_making_flag;
	}
	public String getTax_serv_flag() {
		return tax_serv_flag;
	}
	public void setTax_serv_flag(String tax_serv_flag) {
		this.tax_serv_flag = tax_serv_flag;
	}
	public String getSuit_flag() {
		return suit_flag;
	}
	public void setSuit_flag(String suit_flag) {
		this.suit_flag = suit_flag;
	}
	public String getTax_rate() {
		return tax_rate;
	}
	public void setTax_rate(String tax_rate) {
		this.tax_rate = tax_rate;
	}
	public String getUnit_weight() {
		return unit_weight;
	}
	public void setUnit_weight(String unit_weight) {
		this.unit_weight = unit_weight;
	}
	public String getUnit_volume() {
		return unit_volume;
	}
	public void setUnit_volume(String unit_volume) {
		this.unit_volume = unit_volume;
	}
	public String getPro_sale_price() {
		return pro_sale_price;
	}
	public void setPro_sale_price(String pro_sale_price) {
		this.pro_sale_price = pro_sale_price;
	}
	public String getRef_cost() {
		return ref_cost;
	}
	public void setRef_cost(String ref_cost) {
		this.ref_cost = ref_cost;
	}
	public String getRef_sale_price() {
		return ref_sale_price;
	}
	public void setRef_sale_price(String ref_sale_price) {
		this.ref_sale_price = ref_sale_price;
	}
	public String getBottom_sale_price() {
		return bottom_sale_price;
	}
	public void setBottom_sale_price(String bottom_sale_price) {
		this.bottom_sale_price = bottom_sale_price;
	}
	public String getNew_cost() {
		return new_cost;
	}
	public void setNew_cost(String new_cost) {
		this.new_cost = new_cost;
	}
	public String getAdvance_period() {
		return advance_period;
	}
	public void setAdvance_period(String advance_period) {
		this.advance_period = advance_period;
	}
	public String getEcnomic_batch() {
		return ecnomic_batch;
	}
	public void setEcnomic_batch(String ecnomic_batch) {
		this.ecnomic_batch = ecnomic_batch;
	}
	public String getSafe_stock() {
		return safe_stock;
	}
	public void setSafe_stock(String safe_stock) {
		this.safe_stock = safe_stock;
	}
	public String getTop_stock() {
		return top_stock;
	}
	public void setTop_stock(String top_stock) {
		this.top_stock = top_stock;
	}
	public String getBottom_stock() {
		return bottom_stock;
	}
	public void setBottom_stock(String bottom_stock) {
		this.bottom_stock = bottom_stock;
	}
	public String getBacklog() {
		return backlog;
	}
	public void setBacklog(String backlog) {
		this.backlog = backlog;
	}
	public String getABC_type() {
		return ABC_type;
	}
	public void setABC_type(String aBC_type) {
		ABC_type = aBC_type;
	}
	public String getQlty_guarantee_flag() {
		return qlty_guarantee_flag;
	}
	public void setQlty_guarantee_flag(String qlty_guarantee_flag) {
		this.qlty_guarantee_flag = qlty_guarantee_flag;
	}
	public String getBatch_flag() {
		return batch_flag;
	}
	public void setBatch_flag(String batch_flag) {
		this.batch_flag = batch_flag;
	}
	public String getEntrust_flag() {
		return entrust_flag;
	}
	public void setEntrust_flag(String entrust_flag) {
		this.entrust_flag = entrust_flag;
	}
	public String getBacklog_flag() {
		return backlog_flag;
	}
	public void setBacklog_flag(String backlog_flag) {
		this.backlog_flag = backlog_flag;
	}
	public String getStart_date() {
		return start_date;
	}
	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}
	public String getEnd_date() {
		return end_date;
	}
	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}
	public String getFree_item1() {
		return free_item1;
	}
	public void setFree_item1(String free_item1) {
		this.free_item1 = free_item1;
	}
	public String getFree_item2() {
		return free_item2;
	}
	public void setFree_item2(String free_item2) {
		this.free_item2 = free_item2;
	}
	
	public String getFree_item3() {
		return free_item3;
	}
	public void setFree_item3(String free_item3) {
		this.free_item3 = free_item3;
	}
	public String getFree_item4() {
		return free_item4;
	}
	public void setFree_item4(String free_item4) {
		this.free_item4 = free_item4;
	}
	public String getFree_item5() {
		return free_item5;
	}
	public void setFree_item5(String free_item5) {
		this.free_item5 = free_item5;
	}
	public String getFree_item6() {
		return free_item6;
	}
	public void setFree_item6(String free_item6) {
		this.free_item6 = free_item6;
	}
	public String getFree_item7() {
		return free_item7;
	}
	public void setFree_item7(String free_item7) {
		this.free_item7 = free_item7;
	}
	public String getFree_item8() {
		return free_item8;
	}
	public void setFree_item8(String free_item8) {
		this.free_item8 = free_item8;
	}
	public String getFree_item9() {
		return free_item9;
	}
	public void setFree_item9(String free_item9) {
		this.free_item9 = free_item9;
	}
	public String getFree_item10() {
		return free_item10;
	}
	public void setFree_item10(String free_item10) {
		this.free_item10 = free_item10;
	}
	public String getSelf_define1() {
		return self_define1;
	}
	public void setSelf_define1(String self_define1) {
		this.self_define1 = self_define1;
	}
	public String getSelf_define2() {
		return self_define2;
	}
	public void setSelf_define2(String self_define2) {
		this.self_define2 = self_define2;
	}
	public String getDiscount_flag() {
		return discount_flag;
	}
	public void setDiscount_flag(String discount_flag) {
		this.discount_flag = discount_flag;
	}
	public String getTop_source_price() {
		return top_source_price;
	}
	public void setTop_source_price(String top_source_price) {
		this.top_source_price = top_source_price;
	}
	public String getQuality() {
		return quality;
	}
	public void setQuality(String quality) {
		this.quality = quality;
	}
	public String getRetailprice() {
		return retailprice;
	}
	public void setRetailprice(String retailprice) {
		this.retailprice = retailprice;
	}
	public String getCreatePerson() {
		return CreatePerson;
	}
	public void setCreatePerson(String createPerson) {
		CreatePerson = createPerson;
	}
	public String getModifyPerson() {
		return ModifyPerson;
	}
	public void setModifyPerson(String modifyPerson) {
		ModifyPerson = modifyPerson;
	}
	public String getModifyDate() {
		return ModifyDate;
	}
	public void setModifyDate(String modifyDate) {
		ModifyDate = modifyDate;
	}
	public String getSubscribe_point() {
		return subscribe_point;
	}
	public void setSubscribe_point(String subscribe_point) {
		this.subscribe_point = subscribe_point;
	}
	public String getAvgquantity() {
		return avgquantity;
	}
	public void setAvgquantity(String avgquantity) {
		this.avgquantity = avgquantity;
	}
	public String getPricetype() {
		return pricetype;
	}
	public void setPricetype(String pricetype) {
		this.pricetype = pricetype;
	}
	public String getBfixunit() {
		return bfixunit;
	}
	public void setBfixunit(String bfixunit) {
		this.bfixunit = bfixunit;
	}
	public String getOutline() {
		return outline;
	}
	public void setOutline(String outline) {
		this.outline = outline;
	}
	public String getInline() {
		return inline;
	}
	public void setInline(String inline) {
		this.inline = inline;
	}
	public String getOverdate() {
		return overdate;
	}
	public void setOverdate(String overdate) {
		this.overdate = overdate;
	}
	public String getWarndays() {
		return warndays;
	}
	public void setWarndays(String warndays) {
		this.warndays = warndays;
	}
	public String getExpense_rate() {
		return expense_rate;
	}
	public void setExpense_rate(String expense_rate) {
		this.expense_rate = expense_rate;
	}
	public String getBtrack() {
		return btrack;
	}
	public void setBtrack(String btrack) {
		this.btrack = btrack;
	}
	public String getBserial() {
		return bserial;
	}
	public void setBserial(String bserial) {
		this.bserial = bserial;
	}
	public String getBbarcode() {
		return bbarcode;
	}
	public void setBbarcode(String bbarcode) {
		this.bbarcode = bbarcode;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getAuth_class() {
		return auth_class;
	}
	public void setAuth_class(String auth_class) {
		this.auth_class = auth_class;
	}
	public String getUnitgroup_type() {
		return unitgroup_type;
	}
	public void setUnitgroup_type(String unitgroup_type) {
		this.unitgroup_type = unitgroup_type;
	}
	public String getUnitgroup_code() {
		return unitgroup_code;
	}
	public void setUnitgroup_code(String unitgroup_code) {
		this.unitgroup_code = unitgroup_code;
	}
	public String getPuunit_code() {
		return puunit_code;
	}
	public void setPuunit_code(String puunit_code) {
		this.puunit_code = puunit_code;
	}
	public String getSaunit_code() {
		return saunit_code;
	}
	public void setSaunit_code(String saunit_code) {
		this.saunit_code = saunit_code;
	}
	public String getStunit_code() {
		return stunit_code;
	}
	public void setStunit_code(String stunit_code) {
		this.stunit_code = stunit_code;
	}
	public String getCaunit_code() {
		return caunit_code;
	}
	public void setCaunit_code(String caunit_code) {
		this.caunit_code = caunit_code;
	}
	public String getUnitgroup_name() {
		return unitgroup_name;
	}
	public void setUnitgroup_name(String unitgroup_name) {
		this.unitgroup_name = unitgroup_name;
	}
	public String getCcomunitname() {
		return ccomunitname;
	}
	public void setCcomunitname(String ccomunitname) {
		this.ccomunitname = ccomunitname;
	}
	public String getPuunit_name() {
		return puunit_name;
	}
	public void setPuunit_name(String puunit_name) {
		this.puunit_name = puunit_name;
	}
	public String getSaunit_name() {
		return saunit_name;
	}
	public void setSaunit_name(String saunit_name) {
		this.saunit_name = saunit_name;
	}
	public String getStunit_name() {
		return stunit_name;
	}
	public void setStunit_name(String stunit_name) {
		this.stunit_name = stunit_name;
	}
	public String getCaunit_name() {
		return caunit_name;
	}
	public void setCaunit_name(String caunit_name) {
		this.caunit_name = caunit_name;
	}
	public String getPuunit_ichangrate() {
		return puunit_ichangrate;
	}
	public void setPuunit_ichangrate(String puunit_ichangrate) {
		this.puunit_ichangrate = puunit_ichangrate;
	}
	public String getSaunit_ichangrate() {
		return saunit_ichangrate;
	}
	public void setSaunit_ichangrate(String saunit_ichangrate) {
		this.saunit_ichangrate = saunit_ichangrate;
	}
	public String getStunit_ichangrate() {
		return stunit_ichangrate;
	}
	public void setStunit_ichangrate(String stunit_ichangrate) {
		this.stunit_ichangrate = stunit_ichangrate;
	}
	public String getCaunit_ichangrate() {
		return caunit_ichangrate;
	}
	public void setCaunit_ichangrate(String caunit_ichangrate) {
		this.caunit_ichangrate = caunit_ichangrate;
	}
	public String getCheck_frequency() {
		return check_frequency;
	}
	public void setCheck_frequency(String check_frequency) {
		this.check_frequency = check_frequency;
	}
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	public String getCheck_day() {
		return check_day;
	}
	public void setCheck_day(String check_day) {
		this.check_day = check_day;
	}
	public String getLastcheck_date() {
		return lastcheck_date;
	}
	public void setLastcheck_date(String lastcheck_date) {
		this.lastcheck_date = lastcheck_date;
	}
	public String getWastage() {
		return wastage;
	}
	public void setWastage(String wastage) {
		this.wastage = wastage;
	}
	public String getSolitude() {
		return solitude;
	}
	public void setSolitude(String solitude) {
		this.solitude = solitude;
	}
	public String getEnterprise() {
		return enterprise;
	}
	public void setEnterprise(String enterprise) {
		this.enterprise = enterprise;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getCheckout_no() {
		return checkout_no;
	}
	public void setCheckout_no(String checkout_no) {
		this.checkout_no = checkout_no;
	}
	public String getLicence() {
		return licence;
	}
	public void setLicence(String licence) {
		this.licence = licence;
	}
	public String getSpecialties() {
		return specialties;
	}
	public void setSpecialties(String specialties) {
		this.specialties = specialties;
	}
	public String getDefwarehouse() {
		return defwarehouse;
	}
	public void setDefwarehouse(String defwarehouse) {
		this.defwarehouse = defwarehouse;
	}
	public String getSalerate() {
		return salerate;
	}
	public void setSalerate(String salerate) {
		this.salerate = salerate;
	}
	public String getAdvanceDate() {
		return advanceDate;
	}
	public void setAdvanceDate(String advanceDate) {
		this.advanceDate = advanceDate;
	}
	public String getCurrencyName() {
		return currencyName;
	}
	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}
	public String getProduceAddress() {
		return ProduceAddress;
	}
	public void setProduceAddress(String produceAddress) {
		ProduceAddress = produceAddress;
	}
	public String getProduceNation() {
		return produceNation;
	}
	public void setProduceNation(String produceNation) {
		this.produceNation = produceNation;
	}
	public String getRegisterNo() {
		return RegisterNo;
	}
	public void setRegisterNo(String registerNo) {
		RegisterNo = registerNo;
	}
	public String getEnterNo() {
		return EnterNo;
	}
	public void setEnterNo(String enterNo) {
		EnterNo = enterNo;
	}
	public String getPackingType() {
		return PackingType;
	}
	public void setPackingType(String packingType) {
		PackingType = packingType;
	}
	public String getEnglishName() {
		return EnglishName;
	}
	public void setEnglishName(String englishName) {
		EnglishName = englishName;
	}
	public String getPropertyCheck() {
		return PropertyCheck;
	}
	public void setPropertyCheck(String propertyCheck) {
		PropertyCheck = propertyCheck;
	}
	public String getPreparationType() {
		return PreparationType;
	}
	public void setPreparationType(String preparationType) {
		PreparationType = preparationType;
	}
	public String getCommodity() {
		return Commodity;
	}
	public void setCommodity(String commodity) {
		Commodity = commodity;
	}
	public String getRecipeBatch() {
		return RecipeBatch;
	}
	public void setRecipeBatch(String recipeBatch) {
		RecipeBatch = recipeBatch;
	}
	public String getNotPatentName() {
		return NotPatentName;
	}
	public void setNotPatentName(String notPatentName) {
		NotPatentName = notPatentName;
	}
	public String getcAssComunitCode() {
		return cAssComunitCode;
	}
	public void setcAssComunitCode(String cAssComunitCode) {
		this.cAssComunitCode = cAssComunitCode;
	}
	public String getROPMethod() {
		return ROPMethod;
	}
	public void setROPMethod(String rOPMethod) {
		ROPMethod = rOPMethod;
	}
	public String getSubscribePoint() {
		return SubscribePoint;
	}
	public void setSubscribePoint(String subscribePoint) {
		SubscribePoint = subscribePoint;
	}
	public String getBatchRule() {
		return BatchRule;
	}
	public void setBatchRule(String batchRule) {
		BatchRule = batchRule;
	}
	public String getAssureProvideDays() {
		return AssureProvideDays;
	}
	public void setAssureProvideDays(String assureProvideDays) {
		AssureProvideDays = assureProvideDays;
	}
	public String getVagQuantity() {
		return VagQuantity;
	}
	public void setVagQuantity(String vagQuantity) {
		VagQuantity = vagQuantity;
	}
	public String getTestStyle() {
		return TestStyle;
	}
	public void setTestStyle(String testStyle) {
		TestStyle = testStyle;
	}
	public String getDTMethod() {
		return DTMethod;
	}
	public void setDTMethod(String dTMethod) {
		DTMethod = dTMethod;
	}
	public String getDTRate() {
		return DTRate;
	}
	public void setDTRate(String dTRate) {
		DTRate = dTRate;
	}
	public String getDTNum() {
		return DTNum;
	}
	public void setDTNum(String dTNum) {
		DTNum = dTNum;
	}
	public String getDTUnit() {
		return DTUnit;
	}
	public void setDTUnit(String dTUnit) {
		DTUnit = dTUnit;
	}
	public String getDTStyle() {
		return DTStyle;
	}
	public void setDTStyle(String dTStyle) {
		DTStyle = dTStyle;
	}
	public String getQTMethod() {
		return QTMethod;
	}
	public void setQTMethod(String qTMethod) {
		QTMethod = qTMethod;
	}
	public String getbPlanInv() {
		return bPlanInv;
	}
	public void setbPlanInv(String bPlanInv) {
		this.bPlanInv = bPlanInv;
	}
	public String getbProxyForeign() {
		return bProxyForeign;
	}
	public void setbProxyForeign(String bProxyForeign) {
		this.bProxyForeign = bProxyForeign;
	}
	public String getbATOModel() {
		return bATOModel;
	}
	public void setbATOModel(String bATOModel) {
		this.bATOModel = bATOModel;
	}
	public String getbCheckItem() {
		return bCheckItem;
	}
	public void setbCheckItem(String bCheckItem) {
		this.bCheckItem = bCheckItem;
	}
	public String getbPTOModel() {
		return bPTOModel;
	}
	public void setbPTOModel(String bPTOModel) {
		this.bPTOModel = bPTOModel;
	}
	public String getBequipment() {
		return bequipment;
	}
	public void setBequipment(String bequipment) {
		this.bequipment = bequipment;
	}
	public String getcProductUnit() {
		return cProductUnit;
	}
	public void setcProductUnit(String cProductUnit) {
		this.cProductUnit = cProductUnit;
	}
	public String getfOrderUpLimit() {
		return fOrderUpLimit;
	}
	public void setfOrderUpLimit(String fOrderUpLimit) {
		this.fOrderUpLimit = fOrderUpLimit;
	}
	public String getcMassUnit() {
		return cMassUnit;
	}
	public void setcMassUnit(String cMassUnit) {
		this.cMassUnit = cMassUnit;
	}
	public String getfRetailPrice() {
		return fRetailPrice;
	}
	public void setfRetailPrice(String fRetailPrice) {
		this.fRetailPrice = fRetailPrice;
	}
	public String getcInvDepCode() {
		return cInvDepCode;
	}
	public void setcInvDepCode(String cInvDepCode) {
		this.cInvDepCode = cInvDepCode;
	}
	public String getiAlterAdvance() {
		return iAlterAdvance;
	}
	public void setiAlterAdvance(String iAlterAdvance) {
		this.iAlterAdvance = iAlterAdvance;
	}
	public String getfAlterBaseNum() {
		return fAlterBaseNum;
	}
	public void setfAlterBaseNum(String fAlterBaseNum) {
		this.fAlterBaseNum = fAlterBaseNum;
	}
	public String getcPlanMethod() {
		return cPlanMethod;
	}
	public void setcPlanMethod(String cPlanMethod) {
		this.cPlanMethod = cPlanMethod;
	}
	public String getbMPS() {
		return bMPS;
	}
	public void setbMPS(String bMPS) {
		this.bMPS = bMPS;
	}
	public String getbROP() {
		return bROP;
	}
	public void setbROP(String bROP) {
		this.bROP = bROP;
	}
	public String getbRePlan() {
		return bRePlan;
	}
	public void setbRePlan(String bRePlan) {
		this.bRePlan = bRePlan;
	}
	public String getcSRPolicy() {
		return cSRPolicy;
	}
	public void setcSRPolicy(String cSRPolicy) {
		this.cSRPolicy = cSRPolicy;
	}
	public String getbBillUnite() {
		return bBillUnite;
	}
	public void setbBillUnite(String bBillUnite) {
		this.bBillUnite = bBillUnite;
	}
	public String getiSupplyDay() {
		return iSupplyDay;
	}
	public void setiSupplyDay(String iSupplyDay) {
		this.iSupplyDay = iSupplyDay;
	}
	public String getfSupplyMulti() {
		return fSupplyMulti;
	}
	public void setfSupplyMulti(String fSupplyMulti) {
		this.fSupplyMulti = fSupplyMulti;
	}
	public String getfMinSupply() {
		return fMinSupply;
	}
	public void setfMinSupply(String fMinSupply) {
		this.fMinSupply = fMinSupply;
	}
	public String getbCutMantissa() {
		return bCutMantissa;
	}
	public void setbCutMantissa(String bCutMantissa) {
		this.bCutMantissa = bCutMantissa;
	}
	public String getcInvPersonCode() {
		return cInvPersonCode;
	}
	public void setcInvPersonCode(String cInvPersonCode) {
		this.cInvPersonCode = cInvPersonCode;
	}
	public String getiInvTfId() {
		return iInvTfId;
	}
	public void setiInvTfId(String iInvTfId) {
		this.iInvTfId = iInvTfId;
	}
	public String getcEngineerFigNo() {
		return cEngineerFigNo;
	}
	public void setcEngineerFigNo(String cEngineerFigNo) {
		this.cEngineerFigNo = cEngineerFigNo;
	}
	public String getbInTotalCost() {
		return bInTotalCost;
	}
	public void setbInTotalCost(String bInTotalCost) {
		this.bInTotalCost = bInTotalCost;
	}
	public String getiSupplyType() {
		return iSupplyType;
	}
	public void setiSupplyType(String iSupplyType) {
		this.iSupplyType = iSupplyType;
	}
	public String getbConfigFree1() {
		return bConfigFree1;
	}
	public void setbConfigFree1(String bConfigFree1) {
		this.bConfigFree1 = bConfigFree1;
	}
	public String getbConfigFree2() {
		return bConfigFree2;
	}
	public void setbConfigFree2(String bConfigFree2) {
		this.bConfigFree2 = bConfigFree2;
	}
	
	public String getbConfigFree3() {
		return bConfigFree3;
	}
	public void setbConfigFree3(String bConfigFree3) {
		this.bConfigFree3 = bConfigFree3;
	}
	public String getbConfigFree4() {
		return bConfigFree4;
	}
	public void setbConfigFree4(String bConfigFree4) {
		this.bConfigFree4 = bConfigFree4;
	}
	public String getbConfigFree5() {
		return bConfigFree5;
	}
	public void setbConfigFree5(String bConfigFree5) {
		this.bConfigFree5 = bConfigFree5;
	}
	public String getbConfigFree6() {
		return bConfigFree6;
	}
	public void setbConfigFree6(String bConfigFree6) {
		this.bConfigFree6 = bConfigFree6;
	}
	public String getbConfigFree7() {
		return bConfigFree7;
	}
	public void setbConfigFree7(String bConfigFree7) {
		this.bConfigFree7 = bConfigFree7;
	}
	public String getbConfigFree8() {
		return bConfigFree8;
	}
	public void setbConfigFree8(String bConfigFree8) {
		this.bConfigFree8 = bConfigFree8;
	}
	public String getbConfigFree9() {
		return bConfigFree9;
	}
	public void setbConfigFree9(String bConfigFree9) {
		this.bConfigFree9 = bConfigFree9;
	}
	public String getbConfigFree10() {
		return bConfigFree10;
	}
	public void setbConfigFree10(String bConfigFree10) {
		this.bConfigFree10 = bConfigFree10;
	}
	public String getiDTLevel() {
		return iDTLevel;
	}
	public void setiDTLevel(String iDTLevel) {
		this.iDTLevel = iDTLevel;
	}
	public String getcDTAQL() {
		return cDTAQL;
	}
	public void setcDTAQL(String cDTAQL) {
		this.cDTAQL = cDTAQL;
	}
	public String getbOutInvDT() {
		return bOutInvDT;
	}
	public void setbOutInvDT(String bOutInvDT) {
		this.bOutInvDT = bOutInvDT;
	}
	public String getbPeriodDT() {
		return bPeriodDT;
	}
	public void setbPeriodDT(String bPeriodDT) {
		this.bPeriodDT = bPeriodDT;
	}
	public String getcDTPeriod() {
		return cDTPeriod;
	}
	public void setcDTPeriod(String cDTPeriod) {
		this.cDTPeriod = cDTPeriod;
	}
	public String getbBackInvDT() {
		return bBackInvDT;
	}
	public void setbBackInvDT(String bBackInvDT) {
		this.bBackInvDT = bBackInvDT;
	}
	public String getiEndDTStyle() {
		return iEndDTStyle;
	}
	public void setiEndDTStyle(String iEndDTStyle) {
		this.iEndDTStyle = iEndDTStyle;
	}
	public String getbDTWarnInv() {
		return bDTWarnInv;
	}
	public void setbDTWarnInv(String bDTWarnInv) {
		this.bDTWarnInv = bDTWarnInv;
	}
	public String getfBackTaxRate() {
		return fBackTaxRate;
	}
	public void setfBackTaxRate(String fBackTaxRate) {
		this.fBackTaxRate = fBackTaxRate;
	}
	public String getcCIQCode() {
		return cCIQCode;
	}
	public void setcCIQCode(String cCIQCode) {
		this.cCIQCode = cCIQCode;
	}
	public String getcWGroupCode() {
		return cWGroupCode;
	}
	public void setcWGroupCode(String cWGroupCode) {
		this.cWGroupCode = cWGroupCode;
	}
	public String getcWUnit() {
		return cWUnit;
	}
	public void setcWUnit(String cWUnit) {
		this.cWUnit = cWUnit;
	}
	public String getfGrossW() {
		return fGrossW;
	}
	public void setfGrossW(String fGrossW) {
		this.fGrossW = fGrossW;
	}
	public String getcVGroupCode() {
		return cVGroupCode;
	}
	public void setcVGroupCode(String cVGroupCode) {
		this.cVGroupCode = cVGroupCode;
	}
	public String getcVUnit() {
		return cVUnit;
	}
	public void setcVUnit(String cVUnit) {
		this.cVUnit = cVUnit;
	}
	public String getfLength() {
		return fLength;
	}
	public void setfLength(String fLength) {
		this.fLength = fLength;
	}
	public String getfWidth() {
		return fWidth;
	}
	public void setfWidth(String fWidth) {
		this.fWidth = fWidth;
	}
	public String getfHeight() {
		return fHeight;
	}
	public void setfHeight(String fHeight) {
		this.fHeight = fHeight;
	}
	public String getCpurpersoncode() {
		return cpurpersoncode;
	}
	public void setCpurpersoncode(String cpurpersoncode) {
		this.cpurpersoncode = cpurpersoncode;
	}
	public String getiBigMonth() {
		return iBigMonth;
	}
	public void setiBigMonth(String iBigMonth) {
		this.iBigMonth = iBigMonth;
	}
	public String getiBigDay() {
		return iBigDay;
	}
	public void setiBigDay(String iBigDay) {
		this.iBigDay = iBigDay;
	}
	public String getiSmallMonth() {
		return iSmallMonth;
	}
	public void setiSmallMonth(String iSmallMonth) {
		this.iSmallMonth = iSmallMonth;
	}
	public String getiSmallDay() {
		return iSmallDay;
	}
	public void setiSmallDay(String iSmallDay) {
		this.iSmallDay = iSmallDay;
	}
	public String getCshopunit() {
		return cshopunit;
	}
	public void setCshopunit(String cshopunit) {
		this.cshopunit = cshopunit;
	}
	public String getBimportmedicine() {
		return bimportmedicine;
	}
	public void setBimportmedicine(String bimportmedicine) {
		this.bimportmedicine = bimportmedicine;
	}
	public String getBfirstbusimedicine() {
		return bfirstbusimedicine;
	}
	public void setBfirstbusimedicine(String bfirstbusimedicine) {
		this.bfirstbusimedicine = bfirstbusimedicine;
	}
	public String getBforeexpland() {
		return bforeexpland;
	}
	public void setBforeexpland(String bforeexpland) {
		this.bforeexpland = bforeexpland;
	}
	public String getCinvplancode() {
		return cinvplancode;
	}
	public void setCinvplancode(String cinvplancode) {
		this.cinvplancode = cinvplancode;
	}
	public String getFconvertrate() {
		return fconvertrate;
	}
	public void setFconvertrate(String fconvertrate) {
		this.fconvertrate = fconvertrate;
	}
	public String getDreplacedate() {
		return dreplacedate;
	}
	public void setDreplacedate(String dreplacedate) {
		this.dreplacedate = dreplacedate;
	}
	public String getBinvmodel() {
		return binvmodel;
	}
	public void setBinvmodel(String binvmodel) {
		this.binvmodel = binvmodel;
	}
	public String getIimptaxrate() {
		return iimptaxrate;
	}
	public void setIimptaxrate(String iimptaxrate) {
		this.iimptaxrate = iimptaxrate;
	}
	public String getIexptaxrate() {
		return iexptaxrate;
	}
	public void setIexptaxrate(String iexptaxrate) {
		this.iexptaxrate = iexptaxrate;
	}
	public String getBexpsale() {
		return bexpsale;
	}
	public void setBexpsale(String bexpsale) {
		this.bexpsale = bexpsale;
	}
	public String getIdrawbatch() {
		return idrawbatch;
	}
	public void setIdrawbatch(String idrawbatch) {
		this.idrawbatch = idrawbatch;
	}
	public String getBcheckbsatp() {
		return bcheckbsatp;
	}
	public void setBcheckbsatp(String bcheckbsatp) {
		this.bcheckbsatp = bcheckbsatp;
	}
	public String getCinvprojectcode() {
		return cinvprojectcode;
	}
	public void setCinvprojectcode(String cinvprojectcode) {
		this.cinvprojectcode = cinvprojectcode;
	}
	public String getItestrule() {
		return itestrule;
	}
	public void setItestrule(String itestrule) {
		this.itestrule = itestrule;
	}
	public String getCrulecode() {
		return crulecode;
	}
	public void setCrulecode(String crulecode) {
		this.crulecode = crulecode;
	}
	public String getBcheckfree1() {
		return bcheckfree1;
	}
	public void setBcheckfree1(String bcheckfree1) {
		this.bcheckfree1 = bcheckfree1;
	}
	public String getBcheckfree2() {
		return bcheckfree2;
	}
	public void setBcheckfree2(String bcheckfree2) {
		this.bcheckfree2 = bcheckfree2;
	}
	
	public String getBcheckfree3() {
		return bcheckfree3;
	}
	public void setBcheckfree3(String bcheckfree3) {
		this.bcheckfree3 = bcheckfree3;
	}
	public String getBcheckfree4() {
		return bcheckfree4;
	}
	public void setBcheckfree4(String bcheckfree4) {
		this.bcheckfree4 = bcheckfree4;
	}
	public String getBcheckfree5() {
		return bcheckfree5;
	}
	public void setBcheckfree5(String bcheckfree5) {
		this.bcheckfree5 = bcheckfree5;
	}
	public String getBcheckfree6() {
		return bcheckfree6;
	}
	public void setBcheckfree6(String bcheckfree6) {
		this.bcheckfree6 = bcheckfree6;
	}
	public String getBcheckfree7() {
		return bcheckfree7;
	}
	public void setBcheckfree7(String bcheckfree7) {
		this.bcheckfree7 = bcheckfree7;
	}
	public String getBcheckfree8() {
		return bcheckfree8;
	}
	public void setBcheckfree8(String bcheckfree8) {
		this.bcheckfree8 = bcheckfree8;
	}
	public String getBcheckfree9() {
		return bcheckfree9;
	}
	public void setBcheckfree9(String bcheckfree9) {
		this.bcheckfree9 = bcheckfree9;
	}
	public String getBcheckfree10() {
		return bcheckfree10;
	}
	public void setBcheckfree10(String bcheckfree10) {
		this.bcheckfree10 = bcheckfree10;
	}
	public String getBbommain() {
		return bbommain;
	}
	public void setBbommain(String bbommain) {
		this.bbommain = bbommain;
	}
	public String getBbomsub() {
		return bbomsub;
	}
	public void setBbomsub(String bbomsub) {
		this.bbomsub = bbomsub;
	}
	public String getBproductbill() {
		return bproductbill;
	}
	public void setBproductbill(String bproductbill) {
		this.bproductbill = bproductbill;
	}
	public String getIcheckatp() {
		return icheckatp;
	}
	public void setIcheckatp(String icheckatp) {
		this.icheckatp = icheckatp;
	}
	public String getIplantfday() {
		return iplantfday;
	}
	public void setIplantfday(String iplantfday) {
		this.iplantfday = iplantfday;
	}
	public String getIoverlapday() {
		return ioverlapday;
	}
	public void setIoverlapday(String ioverlapday) {
		this.ioverlapday = ioverlapday;
	}
	public String getIinvatpid() {
		return iinvatpid;
	}
	public void setIinvatpid(String iinvatpid) {
		this.iinvatpid = iinvatpid;
	}
	public String getFmaxsupply() {
		return fmaxsupply;
	}
	public void setFmaxsupply(String fmaxsupply) {
		this.fmaxsupply = fmaxsupply;
	}
	public String getBpiece() {
		return bpiece;
	}
	public void setBpiece(String bpiece) {
		this.bpiece = bpiece;
	}
	public String getBsrvitem() {
		return bsrvitem;
	}
	public void setBsrvitem(String bsrvitem) {
		this.bsrvitem = bsrvitem;
	}
	public String getBsrvfittings() {
		return bsrvfittings;
	}
	public void setBsrvfittings(String bsrvfittings) {
		this.bsrvfittings = bsrvfittings;
	}
	public String getFminsplit() {
		return fminsplit;
	}
	public void setFminsplit(String fminsplit) {
		this.fminsplit = fminsplit;
	}
	public String getBspecialorder() {
		return bspecialorder;
	}
	public void setBspecialorder(String bspecialorder) {
		this.bspecialorder = bspecialorder;
	}
	public String getBtracksalebill() {
		return btracksalebill;
	}
	public void setBtracksalebill(String btracksalebill) {
		this.btracksalebill = btracksalebill;
	}
	public String getFbuyexcess() {
		return fbuyexcess;
	}
	public void setFbuyexcess(String fbuyexcess) {
		this.fbuyexcess = fbuyexcess;
	}
	public String getIsurenesstype() {
		return isurenesstype;
	}
	public void setIsurenesstype(String isurenesstype) {
		this.isurenesstype = isurenesstype;
	}
	public String getIdatetype() {
		return idatetype;
	}
	public void setIdatetype(String idatetype) {
		this.idatetype = idatetype;
	}
	public String getIdatesum() {
		return idatesum;
	}
	public void setIdatesum(String idatesum) {
		this.idatesum = idatesum;
	}
	public String getIdynamicsurenesstype() {
		return idynamicsurenesstype;
	}
	public void setIdynamicsurenesstype(String idynamicsurenesstype) {
		this.idynamicsurenesstype = idynamicsurenesstype;
	}
	public String getIbestrowsum() {
		return ibestrowsum;
	}
	public void setIbestrowsum(String ibestrowsum) {
		this.ibestrowsum = ibestrowsum;
	}
	public String getIpercentumsum() {
		return ipercentumsum;
	}
	public void setIpercentumsum(String ipercentumsum) {
		this.ipercentumsum = ipercentumsum;
	}
	public String getBinbyprocheck() {
		return binbyprocheck;
	}
	public void setBinbyprocheck(String binbyprocheck) {
		this.binbyprocheck = binbyprocheck;
	}
	public String getIrequiretrackstyle() {
		return irequiretrackstyle;
	}
	public void setIrequiretrackstyle(String irequiretrackstyle) {
		this.irequiretrackstyle = irequiretrackstyle;
	}
	public String getIbomexpandunittype() {
		return ibomexpandunittype;
	}
	public void setIbomexpandunittype(String ibomexpandunittype) {
		this.ibomexpandunittype = ibomexpandunittype;
	}
	public String getIexpiratdatecalcu() {
		return iexpiratdatecalcu;
	}
	public void setIexpiratdatecalcu(String iexpiratdatecalcu) {
		this.iexpiratdatecalcu = iexpiratdatecalcu;
	}
	public String getBpurpricefree1() {
		return bpurpricefree1;
	}
	public void setBpurpricefree1(String bpurpricefree1) {
		this.bpurpricefree1 = bpurpricefree1;
	}
	public String getBpurpricefree2() {
		return bpurpricefree2;
	}
	public void setBpurpricefree2(String bpurpricefree2) {
		this.bpurpricefree2 = bpurpricefree2;
	}
	public String getBompricefree1() {
		return bompricefree1;
	}
	public void setBompricefree1(String bompricefree1) {
		this.bompricefree1 = bompricefree1;
	}
	public String getBompricefree2() {
		return bompricefree2;
	}
	public void setBompricefree2(String bompricefree2) {
		this.bompricefree2 = bompricefree2;
	}
	public String getBsalepricefree1() {
		return bsalepricefree1;
	}
	public void setBsalepricefree1(String bsalepricefree1) {
		this.bsalepricefree1 = bsalepricefree1;
	}
	public String getBsalepricefree2() {
		return bsalepricefree2;
	}
	public void setBsalepricefree2(String bsalepricefree2) {
		this.bsalepricefree2 = bsalepricefree2;
	}
	public String getFinvoutuplimit() {
		return finvoutuplimit;
	}
	public void setFinvoutuplimit(String finvoutuplimit) {
		this.finvoutuplimit = finvoutuplimit;
	}
	public String getBbondedinv() {
		return bbondedinv;
	}
	public void setBbondedinv(String bbondedinv) {
		this.bbondedinv = bbondedinv;
	}
	public String getBbatchcreate() {
		return bbatchcreate;
	}
	public void setBbatchcreate(String bbatchcreate) {
		this.bbatchcreate = bbatchcreate;
	}
	public String getBbatchproperty1() {
		return bbatchproperty1;
	}
	public void setBbatchproperty1(String bbatchproperty1) {
		this.bbatchproperty1 = bbatchproperty1;
	}
	public String getBbatchproperty2() {
		return bbatchproperty2;
	}
	public void setBbatchproperty2(String bbatchproperty2) {
		this.bbatchproperty2 = bbatchproperty2;
	}
	public String getBcontrolfreerange1() {
		return bcontrolfreerange1;
	}
	public void setBcontrolfreerange1(String bcontrolfreerange1) {
		this.bcontrolfreerange1 = bcontrolfreerange1;
	}
	public String getBcontrolfreerange2() {
		return bcontrolfreerange2;
	}
	public void setBcontrolfreerange2(String bcontrolfreerange2) {
		this.bcontrolfreerange2 = bcontrolfreerange2;
	}
	public String getFinvciqexch() {
		return finvciqexch;
	}
	public void setFinvciqexch(String finvciqexch) {
		this.finvciqexch = finvciqexch;
	}
	public String getIwarrantyperiod() {
		return iwarrantyperiod;
	}
	public void setIwarrantyperiod(String iwarrantyperiod) {
		this.iwarrantyperiod = iwarrantyperiod;
	}
	public String getIwarrantyunit() {
		return iwarrantyunit;
	}
	public void setIwarrantyunit(String iwarrantyunit) {
		this.iwarrantyunit = iwarrantyunit;
	}
	public String getBinvkeypart() {
		return binvkeypart;
	}
	public void setBinvkeypart(String binvkeypart) {
		this.binvkeypart = binvkeypart;
	}
	public String getIacceptearlydays() {
		return iacceptearlydays;
	}
	public void setIacceptearlydays(String iacceptearlydays) {
		this.iacceptearlydays = iacceptearlydays;
	}
	public String getFcurllaborcost() {
		return fcurllaborcost;
	}
	public void setFcurllaborcost(String fcurllaborcost) {
		this.fcurllaborcost = fcurllaborcost;
	}
	public String getFcurlvarmanucost() {
		return fcurlvarmanucost;
	}
	public void setFcurlvarmanucost(String fcurlvarmanucost) {
		this.fcurlvarmanucost = fcurlvarmanucost;
	}
	public String getFcurlfixmanucost() {
		return fcurlfixmanucost;
	}
	public void setFcurlfixmanucost(String fcurlfixmanucost) {
		this.fcurlfixmanucost = fcurlfixmanucost;
	}
	public String getFcurlomcost() {
		return fcurlomcost;
	}
	public void setFcurlomcost(String fcurlomcost) {
		this.fcurlomcost = fcurlomcost;
	}
	public String getFnextllaborcost() {
		return fnextllaborcost;
	}
	public void setFnextllaborcost(String fnextllaborcost) {
		this.fnextllaborcost = fnextllaborcost;
	}
	public String getFnextlvarmanucost() {
		return fnextlvarmanucost;
	}
	public void setFnextlvarmanucost(String fnextlvarmanucost) {
		this.fnextlvarmanucost = fnextlvarmanucost;
	}
	public String getFnextlfixmanucost() {
		return fnextlfixmanucost;
	}
	public void setFnextlfixmanucost(String fnextlfixmanucost) {
		this.fnextlfixmanucost = fnextlfixmanucost;
	}
	public String getFnextlomcost() {
		return fnextlomcost;
	}
	public void setFnextlomcost(String fnextlomcost) {
		this.fnextlomcost = fnextlomcost;
	}
	public String getDinvcreatedatetime() {
		return dinvcreatedatetime;
	}
	public void setDinvcreatedatetime(String dinvcreatedatetime) {
		this.dinvcreatedatetime = dinvcreatedatetime;
	}
	public String getBpuquota() {
		return bpuquota;
	}
	public void setBpuquota(String bpuquota) {
		this.bpuquota = bpuquota;
	}
	public String getBinvrohs() {
		return binvrohs;
	}
	public void setBinvrohs(String binvrohs) {
		this.binvrohs = binvrohs;
	}
	public String getFprjmatlimit() {
		return fprjmatlimit;
	}
	public void setFprjmatlimit(String fprjmatlimit) {
		this.fprjmatlimit = fprjmatlimit;
	}
	public String getBprjmat() {
		return bprjmat;
	}
	public void setBprjmat(String bprjmat) {
		this.bprjmat = bprjmat;
	}
	public String getBinvasset() {
		return binvasset;
	}
	public void setBinvasset(String binvasset) {
		this.binvasset = binvasset;
	}
	public String getBsrvproduct() {
		return bsrvproduct;
	}
	public void setBsrvproduct(String bsrvproduct) {
		this.bsrvproduct = bsrvproduct;
	}
	public String getIacceptdelaydays() {
		return iacceptdelaydays;
	}
	public void setIacceptdelaydays(String iacceptdelaydays) {
		this.iacceptdelaydays = iacceptdelaydays;
	}
	public String getcInvMnemCode() {
		return cInvMnemCode;
	}
	public void setcInvMnemCode(String cInvMnemCode) {
		this.cInvMnemCode = cInvMnemCode;
	}
	public String getiPlanCheckDay() {
		return iPlanCheckDay;
	}
	public void setiPlanCheckDay(String iPlanCheckDay) {
		this.iPlanCheckDay = iPlanCheckDay;
	}
	public String getiMaterialsCycle() {
		return iMaterialsCycle;
	}
	public void setiMaterialsCycle(String iMaterialsCycle) {
		this.iMaterialsCycle = iMaterialsCycle;
	}
	public String getIdrawtype() {
		return idrawtype;
	}
	public void setIdrawtype(String idrawtype) {
		this.idrawtype = idrawtype;
	}
	public String getbSCkeyProjections() {
		return bSCkeyProjections;
	}
	public void setbSCkeyProjections(String bSCkeyProjections) {
		this.bSCkeyProjections = bSCkeyProjections;
	}
	public String getiSupplyPeriodType() {
		return iSupplyPeriodType;
	}
	public void setiSupplyPeriodType(String iSupplyPeriodType) {
		this.iSupplyPeriodType = iSupplyPeriodType;
	}
	public String getiTimeBucketId() {
		return iTimeBucketId;
	}
	public void setiTimeBucketId(String iTimeBucketId) {
		this.iTimeBucketId = iTimeBucketId;
	}
	public String getiAvailabilityDate() {
		return iAvailabilityDate;
	}
	public void setiAvailabilityDate(String iAvailabilityDate) {
		this.iAvailabilityDate = iAvailabilityDate;
	}
	public String getFmaterialcost() {
		return fmaterialcost;
	}
	public void setFmaterialcost(String fmaterialcost) {
		this.fmaterialcost = fmaterialcost;
	}
	public String getInearrejectdays() {
		return inearrejectdays;
	}
	public void setInearrejectdays(String inearrejectdays) {
		this.inearrejectdays = inearrejectdays;
	}
	public String getBimport() {
		return bimport;
	}
	public void setBimport(String bimport) {
		this.bimport = bimport;
	}
	public String getBsuitretail() {
		return bsuitretail;
	}
	public void setBsuitretail(String bsuitretail) {
		this.bsuitretail = bsuitretail;
	}
	public String getFroundfactor() {
		return froundfactor;
	}
	public void setFroundfactor(String froundfactor) {
		this.froundfactor = froundfactor;
	}
	public String getBchecksubitemcost() {
		return bchecksubitemcost;
	}
	public void setBchecksubitemcost(String bchecksubitemcost) {
		this.bchecksubitemcost = bchecksubitemcost;
	}
	public String getBconsiderfreestock() {
		return bconsiderfreestock;
	}
	public void setBconsiderfreestock(String bconsiderfreestock) {
		this.bconsiderfreestock = bconsiderfreestock;
	}
	public String getBreceiptbydt() {
		return breceiptbydt;
	}
	public void setBreceiptbydt(String breceiptbydt) {
		this.breceiptbydt = breceiptbydt;
	}
	public String getBkccutmantissa() {
		return bkccutmantissa;
	}
	public void setBkccutmantissa(String bkccutmantissa) {
		this.bkccutmantissa = bkccutmantissa;
	}
	public String getBpurpricefree3() {
		return bpurpricefree3;
	}
	public void setBpurpricefree3(String bpurpricefree3) {
		this.bpurpricefree3 = bpurpricefree3;
	}
	public String getBpurpricefree4() {
		return bpurpricefree4;
	}
	public void setBpurpricefree4(String bpurpricefree4) {
		this.bpurpricefree4 = bpurpricefree4;
	}
	public String getBpurpricefree5() {
		return bpurpricefree5;
	}
	public void setBpurpricefree5(String bpurpricefree5) {
		this.bpurpricefree5 = bpurpricefree5;
	}
	public String getBpurpricefree6() {
		return bpurpricefree6;
	}
	public void setBpurpricefree6(String bpurpricefree6) {
		this.bpurpricefree6 = bpurpricefree6;
	}
	public String getBpurpricefree7() {
		return bpurpricefree7;
	}
	public void setBpurpricefree7(String bpurpricefree7) {
		this.bpurpricefree7 = bpurpricefree7;
	}
	public String getBpurpricefree8() {
		return bpurpricefree8;
	}
	public void setBpurpricefree8(String bpurpricefree8) {
		this.bpurpricefree8 = bpurpricefree8;
	}
	public String getBpurpricefree9() {
		return bpurpricefree9;
	}
	public void setBpurpricefree9(String bpurpricefree9) {
		this.bpurpricefree9 = bpurpricefree9;
	}
	public String getBpurpricefree10() {
		return bpurpricefree10;
	}
	public void setBpurpricefree10(String bpurpricefree10) {
		this.bpurpricefree10 = bpurpricefree10;
	}
	public String getBompricefree3() {
		return bompricefree3;
	}
	public void setBompricefree3(String bompricefree3) {
		this.bompricefree3 = bompricefree3;
	}
	public String getBompricefree4() {
		return bompricefree4;
	}
	public void setBompricefree4(String bompricefree4) {
		this.bompricefree4 = bompricefree4;
	}
	public String getBompricefree5() {
		return bompricefree5;
	}
	public void setBompricefree5(String bompricefree5) {
		this.bompricefree5 = bompricefree5;
	}
	public String getBompricefree6() {
		return bompricefree6;
	}
	public void setBompricefree6(String bompricefree6) {
		this.bompricefree6 = bompricefree6;
	}
	public String getBompricefree7() {
		return bompricefree7;
	}
	public void setBompricefree7(String bompricefree7) {
		this.bompricefree7 = bompricefree7;
	}
	public String getBompricefree8() {
		return bompricefree8;
	}
	public void setBompricefree8(String bompricefree8) {
		this.bompricefree8 = bompricefree8;
	}
	public String getBompricefree9() {
		return bompricefree9;
	}
	public void setBompricefree9(String bompricefree9) {
		this.bompricefree9 = bompricefree9;
	}
	public String getBompricefree10() {
		return bompricefree10;
	}
	public void setBompricefree10(String bompricefree10) {
		this.bompricefree10 = bompricefree10;
	}
	public String getBsalepricefree3() {
		return bsalepricefree3;
	}
	public void setBsalepricefree3(String bsalepricefree3) {
		this.bsalepricefree3 = bsalepricefree3;
	}
	public String getBsalepricefree4() {
		return bsalepricefree4;
	}
	public void setBsalepricefree4(String bsalepricefree4) {
		this.bsalepricefree4 = bsalepricefree4;
	}
	public String getBsalepricefree5() {
		return bsalepricefree5;
	}
	public void setBsalepricefree5(String bsalepricefree5) {
		this.bsalepricefree5 = bsalepricefree5;
	}
	public String getBsalepricefree6() {
		return bsalepricefree6;
	}
	public void setBsalepricefree6(String bsalepricefree6) {
		this.bsalepricefree6 = bsalepricefree6;
	}
	public String getBsalepricefree7() {
		return bsalepricefree7;
	}
	public void setBsalepricefree7(String bsalepricefree7) {
		this.bsalepricefree7 = bsalepricefree7;
	}
	public String getBsalepricefree8() {
		return bsalepricefree8;
	}
	public void setBsalepricefree8(String bsalepricefree8) {
		this.bsalepricefree8 = bsalepricefree8;
	}
	public String getBsalepricefree9() {
		return bsalepricefree9;
	}
	public void setBsalepricefree9(String bsalepricefree9) {
		this.bsalepricefree9 = bsalepricefree9;
	}
	public String getBsalepricefree10() {
		return bsalepricefree10;
	}
	public void setBsalepricefree10(String bsalepricefree10) {
		this.bsalepricefree10 = bsalepricefree10;
	}
	public String getBbatchproperty3() {
		return bbatchproperty3;
	}
	public void setBbatchproperty3(String bbatchproperty3) {
		this.bbatchproperty3 = bbatchproperty3;
	}
	public String getBbatchproperty4() {
		return bbatchproperty4;
	}
	public void setBbatchproperty4(String bbatchproperty4) {
		this.bbatchproperty4 = bbatchproperty4;
	}
	public String getBbatchproperty5() {
		return bbatchproperty5;
	}
	public void setBbatchproperty5(String bbatchproperty5) {
		this.bbatchproperty5 = bbatchproperty5;
	}
	public String getBbatchproperty6() {
		return bbatchproperty6;
	}
	public void setBbatchproperty6(String bbatchproperty6) {
		this.bbatchproperty6 = bbatchproperty6;
	}
	public String getBbatchproperty7() {
		return bbatchproperty7;
	}
	public void setBbatchproperty7(String bbatchproperty7) {
		this.bbatchproperty7 = bbatchproperty7;
	}
	public String getBbatchproperty8() {
		return bbatchproperty8;
	}
	public void setBbatchproperty8(String bbatchproperty8) {
		this.bbatchproperty8 = bbatchproperty8;
	}
	public String getBbatchproperty9() {
		return bbatchproperty9;
	}
	public void setBbatchproperty9(String bbatchproperty9) {
		this.bbatchproperty9 = bbatchproperty9;
	}
	public String getBbatchproperty10() {
		return bbatchproperty10;
	}
	public void setBbatchproperty10(String bbatchproperty10) {
		this.bbatchproperty10 = bbatchproperty10;
	}
	public String getBcontrolfreerange3() {
		return bcontrolfreerange3;
	}
	public void setBcontrolfreerange3(String bcontrolfreerange3) {
		this.bcontrolfreerange3 = bcontrolfreerange3;
	}
	public String getBcontrolfreerange4() {
		return bcontrolfreerange4;
	}
	public void setBcontrolfreerange4(String bcontrolfreerange4) {
		this.bcontrolfreerange4 = bcontrolfreerange4;
	}
	public String getBcontrolfreerange5() {
		return bcontrolfreerange5;
	}
	public void setBcontrolfreerange5(String bcontrolfreerange5) {
		this.bcontrolfreerange5 = bcontrolfreerange5;
	}
	public String getBcontrolfreerange6() {
		return bcontrolfreerange6;
	}
	public void setBcontrolfreerange6(String bcontrolfreerange6) {
		this.bcontrolfreerange6 = bcontrolfreerange6;
	}
	public String getBcontrolfreerange7() {
		return bcontrolfreerange7;
	}
	public void setBcontrolfreerange7(String bcontrolfreerange7) {
		this.bcontrolfreerange7 = bcontrolfreerange7;
	}
	public String getBcontrolfreerange8() {
		return bcontrolfreerange8;
	}
	public void setBcontrolfreerange8(String bcontrolfreerange8) {
		this.bcontrolfreerange8 = bcontrolfreerange8;
	}
	public String getBcontrolfreerange9() {
		return bcontrolfreerange9;
	}
	public void setBcontrolfreerange9(String bcontrolfreerange9) {
		this.bcontrolfreerange9 = bcontrolfreerange9;
	}
	public String getBcontrolfreerange10() {
		return bcontrolfreerange10;
	}
	public void setBcontrolfreerange10(String bcontrolfreerange10) {
		this.bcontrolfreerange10 = bcontrolfreerange10;
	}
	
	
	
	
}
