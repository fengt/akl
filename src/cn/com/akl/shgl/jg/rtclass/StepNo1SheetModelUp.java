package cn.com.akl.shgl.jg.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.ExcelUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StepNo1SheetModelUp extends ExcelDownFilterRTClassA {

    /**
     * Excel模板： 加工物料型号.
     */
    private static final int EXCEL_COL_PN = 0;
    /**
     * Excel模板： 物料名称.
     */
    private static final int EXCEL_COL_WLMC = 1;
    /**
     * Excel模板： 加工类型.
     */
    private static final int EXCEL_COL_JGLX = 2;
    /**
     * Excel模板： 规则编号
     */
    private static final int EXCEL_COL_GZBH = 3;
    
    private static final String jgfs0 = "非正常加工";
    private static final String jgfs1 = "正常加工";

    
    public StepNo1SheetModelUp(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("加工导入自动匹配加工规则。");
    }

    @Override
    public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
        HSSFSheet sheet = arg0.getSheetAt(0);
        int maxRowNum = sheet.getLastRowNum();
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

        Connection conn = null;
        try {
            conn = DBSql.open();

            // 验证是否填写了仓库.
            String xmlb = DAOUtil.getStringOrNull(conn, "SELECT XMLB FROM BO_AKL_SH_JG_P WHERE BINDID=?", bindid);
            String ckdm = DAOUtil.getStringOrNull(conn, "SELECT CKDM FROM BO_AKL_SH_JG_P WHERE BINDID=?", bindid);
            String jgfs = DAOUtil.getStringOrNull(conn, "SELECT JGLX FROM BO_AKL_SH_JG_P WHERE BINDID=?", bindid);

            if (xmlb == null || xmlb.equals("") || ckdm == null || ckdm.equals("")) {
                throw new RuntimeException("请暂存后再导入!");
            }else if(jgfs0.equals(jgfs)){
            	throw new RuntimeException("非正常加工无导入功能！");
            }

            for (int i = ExcelUtil.EXCEL_START; i <= maxRowNum; i++) {
                HSSFRow row = sheet.getRow(i);
                // 填充行记录
                fillRow(conn, row, xmlb, jgfs);
            }
            return arg0;
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
            return ExcelUtil.getClearWorkBook(arg0);
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现错误，请联系管理员!", true);
            return ExcelUtil.getClearWorkBook(arg0);
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 填充行记录，根据客户物料编号或型号，询单数量，答复数量.
     *
     * @param row
     * @throws SQLException
     */
    private void fillRow(Connection conn, HSSFRow row, String xmlb, String jgfs) throws SQLException {
        String xh = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_PN));

        // 转换加工类型信息.
        String jglx = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_JGLX));
        String jglxNum = parseJGLX(conn, row, jglx);
        if (jglxNum == null) {
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "第" + (row.getRowNum() + 1) + "行的加工类型[" + jglx + "]在系统中找不到!");
            ExcelUtil.setCellValue(row, EXCEL_COL_JGLX, "");
        } else {
            ExcelUtil.setCellValue(row, EXCEL_COL_JGLX, jglxNum);
        }

        // 获取物料信息.
        putWLXX(conn, row, jgfs, xmlb, jglxNum, xh, jglx);

    }

    /**
     * 转换属性信息.
     *
     * @param conn
     * @param row
     */
    private String parseJGLX(Connection conn, HSSFRow row, String jggzlx) throws SQLException {
        String jglxNum = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM BO_AKL_DATA_DICT_S WHERe DLBM='085' AND XLMC=?", jggzlx);
        return jglxNum;
    }

    /**
     * 填充物料信息.
     *
     * @param conn
     * @param row
     * @param wlbh
     * @throws SQLException
     */
    private void putWLXX(Connection conn, HSSFRow row, String jgfs, String xmlb, String jglxNum, String xh, String jglx) throws SQLException {
        /** 单身. */
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement("SELECT GZBH, WLMC FROM BO_AKL_SH_JGGZ_VIEW WHERE XMLX =? AND JGLX=? AND JGWLXH=? GROUP BY GZBH, WLMC");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, jglxNum, xh);
            if (reset.next()) {
            	ExcelUtil.setCellValue(row, EXCEL_COL_GZBH, PrintUtil.parseNull(reset.getString("GZBH")));
                ExcelUtil.setCellValue(row, EXCEL_COL_WLMC, PrintUtil.parseNull(reset.getString("WLMC")));
            } else {
//                MessageQueue.getInstance().putMessage(getUserContext().getUID(), "第" + (row.getRowNum() + 1) + "行的PN[" + xh+ "] ，加工类型[" + jglx+ "] 在系统中找不到!");
//                return;
            	throw new RuntimeException("第" + (row.getRowNum() + 1) + "行的PN[" + xh+ "] ，加工类型[" + jglx+ "] 在系统中找不到，请先维护!"); 
            }
        } finally {
            DBSql.close(ps, reset);
        }

    }

}
