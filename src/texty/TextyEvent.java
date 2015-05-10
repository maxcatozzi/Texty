package texty;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 *
 * @author Steve Karwacki
 */
public class TextyEvent {
    
    // Access to Model and View
    TextyModel textyModel;
    Texty textyView;
    
    public TextyEvent(Texty gui, TextyModel model) {
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
                    TextyHelper.closeWindow(textyView.saveAnywayWin); // remove resource
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
        String filepath = fileLocation[0];
        String filename = fileLocation[1];
        if(!filepath.substring(filepath.length()-1).equals("\\")) { // append backslash to filepath if filepath does not end in a backslash
            filepath += "\\";
        }
        File file = new File(filepath + filename);
        String content = "";

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String text;

            while ((text = reader.readLine()) != null) {
                content += text;
            }

            TextyHelper.closeWindow(textyView.openWin); // remove resource
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
            String content = textyView.textarea.getText();

            try(FileOutputStream fop = new FileOutputStream(file)) {

                if(!file.exists()) {
                    file.createNewFile();
                }

                byte[] contentInBytes = content.getBytes();
                fop.write(contentInBytes);

                JOptionPane.showMessageDialog(textyView, "File Saved!", "Success", JOptionPane.PLAIN_MESSAGE);

                saveSuccess = true;

            } catch(IOException e) {
                JOptionPane.showMessageDialog(textyView, "File was not saved!\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return saveSuccess;
    }
    
    private void saveNewFile(String[] fileLocation) {
        try {
            String filepath = fileLocation[0];
            String filename = fileLocation[1];
            if(!filepath.substring(filepath.length()-1).equals("\\")) { // append backslash to filepath if filepath does not end in a backslash
                filepath += "\\";
            }
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
                    TextyHelper.closeWindow(textyView.saveWin); // remove resource
                    TextyModel.globalFilepath = filepath;
                }
                else {
                    textyView.saveWin.setVisible(true);
                }
            }

        } catch(Exception e) {
        }
    }

    private void saveFileAnyway(String[] fileLocation) { 
        try {
            String filepath = fileLocation[0];
            String filename = fileLocation[1];
            if(!filepath.substring(filepath.length()-1).equals("\\")) { // append backslash to filepath if filepath does not end in a backslash
                filepath += "\\";
            }
            textyModel.setFilepath(filepath);
            textyModel.setFilename(filename);
            textyModel.fileIsNew = false;
            
            textyView.saveAnywayWin.setVisible(false);
            textyView.saveWin.setVisible(false);
            
            if(saveFile()) {
                TextyHelper.closeWindow(textyView.saveAnywayWin); // remove resource
                TextyHelper.closeWindow(textyView.saveWin); // remove resource
                TextyModel.globalFilepath = filepath;
            }
            else {
                textyView.saveAnywayWin.setVisible(true);
                textyView.saveWin.setVisible(true);
            }
            
        } catch(Exception e) {
        }
    }

    private void renameFile(String filename) {
        try {
            textyModel.setFilename(filename);
            textyModel.fileIsNew = true;
            textyView.setTitle("Texty - " + filename);
            
            TextyHelper.closeWindow(textyView.renameWin); // remove resource
        } catch(Exception e) {
            JOptionPane.showMessageDialog(textyView, "File could not be renamed!\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}