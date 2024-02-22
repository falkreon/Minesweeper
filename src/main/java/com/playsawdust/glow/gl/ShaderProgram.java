package com.playsawdust.glow.gl;

import java.nio.ByteBuffer;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.playsawdust.glow.image.color.Colors;
import com.playsawdust.glow.image.color.RGBColor;
import com.playsawdust.glow.vecmath.Matrix4;

import blue.endless.minesweeper.Minesweeper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class ShaderProgram {
	private final int vert;
	private final int frag;
	private final int prog;
	private Object2IntMap<String> uniforms = new Object2IntOpenHashMap<>();
	//private Object2IntMap<String> attributes = new Object2IntOpenHashMap<>();
	
	public ShaderProgram(String vertexSource, String fragmentSource) {
		vert = GL32.glCreateShader(GL32.GL_VERTEX_SHADER);
		frag = GL32.glCreateShader(GL32.GL_FRAGMENT_SHADER);
		prog = GL32.glCreateProgram();
		
		int[] status = { 0 };
		
		GL32.glShaderSource(vert, vertexSource);
		GL32.glCompileShader(vert);
		GL32.glGetShaderiv(vert, GL32.GL_COMPILE_STATUS, status);
		if (status[0] != GL32.GL_TRUE) {
			Minesweeper.LOGGER.error("ERROR compiling vertex shader: " + GL32.glGetShaderInfoLog(vert));
			return;
		} else {
			String vertResult = GL32.glGetShaderInfoLog(vert);
			if (vertResult != null && !vertResult.isBlank()) {
				Minesweeper.LOGGER.info("[Vert] " + vertResult);
			}
		}
		
		GL32.glShaderSource(frag, fragmentSource);
		GL32.glCompileShader(frag);
		GL32.glGetShaderiv(frag, GL32.GL_COMPILE_STATUS, status);
		if (status[0] != GL32.GL_TRUE) {
			Minesweeper.LOGGER.error("ERROR compiling fragment shader: " + GL32.glGetShaderInfoLog(frag));
			return;
		} else {
			String fragResult = GL32.glGetShaderInfoLog(frag);
			if (fragResult != null && !fragResult.isBlank()) {
				Minesweeper.LOGGER.info("[Frag] " + fragResult);
			}
		}
		
		
		GL32.glAttachShader(prog, vert);
		GL32.glAttachShader(prog, frag);
		GL32.glLinkProgram(prog);
		
		String programResult = GL32.glGetProgramInfoLog(prog);
		if (GL32.glGetProgrami(prog, GL32.GL_LINK_STATUS) == GL32.GL_FALSE) {
			Minesweeper.LOGGER.error("ERROR linking shader program: " + programResult);
		} else {
			if (programResult != null && !programResult.isBlank()) {
				Minesweeper.LOGGER.info("[Shader] " + programResult);
			}
		}
		
		int activeUniforms = GL32.glGetProgrami(prog, GL32.GL_ACTIVE_UNIFORMS);
		int maxNameLength = GL32.glGetProgrami(prog, GL32.GL_ACTIVE_UNIFORM_MAX_LENGTH);
		int[] len = { maxNameLength };
		int[] size = { 0 };
		int[] type = { 0 };
		MemoryStack stack = MemoryStack.stackPush();
		ByteBuffer buf = stack.malloc(maxNameLength + 1); //A trailing null will be written
		
		Minesweeper.LOGGER.info("" + activeUniforms + " shader uniform(s) available.");
		
		for(int i=0; i<activeUniforms; i++) {
			GL32.glGetActiveUniform(prog, i, len, size, type, buf);
			String uniformName = MemoryUtil.memASCII(buf, len[0]);
			
			String declaration = (size[0] == 1) ?
					AttribType.valueOf(type[0]).toString().toLowerCase() + " " + uniformName :
					AttribType.valueOf(type[0]).toString().toLowerCase() + "[" + size[0] + "] " + uniformName;
			
			Minesweeper.LOGGER.info("  " + declaration);
			
			uniforms.put(uniformName, i);
			len[0] = maxNameLength;
		}
		
		stack.pop();
		
		int activeAttributes = GL32.glGetProgrami(prog, GL32.GL_ACTIVE_ATTRIBUTES);
		for(int i=0; i<activeAttributes; i++) {
			
		}
	}
	
	public void set(String str, int value) {
		if (uniforms.containsKey(str)) {
			GL32.glUniform1i(uniforms.getInt(str), value);
		}
	}
	
	public void set(String str, float value) {
		if (uniforms.containsKey(str)) {
			GL32.glUniform1f(uniforms.getInt(str), value);
		}
	}
	
	public void set(String str, float x, float y, float z, float w) {
		if (uniforms.containsKey(str)) {
			GL32.glUniform4f(uniforms.getInt(str), x, y, z, w);
		}
	}
	
	public void set(String str, RGBColor value) {
		if (uniforms.containsKey(str)) {
			GL32.glUniform4f(uniforms.getInt(str),
				(float) Colors.linearElementToGamma(value.r()),
				(float) Colors.linearElementToGamma(value.g()),
				(float) Colors.linearElementToGamma(value.b()),
				value.alpha());
		}
	}
	
	public void set(String attribName, Matrix4f value) {
		if (uniforms.containsKey(attribName)) {
			float[] buf = value.get(new float[16]);
			GL32.glUniformMatrix4fv(uniforms.getInt(attribName), false, buf);
		}
	}
	
	public void set(String attribName, Matrix4 value) {
		if (uniforms.containsKey(attribName)) {
			/*
			float[] buf = new float[] {
				(float) value.a(), (float) value.b(), (float) value.c(), (float) value.d(),
				(float) value.e(), (float) value.f(), (float) value.g(), (float) value.h(),
				(float) value.i(), (float) value.j(), (float) value.k(), (float) value.l(),
				(float) value.m(), (float) value.n(), (float) value.o(), (float) value.p()
			};*/
			
			float[] buf = new float[] {
				(float) value.a(), (float) value.e(), (float) value.i(), (float) value.m(),
				(float) value.b(), (float) value.f(), (float) value.j(), (float) value.n(),
				(float) value.c(), (float) value.g(), (float) value.k(), (float) value.o(),
				(float) value.d(), (float) value.h(), (float) value.l(), (float) value.p()
			};
			GL32.glUniformMatrix4fv(uniforms.getInt(attribName), false, buf);
		}
	}
	
	public void use() {
		GL32.glUseProgram(prog);
	}
	
	public void delete() {
		GL32.glDeleteProgram(prog);
		GL32.glDeleteShader(vert);
		GL32.glDeleteShader(frag);
	}
	
	
}
