package pl.com.mat.painter;

import java.awt.Graphics2D;

import javax.swing.JFrame;

import pl.com.mat.painter.fractals.Taylor;


public class PaintingsSupplier implements Runnable {
	
	private final long DRAWING_TIME = 17000;
	private Thread current;
	
	private Graphics2D[] graph;
	private Genom[] genoms;
	private JFrame parent;
	
	private Taylor[] taylorFractals;

	public PaintingsSupplier(Graphics2D[] graph, JFrame parent) {
		this.graph = graph;
		this.parent = parent;
		
		this.taylorFractals = new Taylor[8];
	}
	
	public void setGenoms(Genom[] genoms) {
		this.genoms = genoms;
	}
	
	public void randomizePaintings() {
		for (int i = 0; i<taylorFractals.length; i++) {
			taylorFractals[i] = new Taylor(graph[i], parent);
			taylorFractals[i].setConstants(genoms[i]);
			taylorFractals[i].start();
		}
	}
	
	public void start() {
		current = new Thread(this);
		current.start();
	}

	@Override
	public void run() {
		randomizePaintings();
		
		try {
			Thread.sleep(DRAWING_TIME);
		} catch (InterruptedException e) {}
		
		for (int i = 0; i<taylorFractals.length; i++) {
			taylorFractals[i].stop();
		}
		
		current = null;
	}

}