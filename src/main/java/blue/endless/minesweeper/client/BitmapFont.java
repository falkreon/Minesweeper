package blue.endless.minesweeper.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import com.playsawdust.glow.gl.WindowPainter;
import com.playsawdust.glow.image.color.RGBColor;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class BitmapFont {
	private Long2ObjectOpenHashMap<BitmapSprite> glyphs = new Long2ObjectOpenHashMap<>();
	
	public void addGlyph(long code, BitmapSprite im) {
		glyphs.put(code, im);
	}

	public void loadHex(InputStream in) throws IOException {
		BufferedReader reader= new BufferedReader(new InputStreamReader(in));
		
		for(int i=0; i<256; i++) {
			String s = reader.readLine();
			String[] parts = s.split(Pattern.quote(":"));
			long codePoint = Long.parseLong(parts[0], 16);
			BitmapSprite sprite = BitmapSprite.fromHex(parts[1]);
			glyphs.put(codePoint, sprite);
		}
	}
	
	/**
	 * Draws a single glyph
	 * @param p the Painter to use for drawing commands
	 * @param x the x coordinate to draw at
	 * @param y the y coordinate to draw at - glyphs do not draw at their baseline, the y coordinate given will be the
	 *          top edge of the character bounding box.
	 * @param codePoint the code point / glyph number to draw
	 * @param color the color to draw the character in
	 * @return the advance width of the character drawn
	 */
	public int drawChar(WindowPainter p, int x, int y, int codePoint, RGBColor color, RGBColor outlineColor, int scale) { //TODO: Change this to Painter when this drawImage is upstreamed
		BitmapSprite sprite = glyphs.get(codePoint);
		if (sprite == null) {
			return 8;
		} else {
			sprite.setColor(color);
			if (outlineColor.alpha() > 0) {
				sprite.setOutline(true);
				sprite.setOutlineColor(outlineColor);
			} else {
				sprite.setOutline(false);
			}
			p.drawImage(sprite, x, y, sprite.getWidth() * scale, sprite.getHeight() * scale, 0, 0, sprite.getWidth(), sprite.getHeight(), 1f);
			return sprite.getWidth() * scale;
		}
	}
	
	public void drawString(WindowPainter p, int x, int y, String s, RGBColor color, int scale) { //TODO: Change this to Painter when this drawImage is upstreamed
		int xofs = 0;
		
		float outlineBrightness = 0.05f;
		RGBColor outline = new RGBColor(color.alpha(), color.r() * outlineBrightness, color.g() * outlineBrightness, color.b() * outlineBrightness);
		
		for(int i=0; i<s.length(); i++) {
			if (Character.isLowSurrogate(s.charAt(i))) continue; //We're using code points
			
			int codePoint = s.codePointAt(i);
			xofs += drawChar(p, x + xofs, y, codePoint, color, outline, scale);
		}
	}
	
	public void drawShadowString(WindowPainter p, int x, int y, String s, RGBColor color, int scale) { //TODO: Change this to Painter when this drawImage is upstreamed
		drawString(p, x + scale, y + scale, s, RGBColor.fromGamma(1, 0, 0, 0), scale);
		drawString(p, x, y, s, color, scale);
	}
}
