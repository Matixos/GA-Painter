package pl.com.mat.painter.fractals;

//**************************************************************************
//Mandelbrot Fractor Generator
//Ryan Sweny
//rsweny@alumni.uwaterloo.ca
//2002
//v2.1
//**************************************************************************

import java.awt.*;
import javax.swing.JFrame;

import pl.com.mat.painter.Genom;
import pl.com.mat.painter.math.Complex;


public class Taylor implements Runnable {

	int lasti;
	Graphics2D offscreenGraphics;
	JFrame painter;
	Thread runner;

	double zoom = 8;
	double xcen = 0;
	double ycen = 3;
	
	int p_r = 1;
	int p_g = 1;
	int p_b = 1;
	
	double useRandCols;

	int quad = 1;
	long depth = 8;
	int points = 0;

	boolean scalePoint;
	boolean compoundPoint;
	boolean recurse;
	boolean mandelbrot;
	double complexScale = 4;
	float cx = 0.0f;
	float cy = 0.0f;
	double glow = 0.07;

	double brightness = 1.0;
	double brightness_factor = 1.0;

	int yimlen = 180;
	int ximlen = 250;

	int[][] img_red = null;
	int[][] img_green = null;
	int[][] img_blue = null;
	int[][] img_alpha = null;
	int[] pixels;

	int xcurr, ycurr, xanchor, yanchor;
	int alg = 0;

	long lastRunTime;
	double reset = 2;
	
	public Taylor(Graphics2D graph, JFrame painter) {
		this.offscreenGraphics = graph;
		this.painter = painter;

		points = ximlen;

		clearScreenAndReset();
	}

	public void start() {
		if (runner == null)
			;
		{
			runner = new Thread(this);
			runner.start();
		}
	}

	public void stop() {
		runner = null;
	}

	public void newFractal() {
		alg = (alg + 1) % 3;
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		while (runner == thisThread) {
			try {
				if (reset > -1) {
					clearScreenAndReset();
				}
				nextpoints(alg);

				long diff = (System.currentTimeMillis() - lastRunTime);
				if (diff > 5000) {
					lastRunTime = System.currentTimeMillis();

					Thread.sleep(100);
				}
				painter.repaint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void clearScreenAndReset() {
		lasti = 0;
		reset = -1;

		img_red = new int[ximlen][yimlen];
		img_green = new int[ximlen][yimlen];
		img_blue = new int[ximlen][yimlen];
		img_alpha = new int[ximlen][yimlen];
		pixels = new int[ximlen * yimlen];

		lastRunTime = 0;
	}

	public void setConstants(Genom g) {
		zoom = g.getZoom();
		xcen = g.getX();
		ycen = g.getY();
		glow = g.getGlow();
		brightness = g.getBrightness();
		depth = g.getDepth();
		brightness_factor = Math.pow(depth, brightness);
		complexScale = g.getComplexScale();
		
		p_r = g.getRedParam();
		p_g = g.getGreenParam();
		p_b = g.getBlueParam();
		
		useRandCols = g.getUseRandCols();
	}

	private synchronized void nextpoints(int alg) {
		double xf, yf;
		int xi, yi, i;

		Complex one = new Complex(1, 0);
		Complex two = new Complex(2, 0);

		for (i = lasti; i < lasti + points - quad; i += quad) {
			if (i >= pixels.length) // if we're done the first pass, do random
									// points to anti-alias
			{
				xf = Math.random();
				yf = Math.random();
				xi = (int) (ximlen * xf);
				yi = (int) (yimlen * yf);
			} else // do every pixel in order
			{
				xi = (i % ximlen);
				yi = (i / ximlen);
				xf = (double) xi / (double) ximlen;
				yf = (double) yi / (double) yimlen;
			}

			// based on the algorithm published here:
			// http://fractorama.com/doc/taylor.html
			// ////////////////////////////////////
			double a = 2 * (xf - 0.5) * zoom + xcen;
			double b = 2 * (yf - 0.5) * zoom + ycen;

			// the current pixel we are calculating
			Complex current = new Complex(a, b);

			// the real value for this pixel
			Complex zvalue;
			if (alg == 0) {
				zvalue = Complex.pow(current, Math.E);
			} else if (alg == 1) {
				zvalue = (one.add(current).div(one.sub(current))).log();
			} else {
				zvalue = current.sinh();
			}

			double xavg = 0;
			double yavg = 0;
			double mavg = 0;

			double xmax = 0;
			double ymax = 0;
			double mmax = 0;

			double xtot = 0;
			double ytot = 0;
			double mtot = 0;

			double den = 1;
			double den2 = 1;
			Complex denominator = new Complex(den);
			Complex denominator2 = new Complex(den);

			// expand at f(x - point)
			Complex point = Complex.pow(current, Math.E).add(
					new Complex(cx, cy));

			Complex value = new Complex(0, 0);
			Complex zSum = new Complex(0, 0);

			// Loop until we've been through the loop 'depth' times
			long count = 0;
			while (count < depth) {
				Complex scale = new Complex(1, complexScale);
				Complex oldcurrent = current;
				if (scalePoint) // multiply by f(a)
				{
					if (alg == 0) {
						scale = Complex.pow(point, Math.E);
					} else if (alg == 1) {
						scale = (one.add(point).div(one.sub(point))).log();
					} else {
						scale = point.sinh();
					}
				}

				// f(x-a): we will approximate the series about a
				if (compoundPoint) {
					current = current.sub(point);
				}

				// factorial denominators
				if (count > 0) {
					den *= count;
					den2 *= (count * 2 + 1);
					denominator = new Complex(den);
					denominator2 = new Complex(den2);
				}

				if (alg == 0) { // f(x) = x^n / n!
					Complex numerator = scale.mul(Complex.pow(current, count));
					zSum = zSum.add(numerator.div(denominator));
				} else if (alg == 1) { // f(x) = 2x^(2n-1) / (2n-1)
					Complex twoN1 = new Complex(2 * count - 1);
					Complex numerator = scale.mul(two.mul(Complex.pow(current,
							twoN1)));
					zSum = zSum.add(numerator.div(twoN1));
				} else { // f(x) = x^(2n+1) / (2n+1)!
					Complex numerator = scale.mul(Complex.pow(current, den2));
					zSum = zSum.add(numerator.div(denominator2));
				}

				if (mandelbrot)
					zSum = zSum.mul(zvalue.sub(zSum));

				if (!recurse)
					current = oldcurrent;

				value = zSum.cot();
				// Complex cotz = zvalue.cot();

				// System.out.println("val re " + value.re + " val im " +
				// value.im);
				if (Double.isNaN(value.re) || Double.isNaN(value.im)) {
					// count = depth;
				} else {
					double x = Complex.pow(value, 0.1).abs();
					double y = Complex.pow(value, 0.5).abs();
					double m = Complex.pow(value, 0.9).abs();

					if (xavg == 0) {
						xavg = x;
						yavg = y;
						mavg = m;
					}

					double xdev = Math.abs(x - xavg);
					double ydev = Math.abs(y - yavg);
					double mdev = Math.abs(m - mavg);

					xavg = (x * 0.5 + xavg * (0.5 + 0.01 * glow));
					yavg = (y * 0.5 + yavg * (0.5 + 0.1 * glow));
					mavg = (m * 0.5 + mavg * (0.5 + 1 * glow));

					xmax = Math.max(xmax, xdev);
					ymax = Math.max(ymax, ydev);
					mmax = Math.max(mmax, mdev);

					xtot += xdev;
					ytot += ydev;
					mtot += mdev;
				}
				count++;
			}

			int red = 0;
			int green = 0;
			int blue = 0;

			if (count > 0) {
				if(useRandCols > 0.6) {
					red = ((int) (((xtot * 255) / brightness_factor) / xmax)*p_r)%256 ;
					green = ((int) (((ytot * 255) / brightness_factor) / ymax)*p_g)%256;
					blue = ((int) (((mtot * 255) / brightness_factor) / mmax)*p_b)%256;
				} else {
					red = (int) (((xtot * 255) / brightness_factor) / xmax);
					green = (int) (((ytot * 255) / brightness_factor) / ymax);
					blue = (int) (((mtot * 255) / brightness_factor) / mmax);
				}
			}

			img_red[xi][yi] += red;
			img_green[xi][yi] += green;
			img_blue[xi][yi] += blue;
			img_alpha[xi][yi]++;

			red = img_red[xi][yi] / img_alpha[xi][yi];
			green = img_green[xi][yi] / img_alpha[xi][yi];
			blue = img_blue[xi][yi] / img_alpha[xi][yi];

			if (red > 255)
				red = 255;
			if (green > 255)
				green = 255;
			if (blue > 255)
				blue = 255;

			red = red << 16;
			green = green << 8;
			pixels[yi * ximlen + xi] = 0xff000000 | (red & 0xffff0000)
					| (green & 0x0000ff00) | (blue & 0xff);

			// rough painting
			if (i < pixels.length) {
				offscreenGraphics.setColor(new Color(pixels[yi * ximlen + xi]));
				offscreenGraphics.fillRect(xi, yi, quad, quad);
			}
		}

		if (i <= pixels.length - points * quad) {
			lasti += points * quad;
		} else if (quad > 1) {
			lasti = 0;
			quad = 1;
		} else // start of random points
		{
			lasti += points;
		}

	}

}