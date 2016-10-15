package cn.com.akl.shgl.fjjh.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class SpotcheckBizBackup {
	
	/**
	 * ������ȡ���ϵĳ������ƽ����������и����ϵĸ����ͷ�������ȡ��Щ�ͷ���������Ϣ���븴��ƻ��ӱ���
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param allBindid
	 * @param wlbh
	 * @param sx
	 * @param cjsl
	 * @throws SQLException
	 */
	public void numberAvarageAndFillBackData(Connection conn, final int bindid, final String uid, 
			ArrayList<Integer> allBindid, String wlbh, String sx, int cjsl) throws SQLException{
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < allBindid.size(); i++) {
			int dbBindid = allBindid.get(i);
			Integer sjcksl = DAOUtil.getIntOrNull(conn, FJJHCnt.QUERY_DB_S_SJCKSL, dbBindid, wlbh, sx);
			if(sjcksl != null && sjcksl >=1){
				map.put(dbBindid, sjcksl);
			}
		}
		
		List<Map.Entry<Integer, Integer>> list = sortMap(map);
		int selectedNumber = list.size();
		int average = cjsl % selectedNumber == 0 ? 0 : 1;
		int cycleNumber = (int)Math.floor(cjsl / selectedNumber) + average;
		boolean flag = true;
		int count = 0;
		for (int i = 0; i < cycleNumber; i++) {
			for(Iterator<Map.Entry<Integer, Integer>> ite = list.iterator(); ite.hasNext() && flag; ){
				Map.Entry<Integer, Integer> sortedMap = ite.next();
				int dbBindid = sortedMap.getKey();
				int sjcksl = sortedMap.getValue();
				
				if(count >= cjsl){
					flag = false;
					continue;
				}
				
				Integer temp = DAOUtil.getIntOrNull(conn, FJJHCnt.QUERY_DB_S_FJSL, dbBindid, wlbh, sx);
				int fjsl = temp == null ? 0 : temp.intValue();
				if(fjsl < sjcksl){
					int updateCount = DAOUtil.executeUpdate(conn, FJJHCnt.UPDATE_DB_S_FJSL, dbBindid, wlbh, sx);
					if(updateCount != 1) throw new RuntimeException("���ʱ��������������ʧ�ܣ�");
					count ++;
				}else{//��ʱ��������������ѭ������
					cycleNumber ++;
				}
			}
		}
		
		Iterator<Map.Entry<Integer, Integer>> ite = map.entrySet().iterator();
		while(ite.hasNext()){
			Map.Entry<Integer, Integer> initMap = ite.next();
			DAOUtil.executeQueryForParser(conn, FJJHCnt.QUERY_DB_P_S, new ResultPaserAbs(){
				public boolean parse(Connection conn, ResultSet rs) throws SQLException{
					insertFJJHData(conn, bindid, rs, uid);
					return true;
				}
			}, initMap.getKey(), wlbh, sx);
		}
		
	}
	
	/**
	 * ��дComparator��������map�����ɴ�С��
	 * @param map
	 * @return
	 */
	public List<Map.Entry<Integer, Integer>> sortMap(Map<Integer, Integer> map){
		List<Map.Entry<Integer, Integer>> list = new LinkedList<Map.Entry<Integer, Integer>>();
		list.addAll(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>(){
			public int compare(Map.Entry<Integer, Integer> obj1, Map.Entry<Integer, Integer> obj2){
				if(obj1.getValue().intValue() < obj2.getValue().intValue())
					return 1;
				if(obj1.getValue().intValue() == obj2.getValue().intValue())
					return 0;
				else
					return -1;
			}
		});
		return list;
	}
	
	/**
	 * ����ƻ��ӱ����ݴ���
	 * @param conn
	 * @param bindid
	 * @param rs
	 * @param uid
	 * @throws SQLException
	 */
	public void insertFJJHData(Connection conn, int bindid, ResultSet rs, String uid) throws SQLException{
		Hashtable<String, String> body = new Hashtable<String, String>();
		String kfzx = StrUtil.returnStr(rs.getString("FHKFCKBM"));
		String kfbm = StrUtil.returnStr(rs.getString("FHKFCKMC"));
		String dbdh = StrUtil.returnStr(rs.getString("DBDH"));
		String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
		String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
		String xh = StrUtil.returnStr(rs.getString("XH"));
		String sx = StrUtil.returnStr(rs.getString("CPSX"));
		String fjsl = StrUtil.returnStr(rs.getString("SJCKSL"));
		int fjjhsl = rs.getInt("FJSL");
		body.put("KFZX", kfzx);
		body.put("KFBM", kfbm);
		body.put("DBDH", dbdh);
		body.put("WLBH", wlbh);
		body.put("WLMC", wlmc);
		body.put("XH", xh);
		body.put("SX", sx);
		body.put("FJSL", fjsl);
		body.put("FJJHSL", String.valueOf(fjjhsl));
		
		try {
			if(fjjhsl > 0)
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_FJJH_S", body, bindid, uid);
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * ��ո��������������޸ĳ���������ٴε������ʱ��
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void clearFJSL(Connection conn, int bindid) throws SQLException{
		DAOUtil.executeQueryForParser(conn, FJJHCnt.QUERY_FJJH_S, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String dbdh = rs.getString("DBDH");
				String wlbh = rs.getString("WLBH");
				String sx = rs.getString("SX");
				int updateCount = DAOUtil.executeUpdate(conn, FJJHCnt.UPDATE_FJSLToNull, wlbh, sx, dbdh);
				if(updateCount != 1) throw new RuntimeException("����������ʱ�����ʧ�ܣ�");
				return true;
			}
		}, bindid);
		
		//����ӱ�����
		DAOUtil.executeUpdate(conn, FJJHCnt.DELETE_FJJH_S, bindid);
	}
	
	/**
	 * У��ͷ������и��������������趨ֵ�Ŀͷ�����
	 * @param conn
	 * @param bindid
	 * @param fhck
	 * @param kffz
	 * @return
	 * @throws SQLException
	 */
	public List<String> checkTheNumber(Connection conn, int bindid, String fhck, String kffz) throws SQLException{
		List<String> list = new ArrayList<String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(FJJHCnt.QUERY_UNFINISHED);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, kffz, fhck, kffz);
			while(rs.next()){
				String kfzx = StrUtil.returnStr(rs.getString("KFCKMC"));
				int sdz = rs.getInt("SDZ");//�趨ֵ
				int fjzl = rs.getInt("FJZL");//��������
				String message = "��" + kfzx + "���ĸ�������" + fjzl + "С���趨ֵ" + sdz;
				list.add(message);
			}
		} finally {
			DBSql.close(ps, rs);
		}
		return list;
	}
}
