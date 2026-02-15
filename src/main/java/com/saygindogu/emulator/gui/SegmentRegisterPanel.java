package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.RegisterType;
import com.saygindogu.emulator.Emulator;
import com.saygindogu.emulator.RegisterConstants;

import javax.swing.*;
import java.awt.*;

public class SegmentRegisterPanel extends JPanel {
	
	JPanel gridPanel;
	JPanel namePanel;

	JLabel CSValueLabel;
	JLabel SSValueLabel;
	JLabel DSValueLabel;
	JLabel ESValueLabel;

	Emulator emulator;

	private int representationMode;
	private int[] prevValues = new int[4];
	
	public SegmentRegisterPanel( Emulator emulator, double w, double h){
		representationMode = GUIConstants.HEX_MODE;
		this.emulator = emulator;

		var CSNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[12]);
		var DSNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[13]);
		var SSNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[14]);
		var ESNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[15]);
		
		CSValueLabel = new JLabel();
		DSValueLabel = new JLabel();
		SSValueLabel = new JLabel();
		ESValueLabel = new JLabel();
		
		var segmentLabel = new JLabel( GUIConstants.SEGMENT_STRING);

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

		var pro = emulator.getProcessor();

		var cs = new EmuWord((short) pro.getRegisterValue( new RegisterType("CS")));
		var ds = new EmuWord((short) pro.getRegisterValue( new RegisterType("DS")));
		var es = new EmuWord((short) pro.getRegisterValue( new RegisterType("ES")));
		var ss = new EmuWord((short) pro.getRegisterValue( new RegisterType("SS")));
		CSValueLabel.setText( cs.toString() );
		DSValueLabel.setText( ds.toString() );
		ESValueLabel.setText( es.toString() );
		SSValueLabel.setText( ss.toString() );

		segmentLabel.setBackground( GUIConstants.LABEL_BACKGROUND );
		gridPanel.setBackground( GUIConstants.MEMORY_BACKGROUND );
		namePanel.setBackground( GUIConstants.LABEL_BACKGROUND );
		CSNameLabel.setBackground( GUIConstants.LABEL_BACKGROUND );
		ESNameLabel.setBackground( GUIConstants.LABEL_BACKGROUND );
		SSNameLabel.setBackground( GUIConstants.LABEL_BACKGROUND );
		DSNameLabel.setBackground( GUIConstants.LABEL_BACKGROUND );
		CSValueLabel.setBackground( GUIConstants.MEMORY_BACKGROUND );
		SSValueLabel.setBackground( GUIConstants.MEMORY_BACKGROUND );
		DSValueLabel.setBackground( GUIConstants.MEMORY_BACKGROUND );
		ESValueLabel.setBackground( GUIConstants.MEMORY_BACKGROUND );

		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
		add( namePanel);
		add( gridPanel);
	}

	public void updateView() {
		var pro = emulator.getProcessor();

		int[] curValues = new int[] {
			pro.getRegisterValue( new RegisterType("CS")),
			pro.getRegisterValue( new RegisterType("DS")),
			pro.getRegisterValue( new RegisterType("ES")),
			pro.getRegisterValue( new RegisterType("SS")),
		};

		JLabel[] labels = { CSValueLabel, DSValueLabel, ESValueLabel, SSValueLabel };
		for (int i = 0; i < labels.length; i++) {
			labels[i].setOpaque( true );
			labels[i].setBackground( curValues[i] != prevValues[i] ? GUIConstants.CHANGED_VALUE_COLOR : GUIConstants.MEMORY_BACKGROUND );
		}

		var cs = new EmuWord((short) curValues[0]);
		var ds = new EmuWord((short) curValues[1]);
		var es = new EmuWord((short) curValues[2]);
		var ss = new EmuWord((short) curValues[3]);
		cs.setRepresentationMode( representationMode );
		ds.setRepresentationMode( representationMode );
		ss.setRepresentationMode( representationMode );
		es.setRepresentationMode( representationMode );
		CSValueLabel.setText( cs.toString() );
		DSValueLabel.setText( ds.toString() );
		ESValueLabel.setText( es.toString() );
		SSValueLabel.setText( ss.toString() );

		System.arraycopy(curValues, 0, prevValues, 0, curValues.length);
		repaint();

	}

	public void setValRepMode(int mode) {
		representationMode = mode;
		
	}

}
