package cn.com.akl.cggl.cgdd.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.cggl.cgdd.constant.CgddConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * 
 * @author ³����
 *
 */

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	// �ղι�����
	public StepNo1BeforeSave() {
	}

	// ���ι�����
	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		setProvider("³����");
		setDescription("�ɹ���������������������");
	}

	@Override
	public boolean execute() {
		// �������ʵ��id
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		// ��ñ���
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		// ��ȡ���ֶ�ֵ
		Hashtable<String, String> frmHead = this.getParameter(PARAMETER_FORM_DATA).toHashtable();

		// �ο�¼��ʱ����������Ϊ
		if (CgrkCnt.tableName6.equals(tablename)) {
			Connection conn = null;
			try {
				conn = DBSql.open();
				// ���ݿ�����
				Vector<Hashtable<String, String>> vector = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName7, bindid);
				String wlbh = "";// ���ϱ��
				int cgsl = 0;// �ɹ�����
				int kcsl = 0;// �������
				int rjxl = 0;// �վ�����
				int yjxsts = 0; // Ԥ����������
				if (vector != null) {
					for (Hashtable<String, String> a : vector) {
						wlbh = a.get("WLBH").toString();
						cgsl = Integer.parseInt(a.get("CGSL").toString());
						kcsl = Integer.parseInt(a.get("KCSL").toString());
						System.out.println("������ǿյİ�"+kcsl);
					}
				}
				// ���ϳ�ʼ����ʱ��
				Date scxssj = DAOUtil.getDateOrNull(conn,"SELECT TOP 1 CREATEDATE FROM BO_AKL_WXB_XSDD_BODY WHERE WLBH =? ORDER BY createdate",wlbh);
				if (scxssj != null) {
					Date xtsj = new Date();
					// ����������
					int s = (int) ((xtsj.getTime() - scxssj.getTime()) / (1000 * 60 * 60 * 24));
					// �ж������Ƿ���30��
					if (s < 30) {
						// ������
						int xl;
						xl = DAOUtil.getInt(conn,"SELECT SUM(ddsl) FROM BO_AKL_WXB_XSDD_BODY WHERE wlbh=?",wlbh);
						rjxl = xl / s;
					} else {
						// ������
						int yxl;
						yxl = DAOUtil.getInt(conn,"SELECT SUM(ddsl) FROM BO_AKL_WXB_XSDD_BODY WHERE CREATEDATE BETWEEN DATEADD(day,-30,getdate()) AND getdate() and wlbh=?",wlbh);
						rjxl = yxl / 30;
					}
					if (rjxl != 0) {
						// Ԥ����������
						yjxsts = (cgsl + kcsl) / rjxl;
						
					}
				}
				// �����վ����� �� Ԥ����������
				DAOUtil.executeUpdate(conn,"UPDATE BO_AKL_CGDD_BODY SET RJXL=? WHERE WLBH=?",rjxl, wlbh);
				DAOUtil.executeUpdate(conn,"UPDATE BO_AKL_CGDD_BODY SET YJXSTS=? WHERE WLBH=?",yjxsts, wlbh);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBSql.close(conn, null, null);
			}
		}
		//���뱾������ʱ���� �ɹ������������Ϊ
		if (CgrkCnt.tableName7.equals(tablename)) {
			// �ɹ�����
			int cgsl = Integer.parseInt(frmHead.get("CGSL"));
			//�ͺ�
			String xh = frmHead.get("XH");
			// ���ϱ�� 
			String str = "select * from " + CgddConstant.tableName3 + " where xh = '"+xh+"' and hzbm = '"+CgddConstant.hzbm0+"'";
			String wlbh = DBSql.getString(str, "wlbh");
			// �������
			String str4 = "SELECT SUM(PCSL) AS KCSL FROM " + CgddConstant.tableName6 + " WHERE WLBH='"+wlbh+"'";
			int kcsl = DBSql.getInt(str4, "KCSL");
			Connection conn = null;
			int rjxl =0;//�վ�����
			int yjxsts =0;//Ԥ����������
			try{
				conn = DBSql.open();
				// ���ϳ�ʼ����ʱ��
				Date scxssj = DAOUtil.getDateOrNull(conn,"SELECT TOP 1 CREATEDATE FROM BO_AKL_WXB_XSDD_BODY WHERE WLBH =? ORDER BY createdate",wlbh);
				if (scxssj != null) {
					Date xtsj = new Date();
					// ����������
					int s = (int) ((xtsj.getTime() - scxssj.getTime()) / (1000 * 60 * 60 * 24));
					// �ж������Ƿ���30��
					if (s < 30) {
						// ������
						int xl;
						xl = DAOUtil.getInt(conn,"SELECT SUM(ddsl) FROM BO_AKL_WXB_XSDD_BODY WHERE wlbh=?",wlbh);
						rjxl = xl / s;
					} else {
						// ������
						int yxl;
						yxl = DAOUtil.getInt(conn,"SELECT SUM(ddsl) FROM BO_AKL_WXB_XSDD_BODY WHERE CREATEDATE BETWEEN DATEADD(day,-30,getdate()) AND getdate() and wlbh=?",wlbh);
						rjxl = yxl / 30;
					}
					if (rjxl != 0) {
						// Ԥ����������
						yjxsts = (cgsl + kcsl) / rjxl;
						
					}
				}
			}catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBSql.close(conn, null, null);
			}
			frmHead.put("RJXL", String.valueOf(rjxl));
			frmHead.put("YJXSTS", String.valueOf(yjxsts));
		}

		return true;
	}

}
