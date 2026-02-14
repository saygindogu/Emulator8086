package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.Emulator;

import javax.swing.*;
import java.awt.*;

public class MemoryPanel extends JPanel {

	private Color backColor;
	private JPanel listsPanel;
	private Emulator emulator;
	private EmuByte[] bytes;
	private EmuAddress[] indexes;
	private JList<EmuByte> byteList;
	private JList<EmuAddress> indexList;
	private JScrollPane scrollPane;
	private JLabel memoryLabel;
	private JLabel addressLabel;
	private JPanel topPanel;
	private JPanel bottomPanel;
	private double width;
	private double height;
	private int addressRepMode;
	private int valueRepMode;

	public MemoryPanel(Emulator emulator, double w, double h){
		addressRepMode = GUIConstants.HEX_MODE;
		valueRepMode = GUIConstants.HEX_MODE;
		width = w;
		height = h;
		backColor = GUIConstants.MEMORY_BACKGROUND;
		this.emulator = emulator;

		memoryLabel = new JLabel( GUIConstants.MEMORY_STRING);
		memoryLabel.setBackground( GUIConstants.LABEL_BACKGROUND);
		addressLabel = new JLabel( GUIConstants.ADDRESS_STRING);
		addressLabel.setBackground( GUIConstants.LABEL_BACKGROUND);

		topPanel = new JPanel();
		topPanel.setBackground( GUIConstants.LABEL_BACKGROUND);
		topPanel.add( addressLabel);
		topPanel.add( Box.createHorizontalGlue());
		topPanel.add( memoryLabel);

		listsPanel = new JPanel();
		listsPanel.setBackground( backColor);

		updateView();

		bottomPanel = new JPanel();
		bottomPanel.setBackground( GUIConstants.MEMORY_BACKGROUND);
		bottomPanel.add(listsPanel);

		scrollPane = new JScrollPane( bottomPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement( 10);

		setLayout( new BorderLayout());
		add( scrollPane, BorderLayout.CENTER);
		add( topPanel, BorderLayout.NORTH );
		scrollPane.setPreferredSize(new Dimension( (int) (w * GUIConstants.MEMORY_PANEL_FRACTION_W), (int) (h * GUIConstants.MEMORY_PANEL_FRACTION_H) ));
		setBackground( backColor );
		add( scrollPane);
	}

	public void updateView() {
		var memSize = emulator.getProcessor().getMemorySize();
		bytes = new EmuByte[ memSize];
		indexes = new EmuAddress[ memSize];
		for( var i = 0; i < memSize; i++ ){
			bytes[i] =  new EmuByte( emulator.getProcessor().getMemory().getByteArray()[i]);
			bytes[i].setRepresentationMode( valueRepMode);
			indexes[i] = new EmuAddress(i);
			indexes[i].setRepresentationMode( addressRepMode);
		}

		listsPanel.removeAll();

		byteList = new JList<>( bytes );
		byteList.setSelectionBackground( GUIConstants.LIST_SELECTED);
		byteList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		byteList.setLayoutOrientation(JList.VERTICAL);
		byteList.setBackground( backColor);
		byteList.setVisibleRowCount(-1);

		indexList = new JList<>( indexes );
		indexList.setSelectionBackground( GUIConstants.LIST_SELECTED);
		indexList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		indexList.setLayoutOrientation(JList.VERTICAL);
		indexList.setBackground( backColor);
		indexList.setVisibleRowCount(-1);
		indexList.addListSelectionListener( e -> {
			if( e.getValueIsAdjusting() ) return;
			byteList.clearSelection();
			byteList.setSelectedIndices( indexList.getSelectedIndices());
		});

		listsPanel.add( indexList);
		listsPanel.add( Box.createRigidArea(new Dimension(GUIConstants.RIGID_AREA_3, 0)));
		listsPanel.add( byteList);
		repaint();

	}

	public void setAddressRepMode(int mode) {
		addressRepMode = mode;
	}

	public void setValRepMode(int mode) {
		valueRepMode = mode;

	}

}
