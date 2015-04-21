package texty;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author Steve Karwacki
 */
public class TextyEvent {
    
    // Access to Model and View
    TextyModel textyModel;
    Texty textyView;
    // controls for waiting for user input
    boolean waitForInput = false;
    final Thread waitThread;
    
    public TextyEvent(Texty gui, TextyModel model) {
        textyView = gui;
        textyModel = model;
        waitThread = new Thread();
    }
    
    // Event Handlers
    class SaveEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            String[] fileLocation;
            switch(command){
                case "InitSave":
                    saveFile();
                    break;
                case "SaveNew":
                    fileLocation = Texty.saveWin.getFileLocation();
                    saveNewFile(fileLocation);
                    break;
                case "SaveAnyway":
                    textyModel.saveAnyway = true;
                    waitForInput = false;
                    synchronized(waitThread) {
                        waitThread.notify();
                    }
                    break;
                case "CancelSaveAnyway":
                    waitForInput = false;
                    synchronized(waitThread) {
                        waitThread.notify();
                    }
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
                case "InitOpen":
                    Texty.openWin = textyView.new OpenFileWin();
                    break;
                case "Open":
                    fileLocation = Texty.openWin.getFileLocation();
                    openFile(fileLocation);
                    break;
            }
        }
    }

    class NewEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch(command){
                case "InitNew":
                    TextyModel textyEditor = new TextyModel();
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
                case "InitRename":
                    Texty.renameWin = textyView.new RenameFileWin();
                    break;
                case "Rename":
                    newFilename = Texty.renameWin.getNewFilename();
                    renameFile(newFilename);
                    break;
            }
        }
    }

    // Helper methods
    public static boolean containsRegxChars(String haystack, String needle) {
        Pattern pattern = Pattern.compile(needle);
        Matcher matcher = pattern.matcher(haystack);
        return matcher.find();
    }

    // Body of methods
    private void openFile(String[] fileLocation) {
        String filepath = fileLocation[0];
        String filename = fileLocation[1];
        File file = new File(filepath + filename);
        String content = "";

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String text;

            while ((text = reader.readLine()) != null) {
                content += text;
            }

            Texty.openWin.dispatchEvent(new WindowEvent(Texty.openWin, WindowEvent.WINDOW_CLOSING));
            Texty.openWin.dispose();
            TextyModel.globalFilepath = filepath;

            TextyModel textyEditor = new TextyModel(fileLocation, false);
            textyEditor.textyView.textarea.setText(content);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(textyView, "File Not Found!\nError: The file \"" + filepath + filename + "\" does not exist", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(textyView, "File Could Not Be Opened!\nError: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveNewFile(String[] fileLocation) {
        try {
            boolean continueSave = true;
            String filepath = fileLocation[0];
            String filename = fileLocation[1];
            String fullFilepath = filepath + filename;
            textyModel.setFilepath(filepath);
            textyModel.setFilename(filename);

            File file = new File(fullFilepath);
            
            /*if(file.exists()) { // breaks
                continueSave = saveFileAnyway();
            }*/

            if(continueSave) {
                textyModel.fileIsNew = false;
                Texty.saveWin.setVisible(false);

                if(saveFile()) {
                    textyView.setTitle("Texty - " + filename);
                    Texty.saveWin.dispatchEvent(new WindowEvent(Texty.saveWin, WindowEvent.WINDOW_CLOSING));
                    Texty.saveWin.dispose();
                    TextyModel.globalFilepath = filepath;
                }
                else {
                    Texty.saveWin.setVisible(true);
                }
            }

        } catch(Exception e) {
            JOptionPane.showMessageDialog(textyView, "File could not be saved!\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean saveFileAnyway() { 
        Texty.SaveAnywayWin saveAnywayWin = textyView.new SaveAnywayWin();
        waitForInput = true;
        try {
            synchronized(waitThread) { // does not work
                while(waitForInput) {
                    waitThread.wait();
                }
            }
            } catch (InterruptedException e) {
            }
        return textyModel.saveAnyway;
    }

    public boolean saveFile() { 
        String filepath;
        boolean saveSuccess = false;
        if(textyModel.fileIsNew) {
            Texty.saveWin = textyView.new saveLocationWin();
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

    private void renameFile(String filename) {
        try {
            textyModel.setFilename(filename);
            textyModel.fileIsNew = true;
            textyView.setTitle("Texty - " + filename);
            Texty.renameWin.dispatchEvent(new WindowEvent(Texty.renameWin, WindowEvent.WINDOW_CLOSING));
            Texty.renameWin.dispose();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(textyView, "File could not be renamed!\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}