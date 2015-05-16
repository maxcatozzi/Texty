package texty;

import java.io.File;

/**
 *
 * @author Steve Karwacki
 */
public class TextyModel {
    
    // Access to View
    protected TextyView textyView;
    
    public static final String DS = File.separator;
    public static final String DEFAULT_FILEPATH = "C:"+DS+"Users"+DS+"Steve"+DS+"Documents"+DS+"Work"+DS+"Java"+DS+"Texty"+DS;
    public static final String DEFAULT_FILENAME = "New Document.txt";
    public static String globalFilepath = DEFAULT_FILEPATH;
    private static int instanceCount = 0;
    
    private String filepath;
    private String filename;
    protected boolean fileIsNew;
    protected boolean saveAnyway;
    
    public TextyModel() {
        this.saveAnyway = false;
        TextyModel textyEditor = new TextyModel(new String[]{DEFAULT_FILEPATH,DEFAULT_FILENAME}, true);
    }
    
    public TextyModel(String[] fileLocation, boolean newFile) {
        this.saveAnyway = false;
        fileIsNew = newFile;
        filepath = fileLocation[0];
        filename = fileLocation[1];
        textyView = new TextyView(filename, this);
        instanceCount++;
    }
    
    // Getters and setters
    public static void removeInstance() {
        instanceCount--;
    }

    public static int getInstanceCount() {
        return instanceCount;
    }
    
    protected String getFilepath() {
        return filepath;
    }
    
    protected void setFilepath(String newPath) {
        filepath = newPath;
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