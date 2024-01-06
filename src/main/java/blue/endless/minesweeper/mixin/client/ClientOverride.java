package blue.endless.minesweeper.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import blue.endless.minesweeper.Minesweeper;
import blue.endless.minesweeper.client.MinesweeperClient;

@Mixin(net.minecraft.client.main.Main.class)
public class ClientOverride {
	@Inject(at = @At("HEAD"), method="main", cancellable = true)
	private static void overrideMain(String[] args, CallbackInfo ci) {
		Minesweeper.LOGGER.info("Total control obtained from client.");
		
		Minesweeper.runMainInitializers();
		
		MinesweeperClient client = new MinesweeperClient();
		client.init();
		
		ci.cancel();
	}
}