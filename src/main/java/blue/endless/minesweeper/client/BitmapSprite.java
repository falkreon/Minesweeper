package blue.endless.minesweeper.client;

import com.playsawdust.glow.image.ImageData;
import com.playsawdust.glow.image.color.RGBColor;

/**
 * Bitmapped ImageData with a maximum width of 64. Pixels will be reported as black (or whatever the color is set to)
 */
public class BitmapSprite implements ImageData {
	private RGBColor color = RGBColor.fromGamma(1, 1, 1, 1);
	private boolean outline = true;
	private RGBColor outlineColor = RGBColor.fromGamma(1, 0, 0, 0);
	private int width;
	private int height;
	private long[] data;
	
	public BitmapSprite(int width, int height) {
		if (width<0 || height<0) throw new IllegalArgumentException("Width and height must be zero or positive.");
		if (width>64) throw new IllegalArgumentException("Width "+width+" is not allowed (max width is 64)");
		this.width = width;
		this.height = height;
		this.data = new long[height];
	}
	
	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public RGBColor getLinearPixel(int x, int y) {
		boolean bit = getBitPixel(x, y);
		if (bit) return color;
		
		boolean outline =
			getBitPixel(x-1, y) ||
			getBitPixel(x+1, y) ||
			getBitPixel(x, y-1) ||
			getBitPixel(x, y+1);
		return (outline) ? outlineColor : RGBColor.TRANSPARENT;
	}

	@Override
	public int getSrgbPixel(int x, int y) {
		return getLinearPixel(x, y).toSrgb();
	}
	
	public boolean getBitPixel(int x, int y) {
		if (x<0 || y<0 || x>=width || y>=height) return false;
		
		long bin = data[y];
		//Pixels are LSB-first to make shifts less confusing
		int pixel = (int) ((bin >> x) & 1L);
		return (pixel==1);
	}

	@Override
	public void setPixel(int x, int y, int argb) {
		int a = (argb >>24) & 0xFF;
		
		setPixel(x, y, a > 127);
	}

	@Override
	public void setPixel(int x, int y, RGBColor color) {
		setPixel(x, y, color.alpha()>0.5f);
	}
	
	public void setPixel(int x, int y, boolean bit) {
		if (x<0 || y<0 || x>=width || y>=height) return;
		
		long bin = data[y];
		long mask = 1 << x;
		if (bit) {
			bin |= mask;
		} else {
			bin &= ~mask;
		}
		data[y] = bin;
	}
	
	/**
	 * Sets the color reported for filled pixels in this bitmap
	 * @param color the new color
	 */
	public void setColor(RGBColor color) {
		this.color = color;
	}
	
	public static BitmapSprite fromHex(String s) {
		int height = 16;
		int width = (s.length() / 32) * 8;
		if (width<=0) return new BitmapSprite(0,0);
		
		BitmapSprite result = new BitmapSprite(width, height);
		
		int ofs = 0;
		for(int y=0; y<height; y++) {
			for(int xn=0; xn<width/4; xn++) {
				int n = Integer.parseInt(""+s.charAt(ofs), 16);
				for(int b=0; b<4; b++) {
					int x = xn * 4 + b;
					boolean bit = ((n >> (3-b)) & 1) == 1;
					result.setPixel(x, y, bit);
				}
				ofs++;
			}
		}
		
		return result;
	}

	public void setOutlineColor(RGBColor outlineColor) {
		this.outlineColor = outlineColor;
	}

	public void setOutline(boolean outline) {
		this.outline = outline;
	}
}
