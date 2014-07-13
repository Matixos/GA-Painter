package pl.com.mat.painter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class ViewManager extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private static final int x = 1100, y = 600;
	
	private MainViewPanel mPanel;
	private JButton confirm;
	private JButton repaint;
	
	private Border bord = BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK);

	public ViewManager() {
		super("Artificial Painter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(x, y);
		setLocationRelativeTo(null);
		setResizable(false); 
		
		mPanel = new MainViewPanel();
		mPanel.setBorder(bord);  
		
		confirm = new JButton("Confirm");
		JPanel bottom = new JPanel();
		bottom.setBackground(Color.WHITE);
		bottom.add(confirm);
		
		repaint = new JButton("Repaint");
		bottom.add(repaint);
		
		getContentPane().add(BorderLayout.CENTER, mPanel);
		getContentPane().add(BorderLayout.SOUTH, bottom);
		
		setVisible(true); 
	}
	
	public void addListener(ActionListener aListener) {
		confirm.setActionCommand("confirm");
		confirm.addActionListener(aListener);
		
		repaint.setActionCommand("repaint");
		repaint.addActionListener(aListener);
	}
	
	public Graphics2D[] getGraphicsFromPaintings() {
		return mPanel.getGraphicsFromPaintings();
	}
	
	public int[] getPaintingsQualities() {
		return mPanel.getPaintingsQualities();
	}
	
	public void disableRepaintButton() {
		repaint.setVisible(false);
	}

}