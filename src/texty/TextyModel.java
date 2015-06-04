package texty;

import java.io.File;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Steve Karwacki
 */
public class TextyModel {
    
    protected static final String DS = File.separator;
    protected static final String DEFAULT_FILEPATH = FileSystemView.getFileSystemView().getDefaultDirectory().toPath().toString();
    protected static final String DEFAULT_FILENAME = "New Document.txt";
    protected static String globalFilepath = DEFAULT_FILEPATH;
    private static int instanceCount = 0;
    
    protected StyledDocument styledDoc;
    private String filepath;
    private String filename;
    protected boolean fileIsNew;
    protected boolean hasStyles;
    
    public TextyModel(String[] fileLocation, boolean newFile) {
        hasStyles = false;
        fileIsNew = newFile;
        filepath = TextyHelper.getFixedPath(fileLocation[0]);
        filename = fileLocation[1];
    }
    
    // Getters and setters
    public static void removeInstance() {
        instanceCount--;
    }
    
    public static void addInstanceCount() {
        instanceCount++;
    }

    public static int getInstanceCount() {
        return instanceCount;
    }
    
    protected String getFilepath() {
        return filepath;
    }
    
    protected void setFilepath(String newPath) {
        filepath = TextyHelper.getFixedPath(newPath);
    }
    
    protected String getFilename() {
        return filename;
    }
    
    protected void setFilename(String newFilename) throws Exception {
        if(TextyHelper.containsRegxChars(newFilename,"[:\\\\/*?|<>]")) {
            throw new Exception("Illegal Filename: filenames may not contain the following characters :, \\, \\/, *, ?, |, <, or >");
        }
        else if(TextyHelper.containsRegxChars(newFilename,"[.]\\w+")){
            filename = newFilename;
        }
        else {
            throw new Exception("Illegal Filename: filename requires an extension");
        }
    }
    
}