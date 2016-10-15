package cn.com.akl.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sun.net.TelnetOutputStream;
import sun.net.ftp.FtpClient;

public class FileUtil {

	private static final String PATH = System.getProperty("user.dir") + "\\..\\logdir\\";

	public static void main(String[] args) throws IOException {
		File file = saveBytesToFile("hello3.csv", new String("totalcode,Pn").getBytes());
		 file = saveBytesToFile("hello3.csv", new String("\n123122,po-20150204001").getBytes());
		System.out.println(file.getAbsolutePath());
		saveFileToFtpServer("10.10.10.221", 2131, "ftp", "akl,.ftp12b11", file);
	}

	/**
	 * 保存文件.
	 * 
	 * @param filename
	 * @param bytes
	 * @return
	 * @throws IOException
	 */
	public static File saveBytesToFile(String filename, byte[] bytes) throws IOException {
		File file = new File(PATH + "/" + filename);
		if (!file.exists()) {
			File parentFile = file.getParentFile();
			parentFile.mkdirs();
			file.getParentFile().mkdirs();
			if (!file.createNewFile()) {
				throw new IOException("文件创建失败!");
			}
		}
		else
			
		{
			file.delete();
			
		}

		FileOutputStream output = new FileOutputStream(file,true);
		try {
			output.write(bytes);
		} finally {
			output.close();
		}

		return file;
	}
	/**
	 * 保存文件.
	 * 
	 * @param filename
	 * @param bytes
	 * @return
	 * @throws IOException
	 */
	public static File addBytesToFile(String filename, byte[] bytes) throws IOException {
		File file = new File(PATH + "/" + filename);
		if (!file.exists()) {
			File parentFile = file.getParentFile();
			parentFile.mkdirs();
			file.getParentFile().mkdirs();
			if (!file.createNewFile()) {
				throw new IOException("文件创建失败!");
			}
		}
		

		FileOutputStream output = new FileOutputStream(file,true);
		try {
			output.write(bytes);
		} finally {
			output.close();
		}

		return file;
	}
	
	
	
	/**
	 * 将文件写出到指定的输出流中.
	 * 
	 * @param file
	 * @param output
	 * @throws IOException
	 */
	public static void writeFileToOutputStream(File file, OutputStream output) throws IOException {
		FileInputStream input = new FileInputStream(file);
		ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int length = 0;
		try {
			while ((length = input.read(buffer)) != -1) {
				bufferOut.write(buffer, 0, length);
			}
		} finally {
			input.close();
		}

		output.write(bufferOut.toByteArray());
	}

	/**
	 * 保存文件至Ftp服务器.
	 * 
	 * @param ftpUrl
	 * @param ftpPort
	 * @param username
	 * @param password
	 * @param file
	 * @throws IOException
	 */
	public static void saveFileToFtpServer(String ftpUrl, int ftpPort, String username, String password, File file) throws IOException {
		FtpClient client = new FtpClient(ftpUrl, ftpPort);
		try {
			client.login(username, password);
		} catch (IOException e) {
			throw new RuntimeException("登录失败!'");
		}

		client.binary();
		TelnetOutputStream output = client.put(file.getName());
		try {
			writeFileToOutputStream(file, output);
			output.flush();
		} finally {
			output.close();
		}
	}

}
