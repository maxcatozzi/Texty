package texty;

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
    
    public static void closeWindow(JFrame window) {
        window.dispose();
    }
    
    public static String fixPath(String path) {
        String filepath;
        if(!path.substring(path.length()-1).equals(DS)) { // append backslash to filepath if filepath does not end in a backslash
            filepath = path + DS;
        }
        else {
            filepath = path;
        }
        return filepath;
    }
    
}
