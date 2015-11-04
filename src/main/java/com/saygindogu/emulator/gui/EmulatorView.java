package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.Emulator;

import javax.swing.*;
import java.awt.*;


public class EmulatorView extends JFrame {
	
	private MainPanel mainPanel;
	private Emulator emulator;
	private double width;
	private double height;
	
	public EmulatorView( Emulator emulator){
		this.emulator = emulator;
		Toolkit tk = Toolkit.getDefaultToolkit();  
		width  = tk.getScreenSize().getWidth() * 0.5;  
		height = tk.getScreenSize().getHeight() * 0.5 ;
		
		mainPanel = new MainPanel( emulator, width, height);
		add(mainPanel);
		pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	public void updateView(Emulator emulator) {
		setEmulator( emulator);
		mainPanel.updateView();
		invalidate();
		validate();
		repaint();
	}

	public void setEmulator(Emulator emulator) {
		this.emulator = emulator;
		mainPanel.setEmulator( emulator);
		
	}

	public JTextArea getTextArea() {
		return mainPanel.getTextArea();
	}

	public void setAddressRepMode(int mode) {
		mainPanel.setAddressRepMode( mode);
	}

	public void setValRepMode(int mode) {
		mainPanel.setValRepMode( mode);
	}

}
