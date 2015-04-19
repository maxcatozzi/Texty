/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package texty;

/**
 *
 * @author Steve
 */
public class TextyModel {
    
    public static final String DEFAULT_FILEPATH = "C:\\Users\\Steve\\Documents\\Work\\Java\\Texty\\";
    public static final String DEFAULT_FILENAME = "New Document.txt";
    public static String globalFilepath = DEFAULT_FILEPATH;
    private static int instanceCount = 0;
    
    protected Texty textyView;
    private String filepath;
    private String filename;
    protected boolean fileIsNew;
    
    public TextyModel() {
        TextyModel textyModel = new TextyModel(new String[]{DEFAULT_FILEPATH,DEFAULT_FILENAME}, true);
    }
    
    public TextyModel(String[] fileLocation, boolean newFile) {
        fileIsNew = newFile;
        filepath = fileLocation[0];
        filename = fileLocation[1];
        textyView = new Texty(filename, this);
        instanceCount++;
    }
    
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
        if(TextyEvent.containsRegxChars(newFilename,"[:\\\\/*?|<>]")) {
            throw new Exception("Illegal Filename: filenames may not contain the following characters :, \\, \\/, *, ?, |, <, or >");
        }
        else if(TextyEvent.containsRegxChars(newFilename,"[.]\\w+")){
            filename = newFilename;
        }
        else {
            throw new Exception("Illegal Filename: filename requires an extension");
        }
    }
    
}