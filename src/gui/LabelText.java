package gui;

import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Text to be used for a label. It is a read-only, backgroundless, borderless text pane. 
 * @author Adam Campbell
 */
@SuppressWarnings("serial")
public class LabelText extends JTextPane {
	public LabelText(String text){
		this(text, false, 12);
	}
	
	public LabelText(String text, boolean isUnderlined){
		this(text, isUnderlined, 12);
	}
	
	/**
	 * Construct a LabelText with the given parameters
	 * @param text
	 * The text for the label
	 * @param isUnderlined
	 * Whether or not the text should be underlined
	 * @param fontSize
	 * The size of the font
	 */
	public LabelText(String text, boolean isUnderlined, int fontSize){
//		setText(text);
		setBorder(null);
		setBackground(null);
		
		setFocusable(false);
		
		setEditable(true);
		Document doc = getDocument();
		
		setCaretPosition(doc.getLength());
		replaceSelection(text);
	    setCaretPosition(doc.getLength());
	    
	    setEditable(false);
		
		setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
		
		if(isUnderlined){
			//change the attributeset for the text pane to make the text underlined
			SimpleAttributeSet attributes = new SimpleAttributeSet();
			StyleConstants.setUnderline(attributes, true);
			getStyledDocument().setCharacterAttributes(0, text.length(), attributes, true);
		}
	}
}
