package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.RegisterType;
import com.saygindogu.emulator.Emulator;
import com.saygindogu.emulator.Processor;
import com.saygindogu.emulator.RegisterConstants;

import javax.swing.*;
import java.awt.*;

public class GeneralPurposeRegistersPanel extends JPanel {
	private JPanel leftPanel;
	private JPanel rightPanel;

	private JLabel AXNameLabel;
	private JLabel BXNameLabel;
	private JLabel CXNameLabel;
	private JLabel DXNameLabel;

	private JLabel highLabel;
	private JLabel lowLabel;
	
	private JLabel AXValueLabel;
	private JLabel BXValueLabel;
	private JLabel CXValueLabel;
	private JLabel DXValueLabel;

	private JLabel AHValueLabel;
	private JLabel BHValueLabel;
	private JLabel CHValueLabel;
	private JLabel DHValueLabel;

	private JLabel ALValueLabel;
	private JLabel BLValueLabel;
	private JLabel CLValueLabel;
	private JLabel DLValueLabel;
	
	private EmuWord AX;
	private EmuWord BX;
	private EmuWord CX;
	private EmuWord DX;
	
	private EmuByte AL;
	private EmuByte BL;
	private EmuByte CL;
	private EmuByte DL;
	
	private EmuByte AH;
	private EmuByte BH;
	private EmuByte CH;
	private EmuByte DH;
	
	private JPanel bottomPanel;
	private JPanel namePanel;
	
	private Emulator emulator;
	public Emulator getEmulator() {
		return emulator;
	}

	public void setEmulator(Emulator emulator) {
		this.emulator = emulator;
	}

	public int getRegisterValueRepresentationMode() {
		return registerValueRepresentationMode;
	}

	public void setRegisterValueRepresentationMode(
			int registerValueRepresentationMode) {
		this.registerValueRepresentationMode = registerValueRepresentationMode;
	}

	private int registerValueRepresentationMode;
	private double width;
	private double height;
	private JLabel nameLabel;
	private int valueRepresentationMode;
	
	public GeneralPurposeRegistersPanel( Emulator emulator, double w, double h){
		valueRepresentationMode = GUIConstants.HEX_MODE;
		
		width = w;
		height = h;
		this.emulator = emulator;
		
		leftPanel = new JPanel();
		rightPanel = new JPanel();

		AXNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[2] );
		BXNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[5]);
		CXNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[8]);
		DXNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[11]);

		AXValueLabel = new JLabel();
		BXValueLabel = new JLabel();
		CXValueLabel = new JLabel();
		DXValueLabel = new JLabel();

		AHValueLabel = new JLabel();
		BHValueLabel = new JLabel();
		CHValueLabel = new JLabel();
		DHValueLabel = new JLabel();

		ALValueLabel = new JLabel();
		BLValueLabel = new JLabel();
		CLValueLabel = new JLabel();
		DLValueLabel = new JLabel();
		
		highLabel = new JLabel(GUIConstants.HIGH_STRING);
		lowLabel = new JLabel(GUIConstants.LOW_STRING);
		
		updateView();
	
		nameLabel = new JLabel( GUIConstants.GENERAL_PURPOSE_REGISTERS_STRING);
		nameLabel.setHorizontalAlignment( JLabel.CENTER );
		namePanel = new JPanel();
		namePanel.add( nameLabel);
		bottomPanel = new JPanel();
		
		bottomPanel.setLayout( new GridLayout(5,4));
		bottomPanel.add( Box.createRigidArea( new Dimension()) );
		bottomPanel.add( Box.createRigidArea( new Dimension()) );
		bottomPanel.add( highLabel);
		bottomPanel.add( lowLabel );
		
		bottomPanel.add( AXValueLabel);
		bottomPanel.add( AXNameLabel );
		bottomPanel.add( AHValueLabel );
		bottomPanel.add( ALValueLabel );
		
		bottomPanel.add( BXValueLabel);
		bottomPanel.add( BXNameLabel );
		bottomPanel.add( BHValueLabel );
		bottomPanel.add( BLValueLabel );
		
		bottomPanel.add( CXValueLabel);
		bottomPanel.add( CXNameLabel );
		bottomPanel.add( CHValueLabel );
		bottomPanel.add( CLValueLabel );
		
		bottomPanel.add( DXValueLabel);
		bottomPanel.add( DXNameLabel );
		bottomPanel.add( DHValueLabel );
		bottomPanel.add( DLValueLabel );
		
		setLayout( new BoxLayout(this,BoxLayout.Y_AXIS));
		add( namePanel);
		add( bottomPanel);
		
		handleBackGrounds();
	}

	private void handleBackGrounds() {
		leftPanel.setBackground( GUIConstants.MEMORY_BACKGROUND);
		rightPanel.setBackground( GUIConstants.MEMORY_BACKGROUND);
		namePanel.setBackground( GUIConstants.LABEL_BACKGROUND);	
		bottomPanel.setBackground(GUIConstants.MEMORY_BACKGROUND);
		setBackground( GUIConstants.LABEL_BACKGROUND);	
	}

	public void updateView() {
		Processor pro = emulator.getProcessor();
		
		AX = new EmuWord( (short) pro.getRegisterValue( new RegisterType( "AX" )));
		BX = new EmuWord( (short) pro.getRegisterValue( new RegisterType( "BX" )));
		CX = new EmuWord( (short) pro.getRegisterValue( new RegisterType( "CX" )));
		DX = new EmuWord( (short) pro.getRegisterValue( new RegisterType( "DX" )));
		AX.setRepresentationMode( valueRepresentationMode);
		BX.setRepresentationMode( valueRepresentationMode);
		CX.setRepresentationMode( valueRepresentationMode);
		DX.setRepresentationMode( valueRepresentationMode);
		
		AL = new EmuByte( (byte) pro.getRegisterValue( new RegisterType( "AL" )));
		BL = new EmuByte( (byte) pro.getRegisterValue( new RegisterType( "BL" )));
		CL = new EmuByte( (byte) pro.getRegisterValue( new RegisterType( "CL" )));
		DL = new EmuByte( (byte) pro.getRegisterValue( new RegisterType( "DL" )));
		AL.setRepresentationMode( valueRepresentationMode);
		BL.setRepresentationMode( valueRepresentationMode);
		CL.setRepresentationMode( valueRepresentationMode);
		DL.setRepresentationMode( valueRepresentationMode);
		
		AH = new EmuByte( (byte) pro.getRegisterValue( new RegisterType( "AH" )));
		BH = new EmuByte( (byte) pro.getRegisterValue( new RegisterType( "BH" )));
		CH = new EmuByte( (byte) pro.getRegisterValue( new RegisterType( "CH" )));
		DH = new EmuByte( (byte) pro.getRegisterValue( new RegisterType( "DH" )));
		AH.setRepresentationMode( valueRepresentationMode);
		BH.setRepresentationMode( valueRepresentationMode);
		CH.setRepresentationMode( valueRepresentationMode);
		DH.setRepresentationMode( valueRepresentationMode);
		
		AXValueLabel.setText( AX.toString() );
		BXValueLabel.setText( BX.toString() );
		CXValueLabel.setText( CX.toString() );
		DXValueLabel.setText( DX.toString() );
		
		AHValueLabel.setText( AH.toString() );
		BHValueLabel.setText( BH.toString() );
		CHValueLabel.setText( CH.toString() );
		DHValueLabel.setText( DH.toString() );
		
		ALValueLabel.setText( AL.toString() );
		BLValueLabel.setText( BL.toString() );
		CLValueLabel.setText( CL.toString() );
		DLValueLabel.setText( DL.toString() );		
		
	}

	public void setValRepMode(int mode) {
		valueRepresentationMode = mode;
		
	}



}
