/**
 * 
 */
package cn.com.akl.u8.entity;

import java.math.BigDecimal;

/**
 * @author hzy
 *
 */
public class StoreinEntity {

	//单头
    //receiveflag	收发标志	int	4	非空	入库单-收 TRUE，出库单-发 FALSE
  	private int receiveFlag;
	//vouchtype	单据类型	varchar	2	非空	01：采购入库单08：其他入库单09：其他出库单10：产成品入库单11：材料出库单32：销售出库单
  	private String vouchType;
	//businesstype	业务类型	varchar	8	可空	商业版采购入库单的业务类型：普通采购、受托代销、直运销售工业版采购入库单的业务类型：普通采购销售出库单的业务类型：普通销售、委托代销、直运销售、分期收款其他入库单的业务类型：调拨入库、盘盈入库单、组装入库、拆卸入库、转换入库、其他入库。其他出库单的业务类型：调拨出库、盘盈出库单、组装出库、拆卸出库、转换出库、其他出库。材料出库单的业务类型：限额领料、材料出库
	private String businessType;
	//source	单据来源	varchar	10	非空	填制单据的模块：包括采购、销售、库存、存货。
	private String source;
	//businesscode	对应业务单号	varchar	10	可空	其它入库单对应的业务类型为：调拨入库、盘盈入库单、组装入库、拆卸入库、转换入库；其他出库单对应的业务类型为：调拨出库、盘盈出库单、组装出库、拆卸出库、转换出库，则应有对应的调拨单、盘点单、组装单、拆卸单、形态转换单的单据号。
	private String businessCode;
	//warehousecode	仓库编码	varchar	10	非空	必须是仓库档案中已有的仓库
	private String warehouseCode;
	//date	单据日期	datetime	8	非空	出、入库日期必须大于等于当前会计月的第一天
	private String dateTime;
	//code	单据号	varchar	10	非空	出、入库单号必须符合用户设置的单据编号规则
	private String code;
	//receivecode	收发类别编码	varchar	5	可空	必须是收发类别档案中已有的类别
	private String receiveCode;
	//departmentcode	部门编码	varchar	12	可空	必须是部门档案中已有的部门
	private String departmentCode;
	//personcode	职员编码	varchar	8	可空	必须是职员档案中已有的职员
	private String personCode;
	//purchasecode	采购类型编码	varchar	2	可空	采购入库单用，必须是采购类型档案中已有的类型
	private String purchaseCode;
	//saletypecode	销售类型编码	varchar	2	可空	销售出库单用，必须是销售类型档案中已有的类型
	private String saletyeCode;
	//vendorcode	客户编码	varchar	20	可空	销售出库单、其他出库单用。必须是客户档案中已有的客户
	private String vendorCode;
	//shipaddress     发货地址        nvarchar 200    可空
	private String Shipaddress;
	//customercode	供应商编码	varchar	20	可空	采购入库单、其他入库单用。必须是供应商档案中已有的供应商
	private String customerCode;
	//ordercode	订单号	varchar	10	可空	采购入库单指对应的采购订单号。产成品入库、材料出库、指对应的生产订单号。
	private String orderCode;
	//quantity	产量	float	8	可空	
	private BigDecimal quantity;
	//arrivecode	到货单号	varchar	10	可空	采购入库单用
	private String arriveCode;
	//billcode	发票号	int	4	可空	采购入库单、销售出库单用
	private int billCode;
	//consignmentcode	发货单号	int	4	可空	销售出库单用
	private int cunsignmentCode;
	//arrivedate	到货日期	datetime		可空	
	private String arriveDate;
	//checkcode	检验单号	varchar		可空	长度=检验单号
	private String checkCode;
	//checkdate	检验日期	datetime		可空	
	private String checkDate;
	//checkperson	检验员	varchar	8	可空	必须是职员档案中已有的职员
	private String checkPerson;
	//templatenumber	模版号	int	4	可空	如果不空应检查模版号是否合法，如果不合法则取当前单据类型默认的模版号。
	private int templateNumber;
	//serial	生产批号	varchar	12	可空	产成品入库单、材料出库单用
	private String serial;
	//handler	经手人	varchar	20	可空	必须是职员档案中已有的职员
	private String handler;
	//memory	备注	varchar	60	可空	
	private String memory;
	//maker	制单人	varchar	20	非空	
	private String marker;
	/**define1	自定义字段1	varchar	20	可空	
	
	define2	自定义字段2	varchar	20	可空	
	
	define3	自定义字段3	varchar	20	可空	
	
	define4	自定义字段4	datetime	8	可空	
	
	define5	自定义字段5	int	4	可空	
	
	define6	自定义字段6	datetime	8	可空	
	
	define7	自定义字段7	float	8	可空	
	
	define8	自定义字段8	varchar	4	可空	
	
	define9	自定义字段9	varchar	8	可空	
	define10	自定义字段10	varchar	60	可空	
	define11	自定义字段11	varchar	120	可空	
	define12	自定义字段12	varchar	120	可空	
	define13	自定义字段13	varchar	120	可空	
	define14	自定义字段14	varchar	120	可空	
	define15	自定义字段15	int	4	可空	
	define16	自定义字段16	float	8	可空	
	auditdate	审核日期	datetime	8	可空	*/
	/**
	 * @return the receiveFlag
	 */
	public int getReceiveFlag() {
		return receiveFlag;
	}
	/**
	 * @param receiveFlag the receiveFlag to set
	 */
	public void setReceiveFlag(int receiveFlag) {
		this.receiveFlag = receiveFlag;
	}
	/**
	 * @return the vouchType
	 */
	public String getVouchType() {
		return vouchType;
	}
	/**
	 * @param vouchType the vouchType to set
	 */
	public void setVouchType(String vouchType) {
		this.vouchType = vouchType;
	}
	/**
	 * @return the businessType
	 */
	public String getBusinessType() {
		return businessType;
	}
	/**
	 * @param businessType the businessType to set
	 */
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}
	/**
	 * @return the businessCode
	 */
	public String getBusinessCode() {
		return businessCode;
	}
	/**
	 * @param businessCode the businessCode to set
	 */
	public void setBusinessCode(String businessCode) {
		this.businessCode = businessCode;
	}
	/**
	 * @return the warehouseCode
	 */
	public String getWarehouseCode() {
		return warehouseCode;
	}
	/**
	 * @param warehouseCode the warehouseCode to set
	 */
	public void setWarehouseCode(String warehouseCode) {
		this.warehouseCode = warehouseCode;
	}
	/**
	 * @return the dateTime
	 */
	public String getDateTime() {
		return dateTime;
	}
	/**
	 * @param dateTime the dateTime to set
	 */
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @return the receiveCode
	 */
	public String getReceiveCode() {
		return receiveCode;
	}
	/**
	 * @param receiveCode the receiveCode to set
	 */
	public void setReceiveCode(String receiveCode) {
		this.receiveCode = receiveCode;
	}
	/**
	 * @return the departmentCode
	 */
	public String getDepartmentCode() {
		return departmentCode;
	}
	/**
	 * @param departmentCode the departmentCode to set
	 */
	public void setDepartmentCode(String departmentCode) {
		this.departmentCode = departmentCode;
	}
	/**
	 * @return the personCode
	 */
	public String getPersonCode() {
		return personCode;
	}
	/**
	 * @param personCode the personCode to set
	 */
	public void setPersonCode(String personCode) {
		this.personCode = personCode;
	}
	/**
	 * @return the purchaseCode
	 */
	public String getPurchaseCode() {
		return purchaseCode;
	}
	/**
	 * @param purchaseCode the purchaseCode to set
	 */
	public void setPurchaseCode(String purchaseCode) {
		this.purchaseCode = purchaseCode;
	}
	/**
	 * @return the saletyeCode
	 */
	public String getSaletyeCode() {
		return saletyeCode;
	}
	/**
	 * @param saletyeCode the saletyeCode to set
	 */
	public void setSaletyeCode(String saletyeCode) {
		this.saletyeCode = saletyeCode;
	}
	/**
	 * @return the vendorCode
	 */
	public String getVendorCode() {
		return vendorCode;
	}
	/**
	 * @param vendorCode the vendorCode to set
	 */
	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}
	/**
	 * @return the shipaddress
	 */
	public String getShipaddress() {
		return Shipaddress;
	}
	/**
	 * @param shipaddress the shipaddress to set
	 */
	public void setShipaddress(String shipaddress) {
		Shipaddress = shipaddress;
	}
	/**
	 * @return the customerCode
	 */
	public String getCustomerCode() {
		return customerCode;
	}
	/**
	 * @param customerCode the customerCode to set
	 */
	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}
	/**
	 * @return the orderCode
	 */
	public String getOrderCode() {
		return orderCode;
	}
	/**
	 * @param orderCode the orderCode to set
	 */
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	/**
	 * @return the quantity
	 */
	public BigDecimal getQuantity() {
		return quantity;
	}
	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}
	/**
	 * @return the arriveCode
	 */
	public String getArriveCode() {
		return arriveCode;
	}
	/**
	 * @param arriveCode the arriveCode to set
	 */
	public void setArriveCode(String arriveCode) {
		this.arriveCode = arriveCode;
	}
	/**
	 * @return the billCode
	 */
	public int getBillCode() {
		return billCode;
	}
	/**
	 * @param billCode the billCode to set
	 */
	public void setBillCode(int billCode) {
		this.billCode = billCode;
	}
	/**
	 * @return the cunsignmentCode
	 */
	public int getCunsignmentCode() {
		return cunsignmentCode;
	}
	/**
	 * @param cunsignmentCode the cunsignmentCode to set
	 */
	public void setCunsignmentCode(int cunsignmentCode) {
		this.cunsignmentCode = cunsignmentCode;
	}
	/**
	 * @return the arriveDate
	 */
	public String getArriveDate() {
		return arriveDate;
	}
	/**
	 * @param arriveDate the arriveDate to set
	 */
	public void setArriveDate(String arriveDate) {
		this.arriveDate = arriveDate;
	}
	/**
	 * @return the checkCode
	 */
	public String getCheckCode() {
		return checkCode;
	}
	/**
	 * @param checkCode the checkCode to set
	 */
	public void setCheckCode(String checkCode) {
		this.checkCode = checkCode;
	}
	/**
	 * @return the checkDate
	 */
	public String getCheckDate() {
		return checkDate;
	}
	/**
	 * @param checkDate the checkDate to set
	 */
	public void setCheckDate(String checkDate) {
		this.checkDate = checkDate;
	}
	/**
	 * @return the checkPerson
	 */
	public String getCheckPerson() {
		return checkPerson;
	}
	/**
	 * @param checkPerson the checkPerson to set
	 */
	public void setCheckPerson(String checkPerson) {
		this.checkPerson = checkPerson;
	}
	/**
	 * @return the templateNumber
	 */
	public int getTemplateNumber() {
		return templateNumber;
	}
	/**
	 * @param templateNumber the templateNumber to set
	 */
	public void setTemplateNumber(int templateNumber) {
		this.templateNumber = templateNumber;
	}
	/**
	 * @return the serial
	 */
	public String getSerial() {
		return serial;
	}
	/**
	 * @param serial the serial to set
	 */
	public void setSerial(String serial) {
		this.serial = serial;
	}
	/**
	 * @return the handler
	 */
	public String getHandler() {
		return handler;
	}
	/**
	 * @param handler the handler to set
	 */
	public void setHandler(String handler) {
		this.handler = handler;
	}
	/**
	 * @return the memory
	 */
	public String getMemory() {
		return memory;
	}
	/**
	 * @param memory the memory to set
	 */
	public void setMemory(String memory) {
		this.memory = memory;
	}
	/**
	 * @return the marker
	 */
	public String getMarker() {
		return marker;
	}
	/**
	 * @param marker the marker to set
	 */
	public void setMarker(String marker) {
		this.marker = marker;
	}
	
	
	}
