package cn.com.akl.shgl.sx.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.ExcelUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

public class StepNo6SheetModelUp extends ExcelDownFilterRTClassA {

    /** 物料编号 */
    private static final int EXCEL_COL_WLBH=0;
    /** P/N号 */
    private static final int EXCEL_COL_XH=1;
    /** 物料名称 */
    private static final int EXCEL_COL_WLMC=2;
    /** 首次送修SN */
    private static final int EXCEL_COL_SCSXSN=3;
    /** SN */
    private static final int EXCEL_COL_SN=4;
    /** 日报料号 */
    private static final int EXCEL_COL_RBLH=5;
    /** 北京CCP/N */
    private static final int EXCEL_COL_CCPN=6;
    /** 使用人类型 */
    private static final int EXCEL_COL_SYRLX=7;
    /** 数量 */
    private static final int EXCEL_COL_SL=8;
    /** 质保类型 */
    private static final int EXCEL_COL_ZBLX=9;
    /** 凭证编号 */
    private static final int EXCEL_COL_PZBH=10;
    /** 是否首次质保 */
    private static final int EXCEL_COL_SFSCZB=11;
    /** 购买日期 */
    private static final int EXCEL_COL_GMRQ=12;
    /** 质保截止日期 */
    private static final int EXCEL_COL_ZBJZRQ=13;
    /** 价格 */
    private static final int EXCEL_COL_JG=14;
    /** 故障条码 */
    private static final int EXCEL_COL_GZTM=15;
    /** 故障原因 */
    private static final int EXCEL_COL_GZYY=16;
    /** 故障原因备注 */
    private static final int EXCEL_COL_GZYYBZ=17;
    /** 处理方式 */
    private static final int EXCEL_COL_CLFS=18;
    /** 处理意见 */
    private static final int EXCEL_COL_CLYJ=19;
    /** 是否升级 */
    private static final int EXCEL_COL_SFSJ=20;
    /** 升级类型 */
    private static final int EXCEL_COL_SJLX=21;
    /** 升级描述 */
    private static final int EXCEL_COL_SJMS=22;
    /** 批复描述 */
    private static final int EXCEL_COL_PFMS=23;
    /** 三级描述 */
    private static final int EXCEL_COL_SJMS2=24;
    /** 是否特批 */
    private static final int EXCEL_COL_SFTP=25;
    /** 特批号 */
    private static final int EXCEL_COL_TPH=26;
    /** 是否调查 */
    private static final int EXCEL_COL_SFDC=27;
    /** 附件 */
    private static final int EXCEL_COL_FJ=28;
    /** 是否已加工 */
    private static final int EXCEL_COL_SFYJG=29;

    public StepNo6SheetModelUp(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("调拨单自动填充物料信息.");
    }

    @Override
    public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
        HSSFSheet sheet = arg0.getSheetAt(0);
        int maxRowNum = sheet.getLastRowNum();
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

        Connection conn = null;
        try {
            conn = DBSql.open();

            Hashtable<String, String> sxpData = BOInstanceAPI.getInstance().getBOData("BO_AKL_SX_P", bindid);
            // 验证是否填写了项目类别,业务类型.
            String xmlb = sxpData.get("XMLB");
            Vector<Hashtable<String, String>> sxds = BOInstanceAPI.getInstance().getBODatas("BO_AKL_SX_S", bindid);

            if (xmlb == null || xmlb.equals("")) {
                throw new RuntimeException("请暂存后再上传!");
            }

            for (int i = ExcelUtil.EXCEL_START; i <= maxRowNum; i++) {
                HSSFRow row = sheet.getRow(i);
                // 填充行记录
                fillRow(conn, row, xmlb, sxds.get(i - ExcelUtil.EXCEL_START));
            }
            return ExcelUtil.getClearWorkBook(arg0);
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
    private void fillRow(Connection conn, HSSFRow row, String xmlb, Hashtable<String, String> reData) throws SQLException {
        // 获取物料信息.
        bmzh(conn, row, xmlb);

        // 校验每列信息是否改变.
        String WLBHStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_WLBH));
        String WLBHStr2 = reData.get("WLBH");
        if (!isEquals(WLBHStr, WLBHStr2)) {
            throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，物料编号 " + WLBHStr + " 与原数据" + WLBHStr2 + "不符，请检查!");
        }

        String XHStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_XH));
        String XHStr2 = reData.get("XH");
        if (!isEquals(XHStr, XHStr2)) {
            throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，P/N号 " + XHStr + " 与原数据" + XHStr2 + "不符，请检查!");
        }

        String SNStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SN));
        String SNStr2 = reData.get("SN");
        if (!isEquals(SNStr, SNStr2)) {
            throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，SN " + SNStr + " 与原数据" + SNStr2 + "不符，请检查!");
        }

        String WLMCStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_WLMC));
        String WLMCStr2 = reData.get("WLMC");
        if (!isEquals(WLMCStr, WLMCStr2)) {
            throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，物料名称 " + WLMCStr + " 与原数据" + WLMCStr2 + "不符，请检查!");
        }

        // 更新可填写字段.
        String id = reData.get("ID");
        DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SX_S SET RBLH=?,CCPN=?,SYRLX=?,ZBLX=?,PZBH=?,SFSCZB=?,GMRQ=?,ZBJZRQ=?,GZTM=?,GZYY=?,GZYYBZ=?,CLFS=?,CLYJ=?,SFTP=?,SFDC=? WHERE ID=?",
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_RBLH)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_CCPN)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SYRLX)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_ZBLX)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_PZBH)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFSCZB)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_GMRQ)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_ZBJZRQ)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_GZTM)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_GZYY)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_GZYYBZ)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_CLFS)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_CLYJ)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFTP)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFDC)),
                id);
    }

    public boolean isEquals(String str1, String str2) {
        if (str1 == null && str2 != null) {
            return false;
        }
        if (str1 == str2 || str1.equals(str2)) {
            return true;
        }
        return false;
    }

    /**
     * 编码转换.
     *
     * @param conn
     * @param row
     * @throws SQLException
     */
    private void bmzh(Connection conn, HSSFRow row, String xmlb) throws SQLException {
        String SYRLXStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SYRLX));
        if (SYRLXStr != null && !SYRLXStr.equals("")) {
            String SYRLX = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='062' AND (XLMC=? or XLBM=?)", SYRLXStr, SYRLXStr);
            if (SYRLX == null || SYRLX.equals("")) {
                throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，使用人类型 " + SYRLXStr + " 不存在，请检查!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SYRLX, SYRLX);
        }
        String ZBLXStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_ZBLX));
        if (ZBLXStr != null && !ZBLXStr.equals("")) {
            String ZBLX = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='063' AND (XLMC=? or XLBM=?)", ZBLXStr, ZBLXStr);
            if (ZBLX == null || ZBLX.equals("")) {
                throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，质保类型 " + ZBLXStr + " 不存在，请检查!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_ZBLX, ZBLX);
        }
        String SFSCZBStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFSCZB));
        if (SFSCZBStr != null && !SFSCZBStr.equals("")) {
            String SFSCZB = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFSCZBStr, SFSCZBStr);
            if (SFSCZB == null || SFSCZB.equals("")) {
                throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，是否首次质保 " + SFSCZBStr + " 不存在，请检查!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SFSCZB, SFSCZB);
        }
        String GZYYStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_GZYY));
        if (GZYYStr != null && !GZYYStr.equals("")) {
            String GZYY = DAOUtil.getStringOrNull(conn, "SELECT REPLACE(XLMC, '/', '-') XLMC FROM  BO_AKL_DATA_DICT_S WHERE DLBM='075' AND  CHARINDEX((select XLMC FROM BO_AKL_DATA_DICT_S WHERE XLBM=?), XLMC)<>0 AND (XLMC like '%'+? or XLMC like ?+'%')", xmlb, GZYYStr, GZYYStr);
            if (GZYY == null || GZYY.equals("")) {
                throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，故障原因 " + GZYYStr + " 不存在，请检查!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_GZYY, GZYY);
        }
        String CLFSStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_CLFS));
        if (CLFSStr != null && !CLFSStr.equals("")) {
            String CLFS = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='064' AND (XLMC=? or XLBM=?)", CLFSStr, CLFSStr);
            if (CLFS == null || CLFS.equals("")) {
                throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，处理方式 " + CLFSStr + " 不存在，请检查!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_CLFS, CLFS);
        }
        String SFSJStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFSJ));
        if (SFSJStr != null && !SFSJStr.equals("")) {
            String SFSJ = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFSJStr, SFSJStr);
            if (SFSJ == null || SFSJ.equals("")) {
                throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，是否升级 " + SFSJStr + " 不存在，请检查!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SFSJ, SFSJ);
        }
        String SFTPStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFTP));
        if (SFTPStr != null && !SFTPStr.equals("")) {
            String SFTP = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFTPStr, SFTPStr);
            if (SFTP == null || SFTP.equals("")) {
                throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，是否特批 " + SFTPStr + " 不存在，请检查!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SFTP, SFTP);
        }
        String SFDCStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFDC));
        if (SFDCStr != null && !SFDCStr.equals("")) {
            String SFDC = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFDCStr, SFDCStr);
            if (SFDC == null || SFDC.equals("")) {
                throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，是否调查 " + SFDCStr + " 不存在，请检查!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SFDC, SFDC);
        }
    }
}
