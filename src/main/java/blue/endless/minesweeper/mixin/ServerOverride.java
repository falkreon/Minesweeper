package blue.endless.minesweeper.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import blue.endless.minesweeper.Minesweeper;

@Mixin(net.minecraft.server.Main.class)
public class ServerOverride {
	@Inject(at = @At("HEAD"), method="main", cancellable = true)
	private static void overrideMain(String[] args, CallbackInfo ci) {
		Minesweeper.LOGGER.info("Total control obtained from server.");
		
		Minesweeper.runMainInitializers();
		
		ci.cancel();
	}
}