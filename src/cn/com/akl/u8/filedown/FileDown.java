/**
 * 
 */
package cn.com.akl.u8.filedown;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * @author hzy
 *
 */
public class FileDown {

	/**
	 * 
	 * @param orderType ����u8�ĵ������� �� �ɹ���
	 * @param drive �̷� ����E:\\send\\ | E:\\return\\
	 * @param xml ��Ҫ����u8��xml �ַ���
	 * @throws Exception
	 * @author hzy
	 * @desc
	 */
	public static String fileDown(String orderType,String drive,String xml)throws Exception{
		String rets = "";
		FileOutputStream os = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		try{
			//�����ļ���
			File file = new File(drive);//��Ҫ�����ݿ���ȡ��
			if(!file.exists())
				file.mkdirs();
			//�¼�Ŀ¼
			SimpleDateFormat sf  = new SimpleDateFormat("yyyyMM");
			Date date = new Date();
			file = new File(file.getPath()+"\\"+sf.format(date));
			if(!file.exists())
				file.mkdirs();
			//��õ�ǰʱ��
			SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String s = sdf.format(date);
			String fileName = orderType+s+".xml";
			String filePath = file.getPath()+"\\"+fileName;
			rets = filePath;
			file = new File(filePath);
			if(!file.exists())
				file.createNewFile();
			else
				throw new Exception("�ļ��Ѵ��ڣ�");
			//�������������
			os = new FileOutputStream(filePath);
		    osw = new OutputStreamWriter(os);
			bw = new BufferedWriter(osw);
			bw.write(xml);
		}catch(Exception ex){
			throw ex;
		}finally{
			if(bw!=null)
				bw.close();
			if(osw!=null)
				osw.close();
			if(os!=null)
				os.close();
		}
		return rets;
	}
}
