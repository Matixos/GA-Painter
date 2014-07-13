package pl.com.mat.painter.fractals;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.Random;

import pl.com.mat.painter.fractals.HeightField.WrapMode;

public class Clouds {
	
	Random rn = new Random();

	public BufferedImage createCloud(Graphics2D g) {

		final int HEIGHT_MAX = 255;
		final int w = 250;
		final int h = 180;

		HeightField hf = new HeightField(w, h, WrapMode.Spherical);

		long seed = Math.abs(rn.nextLong());

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);

		midpointDisplacement(hf, 512, w, h, HEIGHT_MAX, 0.8f, seed);
		hf.normalize(0, 256);
		
		g.drawImage(hf.getImage(), 0, 0, null);
		
		return hf.getImage();
	}
	
	

	protected static void midpointDisplacement(final HeightField hf,
			final int L, final int MX, final int MY, int range,
			final float persist, final long seed) {
		/*
		 * NOTE: A is (x1, y1) B is (x2, y2) A,B,C,D are the original "corner"
		 * points
		 * 
		 * G
		 * 
		 * B D
		 * 
		 * H E I
		 * 
		 * A C
		 * 
		 * F
		 */

		Random r = new Random(seed);

		for (int y = 0; y < MY; y += L) {
			for (int x = 0; x < MX; x += L) {
				hf.set(x, y, r.nextInt(256));
			}
		}

		int step = L;
		int halfStep = L / 2;
		int x2, y2, midx, midy;

		while (step >= 1) {
			// Diamond step across entire array...
			for (int y1 = 0; y1 < MY; y1 += step) {
				for (int x1 = 0; x1 < MX; x1 += step) {
					x2 = x1 + step;
					y2 = y1 + step;
					midx = x1 + halfStep;
					midy = y1 + halfStep;

					final int sum = hf.get(x1, y1) + hf.get(x1, y2)
							+ hf.get(x2, y1) + hf.get(x2, y2);

					hf.set(midx, midy, sum / 4 + perturb(r, range));
				}
			}

			// Square step across entire array...
			for (int y1 = 0; y1 < MY; y1 += step) {
				for (int x1 = 0; x1 < MX; x1 += step) {
					x2 = x1 + step;
					y2 = y1 + step;
					midx = x1 + halfStep;
					midy = y1 + halfStep;

					/*
					 * x1 mx x2 G
					 * 
					 * B 4 D y2
					 * 
					 * H 1 E 2 I midy
					 * 
					 * A 3 C y1
					 * 
					 * F
					 */
					int A = hf.get(x1, y1);
					int B = hf.get(x1, y2);
					int C = hf.get(x2, y1);
					int D = hf.get(x2, y2);
					int E = hf.get(midx, midy);
					int F = hf.get(midx, y1 - halfStep);
					int G = hf.get(midx, y2 + halfStep);
					int H = hf.get(x1 - halfStep, midy);
					int I = hf.get(x2 + halfStep, midy);

					hf.set(x1, midy, (A + B + E + H) / 4); // 1
					hf.set(x2, midy, (C + D + E + I) / 4); // 2
					hf.set(midx, y1, (A + C + E + F) / 4); // 3
					hf.set(midx, y2, (B + D + E + G) / 4); // 4
				}
			}

			// Prepare for next iteration...
			range *= persist;
			step /= 2;
			halfStep /= 2;
		}
	}

	protected static int perturb(Random r, int range) {
		if (range == 0)
			return 0;
		return r.nextInt(range * 2) - range;
	}
	
	
}

class HeightField {

	public enum WrapMode {
		Bicylindrical {
			public int index(int x, int y, int width, int height) {
				x %= width;
				if (x < 0) {
					x += width;
				}
				y %= height;
				if (y < 0) {
					y += height;
				}
				return y * width + x;
			}
		},
		/**
		 * In the X direction, map as if the world were a cylinder. In the Y
		 * direction, if we overflow above, flow down on the opposite side; if
		 * we overflow below, flow up on the opposite side. Do the Y direction
		 * first so we only need to normalise width once.
		 */
		Spherical {
			public int index(int x, int y, int width, int height) {
				// Y / Height
				if (y >= height) {
					// height, minus the amount we exceeded height by
					y = height - (y % height);
					x += width / 2;
				} else if (y < 0) {
					// 0, plus the amount we were "too low" by
					y = -(y % height);
					x += width / 2;
				}
				if (y == height) {
					y = height - 1;
				}

				// X / Width
				x %= width;
				if (x < 0) {
					x += width;
				}

				return y * width + x;
			}
		};

		public abstract int index(int x, int y, int width, int height);
	}

	protected final int width, height;
	protected final WrapMode wrap;

	/**
	 * "Two-dimensional" array, in row-major order.
	 */
	protected final int[] array;

	public HeightField(int width, int height, WrapMode wrap) {
		this.width = width;
		this.height = height;
		this.wrap = wrap;
		array = new int[width * height];
	}

	public int get(int x, int y) {
		return array[wrap.index(x, y, width, height)];
	}

	public void set(int x, int y, int value) {
		array[wrap.index(x, y, width, height)] = value;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public BufferedImage getImage() {
		BufferedImage img = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				int h = clamp(get(x, y), 0, 255);
				int rgb;
				if (h < 128) {
					rgb = (h << 16) | (h << 8) | 255;
					/*
					 * } else if ( h < 128 ) { rgb = (h + 30) << 16 | (h + 15)
					 * << 8 | h; } else if ( h < 196 ) { rgb = (h - 128) << 16 |
					 * h << 8;
					 */
				} else {
					rgb = (h << 16) | (h << 8) | h;
				}
				img.setRGB(
				// ( x + width / 2 ) % width,
						x, y, rgb);
			}
		}
		
		return img;
	}

	/**
         *
         */
	protected void normalize(int bottom, int top) {
		int min = top, max = bottom;
		int range = top - bottom;
		for (int i = 0, j = width * height; i < j; ++i) {
			int h = array[i];
			min = Math.min(min, h);
			max = Math.max(max, h);
		}

		// Now we know that the entire heightmap lies in [min,max].
		// We re-scale so that min->bottom, max->top
		float difference = max - min;
		for (int i = 0, j = width * height; i < j; ++i) {
			int h = array[i];
			array[i] = Math.round(range * (h - min) / difference);
		}
	}

	protected static int clamp(int value, int min, int max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}
}