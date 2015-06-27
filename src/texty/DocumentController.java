package texty;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import static texty.TextyView.DEFAULT_BUTTON_BG;
import static texty.TextyView.DEPRESSED_BUTTON_BG;

/**
 *
 * @author Steve Karwacki
 */
public class DocumentController {
    
    TextyEvent textyEvent;
    TextyModel textyModel;
    TextyView textyView;
    
    public DocumentController(TextyEvent mainEvent) {
        textyEvent = mainEvent;
        textyModel = textyEvent.textyModel;
        textyView = textyEvent.textyView;
    }
    
    public void setButtonState(JButton btn, int isActive, String btnCommand) {
        switch(isActive) {
            case 0: //false
                btn.setBackground(DEFAULT_BUTTON_BG);
                break;
            case 1: //true
                btn.setBackground(DEPRESSED_BUTTON_BG);
                break;
        }
        btn.setActionCommand(btnCommand);
    }
    
    public void setComboSelection(JComboBox combo, String selection) {
        combo.setSelectedItem(selection);
    }
    
    public void setComboSelection(JComboBox combo, int selection) {
        combo.setSelectedIndex(selection);
    }
    
    public void setTextFontSize(int fontSize) {
        textyModel.fontSize = fontSize;
        MutableAttributeSet setFontSize = new SimpleAttributeSet();
        StyleConstants.setFontSize(setFontSize, fontSize); 
        textyView.textarea.setCharacterAttributes(setFontSize, false);
    }
    
    public void setTextFontFamily(String fontName) {
        textyModel.fontName = fontName;
        MutableAttributeSet setFontName = new SimpleAttributeSet();
        StyleConstants.setFontFamily(setFontName, fontName); 
        textyView.textarea.setCharacterAttributes(setFontName, false);
    }
        
    public void setTextStyle(String style, boolean isActive) {
        final MutableAttributeSet BOLD = new SimpleAttributeSet();
        final MutableAttributeSet ITALIC = new SimpleAttributeSet();
        final MutableAttributeSet UNDERLINE = new SimpleAttributeSet();
        switch(style) {
            case "bold":
                StyleConstants.setBold(BOLD, isActive); 
                textyView.textarea.setCharacterAttributes(BOLD, false);
                break;
            case "italic":
                StyleConstants.setItalic(ITALIC, isActive); 
                textyView.textarea.setCharacterAttributes(ITALIC, false);
                break;
            case "underline":
                StyleConstants.setUnderline(UNDERLINE, isActive); 
                textyView.textarea.setCharacterAttributes(UNDERLINE, false);
                break;
        }
        if(isActive) {
            textyModel.hasStyles = true;
        }
    }
    
    public void setTypeStyles(AttributeSet attributes) {
        boolean isBold = StyleConstants.isBold(attributes);
        boolean isItalic = StyleConstants.isItalic(attributes);
        boolean isUnderlined = StyleConstants.isUnderline(attributes);
        String textFontFamily = StyleConstants.getFontFamily(attributes);
        int textFontSize = StyleConstants.getFontSize(attributes);
        
        if(isBold) {
            setButtonState(textyView.boldBtn, 1, "ToolbarUnbold");
            setTextStyle("bold", true);
        }
        else {
            setButtonState(textyView.boldBtn, 0, "ToolbarEmbolden");
            setTextStyle("bold", false);
        }
        
        if(isItalic) {
            setButtonState(textyView.italicBtn, 1, "ToolbarDetalicize");
            setTextStyle("italic", true);
        }
        else {
            setButtonState(textyView.italicBtn, 0, "ToolbarItalicize");
            setTextStyle("italic", false);
        }
        
        if(isUnderlined) {
            setButtonState(textyView.underlineBtn, 1, "ToolbarUnline");
            setTextStyle("underline", true);
        }
        else {
            setButtonState(textyView.underlineBtn, 0, "ToolbarUnderline");
            setTextStyle("underline", false);
        }
        
        setComboSelection(textyView.fontFamilyChooser, textFontFamily);
        setTextFontFamily(textFontFamily);
        setComboSelection(textyView.fontSizeChooser, Integer.toString(textFontSize));
        setTextFontSize(textFontSize);
        
    }
    
    public void setSelectionStyles(int pos1, int pos2) {
        // order selectionStart and selectionEnd
        int selectionStart;
        int selectionEnd;
        if(pos1 < pos2) {
            selectionStart = pos1;
            selectionEnd = pos2;
        }
        else {
            selectionStart = pos2;
            selectionEnd = pos1;
        }
        
        int isBold = getSelectionStyleContinuity("bold", selectionStart, selectionEnd);
        int isItalic = getSelectionStyleContinuity("italic", selectionStart, selectionEnd);
        int isUnderline = getSelectionStyleContinuity("underline", selectionStart, selectionEnd);
        String selectionFontFamily = getSelectionFontContinuity("font", selectionStart, selectionEnd);
        String selectionFontSize = getSelectionFontContinuity("size", selectionStart, selectionEnd);
        
        switch(isBold) {
            case 0:
                setButtonState(textyView.boldBtn, 0, "ToolbarEmbolden");
                setTextStyle("bold", false);
                break;
            case 1:
                setButtonState(textyView.boldBtn, 1, "ToolbarUnbold");
                setTextStyle("bold", true);
                break;
            case 2:
                setButtonState(textyView.boldBtn, 0, "ToolbarEmbolden");
                break;
        }
        
        switch(isItalic) {
            case 0:
                setButtonState(textyView.italicBtn, 0, "ToolbarItalicize");
                setTextStyle("italic", false);
                break;
            case 1:
                setButtonState(textyView.italicBtn, 1, "ToolbarDetalicize");
                setTextStyle("italic", true);
                break;
            case 2:
                setButtonState(textyView.italicBtn, 0, "ToolbarItalicize");
                break;
        }
        
        switch(isUnderline) {
            case 0:
                setButtonState(textyView.underlineBtn, 0, "ToolbarUnderline");
                setTextStyle("underline", false);
                break;
            case 1:
                setButtonState(textyView.underlineBtn, 1, "ToolbarUnline");
                setTextStyle("underline", true);
                break;
            case 2:
                setButtonState(textyView.underlineBtn, 0, "ToolbarUnderline");
                break;
        }
        
        if(!selectionFontFamily.equals("")) {
            setComboSelection(textyView.fontFamilyChooser, selectionFontFamily);
            setTextFontFamily(selectionFontFamily);
        }
        else setComboSelection(textyView.fontFamilyChooser, 0);
        
        if(!selectionFontSize.equals("")) {
            setComboSelection(textyView.fontSizeChooser, selectionFontSize);
            setTextFontSize(Integer.parseInt(selectionFontSize));
        }
        else {
            setComboSelection(textyView.fontSizeChooser, 0);
        }
        
        
    }
    
    // check if entire selected string has same styles
    public int getSelectionStyleContinuity(String style, int selectionStart, int selectionEnd) {
        int totalChars = selectionEnd - selectionStart;
        int styledChars = 0;
        AttributeSet attributes;
        boolean isStyled;
        for(int i = selectionStart; i < selectionEnd; i++) {
            Element charElement = textyModel.styledDoc.getCharacterElement(i);
            attributes = charElement.getAttributes();
            switch(style) {
                case "bold":
                    isStyled = StyleConstants.isBold(attributes);
                    break;
                case "italic":
                    isStyled = StyleConstants.isItalic(attributes);
                    break;
                case "underline":
                    isStyled = StyleConstants.isUnderline(attributes);
                    break;
                default:
                    isStyled = false;
                    break;
            }
            if(isStyled) styledChars++;
        }
        if(styledChars == 0) return 0; // if no characters are styled
        else if(styledChars == totalChars) return 1; // if all characters are styled
        else return 2; // if there is a mix of styled and unstyled characters
    }
    
    // check if entire selected string has same font family/size
    public String getSelectionFontContinuity(String checkType, int selectionStart, int selectionEnd) {
        int totalChars = selectionEnd - selectionStart;
        int sameTypeChars = 0;
        AttributeSet attributes;
        String fontStyle = "";
        String currentCharFontStyle;
        for(int i = selectionStart; i < selectionEnd; i++) {
            Element charElement = textyModel.styledDoc.getCharacterElement(i);
            attributes = charElement.getAttributes();
            switch(checkType) {
                case "font":
                    currentCharFontStyle = StyleConstants.getFontFamily(attributes);
                    if(i == selectionStart) fontStyle = currentCharFontStyle; // get font of first character to be compared to rest of string
                    break;
                case "size":
                    currentCharFontStyle = Integer.toString(StyleConstants.getFontSize(attributes));
                    if(i == selectionStart) fontStyle = currentCharFontStyle; // get size of first character to be compared to rest of string
                    break;
                default:
                    currentCharFontStyle = "";
                    break;
            }
            if(fontStyle.equals(currentCharFontStyle)) {
                sameTypeChars++;
            }
        }
        if(sameTypeChars == totalChars) return fontStyle;
        else return "";
    }
    
}
