package texty;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;

/**
 *
 * @author Steve Karwacki
 */
public class SaveController {
    
    TextyEvent textyEvent;
    TextyModel textyModel;
    TextyView textyView;
    
    public SaveController(TextyEvent mainEvent) {
        textyEvent = mainEvent;
        textyModel = textyEvent.textyModel;
        textyView = textyEvent.textyView;
    }
    
    protected boolean saveFile(File saveFile) {        
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
    
    protected void saveExistingFile(File saveFile) {
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
    
    protected void saveNewFile(File saveFile) {
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
