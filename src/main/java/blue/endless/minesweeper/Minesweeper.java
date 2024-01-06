package blue.endless.minesweeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class Minesweeper implements ModInitializer {
	public static final String MODID = "minesweeper";
	public static final Logger LOGGER = LoggerFactory.getLogger("Minesweeper");

	public static void runMainInitializers() {
		var containers = FabricLoader.getInstance().getEntrypointContainers("main", ModInitializer.class);
		for(var container : containers) {
			String modid = container.getProvider().getMetadata().getId();
			
			// Fabric errors horribly if allowed to initialize because it checks whether minecraft has been bootstrapped.
			if (modid.startsWith("fabric-")) continue;
			
			LOGGER.info("Initializing \"" + container.getProvider().getMetadata().getId() + "\"");
			container.getEntrypoint().onInitialize();
		}
	}
	
	@Override
	public void onInitialize() {
		//LOGGER.info("Minesweeper Mod Initialized.");
	}
}