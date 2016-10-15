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
     * Excelģ�壺 �ӹ������ͺ�.
     */
    private static final int EXCEL_COL_PN = 0;
    /**
     * Excelģ�壺 ��������.
     */
    private static final int EXCEL_COL_WLMC = 1;
    /**
     * Excelģ�壺 �ӹ�����.
     */
    private static final int EXCEL_COL_JGLX = 2;
    /**
     * Excelģ�壺 ������
     */
    private static final int EXCEL_COL_GZBH = 3;
    
    private static final String jgfs0 = "�������ӹ�";
    private static final String jgfs1 = "�����ӹ�";

    
    public StepNo1SheetModelUp(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("�ӹ������Զ�ƥ��ӹ�����");
    }

    @Override
    public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
        HSSFSheet sheet = arg0.getSheetAt(0);
        int maxRowNum = sheet.getLastRowNum();
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

        Connection conn = null;
        try {
            conn = DBSql.open();

            // ��֤�Ƿ���д�˲ֿ�.
            String xmlb = DAOUtil.getStringOrNull(conn, "SELECT XMLB FROM BO_AKL_SH_JG_P WHERE BINDID=?", bindid);
            String ckdm = DAOUtil.getStringOrNull(conn, "SELECT CKDM FROM BO_AKL_SH_JG_P WHERE BINDID=?", bindid);
            String jgfs = DAOUtil.getStringOrNull(conn, "SELECT JGLX FROM BO_AKL_SH_JG_P WHERE BINDID=?", bindid);

            if (xmlb == null || xmlb.equals("") || ckdm == null || ckdm.equals("")) {
                throw new RuntimeException("���ݴ���ٵ���!");
            }else if(jgfs0.equals(jgfs)){
            	throw new RuntimeException("�������ӹ��޵��빦�ܣ�");
            }

            for (int i = ExcelUtil.EXCEL_START; i <= maxRowNum; i++) {
                HSSFRow row = sheet.getRow(i);
                // ����м�¼
                fillRow(conn, row, xmlb, jgfs);
            }
            return arg0;
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
            return ExcelUtil.getClearWorkBook(arg0);
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨���ִ�������ϵ����Ա!", true);
            return ExcelUtil.getClearWorkBook(arg0);
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * ����м�¼�����ݿͻ����ϱ�Ż��ͺţ�ѯ��������������.
     *
     * @param row
     * @throws SQLException
     */
    private void fillRow(Connection conn, HSSFRow row, String xmlb, String jgfs) throws SQLException {
        String xh = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_PN));

        // ת���ӹ�������Ϣ.
        String jglx = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_JGLX));
        String jglxNum = parseJGLX(conn, row, jglx);
        if (jglxNum == null) {
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��" + (row.getRowNum() + 1) + "�еļӹ�����[" + jglx + "]��ϵͳ���Ҳ���!");
            ExcelUtil.setCellValue(row, EXCEL_COL_JGLX, "");
        } else {
            ExcelUtil.setCellValue(row, EXCEL_COL_JGLX, jglxNum);
        }

        // ��ȡ������Ϣ.
        putWLXX(conn, row, jgfs, xmlb, jglxNum, xh, jglx);

    }

    /**
     * ת��������Ϣ.
     *
     * @param conn
     * @param row
     */
    private String parseJGLX(Connection conn, HSSFRow row, String jggzlx) throws SQLException {
        String jglxNum = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM BO_AKL_DATA_DICT_S WHERe DLBM='085' AND XLMC=?", jggzlx);
        return jglxNum;
    }

    /**
     * ���������Ϣ.
     *
     * @param conn
     * @param row
     * @param wlbh
     * @throws SQLException
     */
    private void putWLXX(Connection conn, HSSFRow row, String jgfs, String xmlb, String jglxNum, String xh, String jglx) throws SQLException {
        /** ����. */
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement("SELECT GZBH, WLMC FROM BO_AKL_SH_JGGZ_VIEW WHERE XMLX =? AND JGLX=? AND JGWLXH=? GROUP BY GZBH, WLMC");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, jglxNum, xh);
            if (reset.next()) {
            	ExcelUtil.setCellValue(row, EXCEL_COL_GZBH, PrintUtil.parseNull(reset.getString("GZBH")));
                ExcelUtil.setCellValue(row, EXCEL_COL_WLMC, PrintUtil.parseNull(reset.getString("WLMC")));
            } else {
//                MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��" + (row.getRowNum() + 1) + "�е�PN[" + xh+ "] ���ӹ�����[" + jglx+ "] ��ϵͳ���Ҳ���!");
//                return;
            	throw new RuntimeException("��" + (row.getRowNum() + 1) + "�е�PN[" + xh+ "] ���ӹ�����[" + jglx+ "] ��ϵͳ���Ҳ���������ά��!"); 
            }
        } finally {
            DBSql.close(ps, reset);
        }

    }

}
