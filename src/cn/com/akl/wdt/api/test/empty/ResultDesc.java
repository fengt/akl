package cn.com.akl.wdt.api.test.empty;
/**
 * ����Ԫ��˵��
 * */
public class ResultDesc {

	private int ResultCode;//������ս��0�ɣ���0ʧ��
	private String ResultMag;//����ʧ��ԭ��
	private int TotalCount;//��ѯ���ĵ�����
	
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
