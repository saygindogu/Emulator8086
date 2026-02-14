package com.saygindogu.emulator;

import com.saygindogu.emulator.gui.EmulatorView;
import com.saygindogu.emulator.exception.AssemblerException;
import com.saygindogu.emulator.exception.EMURunTimeException;

import javax.swing.*;
import java.io.File;

public class Emulator {

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
			e.printStackTrace();
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
			e.printStackTrace();
		}
		notifyViewForChanges();
	}

	public Assembler getAssembler() { return assembler; }
	public void setAssembler(Assembler assembler) { this.assembler = assembler; }
	public Processor getProcessor() { return processor; }
	public void setProcessor(Processor processor) { this.processor = processor; }
	public EmulatorView getView() { return view; }

	public void setView(EmulatorView view) {
		this.view = view;
		view.setEmulator(this);
		notifyViewForChanges();
	}

	public void oneStep() {
		try {
			processor.fetch();
			processor.execute();
		} catch (EMURunTimeException rte) {
			JOptionPane.showMessageDialog(getView(), "Run Time Exception:\n" + rte.getMessage());
			rte.printStackTrace();
		} catch (AssemblerException ae) {
			JOptionPane.showMessageDialog(getView(), "Assembler Exception:\n" + ae.getMessage());
			ae.printStackTrace();
		}
		notifyViewForChanges();
	}

	private void notifyViewForChanges() {
		if (view != null)
			view.updateView(this);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				var emu = new Emulator();
				var file = new File("test.asm");
				if (!file.canRead()) {
					file.createNewFile();
				}
				emu.setAssemblyFile(file);
				var view = new EmulatorView(emu);
				emu.setView(view);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Exception Occured!\n" + e.getMessage());
				e.printStackTrace();
			}
		});
	}

	public void runAll() throws InterruptedException {
		while (!processor.isFinished() && !processor.isWaiting()) {
			oneStep();
		}
	}

	public void reset() {
		try {
			assembler.setAssemblyFile(assemblyFile);
			processor.reset();
			processor.startOS();
		} catch (AssemblerException e) {
			JOptionPane.showMessageDialog(view, "Exception while reseting: " + e.getMessage());
			e.printStackTrace();
		}
		notifyViewForChanges();
	}
}
