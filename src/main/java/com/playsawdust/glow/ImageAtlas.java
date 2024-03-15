package com.playsawdust.glow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.playsawdust.glow.image.ImageData;

public class ImageAtlas {
	private ImageData image;
	private List<Sprite> sprites = new ArrayList<>();
	private LinkedHashMap<String, Sprite> identifiedSprites = new LinkedHashMap<>();
	
	public static class Sprite {
		private ImageAtlas atlas;
		private int x;
		private int y;
		private int width;
		private int height;
		
		public Sprite(ImageAtlas atlas, int x, int y, int width, int height) {
			
		}
	}
}
