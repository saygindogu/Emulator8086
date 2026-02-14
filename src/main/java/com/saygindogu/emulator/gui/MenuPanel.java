package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.Emulator;
import com.saygindogu.emulator.Processor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MenuPanel extends JPanel {

	Emulator emulator;
	JMenu file;
	JMenu edit;
	JMenu run;
	JMenu options;
	JMenuItem open;
	JMenuItem save;
	JMenuItem saveas;
	JMenuItem newFile;
	JMenuItem exit;
	JMenuItem nextItem;
	JMenuItem runAll;
	JMenuItem reset;
	JMenuItem representationMode;
	JMenuItem help;
	JMenuBar bar;
	JButton button;

	JPanel representationTypePanel;
	JRadioButton hexAddrButton, binAddrButton, decAddrButton;
	ButtonGroup valueGroup, addressGroup;
	JRadioButton hexButton, binButton, decButton;
	ActionListener actionListener;
	JLabel addressLabel, valuesLabel, titleLabel;
	JPanel repModBotPanel;

	private double width;
	private double height;

	private void createRepModPanel() {
		representationTypePanel = new JPanel();
		representationTypePanel.setBackground( GUIConstants.LABEL_BACKGROUND);
		representationTypePanel.setLayout( new BoxLayout(representationTypePanel, BoxLayout.Y_AXIS));

		addressLabel = new JLabel( "Address");
		valuesLabel = new JLabel( "Values");
		titleLabel = new JLabel( "Set Representation Mode");
		titleLabel.setAlignmentX( JLabel.CENTER_ALIGNMENT);
		addressLabel.setHorizontalAlignment(JLabel.CENTER);
		valuesLabel.setHorizontalAlignment(JLabel.CENTER);

		repModBotPanel = new JPanel();
		repModBotPanel.setLayout( new GridLayout( 4, 2, 10, 10) );
		repModBotPanel.add( addressLabel);
		repModBotPanel.add( valuesLabel);
		repModBotPanel.setBackground( GUIConstants.MEMORY_BACKGROUND );

		representationTypePanel.add( titleLabel);
		representationTypePanel.add( repModBotPanel);

		actionListener = e -> {
			if( e.getActionCommand().equals("hexAddr") ){
				emulator.getView().setAddressRepMode( GUIConstants.HEX_MODE );
			}
			else if( e.getActionCommand().equals("binAddr") ){
				emulator.getView().setAddressRepMode( GUIConstants.BINARY_MODE );
			}
			else if( e.getActionCommand().equals("decAddr") ){
				emulator.getView().setAddressRepMode( GUIConstants.DECIMAL_MODE );
			}
			else if( e.getActionCommand().equals("hex") ){
				emulator.getView().setValRepMode( GUIConstants.HEX_MODE );
			}
			else if( e.getActionCommand().equals("bin") ){
				emulator.getView().setValRepMode( GUIConstants.BINARY_MODE );
			}
			else if( e.getActionCommand().equals("dec") ){
				emulator.getView().setValRepMode( GUIConstants.DECIMAL_MODE );
			}
		};
		hexAddrButton = new JRadioButton( "Hex");
		hexAddrButton.setActionCommand("hexAddr");
		hexAddrButton.addActionListener(actionListener);
		hexAddrButton.setBackground( GUIConstants.MEMORY_BACKGROUND);
		binAddrButton = new JRadioButton( "Binary");
		binAddrButton.setActionCommand("binAddr");
		binAddrButton.addActionListener(actionListener);
		binAddrButton.setBackground( GUIConstants.MEMORY_BACKGROUND);
		decAddrButton = new JRadioButton( "Decimal");
		decAddrButton.setActionCommand("decAddr");
		decAddrButton.addActionListener(actionListener);
		decAddrButton.setBackground( GUIConstants.MEMORY_BACKGROUND);
		addressGroup = new ButtonGroup();
		addressGroup.add(hexAddrButton);
		addressGroup.add(binAddrButton);
		addressGroup.add(decAddrButton);

		hexButton = new JRadioButton( "Hex");
		hexButton.setActionCommand("hex");
		hexButton.addActionListener(actionListener);
		hexButton.setBackground( GUIConstants.MEMORY_BACKGROUND);
		binButton = new JRadioButton( "Binary");
		binButton.setActionCommand("bin");
		binButton.addActionListener(actionListener);
		binButton.setBackground( GUIConstants.MEMORY_BACKGROUND);
		decButton = new JRadioButton( "Decimal");
		decButton.setActionCommand("dec");
		decButton.addActionListener(actionListener);
		decButton.setBackground( GUIConstants.MEMORY_BACKGROUND);
		valueGroup = new ButtonGroup();
		valueGroup.add(hexButton);
		valueGroup.add(binButton);
		valueGroup.add(decButton);

		repModBotPanel.add( hexAddrButton);
		repModBotPanel.add( hexButton);
		repModBotPanel.add( binAddrButton);
		repModBotPanel.add( binButton);
		repModBotPanel.add( decAddrButton);
		repModBotPanel.add( decButton);
	}

	public MenuPanel( Emulator emu, double w, double h){
		createRepModPanel();
		width = w;
		height = h;
		emulator = emu;

		button = new JButton( "Next Step");
		button.addActionListener( e -> {
			emulator.oneStep();
			emulator.getProcessor().setWaiting( false);
		});

		bar = new JMenuBar();
		add(bar);

		file = new JMenu( "File");
		bar.add(file);

		edit = new JMenu( "Edit");
		bar.add(edit);

		run = new JMenu( "Run");
		bar.add(run);

		options = new JMenu("Options");
		bar.add( options);

		newFile = new JMenuItem( "New File");
		file.add(newFile);
		newFile.addActionListener( e -> {
			var fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory( new File( ".") );
			var filter = new FileNameExtensionFilter("ASSEMBLY FILES", "asm", "assembly");
			fileChooser.setFileFilter(filter);
			var choice = fileChooser.showSaveDialog( null);

			if ( choice == JFileChooser.APPROVE_OPTION)
			{
				var selectedFile = fileChooser.getSelectedFile();
				try {
					selectedFile.createNewFile();
					emulator.setAssemblyFile( selectedFile);
				}
				catch( Exception exception){
					JOptionPane.showMessageDialog( null, "Exception in new file.");
				}
			}
		});

		open = new JMenuItem( "Open File");
		file.add(open);
		open.addActionListener( e -> {
			var chooser = new JFileChooser();
			chooser.setCurrentDirectory( new File( ".") );
			var filter = new FileNameExtensionFilter("ASSEMBLY FILES", "asm", "assembly");
			chooser.setFileFilter(filter);
			var fileChooser = chooser.showOpenDialog( null);
			if (fileChooser == JFileChooser.APPROVE_OPTION)
			{
				var selectedFile = chooser.getSelectedFile();
				emulator.setAssemblyFile(selectedFile);
			}

		});

		save = new JMenuItem("Save");
		file.add(save);
		save.addActionListener( e -> {
			var assemblyCode = emulator.getView().getTextArea().getText();
			var savedFile = emulator.getAssemblyFile();
			try {
				var br = new BufferedWriter( new FileWriter(savedFile));
				br.append( assemblyCode);
				br.flush();
				br.close();
			} catch (IOException ex) {
				JOptionPane.showMessageDialog( null, "Exception while saving!" );
				ex.printStackTrace();
			}
			emulator.reset();
			JOptionPane.showMessageDialog( emulator.getView(), "Succesfully Saved");
		});

		saveas = new JMenuItem("Save as");
		file.add(saveas);
		saveas.addActionListener( e -> {
			var assemblyCode = emulator.getView().getTextArea().getText();
			var fileChooser = new JFileChooser();
			var filter = new FileNameExtensionFilter("ASSEMBLY FILES", "asm", "assembly");
			fileChooser.setFileFilter(filter);
			fileChooser.setCurrentDirectory( new File( ".") );
			var choice = fileChooser.showSaveDialog( null);

			if ( choice == JFileChooser.APPROVE_OPTION)
			{
				var selectedFile = fileChooser.getSelectedFile();

				try {
					var br = new BufferedWriter( new FileWriter(selectedFile));
					br.append( assemblyCode);
					br.flush();
					br.close();

				}
				catch( Exception exception){
					JOptionPane.showMessageDialog( null, "Exception while saving!" );
				}
				emulator.setAssemblyFile( selectedFile);
			}
		});

		exit = new JMenuItem("Exit");
		file.add(exit);
		exit.addActionListener( e -> System.exit(0));

		nextItem = new JMenuItem("NextStep");
		run.add(nextItem);
		nextItem.addActionListener( e -> {
			emulator.oneStep();
			emulator.getProcessor().setWaiting( false);
		});

		runAll = new JMenuItem("Run");
		run.add(runAll);
		runAll.addActionListener( e -> {
			try {
				emulator.runAll();
			} catch (InterruptedException ex) {
				JOptionPane.showMessageDialog( null, "Interrupted exception");
				ex.printStackTrace();
			}
		});

		reset = new JMenuItem( "Reset");
		run.add( reset);
		reset.addActionListener( e -> emulator.reset());

		representationMode = new JMenuItem( "Set representation mode");
		options.add( representationMode);
		representationMode.addActionListener( e -> {
			JOptionPane.showMessageDialog( emulator.getView(), representationTypePanel);
			emulator.getView().updateView(emulator);
		});

		help = new JMenuItem( "Help!");
		options.add( help);
		help.addActionListener( e -> {
			var message = new StringBuilder("x86 Architecture help :\n"
					+ "https://www.pcpolytechnic.com/it/ppt/8086_instruction_set.pdf"
					+ "\n" + "Supported mnemonics:\n");
			for( String s : Processor.SUPPORTED_MNEMONICS_LIST ){
				message.append(s).append(", ");
			}
			message.append("\nVariable declerations are supported except for arrays and strings.");
			JOptionPane.showMessageDialog( null, message.toString());
		});

		add( button);


	}//constructor

}
