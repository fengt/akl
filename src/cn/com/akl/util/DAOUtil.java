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
     * 打开连接
     *
     * @return
     */
    public static Connection openConnection() {
        return DBSql.open();
    }

    /**
     * 打开事务连接
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
     * 连接事务回滚，抛出异常
     *
     * @param conn
     * @throws SQLException
     */
    public static void connectRollBackThrowExp(Connection conn) throws SQLException {
        if (conn != null)
            conn.rollback();
    }

    /**
     * 连接事务回滚， 默认处理
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
     * 获取多行记录，多行记录被封装成List，最好不好过多，50条以内最好
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
     * 获取单行记录
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
     * 获取时间，当SQL语句只查询到一个时间字段的时候使用。
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
            throw new RuntimeException("锟睫硷拷录锟斤拷锟睫凤拷取锟斤拷值!");
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * 获取发货日期，没有则返回NULL.
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
     * 获取BigDecimal，当SQL语句只查询到一个小数字段的时候使用。
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
            throw new RuntimeException("锟睫硷拷录锟斤拷锟睫凤拷取锟斤拷值!");
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * 获取BigDecimal，当SQL语句只查询到一个小数字段的时候使用。
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
     * 获取整型，当SQL语句只查询到一个整数字段的时候使用。
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
            throw new RuntimeException("锟睫硷拷录锟斤拷锟睫凤拷取锟斤拷值!");
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * 获取整型，当SQL语句只查询到一个整数字段的时候使用。
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
     * 返回一个int数组，可以用于存储ID
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
     * 获取字符串，当SQL语句只查询到一个字符串字段的时候使用。
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
            throw new RuntimeException("锟睫硷拷录锟斤拷锟睫凤拷取锟斤拷值!");
        } finally {
            DBSql.close(state, reset);
        }
    }

    /**
     * 获取字符串，当SQL语句只查询到一个字符串字段的时候使用。如果没有记录则返回NULL不会抛出异常
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
     * 获取String集合
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
     * 获取结果集中，字段1、字段2(按SQL查询的字段顺序)形成的Map， 使用时，请注意Key唯一。
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
     * 批处理更新，用于一个参数的时候
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
     * 批处理更新
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
     * 批处理更新，用于一个参数的时候
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
     * 执行查询，并利用接口进行处理，允许在此方法中向接口传参。不适用于结果集较多的场景.
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
     * 执行查询，并利用接口进行处理，允许在此方法中向接口传参。不适用于结果集较多的场景.
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
     * 执行查询，并利用接口进行处理，允许在此方法中向接口传参。不适用于结果集较多的场景.
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
     * 执行查询，并利用抽象类进行处理，允许在此方法中向接口传参。不适用于结果集较多的场景.
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
     * 结果集处理接口
     *
     * @author huangming
     */
    public interface ResultPaser {
        /**
         * 结果集处理方法.
         *
         * @param conn
         * @param reset
         * @return
         * @throws SQLException
         */
        public boolean parse(Connection conn, ResultSet reset) throws SQLException;
    }

}
