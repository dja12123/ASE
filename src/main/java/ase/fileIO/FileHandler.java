package ase.fileIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ase.console.LogWriter;

public class FileHandler
{
	public static final Logger fileLogger = LogWriter.createLogger(FileHandler.class, "file");
	public static final String jarDir = new File(
			FileHandler.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath()
			+ "/";
	public static final String EXTRES_DIR = "extResource/";
	
	public static boolean isExistResFile(String dir)
	{
		InputStream is = getResInputStream(dir);
		if(is == null)
		{// 파일이나 디렉토리가 있는지 검사
			return false;
		}
		if(is.getClass().getSimpleName().equals("ByteArrayInputStream"))
		{// 디렉토리인지 검사
			return false;
		}
		
		return true;
	}
	
	public static List<String> getExtFileList(String dir)
	{
		List<String> fileList = new ArrayList<>();
		File[] files = getFileList(getExtResourceFile(dir));
		for(File f : files)
		{
			fileList.add(f.toString());
		}
		return fileList;
	}

	private static List<String> getResFileList(String dir)
	{
		List<String> filenames = new ArrayList<>();

		try
		{
			try (
					InputStream in = getResInputStream(dir);
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
				)
			{
				String resource;

				while ((resource = br.readLine()) != null)
				{
					filenames.add(resource);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return filenames;
	}
	
	public static File[] getFileList(File file)
	{
		File[] fileList = file.listFiles();

		if (fileList == null)
		{
			fileLogger.log(Level.SEVERE, "디렉토리를 찾을 수 없음");
		}

		return fileList;
	}
	
	public static File getExtResourceFile(String filePath)
	{
		StringBuffer dir = new StringBuffer(jarDir);

		dir.append(EXTRES_DIR);
		dir.append(filePath);

		return new File(dir.toString());
	}

	public static InputStream getResInputStream(String dir)
	{
		return FileHandler.class.getResourceAsStream(dir);
	}

	public static InputStream getInputStream(File file)
	{
		FileInputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(file);
		}
		catch (FileNotFoundException e)
		{
			fileLogger.log(Level.SEVERE, "인풋 스트림을 가져올 수 없음", e);
		}
		return inputStream;
	}

	public static InputStream getExtInputStream(String dir)
	{
		return getInputStream(getExtResourceFile(dir));
	}

	public static FileOutputStream getOutputStream(File file)
	{
		FileOutputStream outputStream = null;
		try
		{
			outputStream = new FileOutputStream(file);
		}
		catch (FileNotFoundException e)
		{
			fileLogger.log(Level.SEVERE, "아웃풋 스트림을 가져올 수 없음", e);
		}
		return outputStream;
	}

	public static FileOutputStream getExtOutputStream(String dir)
	{
		return getOutputStream(getExtResourceFile(dir));
	}

	public static String readFileString(InputStream is) throws FileNotFoundException
	{
		BufferedReader bufRead;
		
		InputStreamReader isr = new InputStreamReader(is);
		bufRead = new BufferedReader(isr);

		StringBuffer fileReadString = new StringBuffer();
		String tempReadString = "";

		try
		{
			while ((tempReadString = bufRead.readLine()) != null)
			{
				// System.out.println(tempReadString);
				fileReadString.append(tempReadString + "\n");
			}

			bufRead.close();
		}
		catch (IOException e)
		{
			fileLogger.log(Level.SEVERE, "파일 버퍼 오류: 파일을 찾을 수 없음", e);
			return null;
		}

		return fileReadString.toString();
	}

	public static String readExtFileString(String dir)
	{
		try
		{
			return readFileString(new FileInputStream(getExtResourceFile(dir)));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readResFileString(String dir)
	{
		try
		{
			return readFileString(getResInputStream(dir));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		List<String> l = getResFileList("/");
		for(String s : l)
		{
			System.out.println(s);
		}
	}

}