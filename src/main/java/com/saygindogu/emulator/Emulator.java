package com.saygindogu.emulator;

import com.saygindogu.emulator.gui.EmulatorView;
import com.saygindogu.emulator.exception.AssemblerException;
import com.saygindogu.emulator.exception.EMURunTimeException;

import javax.swing.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Emulator {

	private static final Logger LOGGER = Logger.getLogger(Emulator.class.getName());
	public static final int MIN_MEMORY_SIZE = 4096;

	private File assemblyFile;
	private Assembler assembler;
	private Processor processor;
	private EmulatorView view;

	public Emulator() {
		assemblyFile = null;
		try {
			assembler = new Assembler(assemblyFile);
		} catch (AssemblerException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		processor = new Processor(assembler, MIN_MEMORY_SIZE);
	}

	public File getAssemblyFile() { return assemblyFile; }

	public void setAssemblyFile(File assemblyFile) {
		this.assemblyFile = assemblyFile;
		try {
			assembler.setAssemblyFile(assemblyFile);
			processor.reset();
		} catch (AssemblerException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		notifyViewForChanges();
	}

	public Processor getProcessor() { return processor; }
	public EmulatorView getView() { return view; }

	public void setView(EmulatorView view) {
		this.view = view;
		view.setEmulator(this);
		notifyViewForChanges();
	}

	public void oneStep() throws EMURunTimeException, AssemblerException {
		processor.fetch();
		processor.execute();
		notifyViewForChanges();
	}

	private void notifyViewForChanges() {
		if (view != null)
			view.updateView(this);
	}

	public static void main(String[] args) {
		LoggingConfig.init();
		SwingUtilities.invokeLater(() -> {
			try {
				var emu = new Emulator();
				var file = new File("test_inputs/comprehensive_demo.asm");
				if (!file.canRead()) {
					file.createNewFile();
				}
				emu.setAssemblyFile(file);
				var view = new EmulatorView(emu);
				emu.setView(view);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Exception Occured!\n" + e.getMessage());
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		});
	}

	public void runAll() {
		while (!processor.isFinished() && !processor.isWaiting()) {
			try {
				oneStep();
			} catch (EMURunTimeException rte) {
				JOptionPane.showMessageDialog(getView(), "Run Time Exception:\n" + rte.getMessage());
				LOGGER.log(Level.SEVERE, rte.getMessage(), rte);
				break;
			} catch (AssemblerException ae) {
				JOptionPane.showMessageDialog(getView(), "Assembler Exception:\n" + ae.getMessage());
				LOGGER.log(Level.SEVERE, ae.getMessage(), ae);
				break;
			}
		}
	}

	public void reset() {
		try {
			assembler.setAssemblyFile(assemblyFile);
			processor.reset();
			processor.startOS();
		} catch (AssemblerException e) {
			JOptionPane.showMessageDialog(view, "Exception while reseting: " + e.getMessage());
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		notifyViewForChanges();
	}
}
