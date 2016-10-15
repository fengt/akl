package cn.com.akl.wdt.api.test.empty;
/**
 * 返回元素说明
 * */
public class ResultDesc {

	private int ResultCode;//请求接收结果0成；非0失败
	private String ResultMag;//请求失败原因
	private int TotalCount;//查询到的单据数
	
	public int getTotalCount() {
		return TotalCount;
	}
	public int setTotalCount(int totalCount) {
		return TotalCount = totalCount;
	}
	public int getResultCode() {
		return ResultCode;
	}
	public void setResultCode(int resultCode) {
		ResultCode = resultCode;
	}
	public String getResultMag() {
		return ResultMag;
	}
	public void setResultMag(String resultMag) {
		ResultMag = resultMag;
	}
	
}
