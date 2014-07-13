package pl.com.mat.painter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

public class MainViewPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final int x = 1100, y = 200;
	
	private Painting[] paintings = new Painting[8];
	private Graphics2D[] graphics = new Graphics2D[8];
	private JSlider[] sliders = new JSlider[8];
	
	public MainViewPanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.WHITE);
		
		for (int i = 0; i < paintings.length; i++)
			paintings[i] = new Painting();
		
		for(int i = 0; i< graphics.length; i++)
			graphics[i] = paintings[i].createGraphics(); 

		for (int i = 0; i < sliders.length; i++) {
			sliders[i] = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
			sliders[i].setBackground(Color.WHITE);
			sliders[i].setMajorTickSpacing(1);
			sliders[i].setPaintTicks(true);
			sliders[i].setPaintLabels(true);
		}
		
		add(Box.createRigidArea(new Dimension(x, y)));

		JPanel pan2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 0));
		pan2.setBackground(Color.WHITE);
		for (int i = 0; i < 4; i++)
			pan2.add(sliders[i]);
		add(pan2);

		add(Box.createRigidArea(new Dimension(x, y)));

		JPanel pan4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 0));
		pan4.setBackground(Color.WHITE);
		for (int i = 4; i < sliders.length; i++)
			pan4.add(sliders[i]);
		
		add(pan4);
	}
	
	public void addListener(ChangeListener listener) {
		for(int i = 0 ; i < sliders.length; i++) {
			sliders[i].setName("s" + (i+1));
			sliders[i].addChangeListener(listener);
		}
	}
	
	public Graphics2D[] getGraphicsFromPaintings() {
		return graphics;
	}
	
	public int[] getPaintingsQualities() {
		int[] result = new int[8];
		
		for(int i=0; i< sliders.length; i++)
			result[i] = sliders[i].getValue();
		
		return result;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		
		for(int i = 0; i< 4; i++)
			g2d.drawImage(paintings[i], i*260 + 30, 10, null);
		
		for(int i = 4; i< paintings.length; i++)
			g2d.drawImage(paintings[i], (i-4)*260 + 30, 270, null);
	}

}