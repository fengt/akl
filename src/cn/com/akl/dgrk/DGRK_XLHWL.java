package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGRK_XLHWL extends ExcelDownFilterRTClassA {

	public DGRK_XLHWL(UserContext arg0) {
		super(arg0);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("ƥ�����Ϻţ���ʾ������кų�����Ϣ");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tableName = this.getParameter(PARAMETER_TABLE_NAME).toString();
		//��ȡ��ⵥͷ��Ϣ
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);
		String rklx = rkdtData.get("RKLX") == null ?"":rkdtData.get("RKLX").toString();//�������
		if(tableName.equals("BO_AKL_CCB_RKD_XLH_S")){
			//���кű�
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			String str = "SELECT XH,XLH FROM BO_AKL_CCB_RKD_XLH_S WHERE BINDID="+bindid;
			try {
				conn = DBSql.open();
				ps = conn.prepareStatement(str);
				rs = ps.executeQuery();
				while(rs.next()){
					//��ȡ���кŵ�������
					String xh = parseNull(rs.getString("XH"));//�ͺ�
					String xlh = parseNull(rs.getString("XLH"));//���к�
					String wlsql = "select WLBH from BO_AKL_WLXX where XH='"+xh+"'";
					String wlh = DBSql.getString(wlsql, "WLBH");
					//�������Ϻ�
					String updatewl = "update BO_AKL_CCB_RKD_XLH_S set WLBH='"+wlh+"',ZT='�ڿ�' where bindid = '"+bindid+"' and XH='"+xh+"'";
					DBSql.executeUpdate(updatewl);
					//������˻���⣬��ʾ������кų�����Ϣ
					if(rklx.equals("�˻����")){
						String ckxxsql = "select b.ckdh,b.fhrq,b.lxrx1,b.shdz1,c.qsrq from (select max(ID) as id,bindid from BO_AKL_CCB_CKD_XLH_S where XLH='"+xlh+"' and ISEND=1 GROUP BY bindid) a LEFT JOIN BO_BO_AKL_DGCK_P b on a.bindid=b.bindid LEFT JOIN BO_AKL_QSD_P c on b.bindid=c.bindid ";
						String ckdh = parseNull(DBSql.getString(ckxxsql, "CKDH"));//���ⵥ��
						String fhrq = parseNull(DBSql.getString(ckxxsql, "FHRQ"));//��������
						String shr = parseNull(DBSql.getString(ckxxsql, "LXRX1"));//�ջ���
						String shdz = parseNull(DBSql.getString(ckxxsql, "SHDZ1"));//�ջ���ַ
						String qsrq = parseNull(DBSql.getString(ckxxsql, "QSRQ"));//ǩ������
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�ͺ�"+xh+",���к�"+xlh+"�ĳ��ⵥ��"+ckdh+",��������"+fhrq+",�ջ���"+shr+",�ջ���ַ"+shdz+",ǩ������"+qsrq+"");
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				DBSql.close(conn, ps, rs);
			}
		}
		return null;
	}
	public String parseNull(String str){
		return str == null?"":str;
	}
}
