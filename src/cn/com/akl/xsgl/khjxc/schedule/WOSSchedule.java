package cn.com.akl.xsgl.khjxc.schedule;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.util.DBSql;

public class WOSSchedule implements IJob {

	// ��ѯ��������������ܴλ��� 
	private final static String queryJXCTable = "SELECT KHBH, WLBH, KSSJNF, KSSJYF, KSSJZS, min(KSSJ) as MINKSSJ, SUM(ISNULL(XSL, 0)) AS VALUE FROM BO_AKL_KHJXC WHERE WOS IS NULL AND KSSJ BETWEEN ? AND ? GROUP BY WLBH, KHBH, KSSJNF, KSSJYF, KSSJZS ORDER BY KSSJNF,KSSJYF,KSSJZS";
	
	// ��ѯ��Ҫ����wos�����ʱ�����Сʱ��
	private final static String queryMaxTime = "SELECT MAX(KSSJ) maxtime FROM BO_AKL_KHJXC WHERE WOS IS NULL";
	private final static String queryMinTime = "SELECT MIN(KSSJ) maxtime FROM BO_AKL_KHJXC WHERE WOS IS NULL";
	
	private final static String updateWOS = "update BO_AKL_KHJXC SET WOS=? WHERE KHBH=? AND WLBH=? AND KSSJNF=? AND KSSJYF=? AND KSSJZS=?";
	
	private final static BigDecimal four = new BigDecimal(4);
	
	/**
	 * ��ʱ������
	 * 	1���ҵ���ǰ������WOSΪnull�ļ�¼�����ݿ�ʼ���ڣ�������ǰ����WOS��
	 * 	�ͻ��������BO_AKL_KHJXC
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Connection conn = null;
		
		try{
			conn = DBSql.open();
			
			// 1��������Ҫ����WOS��¼�����ʱ�����Сʱ��
			Date maxTime = DAOUtil.getDate(conn, queryMaxTime);
			Date minTime = DAOUtil.getDate(conn, queryMinTime);
			
			// 2������Сʱ��-4�ܣ�Ȼ���ѯ�����¶���
			Calendar minCalendar = Calendar.getInstance();
			minCalendar.setTime(minTime);
			
			Calendar maxCalendar = Calendar.getInstance();
			maxCalendar.setTime(maxTime);
			maxCalendar.add(Calendar.WEEK_OF_MONTH, -1);
			
			// ȡֵ��Χ
			int maxRow = getRow(maxCalendar.get(Calendar.YEAR), maxCalendar.get(Calendar.MONTH), maxCalendar.get(Calendar.WEEK_OF_MONTH));
			int minRow = getRow(minCalendar.get(Calendar.YEAR), minCalendar.get(Calendar.MONTH), minCalendar.get(Calendar.WEEK_OF_MONTH));

			maxCalendar.add(Calendar.WEEK_OF_MONTH, 1);
			minCalendar.add(Calendar.DAY_OF_YEAR, -35);

			Map<String ,Map<String, Map<Integer, BigDecimal>>> map = new HashMap<String, Map<String, Map<Integer, BigDecimal>>>();
			
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("param", map);
			
			PreparedStatement pstat = conn.prepareStatement(queryJXCTable);
			ResultSet reset = DAOUtil.executeFillArgsAndQuery(conn, pstat,  new java.sql.Date(minCalendar.getTimeInMillis()), new java.sql.Date(maxTime.getTime()));
			
			// 2�������������Ϣ���� �ܴΡ����ϱ�š��ͻ� ����
			while(reset.next()){
				// 3������˳���+���+�·�+�ܴ�
				String khbh = reset.getString("KHBH");
				String wlbh = reset.getString("WLBH");
				
				Map<String, Map<Integer, BigDecimal>> wlbhMap = map.get(khbh);
				if(wlbhMap == null){
					wlbhMap = new HashMap<String, Map<Integer, BigDecimal>>();
				}
				
				Map<Integer, BigDecimal> rowMap = wlbhMap.get(wlbh);
				int kssjzs = reset.getInt("KSSJZS");
				int kssjnf = reset.getInt("KSSJNF");
				int kssjyf = reset.getInt("KSSJYF");
				BigDecimal value = reset.getBigDecimal("VALUE");
				
				Calendar instance = Calendar.getInstance();
				instance.set(Calendar.YEAR, kssjyf);
				instance.set(Calendar.MONTH, kssjyf);
				instance.set(Calendar.WEEK_OF_MONTH, kssjzs);
				instance.add(Calendar.WEEK_OF_MONTH, -1);
				
				int row = getRow(kssjnf, kssjyf, kssjzs);
				for(int i=0; i<4; i++){
					if(row<=maxRow && row>=minRow){
						BigDecimal bigDecimal = rowMap.get(row);
						if(bigDecimal == null){
							rowMap.put(row, value.divide(four , 4, BigDecimal.ROUND_HALF_UP));
						} else {
							bigDecimal = bigDecimal.add(value.divide(four , 4, BigDecimal.ROUND_HALF_UP));
						}
					}
					
					instance.add(Calendar.WEEK_OF_MONTH, -1);
					row = getRow(instance.get(Calendar.YEAR), instance.get(Calendar.MONTH), instance.get(Calendar.WEEK_OF_MONTH));
				}
			}
			
			// ����WOS
			updateWos(conn, map);
			
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * ����WOS
	 * @param conn
	 * @param map
	 * @throws SQLException
	 */
	private void updateWos(Connection conn,
			Map<String, Map<String, Map<Integer, BigDecimal>>> map)
			throws SQLException {
		int buffer = 0;
		Object[][] args = new Object[256][6];
		
		Set<Entry<String, Map<String, Map<Integer, BigDecimal>>>> entrySet = map.entrySet();
		for (Entry<String, Map<String, Map<Integer, BigDecimal>>> entry : entrySet) {
			String khbh = entry.getKey();
			
			Set<Entry<String, Map<Integer, BigDecimal>>> entrySet2 = entry.getValue().entrySet();
			for (Entry<String, Map<Integer, BigDecimal>> entry2 : entrySet2) {
				String wlbh = entry2.getKey();

				Set<Entry<Integer, BigDecimal>> entrySet3 = entry2.getValue().entrySet();
				for (Entry<Integer, BigDecimal> entry3 : entrySet3) {
					Integer row = entry3.getKey();
					BigDecimal value = entry3.getValue();
					int year = row/10000;
					int month = year*100 - row/100;
					int week = row%100;
					
					if(buffer > 100){
						args[buffer][0]=value;
						args[buffer][1]=khbh;
						args[buffer][2]=wlbh;
						args[buffer][3]=year;
						args[buffer][4]=month;
						args[buffer][5]=week;
						// ִ��
						DAOUtil.executeUpdate(conn, updateWOS, value, khbh, wlbh, year, month, week);
						args = new Object[256][6];
						buffer = 0;
					} else {
						// ����
						args[buffer][0]=value;
						args[buffer][1]=khbh;
						args[buffer][2]=wlbh;
						args[buffer][3]=year;
						args[buffer][4]=month;
						args[buffer][5]=week;
						buffer++;
					}
				}
			}
		}
	}
	
	
	private static int getRow(int nf, int yf, int weekOfMonth){
		return nf*10000+yf*100+weekOfMonth;
	}
}
