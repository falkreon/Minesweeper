package blue.endless.minesweeper.client;

import org.lwjgl.opengl.GL32;

import blue.endless.minesweeper.Minesweeper;

public record ShaderProgram(int vert, int frag, int prog) {
	public ShaderProgram(String vertexSource, String fragmentSource) {
		this(
			GL32.glCreateShader(GL32.GL_VERTEX_SHADER),
			GL32.glCreateShader(GL32.GL_FRAGMENT_SHADER),
			GL32.glCreateProgram());
		
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
				Minesweeper.LOGGER.info(vertResult);
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
				Minesweeper.LOGGER.info(fragResult);
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
				Minesweeper.LOGGER.info(programResult);
			}
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
