package texty;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.rtf.RTFEditorKit;
import static texty.TextyView.DEFAULT_BUTTON_BG;
import static texty.TextyView.DEPRESSED_BUTTON_BG;

/**
 *
 * @author Steve Karwacki
 */
public class TextyEvent {
    
    // Access to Model and View
    TextyModel textyModel;
    TextyView textyView;
    
    public TextyEvent() {
        String filepath = TextyModel.DEFAULT_FILEPATH;
        String filename = TextyModel.DEFAULT_FILENAME;
        TextyEvent textyEditor = new TextyEvent(new String[]{filepath,filename}, true);
    }
    
    public TextyEvent(String[] fileLocation, boolean newFile) {
        textyModel = new TextyModel(fileLocation, newFile);
        textyView = new TextyView(fileLocation[1]);
        TextyModel.addInstanceCount();   
        if(newFile) {
            textyModel.styledDoc = textyView.textarea.getStyledDocument();
            textyModel.fontName = (String)textyView.fontFamilyChooser.getSelectedItem();
            textyModel.fontSize = Integer.parseInt((String) textyView.fontSizeChooser.getSelectedItem());
            setTextFontFamily(textyModel.fontName);
            setTextFontSize(textyModel.fontSize);
        }
        
        // action listeners
        // menu
        textyView.saveMenuBtn.addActionListener(new SaveEvent());
        textyView.saveasMenuBtn.addActionListener(new SaveEvent());
        textyView.openMenuBtn.addActionListener(new OpenEvent());
        textyView.newMenuBtn.addActionListener(new NewEvent());
        textyView.exitMenuBtn.addActionListener(new ExitEvent());
        // toolbar
        textyView.textarea.addCaretListener(new CaretEvent());
        textyView.boldBtn.addActionListener(new ToolbarEvent());
        textyView.italicBtn.addActionListener(new ToolbarEvent());
        textyView.underlineBtn.addActionListener(new ToolbarEvent());
        textyView.fontSizeChooser.addActionListener(new ToolbarEvent());
        textyView.fontFamilyChooser.addActionListener(new ToolbarEvent());
        // dialogs
        textyView.saveAnywayBtn.addActionListener(new SaveEvent());
        textyView.saveAnywayCancelBtn.addActionListener(new SaveEvent());
    }
    
    // Event Handlers
    class NewEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch(command){
                case "FileMenuNew":
                    TextyEvent textyEditor = new TextyEvent();
                    break;
            }
        }
    }
    
    class OpenEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch(command){
                case "FileMenuOpen":
                    JFileChooser fc = new JFileChooser(TextyModel.globalFilepath);
                    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    int returnVal = fc.showOpenDialog(textyView);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        openFile(file);
                    }
                    break;
            }
        }
    }
    
    class SaveEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            String fullFilepath;
            File saveFile;
            switch(command){
                case "FileMenuSave":
                    fullFilepath = textyModel.getFilepath() + textyModel.getFilename();
                    saveFile = new File(fullFilepath);
                    saveFile(saveFile);
                    break;
                case "FileMenuSaveAs":
                    JFileChooser fc = new JFileChooser(TextyModel.globalFilepath);
                    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    fc.setSelectedFile(new File(textyModel.getFilename()));
                    fc.setDialogTitle("Save As");
                    int returnVal = fc.showSaveDialog(textyView);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        textyModel.fileIsNew = false;
                        saveFile = fc.getSelectedFile();
                        if(saveFile.exists()) {
                            saveExistingFile(saveFile);
                        }
                        else {
                            saveFile(saveFile);
                        }
                    }
                    break;
            }
        }
    }
    
    class ExitEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch(command){
                case "FileMenuExit":
                    System.exit(0);
                    break;
            }
        }
    }
    
    class CaretEvent implements CaretListener {
        @Override
        public void caretUpdate(javax.swing.event.CaretEvent e) {
            final int dot = e.getDot();
            final int mark = e.getMark();
            if(dot == mark) {
                Element charElement = textyModel.styledDoc.getCharacterElement(dot - 1);
                AttributeSet attributes = charElement.getAttributes();
                setTypeStyles(attributes);
            }
            else {
                setSelectionStyles(mark, dot);
            }
        }    
    }
    
    class ToolbarEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {   
            if(e.getSource() == textyView.fontFamilyChooser) {
                Caret cursorPos = textyView.textarea.getCaret();
                String fontName = (String) textyView.fontFamilyChooser.getSelectedItem();
                if(!fontName.equals("- Choose Font -")) setTextFontFamily(fontName);
                else if(cursorPos.getDot() == cursorPos.getMark()) textyView.fontFamilyChooser.setSelectedItem(textyModel.fontName);
            }
            else if(e.getSource() == textyView.fontSizeChooser) {
                Caret cursorPos = textyView.textarea.getCaret();
                String fontSizeString = (String) textyView.fontSizeChooser.getSelectedItem();
                if(!fontSizeString.equals("- -")) {
                    int fontSize = Integer.parseInt(fontSizeString);
                    setTextFontSize(fontSize);
                }
                else if(cursorPos.getDot() == cursorPos.getMark()) textyView.fontSizeChooser.setSelectedItem(Integer.toString(textyModel.fontSize));
            }
            else { // buttons
                String command = e.getActionCommand();
                switch(command){
                    case "ToolbarEmbolden":
                        setButtonState(textyView.boldBtn, 1, "ToolbarUnbold");
                        setTextStyle("bold", true);
                        break;
                    case "ToolbarUnbold":
                        setButtonState(textyView.boldBtn, 0, "ToolbarEmbolden");
                        setTextStyle("bold", false);
                        break;
                    case "ToolbarItalicize":
                        setButtonState(textyView.italicBtn, 1, "ToolbarDetalicize");
                        setTextStyle("italic", true);
                        break;
                    case "ToolbarDetalicize":
                        setButtonState(textyView.italicBtn, 0, "ToolbarItalicize");
                        setTextStyle("italic", false);
                        break;
                    case "ToolbarUnderline":
                        setButtonState(textyView.underlineBtn, 1, "ToolbarUnline");
                        setTextStyle("underline", true);
                        break;
                    case "ToolbarUnline":
                        setButtonState(textyView.underlineBtn, 0, "ToolbarUnderline");
                        setTextStyle("underline", false);
                        break;
                }
            }
            textyView.textarea.requestFocusInWindow();
        }
    }

    // Document and toolbar styling
    private void setButtonState(JButton btn, int isActive, String btnCommand) {
        switch(isActive) {
            case 0: //false
                btn.setBackground(DEFAULT_BUTTON_BG);
                break;
            case 1: //true
                btn.setBackground(DEPRESSED_BUTTON_BG);
                break;
        }
        btn.setActionCommand(btnCommand);
    }
    
    private void setComboSelection(JComboBox combo, String selection) {
        combo.setSelectedItem(selection);
    }
    
    private void setComboSelection(JComboBox combo, int selection) {
        combo.setSelectedIndex(selection);
    }
    
    private void setTextFontSize(int fontSize) {
        textyModel.fontSize = fontSize;
        MutableAttributeSet setFontSize = new SimpleAttributeSet();
        StyleConstants.setFontSize(setFontSize, fontSize); 
        textyView.textarea.setCharacterAttributes(setFontSize, false);
    }
    
    private void setTextFontFamily(String fontName) {
        textyModel.fontName = fontName;
        MutableAttributeSet setFontName = new SimpleAttributeSet();
        StyleConstants.setFontFamily(setFontName, fontName); 
        textyView.textarea.setCharacterAttributes(setFontName, false);
    }
        
    private void setTextStyle(String style, boolean isActive) {
        final MutableAttributeSet BOLD = new SimpleAttributeSet();
        final MutableAttributeSet ITALIC = new SimpleAttributeSet();
        final MutableAttributeSet UNDERLINE = new SimpleAttributeSet();
        switch(style) {
            case "bold":
                StyleConstants.setBold(BOLD, isActive); 
                textyView.textarea.setCharacterAttributes(BOLD, false);
                break;
            case "italic":
                StyleConstants.setItalic(ITALIC, isActive); 
                textyView.textarea.setCharacterAttributes(ITALIC, false);
                break;
            case "underline":
                StyleConstants.setUnderline(UNDERLINE, isActive); 
                textyView.textarea.setCharacterAttributes(UNDERLINE, false);
                break;
        }
        if(isActive) {
            textyModel.hasStyles = true;
        }
    }
    
    private void setTypeStyles(AttributeSet attributes) {
        boolean isBold = StyleConstants.isBold(attributes);
        boolean isItalic = StyleConstants.isItalic(attributes);
        boolean isUnderlined = StyleConstants.isUnderline(attributes);
        String textFontFamily = StyleConstants.getFontFamily(attributes);
        int textFontSize = StyleConstants.getFontSize(attributes);
        
        if(isBold) {
            setButtonState(textyView.boldBtn, 1, "ToolbarUnbold");
            setTextStyle("bold", true);
        }
        else {
            setButtonState(textyView.boldBtn, 0, "ToolbarEmbolden");
            setTextStyle("bold", false);
        }
        
        if(isItalic) {
            setButtonState(textyView.italicBtn, 1, "ToolbarDetalicize");
            setTextStyle("italic", true);
        }
        else {
            setButtonState(textyView.italicBtn, 0, "ToolbarItalicize");
            setTextStyle("italic", false);
        }
        
        if(isUnderlined) {
            setButtonState(textyView.underlineBtn, 1, "ToolbarUnline");
            setTextStyle("underline", true);
        }
        else {
            setButtonState(textyView.underlineBtn, 0, "ToolbarUnderline");
            setTextStyle("underline", false);
        }
        
        setComboSelection(textyView.fontFamilyChooser, textFontFamily);
        setTextFontFamily(textFontFamily);
        setComboSelection(textyView.fontSizeChooser, Integer.toString(textFontSize));
        setTextFontSize(textFontSize);
        
    }
    
    private void setSelectionStyles(int pos1, int pos2) {
        // order selectionStart and selectionEnd
        int selectionStart;
        int selectionEnd;
        if(pos1 < pos2) {
            selectionStart = pos1;
            selectionEnd = pos2;
        }
        else {
            selectionStart = pos2;
            selectionEnd = pos1;
        }
        
        int isBold = getSelectionStyleContinuity("bold", selectionStart, selectionEnd);
        int isItalic = getSelectionStyleContinuity("italic", selectionStart, selectionEnd);
        int isUnderline = getSelectionStyleContinuity("underline", selectionStart, selectionEnd);
        String selectionFontFamily = getSelectionFontContinuity("font", selectionStart, selectionEnd);
        String selectionFontSize = getSelectionFontContinuity("size", selectionStart, selectionEnd);
        
        switch(isBold) {
            case 0:
                setButtonState(textyView.boldBtn, 0, "ToolbarEmbolden");
                setTextStyle("bold", false);
                break;
            case 1:
                setButtonState(textyView.boldBtn, 1, "ToolbarUnbold");
                setTextStyle("bold", true);
                break;
            case 2:
                setButtonState(textyView.boldBtn, 0, "ToolbarEmbolden");
                break;
        }
        
        switch(isItalic) {
            case 0:
                setButtonState(textyView.italicBtn, 0, "ToolbarItalicize");
                setTextStyle("italic", false);
                break;
            case 1:
                setButtonState(textyView.italicBtn, 1, "ToolbarDetalicize");
                setTextStyle("italic", true);
                break;
            case 2:
                setButtonState(textyView.italicBtn, 0, "ToolbarItalicize");
                break;
        }
        
        switch(isUnderline) {
            case 0:
                setButtonState(textyView.underlineBtn, 0, "ToolbarUnderline");
                setTextStyle("underline", false);
                break;
            case 1:
                setButtonState(textyView.underlineBtn, 1, "ToolbarUnline");
                setTextStyle("underline", true);
                break;
            case 2:
                setButtonState(textyView.underlineBtn, 0, "ToolbarUnderline");
                break;
        }
        
        if(!selectionFontFamily.equals("")) {
            setComboSelection(textyView.fontFamilyChooser, selectionFontFamily);
            setTextFontFamily(selectionFontFamily);
        }
        else setComboSelection(textyView.fontFamilyChooser, 0);
        
        if(!selectionFontSize.equals("")) {
            setComboSelection(textyView.fontSizeChooser, selectionFontSize);
            setTextFontSize(Integer.parseInt(selectionFontSize));
        }
        else {
            setComboSelection(textyView.fontSizeChooser, 0);
        }
        
        
    }
    
    // check if entire selected string has same styles
    private int getSelectionStyleContinuity(String style, int selectionStart, int selectionEnd) {
        int totalChars = selectionEnd - selectionStart;
        int styledChars = 0;
        AttributeSet attributes;
        boolean isStyled;
        for(int i = selectionStart; i < selectionEnd; i++) {
            Element charElement = textyModel.styledDoc.getCharacterElement(i);
            attributes = charElement.getAttributes();
            switch(style) {
                case "bold":
                    isStyled = StyleConstants.isBold(attributes);
                    break;
                case "italic":
                    isStyled = StyleConstants.isItalic(attributes);
                    break;
                case "underline":
                    isStyled = StyleConstants.isUnderline(attributes);
                    break;
                default:
                    isStyled = false;
                    break;
            }
            if(isStyled) styledChars++;
        }
        if(styledChars == 0) return 0; // if no characters are styled
        else if(styledChars == totalChars) return 1; // if all characters are styled
        else return 2; // if there is a mix of styled and unstyled characters
    }
    
    // check if entire selected string has same font family/size
    private String getSelectionFontContinuity(String checkType, int selectionStart, int selectionEnd) {
        int totalChars = selectionEnd - selectionStart;
        int sameTypeChars = 0;
        AttributeSet attributes;
        String fontStyle = "";
        String currentCharFontStyle;
        for(int i = selectionStart; i < selectionEnd; i++) {
            Element charElement = textyModel.styledDoc.getCharacterElement(i);
            attributes = charElement.getAttributes();
            switch(checkType) {
                case "font":
                    currentCharFontStyle = StyleConstants.getFontFamily(attributes);
                    if(i == selectionStart) fontStyle = currentCharFontStyle; // get font of first character to be compared to rest of string
                    break;
                case "size":
                    currentCharFontStyle = Integer.toString(StyleConstants.getFontSize(attributes));
                    if(i == selectionStart) fontStyle = currentCharFontStyle; // get size of first character to be compared to rest of string
                    break;
                default:
                    currentCharFontStyle = "";
                    break;
            }
            if(fontStyle.equals(currentCharFontStyle)) {
                sameTypeChars++;
            }
        }
        if(sameTypeChars == totalChars) return fontStyle;
        else return "";
    }
    
    // Open files
    private void openFile(File openFile) {
        String filepath = TextyHelper.getFixedPath(openFile.getAbsolutePath());
        String filename = openFile.getName();
        String[] fileLocation = new String[]{filepath,filename};

        try(BufferedReader reader = new BufferedReader(new FileReader(openFile))) {
            TextyEvent newTextyEditor = new TextyEvent(fileLocation, false);
            String content = "";
            
            String doctype; // check if file contains styled text
            InputStream checkMimeInput = new BufferedInputStream(new FileInputStream(openFile));
            String mimeType = URLConnection.guessContentTypeFromStream(checkMimeInput);
            if(mimeType == null) mimeType = URLConnection.guessContentTypeFromName(openFile.getName());
            switch(mimeType){
                case "application/rtf":
                    doctype = "rtf";
                    break;
                case "application/x-rtf":
                    doctype = "rtf";
                    break;
                case "text/richtext":
                    doctype = "rtf";
                    break;
                default:
                    doctype = "plain";
                    break;
            }
            
            switch (doctype) {
                case "rtf": // open as rich text
                    RTFEditorKit rtfKit = new RTFEditorKit();
                    newTextyEditor.textyView.textarea.setEditorKit(rtfKit);
                    FileInputStream fi = new FileInputStream(openFile);
                    
                    newTextyEditor.textyModel.styledDoc = newTextyEditor.textyView.textarea.getStyledDocument();
                    rtfKit.read( fi, newTextyEditor.textyModel.styledDoc, 0 );
                    break;
                case "plain": // open as plain text
                    String text;
                    while ((text = reader.readLine()) != null) {
                        content += text;
                    }   
                    newTextyEditor.textyView.textarea.setText(content);
                    break;
            }

            TextyModel.globalFilepath = filepath;

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(textyView, "File Not Found!\nError: The file \"" + filepath + filename + "\" does not exist", "Error", JOptionPane.ERROR_MESSAGE);
            TextyHelper.closeWindow(textyView, textyView);
        } catch (IOException | BadLocationException e) {
            JOptionPane.showMessageDialog(textyView, "File Could Not Be Opened!\nError: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            TextyHelper.closeWindow(textyView, textyView);
        }
    }
    
    // Save files
    public boolean saveFile(File saveFile) {        
        boolean saveSuccess = false;
        
        if(textyModel.fileIsNew) { // check if first ever save
            JFileChooser fc = new JFileChooser(TextyModel.globalFilepath);
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setSelectedFile(new File(textyModel.getFilename()));
            fc.setDialogTitle("Save New");
            int returnVal = fc.showSaveDialog(textyView);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                saveFile = fc.getSelectedFile();
                saveNewFile(saveFile);
            }
        }
        else {
            
            String filepath;
            String filename = saveFile.getName();
            String fileExt = TextyHelper.getFileExtension(filename);

            if(fileExt.equals("")) { // check and fix file extension
                fileExt = "txt";
                filename += ".txt";
                saveFile = new File(saveFile.getParentFile(), filename);
                filepath = TextyHelper.getFixedPath(saveFile.getAbsolutePath());
                try { textyModel.setFilepath(filepath); textyModel.setFilename(filename); } catch(Exception e) {}
            }
            else {
                filepath = TextyHelper.getFixedPath(saveFile.getAbsolutePath());
            }
            
            try(BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(saveFile))) {
                if(!saveFile.exists()) {
                    saveFile.createNewFile();
                }
                
                if(fileExt.equals("rtf")) {
                    RTFEditorKit kit = new RTFEditorKit();
                    kit.write(fileOut, textyModel.styledDoc, 0, textyModel.styledDoc.getLength());
                }
                else {
                    if(textyModel.hasStyles) { // if document has styles applied, they will be lost upon save
                        JOptionPane.showMessageDialog(textyView, "Saving as plain text will not save styles and formatting.", "Warning - Styles Will Be Lost", JOptionPane.WARNING_MESSAGE);
                    }
                    String content = textyView.textarea.getText();
                    fileOut.write(content.getBytes());
                }

                JOptionPane.showMessageDialog(textyView, "File Saved!", "Success", JOptionPane.PLAIN_MESSAGE);
                saveSuccess = true;
                
                textyView.setTitle("Texty - " + filename);
                TextyModel.globalFilepath = filepath;

            } catch(IOException | BadLocationException e) {
                JOptionPane.showMessageDialog(textyView, "File was not saved!\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return saveSuccess;
    }
    
    private void saveExistingFile(File saveFile) {
        try {
            String filepath = TextyHelper.getFixedPath(saveFile.getAbsolutePath());
            String filename = saveFile.getName();
            String fullFilepath = filepath + filename;

            int confirm = JOptionPane.showConfirmDialog(null, "File: \"" + fullFilepath + "\" already exists. Save anyway?", "Save - File Exists", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                textyModel.setFilepath(filepath);
                textyModel.setFilename(filename);
                textyModel.fileIsNew = false;
                saveFile(saveFile);
            }
        } catch(Exception e) {
            JOptionPane.showMessageDialog(textyView, "File was not saved! " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveNewFile(File saveFile) {
        try {
            String filepath = TextyHelper.getFixedPath(saveFile.getAbsolutePath());
            String filename = saveFile.getName();
            
            if(saveFile.exists()) {
                saveExistingFile(saveFile);
            }
            else {
                textyModel.setFilepath(filepath);
                textyModel.setFilename(filename);
                textyModel.fileIsNew = false;
                saveFile(saveFile);
            }

        } catch(Exception e) {
            JOptionPane.showMessageDialog(textyView, "File was not saved! " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}