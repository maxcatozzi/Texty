package texty;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
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
        textyModel.styledDoc = textyView.textarea.getStyledDocument();
        TextyModel.addInstanceCount();   
        textyModel.fontName = (String)textyView.fontFamilyChooser.getSelectedItem();
        textyModel.fontSize = Integer.parseInt((String) textyView.fontSizeChooser.getSelectedItem());
        setTextFontSize(textyModel.fontSize);
        setTextFontFamily(textyModel.fontName);
        
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
        textyView.fontSizeChooser.addItemListener(new ToolbarEvent());
        textyView.fontFamilyChooser.addItemListener(new ToolbarEvent());
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
                            textyView.saveAnywayWin = textyView.new SaveAnywayWin(TextyHelper.getFixedPath(saveFile.getAbsolutePath()), saveFile.getName());
                        }
                        else {
                            saveFile(saveFile);
                        }
                    }
                    break;
                case "SaveAnyway":
                    textyModel.fileIsNew = false;
                    TextyHelper.closeWindow(textyView.saveAnywayWin, textyView);
                    fullFilepath = TextyHelper.getFixedPath(textyModel.getFilepath()) + textyModel.getFilename();
                    saveFile = new File(fullFilepath);
                    if(saveFile(saveFile)) {
                        textyView.setTitle("Texty - " + textyModel.getFilename());
                        TextyModel.globalFilepath = textyModel.getFilepath();
                    }
                    break;
                case "CancelSaveAnyway":
                    TextyHelper.closeWindow(textyView.saveAnywayWin, textyView);
                    textyModel.fileIsNew = true;
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
    
    class ToolbarEvent implements ActionListener, ItemListener {
        @Override
        public void actionPerformed(ActionEvent e) {
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

        @Override
        public void itemStateChanged(ItemEvent e) {
            if(e.getStateChange()==ItemEvent.SELECTED){
                if(e.getSource() == textyView.fontFamilyChooser) {
                    String fontName = (String) e.getItem();
                    setTextFontFamily(fontName);
                }
                else if(e.getSource() == textyView.fontSizeChooser) {
                    int fontSize = Integer.parseInt((String) e.getItem());
                    setTextFontSize(fontSize);
                }
            }
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
    
    private void setTextFontSize(int fontSize) {
        MutableAttributeSet setFontSize = new SimpleAttributeSet();
        StyleConstants.setFontSize(setFontSize, fontSize); 
        textyView.textarea.setCharacterAttributes(setFontSize, false);
        textyView.textarea.requestFocusInWindow();
    }
    
    private void setTextFontFamily(String fontName) {
        MutableAttributeSet setFontName = new SimpleAttributeSet();
        StyleConstants.setFontFamily(setFontName, fontName); 
        textyView.textarea.setCharacterAttributes(setFontName, false);
        textyView.textarea.requestFocusInWindow();
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
        textyView.textarea.requestFocusInWindow();
    }
    
    private void setTypeStyles(AttributeSet attributes) {
        boolean isBold = StyleConstants.isBold(attributes);
        boolean isItalic = StyleConstants.isItalic(attributes);
        boolean isUnderlined = StyleConstants.isUnderline(attributes);
        
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
    }
    
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
            
            try(BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(saveFile))) {
                if(!saveFile.exists()) {
                    saveFile.createNewFile();
                }
                
                if(fileExt.equals("rtf")) {
                    RTFEditorKit kit = new RTFEditorKit();
                    kit.write(fileOut, textyModel.styledDoc, 0, textyView.textarea.getDocument().getLength());
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
    
    private void saveNewFile(File saveFile) {
        try {
            String filepath = TextyHelper.getFixedPath(saveFile.getAbsolutePath());
            String filename = saveFile.getName();
            
            if(saveFile.exists()) {
                textyView.saveAnywayWin = textyView.new SaveAnywayWin(filepath, filename);
            }
            else {
                String oldFilepath = textyModel.getFilepath();
                String oldFilename = textyModel.getFilename();
                textyModel.setFilepath(filepath);
                textyModel.setFilename(filename);

                textyModel.fileIsNew = false;

                if(saveFile(saveFile));
                else {
                    textyModel.fileIsNew = true;
                    textyModel.setFilepath(oldFilepath);
                    textyModel.setFilename(oldFilename);
                }
            }

        } catch(Exception e) {
            JOptionPane.showMessageDialog(textyView, "File was not saved! " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}