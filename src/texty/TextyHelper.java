package texty;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import static texty.TextyModel.DS;

/**
 *
 * @author Steve Karwacki
 */
public class TextyHelper {
    
    public static boolean containsRegxChars(String haystack, String needle) {
        Pattern pattern = Pattern.compile(needle);
        Matcher matcher = pattern.matcher(haystack);
        return matcher.find();
    }
    
    public static void closeWindow(JFrame closeWindow, JFrame enableWindow) {
        closeWindow.dispose();
        enableWindow.setEnabled(true);
        enableWindow.setVisible(true);
    }
     
    public static String getFixedPath(String path) {
        String filepath;
        
        // check if path has filename
        String endpath = path.substring(path.lastIndexOf(File.separator), path.length());
        if(endpath.contains(".")) {
            path = path.substring(0,path.lastIndexOf(File.separator));
        }
        
        if(!path.substring(path.length()-1).equals(DS)) { // append backslash to filepath if filepath does not end in a backslash
            filepath = path + DS;
        }
        else {
            filepath = path;
        }
        return filepath;
    }
    
    public static String getFileExtension(String filename) {
        try {
            if(filename.contains(".")) {
                return filename.substring(filename.lastIndexOf(".") + 1);
            }
            else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }

    }
    
}
