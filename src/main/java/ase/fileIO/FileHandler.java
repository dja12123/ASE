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

	public static List<String> getExtFileList(String file)
	{
		List<String> fileList = new ArrayList<>();
		File[] files = getFileList(getExtResourceFile(file));
		for(File f : files)
		{
			fileList.add(f.toString());
		}
		return fileList;
	}

	private static List<String> getResFileList(String path)
	{
		List<String> filenames = new ArrayList<>();

		try
		{
			try (
					InputStream in = getResInputStream(path);
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

		dir.append("extResource/");
		dir.append(filePath);

		return new File(dir.toString());
	}

	public static InputStream getResInputStream(String path)
	{
		return FileHandler.class.getResourceAsStream(path);
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

	public static InputStream getExtInputStream(String file)
	{
		return getInputStream(getExtResourceFile(file));
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

	public static FileOutputStream getExtOutputStream(String file)
	{
		return getOutputStream(getExtResourceFile(file));
	}

	public static String readFileString(InputStream is) throws FileNotFoundException
	{
		BufferedReader bufRead;

		bufRead = new BufferedReader(new InputStreamReader(is));

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

	public static String readExtFileString(String file)
	{
		try
		{
			return readFileString(new FileInputStream(getExtResourceFile(file)));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readResFileString(String file)
	{
		try
		{
			return readFileString(getResInputStream(file));
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