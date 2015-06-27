package texty;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.Element;

/**
 *
 * @author Steve Karwacki
 */
public class TextyEvent {
    
    // Access to Model/View/Controllers
    TextyModel textyModel;
    TextyView textyView;
    OpenController openController;
    SaveController saveController;
    DocumentController documentController;
    
    public TextyEvent() {
        String filepath = TextyModel.DEFAULT_FILEPATH;
        String filename = TextyModel.DEFAULT_FILENAME;
        TextyEvent textyEditor = new TextyEvent(new String[]{filepath,filename}, true);
    }
    
    public TextyEvent(String[] fileLocation, boolean newFile) {
        textyModel = new TextyModel(fileLocation, newFile);
        textyView = new TextyView(fileLocation[1]);
        TextyModel.addInstanceCount();
        
        openController = new OpenController(this);
        saveController = new SaveController(this);
        documentController = new DocumentController(this);
        
        if(newFile) {
            textyModel.styledDoc = textyView.textarea.getStyledDocument();
            textyModel.fontName = (String)textyView.fontFamilyChooser.getSelectedItem();
            textyModel.fontSize = Integer.parseInt((String) textyView.fontSizeChooser.getSelectedItem());
            documentController.setTextFontFamily(textyModel.fontName);
            documentController.setTextFontSize(textyModel.fontSize);
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
                        openController.openFile(file);
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
                    saveController.saveFile(saveFile);
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
                            saveController.saveExistingFile(saveFile);
                        }
                        else {
                            saveController.saveFile(saveFile);
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
                documentController.setTypeStyles(attributes);
            }
            else {
                documentController.setSelectionStyles(mark, dot);
            }
        }    
    }
    
    class ToolbarEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {   
            if(e.getSource() == textyView.fontFamilyChooser) {
                Caret cursorPos = textyView.textarea.getCaret();
                String fontName = (String) textyView.fontFamilyChooser.getSelectedItem();
                if(!fontName.equals("- Choose Font -")) documentController.setTextFontFamily(fontName);
                else if(cursorPos.getDot() == cursorPos.getMark()) textyView.fontFamilyChooser.setSelectedItem(textyModel.fontName);
            }
            else if(e.getSource() == textyView.fontSizeChooser) {
                Caret cursorPos = textyView.textarea.getCaret();
                String fontSizeString = (String) textyView.fontSizeChooser.getSelectedItem();
                if(!fontSizeString.equals("- -")) {
                    int fontSize = Integer.parseInt(fontSizeString);
                    documentController.setTextFontSize(fontSize);
                }
                else if(cursorPos.getDot() == cursorPos.getMark()) textyView.fontSizeChooser.setSelectedItem(Integer.toString(textyModel.fontSize));
            }
            else { // buttons
                String command = e.getActionCommand();
                switch(command){
                    case "ToolbarEmbolden":
                        documentController.setButtonState(textyView.boldBtn, 1, "ToolbarUnbold");
                        documentController.setTextStyle("bold", true);
                        break;
                    case "ToolbarUnbold":
                        documentController.setButtonState(textyView.boldBtn, 0, "ToolbarEmbolden");
                        documentController.setTextStyle("bold", false);
                        break;
                    case "ToolbarItalicize":
                        documentController.setButtonState(textyView.italicBtn, 1, "ToolbarDetalicize");
                        documentController.setTextStyle("italic", true);
                        break;
                    case "ToolbarDetalicize":
                        documentController.setButtonState(textyView.italicBtn, 0, "ToolbarItalicize");
                        documentController.setTextStyle("italic", false);
                        break;
                    case "ToolbarUnderline":
                        documentController.setButtonState(textyView.underlineBtn, 1, "ToolbarUnline");
                        documentController.setTextStyle("underline", true);
                        break;
                    case "ToolbarUnline":
                        documentController.setButtonState(textyView.underlineBtn, 0, "ToolbarUnderline");
                        documentController.setTextStyle("underline", false);
                        break;
                }
            }
            textyView.textarea.requestFocusInWindow();
        }
    }

}