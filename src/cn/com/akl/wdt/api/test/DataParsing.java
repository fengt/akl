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
	  * 解析
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws SQLException 
	 * @throws InterruptedException 
	  */
	public void resultCount() throws IOException, JSONException, SQLException, InterruptedException{
		int page=1;
		HttpURLConnectionPost hp=new HttpURLConnectionPost();
	
		String str=hp.readContentFromPost(page);//调用接口获取返回JSON
		
		JSONObject json=new JSONObject(str);//初始化解析数据
		ResultDesc rd=new ResultDesc();
		
		//接受结果
		rd.setResultMag(json.getString("ResultMsg"));//失败原因
		rd.setTotalCount(json.getInt("TotalCount"));//单据数量
	
		Double number=json.getInt("TotalCount")/40.0;//计算页面数量
		 page = number.intValue();
		if(page < number.doubleValue()){
			page++;
		}
		
	
						 
		//根据页面数量循环调用接口获取返回JSON
		for (int i = 1; i <= page; i++) {
			Thread.sleep(500);
			str=hp.readContentFromPost(i);
		
			json=new JSONObject(str);//初始化解析数据
			returnResult(json);
			
		}
		
	}

	/**
	 * 
	 * 解析订单货品信息
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
				
					for (int i = 0; i < tradeObj.length(); i++) {//订单
						TradeListJ tl=new TradeListJ();//订单信息
						JSONObject regtimeObj=tradeObj.getJSONObject(i);//Trade
						//存入返回订单信息
						
						tl.setTradeNo(regtimeObj.getString("TradeNO"));//ERP内订单编号
						
						tl.setTradeNO2(regtimeObj.getString("TradeNO2"));//来源单号
					
						tl.setWarehouseNo(regtimeObj.getString("WarehouseNO"));//仓库编号
				
						tl.setRegTime(regtimeObj.getString("RegTime"));//订单创建时间
					
						tl.setTradeTime(regtimeObj.getString("TradeTime"));//交易时间
					
						tl.setPayTime(regtimeObj.getString("PayTime"));//付款时间
					
						tl.setChkTime(regtimeObj.getString("ChkTime"));//审单时间
					
						tl.setStockOutTime(regtimeObj.getString("StockOutTime"));//出库时间
					
						tl.setSndTime(regtimeObj.getString("SndTime"));//发货时间
				
						tl.setLastModifyTime(regtimeObj.getString("LastModifyTime"));//最后修改时间
						
						tl.setTradeStatus(regtimeObj.getString("TradeStatus"));//订单状态
					
						tl.setRefundStatus(regtimeObj.getString("RefundStatus"));//退款状态
					
						tl.setbInvoice(regtimeObj.getInt("bInvoice"));//是否需要发票
						
						tl.setInvoiceTitle(regtimeObj.getString("InvoiceTitle"));//发票抬头
						
						tl.setInvoiceContent(regtimeObj.getString("InvoiceContent"));//发票内容
						
						tl.setNickName(regtimeObj.getString("NickName"));//客户网名
					
						tl.setSndTo(regtimeObj.getString("SndTo"));//收件人姓名
						
						tl.setCountry(regtimeObj.getString("Country"));//收件人国家
					
						tl.setProvince(regtimeObj.getString("Province"));//收件人省份
					
						tl.setCity(regtimeObj.getString("City"));//收件人城市
					
						tl.setTown(regtimeObj.getString("Town"));//收件人区县
					
						tl.setAdr(regtimeObj.getString("Adr"));//收件人地址
						
						tl.setTel(regtimeObj.getString("Tel"));//收件人电话
					
						tl.setZip(regtimeObj.getString("Zip"));//收件人邮编
						
						tl.setChargeType(regtimeObj.getString("ChargeType"));//付款方式
						
						tl.setSellSkuCount(regtimeObj.getString("SellSkuCount"));//货品数量
					
						tl.setGoodsTotal(regtimeObj.getString("GoodsTotal"));//货品总额
						
						tl.setPostageTotal(regtimeObj.getString("PostageTotal"));//应收邮费
						
						tl.setFavourableTotal(regtimeObj.getString("FavourableTotal"));//订单总优惠
					
						tl.setAllTotal(regtimeObj.getString("AllTotal"));//应收金额
					
						tl.setLogisticsCode(regtimeObj.getString("LogisticsCode"));//物流公司编码
					
						tl.setPostID(regtimeObj.getString("PostID"));//货运单号
						
						tl.setCustomerRemark(regtimeObj.getString("CustomerRemark"));//买家留言
						
						tl.setRemark(regtimeObj.getString("Remark"));//卖家备注
						
						tl.setShopType(regtimeObj.getString("ShopType"));//平台店铺类型
						
						tl.setShopName(regtimeObj.getString("ShopName"));//平台店铺名称
						
						tl.setTradeFlag(regtimeObj.getString("TradeFlag"));//ERP中的订单标记名称
					
						tl.setChkOperatorName(regtimeObj.getString("ChkOperatorName"));//审单员名称
						 Calendar c = Calendar.getInstance();  
							//获取前一天
						    c.add(Calendar.DAY_OF_MONTH, -1);  
						    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
						    String createTime=formatter.format(c.getTime());
						    String ct=createTime.substring(8,10);//前一天
						//发货时间
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
					
						
							
							for (int j = 0; j < detailObj.length(); j++) {//货品
								DetailList dl=new DetailList();//货品信息
								JSONObject detailArrayObj = detailObj.getJSONObject(j); //DetailList
								String deptNumber=null;
							
								
								dl.setSkuCode(detailArrayObj.getString("SkuCode"));//ERP内SKU唯一标识商家编号
								dl.setSkuName(detailArrayObj.getString("SkuName"));//货品SKU名称
								dl.setPlatformGoodsCode(detailArrayObj.getString("PlatformGoodsCode"));//平台货品编号
								dl.setPlatformGoodsName(detailArrayObj.getString("PlatformGoodsName"));//平台货品名称
								dl.setPlatformSkuCode(detailArrayObj.getString("PlatformSkuCode"));//平台SKU编码
								dl.setPlatformSkuName(detailArrayObj.getString("PlatformSkuName"));//平台SKU名称
								dl.setSellCount(detailArrayObj.getString("SellCount"));//卖出数量
								dl.setSellPrice(detailArrayObj.getString("SellPrice"));//商品零售价
								dl.setDiscountMoney(detailArrayObj.getString("DiscountMoney"));//货品优惠金额
								dl.setbGift(detailArrayObj.getInt("bGift"));//是否赠品，是为1，否为0
								dl.setTRADENO(regtimeObj.getString("TradeNO"));
								
					try {
						if(tl.getShopName().equals("中信-中信银行邮购")){
							deptNumber= DBSql.getString(conn, "SELECT a.deptNumber FROM BO_AKL_WDT_Dept a, BO_AKL_WLXX w WHERE w.ppid = a.ppid and shopname = '中信-中信银行邮购' AND w.WLBH= '"+dl.getPlatformGoodsCode()+"'", "deptNumber");
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
