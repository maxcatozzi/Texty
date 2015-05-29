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
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
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
                    textyModel.fileIsNew = true;
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

    class RenameEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            String newFilename;
            switch(command){
                case "FileMenuRename":
                    textyView.renameWin = textyView.new RenameFileWin();
                    break;
                case "Rename":
                    newFilename = textyView.renameWin.getNewFilename();
                    renameFile(newFilename);
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
    
    class ToolbarEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            //String selectedText;
            switch(command){
                case "ToolbarEmbolden":
                    // need to get style of selected text and toggle
                    /*selectedText = textyView.textarea.getSelectedText();
                    if(selectedText != null) {
                        SimpleAttributeSet newFontAttributes = new SimpleAttributeSet();
                        StyleConstants.setBold(newFontAttributes, true); 
                        textyView.textarea.setCharacterAttributes(newFontAttributes,false);
                    }
                    else {*/
                        StyleConstants.setBold(textyModel.fontAttributes, true); 
                        textyView.textarea.setCharacterAttributes(textyModel.fontAttributes,false);
                    //}
                    textyView.boldBtn.setBackground(DEPRESSED_BUTTON_BG);
                    textyView.boldBtn.setActionCommand("ToolbarUnbold");
                    textyView.textarea.requestFocusInWindow();
                    break;
                case "ToolbarUnbold":
                    /*selectedText = textyView.textarea.getSelectedText();
                    if(selectedText != null) {
                        SimpleAttributeSet newFontAttributes = new SimpleAttributeSet();
                        StyleConstants.setBold(newFontAttributes, false); 
                        textyView.textarea.setCharacterAttributes(newFontAttributes,false);
                    }
                    else {*/
                        StyleConstants.setBold(textyModel.fontAttributes, false); 
                        textyView.textarea.setCharacterAttributes(textyModel.fontAttributes,false);
                    //}
                    textyView.boldBtn.setBackground(DEFAULT_BUTTON_BG);
                    textyView.boldBtn.setActionCommand("ToolbarEmbolden");
                    textyView.textarea.requestFocusInWindow();
                    break;
                case "ToolbarItalicize":
                    /*selectedText = textyView.textarea.getSelectedText();
                    if(selectedText != null) {
                        SimpleAttributeSet newFontAttributes = new SimpleAttributeSet();
                        StyleConstants.setItalic(newFontAttributes, true); 
                        textyView.textarea.setCharacterAttributes(newFontAttributes,false);
                    }
                    else {*/
                        StyleConstants.setItalic(textyModel.fontAttributes, true); 
                        textyView.textarea.setCharacterAttributes(textyModel.fontAttributes,false);
                    //}
                    textyView.italicBtn.setBackground(DEPRESSED_BUTTON_BG);
                    textyView.italicBtn.setActionCommand("ToolbarDetalicize");
                    textyView.textarea.requestFocusInWindow();
                    break;
                case "ToolbarDetalicize":
                    /*selectedText = textyView.textarea.getSelectedText();
                    if(selectedText != null) {
                        SimpleAttributeSet newFontAttributes = new SimpleAttributeSet();
                        StyleConstants.setItalic(newFontAttributes, false); 
                        textyView.textarea.setCharacterAttributes(newFontAttributes,false);
                    }
                    else {*/
                        StyleConstants.setItalic(textyModel.fontAttributes, false); 
                        textyView.textarea.setCharacterAttributes(textyModel.fontAttributes,false);
                    //}
                    textyView.italicBtn.setBackground(DEFAULT_BUTTON_BG);
                    textyView.italicBtn.setActionCommand("ToolbarItalicize");
                    textyView.textarea.requestFocusInWindow();
                    break;
                case "ToolbarUnderline":
                    /*selectedText = textyView.textarea.getSelectedText();
                    if(selectedText != null) {
                        SimpleAttributeSet newFontAttributes = new SimpleAttributeSet();
                        StyleConstants.setUnderline(newFontAttributes, true); 
                        textyView.textarea.setCharacterAttributes(newFontAttributes,false);
                    }
                    else {*/
                        StyleConstants.setUnderline(textyModel.fontAttributes, true); 
                        textyView.textarea.setCharacterAttributes(textyModel.fontAttributes,false);
                    //}
                    textyView.underlineBtn.setBackground(DEPRESSED_BUTTON_BG);
                    textyView.underlineBtn.setActionCommand("ToolbarUnline");
                    textyView.textarea.requestFocusInWindow();
                    break;
                case "ToolbarUnline":
                    /*selectedText = textyView.textarea.getSelectedText();
                    if(selectedText != null) {
                        SimpleAttributeSet newFontAttributes = new SimpleAttributeSet();
                        StyleConstants.setUnderline(newFontAttributes, false); 
                        textyView.textarea.setCharacterAttributes(newFontAttributes,false);
                    }
                    else {*/
                        StyleConstants.setUnderline(textyModel.fontAttributes, false); 
                        textyView.textarea.setCharacterAttributes(textyModel.fontAttributes,false);
                    //}
                    textyView.underlineBtn.setBackground(DEFAULT_BUTTON_BG);
                    textyView.underlineBtn.setActionCommand("ToolbarUnderline");
                    textyView.textarea.requestFocusInWindow();
                    break;
            }
        }
    }

    // Body of methods
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
            
            if(doctype.equals("rtf")) {
                RTFEditorKit rtfKit = new RTFEditorKit(); 
                newTextyEditor.textyView.textarea.setEditorKit(rtfKit); 
                FileInputStream fi = new FileInputStream(openFile); 
                rtfKit.read( fi, newTextyEditor.textyView.textarea.getDocument(), 0 ); 
            }
            else if(doctype.equals("plain")){
                String text;
                while ((text = reader.readLine()) != null) {
                    content += text;
                }
                newTextyEditor.textyView.textarea.setText(content);
            }

            TextyModel.globalFilepath = filepath;

            
            

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(textyView, "File Not Found!\nError: The file \"" + filepath + filename + "\" does not exist", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(textyView, "File Could Not Be Opened!\nError: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (BadLocationException ex) {
            Logger.getLogger(TextyEvent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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
                
                RTFEditorKit kit = new RTFEditorKit();
                StyledDocument doc = textyView.textarea.getStyledDocument();
                kit.write(fileOut, doc, 0, textyView.textarea.getDocument().getLength());

                JOptionPane.showMessageDialog(textyView, "File Saved!", "Success", JOptionPane.PLAIN_MESSAGE);
                saveSuccess = true;

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
                
                if(saveFile(saveFile)) {
                    textyView.setTitle("Texty - " + filename);
                    TextyModel.globalFilepath = filepath;
                }
                else {
                    textyModel.setFilepath(oldFilepath);
                    textyModel.setFilename(oldFilename);
                }
            }

        } catch(Exception e) {
            JOptionPane.showMessageDialog(textyView, "File was not saved! " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renameFile(String filename) {
        try {
            textyModel.setFilename(filename);
            textyModel.fileIsNew = true;
            textyView.setTitle("Texty - " + filename);
            
            TextyHelper.closeWindow(textyView.renameWin, textyView); // remove resource
        } catch(Exception e) {
            JOptionPane.showMessageDialog(textyView, "File could not be renamed!\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}