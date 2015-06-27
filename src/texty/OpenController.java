package texty;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;

/**
 *
 * @author Steve Karwacki
 */
public class OpenController {
    
    TextyEvent textyEvent;
    TextyView textyView;
    
    public OpenController(TextyEvent mainEvent) {
        textyEvent = mainEvent;
        textyView = textyEvent.textyView;
    }
    
    protected void openFile(File openFile) {
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
    
}
