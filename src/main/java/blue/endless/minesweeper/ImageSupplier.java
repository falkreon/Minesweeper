package blue.endless.minesweeper;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.playsawdust.glow.image.ImageData;
import com.playsawdust.glow.image.ImagePainter;
import com.playsawdust.glow.image.SrgbImageData;
import com.playsawdust.glow.image.color.BlendMode;
import com.playsawdust.glow.image.color.RGBColor;
import com.playsawdust.glow.image.io.PngImageIO;
import com.playsawdust.glow.io.DataSlice;

import blue.endless.minesweeper.Resources.Resource;

public class ImageSupplier {
	private ImageData missingno = new SrgbImageData(16, 16);
	private Map<Identifier, ImageData> images = new HashMap<>();
	
	public ImageSupplier() {
		Optional<ImageData> image = loadImageInternal(new Identifier("ms","textures/missing.png", ""));
		if (image.isPresent()) {
			missingno = image.get();
		} else {
			Minesweeper.LOGGER.info("Missingno is missing! Using magic pink instead.");
			ImagePainter p = new ImagePainter(missingno, BlendMode.NORMAL);
			p.clear(RGBColor.fromGamma(1, 1, 0, 1));
			RGBColor black = new RGBColor(1,0,0,0);
			p.fillRect(8, 0, 8, 8, black);
			p.fillRect(0, 8, 8, 8, black);
		}
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
				maybe = getImage(sourceImage);
				//maybe = images.get(sourceImage);
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
			
		} else {
			//Try and get it from Resources
			List<Resource> resources = Resources.get(id);
			for(Resource r : resources) {
				try {
					ImageData image = PngImageIO.load(DataSlice.of(Files.readAllBytes(r.path())));
					images.put(id, image);
					return image;
				} catch (IOException ex) {
				}
			}
		}
		
		return missingno;
	}
	
	private Optional<ImageData> loadImageInternal(Identifier id) {
		List<Resource> resources = Resources.get(id);
		for(Resource r : resources) {
			try {
				ImageData image = PngImageIO.load(DataSlice.of(Files.readAllBytes(r.path())));
				images.put(id, image);
				return Optional.of(image);
			} catch (IOException ex) {
			}
		}
		return Optional.empty();
	}
}
