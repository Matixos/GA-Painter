package pl.com.mat.painter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

public class Controller implements ActionListener {

	private ViewManager win;
	private GeneticUtils gen;
	private PaintingsSupplier pSupplier;
	private Genom[] genoms;
	
	@Override
	public void actionPerformed(ActionEvent source) {
		if(source.getActionCommand().equals("confirm")) {
			gen.enrichPopulationWithQuality(genoms, win.getPaintingsQualities());
			//win.disableRepaintButton();
			genoms = gen.geneticAlgorithm(genoms);
			generatePaintings();
			
		} else {
			this.genoms = gen.gerenatePopulation();
			generatePaintings();
		}
	}
	
	public Controller(ViewManager win) {
		this.win = win;
		this.win.addListener(this);
		this.gen = new GeneticUtils(0.9, 0.15);
		
		pSupplier = new PaintingsSupplier(win.getGraphicsFromPaintings(), win);
		this.genoms = gen.gerenatePopulation();
		generatePaintings();
	}
	
	private void generatePaintings() {
		pSupplier.setGenoms(this.genoms);
		pSupplier.start();
		
		JOptionPane.showMessageDialog(win,
				"Give a while to draw nice paintings",
				"Info",
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static void main(String[] args) {
		new Controller(new ViewManager());
	}
}