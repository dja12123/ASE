package ase.hardware;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import ase.ServerCore;
import ase.console.LogWriter;
import ase.fileIO.FileHandler;
import de.pi3g.pi.oled.OLEDDisplay;

public class DisplayControl
{
	public static final String FONT_METADATA = "/displayFont/rawfont.fnt";
	public static final String FONT_BITMAP = "/displayFont/rawfont.png";
	public static final Logger logger = LogWriter.createLogger(DisplayControl.class, "DisplayControl");
	public static final int DISPLAY_WIDTH = 128;
	public static final int DISPLAY_HEIGHT = 64;
	public static final int FONT_SIZE = 12;
	public static final int FONT_MARGIN = 1;

	private static final boolean[][] NULLCHAR = new boolean[][] {
			{ true, true, true, true, true, true, true, true, true, true, true, true },
			{ true, true, false, false, false, false, false, false, false, false, true, true },
			{ true, false, true, false, false, false, false, false, false, true, false, true },
			{ true, false, false, true, false, false, false, false, true, false, false, true },
			{ true, false, false, false, true, false, false, true, false, false, false, true },
			{ true, false, false, false, false, true, true, false, false, false, false, true },
			{ true, false, false, false, false, true, true, false, false, false, false, true },
			{ true, false, false, false, true, false, false, true, false, false, false, true },
			{ true, false, false, true, false, false, false, false, true, false, false, true },
			{ true, false, true, false, false, false, false, false, false, true, false, true },
			{ true, true, false, false, false, false, false, false, false, false, true, true },
			{ true, true, true, true, true, true, true, true, true, true, true, true },

	};

	private static DisplayControl inst;

	public static void init()
	{
		inst = new DisplayControl();
	}

	public static DisplayControl inst()
	{
		return inst;
	}

	private HashMap<Character, boolean[][]> fontData;

	private OLEDDisplay display;
	private ArrayList<DisplayObject> lcdObjList;
	private Timer timer;

	private DisplayControl()
	{
		this.lcdObjList = new ArrayList<>();
		this.timer = new Timer(true);
		this.fontData = new HashMap<Character, boolean[][]>();

		try
		{
			BufferedImage fontBitmap = ImageIO.read(FileHandler.getResInputStream(FONT_BITMAP));
			BufferedReader fontMetadata = new BufferedReader(
					new InputStreamReader(FileHandler.getResInputStream(FONT_METADATA)));

			String line = fontMetadata.readLine();
			while (line != null)
			{
				int data_charid = Integer.parseInt(line.substring("char id=".length(), line.indexOf("x=")).trim());

				int data_x = Integer.parseInt(line.substring(line.indexOf("x=") + 2, line.indexOf("y=")).trim());
				int data_y = Integer.parseInt(line.substring(line.indexOf("y=") + 2, line.indexOf("width=")).trim());
				int data_width = Integer
						.parseInt(line.substring(line.indexOf("width=") + 6, line.indexOf("height=")).trim());
				int data_height = Integer
						.parseInt(line.substring(line.indexOf("height=") + 7, line.indexOf("xoffset=")).trim());
				int xadvance = Integer
						.parseInt(line.substring(line.indexOf("xadvance=") + 9, line.indexOf("page=")).trim());

				boolean[][] dataArr = new boolean[data_height][xadvance];

				int i = 0;
				for (int y = data_y; y < data_y + data_height; ++y)
				{
					int j = 0;
					for (int x = data_x; x < data_x + data_width; ++x)
					{
						if (fontBitmap.getRGB(x, y) >= -10000000)
						{
							dataArr[i][j] = true;
						}
						++j;
					}
					++i;
				}

				this.fontData.put((char) data_charid, dataArr);
				line = fontMetadata.readLine();
			}

			fontMetadata.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			this.display = new OLEDDisplay();
		}
		catch (IOException | UnsupportedBusNumberException e)
		{
			logger.log(Level.SEVERE, "디스플레이 로드 불가", e);
			return;
		}

		logger.log(Level.INFO, "디스플레이 제어모듈 초기화 완료");
	}

	public DisplayObject showString(int x, int y, String str)
	{// 해당 좌표에 문자열 출력(한글지원)
		// x나 y에 -1을 입력할 경우 해당 좌표가 중앙으로 정렬됨
		boolean[][] bitmap = this.stringToBitMap(str);
		return showShape(x, y, bitmap);
	}

	public DisplayObject replaceString(DisplayObject before, String str)
	{// 문자열 교체
		// 주의사항!! 새 오브젝트가 반환되므로 다음에 변환할때는 반환된 오브젝트를 사용해야 함
		boolean[][] bitmap = this.stringToBitMap(str);
		return replaceShape(before, bitmap);
	}

	public DisplayObject showRect(int x, int y, int width, int height)
	{// 사각형 출력(기준좌표, 사각형크기)
		boolean[][] bitmap = new boolean[width][height];
		for (int i = 0; i < height; ++i)
		{
			bitmap[0][i] = true;
			bitmap[width - 1][i] = true;
		}
		for (int i = 0; i < width; ++i)
		{
			bitmap[i][0] = true;
			bitmap[i][height - 1] = true;
		}
		return showShape(x, y, bitmap);
	}

	public DisplayObject showFillRect(int x, int y, int width, int height)
	{// 꽉찬 사각형 출력(기준좌표, 사각형크기)
		boolean[][] bitmap = new boolean[width][height];
		for (int i = 0; i < height; ++i)
		{
			for (int j = 0; j < width; ++j)
			{
				bitmap[i][j] = true;
			}
		}
		return showShape(x, y, bitmap);
	}

	public DisplayObject showLine(int x0, int y0, int x1, int y1)
	{// 선 출력(시작좌표, 끝좌표)
		int temp;
		if (x0 > x1)
		{
			temp = x1;
			x1 = x0;
			x0 = temp;
		}
		if (y0 > y1)
		{
			temp = y1;
			y1 = x0;
			y0 = temp;
		}
		int dx = x1 - x0;
		int dy = y1 - y0;
		int basex = x0;
		int basey = y0;
		boolean[][] bitmap = new boolean[dx + 1][dy + 1];
		if (Math.abs(dx) > Math.abs(dy))
		{
			float m = (float) dy / (float) dx;
			float n = y0 - m * x0;
			dx = (x1 > x0) ? 1 : -1;
			while (x0 != x1)
			{
				x0 += dx;
				y0 = (int) (m * x0 + n + (float) 0.5);

				bitmap[x0 - basex][y0 - basey] = true;
			}
		}
		else if (dy != 0)
		{
			float m = (float) dx / (float) dy;
			float n = x0 - m * y0;
			dy = (dy < 0) ? -1 : 1;
			while (y0 != y1)
			{
				y0 += dy;
				x0 = (int) (m * y0 + n + (float) 0.5);
				bitmap[x0 - basex][y0 - basey] = true;
			}
		}
		return showShape(basex, basey, bitmap);
	}

	public DisplayObject showShape(int x, int y, boolean[][] shape)
	{// 비트맵 도형 출력
		if (shape.length <= 0)
		{
			return null;
		}
		DisplayObject obj = new DisplayObject(x, y, shape.length, shape[0].length, shape);
		this.addLCDObj(obj);
		this.updateDisplay();
		return obj;
	}

	public DisplayObject replaceShape(DisplayObject before, boolean[][] shape)
	{// 주의사항!! 새 오브젝트가 반환되므로 다음에 변환할때는 반환된 오브젝트를 사용해야 함
		DisplayObject obj = new DisplayObject(before.xcenter ? -1 : before.x, before.ycenter ? -1 : before.y,
				shape.length, shape[0].length, shape);
		this.removeLCDObj(before);
		this.addLCDObj(obj);
		this.updateDisplay();
		return obj;
	}

	public void removeShape(DisplayObject obj)
	{
		this.removeLCDObj(obj);
		this.updateDisplay();
	}

	public DisplayObject removeShapeTimer(DisplayObject obj, int time)
	{// 지정한 시간 뒤에 도형 삭제
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				DisplayControl.this.removeLCDObj(obj);
				DisplayControl.this.updateDisplay();
			}
		};
		this.timer.schedule(task, time);
		return obj;
	}

	public DisplayObject blinkShape(DisplayObject obj, int time, int count)
	{// 깜빡임간격, 깜빡임횟수 입력(-1이면 무한 깜빡임)
		TimerTask task = new TimerTask()
		{
			boolean blink = true;
			int c = count;

			@Override
			public void run()
			{
				if (!DisplayControl.this.lcdObjList.contains(obj))
				{
					this.cancel();
					return;
				}
				if (this.blink)
				{
					this.blink = false;
					DisplayControl.this.undraw(obj);
				}
				else
				{
					this.blink = true;
					DisplayControl.this.draw(obj);
					if (this.c != -1)
					{
						--this.c;
						if (this.c == 0)
						{
							this.cancel();
						}
					}
				}
				DisplayControl.this.updateDisplay();
			}
		};
		this.timer.schedule(task, 0, time);
		return obj;
	}

	private synchronized void addLCDObj(DisplayObject obj)
	{
		this.lcdObjList.add(obj);
		this.draw(obj);
	}

	private synchronized void draw(DisplayObject obj)
	{// 실제 LCD에 그림
		for (int x = 0; x < obj.bitmap.length; ++x)
		{
			for (int y = 0; y < obj.bitmap[x].length; ++y)
			{
				/*
				 * if(obj.bitmap[x][y] != true) { continue; }
				 */
				this.display.setPixel(x + obj.x, y + obj.y, obj.bitmap[x][y]);
			}
		}
	}

	private synchronized void undraw(DisplayObject obj)
	{// 실제 LCD에 지움
		for (int x = 0; x < obj.width; ++x)
		{
			for (int y = 0; y < obj.height; ++y)
			{
				if (obj.bitmap[x][y] != true)
				{
					continue;
				}
				this.display.setPixel(x + obj.x, y + obj.y, false);
				// 켜진 픽셀 끄기
			}
		}
		for (int i = 0; i < this.lcdObjList.size(); ++i)
		{
			DisplayObject nextObj = this.lcdObjList.get(i);
			if (nextObj == obj)
				continue;

			if (obj.x > nextObj.x + nextObj.width)
				continue;
			if (obj.x + obj.width < nextObj.x)
				continue;
			if (obj.y > nextObj.y + nextObj.height)
				continue;
			if (obj.y + obj.height < nextObj.y)
				continue;
			// 사각형 겹치는지 확인

			int cx = Math.max(obj.x, nextObj.x);
			int cy = Math.max(obj.y, nextObj.y);
			int cwidth = Math.min(obj.x + obj.width, nextObj.x + nextObj.width) - cx;
			int cheight = Math.min(obj.y + obj.height, nextObj.y + nextObj.height) - cy;

			for (int x = 0; x < cwidth; ++x)
			{
				for (int y = 0; y < cheight; ++y)
				{
					/*
					 * if(nextObj.bitmap[cx + x - nextObj.x][cy + y - nextObj.y] != true) {
					 * continue; }
					 */
					this.display.setPixel(cx + x, cy + y, true);
					// 이전 픽셀 보이게
				}
			}
		}
	}

	private synchronized void removeLCDObj(DisplayObject obj)
	{
		if (obj == null)
			return;
		if (!this.lcdObjList.contains(obj))
			return;
		this.lcdObjList.remove(obj);
		this.undraw(obj);
	}

	private void updateDisplay()
	{
		ServerCore.mainThreadPool.execute(() ->
		{
			try
			{
				this.display.update();
			}
			catch (IOException e)
			{
				logger.log(Level.SEVERE, "LCD컨트롤 오류", e);
			}
		});
	}

	private boolean[][] stringToBitMap(String s)
	{
		boolean[][][] list = new boolean[s.length()][][];
		int width = 0;
		for (int i = 0; i < s.length(); ++i)
		{
			boolean[][] bitmap = this.fontData.get(s.charAt(i));
			if (bitmap == null)
				bitmap = NULLCHAR;
			width += bitmap[0].length + FONT_MARGIN;
			list[i] = bitmap;
		}

		boolean[][] result = new boolean[width][FONT_SIZE];
		
		int position = 0;
		for (int i = 0; i < s.length(); ++i)
		{
			for (int h = 0; list[i].length > h; ++h)
			{
				int align = FONT_SIZE - list[i][h].length;
				for(int w = 0; w < list[i][h].length; ++w)
				{
					result[w + position][h + align] = list[i][h][w];
				}
			}
			position += list[i][0].length + FONT_MARGIN;
		}
		
		return result;
	}
}
