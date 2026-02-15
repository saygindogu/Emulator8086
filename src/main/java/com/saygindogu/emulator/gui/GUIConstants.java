package com.saygindogu.emulator.gui;

import java.awt.*;

public interface GUIConstants {

	int HEX_MODE = 16;
	int DECIMAL_MODE = 10;
	int BINARY_MODE = 2;
	
	Color MEMORY_BACKGROUND = new Color( 200, 200, 250);
	Color LIST_SELECTED = new Color( 255, 220, 250);
	Color LABEL_BACKGROUND = new Color( 110, 220, 250);
	Color HGHLIGHT_COLOR = new Color( 200, 230, 255);
	Color ERROR_COLOR = new Color( 250, 65, 80);
	Color CHANGED_VALUE_COLOR = new Color(200, 96, 64);
	
	double TEXT_PANEL_FRACTION_H = 8/12.0;
	double TEXT_PANEL_FRACTION_W = 9/12.0;
	double MEMORY_PANEL_FRACTION_W = 3/12.0;
	double MEMORY_PANEL_FRACTION_H = 8/12.0;
	double REGISTER_PANEL_FRACTION_H = 3/12.0;
	double REGISTER_PANEL_FRACTION_W = 12/12.0;
	
	int RIGID_AREA_3 = 30;
	int RIGID_AREA_1 = 10;
	
	String PTR_INDEX_REG_STRING = "Pointer/Index Registers";
	String MEMORY_STRING = "Value";
	String ADDRESS_STRING = "Address";
	String HIGH_STRING = "High";
	String LOW_STRING = "Low";
	String SEGMENT_STRING = "Segment Registers";
	String FLAG_STRING = "Flag";
	String GENERAL_PURPOSE_REGISTERS_STRING = "General Purpose Registers";
}
