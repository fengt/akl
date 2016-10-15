package cn.com.akl.wdt.api.test;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.util.DBSql;

import cn.com.akl.util.StrUtil;
import cn.com.akl.wdt.api.test.empty.DetailList;
import cn.com.akl.wdt.api.test.empty.ResultDesc;
import cn.com.akl.wdt.api.test.empty.TradeListJ;

public class DataParsing implements IJob {

	List<DetailList> detail=new ArrayList<DetailList>();
	List<TradeListJ> trade=new ArrayList<TradeListJ>();
	
	 /**
	  * ����
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws SQLException 
	 * @throws InterruptedException 
	  */
	public void resultCount() throws IOException, JSONException, SQLException, InterruptedException{
		int page=1;
		HttpURLConnectionPost hp=new HttpURLConnectionPost();
	
		String str=hp.readContentFromPost(page);//���ýӿڻ�ȡ����JSON
		
		JSONObject json=new JSONObject(str);//��ʼ����������
		ResultDesc rd=new ResultDesc();
		
		//���ܽ��
		rd.setResultMag(json.getString("ResultMsg"));//ʧ��ԭ��
		rd.setTotalCount(json.getInt("TotalCount"));//��������
	
		Double number=json.getInt("TotalCount")/40.0;//����ҳ������
		 page = number.intValue();
		if(page < number.doubleValue()){
			page++;
		}
		
	
						 
		//����ҳ������ѭ�����ýӿڻ�ȡ����JSON
		for (int i = 1; i <= page; i++) {
			Thread.sleep(500);
			str=hp.readContentFromPost(i);
		
			json=new JSONObject(str);//��ʼ����������
			returnResult(json);
			
		}
		
	}

	/**
	 * 
	 * ����������Ʒ��Ϣ
	 * @throws SQLException 
	 * 
	 * */

	
	public  void returnResult(JSONObject js)throws IOException, JSONException, SQLException{
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet reset = null;
			
			try {
				
				conn = DBSql.open();
				JSONObject tradeList = js.getJSONObject("TradeList");
				JSONArray tradeObj = tradeList.getJSONArray("Trade");
				
					for (int i = 0; i < tradeObj.length(); i++) {//����
						TradeListJ tl=new TradeListJ();//������Ϣ
						JSONObject regtimeObj=tradeObj.getJSONObject(i);//Trade
						//���뷵�ض�����Ϣ
						
						tl.setTradeNo(regtimeObj.getString("TradeNO"));//ERP�ڶ������
						
						tl.setTradeNO2(regtimeObj.getString("TradeNO2"));//��Դ����
					
						tl.setWarehouseNo(regtimeObj.getString("WarehouseNO"));//�ֿ���
				
						tl.setRegTime(regtimeObj.getString("RegTime"));//��������ʱ��
					
						tl.setTradeTime(regtimeObj.getString("TradeTime"));//����ʱ��
					
						tl.setPayTime(regtimeObj.getString("PayTime"));//����ʱ��
					
						tl.setChkTime(regtimeObj.getString("ChkTime"));//��ʱ��
					
						tl.setStockOutTime(regtimeObj.getString("StockOutTime"));//����ʱ��
					
						tl.setSndTime(regtimeObj.getString("SndTime"));//����ʱ��
				
						tl.setLastModifyTime(regtimeObj.getString("LastModifyTime"));//����޸�ʱ��
						
						tl.setTradeStatus(regtimeObj.getString("TradeStatus"));//����״̬
					
						tl.setRefundStatus(regtimeObj.getString("RefundStatus"));//�˿�״̬
					
						tl.setbInvoice(regtimeObj.getInt("bInvoice"));//�Ƿ���Ҫ��Ʊ
						
						tl.setInvoiceTitle(regtimeObj.getString("InvoiceTitle"));//��Ʊ̧ͷ
						
						tl.setInvoiceContent(regtimeObj.getString("InvoiceContent"));//��Ʊ����
						
						tl.setNickName(regtimeObj.getString("NickName"));//�ͻ�����
					
						tl.setSndTo(regtimeObj.getString("SndTo"));//�ռ�������
						
						tl.setCountry(regtimeObj.getString("Country"));//�ռ��˹���
					
						tl.setProvince(regtimeObj.getString("Province"));//�ռ���ʡ��
					
						tl.setCity(regtimeObj.getString("City"));//�ռ��˳���
					
						tl.setTown(regtimeObj.getString("Town"));//�ռ�������
					
						tl.setAdr(regtimeObj.getString("Adr"));//�ռ��˵�ַ
						
						tl.setTel(regtimeObj.getString("Tel"));//�ռ��˵绰
					
						tl.setZip(regtimeObj.getString("Zip"));//�ռ����ʱ�
						
						tl.setChargeType(regtimeObj.getString("ChargeType"));//���ʽ
						
						tl.setSellSkuCount(regtimeObj.getString("SellSkuCount"));//��Ʒ����
					
						tl.setGoodsTotal(regtimeObj.getString("GoodsTotal"));//��Ʒ�ܶ�
						
						tl.setPostageTotal(regtimeObj.getString("PostageTotal"));//Ӧ���ʷ�
						
						tl.setFavourableTotal(regtimeObj.getString("FavourableTotal"));//�������Ż�
					
						tl.setAllTotal(regtimeObj.getString("AllTotal"));//Ӧ�ս��
					
						tl.setLogisticsCode(regtimeObj.getString("LogisticsCode"));//������˾����
					
						tl.setPostID(regtimeObj.getString("PostID"));//���˵���
						
						tl.setCustomerRemark(regtimeObj.getString("CustomerRemark"));//�������
						
						tl.setRemark(regtimeObj.getString("Remark"));//���ұ�ע
						
						tl.setShopType(regtimeObj.getString("ShopType"));//ƽ̨��������
						
						tl.setShopName(regtimeObj.getString("ShopName"));//ƽ̨��������
						
						tl.setTradeFlag(regtimeObj.getString("TradeFlag"));//ERP�еĶ����������
					
						tl.setChkOperatorName(regtimeObj.getString("ChkOperatorName"));//��Ա����
						 Calendar c = Calendar.getInstance();  
							//��ȡǰһ��
						    c.add(Calendar.DAY_OF_MONTH, -1);  
						    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
						    String createTime=formatter.format(c.getTime());
						    String ct=createTime.substring(8,10);//ǰһ��
						//����ʱ��
						String snd=tl.getSndTime().substring(8, 10);
				
						if(ct.equals(snd)){
							DBSql.executeUpdate(conn,"INSERT INTO BO_AKL_WDT_TRADE_P (TRADENO,TRADENO2,WAREHOUSENO,REGTIME,TRADETIME,PAYTIME,CHKTIME,STOCKOUTTIME," +
									"SNDTIME,LASTMODIFYTIME,TRADESTATUS,REFUNDSTATUS,BINVOICE,INVOICETITLE,INVOICECONTENT,NICKNAME,SNDTO,COUNTRY,PROVINCE," +
									"CITY,TOWN,ADR,TEL,ZIP,CHARGETYPE,SELLSKUCOUNT,GOODSTOTAL,POSTAGETOTAL,FAVOURABLETOTAL,ALLTOTAL,LOGISTICSCODE," +
									"POSTID,CUSTOMERREMARK,REMARK,SHOPTYPE,SHOPNAME,TRADEFLAG,CHKOPERATORNAME) VALUES	('"+tl.getTradeNo()+"','"+tl.getTradeNO2()+"','"+tl.getWarehouseNo()+"','"+
									tl.getRegTime()+"','"+tl.getTradeTime()+"','"+tl.getPayTime()+"','"+tl.getChkTime()+"','"+tl.getStockOutTime()+"','"+tl.getSndTime()+"','"+
									tl.getLastModifyTime()+"','"+tl.getTradeStatus()+"','"+tl.getRefundStatus()+"',"+tl.getbInvoice()+",'"+StrUtil.returnStr(tl.getInvoiceTitle())+"','" +
									StrUtil.returnStr(tl.getInvoiceContent())+"','"+tl.getNickName()+"','"+tl.getSndTo()+"','"+StrUtil.returnStr(tl.getCountry())+"','"+tl.getProvince()+"','"+tl.getCity()+"','" +
									tl.getTown()+"','"+tl.getAdr()+"','"+tl.getTel()+"','"+StrUtil.returnStr(tl.getZip())+"',"+StrUtil.returnStr(tl.getChargeType())+","+StrUtil.returnStr(tl.getSellSkuCount())+","+tl.getGoodsTotal()+","+
									tl.getPostageTotal()+","+tl.getFavourableTotal()+","+tl.getAllTotal()+",'"+tl.getLogisticsCode()+"','"+tl.getPostID()+"','" +
									StrUtil.returnStr(tl.getCustomerRemark())+"','"+StrUtil.returnStr(tl.getRemark())+"','"+tl.getShopType()+"','"+tl.getShopName()+"','"+StrUtil.returnStr(tl.getTradeFlag())+"','"+StrUtil.returnStr(tl.getChkOperatorName())+"')");
							
							JSONObject detailListObj = regtimeObj.getJSONObject("DetailList");
							JSONArray detailObj = detailListObj.getJSONArray("Detail");
					
						
							
							for (int j = 0; j < detailObj.length(); j++) {//��Ʒ
								DetailList dl=new DetailList();//��Ʒ��Ϣ
								JSONObject detailArrayObj = detailObj.getJSONObject(j); //DetailList
								String deptNumber=null;
							
								
								dl.setSkuCode(detailArrayObj.getString("SkuCode"));//ERP��SKUΨһ��ʶ�̼ұ��
								dl.setSkuName(detailArrayObj.getString("SkuName"));//��ƷSKU����
								dl.setPlatformGoodsCode(detailArrayObj.getString("PlatformGoodsCode"));//ƽ̨��Ʒ���
								dl.setPlatformGoodsName(detailArrayObj.getString("PlatformGoodsName"));//ƽ̨��Ʒ����
								dl.setPlatformSkuCode(detailArrayObj.getString("PlatformSkuCode"));//ƽ̨SKU����
								dl.setPlatformSkuName(detailArrayObj.getString("PlatformSkuName"));//ƽ̨SKU����
								dl.setSellCount(detailArrayObj.getString("SellCount"));//��������
								dl.setSellPrice(detailArrayObj.getString("SellPrice"));//��Ʒ���ۼ�
								dl.setDiscountMoney(detailArrayObj.getString("DiscountMoney"));//��Ʒ�Żݽ��
								dl.setbGift(detailArrayObj.getInt("bGift"));//�Ƿ���Ʒ����Ϊ1����Ϊ0
								dl.setTRADENO(regtimeObj.getString("TradeNO"));
								
					try {
						if(tl.getShopName().equals("����-���������ʹ�")){
							deptNumber= DBSql.getString(conn, "SELECT a.deptNumber FROM BO_AKL_WDT_Dept a, BO_AKL_WLXX w WHERE w.ppid = a.ppid and shopname = '����-���������ʹ�' AND w.WLBH= '"+dl.getPlatformGoodsCode()+"'", "deptNumber");
						}else{
							deptNumber = DBSql.getString(conn, "SELECT deptNumber FROM bo_akl_WDT_Dept WHERE ShopName='"+tl.getShopName()+"'", "deptNumber");
						}
					
						DBSql.executeUpdate(conn,"INSERT INTO BO_AKL_WDT_TRADE_S (PID,SkuCode,SKUNAME,PlatformGoodsCode,PlatformGoodsName,PlatformSkuCode,PlatformSkuName,SellCount,SellPrice,DISCOUNTMONEY,bGift,deptNumber) " +
	"VALUES('"+dl.getTRADENO()+"','"+dl.getSkuCode()+"','"+dl.getSkuName()+"','"+StrUtil.returnStr(dl.getPlatformGoodsCode())+"','"+StrUtil.returnStr(dl.getPlatformGoodsName())+"','"+StrUtil.returnStr(dl.getPlatformSkuCode())+"','"+StrUtil.returnStr(dl.getPlatformSkuName())+"','"+dl.getSellCount()+"','"+
								dl.getSellPrice()+"','"+StrUtil.returnStr(dl.getDiscountMoney())+"','"+dl.getbGift()+"','"+deptNumber+"')");

					} catch (SQLException e) {
						e.printStackTrace();
					}
								
							}
				}
			

			}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				
				DBSql.close(conn,ps, reset);
			}
		 	
			
	 }

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			
			resultCount();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 

	
}
