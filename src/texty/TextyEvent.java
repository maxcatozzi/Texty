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
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    public TextyEvent(TextyView gui, TextyModel model) {
        textyView = gui; 
        textyModel = model;
    }
    
    // Event Handlers
    class NewEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch(command){
                case "FileMenuNew":
                    TextyModel textyEditor = new TextyModel();
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
                    textyModel.fileIsNew = false;
                    JFileChooser fc = new JFileChooser(TextyModel.globalFilepath);
                    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    fc.setDialogTitle("Save As");
                    fc.setApproveButtonText("Save As");
                    int returnVal = fc.showSaveDialog(textyView);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        saveFile = fc.getSelectedFile();
                        saveFile(saveFile);
                    }
                    break;
                case "SaveAnyway":
                    textyModel.fileIsNew = false;
                    TextyHelper.closeWindow(textyView.saveAnywayWin, textyView);
                    fullFilepath = TextyHelper.fixPath(textyModel.getFilepath()) + textyModel.getFilename();
                    saveFile = new File(fullFilepath);
                    if(saveFile(saveFile)) {
                        textyView.setTitle("Texty - " + textyModel.getFilename());
                        TextyModel.globalFilepath = textyModel.getFilepath();
                    }
                    break;
                case "CancelSaveAnyway":
                    TextyHelper.closeWindow(textyView.saveAnywayWin, textyView);
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
                Element charElement = textyView.textarea.getStyledDocument().getCharacterElement(dot);
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
            String command = e.getActionCommand();
            switch(command){
                case "ToolbarEmbolden":
                    setButtonState(textyView.boldBtn, 1, "ToolbarUnbold");
                    setTextStyle("bold", true);
                    textyView.textarea.requestFocusInWindow();
                    break;
                case "ToolbarUnbold":
                    setButtonState(textyView.boldBtn, 0, "ToolbarEmbolden");
                    setTextStyle("bold", false);
                    textyView.textarea.requestFocusInWindow();
                    break;
                case "ToolbarItalicize":
                    setButtonState(textyView.italicBtn, 1, "ToolbarDetalicize");
                    setTextStyle("italic", true);
                    textyView.textarea.requestFocusInWindow();
                    break;
                case "ToolbarDetalicize":
                    setButtonState(textyView.italicBtn, 0, "ToolbarItalicize");
                    setTextStyle("italic", false);
                    textyView.textarea.requestFocusInWindow();
                    break;
                case "ToolbarUnderline":
                    setButtonState(textyView.underlineBtn, 1, "ToolbarUnline");
                    setTextStyle("underline", true);
                    textyView.textarea.requestFocusInWindow();
                    break;
                case "ToolbarUnline":
                    setButtonState(textyView.underlineBtn, 0, "ToolbarUnderline");
                    setTextStyle("underline", false);
                    textyView.textarea.requestFocusInWindow();
                    break;
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
            Element charElement = textyView.textarea.getStyledDocument().getCharacterElement(i);
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
        String fullpath = openFile.getAbsolutePath();
        String filepath = TextyHelper.fixPath(fullpath.substring(0,fullpath.lastIndexOf(File.separator)));
        String filename = openFile.getName();
        String[] fileLocation = new String[]{filepath,filename};

        try(BufferedReader reader = new BufferedReader(new FileReader(openFile))) {
            String content = "";
            String doctype;
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
            
            TextyModel newTextyEditor = new TextyModel(fileLocation, false);
            
            switch (doctype) {
                case "rtf":
                    RTFEditorKit rtfKit = new RTFEditorKit();
                    newTextyEditor.textyView.textarea.setEditorKit(rtfKit);
                    FileInputStream fi = new FileInputStream(openFile);
                    rtfKit.read( fi, newTextyEditor.textyView.textarea.getStyledDocument(), 0 );
                    break;
                case "plain":
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
        } catch (IOException | BadLocationException e) {
            JOptionPane.showMessageDialog(textyView, "File Could Not Be Opened!\nError: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Save files
    public boolean saveFile(File saveFile) {        
        boolean saveSuccess = false;
        
        if(textyModel.fileIsNew) {
            if(saveFile.exists()) {
                textyView.saveAnywayWin = textyView.new SaveAnywayWin();
            }
            else {
                JFileChooser fc = new JFileChooser(TextyModel.globalFilepath);
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int returnVal = fc.showSaveDialog(textyView);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    saveFile = fc.getSelectedFile();
                    saveNewFile(saveFile);
                }
            }
        }
        else {

            try(BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(saveFile))) {
                if(!saveFile.exists()) {
                    saveFile.createNewFile();
                }
                
                String fullpath = saveFile.getAbsolutePath();
                String filepath = TextyHelper.fixPath(fullpath.substring(0,fullpath.lastIndexOf(File.separator)));
                String filename = saveFile.getName();
                
                RTFEditorKit kit = new RTFEditorKit();
                kit.write(fileOut, textyView.textarea.getStyledDocument(), 0, textyView.textarea.getDocument().getLength());

                JOptionPane.showMessageDialog(textyView, "File Saved!", "Success", JOptionPane.PLAIN_MESSAGE);
                saveSuccess = true;
                
                textyView.setTitle("Texty - " + filename);
                TextyModel.globalFilepath = filepath;

            } catch(IOException e) {
                JOptionPane.showMessageDialog(textyView, "File was not saved!\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (BadLocationException ex) {
                Logger.getLogger(TextyEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return saveSuccess;
    }
    
    private void saveNewFile(File saveFile) {
        try {
            String fullpath = saveFile.getAbsolutePath();
            String filepath = TextyHelper.fixPath(fullpath.substring(0,fullpath.lastIndexOf(File.separator)));
            String filename = saveFile.getName();
            
            String oldFilepath = textyModel.getFilepath();
            String oldFilename = textyModel.getFilename();
            textyModel.setFilepath(filepath);
            textyModel.setFilename(filename);
            
            if(saveFile.exists()) {
                textyView.saveAnywayWin = textyView.new SaveAnywayWin();
            }
            else {
                
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