package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.RegisterType;
import com.saygindogu.emulator.Emulator;
import com.saygindogu.emulator.RegisterConstants;

import javax.swing.*;
import java.awt.*;

public class GeneralPurposeRegistersPanel extends JPanel {

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

	private JPanel bottomPanel;
	private JPanel namePanel;

	private Emulator emulator;

	private int valueRepresentationMode;
	private int[] prevValues = new int[12];

	public GeneralPurposeRegistersPanel( Emulator emulator, double w, double h){
		valueRepresentationMode = GUIConstants.HEX_MODE;
		this.emulator = emulator;

		var AXNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[2] );
		var BXNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[5]);
		var CXNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[8]);
		var DXNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[11]);

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
		
		var highLabel = new JLabel(GUIConstants.HIGH_STRING);
		var lowLabel = new JLabel(GUIConstants.LOW_STRING);
		
		updateView();
	
		var nameLabel = new JLabel( GUIConstants.GENERAL_PURPOSE_REGISTERS_STRING);
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
		namePanel.setBackground( GUIConstants.LABEL_BACKGROUND);
		bottomPanel.setBackground(GUIConstants.MEMORY_BACKGROUND);
		setBackground( GUIConstants.LABEL_BACKGROUND);
	}

	public void updateView() {
		var pro = emulator.getProcessor();

		int[] curValues = new int[] {
			pro.getRegisterValue( new RegisterType( "AX" )),
			pro.getRegisterValue( new RegisterType( "BX" )),
			pro.getRegisterValue( new RegisterType( "CX" )),
			pro.getRegisterValue( new RegisterType( "DX" )),
			pro.getRegisterValue( new RegisterType( "AH" )),
			pro.getRegisterValue( new RegisterType( "BH" )),
			pro.getRegisterValue( new RegisterType( "CH" )),
			pro.getRegisterValue( new RegisterType( "DH" )),
			pro.getRegisterValue( new RegisterType( "AL" )),
			pro.getRegisterValue( new RegisterType( "BL" )),
			pro.getRegisterValue( new RegisterType( "CL" )),
			pro.getRegisterValue( new RegisterType( "DL" )),
		};

		JLabel[] labels = { AXValueLabel, BXValueLabel, CXValueLabel, DXValueLabel,
			AHValueLabel, BHValueLabel, CHValueLabel, DHValueLabel,
			ALValueLabel, BLValueLabel, CLValueLabel, DLValueLabel };

		for (int i = 0; i < labels.length; i++) {
			labels[i].setOpaque( true );
			labels[i].setBackground( curValues[i] != prevValues[i] ? GUIConstants.CHANGED_VALUE_COLOR : GUIConstants.MEMORY_BACKGROUND );
		}

		var AX = new EmuWord( (short) curValues[0]);
		var BX = new EmuWord( (short) curValues[1]);
		var CX = new EmuWord( (short) curValues[2]);
		var DX = new EmuWord( (short) curValues[3]);
		AX.setRepresentationMode( valueRepresentationMode);
		BX.setRepresentationMode( valueRepresentationMode);
		CX.setRepresentationMode( valueRepresentationMode);
		DX.setRepresentationMode( valueRepresentationMode);

		var AH = new EmuByte( (byte) curValues[4]);
		var BH = new EmuByte( (byte) curValues[5]);
		var CH = new EmuByte( (byte) curValues[6]);
		var DH = new EmuByte( (byte) curValues[7]);
		AH.setRepresentationMode( valueRepresentationMode);
		BH.setRepresentationMode( valueRepresentationMode);
		CH.setRepresentationMode( valueRepresentationMode);
		DH.setRepresentationMode( valueRepresentationMode);

		var AL = new EmuByte( (byte) curValues[8]);
		var BL = new EmuByte( (byte) curValues[9]);
		var CL = new EmuByte( (byte) curValues[10]);
		var DL = new EmuByte( (byte) curValues[11]);
		AL.setRepresentationMode( valueRepresentationMode);
		BL.setRepresentationMode( valueRepresentationMode);
		CL.setRepresentationMode( valueRepresentationMode);
		DL.setRepresentationMode( valueRepresentationMode);

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

		System.arraycopy(curValues, 0, prevValues, 0, curValues.length);
	}

	public void setValRepMode(int mode) {
		valueRepresentationMode = mode;
		
	}



}
