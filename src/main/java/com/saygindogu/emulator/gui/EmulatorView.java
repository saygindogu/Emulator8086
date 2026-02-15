package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.Emulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class EmulatorView extends JFrame {
	
	private MainPanel mainPanel;
	private Emulator emulator;

	public EmulatorView( Emulator emulator){
		this.emulator = emulator;
		Toolkit tk = Toolkit.getDefaultToolkit();
		var width  = tk.getScreenSize().getWidth() * 0.5;
		var height = tk.getScreenSize().getHeight() * 0.5 ;
		
		mainPanel = new MainPanel( emulator, width, height);
		add(mainPanel);
		pack();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
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
