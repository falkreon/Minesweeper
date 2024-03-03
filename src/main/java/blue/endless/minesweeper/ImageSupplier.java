package blue.endless.minesweeper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.playsawdust.glow.image.ImageData;
import com.playsawdust.glow.image.SrgbImageData;
import com.playsawdust.glow.image.io.PngImageIO;
import com.playsawdust.glow.io.DataSlice;

public class ImageSupplier {
	private ImageData missingno = new SrgbImageData(16, 16);
	private Map<Identifier, ImageData> images = new HashMap<>();
	
	public ImageSupplier() {
		try {
			ImageData tileset = PngImageIO.load(
				DataSlice.of(Files.readAllBytes(Path.of("tiles.png")))
				);
			images.put(Identifier.of("ms:tiles.png"), tileset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			ImageData player = PngImageIO.load(
				DataSlice.of(Files.readAllBytes(Path.of("player.png")))
				);
			images.put(Identifier.of("ms:player.png"), player);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//TODO: Load up missingno
	}
	
	public ImageData getImage(Identifier id) {
		ImageData directResult = images.get(id);
		if (directResult != null) return directResult;
		
		if (!id.selector().isEmpty()) {
			//First try and get a precalculated image
			ImageData maybe = images.get(id);
			if (maybe!=null) return maybe;
			
			//Nope. Do some math, then get the source image.
			try {
				int tileIndex = Integer.parseUnsignedInt(id.selector());
				
				Identifier sourceImage = new Identifier(id.namespace(), id.path(), "");
				maybe = images.get(sourceImage);
				if (maybe!=null) {
					
					//Where will the destitch take place?
					int tilesWide = maybe.getWidth() / 16;
					int tilesHigh = maybe.getHeight() / 16;
					int tileY = tileIndex / tilesWide;
					int tileX = tileIndex % tilesWide;
					if (tileX >= tilesWide || tileY >= tilesHigh) return missingno;
					
					//Destitch the tile out of the image
					ImageData result = new SrgbImageData(16, 16);
					for(int y=0; y<16; y++) {
						for(int x=0; x<16; x++) {
							result.setPixel(x, y, maybe.getSrgbPixel(tileX * 16 + x, tileY * 16 + y));
						}
					}
					
					images.put(id, result); // Make future lookups fast
					return result;
				} else {
					return missingno;
				}
			} catch (NumberFormatException nfex) {
				return missingno;
			}
			
		}
		
		return missingno;
	}
}
