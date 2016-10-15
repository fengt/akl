package cn.com.akl.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.com.akl.dao.util.abs.ResultPaserAbs;

import com.actionsoft.awf.util.DBSql;

public class DAOUtil {

    /**
     * ������
     *
     * @return
     */
    public static Connection openConnection() {
        return DBSql.open();
    }

    /**
     * ����������
     *
     * @return
     * @throws SQLException
     */
    public static Connection openConnectionTransaction() throws SQLException {
        Connection conn = DBSql.open();
        conn.setAutoCommit(false);
        return conn;
    }

    /**
     * ��������ع����׳��쳣
     *
     * @param conn
     * @throws SQLException
     */
    public static void connectRollBackThrowExp(Connection conn) throws SQLException {
        if (conn != null)
            conn.rollback();
    }

    /**
     * ��������ع��� Ĭ�ϴ���
     *
     * @param conn
     */
    public static void connectRollBack(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ��ȡ���м�¼�����м�¼����װ��List����ò��ù��࣬50���������
     *
     * @param conn
     * @param sql
     * @param maxRows
     * @param args
     * @return
     * @throws SQLException
     */
    public static List<Map<String, Object>> getRowRecords(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            ResultSetMetaData metaData = reset.getMetaData();

            int rowCount = metaData.getColumnCount();
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(50);
            while (reset.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= args.length; i++) {
                    map.put(metaData.getColumnLabel(i), reset.getObject(rowCount));
                }
                list.add(map);
            }
            return list;
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ��ȡ���м�¼
     *
     * @param conn
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static Map<String, Object> getRowRecord(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            ResultSetMetaData metaData = reset.getMetaData();
            int rowCount = metaData.getColumnCount();
            if (reset.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= rowCount; i++) {
                    map.put(metaData.getColumnLabel(i), reset.getObject(i));
                }
                return map;
            }
            return null;
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ��ȡʱ�䣬��SQL���ֻ��ѯ��һ��ʱ���ֶε�ʱ��ʹ�á�
     *
     * @param conn
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static Date getDate(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            if (reset.next()) {
                return new Date(reset.getTimestamp(1).getTime());
            }
            throw new RuntimeException("�޼�¼���޷�ȡ��ֵ!");
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ��ȡ�������ڣ�û���򷵻�NULL.
     *
     * @param conn
     * @param string
     * @param bindid
     * @return
     * @throws SQLException
     */
    public static Date getDateOrNull(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            if (reset.next()) {
                Timestamp timestamp = reset.getTimestamp(1);
                if (timestamp == null) {
                    return null;
                } else {
                    return new Date(timestamp.getTime());
                }
            } else {
                return null;
            }
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ��ȡBigDecimal����SQL���ֻ��ѯ��һ��С���ֶε�ʱ��ʹ�á�
     *
     * @param conn
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static BigDecimal getBigDecimal(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            if (reset.next()) {
                return reset.getBigDecimal(1);
            }
            throw new RuntimeException("�޼�¼���޷�ȡ��ֵ!");
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ��ȡBigDecimal����SQL���ֻ��ѯ��һ��С���ֶε�ʱ��ʹ�á�
     *
     * @param conn
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static BigDecimal getBigDecimalOrNull(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            if (reset.next()) {
                return reset.getBigDecimal(1);
            }
            return null;
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ��ȡ���ͣ���SQL���ֻ��ѯ��һ�������ֶε�ʱ��ʹ�á�
     *
     * @param conn
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static int getInt(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            if (reset.next()) {
                return reset.getInt(1);
            }
            throw new RuntimeException("�޼�¼���޷�ȡ��ֵ!");
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ��ȡ���ͣ���SQL���ֻ��ѯ��һ�������ֶε�ʱ��ʹ�á�
     *
     * @param conn
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static Integer getIntOrNull(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            if (reset.next()) {
                return reset.getInt(1);
            } else {
                return null;
            }
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ����һ��int���飬�������ڴ洢ID
     *
     * @param conn
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static ArrayList<Integer> getInts(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        ArrayList<Integer> array = new ArrayList<Integer>(30);
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            while (reset.next()) {
                array.add(reset.getInt(1));
            }
            return array;
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ��ȡ�ַ�������SQL���ֻ��ѯ��һ���ַ����ֶε�ʱ��ʹ�á�
     *
     * @param conn
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static String getString(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            if (reset.next()) {
                return reset.getString(1);
            }
            throw new RuntimeException("�޼�¼���޷�ȡ��ֵ!");
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ��ȡ�ַ�������SQL���ֻ��ѯ��һ���ַ����ֶε�ʱ��ʹ�á����û�м�¼�򷵻�NULL�����׳��쳣
     *
     * @param conn
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static String getStringOrNull(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            if (reset.next()) {
                return reset.getString(1);
            } else {
                return null;
            }
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ��ȡString����
     *
     * @param conn
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static ArrayList<String> getStringCollection(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        ArrayList<String> array = new ArrayList<String>(30);
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            while (reset.next()) {
                array.add(reset.getString(1));
            }
            return array;
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ��ȡ������У��ֶ�1���ֶ�2(��SQL��ѯ���ֶ�˳��)�γɵ�Map�� ʹ��ʱ����ע��KeyΨһ��
     *
     * @param conn
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static Map getMap(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        LinkedHashMap<Object, Object> map = new LinkedHashMap<Object, Object>();
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            while (reset.next()) {
                map.put(reset.getObject(1), reset.getObject(2));
            }
            return map;
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ��������£�����һ��������ʱ��
     *
     * @param conn
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static int executeUpdate(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement state = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            return state.executeUpdate();
        } finally {
            DBSql.close(state, null);
        }
    }

    /**
     * ���������
     *
     * @param conn
     * @param sql
     * @param args
     * @throws SQLException
     */
    public static void executeBatchUpdate(Connection conn, String sql, Object[][] args) throws SQLException {
        PreparedStatement state = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                for (int j = 0; j < args[i].length; j++) {
                    state.setObject(j + 1, args[i][j]);
                }
                state.addBatch();
            }
            state.executeBatch();
        } finally {
            DBSql.close(state, null);
        }
    }

    /**
     * ��������£�����һ��������ʱ��
     *
     * @param conn
     * @param sql
     * @param args
     * @throws SQLException
     */
    public static void executeBatchUpdate(Connection conn, String sql, List<? extends Object> args) throws SQLException {
        PreparedStatement state = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 0; i < args.size(); i++) {
                state.setObject(1, args.get(i));
                if ((byte) i == (byte) 127) {
                    state.executeBatch();
                    state.clearBatch();
                } else {
                    state.addBatch();
                }
            }
            state.executeBatch();
        } finally {
            DBSql.close(state, null);
        }
    }

    /**
     * ִ�в�ѯ�������ýӿڽ��д��������ڴ˷�������ӿڴ��Ρ��������ڽ�����϶�ĳ���.
     *
     * @param conn
     * @param sql
     * @param paser
     * @param args
     * @throws SQLException
     */
    public static void executeQueryForParser(Connection conn, String sql, ResultPaser paser, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            while (reset.next() && paser.parse(conn, reset)) {
            }
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ִ�в�ѯ�������ýӿڽ��д��������ڴ˷�������ӿڴ��Ρ��������ڽ�����϶�ĳ���.
     *
     * @param conn
     * @param sql
     * @param paser
     * @param args
     * @throws SQLException
     */
    public static void executeQueryForParser(Connection conn, String sql, ResultPaserAbs paser, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            paser.init(conn);
            reset = state.executeQuery();
            while (reset.next() && paser.parse(conn, reset)) {
            }
            paser.destory(conn);
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ִ�в�ѯ�������ýӿڽ��д��������ڴ˷�������ӿڴ��Ρ��������ڽ�����϶�ĳ���.
     *
     * @param conn
     * @param sql
     * @param pasers
     * @param args
     * @throws SQLException
     */
    public static void executeQueryForParser(Connection conn, String sql, ResultPaser[] pasers, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }
            reset = state.executeQuery();
            while (reset.next()) {
                for (ResultPaser paser : pasers) {
                    if (!paser.parse(conn, reset)) {
                        return;
                    }
                }
            }
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * ִ�в�ѯ�������ó�������д��������ڴ˷�������ӿڴ��Ρ��������ڽ�����϶�ĳ���.
     *
     * @param conn
     * @param sql
     * @param pasers
     * @param args
     * @throws SQLException
     */
    public static void executeQueryForParser(Connection conn, String sql, ResultPaserAbs[] pasers, Object... args) throws SQLException {
        PreparedStatement state = null;
        ResultSet reset = null;
        try {
            state = conn.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                state.setObject(i, args[i - 1]);
            }

            for (ResultPaserAbs paser : pasers) {
                paser.init(conn);
            }

            reset = state.executeQuery();
            while (reset.next()) {
                for (ResultPaser paser : pasers) {
                    if (!paser.parse(conn, reset)) {
                        return;
                    }
                }
            }
            for (ResultPaserAbs paser : pasers) {
                paser.destory(conn);
            }

        } finally {
            DBSql.close(state, reset);
        }
    }

    public static ResultSet executeFillArgsAndQuery(Connection conn, PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 1; i <= args.length; i++) {
            ps.setObject(i, args[i - 1]);
        }
        return ps.executeQuery();
    }

    /**
     * ���������ӿ�
     *
     * @author huangming
     */
    public interface ResultPaser {
        /**
         * �����������.
         *
         * @param conn
         * @param reset
         * @return
         * @throws SQLException
         */
        public boolean parse(Connection conn, ResultSet reset) throws SQLException;
    }

}
