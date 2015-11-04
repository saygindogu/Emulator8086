package com.saygindogu.emulator;

import com.saygindogu.emulator.gui.EmulatorView;
import com.saygindogu.emulator.exception.AssemblerException;
import com.saygindogu.emulator.exception.EMURunTimeException;

import javax.swing.*;
import java.io.File;

/**
 * 
 * 8086 emulatrnn ana snf.  
 */
public class Emulator {
	
	public static final int MIN_MEMORY_SIZE = 4096; // 4KB
	
	private File assemblyFile;
	private Assembler assembler;
	private Processor processor;
	
	private EmulatorView view;
	
	/**
	 *  Emulator objesi iin default contructor. Gerekli initilization yaplr.
	 */
	public Emulator(){
		assemblyFile = null;
		try {
			assembler = new Assembler(assemblyFile);
		} catch (AssemblerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		processor = new Processor( assembler, MIN_MEMORY_SIZE);
	}
	
	//Getter ve Setter Methodlar
	public File getAssemblyFile() {
		return assemblyFile;
	}

	public void setAssemblyFile(File assemblyFile) {
		this.assemblyFile = assemblyFile;
		try {
			assembler.setAssemblyFile(assemblyFile);
			processor.reset();
		} catch (AssemblerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		notifyViewForChanges();
	}

	public Assembler getAssembler() {
		return assembler;
	}

	public void setView(EmulatorView view) {
		this.view = view;
		view.setEmulator( this);
		notifyViewForChanges();
	}

	public EmulatorView getView() {
		return view;
	}

	public void setAssembler(Assembler assembler) {
		this.assembler = assembler;
	}

	public Processor getProcessor() {
		return processor;
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}
	
	
	/**
	 *  lemci'ye tek bir instruction'u altrmas komutu verilir.
	 */
	public void oneStep(){
		try {
			processor.fetch();
			processor.execute();
		} catch (EMURunTimeException rte) {
			JOptionPane.showMessageDialog(getView(), "Run Time Exception:\n" + rte.getMessage() + "\n" + rte.getStackTrace().toString());
			rte.printStackTrace();
		}catch( AssemblerException ae){
			JOptionPane.showMessageDialog(getView(), "Assembler Exception:\n" + ae.getMessage() + "\n" + ae.getStackTrace().toString());
			ae.printStackTrace();
		}
		notifyViewForChanges();
	}

	/**
	 *  GUI'ye kendini update etmesi gerektii sinyalini gnderir
	 */
	private void notifyViewForChanges() {
		if( view != null)
			view.updateView( this);
	}

	/**
	 * Main methodu. Yeni bir Emulatr objesi ve onun GUI'sini yaratr. 
	 * Geri kalan etkileim GUI zerinden yaplmaktadr.
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	try {
					Emulator emu = new Emulator();
					File file = new File( "test.asm" );
					if( !file.canRead() ){
						file.createNewFile();
					}
					emu.setAssemblyFile(file);
					EmulatorView view = new EmulatorView( emu);
					emu.setView( view);
				} catch (Exception e) {
					JOptionPane.showMessageDialog( null, "Exception Occured!\n" + e.getMessage() + "\n" + e.getStackTrace());
					e.printStackTrace();
				}
		    }
		 });
	}

	/**
	 * TODO infinite loop'a girerse program durdurmann bir yolu bulunmal
	 * 
	 * Assembly kodundaki tm instructionlar program durana kadar altrlr.
	 * @throws InterruptedException 
	 */
	public void runAll() throws InterruptedException {
		while( !processor.isFinished() && !processor.isWaiting() ){
			oneStep();
			//TODO handle infinite loops..
		}
	}

	

	/**
	 *  Assembly dosyas yeniden okunulur, ilemci'ye en batan balamas gerektii sinyali gnderilir.
	 *  Dosya her deitiinde arlmas gereklidir.
	 */
	public void reset() {
		try {
			assembler.setAssemblyFile(assemblyFile);
			processor.reset();
			processor.startOS();
		} catch (AssemblerException e) {
			JOptionPane.showMessageDialog( view, "Exception while reseting: " + e.getMessage() );
			e.printStackTrace();
		}
		notifyViewForChanges();
	}

}
