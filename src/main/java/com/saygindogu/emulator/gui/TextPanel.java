package com.saygindogu.emulator.gui;

import com.saygindogu.emulator.Emulator;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TextPanel extends JScrollPane {

	private static final Logger LOGGER = Logger.getLogger(TextPanel.class.getName());

	JTextArea textArea;
	private Emulator emulator;
	File editingFile;

	public TextPanel(Emulator emulator, double w, double h){
		this.emulator = emulator;
		textArea = new JTextArea( 300, 300);
		textArea.setFont( new Font( "lucida console", Font.PLAIN, 20));
		textArea.setEditable(true);
		textArea.addKeyListener( new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				textArea.getHighlighter().removeAllHighlights();
			}
		});

		readFile();

		getViewport().add(textArea);
		setPreferredSize( new Dimension( (int) (w * GUIConstants.TEXT_PANEL_FRACTION_W), (int) (w * GUIConstants.TEXT_PANEL_FRACTION_H)));
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	}

	public void setEmulator(Emulator emulator) {
		this.emulator = emulator;


	}

	public void updateView() {
		if( editingFile != emulator.getAssemblyFile() ){
			//TODO ask for save?
			readFile();
		}
		var line = emulator.getProcessor().getTextLineIndex();
		textArea.getHighlighter().removeAllHighlights();
		Highlighter.HighlightPainter painter = null;
		var start = 0;
		var end = 0;
		try {
			if( line == -1){
				start = textArea.getLineStartOffset(0);
				end = textArea.getLineEndOffset( 0);
				painter = new DefaultHighlighter.DefaultHighlightPainter( GUIConstants.ERROR_COLOR);
			}
			else{
				start = textArea.getLineStartOffset(line);
				end = textArea.getLineEndOffset( line);
				painter = new DefaultHighlighter.DefaultHighlightPainter( GUIConstants.HGHLIGHT_COLOR);
			}
			textArea.getHighlighter().addHighlight(start, end, painter);

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	private void readFile() {
		editingFile = emulator.getAssemblyFile();
		try (var bufRead = new BufferedReader( new FileReader(editingFile))) {
			var builder = new StringBuilder();
			var line = bufRead.readLine();
			while( line != null){
				builder.append( line );
				builder.append( "\n");
				line = bufRead.readLine();
			}
			textArea.setText(builder.toString());
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	public JTextArea getTextArea() {
		return textArea;
	}
}
