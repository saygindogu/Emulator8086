package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.Emulator;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
	JPanel bottomPanel;
	JSplitPane text_memory_splitter;
	JSplitPane middle_Bottom_splitter;
	MenuPanel menupanel;
	JPanel mergedPanel;
	JPanel middlePanel;
	TextPanel textPanel;
	MemoryPanel memoryPanel;
	RegisterPanel registerPanel;
	private double width;
	private double height;
	private Emulator emulator;
	
	public MainPanel(Emulator emulator, double w, double h){
		width = w;
		height = h;
		this.emulator = emulator;
		mergedPanel = new JPanel();
		bottomPanel = new JPanel();
		menupanel= new MenuPanel( emulator, width, height);
		middlePanel = new JPanel();
		textPanel = new TextPanel( emulator, width, height);
		memoryPanel = new MemoryPanel( emulator, width, height);
		registerPanel = new RegisterPanel(emulator, width, height);
		
		text_memory_splitter = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, textPanel, memoryPanel);
		
		middle_Bottom_splitter = new JSplitPane( JSplitPane.VERTICAL_SPLIT, text_memory_splitter, registerPanel);
		
		setLayout( new BorderLayout());
		add( menupanel, BorderLayout.NORTH);
		add(middle_Bottom_splitter);
	}

	public void setEmulator(Emulator emulator) {
		this.emulator = emulator;
		textPanel.setEmulator( emulator);
		registerPanel.setEmulator( emulator);
		
	}

	public void updateView() {
		textPanel.updateView();
		registerPanel.updateView();
		memoryPanel.updateView();
		
	}

	public JTextArea getTextArea() {
		return textPanel.getTextArea();
	}

	public void setAddressRepMode(int mode) {
		memoryPanel.setAddressRepMode( mode);
		
	}

	public void setValRepMode(int mode) {
		registerPanel.setValRepMode( mode);
		memoryPanel.setValRepMode( mode);
		
	}
}
