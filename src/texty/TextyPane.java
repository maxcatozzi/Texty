/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package texty;

import java.io.IOException;
import java.io.StringWriter;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

/**
 *
 * @author Steve
 */
public class TextyPane extends JTextPane {
    
    public final void setTextyCharacterAttributes(AttributeSet attr, boolean replace) {
        TextyPane editor = this;
        int p0 = editor.getSelectionStart();
        int p1 = editor.getSelectionEnd();
        if (p0 != p1) {
            StyledDocument doc = (StyledDocument) editor.getDocument();
            doc.setCharacterAttributes(p0, p1 - p0, attr, replace);
        }
        StyledEditorKit k = (StyledEditorKit) editor.getEditorKit();
        MutableAttributeSet inputAttributes = k.getInputAttributes();
        if (replace) {
            inputAttributes.removeAttributes(inputAttributes);
        }
        inputAttributes.addAttributes(attr);
    }
    
    @Override
    public StyledDocument getStyledDocument() {
        return (StyledDocument) getDocument();
    }
    
    public String getText(int beginIndex, int endIndex) {
        String txt;
        try {
            StringWriter buf = new StringWriter();
            write(buf);
            txt = buf.toString();
            txt = txt.substring(beginIndex, endIndex);
        } catch (IOException ioe) {
            txt = null;
        }
        return txt;
    }
    
}
