package texty;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

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
            String[] fileLocation;
            switch(command){
                case "FileMenuOpen":
                    textyView.openWin = textyView.new OpenFileWin();
                    break;
                case "Open":
                    fileLocation = textyView.openWin.getFileLocation();
                    openFile(fileLocation);
                    break;
            }
        }
    }
    
    class SaveEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            String[] fileLocation;
            switch(command){
                case "FileMenuSave":
                    saveFile();
                    break;
                case "SaveNew":
                    fileLocation = textyView.saveWin.getFileLocation();
                    saveNewFile(fileLocation);
                    break;
                case "SaveAnyway":
                    fileLocation = textyView.saveWin.getFileLocation();
                    saveFileAnyway(fileLocation);
                    break;
                case "CancelSaveAnyway":
                    TextyHelper.closeWindow(textyView.saveAnywayWin, textyView); // remove resource
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

    // Body of methods
    private void openFile(String[] fileLocation) {
        String filepath = TextyHelper.fixPath(fileLocation[0]);
        String filename = fileLocation[1];
        File file = new File(filepath + filename);
        String content = "";

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String text;

            while ((text = reader.readLine()) != null) {
                content += text;
            }

            TextyHelper.closeWindow(textyView.openWin, textyView); // remove resource
            TextyModel.globalFilepath = filepath;

            TextyModel textyEditor = new TextyModel(fileLocation, false);
            textyEditor.textyView.textarea.setText(content);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(textyView, "File Not Found!\nError: The file \"" + filepath + filename + "\" does not exist", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(textyView, "File Could Not Be Opened!\nError: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean saveFile() { 
        String filepath;
        boolean saveSuccess = false;
        if(textyModel.fileIsNew) {
            textyView.saveWin = textyView.new saveLocationWin();
        }
        else {

            filepath = textyModel.getFilepath() + textyModel.getFilename();
            File file = new File(filepath);

            try(BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(file))) {

                if(!file.exists()) {
                    file.createNewFile();
                }
                
                RTFEditorKit kit = new RTFEditorKit();
                //String content = textyView.textarea.getDocument().getText(0, textyView.textarea.getDocument().getLength());
                
                StyledDocument doc = textyView.textarea.getStyledDocument();
                /*Style colorStyle = textyView.textarea.addStyle("Color", null);
                StyleConstants.setForeground(colorStyle, Color.blue);
                
                doc.remove(0, textyView.textarea.getDocument().getLength());
                doc.insertString(0, content, colorStyle);*/
                
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
    
    private void saveNewFile(String[] fileLocation) {
        try {
            String filepath = TextyHelper.fixPath(fileLocation[0]);
            String filename = fileLocation[1];
            String fullFilepath = filepath + filename;
            File file = new File(fullFilepath);
            
            if(file.exists()) {
                textyView.saveAnywayWin = textyView.new SaveAnywayWin();
            }
            else {
                textyModel.setFilepath(filepath);
                textyModel.setFilename(filename);
                textyModel.fileIsNew = false;
                textyView.saveWin.setVisible(false);

                if(saveFile()) {
                    textyView.setTitle("Texty - " + filename);
                    TextyHelper.closeWindow(textyView.saveWin, textyView); // remove resource
                    TextyModel.globalFilepath = filepath;
                }
                else {
                    textyView.saveWin.setVisible(true);
                }
            }

        } catch(Exception e) {
            textyView.saveWin.setVisible(true);
        }
    }

    private void saveFileAnyway(String[] fileLocation) { 
        try {
            String filepath = TextyHelper.fixPath(fileLocation[0]);
            String filename = fileLocation[1];
            textyModel.setFilepath(filepath);
            textyModel.setFilename(filename);
            textyModel.fileIsNew = false;
            
            textyView.saveAnywayWin.setVisible(false);
            textyView.saveWin.setVisible(false);
            
            if(saveFile()) {
                TextyHelper.closeWindow(textyView.saveAnywayWin, textyView); // remove resource
                TextyHelper.closeWindow(textyView.saveWin, textyView); // remove resource
                TextyModel.globalFilepath = filepath;
            }
            else {
                textyView.saveWin.setVisible(true);
            }
            
        } catch(Exception e) {
            textyView.saveWin.setVisible(true);
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