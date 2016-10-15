package cn.com.akl.shgl.db.rtclass;

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
     * Excelģ�壺 ���ϱ��.
     */
    private static final int EXCEL_COL_WLBH = 0;
    /**
     * Excelģ�壺 �ͺ�.
     */
    private static final int EXCEL_COL_PN = 1;
    /**
     * Excelģ�壺 ��������.
     */
    private static final int EXCEL_COL_WLMC = 2;
    /**
     * Excelģ�壺 ���.
     */
    private static final int EXCEL_COL_GG = 3;
    /**
     * Excelģ�壺 �ο��������.
     */
    private static final int EXCEL_COL_CKKCSL = 5;
    /**
     * Excelģ�壺 ����.
     */
    private static final int EXCEL_COL_SX = 10;
    /**
     * Excelģ�壺 �۸�.
     */
    private static final int EXCEL_COL_JG = 11;
    /**
     * Excelģ�壺 �ֿ����.
     */
    private static final int EXCEL_COL_CKDM = 14;
    /**
     * Excelģ�壺 �ֿ�����.
     */
    private static final int EXCEL_COL_CKMC = 16;

    private RepositoryBiz repositoryBiz = new RepositoryBiz();

    public StepNo1SheetModelUp(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("�������Զ����������Ϣ.");
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
            String fhkfckbm = DAOUtil.getStringOrNull(conn, "SELECT FHKFCKBM FROM BO_AKL_DB_P WHERE BINDID=?", bindid);
            String fhkfckmc = DAOUtil.getStringOrNull(conn, "SELECT FHKFCKMC FROM BO_AKL_DB_P WHERE BINDID=?", bindid);
            String xmlb = DAOUtil.getStringOrNull(conn, "SELECT XMLX FROM BO_AKL_DB_P WHERE BINDID=?", bindid);

            if (fhkfckbm == null || fhkfckbm.equals("")) {
                throw new RuntimeException("���ݴ���ٵ���!");
            }

            for (int i = ExcelUtil.EXCEL_START; i <= maxRowNum; i++) {
                HSSFRow row = sheet.getRow(i);
                // ����м�¼
                fillRow(conn, row, xmlb, fhkfckbm, fhkfckmc);
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
    private void fillRow(Connection conn, HSSFRow row, String xmlb, String fhkfckbm, String fhkfckmc) throws SQLException {
        String xh = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_PN));
        // �����ֿ�����.
        ExcelUtil.setCellValue(row, EXCEL_COL_CKDM, fhkfckbm);
        ExcelUtil.setCellValue(row, EXCEL_COL_CKMC, fhkfckmc);

        // ת��������Ϣ.
        String sx = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SX));
        String sxNum = parseSX(conn, row, sx);
        if (sxNum == null) {
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��" + (row.getRowNum() + 1) + "�е�����[" + sx + "]��ϵͳ���Ҳ���!");
            ExcelUtil.setCellValue(row, EXCEL_COL_SX, "");
        } else {
            ExcelUtil.setCellValue(row, EXCEL_COL_SX, sxNum);
        }

        // ��ȡ������Ϣ.
        putWLXX(conn, row, xmlb, xh, sx, fhkfckbm);

    }

    /**
     * ת��������Ϣ.
     *
     * @param conn
     * @param row
     */
    private String parseSX(Connection conn, HSSFRow row, String sx) throws SQLException {
        String sxNum = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM BO_AKL_DATA_DICT_S WHERe DLBM='066' AND XLMC=?", sx);
        return sxNum;
    }

    /**
     * ���������Ϣ.
     *
     * @param conn
     * @param row
     * @param wlbh
     * @throws SQLException
     */
    private void putWLXX(Connection conn, HSSFRow row, String xmlb, String xh, String sx, String fhkfckbm) throws SQLException {
        /** ����. */
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement("SELECT * FROM BO_AKL_CPXX WHERE LPN8=?");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xh);
            String wlbh = null;
            if (reset.next()) {
                wlbh = reset.getString("WLBH");
                ExcelUtil.setCellValue(row, EXCEL_COL_WLBH, PrintUtil.parseNull(reset.getString("WLBH")));
                ExcelUtil.setCellValue(row, EXCEL_COL_WLMC, PrintUtil.parseNull(reset.getString("WLMC")));
                ExcelUtil.setCellValue(row, EXCEL_COL_GG, PrintUtil.parseNull(reset.getString("GG")));
            } else {
                MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��" + (row.getRowNum() + 1) + "�е�PN[" + xh + "] ��ϵͳ���Ҳ���!");
                return;
            }

            // ��ȡ����������.
            // ��ȡ���ϼ۸�.
            if (wlbh == null || wlbh.equals("")) {
                MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��" + (row.getRowNum() + 1) + "�е�PN[" + xh + "] ��ϵͳ���Ҳ���!");
            } else {
                int kcsl = repositoryBiz.queryMaterialCanUseInCK(conn, xmlb, wlbh, fhkfckbm, sx);
                ExcelUtil.setCellValue(row, EXCEL_COL_CKKCSL, String.valueOf(kcsl));

                String jg = DAOUtil.getStringOrNull(conn, "SELECT JG FROM BO_AKL_SH_JGGL WHERE WLBH=? ORDER BY ID DESC", wlbh);
                if (jg == null || jg.equals("")) {
                    MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��" + (row.getRowNum() + 1) + "�е�PN[" + xh + "] ��ϵͳ��δά���۸�!");
                    ExcelUtil.setCellValue(row, EXCEL_COL_JG, "0");
                } else {
                    ExcelUtil.setCellValue(row, EXCEL_COL_JG, jg);
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }

    }

}
