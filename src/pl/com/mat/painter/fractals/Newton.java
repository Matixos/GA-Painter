package pl.com.mat.painter.fractals;

//**************************************************************************
// Newton Fractor Generator
// Ryan Sweny
// rsweny@alumni.uwaterloo.ca
// 2004
//**************************************************************************

import java.awt.*;
import java.util.*;

import javax.swing.JFrame;

import pl.com.mat.painter.math.*;

public class Newton implements Runnable {

	Graphics offscreenGraphics;
	JFrame painter;

	Thread runner;
	boolean running = false;

	double zoom = 3;
	double xcen = 0.0;
	double ycen = 0.0;

	float brightness = 6.0f;
	float intensity = 0.05f;
	int alg = 1;
	float rootBoundry = 0.0000000000001f;
	float initColor = 0.6f;

	long runtotal = 0;
	float quad = 25;

	int depth = 1000;
	int points = 250;

	float order = 3;
	float img_order = 0;

	int yimlen = 180;
	int ximlen = 250;

	double p1 = Math.random();
	double p2 = Math.random();

	boolean mandelbrotAddition = false;
	long lastPassTime;
	long lastRunTime = 0;
	int reset = -1;
	boolean fixHoles = false;

	// constants
	Complex two = new Complex(2, 0);
	Complex one = new Complex(1, 0);
	Complex pointone = new Complex(0.1, 0);

	double complex_error = 0;
	Complex oneError = new Complex(1, complex_error);

	int[] pixels;
	int[][] img_red = null;
	int[][] img_green = null;
	int[][] img_blue = null;
	int[][] img_alpha = null;
	LinkedList<Complex> roots = new LinkedList<Complex>();
	
	public Newton(Graphics2D g2d, JFrame painter) {
		this.offscreenGraphics = g2d;
		this.painter = painter;
		
		clearScreenAndReset(1);
	}

	public void start() {
		running = true;

		runner = new Thread(this);
		runner.start();
	}

	public void stop() {
		running = false;

		runner = null;

		System.out.println("stop()");
	}

	public void newFractal() {
		roots = new LinkedList<Complex>();
		p1 = Math.random();
		runtotal = 0;
		quad = 5;
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		while (running) {
			try {

				if (reset > -1) {
					clearScreenAndReset(reset);
				}
				offscreenGraphics.setPaintMode();
				nextpoints(alg);

				Thread.sleep(100);

				long diff = (System.currentTimeMillis() - lastRunTime);
				if (diff > 5000) {
					lastRunTime = System.currentTimeMillis();
					updateHistogram();

				}
				painter.repaint(); // ew fire event
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("run() exited: " + thisThread.toString());
	}

	public synchronized void updateHistogram() {
		// System.out.println("quad is " + quad);
		if (fixHoles) {
			fixHoles = false;
			int cnt = 0;
			System.out.println("Hitting remaining pixels...");
			for (int i = 0; i < ximlen; i++) {
				for (int j = 0; j < yimlen; j++) {
					if (img_alpha[i][j] == 0) {
						int xi = (i % ximlen);
						double xf = (double) xi / (double) ximlen;
						double yf = (double) j / (double) yimlen;
						doCalculation(xf, yf, xi, j);
						cnt++;
					}
				}
			}

			System.out.println("Hitting remaining done: " + cnt);
		} else if (runtotal > pixels.length * 2) {
			;// offscreenImage = new BufferedImage(ximlen, yimlen,
				// BufferedImage.TYPE_INT_RGB);
		}

	}

	public void clearScreenAndReset(int r) {
		oneError = new Complex(1, complex_error);

		if (r == 1)
			newFractal();

		runtotal = 0;
		quad = 10;
		reset = -1;

		img_red = new int[ximlen][yimlen];
		img_green = new int[ximlen][yimlen];
		img_blue = new int[ximlen][yimlen];
		img_alpha = new int[ximlen][yimlen];
		pixels = new int[ximlen * yimlen];

		lastPassTime = System.currentTimeMillis();
		lastRunTime = 0;
	}

	private void doCalculation(double xf, double yf, int xi, int yi) {
		double a, b, aa, bb;
		float hue, huesat, new_gamma;
		double[] iter;

		a = 2 * (xf - 0.5) * zoom + xcen;
		b = 2 * (yf - 0.5) * zoom + ycen;

		aa = a;
		bb = b;
		iter = NewtonIterate(aa, bb, alg);

		if (mandelbrotAddition) {
			// get Color based on angle of z

			// double angle = (float)Math.atan(bb/aa);
			double angle = (float) Math.atan(iter[2] / iter[3]);
			angle = angle + (Math.PI / 2);
			angle = angle / Math.PI;
			hue = (float) angle;
		} else {
			// limit the colors to 30% of the spectrum, unless a complex root
			float limitfactor = 0.3f;
			if (img_order != 0)
				limitfactor = 1.0f;
			hue = initColor + (float) (iter[1] * limitfactor) / roots.size();
		}

		// normal shading
		huesat = (float) iter[0] / (depth / brightness);
		if (huesat >= 1.0f)
			huesat = 0.9999f;

		huesat = (iter[0] == depth) ? 1 : huesat * 16000000;
		if (huesat > 16000000) {
			huesat = Math.abs(16000000 + (16000000 - huesat));
		}
		huesat = ((int) huesat % 16000000) / 16000000.0f;

		new_gamma = (float) Math.pow(huesat, intensity);

		int[] rgb = MPGlobal.HSBtoRGB(hue, 1.0f - huesat, new_gamma);
		addPixel(xi, yi, rgb);

		// rough painting
		if (runtotal < pixels.length * 2) {
			offscreenGraphics.setColor(new Color(pixels[yi * ximlen + xi]));
			offscreenGraphics.fillRect(xi, yi, (int) quad, (int) quad);
		}
	}

	private synchronized void addPixel(int xi, int yi, int[] rgb) {
		int red, green, blue;
		if (quad < 5) {
			// once roots are calculated, start recording pixel data
			img_red[xi][yi] += rgb[0];
			img_green[xi][yi] += rgb[1];
			img_blue[xi][yi] += rgb[2];
			img_alpha[xi][yi]++;

			red = img_red[xi][yi] / img_alpha[xi][yi];
			green = img_green[xi][yi] / img_alpha[xi][yi];
			blue = img_blue[xi][yi] / img_alpha[xi][yi];
		} else {
			// rough colors
			red = rgb[0];
			green = rgb[1];
			blue = rgb[2];
		}

		red = red << 16;
		green = green << 8;
		pixels[yi * ximlen + xi] = 0xff000000 | (red & 0xffff0000)
				| (green & 0x0000ff00) | (blue & 0xff);
	}

	private void nextpoints(int alg) {
		for (int i = 0; i < points; i++) {
			double xf = Math.random();
			double yf = Math.random();
			int xi = (int) Math.floor(ximlen * xf);
			int yi = (int) Math.floor(yimlen * yf);
			doCalculation(xf, yf, xi, yi);
		}

		runtotal += points;
		if (quad < 1.001) {
			quad = 1;
		} else {
			quad = (float) (50 * Math.sqrt((float) points / runtotal));
		}

		if (runtotal % pixels.length == 0) {
			if (runtotal == pixels.length * 2)
				fixHoles = true;

			long diff = (System.currentTimeMillis() - lastPassTime) / 1000;

			String strStatus = "Pass: " + (runtotal / pixels.length) + " - "
					+ diff + "s";
			System.out.println(strStatus);

			lastPassTime = System.currentTimeMillis();
		}

	}

	public double addRoot(Complex root) {
		ListIterator<Complex> i = (ListIterator<Complex>) roots.iterator();
		double colorFactor = 10 / rootBoundry;
		while (i.hasNext()) {
			int index = i.nextIndex();
			Complex c = (Complex) i.next();
			if (distance(root, c) < rootBoundry) {
				double retVal = index + colorFactor * distance(root, c);
				return Math.min(roots.size() - 1, retVal);
			}
		}

		if (roots.size() < 20 && quad > 4) {
			roots.add(root);
			System.out.println("add root:" + roots.size());
		}
		return roots.size() - 1;
	}

	public double distance(Complex a, Complex b) {
		return Math.sqrt((a.re - b.re) * (a.re - b.re) + (a.im - b.im)
				* (a.im - b.im));
	}

	// z^n - 1 / z
	public Complex DivZFunction(Complex z, double i, double j) {
		Complex exponent = new Complex(order, img_order);
		return (z.pow(exponent)).sub(one.div(z));
	}

	// n*z^(n-1) + z^(-2)
	public Complex DivZDerivative(Complex z, double i, double j) {
		Complex minusTwo = new Complex(-2, 0);
		Complex exponent = new Complex(order, img_order);
		Complex exponentLessOne = exponent.sub(oneError);
		return (exponent.mul(z.pow(exponentLessOne))).add(z.pow(minusTwo));
	}

	// z^10 + 0.2 i * z^5 - 1.
	public Complex Poly2Function(Complex z, double i, double j) {
		Complex exponent = new Complex(order, img_order);
		Complex point2i = new Complex(0, 0.2);
		Complex ten = new Complex(10, 0);
		return (z.pow(ten)).add(point2i.mul(z.pow(exponent))).sub(one);
	}

	// 10z^9 + 0.2i*5*z^4
	public Complex Poly2Derivative(Complex z, double i, double j) {
		Complex exponent = new Complex(order, img_order);
		Complex exponentLessOne = exponent.sub(oneError);
		Complex ten = new Complex(10, 0);
		Complex nine = new Complex(9, 0);
		Complex point2i = new Complex(0, 0.2);

		return (ten.mul(z.pow(nine))).add(point2i.mul(exponent).mul(
				z.pow(exponentLessOne)));
	}

	// 2z^3 - c + 1
	public Complex PolyMFunction(Complex z, double i, double j) {
		Complex c = new Complex(i, j);
		Complex exponent = new Complex(order, img_order);
		return two.mul(z.pow(exponent)).sub(c).add(one);
	}

	// 6z^2 - 1
	public Complex PolyMDerivative(Complex z, double i, double j) {
		Complex exponent = new Complex(order, img_order);
		Complex exponentLessOne = exponent.sub(oneError);
		return two.mul(exponent).mul(z.pow(exponentLessOne)).sub(one);
	}

	// z^c - z + 0.1
	public Complex Poly3Function(Complex z, double i, double j) {
		Complex c = new Complex(1 + order, img_order);
		return z.pow(c).sub(z).add(pointone);
	}

	// c*z^(c-1) - 1
	public Complex Poly3Derivative(Complex z, double i, double j) {
		Complex cminus1 = new Complex(1 + order - 1, (img_order == 0) ? 0
				: img_order - 1);
		Complex c = new Complex(1 + order, img_order);
		return c.mul(z.pow(cminus1)).sub(one);
	}

	// z^n - 3z^5 + 6z^3 - 3z + 3
	public Complex PolyFunction(Complex z, double i, double j) {
		Complex exponent = new Complex(order, img_order);
		Complex three = new Complex(3, 0);
		Complex six = new Complex(6, 0);
		Complex five = new Complex(5, 0);

		return (z.pow(exponent)).sub(three.mul(z.pow(five)))
				.add(six.mul(z.pow(three))).sub(three.mul(z)).add(three);
	}

	public Complex PolyDerivative(Complex z, double i, double j) {
		Complex exponent = new Complex(order, img_order);
		Complex exponentLessOne = exponent.sub(oneError);
		Complex three = new Complex(3, 0);
		Complex eighteen = new Complex(18, 0);
		Complex fifteen = new Complex(15, 0);
		Complex four = new Complex(4, 0);
		return (exponent.mul(z.pow(exponentLessOne)))
				.sub(fifteen.mul(z.pow(four))).add(eighteen.mul(z.pow(two)))
				.sub(three);
	}

	// z^n - 1 = 0
	public Complex UnityFunction(Complex z, double i, double j) {
		Complex exponent = new Complex(order, img_order);
		return z.pow(exponent).sub(one);
	}

	public Complex UnityDerivative(Complex z, double i, double j) {
		Complex exponent = new Complex(order, img_order);
		Complex exponentLessOne = exponent.sub(oneError);
		return exponent.mul(z.pow(exponentLessOne));
	}

	// z^z - cz
	public Complex ZZFunction(Complex z, double i, double j) {
		Complex con = new Complex(order, img_order);
		return (z.pow(z)).sub(con.mul(z));
	}

	// z^z * (1 + lnz) - c
	public Complex ZZDerivative(Complex z, double i, double j) {
		Complex con = new Complex(order, img_order);
		Complex lnz = z.log().add(one);
		return ((z.pow(z)).mul(lnz)).sub(con);
	}

	public Complex Newt(Complex z, double i, double j, int mode) {
		if (mode == 1) {
			return z.sub(UnityFunction(z, i, j).div(UnityDerivative(z, i, j)));
		} else if (mode == 2) {
			return z.sub(Poly2Function(z, i, j).div(Poly2Derivative(z, i, j)));
		} else if (mode == 3) {
			return z.sub(DivZFunction(z, i, j).div(DivZDerivative(z, i, j)));
		} else if (mode == 4) {
			return z.sub(Poly3Function(z, i, j).div(Poly3Derivative(z, i, j)));
		} else if (mode == 5) {
			return z.sub(PolyMFunction(z, i, j).div(PolyMDerivative(z, i, j)));
		} else if (mode == 6) {
			return z.sub(ZZFunction(z, i, j).div(ZZDerivative(z, i, j)));
		} else {
			return z.sub(PolyFunction(z, i, j).div(PolyDerivative(z, i, j)));
		}
	}

	public double[] NewtonIterate(double i, double j, int mode) {
		int n = 0;
		Complex z = new Complex(i, j);
		Complex old = new Complex(i, j);

		z = Newt(z, i, j, mode);

		double hue = 0;
		double w = 0;
		while (n < depth && distance(old, z) > rootBoundry) {
			old = z;

			z = Newt(z, i, j, mode);

			if (mandelbrotAddition)
				z = z.add(new Complex(i / 2, j / 2));

			n++;

			// normal smoothing
			w = 1.0 / z.sub(old).abs();
			hue += Math.pow(1.05, -w);
		}

		double[] vals = new double[4];
		if (n != depth) {
			vals[0] = hue;
			vals[1] = addRoot(z);
		} else {
			vals[0] = n;
			vals[1] = 0;
		}

		vals[2] = z.re;
		vals[3] = z.im;

		return vals;
	}

	/*private void setConstants(Double x, Double y, Float inten, Float brig,
			Double zoom, int depth, Float i_o, Float ord, Double comp_err) {
		xcen = x;
		ycen = y;
		intensity = inten;
		brightness = brig;
		this.zoom = zoom;
		this.depth = depth;

		img_order = i_o;
		order = ord;
		complex_error = comp_err;
	}*/
}