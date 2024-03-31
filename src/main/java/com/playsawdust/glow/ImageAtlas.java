package com.playsawdust.glow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.playsawdust.glow.image.ImageData;
import com.playsawdust.glow.render.Painter;

public class ImageAtlas {
	private ImageData image;
	private Sprite wholeAtlasSprite;
	private List<Sprite> sprites = new ArrayList<>();
	private LinkedHashMap<String, Sprite> identifiedSprites = new LinkedHashMap<>();
	
	public ImageAtlas(ImageData atlasImage) {
		this.image = atlasImage;
		this.wholeAtlasSprite = new Sprite(this, 0, 0, atlasImage.getWidth(), atlasImage.getHeight());
	}
	
	/**
	 * Gets a named sprite from this atlas, with fallbacks.
	 * @param id The selector value for the desired sprite
	 * @return The sprite
	 */
	public Sprite getSprite(String id) {
		Sprite result = identifiedSprites.get(id);
		if (result != null) return result;
		
		if (!sprites.isEmpty()) {
			return sprites.get(0);
		} else {
			return wholeAtlasSprite;
		}
	}
	
	public static record Sprite(ImageAtlas atlas, int x, int y, int width, int height) {
		
		public void draw(Painter p, int x, int y, float opacity) {
			p.drawImage(atlas.image, x, y, width, height, this.x, this.y, opacity);
		}
		
	}
	
	private class SpriteImage {
		private ImageAtlas atlas;
		private int x;
		private int y;
		private int width;
		private int height;
	}
}
