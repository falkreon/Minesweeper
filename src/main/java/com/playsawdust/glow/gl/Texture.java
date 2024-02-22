package com.playsawdust.glow.gl;

import org.lwjgl.opengl.GL32;

import com.playsawdust.glow.image.ImageData;
import com.playsawdust.glow.image.LinearImageData;
import com.playsawdust.glow.image.SrgbImageData;
import com.playsawdust.glow.image.color.RGBColor;

import blue.endless.minesweeper.Destroyable;

public class Texture implements ImageData, Destroyable {
	private final int handle;
	private final int width;
	private final int height;
	
	public Texture(int width, int height) {
		this.handle = GL32.glGenTextures();
		this.width = width; this.height = height;
	}
	
	public Texture(ImageData src) {
		this.handle = GL32.glGenTextures();
		this.width = src.getWidth();
		this.height = src.getHeight();
		
		int[] data;
		if (src instanceof SrgbImageData srgb) {
			data = srgb.getData();
		} else if (src instanceof LinearImageData linear) {
			data = linear.toSrgb().getData(); //TODO: We might upload the texture with full depth!
		} else {
			data = new int[width * height];
			for(int y=0; y<height; y++) {
				for(int x=0; x<width; x++) {
					data[y*width + x] = src.getSrgbPixel(x, y);
				}
			}
		}
		
		GL32.glBindTexture(GL32.GL_TEXTURE_2D, handle);
		GL32.glTexImage2D(GL32.GL_TEXTURE_2D, 0, GL32.GL_RGBA8, width, height, 0, GL32.GL_BGRA, GL32.GL_UNSIGNED_INT_8_8_8_8_REV, data);
		GL32.glGenerateMipmap(GL32.GL_TEXTURE_2D);
	}
	
	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}
	
	public int getHandle() {
		return handle;
	}

	@Override
	public RGBColor getLinearPixel(int arg0, int arg1) {
		//GL32.glGetTexImage(handle, 0, GL32.GL_BGRA_INTEGER, GL32.GL_UNSIGNED_INT_8_8_8_8, null);
		//GL32.glGet
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSrgbPixel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPixel(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPixel(int arg0, int arg1, RGBColor arg2) {
		// TODO Auto-generated method stub
		
	}
	
	public void bind() {
		GL32.glBindTexture(0, handle);
	}
	
	@Override
	public void destroy() {
		GL32.glDeleteTextures(handle);
	}
}
