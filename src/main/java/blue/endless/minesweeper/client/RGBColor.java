package blue.endless.minesweeper.client;

/**
 * Represents a linear (non-gamma-ramped) RGB color
 */
public record RGBColor(float alpha, float r, float g, float b) {
	public static RGBColor TRANSPARENT = new RGBColor(0,0,0,0);
	
	/**
	 * "Ideal" gamma suitable for an office setting with bright overhead lights and indirect daylight. The W3C
	 * recommends this value but very few people use it in practice, favoring 2.35 or 2.4 to reflect more typical
	 * viewing conditions such as a living room at night.
	 */
	public static final float IDEAL_GAMMA = 2.2f;
	
	/**
	 * The gamma of typical viewing conditions: gentle room lights with no indirect daylight.
	 */
	public static final float SRGB_GAMMA = 2.4f;
	
	public RGBColor(int srgb) {
		this(
				((srgb >> 24) & 0xFF) / 255.0f,
				gammaElementToLinear(((srgb >> 16) & 0xFF) / 255.0f, SRGB_GAMMA),
				gammaElementToLinear(((srgb >>  8) & 0xFF) / 255.0f, SRGB_GAMMA),
				gammaElementToLinear(((srgb      ) & 0xFF) / 255.0f, SRGB_GAMMA)
				);
	}

	public int toSrgb() {
		int r = (int) (linearElementToGamma(this.r, SRGB_GAMMA) * 255);
		int g = (int) (linearElementToGamma(this.g, SRGB_GAMMA) * 255);
		int b = (int) (linearElementToGamma(this.b, SRGB_GAMMA) * 255);
		int a = (int) (alpha * 255);
		
		// We can expect to encounter some out-of-gamut colors here; clamp everything rather than &'ing so that we hit
		// the closest in-gamut color to this object.
		if (r>0xFF) r=0xFF; if (r<0) r=0;
		if (g>0xFF) g=0xFF; if (g<0) g=0;
		if (b>0xFF) b=0xFF; if (b<0) b=0;
		a &= 0xFF; //Out of range numbers shouldn't happen even out-of-gamut, so we can just blast away any extra bits to be safe.
		
		return a << 24 | r << 16 | g << 8 | b;
	}
	
	/**
	 * Returns a version of this color with opacity scaled to the specified value. If this is an opaque color, the
	 * alpha of the returned color with be equal to the passed-in opacity. If this is a partially translucent color, the
	 * returned color with have alpha of opacity * this.getAlpha()
	 */
	public RGBColor withOpacity(float opacity) {
		return new RGBColor(alpha * opacity, r, g, b);
	}
	
	public static RGBColor fromGamma(float alpha, float r, float g, float b) {
		return new RGBColor(
				alpha,
				gammaElementToLinear(r, SRGB_GAMMA),
				gammaElementToLinear(g, SRGB_GAMMA),
				gammaElementToLinear(b, SRGB_GAMMA)
				);
	}
	
	public float gammaR() {
		return (float) linearElementToGamma(r, SRGB_GAMMA);
	}
	
	public float gammaG() {
		return (float) linearElementToGamma(g, SRGB_GAMMA);
	}
	
	public float gammaB() {
		return (float) linearElementToGamma(b, SRGB_GAMMA);
	}
	
	/** Converts one color sample from gamma colorspace into linear colorspace. */
	private static float gammaElementToLinear(float srgb, float gamma) {
		if (srgb<0) return 0.0f;
		
		if (srgb <= 0.04045) {
			return srgb / 12.92f;
		} else if (srgb <= 1.0) {
			return (float) Math.pow((srgb + 0.055) / 1.055, gamma);
		} else {
			return 1.0f;
		}
	}
	
	/** Converts one color sample from linear colorspace into gamma colorspace. */
	private static double linearElementToGamma(double linearElement, double gamma) {
		if (linearElement<0) {
			return 0;
		} else if (linearElement <= 0.0031308) {
			return linearElement * 12.92;
		} else if (linearElement <= 1.0) {
			return 1.055 * Math.pow(linearElement, 1.0 / gamma) - 0.055;
		} else {
			return 1.0;
		}
	}
}
