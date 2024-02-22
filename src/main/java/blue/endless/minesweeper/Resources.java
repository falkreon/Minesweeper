package blue.endless.minesweeper;

import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class Resources {
	public void get(String id) {
		for(ModContainer container : FabricLoader.getInstance().getAllMods()) {
			for(Path p : container.getRootPaths()) {
				
			}
		}
	}
}
