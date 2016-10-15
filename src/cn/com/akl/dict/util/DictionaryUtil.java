package cn.com.akl.dict.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionsoft.awf.util.DBSql;

public class DictionaryUtil {

	/**
	 * 查询基础字典.
	 */
	private static String QUERY_JCZD = "SELECT XLBM, XLMC FROM BO_AKL_DATA_DICT_S";
	/**
	 * 名称转代码映射缓存.
	 */
	private static Map<String, String> chineseToNoCacheMap = null;
	/**
	 * 代码转名称映射缓存.
	 */
	private static Map<String, String> noToChineseCacheMap = null;

	private static Map<String, String> getChineseToNoCacheMap() {
		if (chineseToNoCacheMap == null) {
			try {
				chineseToNoCacheMap = initChineseToNoCacheMap();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return chineseToNoCacheMap;
	}

	private static Map<String, String> getNoToChineseCacheMap() {
		if (noToChineseCacheMap == null) {
			try {
				noToChineseCacheMap = initNoToChineseCacheMap();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return noToChineseCacheMap;
	}

	/**
	 * 初始化数据字典.
	 * 
	 * @return
	 * @throws SQLException
	 */
	private static Map<String, String> initChineseToNoCacheMap() throws SQLException {
		Map<String, String> chineseToNoCacheMap = new HashMap<String, String>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(QUERY_JCZD);
			reset = ps.executeQuery();
			while (reset.next()) {
				String xlmc = reset.getString("XLMC");
				String xlbm = reset.getString("XLBM");
				chineseToNoCacheMap.put(xlmc, xlbm);
			}
			return chineseToNoCacheMap;
		} finally {
			DBSql.close(conn, ps, reset);
		}
	}

	private static Map<String, String> initNoToChineseCacheMap() throws SQLException {
		Map<String, String> noToChineseCacheMap = new HashMap<String, String>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(QUERY_JCZD);
			reset = ps.executeQuery();
			while (reset.next()) {
				String xlmc = reset.getString("XLMC");
				String xlbm = reset.getString("XLBM");
				noToChineseCacheMap.put(xlbm, xlmc);
			}
			return noToChineseCacheMap;
		} finally {
			DBSql.close(conn, ps, reset);
		}
	}

	/**
	 * 获取计量单位编码.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseJLDWToNo(String str) {
		if (chineseToNoCacheMap == null) {
			chineseToNoCacheMap = getChineseToNoCacheMap();
		}
		String str2 = chineseToNoCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取计量单位中文.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseJLDWToName(String str) {
		if (noToChineseCacheMap == null) {
			noToChineseCacheMap = getNoToChineseCacheMap();
			if (noToChineseCacheMap == null) {
				return str;
			}
		}
		String str2 = noToChineseCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取属性编码.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseSXToNo(String str) {
		if (chineseToNoCacheMap == null) {
			chineseToNoCacheMap = getChineseToNoCacheMap();
		}
		String str2 = chineseToNoCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取属性中文.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseSXToName(String str) {
		if (noToChineseCacheMap == null) {
			noToChineseCacheMap = getNoToChineseCacheMap();
			if (noToChineseCacheMap == null) {
				return str;
			}
		}
		String str2 = noToChineseCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取客户类别编码.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseKHLBToNo(String str) {
		if (chineseToNoCacheMap == null) {
			chineseToNoCacheMap = getChineseToNoCacheMap();
			if (chineseToNoCacheMap == null) {
				return str;
			}
		}
		String str2 = chineseToNoCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取客户类别中文.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseKHLBToName(String str) {
		if (noToChineseCacheMap == null) {
			noToChineseCacheMap = getNoToChineseCacheMap();
			if (noToChineseCacheMap == null) {
				return str;
			}
		}
		String str2 = noToChineseCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取品牌编码.
	 * 
	 * @param str
	 * @return
	 */
	public static String parsePPToNo(String str) {
		if (chineseToNoCacheMap == null) {
			chineseToNoCacheMap = getChineseToNoCacheMap();
			if (chineseToNoCacheMap == null) {
				return str;
			}
		}
		String str2 = chineseToNoCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取品牌中文.
	 * 
	 * @param str
	 * @return
	 */
	public static String parsePPToName(String str) {
		if (noToChineseCacheMap == null) {
			noToChineseCacheMap = getNoToChineseCacheMap();
			if (noToChineseCacheMap == null) {
				return str;
			}
		}
		String str2 = noToChineseCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取是否编码.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseYesOrNoToNo(String str) {
		if (chineseToNoCacheMap == null) {
			chineseToNoCacheMap = getChineseToNoCacheMap();
			if (chineseToNoCacheMap == null) {
				return str;
			}
		}
		String str2 = chineseToNoCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取是否中文.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseYesOrNoToName(String str) {
		if (noToChineseCacheMap == null) {
			noToChineseCacheMap = getNoToChineseCacheMap();
			if (noToChineseCacheMap == null) {
				return str;
			}
		}
		String str2 = noToChineseCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取是否编码.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseSHFSToNo(String str) {
		if (chineseToNoCacheMap == null) {
			chineseToNoCacheMap = getChineseToNoCacheMap();
			if (chineseToNoCacheMap == null) {
				return str;
			}
		}
		String str2 = chineseToNoCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取是否中文.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseSHFSToName(String str) {
		if (noToChineseCacheMap == null) {
			noToChineseCacheMap = getNoToChineseCacheMap();
			if (noToChineseCacheMap == null) {
				return str;
			}
		}
		String str2 = noToChineseCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取是否编码.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseYFJSFSToNo(String str) {
		if (chineseToNoCacheMap == null) {
			chineseToNoCacheMap = getChineseToNoCacheMap();
			if (chineseToNoCacheMap == null) {
				return str;
			}
		}
		String str2 = chineseToNoCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 获取是否中文.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseYFJSFSToName(String str) {
		if (noToChineseCacheMap == null) {
			noToChineseCacheMap = getNoToChineseCacheMap();
			if (noToChineseCacheMap == null) {
				return str;
			}
		}
		String str2 = noToChineseCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 转换中文到编号.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseChineseToNo(String str) {
		if (chineseToNoCacheMap == null) {
			chineseToNoCacheMap = getChineseToNoCacheMap();
			if (chineseToNoCacheMap == null) {
				return str;
			}
		}
		String str2 = chineseToNoCacheMap.get(str);
		return str2 == null ? str : str2;
	}

	/**
	 * 转换编号到中文.
	 * 
	 * @param str
	 * @return
	 */
	public static String parseNoToChinese(String str) {
		if (noToChineseCacheMap == null) {
			noToChineseCacheMap = getNoToChineseCacheMap();
			if (noToChineseCacheMap == null) {
				return str;
			}
		}
		
		String str2 = noToChineseCacheMap.get(str);
		return str2 == null ? str : str2;
	}

}
