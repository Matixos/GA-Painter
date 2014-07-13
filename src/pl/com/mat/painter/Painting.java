package pl.com.mat.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Painting extends BufferedImage {
	
	private static final int x = 250, y = 180;
	private Graphics2D graph;
	
	public Painting() {
		super(x, y, BufferedImage.TYPE_INT_RGB);
		this.graph = createGraphics();
		
		this.graph.setColor(Color.WHITE);
		this.graph.fillRect(0, 0, x, y);
		
		this.graph.setColor(Color.BLACK);
		this.graph.drawRect(0, 0, x-1, y-1);
	}
	
	public Graphics2D getGraph() {
		return graph;
	}

}