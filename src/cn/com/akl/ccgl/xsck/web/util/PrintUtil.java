package cn.com.akl.ccgl.xsck.web.util;

/**
 * ���ⵥ��ӡ������.
 * 
 * @author huangming
 *
 */
public class PrintUtil {

	private PrintUtil() {
	}
	
	/**
	 *  ��ӡÿҳ�������.
	 */
	public static final int PAGE_SIZE = 10;

	/**
	 * �����м�¼װ�����ַ���.
	 * @param args
	 * @return
	 */
	public static String formatBodyRowRecord(String... args) {
		StringBuilder sb = new StringBuilder();
		for (String string : args) {
			if (string != null) {
				sb.append("<td class='subtable_body_td'>").append(string).append("</td>");
			} else {
				sb.append("<td class='subtable_body_td'>&nbsp;</td>");
			}
		}
		return sb.toString();
	}

	/**
	 * �����м�¼װ�����ַ���.
	 * @param args
	 * @return
	 */
	public static String formatBodyRowRecord2(String... args) {
		StringBuilder sb = new StringBuilder();
		for (String string : args) {
			if (string != null) {
				sb.append("<td class='subtable_body_td' nowrap='nowrap'>").append(string).append("</td>");
			} else {
				sb.append("<td class='subtable_body_td'>&nbsp;</td>");
			}
		}
		return sb.toString();
	}
	/**
	 * �����м�¼װ�����ַ���.
	 * @param args
	 * @return
	 */
	public static String formatBodyRowRecordNoWarp(String... args) {
		StringBuilder sb = new StringBuilder();
		for (String string : args) {
			if (string != null) {
				sb.append("<td class='subtable_body_td' nowrap='nowrap'>").append(string).append("</td>");
			} else {
				sb.append("<td class='subtable_body_td'>&nbsp;</td>");
			}
		}
		return sb.toString();
	}
	
	/**
	 * �����м�¼װ�����ַ���.
	 * @param args
	 * @return
	 */
	public static String formatBodyRowRecord(Integer... args) {
		StringBuilder sb = new StringBuilder();
		for (Integer string : args) {
			if (string != null) {
				sb.append("<td class='subtable_body_td' style='text-align:center;'>").append(string.toString()).append("</td>");
			} else {
				sb.append("<td class='subtable_body_td'>&nbsp;</td>");
			}
		}
		return sb.toString();
	}

	/**
	 * ת��null.
	 * @param str
	 * @return
	 */
	public static String parseNull(String str) {
		return str == null ? "" : str;
	}
	
}
