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
	 * @param orderType 传入u8的单据类型 例 采购单
	 * @param drive 盘符 例：E:\\send\\ | E:\\return\\
	 * @param xml 需要传入u8的xml 字符串
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
			//创建文件夹
			File file = new File(drive);//需要从数据库中取出
			if(!file.exists())
				file.mkdirs();
			//下级目录
			SimpleDateFormat sf  = new SimpleDateFormat("yyyyMM");
			Date date = new Date();
			file = new File(file.getPath()+"\\"+sf.format(date));
			if(!file.exists())
				file.mkdirs();
			//获得当前时间
			SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String s = sdf.format(date);
			String fileName = orderType+s+".xml";
			String filePath = file.getPath()+"\\"+fileName;
			rets = filePath;
			file = new File(filePath);
			if(!file.exists())
				file.createNewFile();
			else
				throw new Exception("文件已存在！");
			//创建输出流对象
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
