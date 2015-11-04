package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.Emulator;
import com.saygindogu.emulator.Processor;
import com.saygindogu.emulator.RegisterConstants;
import com.saygindogu.emulator.RegisterType;

import javax.swing.*;
import java.awt.*;

public class PtrIndexRegistersPanel extends JPanel {

	private JPanel topPanel;
	private ProgramStatusResigersPanel bottomPanel;
	private JPanel nameLabelPanel;
	
	private JLabel nameLabel;
	
	private JLabel SPNameLabel;
	private JLabel BPNameLabel;
	private JLabel SINameLabel;
	private JLabel DINameLabel;
	
	private JLabel SPValueLabel;
	private JLabel BPValueLabel;
	private JLabel SIValueLabel;
	private JLabel DIValueLabel;
	
	private EmuWord SP;
	private EmuWord BP;
	private EmuWord DI;
	private EmuWord SI;
	
	Emulator emulator;
	private int valueRepresentationMode;
	
	public PtrIndexRegistersPanel(Emulator emulator, double w, double h){
		valueRepresentationMode = GUIConstants.HEX_MODE;
		setBackground( Color.red);
		this.emulator = emulator;
		
		topPanel = new JPanel();
		bottomPanel = new ProgramStatusResigersPanel( emulator, w, h);
		nameLabel = new JLabel( GUIConstants.PTR_INDEX_REG_STRING );
		nameLabelPanel = new JPanel();
		
		SPValueLabel = new JLabel();
		BPValueLabel = new JLabel();
		SIValueLabel = new JLabel();
		DIValueLabel = new JLabel();
		
		SPNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_SP] );
		BPNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[RegisterConstants.NAME_INDEX_BP] );
		SINameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[RegisterConstants.NAME_INDEX_SI] );
		DINameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[RegisterConstants.NAME_INDEX_DI] );
		
		SPNameLabel.setHorizontalAlignment( JLabel.RIGHT);
		BPNameLabel.setHorizontalAlignment( JLabel.RIGHT);
		SINameLabel.setHorizontalAlignment( JLabel.RIGHT);
		DINameLabel.setHorizontalAlignment( JLabel.RIGHT);
		
		updateView();
		
		topPanel.setLayout( new GridLayout( 2, 4, GUIConstants.RIGID_AREA_1, GUIConstants.RIGID_AREA_1) );
		
		topPanel.add( SPNameLabel);
		topPanel.add( SPValueLabel);
		topPanel.add( BPNameLabel);
		topPanel.add( BPValueLabel);
		topPanel.add( SINameLabel);
		topPanel.add( SIValueLabel);
		topPanel.add( DINameLabel);
		topPanel.add( DIValueLabel);
		
		nameLabelPanel.add( nameLabel);
		
		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
		add( nameLabelPanel);
		add(topPanel);
		add(bottomPanel);
		
		handleBackGroundColors();
	}

	private void handleBackGroundColors() {
		setBackground( GUIConstants.MEMORY_BACKGROUND);
		
		nameLabel.setBackground( GUIConstants.LABEL_BACKGROUND);
		topPanel.setBackground( GUIConstants.MEMORY_BACKGROUND );
		nameLabelPanel.setBackground( GUIConstants.LABEL_BACKGROUND);
		
		SPValueLabel.setBackground( GUIConstants.MEMORY_BACKGROUND);
		BPValueLabel.setBackground( GUIConstants.MEMORY_BACKGROUND);
		SIValueLabel.setBackground( GUIConstants.MEMORY_BACKGROUND);
		DIValueLabel.setBackground( GUIConstants.MEMORY_BACKGROUND);
		
	}

	public void updateView() {
		Processor pro = emulator.getProcessor();
		
		SP = new EmuWord( (short) pro.getRegisterValue( new RegisterType("SP")));
		BP = new EmuWord( (short) pro.getRegisterValue( new RegisterType("BP")));
		SI = new EmuWord( (short) pro.getRegisterValue( new RegisterType("SI")));
		DI = new EmuWord( (short) pro.getRegisterValue( new RegisterType("DI")));
		SP.setRepresentationMode( valueRepresentationMode);
		BP.setRepresentationMode( valueRepresentationMode);
		DI.setRepresentationMode( valueRepresentationMode);
		SI.setRepresentationMode( valueRepresentationMode);
		
		SPValueLabel.setText( SP.toString());
		BPValueLabel.setText( BP.toString());
		SIValueLabel.setText( SI.toString());
		DIValueLabel.setText( DI.toString());
		
		
		bottomPanel.updateView();
		
	}

	public void setValRepMode(int mode) {
		valueRepresentationMode = mode;
		bottomPanel.setValRepMode(mode);
		
	}

}
