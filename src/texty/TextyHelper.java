package texty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;

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
    
}
