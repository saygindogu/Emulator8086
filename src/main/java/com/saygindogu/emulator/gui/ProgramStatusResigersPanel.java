package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.RegisterType;
import com.saygindogu.emulator.Emulator;
import com.saygindogu.emulator.RegisterConstants;

import javax.swing.*;
import java.awt.*;

public class ProgramStatusResigersPanel extends JPanel {
	
	Emulator emulator;
	private JPanel leftPanel;
	private JPanel rightPanel;
	private JPanel flagPanel;

	JLabel IPValueLabel;
	JLabel IPNameLabel;

	private int[] flagValues;
	private JLabel[] flagLabels;
	private JPanel flagNamePanel;
	private int representationMode;
	private int prevIP;
	private int[] prevFlagValues;
	
	public ProgramStatusResigersPanel(Emulator emulator, double w, double h) {
		this.emulator = emulator;
		representationMode = GUIConstants.HEX_MODE;
		
		leftPanel = new JPanel();
		rightPanel = new JPanel();
		flagPanel = new JPanel();
		flagNamePanel = new JPanel();
		
		flagNamePanel.add( new JLabel( GUIConstants.FLAG_STRING));
		
		flagPanel.setLayout( new GridLayout( 2, 16, 3, 3));
		
		var OFNameLabel = new JLabel( "OF");
		var DFNameLabel = new JLabel( "DF");
		var SFNameLabel = new JLabel( "SF");
		var ZFNameLabel = new JLabel( "ZF");
		var PFNameLabel = new JLabel( "PF");
		var CFNameLabel = new JLabel( "CF");
		IPNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_IP]);
		
		IPValueLabel = new JLabel();
		
		flagValues = new int[16];
		prevFlagValues = new int[16];
		flagLabels = new JLabel[16];
		for( int i = 0; i < flagLabels.length; i++){
			flagLabels[i] = new JLabel();
			flagValues[i] = 0;
			prevFlagValues[i] = 0;
		}
		
		for( int i = 0; i < 16; i++){
			switch ( 15 - i) {
			case 11:
				flagPanel.add( OFNameLabel);
				break;
			case 6:
				flagPanel.add( ZFNameLabel);
				break;
			case 7:
				flagPanel.add( SFNameLabel);
				break;
			case 0:
				flagPanel.add( CFNameLabel);
				break;
			case 10:
				flagPanel.add( DFNameLabel);
				break;
			case 2:
				flagPanel.add( PFNameLabel);
				break;
			default:
				flagPanel.add( Box.createRigidArea(new Dimension(0,0)));
				break;
			}
		}
		
		for( int i = 0; i < flagLabels.length; i++){
			flagPanel.add( flagLabels[15 - i] );
		}
		
		leftPanel.setLayout( new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.add( flagNamePanel);
		leftPanel.add( flagPanel);
		
		IPNameLabel.setHorizontalAlignment(JLabel.CENTER);
		IPValueLabel.setHorizontalAlignment(JLabel.CENTER);
		
		rightPanel.setLayout( new GridLayout(2,1,5,5) );
		rightPanel.setAlignmentX( JPanel.RIGHT_ALIGNMENT);
		rightPanel.add( IPNameLabel);
		rightPanel.add( IPValueLabel);

		updateView();
		
		setLayout( new BoxLayout(this, BoxLayout.X_AXIS));
		add( leftPanel);
		add( rightPanel);
		
		handleBackGrounds();
		
	}

	private void handleBackGrounds() {
		IPNameLabel.setBackground( GUIConstants.LABEL_BACKGROUND);
		IPNameLabel.setOpaque( true);
		flagNamePanel.setBackground( GUIConstants.LABEL_BACKGROUND);
		
		flagPanel.setBackground( GUIConstants.MEMORY_BACKGROUND);
		rightPanel.setBackground( GUIConstants.MEMORY_BACKGROUND);
		
	}

	public void updateView() {
		fillFlagValues();

		for( int i = 0; i < flagLabels.length; i++){
			flagLabels[i].setText( "" + flagValues[i] );
			flagLabels[i].setOpaque( true );
			flagLabels[i].setBackground( flagValues[i] != prevFlagValues[i] ? GUIConstants.CHANGED_VALUE_COLOR : GUIConstants.MEMORY_BACKGROUND );
		}

		int curIP = emulator.getProcessor().getRegisterValue( new RegisterType("IP"));
		var ip = new EmuWord( (short) curIP);
		ip.setRepresentationMode(representationMode);
		IPValueLabel.setText( ip.toString() );
		IPValueLabel.setOpaque( true );
		IPValueLabel.setBackground( curIP != prevIP ? GUIConstants.CHANGED_VALUE_COLOR : GUIConstants.MEMORY_BACKGROUND );

		prevIP = curIP;
		System.arraycopy(flagValues, 0, prevFlagValues, 0, flagValues.length);

	}

	private void fillFlagValues() {
		boolean[] flagRegister  = emulator.getProcessor().getFlagRegister();
		
		for( int i = 0; i < flagRegister.length; i++){
			if( flagRegister[i] ){
				flagValues[i] = 1;
			}
			else{
				flagValues[i] = 0;
			}
		}
		
	}

	public void setValRepMode(int mode) {
		representationMode = mode;
		
	}

}
