package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.RegisterType;
import com.saygindogu.emulator.Emulator;
import com.saygindogu.emulator.Processor;
import com.saygindogu.emulator.RegisterConstants;

import javax.swing.*;
import java.awt.*;

public class SegmentRegisterPanel extends JPanel {
	
	JLabel segmentLabel;
	JPanel gridPanel;
	JPanel namePanel;
	
	JLabel CSNameLabel;
	JLabel ESNameLabel;
	JLabel SSNameLabel;
	JLabel DSNameLabel;
	
	JLabel CSValueLabel;
	JLabel SSValueLabel;
	JLabel DSValueLabel;
	JLabel ESValueLabel;
	
	EmuWord cs;
	EmuWord ds;
	EmuWord es;
	EmuWord ss;
	
	Emulator emulator;
	
	private double width;
	private double height;
	private int representationMode;
	
	public SegmentRegisterPanel( Emulator emulator, double w, double h){
		representationMode = GUIConstants.HEX_MODE;
		width = w;
		height = h;
		this.emulator = emulator;
		
		CSNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[12]);
		DSNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[13]);
		SSNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[14]);
		ESNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[15]);
		
		CSValueLabel = new JLabel();
		DSValueLabel = new JLabel();
		SSValueLabel = new JLabel();
		ESValueLabel = new JLabel();
		
		segmentLabel = new JLabel( GUIConstants.SEGMENT_STRING);
		
		namePanel = new JPanel();
		namePanel.add( segmentLabel);
		namePanel.setBackground(GUIConstants.LABEL_BACKGROUND);
		
		gridPanel = new JPanel();
		gridPanel.setLayout( new GridLayout(4,2,10,5));
		
		gridPanel.add( CSNameLabel);
		gridPanel.add( CSValueLabel);
		gridPanel.add( DSNameLabel);
		gridPanel.add( DSValueLabel);
		gridPanel.add( ESNameLabel);
		gridPanel.add( ESValueLabel);
		gridPanel.add( SSNameLabel);
		gridPanel.add( SSValueLabel);
		
		CSNameLabel.setHorizontalAlignment(JLabel.RIGHT);
		DSNameLabel.setHorizontalAlignment(JLabel.RIGHT);
		ESNameLabel.setHorizontalAlignment(JLabel.RIGHT);
		SSNameLabel.setHorizontalAlignment(JLabel.RIGHT);

		Processor pro = emulator.getProcessor();
		
		cs = new EmuWord((short) pro.getRegisterValue( new RegisterType("CS")));
		ds = new EmuWord((short) pro.getRegisterValue( new RegisterType("DS")));
		es = new EmuWord((short) pro.getRegisterValue( new RegisterType("ES")));
		ss = new EmuWord((short) pro.getRegisterValue( new RegisterType("SS")));
		CSValueLabel.setText( cs.toString() );
		DSValueLabel.setText( ds.toString() );
		ESValueLabel.setText( es.toString() );
		SSValueLabel.setText( ss.toString() );
		
		handleBackGroundColors();
		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
		add( namePanel);
		add( gridPanel);
		
	}

	private void handleBackGroundColors() {
		segmentLabel.setBackground( GUIConstants.LABEL_BACKGROUND  );
		gridPanel.setBackground( GUIConstants.MEMORY_BACKGROUND  );
		namePanel.setBackground( GUIConstants.LABEL_BACKGROUND  );
		
		CSNameLabel.setBackground( GUIConstants.LABEL_BACKGROUND  );
		ESNameLabel.setBackground( GUIConstants.LABEL_BACKGROUND  );
		SSNameLabel.setBackground( GUIConstants.LABEL_BACKGROUND  );
		DSNameLabel.setBackground( GUIConstants.LABEL_BACKGROUND  );
		
		CSValueLabel.setBackground( GUIConstants.MEMORY_BACKGROUND  );
		SSValueLabel.setBackground( GUIConstants.MEMORY_BACKGROUND  );
		DSValueLabel.setBackground( GUIConstants.MEMORY_BACKGROUND  );
		ESValueLabel.setBackground( GUIConstants.MEMORY_BACKGROUND  );
		
	}

	public void updateView() {
		Processor pro = emulator.getProcessor();
		
		cs = new EmuWord((short) pro.getRegisterValue( new RegisterType("CS")));
		ds = new EmuWord((short) pro.getRegisterValue( new RegisterType("DS")));
		es = new EmuWord((short) pro.getRegisterValue( new RegisterType("ES")));
		ss = new EmuWord((short) pro.getRegisterValue( new RegisterType("SS")));
		cs.setRepresentationMode( representationMode );
		ds.setRepresentationMode( representationMode );
		ss.setRepresentationMode( representationMode );
		es.setRepresentationMode( representationMode );
		CSValueLabel.setText( cs.toString() );
		DSValueLabel.setText( ds.toString() );
		ESValueLabel.setText( es.toString() );
		SSValueLabel.setText( ss.toString() );
		
		//TODO ?
		repaint();
		
	}

	public void setValRepMode(int mode) {
		representationMode = mode;
		
	}

}
