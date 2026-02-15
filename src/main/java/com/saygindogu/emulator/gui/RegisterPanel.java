package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.Emulator;

import javax.swing.*;
import java.awt.*;

public class RegisterPanel extends JPanel {
	
	private GeneralPurposeRegistersPanel general;
	private SegmentRegisterPanel segment;
	private PtrIndexRegistersPanel ptrIndexregisterPanel;

	public RegisterPanel(Emulator emulator, double w, double h){
		general = new GeneralPurposeRegistersPanel(emulator, w, h );
		segment = new SegmentRegisterPanel(emulator, w, h);

		var leftPanel = new JPanel();
		var rightPanel = new JPanel();

		ptrIndexregisterPanel = new PtrIndexRegistersPanel( emulator, w, h);

		leftPanel.setLayout( new GridLayout( 1, 2));
		leftPanel.add( general);
		leftPanel.add( segment);

		rightPanel.setLayout( new BorderLayout());
		rightPanel.add( ptrIndexregisterPanel);

		setBackground( Color.red);
		setLayout( new GridLayout(1,2) );
		add( leftPanel);
		add( rightPanel);
		setPreferredSize( new Dimension( (int)(w * GUIConstants.REGISTER_PANEL_FRACTION_W), (int)(h * GUIConstants.REGISTER_PANEL_FRACTION_H)));
	}

	public void setEmulator(Emulator emulator) {
	}

	public void updateView() {
		general.updateView();
		segment.updateView();
		ptrIndexregisterPanel.updateView();
		
	}


	public void setValRepMode(int mode) {
		general.setValRepMode(mode);
		segment.setValRepMode(mode);
		ptrIndexregisterPanel.setValRepMode(mode);
		
	}
}
