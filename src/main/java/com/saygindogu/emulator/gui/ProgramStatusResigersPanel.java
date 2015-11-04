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
	private double width;
	private double height;
	
	private JLabel OFNameLabel;
	private JLabel DFNameLabel;
	private JLabel SFNameLabel;
	private JLabel ZFNameLabel;
	private JLabel PFNameLabel;
	private JLabel CFNameLabel;

	private Component rigidArea;
	
	JLabel IPValueLabel;
	JLabel IPNameLabel;
	
	private EmuWord IP;
	private int[] flagValues;
	private JLabel[] flagLabels;
	private JPanel flagNamePanel;
	private int representationMode;
	
	public ProgramStatusResigersPanel(Emulator emulator, double w, double h) {
		this.emulator = emulator;
		representationMode = GUIConstants.HEX_MODE;
		width = w;
		height = h;
		
		leftPanel = new JPanel();
		rightPanel = new JPanel();
		flagPanel = new JPanel();
		flagNamePanel = new JPanel();
		
		flagNamePanel.add( new JLabel( GUIConstants.FLAG_STRING));
		
		flagPanel.setLayout( new GridLayout( 2, 16, 3, 3));
		
		OFNameLabel = new JLabel( "OF");
		DFNameLabel = new JLabel( "DF");
		SFNameLabel = new JLabel( "SF");
		ZFNameLabel = new JLabel( "ZF");
		PFNameLabel = new JLabel( "PF");
		CFNameLabel = new JLabel( "CF");
		IPNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_IP]);
		
		IPValueLabel = new JLabel();
		
		flagValues = new int[16];
		flagLabels = new JLabel[16];
		for( int i = 0; i < flagLabels.length; i++){
			flagLabels[i] = new JLabel();
			flagValues[i] = 0;
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
		}
		
		IP = new EmuWord( (short) emulator.getProcessor().getRegisterValue( new RegisterType("IP")));
		IP.setRepresentationMode(representationMode);
		IPValueLabel.setText( IP.toString() );
		
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
