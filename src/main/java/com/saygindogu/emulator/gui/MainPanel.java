package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.Emulator;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
	TextPanel textPanel;
	MemoryPanel memoryPanel;
	RegisterPanel registerPanel;
	private Emulator emulator;

	public MainPanel(Emulator emulator, double w, double h){
		this.emulator = emulator;
		var menupanel = new MenuPanel( emulator, w, h);
		textPanel = new TextPanel( emulator, w, h);
		memoryPanel = new MemoryPanel( emulator, w, h);
		registerPanel = new RegisterPanel(emulator, w, h);

		var textMemorySplitter = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, textPanel, memoryPanel);
		var middleBottomSplitter = new JSplitPane( JSplitPane.VERTICAL_SPLIT, textMemorySplitter, registerPanel);

		setLayout( new BorderLayout());
		add( menupanel, BorderLayout.NORTH);
		add(middleBottomSplitter);
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
