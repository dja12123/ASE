package ase.hardware;

public class DisplayObject
{
	public final int x, y, width, height;
	public final boolean xcenter, ycenter;
	public final boolean[][] bitmap;
	
	public DisplayObject(int x, int y, int width, int height, boolean[][] bitmap)
	{
		if(x == -1)
		{
			this.xcenter = true;
			x = (DisplayControl.DISPLAY_WIDTH / 2) - (bitmap.length / 2);
		}
		else this.xcenter = false;
		if(y == -1)
		{
			this.ycenter = true;
			y = (DisplayControl.DISPLAY_HEIGHT / 2) - (bitmap[0].length / 2);
		}
		else this.ycenter = false;
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.bitmap = bitmap;
	}
}
