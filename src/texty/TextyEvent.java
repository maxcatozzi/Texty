/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class TextyEvent implements ActionListener {
    
    Texty textyView;
    TextyModel textyModel;
    
    public TextyEvent(Texty gui, TextyModel model) {
        textyView = gui;
        textyModel = model;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        String[] fileLocation;
        String newFilename;
        switch(command){
            case "Open":
                Texty.openWin = textyView.new OpenFileWin();
                break;
            case "Open File":
                fileLocation = Texty.openWin.getFileLocation();
                openFile(fileLocation);
                break;
            case "Save":
                saveFile();
                break;
            case "Save File":
                fileLocation = Texty.saveWin.getFileLocation();
                saveNewFile(fileLocation);
                break;
            case "Rename":
                Texty.renameWin = textyView.new RenameFileWin();
                break;
            case "Rename File":
                newFilename = Texty.renameWin.getNewFilename();
                renameFile(newFilename);
                break;
        }
    }
    
    public static boolean containsRegxChars(String haystack, String needle) {
        Pattern pattern = Pattern.compile(needle);
        Matcher matcher = pattern.matcher(haystack);
        return matcher.find();
    }
    
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
            Texty.openWin = null;
            TextyModel.globalFilepath = filepath;
            
            TextyModel textyEdit = new TextyModel(fileLocation, false);
            textyEdit.textyView.textarea.setText(content);
            
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(textyView, "File Not Found!\nError: The file \"" + filepath + filename + "\" does not exist", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(textyView, "File Could Not Be Opened!\nError: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveNewFile(String[] fileLocation) {
        try {
            String filepath = fileLocation[0];
            String filename = fileLocation[1];
            textyModel.setFilepath(filepath);
            textyModel.setFilename(filename);
            
            textyModel.fileIsNew = false;
            Texty.saveWin.setVisible(false);
            
            if(saveFile()) {
                textyView.setTitle("Texty - " + filename);
                Texty.saveWin.dispatchEvent(new WindowEvent(Texty.saveWin, WindowEvent.WINDOW_CLOSING));
                Texty.saveWin = null;
                TextyModel.globalFilepath = filepath;
            }
            else {
                Texty.saveWin.setVisible(true);
            }
            
        } catch(Exception e) {
            JOptionPane.showMessageDialog(textyView, "File could not be saved!\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean saveFile() { 
        String filepath;
        boolean success = false;
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
                
                success = true;

            } catch(IOException e) {
                JOptionPane.showMessageDialog(textyView, "File was not saved!\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return success;
    }

    private void renameFile(String filename) {
        try {
            textyModel.setFilename(filename);
            textyView.setTitle("Texty - " + filename);
            Texty.renameWin.dispatchEvent(new WindowEvent(Texty.renameWin, WindowEvent.WINDOW_CLOSING));
            Texty.renameWin = null;
        } catch(Exception e) {
            JOptionPane.showMessageDialog(textyView, "File could not be renamed!\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}