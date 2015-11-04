package com.saygindogu.emulator.gui;

import java.awt.*;

public interface GUIConstants {

	public static final int HEX_MODE = 16;
	public static final int DECIMAL_MODE = 10;
	public static final int BINARY_MODE = 2;
	
	public static final Color MEMORY_BACKGROUND = new Color( 200, 200, 250);
	public static final Color LIST_SELECTED = new Color( 255, 220, 250);
	public static final Color LABEL_BACKGROUND = new Color( 110, 220, 250);
	public static final Color HGHLIGHT_COLOR = new Color( 200, 230, 255);
	public static final Color ERROR_COLOR = new Color( 250, 65, 80);
	
	public static final double TEXT_PANEL_FRACTION_H = 8/12.0;
	public static final double TEXT_PANEL_FRACTION_W = 9/12.0;
	public static final double MEMORY_PANEL_FRACTION_W = 3/12.0;
	public static final double MEMORY_PANEL_FRACTION_H = 8/12.0;
	public static final double REGISTER_PANEL_FRACTION_H = 3/12.0;
	public static final double REGISTER_PANEL_FRACTION_W = 12/12.0;
	
	public static final int RIGID_AREA_3 = 30;
	public static final int RIGID_AREA_1 = 10;
	
	public static final String PTR_INDEX_REG_STRING = "Pointer/Index Registers";
	public static final String MEMORY_STRING = "Value";
	public static final String ADDRESS_STRING = "Address";
	public static final String HIGH_STRING = "High";
	public static final String LOW_STRING = "Low";
	public static final String SEGMENT_STRING = "Segment Registers";
	public static final String FLAG_STRING = "Flag";
	public static final String GENERAL_PURPOSE_REGISTERS_STRING = "General Purpose Registers";
	
	

}
