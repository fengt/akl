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

	//��ͷ
    //receiveflag	�շ���־	int	4	�ǿ�	��ⵥ-�� TRUE�����ⵥ-�� FALSE
  	private int receiveFlag;
	//vouchtype	��������	varchar	2	�ǿ�	01���ɹ���ⵥ08��������ⵥ09���������ⵥ10������Ʒ��ⵥ11�����ϳ��ⵥ32�����۳��ⵥ
  	private String vouchType;
	//businesstype	ҵ������	varchar	8	�ɿ�	��ҵ��ɹ���ⵥ��ҵ�����ͣ���ͨ�ɹ������д�����ֱ�����۹�ҵ��ɹ���ⵥ��ҵ�����ͣ���ͨ�ɹ����۳��ⵥ��ҵ�����ͣ���ͨ���ۡ�ί�д�����ֱ�����ۡ������տ�������ⵥ��ҵ�����ͣ�������⡢��ӯ��ⵥ����װ��⡢��ж��⡢ת����⡢������⡣�������ⵥ��ҵ�����ͣ��������⡢��ӯ���ⵥ����װ���⡢��ж���⡢ת�����⡢�������⡣���ϳ��ⵥ��ҵ�����ͣ��޶����ϡ����ϳ���
	private String businessType;
	//source	������Դ	varchar	10	�ǿ�	���Ƶ��ݵ�ģ�飺�����ɹ������ۡ���桢�����
	private String source;
	//businesscode	��Ӧҵ�񵥺�	varchar	10	�ɿ�	������ⵥ��Ӧ��ҵ������Ϊ��������⡢��ӯ��ⵥ����װ��⡢��ж��⡢ת����⣻�������ⵥ��Ӧ��ҵ������Ϊ���������⡢��ӯ���ⵥ����װ���⡢��ж���⡢ת�����⣬��Ӧ�ж�Ӧ�ĵ��������̵㵥����װ������ж������̬ת�����ĵ��ݺš�
	private String businessCode;
	//warehousecode	�ֿ����	varchar	10	�ǿ�	�����ǲֿ⵵�������еĲֿ�
	private String warehouseCode;
	//date	��������	datetime	8	�ǿ�	����������ڱ�����ڵ��ڵ�ǰ����µĵ�һ��
	private String dateTime;
	//code	���ݺ�	varchar	10	�ǿ�	������ⵥ�ű�������û����õĵ��ݱ�Ź���
	private String code;
	//receivecode	�շ�������	varchar	5	�ɿ�	�������շ���𵵰������е����
	private String receiveCode;
	//departmentcode	���ű���	varchar	12	�ɿ�	�����ǲ��ŵ��������еĲ���
	private String departmentCode;
	//personcode	ְԱ����	varchar	8	�ɿ�	������ְԱ���������е�ְԱ
	private String personCode;
	//purchasecode	�ɹ����ͱ���	varchar	2	�ɿ�	�ɹ���ⵥ�ã������ǲɹ����͵��������е�����
	private String purchaseCode;
	//saletypecode	�������ͱ���	varchar	2	�ɿ�	���۳��ⵥ�ã��������������͵��������е�����
	private String saletyeCode;
	//vendorcode	�ͻ�����	varchar	20	�ɿ�	���۳��ⵥ���������ⵥ�á������ǿͻ����������еĿͻ�
	private String vendorCode;
	//shipaddress     ������ַ        nvarchar 200    �ɿ�
	private String Shipaddress;
	//customercode	��Ӧ�̱���	varchar	20	�ɿ�	�ɹ���ⵥ��������ⵥ�á������ǹ�Ӧ�̵��������еĹ�Ӧ��
	private String customerCode;
	//ordercode	������	varchar	10	�ɿ�	�ɹ���ⵥָ��Ӧ�Ĳɹ������š�����Ʒ��⡢���ϳ��⡢ָ��Ӧ�����������š�
	private String orderCode;
	//quantity	����	float	8	�ɿ�	
	private BigDecimal quantity;
	//arrivecode	��������	varchar	10	�ɿ�	�ɹ���ⵥ��
	private String arriveCode;
	//billcode	��Ʊ��	int	4	�ɿ�	�ɹ���ⵥ�����۳��ⵥ��
	private int billCode;
	//consignmentcode	��������	int	4	�ɿ�	���۳��ⵥ��
	private int cunsignmentCode;
	//arrivedate	��������	datetime		�ɿ�	
	private String arriveDate;
	//checkcode	���鵥��	varchar		�ɿ�	����=���鵥��
	private String checkCode;
	//checkdate	��������	datetime		�ɿ�	
	private String checkDate;
	//checkperson	����Ա	varchar	8	�ɿ�	������ְԱ���������е�ְԱ
	private String checkPerson;
	//templatenumber	ģ���	int	4	�ɿ�	�������Ӧ���ģ����Ƿ�Ϸ���������Ϸ���ȡ��ǰ��������Ĭ�ϵ�ģ��š�
	private int templateNumber;
	//serial	��������	varchar	12	�ɿ�	����Ʒ��ⵥ�����ϳ��ⵥ��
	private String serial;
	//handler	������	varchar	20	�ɿ�	������ְԱ���������е�ְԱ
	private String handler;
	//memory	��ע	varchar	60	�ɿ�	
	private String memory;
	//maker	�Ƶ���	varchar	20	�ǿ�	
	private String marker;
	/**define1	�Զ����ֶ�1	varchar	20	�ɿ�	
	
	define2	�Զ����ֶ�2	varchar	20	�ɿ�	
	
	define3	�Զ����ֶ�3	varchar	20	�ɿ�	
	
	define4	�Զ����ֶ�4	datetime	8	�ɿ�	
	
	define5	�Զ����ֶ�5	int	4	�ɿ�	
	
	define6	�Զ����ֶ�6	datetime	8	�ɿ�	
	
	define7	�Զ����ֶ�7	float	8	�ɿ�	
	
	define8	�Զ����ֶ�8	varchar	4	�ɿ�	
	
	define9	�Զ����ֶ�9	varchar	8	�ɿ�	
	define10	�Զ����ֶ�10	varchar	60	�ɿ�	
	define11	�Զ����ֶ�11	varchar	120	�ɿ�	
	define12	�Զ����ֶ�12	varchar	120	�ɿ�	
	define13	�Զ����ֶ�13	varchar	120	�ɿ�	
	define14	�Զ����ֶ�14	varchar	120	�ɿ�	
	define15	�Զ����ֶ�15	int	4	�ɿ�	
	define16	�Զ����ֶ�16	float	8	�ɿ�	
	auditdate	�������	datetime	8	�ɿ�	*/
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
