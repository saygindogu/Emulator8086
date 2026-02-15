package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.Emulator;
import com.saygindogu.emulator.RegisterConstants;
import com.saygindogu.emulator.RegisterType;

import javax.swing.*;
import java.awt.*;

public class PtrIndexRegistersPanel extends JPanel {

	private ProgramStatusResigersPanel bottomPanel;

	private JLabel SPValueLabel;
	private JLabel BPValueLabel;
	private JLabel SIValueLabel;
	private JLabel DIValueLabel;

	Emulator emulator;
	private int valueRepresentationMode;
	private int[] prevValues = new int[4];
	
	public PtrIndexRegistersPanel(Emulator emulator, double w, double h){
		valueRepresentationMode = GUIConstants.HEX_MODE;
		setBackground( Color.red);
		this.emulator = emulator;

		var topPanel = new JPanel();
		bottomPanel = new ProgramStatusResigersPanel( emulator, w, h);
		var nameLabel = new JLabel( GUIConstants.PTR_INDEX_REG_STRING );
		var nameLabelPanel = new JPanel();
		
		SPValueLabel = new JLabel();
		BPValueLabel = new JLabel();
		SIValueLabel = new JLabel();
		DIValueLabel = new JLabel();
		
		var SPNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[RegisterConstants.NAME_INDEX_SP] );
		var BPNameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[RegisterConstants.NAME_INDEX_BP] );
		var SINameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[RegisterConstants.NAME_INDEX_SI] );
		var DINameLabel = new JLabel( RegisterConstants.REGISTER_NAMES[RegisterConstants.NAME_INDEX_DI] );

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
		var pro = emulator.getProcessor();

		int[] curValues = new int[] {
			pro.getRegisterValue( new RegisterType("SP")),
			pro.getRegisterValue( new RegisterType("BP")),
			pro.getRegisterValue( new RegisterType("SI")),
			pro.getRegisterValue( new RegisterType("DI")),
		};

		JLabel[] labels = { SPValueLabel, BPValueLabel, SIValueLabel, DIValueLabel };
		for (int i = 0; i < labels.length; i++) {
			labels[i].setOpaque( true );
			labels[i].setBackground( curValues[i] != prevValues[i] ? GUIConstants.CHANGED_VALUE_COLOR : GUIConstants.MEMORY_BACKGROUND );
		}

		var sp = new EmuWord( (short) curValues[0]);
		var bp = new EmuWord( (short) curValues[1]);
		var si = new EmuWord( (short) curValues[2]);
		var di = new EmuWord( (short) curValues[3]);
		sp.setRepresentationMode( valueRepresentationMode);
		bp.setRepresentationMode( valueRepresentationMode);
		di.setRepresentationMode( valueRepresentationMode);
		si.setRepresentationMode( valueRepresentationMode);

		SPValueLabel.setText( sp.toString());
		BPValueLabel.setText( bp.toString());
		SIValueLabel.setText( si.toString());
		DIValueLabel.setText( di.toString());

		System.arraycopy(curValues, 0, prevValues, 0, curValues.length);

		bottomPanel.updateView();

	}

	public void setValRepMode(int mode) {
		valueRepresentationMode = mode;
		bottomPanel.setValRepMode(mode);
		
	}

}
