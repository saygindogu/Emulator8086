package com.saygindogu.emulator;

import com.saygindogu.emulator.RegisterConstants.Width;
import com.saygindogu.emulator.exception.AssemblerException;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Assembler {

    private static final Logger LOGGER = Logger.getLogger(Assembler.class.getName());

    private List<Pair<Integer, Integer>> instructionToTextMapping;
    private List<Pair<String, Integer>> tagIndexTable;
    private List<String> tagList;

    private File assemblyFile;
    private List<String> instructionLines;
    private List<AssemblyVariable> variableList;

    public Assembler(File file) throws AssemblerException {
        assemblyFile = file;
        tagList = new ArrayList<>();
        tagIndexTable = new ArrayList<>();
        if (file != null) {
            readFile();
        }
    }

    public File getAssemblyFile() {
        return assemblyFile;
    }

    public void setAssemblyFile(File assemblyFile) throws AssemblerException {
        this.assemblyFile = assemblyFile;
        readFile();
    }

    public List<Pair<Integer, Integer>> getInstructionToTextMapping() {
        return instructionToTextMapping;
    }

    public List<Pair<String, Integer>> getTagIndexTable() {
        return tagIndexTable;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public List<String> getInstructionLines() {
        return instructionLines;
    }

    public AssemblyVariable getVariable(String name) {
        for (var v : variableList) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }

    public static List<String> getRidOfWhiteSpaceInTokens(List<String> tokens) {
        var nowspTokens = new ArrayList<String>();
        for (var token : tokens) {
            var scanner = new Scanner(new StringReader(token));
            var builder = new StringBuilder();
            while (scanner.hasNext()) {
                builder.append(scanner.next());
            }
            scanner.close();
            nowspTokens.add(builder.toString());
        }
        return nowspTokens;
    }

    public boolean isAddress(String token) {
        return (token.contains("[") && token.contains("]")) || isVariable(token);
    }

    public static boolean isRegister(String token) {
        for (var registerName : RegisterConstants.REGISTER_NAMES) {
            if (token.equals(registerName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNumber(String string, boolean thirtyTwoBitsMode) {
        if (!thirtyTwoBitsMode) return isNumber(string);

        if (string.contains("h") || string.contains("H")) {
            return string.matches("[\\da-fA-F]{1,8}[H,h]");
        } else if (string.contains("b")) {
            return string.matches("[0-1]{0,32}b");
        }
        for (var c : string.toCharArray()) {
            if (c < 48 || c > 57) {
                return false;
            }
        }
        return true;
    }

    public static Immediate getImmediateValue(String token) throws AssemblerException {
        try {
            if (token.contains("h") || token.contains("H")) {
                var builder = new StringBuilder();
                builder.append("#");
                if (token.contains("h")) {
                    builder.append(token.substring(0, token.indexOf('h')));
                } else {
                    builder.append(token.substring(0, token.indexOf('H')));
                }
                return new Immediate(Integer.decode(builder.toString()));
            } else {
                return new Immediate(Integer.parseInt(token));
            }
        } catch (Exception e) {
            throw new AssemblerException(e);
        }
    }

    public static boolean isNumber(String token) {
        if (token.contains("h") || token.contains("H")) {
            return token.matches("[\\da-fA-F]{1,4}[H,h]");
        } else if (token.contains("b")) {
            return token.matches("[0-1]{0,16}b");
        }
        for (var c : token.toCharArray()) {
            if (c < 48 || c > 57) {
                return false;
            }
        }
        var value = Integer.parseInt(token);
        return (value >= 0 && value <= 65535);
    }

    public List<String> getLineTokensWithoutLabel(int index) {
        if (index < instructionLines.size()) {
            return getRidOfWhiteSpaceInTokens(getTokensForLine(instructionLines.get(index)));
        }
        return null;
    }

    public static RegisterType determineRegisterType(String register) throws AssemblerException {
        var type = RegisterType.fromName(register);
        if (type == null) {
            throw new AssemblerException();
        }
        return type;
    }

    private List<String> getTokensForLine(String line) {
        var tokens = new ArrayList<String>();
        var commaSeperated = line.split(",");
        while (commaSeperated[0].charAt(0) == ' ') {
            commaSeperated[0] = commaSeperated[0].substring(1);
        }
        var mnemonicAndFirstToken = commaSeperated[0].split(" ");
        for (var token : mnemonicAndFirstToken) {
            tokens.add(token);
        }
        for (var i = 1; i < commaSeperated.length; i++) {
            tokens.add(commaSeperated[i]);
        }
        return tokens;
    }

    private void readFile() throws AssemblerException {
        final BufferedReader reader;
        try {
            LOGGER.info("Reading file: " + assemblyFile.getName());
            reader = new BufferedReader(new FileReader(assemblyFile));
            instructionLines = new ArrayList<>();
            tagIndexTable = new ArrayList<>();
            tagList = new ArrayList<>();
            instructionToTextMapping = new ArrayList<>();
            variableList = new ArrayList<>();

            var instructionIndex = 0;
            var textPosition = 0;
            try (reader) {
                var line = reader.readLine();
                while (line != null) {
                    line = line.split(";")[0];

                    if (!manageVariables(line, textPosition + 1)) {
                        if (line.contains(":")) {
                            var potentialTag = line.substring(0, line.indexOf(":"));
                            if (potentialTag.matches("[A-Za-z][A-Za-z1-9]*")) {
                                line = line.substring(line.indexOf(":") + 1);
                                LOGGER.fine("Lines : " + instructionLines);
                                LOGGER.fine("Tag list: " + tagList);
                                if (!tagList.contains(potentialTag)) {
                                    tagIndexTable.add(new Pair<>(potentialTag, instructionLines.size()));
                                    tagList.add(potentialTag);
                                } else {
                                    throw new AssemblerException("Multiple tags", textPosition + 1);
                                }
                            }
                        }

                        if (!line.isEmpty()) {
                            instructionLines.add(line);
                            instructionToTextMapping.add(new Pair<>(instructionIndex, textPosition));
                            instructionIndex++;
                        }
                    }
                    line = reader.readLine();
                    textPosition++;
                }
                LOGGER.fine(instructionLines.toString());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "IO Exception: " + e.getMessage());
                LOGGER.log(Level.SEVERE, "IO Exception while reading file", e);
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "File Not Found Exception:\n" + e.getMessage());
        }
    }

    private boolean manageVariables(String line, int lineNumber) throws AssemblerException {
        var tokensArray = line.split(" ");
        OperationWidth width;
        if (tokensArray.length >= 3) {
            if (tokensArray[1].equals("db")) {
                width = new OperationWidth(Width.EIGHT_BIT);
                if (isValidVariableName(tokensArray[0])) {
                    if (tokensArray[2].equals("?")) {
                        variableList.add(new AssemblyVariable(width, 1, 0, tokensArray[0]));
                    } else if (isNumber(tokensArray[2], true)) {
                        if (tokensArray.length > 3 && tokensArray[3].matches("dup(.*)")) {
                            handleArrayDeclerations(tokensArray, lineNumber);
                        } else {
                            variableList.add(new AssemblyVariable(width, 1, getImmediateValue(tokensArray[2]).getIntValue(), tokensArray[0]));
                        }
                    } else if (isChar(tokensArray[2])) {
                        if (tokensArray.length > 3 && tokensArray[3].matches("dup(.*)")) {
                            handleArrayDeclerations(tokensArray, lineNumber);
                        } else {
                            variableList.add(new AssemblyVariable(width, 1, getCharValue(tokensArray[2]).getIntValue(), tokensArray[0]));
                        }
                    } else throw new AssemblerException("Syntax Error", lineNumber);
                } else throw new AssemblerException("Variable name is not valid", lineNumber);
                return true;
            } else if (tokensArray[1].equals("dw")) {
                width = new OperationWidth(Width.SIXTEEN_BIT);
                if (isValidVariableName(tokensArray[0])) {
                    if (tokensArray[2].equals("?")) {
                        variableList.add(new AssemblyVariable(width, 1, 0, tokensArray[0]));
                    } else if (isNumber(tokensArray[2], true)) {
                        if (tokensArray.length > 3 && tokensArray[3].matches("dup(.*)")) {
                            handleArrayDeclerations(tokensArray, lineNumber);
                        } else {
                            variableList.add(new AssemblyVariable(width, 1, getImmediateValue(tokensArray[2]).getIntValue(), tokensArray[0]));
                        }
                    } else throw new AssemblerException("Syntax Error", lineNumber);
                } else throw new AssemblerException("Variable name is not valid", lineNumber);
                return true;
            } else if (tokensArray[1].equals("dd")) {
                width = new OperationWidth(Width.THIRTY_TWO_BITS);
                if (isValidVariableName(tokensArray[0])) {
                    if (tokensArray[2].equals("?")) {
                        variableList.add(new AssemblyVariable(width, 1, 0, tokensArray[0]));
                    } else if (isNumber(tokensArray[2], true)) {
                        if (tokensArray.length > 3 && tokensArray[3].matches("dup(.*)")) {
                            handleArrayDeclerations(tokensArray, lineNumber);
                        } else {
                            variableList.add(new AssemblyVariable(width, 1, getImmediateValue(tokensArray[2], true).getIntValue(), tokensArray[0]));
                        }
                    } else throw new AssemblerException("Syntax Error", lineNumber);
                } else throw new AssemblerException("Variable name is not valid", lineNumber);
                return true;
            } else return false;
        } else return false;
    }

    private Immediate getImmediateValue(String token, boolean thirtyTwoBitMode) throws AssemblerException {
        if (!thirtyTwoBitMode) return getImmediateValue(token);
        try {
            if (token.contains("h") || token.contains("H")) {
                if (token.length() > 5) {
                    int msb, lsb;
                    var builder = new StringBuilder();
                    builder.append("#");
                    if (token.contains("h")) {
                        builder.append(token.substring(token.indexOf('h') - 4, token.indexOf('h')));
                        msb = Integer.decode(token.substring(0, token.indexOf('h') - 4));
                    } else {
                        builder.append(token.substring(token.indexOf('H') - 4, token.indexOf('H')));
                        msb = Integer.decode(token.substring(0, token.indexOf('H') - 4));
                    }
                    lsb = Integer.decode(builder.toString());
                    long number = msb + lsb;
                    return new Immediate(getUnsignedLongAsSignedInt(number));
                } else {
                    var builder = new StringBuilder();
                    builder.append("#");
                    if (token.contains("h")) {
                        builder.append(token.substring(0, token.indexOf('h')));
                    } else {
                        builder.append(token.substring(0, token.indexOf('H')));
                    }
                    return new Immediate(Integer.decode(builder.toString()));
                }
            } else {
                return new Immediate(Integer.parseInt(token));
            }
        } catch (Exception e) {
            throw new AssemblerException(e);
        }
    }

    private int getUnsignedLongAsSignedInt(long number) {
        var value = 0;
        for (var i = 0; i < 32; i++) {
            value += ((number >> i) & 0x01) << i;
        }
        return value;
    }

    private Immediate getCharValue(String charDecleration) {
        return new Immediate(Character.getNumericValue(charDecleration.charAt(1)));
    }

    boolean isVariable(String name) {
        for (var v : variableList) {
            if (v.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isChar(String string) {
        return string.matches("'.'");
    }

    private void handleArrayDeclerations(String[] tokensArray, int lineNumber) throws AssemblerException {
        var repetitionNo = tokensArray[3].substring(
                tokensArray[3].indexOf("(") + 1,
                tokensArray[3].indexOf(")"));
        if (!isNumber(repetitionNo, true)) {
            throw new AssemblerException("Syntax Error", lineNumber);
        }
    }

    private boolean isValidVariableName(String varName) {
        for (var s : Processor.SUPPORTED_MNEMONICS_LIST) {
            if (s.equalsIgnoreCase(varName))
                return false;
        }
        return !varName.equals("db") && !varName.equals("dw") && !varName.equals("dd");
    }

    public boolean isALabel(String token) {
        return token.matches("[:alpha:]{1}");
    }

    public void reset() throws AssemblerException {
        readFile();
    }

    public void placeVariablesInMemory(Memory memory, int beginningAddress, boolean incrementing) {
        for (var v : variableList) {
            var arraySize = v.getLenght();
            while (arraySize > 0) {
                memory.write(beginningAddress, v.getValue(), v.getUnitWidth());
                v.setAddress(beginningAddress);
                arraySize--;
                if (incrementing) {
                    beginningAddress += v.getUnitWidth().getIntWidth() / 8;
                } else {
                    beginningAddress -= v.getUnitWidth().getIntWidth() / 8;
                }
            }
        }
    }
}
