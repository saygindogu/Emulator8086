package com.saygindogu.emulator;

import com.saygindogu.emulator.exception.AssemblerException;
import javafx.util.Pair;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 
 * 
 * Text dosyasn okuyan, anlamlandran ve ilemci snfna baz fonksiyonlar ile yardm eden Assembler snf.
 *
 */
public class Assembler {
	
	private List<Pair<Integer,Integer>> instructionToTextMapping;
	private List<Pair<String,Integer>> tagIndexTable;
	private ArrayList<String> tagList;
	
	private File assemblyFile;
	private BufferedReader reader;
	private List<String> instructionLines;
	private ArrayList<AssemblyVariable> variableList;
	
	
	/**
	 * constructor
	 * @param file assembly kodu dosyas
	 * @throws AssemblerException 
	 */
	public Assembler( File file) throws AssemblerException{
		assemblyFile = file;

		tagList = new ArrayList<String>();
		tagIndexTable = new ArrayList<Pair<String,Integer>>();
		if( file != null){
			readFile();
		}
	}
	
	// Getter ve setter methodlar.
	public File getAssemblyFile() {
		return assemblyFile;
	}

	public void setAssemblyFile(File assemblyFile) throws AssemblerException {
		this.assemblyFile = assemblyFile;
		readFile();
	}

	public BufferedReader getReader() {
		return reader;
	}

	public void setReader(BufferedReader reader) throws AssemblerException {
		this.reader = reader;
		readFile();
	}
	
	public List<Pair<Integer, Integer>> getInstructionToTextMapping() {
		return instructionToTextMapping;
	}

	public List<Pair<String, Integer>> getTagIndexTable() {
		return tagIndexTable;
	}

	public ArrayList<String> getTagList() {
		return tagList;
	}

	public List<String> getInstructionLines() {
		return instructionLines;
	}

	public AssemblyVariable getVariable(String name) {
		for( AssemblyVariable v : variableList){
			if( v.getName().equals(name)){
				return v;
			}
		}
		return null;
	}

	/**
	 * Stringlerden oluan bir ArrayList'teki Stringlerin her birindeki whiteSpace karakterlerini yok eder.
	 * @param tokens
	 * @return white space olmayan tokens
	 */
	public static ArrayList<String> getRidOfWhiteSpaceInTokens( ArrayList<String> tokens){
		ArrayList<String> nowspTokens = new ArrayList<String>();
		for( String token : tokens){
			Scanner scanner = new Scanner( new StringReader(token));
			StringBuilder builder = new StringBuilder();
			while( scanner.hasNext() ){
				builder.append( scanner.next() );
			}
			scanner.close();
			nowspTokens.add( builder.toString() );
		}
		return nowspTokens;
	}
	
	/**
	 * eer input bir adres ise true dner.
	 * adres formatlar: [AX], [CS:AX], [AX] + immediate, variable
	 * @param token
	 * @return
	 */
	public boolean isAddress( String token){
		return (token.contains( "[") && token.contains( "]") ) || isVariable(token);
	}
	
	/**
	 * eer input bir register ismi ise true dner
	 * @param token
	 * @return
	 */
	public static boolean isRegister( String token){
		boolean isRegister = false;
		for( String registerName : RegisterConstants.REGISTER_NAMES){
			isRegister |= token.equals( registerName);
		}
		return isRegister;
	}
	
	/**
	 * eer input bir immediate deer ise doru dner. thirtyTwoBitsMode false ise say en fazla 16 bit olarak hesaplanr.
	 * @param string
	 * @param thirtyTwoBitsMode
	 * @return
	 */
	private boolean isNumber(String string, boolean thirtyTwoBitsMode) {
		if( !thirtyTwoBitsMode) return isNumber(string);
		
		if( string.contains("h") || string.contains("H") ){
			return string.matches( "[\\da-fA-F]{1,8}[H,h]");
		}
		else if( string.contains("b") ){
			return string.matches( "[0-1]{0,32}b");
		}
		char[] str = string.toCharArray();
		for( char c : str){
			if( c < 48 || c > 57){
				return false;
			}
		}
		return true;
	}

	/**
	 * input saynn immediate deeri dndrlr.
	 * @param token
	 * @return
	 * @throws AssemblerException
	 */
	public static Immediate getImmediateValue( String token) throws AssemblerException{
		try {
			if( token.contains( "h" ) || token.contains("H") ){
				StringBuilder builder = new StringBuilder();
				builder.append( "#");
				if( token.contains( "h" ) ){
					builder.append( token.substring(0, token.indexOf( 'h' )));
				}
				else{
					builder.append( token.substring(0, token.indexOf( 'H' )));
				}
				return new Immediate( Integer.decode( builder.toString()));
			}
			else{
				return new Immediate (Integer.parseInt( token));
			}
		} catch (Exception e) {
			throw new AssemblerException( e);
		}
	}
	
	/**
	 * returns true only in valid numbers ( hex, binary or decimal in range)
	 * @param token
	 * @return
	 */
	public static boolean isNumber( String token){
		if( token.contains("h") || token.contains("H") ){
			return token.matches( "[\\da-fA-F]{1,4}[H,h]");
		}
		else if( token.contains("b") ){
			return token.matches( "[0-1]{0,16}b");
		}
		char[] str = token.toCharArray();
		for( char c : str){
			if( c < 48 || c > 57){
				return false;
			}
		}
		int value = Integer.parseInt(token);
		return ( value >= 0 && value <= 65535 );
	}
	
	/**
	 * eer satrn banda label var ise bu label silinir ve satr return edilir.
	 * @param index
	 * @return
	 */
	public ArrayList<String> getLineTokensWithoutLabel( int index) {
		if( index < instructionLines.size() ){
			return getRidOfWhiteSpaceInTokens( getTokensForLine( instructionLines.get(index)) );
		}
		else
			return null;
	}
	
	/**
	 * Verilen isimdeki register'n RegisterType trndeki deeri return edilir.
	 * @param register
	 * @return
	 * @throws AssemblerException
	 */
	public static RegisterType determineRegisterType( String register) throws AssemblerException{
		RegisterType type;
		int registerIndex = 0;
		while( !RegisterConstants.REGISTER_NAMES[registerIndex].equalsIgnoreCase(register)){
			registerIndex++;
			if( registerIndex >= RegisterConstants.REGISTER_NAMES.length){
				throw new AssemblerException();
			}
		}
		switch( registerIndex){
		case(0): //AH
			type = new RegisterType(RegisterConstants.GENERAL_TYPE_8, RegisterConstants.AH);
			break;
		case(1): //AL
			type = new RegisterType(RegisterConstants.GENERAL_TYPE_8, RegisterConstants.AL);
			break;
		case(2): //AX
			type = new RegisterType(RegisterConstants.GENERAL_TYPE, RegisterConstants.AX);
			break;
		case(3): //BH
			type = new RegisterType(RegisterConstants.GENERAL_TYPE_8, RegisterConstants.BH);
			break;
		case(4): //BL
			type = new RegisterType(RegisterConstants.GENERAL_TYPE_8, RegisterConstants.BL);
			break;
		case(5): //BX
			type = new RegisterType(RegisterConstants.GENERAL_TYPE, RegisterConstants.BX);
			break;
		case(6): //CH
			type = new RegisterType(RegisterConstants.GENERAL_TYPE_8, RegisterConstants.CH);
			break;
		case(7): //CL
			type = new RegisterType(RegisterConstants.GENERAL_TYPE_8, RegisterConstants.CL);
			break;
		case(8): //CX
			type = new RegisterType(RegisterConstants.GENERAL_TYPE, RegisterConstants.CX);
			break;
		case(9): //DH
			type = new RegisterType(RegisterConstants.GENERAL_TYPE_8, RegisterConstants.DH);
			break;
		case(10): //DL
			type = new RegisterType(RegisterConstants.GENERAL_TYPE_8, RegisterConstants.DL);
			break;
		case(11): //DX
			type = new RegisterType(RegisterConstants.GENERAL_TYPE, RegisterConstants.DX);
			break;
		case(12): //CS
			type = new RegisterType(RegisterConstants.SEGMENT_TYPE, RegisterConstants.CS);
			break;
		case(13): //DS
			type = new RegisterType(RegisterConstants.SEGMENT_TYPE, RegisterConstants.DS);
			break;
		case(14): //SS
			type = new RegisterType(RegisterConstants.SEGMENT_TYPE, RegisterConstants.SS);
			break;
		case(15): //ES
			type = new RegisterType(RegisterConstants.SEGMENT_TYPE, RegisterConstants.ES);
			break;
		case(16): //SP
			type = new RegisterType(RegisterConstants.PTR_INDEX_TYPE, RegisterConstants.SP);
			break;
		case(17): //BP
			type = new RegisterType(RegisterConstants.PTR_INDEX_TYPE, RegisterConstants.BP);
			break;
		case(18): //SI
			type = new RegisterType(RegisterConstants.PTR_INDEX_TYPE, RegisterConstants.SI);
			break;
		case(19): //DI
			type = new RegisterType(RegisterConstants.PTR_INDEX_TYPE, RegisterConstants.DI);
			break;
		case(20): //IP
			type = new RegisterType(RegisterConstants.PROGRAM_STATUS_TYPE, RegisterConstants.IP);
			break;
		case(21): //FLAG??
			type = new RegisterType(RegisterConstants.PROGRAM_STATUS_TYPE, RegisterConstants.FLAG);
			break;
		default:
			throw new AssemblerException();
		}
		return type;
	}
	
	/**
	 * Satrdaki kelimeler ayr ayr Stringlere evrilerek yeni bir liste olarak dndrlr.
	 * @param line
	 * @return
	 */
	private ArrayList<String> getTokensForLine( String line){
		ArrayList<String> tokens = new ArrayList<String>();
		String[] commaSeperated = line.split(",");
		while( commaSeperated[0].charAt(0) == ' '){
			commaSeperated[0] = commaSeperated[0].substring(1);
		}
		String[] mnemonicAndFirstToken = commaSeperated[0].split(" ");
		for( String token : mnemonicAndFirstToken){
			tokens.add(token);
		}
		for( int i = 1; i < commaSeperated.length; i++){
			tokens.add( commaSeperated[i]);
		}
		return tokens;
	}
	
	/**
	 * Assembly dosyas okunur.
	 * Okunurken Label ve variable tablolar da oluturulur.
	 * Instruction ieren satrlar ayr bir String array'ine konulur.
	 * @throws AssemblerException
	 */
	private void readFile() throws AssemblerException{
		try {
			reader = new BufferedReader( new FileReader(assemblyFile) );
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "File Not Found Exception:\n" + e.getMessage() );
		}
		if( reader != null){
			//initilize arrays
			instructionLines = new ArrayList<String>();
			tagIndexTable = new ArrayList<Pair<String,Integer>>();
			tagList = new ArrayList<String>();
			instructionToTextMapping = new ArrayList<Pair<Integer,Integer>>();
			variableList = new ArrayList<AssemblyVariable>(); 
	
			String line;
			int instructionIndex = 0;
			int textPosition = 0;
			try {
				line = reader.readLine();
				while( line != null){
					line = line.split(";")[0]; // get rid of comments
					
					if( !manageVariables( line)){
						//Look For Tags in the line
						if( line.contains( ":")){
							String potentialTag = line.substring(0, line.indexOf( ":" ));
							if( potentialTag.matches( "[A-Za-z][A-Za-z1-9]*" ) ){
								line = line.substring( line.indexOf(":")+1);
								System.out.println( "Lines : " + instructionLines);
								System.out.println( "Tag list: " + tagList );
								if( !tagList.contains(potentialTag))
								{
									String tag =  new String( potentialTag);
									tagIndexTable.add( new Pair<String, Integer>( tag, new Integer( instructionLines.size() ) ));
									tagList.add( tag);
								}
								else{
									throw new AssemblerException( "Multiple tags");
								}
							}	
						}
						
						if( line.length() > 0){
							instructionLines.add( line);
							instructionToTextMapping.add( new Pair<Integer, Integer>(new Integer(instructionIndex), new Integer( textPosition)));
							instructionIndex++;
						}
					}
					line = reader.readLine();
					textPosition++;
				}
				reader.close();
				System.out.println( instructionLines.toString() );
			} catch (IOException e) {
				JOptionPane.showMessageDialog( null, "IO Exception: " + e.getMessage() );
				e.printStackTrace();
			}
		}
		else{
			System.out.println("Reader is null!");
		}
	}
	
	/**
	 * Eer satrda variable var ise bu variable listesine eklenir.
	 * @param line
	 * @return Eer satrda bir variable varsa o true dner.
	 * @throws AssemblerException
	 */
	private boolean manageVariables(String line) throws AssemblerException {
		String copyOfLine = new String(line);
		String[] tokensArray = copyOfLine.split(" ");
		OperationWidth width;
		if( tokensArray.length >= 3){
			if( tokensArray[1].equals( "db") ){
				width = new OperationWidth( RegisterConstants.EIGHT_BIT );
				if( isValidVariableName( tokensArray[0] )){
					if( tokensArray[2].equals( "?") ){
						variableList.add( new AssemblyVariable( width, 1,0, tokensArray[0]) );
					}
					else if( isNumber( tokensArray[2], true) ){
						if( tokensArray.length > 3 && tokensArray[3].matches( "dup(.*)")){
							handleArrayDeclerations( tokensArray);
						}
						else{
							variableList.add( new AssemblyVariable( width, 1, getImmediateValue( tokensArray[2]).getIntValue(), tokensArray[0]) );
						}
					}
					else if( isChar( tokensArray[2] )){
						if( tokensArray.length > 3 && tokensArray[3].matches( "dup(.*)")){
							handleArrayDeclerations( tokensArray);
						}
						else{
							variableList.add( new AssemblyVariable( width, 1, getCharValue( tokensArray[2]).getIntValue(), tokensArray[0]) );
						}
					}
					else throw new AssemblerException( "Syntax Error");
					//TODO support strings
				}
				else throw new AssemblerException( "Variable name is not valid");
				return true;
			}
			else if( tokensArray[1].equals( "dw") ){
				width = new OperationWidth( RegisterConstants.SIXTEEN_BIT );
				if( isValidVariableName( tokensArray[0] )){
					if( tokensArray[2].equals( "?") ){
						variableList.add( new AssemblyVariable( width, 1,0, tokensArray[0]) );
					}
					else if( isNumber( tokensArray[2], true) ){
						if( tokensArray.length > 3 && tokensArray[3].matches( "dup(.*)")){
							handleArrayDeclerations( tokensArray);
						}
						else{
							variableList.add( new AssemblyVariable( width, 1, getImmediateValue( tokensArray[2]).getIntValue(), tokensArray[0]) );
						}
					}
					else throw new AssemblerException( "Syntax Error");
				}
				else throw new AssemblerException( "Variable name is not valid");
				return true;
			}
			else if(  tokensArray[1].equals( "dd")){
				width = new OperationWidth( RegisterConstants.THIRTY_TWO_BITS );
				if( isValidVariableName( tokensArray[0] )){
					if( tokensArray[2].equals( "?") ){
						variableList.add( new AssemblyVariable( width, 1,0, tokensArray[0]) );
					}
					else if( isNumber( tokensArray[2], true) ){
						if( tokensArray.length > 3 && tokensArray[3].matches( "dup(.*)")){
							handleArrayDeclerations( tokensArray);
						}
						else{
							variableList.add( new AssemblyVariable( width, 1, getImmediateValue( tokensArray[2], true).getIntValue(), tokensArray[0]) );
						}
					}
					else throw new AssemblerException( "Syntax Error");
					//TODO support strings
				}
				else throw new AssemblerException( "Variable name is not valid");
				return true;
			}
			else return false;
		}
		else return false;
	}

	/**
	 * 
	 * @param token
	 * @param thirtyTwoBitMode eer false ise say en fazla 16 bir olarak hesaplanr.
	 * @return input String'i bir say ise bunun immediate hali dnlr.
	 * @throws AssemblerException 
	 */
	private Immediate getImmediateValue(String token, boolean thirtyTwoBitMode) throws AssemblerException {
		if( !thirtyTwoBitMode) return getImmediateValue(token);
		else{
			try {
				if( token.contains( "h" ) || token.contains("H") ){
					if( token.length() > 5 ){
						long number;
						int msb, lsb;
						StringBuilder builder = new StringBuilder();
						builder.append( "#");
						if( token.contains( "h" ) ){
							builder.append( token.substring( token.indexOf('h') - 4, token.indexOf( 'h' )));
							msb = Integer.decode( token.substring( 0, token.indexOf('h') - 4));
						}
						else{
							builder.append( token.substring(token.indexOf('H') - 4, token.indexOf( 'H' )));
							msb = Integer.decode( token.substring( 0, token.indexOf('H') - 4));
						}
						lsb = Integer.decode( builder.toString() );
						number = msb + lsb;
						return new Immediate(  getUnsignedLongAsSignedInt( number) );
						
					}
					else{
						StringBuilder builder = new StringBuilder();
						builder.append( "#");
						if( token.contains( "h" ) ){
							builder.append( token.substring(0, token.indexOf( 'h' )));
						}
						else{
							builder.append( token.substring(0, token.indexOf( 'H' )));
						}
						return new Immediate( Integer.decode( builder.toString()));
					}
				}
				else{
					return new Immediate (Integer.parseInt( token));
				}
			} catch (Exception e) {
				throw new AssemblerException( e);
			}
		}
	}

	/**
	 * 
	 * @param number en fazla 32 bir data ieren long deeri
	 * @return long deerindeki her bir bit bire bir int iine kopyalanr.
	 */
	private int getUnsignedLongAsSignedInt(long number) {
		int value = 0;
		for( int i = 0; i < 32; i++){
			value += ((number >> i) & 0x01) << i;
		}
		return value;
	}

	/**
	 * @param charDecleration
	 * @return 'a' formatndaki string'in iindeki karakter'in deeri return edilir.
	 */
	private Immediate getCharValue(String charDecleration) {
		return new Immediate( Character.getNumericValue( charDecleration.charAt(1) ));
	}

	/**
	 * 
	 * @param name
	 * @return input variable listesinde bulunuyorsa true return edilir.
	 */
	boolean isVariable( String name){
		for( AssemblyVariable v : variableList){
			if( v.getName().equals(name) ){
				return true;
			}
		}
		return false;
	}

	/**
	 * @param string
	 * @return string in karakter decleration olup olmad kontrol edilir. ( 'a' format)
	 */
	private boolean isChar(String string) {
		return string.matches( "'.'");
	}

	/**
	 * TODO array destei verilemedi.. bu fonksiyonu tamamla
	 * array halindeki variable'larn AssemblyVariable objelerini yaratr.
	 * @param tokensArray
	 * @throws AssemblerException
	 */
	private void handleArrayDeclerations( String[] tokensArray) throws AssemblerException {
		String repetitionNo = tokensArray[3].substring( 
				tokensArray[3].indexOf( "(" ) + 1 , 
				tokensArray[3].indexOf( ")" ));
		if( isNumber( repetitionNo, true) ){
			//TODO implement arrays
		}
		else{
			throw new AssemblerException( "Syntax Error" );
		}
	}

	/**
	 * @param varName
	 * @return eer variable olma artlarna uyuyor ise doru return edilir
	 */
	private boolean isValidVariableName(String varName) {
		String[] mnemonics = Processor.SUPPORTED_MNEMONICS_LIST;
		for( String s : mnemonics){
			if( s.equalsIgnoreCase( varName))
				return false;
		}
		if(  varName.equals("db") || varName.equals("dw") || varName.equals("dd") ){
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param token
	 * @return eer token label olma zellii tayorsa doru return edilir.
	 */
	public boolean isALabel( String token){
		return token.matches( "[:alpha:]{1}");
	}

	
	/**
	 * Assembly dosyas deitii zaman arulmaldr, dosyay en batan okuyup anlamlandrr.
	 * @throws AssemblerException
	 */
	public void reset() throws AssemblerException {
		readFile();
	}

	/**
	 * variable'lar memory deki gerekli yerlere koyar
	 * @param memory
	 * @param beginningAddress
	 * @param incrementing memory addresiinin hangi yne doru ilerleriini belirler.
	 */
	public void placeVariablesInMemory( Memory memory, int beginningAddress, boolean incrementing){
		for( AssemblyVariable v : variableList){
			int arraySize = v.getLenght();
			while( arraySize > 0){
				memory.write( beginningAddress, v.getValue(), v.getUnitWidth());
				v.setAddress( beginningAddress );
				arraySize--;
				if( incrementing ){
					beginningAddress += v.getUnitWidth().getIntWidth() / 8;
				}
				else{
					beginningAddress -= v.getUnitWidth().getIntWidth() / 8;
				}
			}
		}
	}
}
