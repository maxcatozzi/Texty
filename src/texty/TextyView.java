package texty;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Steve Karwacki
 */
public class TextyView extends JFrame {
    
    private final TextyView textyView;
    
    // Access to gui windows
    protected TextyView.SaveAnywayWin saveAnywayWin;
    
    // Menu Bar
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    protected final JMenuItem newMenuBtn = new JMenuItem("New");
    protected final JMenuItem openMenuBtn = new JMenuItem("Open");
    protected final JMenuItem saveMenuBtn = new JMenuItem("Save");
    protected final JMenuItem saveasMenuBtn = new JMenuItem("Save As");
    protected final JMenuItem exitMenuBtn = new JMenuItem("Exit");
    
    // Toolbar
    protected static final Color DEFAULT_BUTTON_BG = new JButton().getBackground();
    protected static final Color DEPRESSED_BUTTON_BG = Color.LIGHT_GRAY;
    protected final JPanel toolbarPanel = new JPanel();
    protected final JToolBar toolbar = new JToolBar();
    protected final JButton boldBtn = new JButton("Bold");
    protected final JButton italicBtn = new JButton("Italic");
    protected final JButton underlineBtn = new JButton("Underline");
    
    // Dialogs
    protected final JButton saveAnywayBtn = new JButton("Save Anyway");
    protected final JButton cancelBtn = new JButton("Cancel");
    
    // Document
    private final JPanel textareaPanel = new JPanel();
    JTextPane textarea = new JTextPane();
    private final JScrollPane scrollpane = new JScrollPane(textarea);
    private static final int JPANEL_WIDTH_INT = 700;
    private static final int JPANEL_HEIGHT_INT = 800;
    
    public TextyView(String file) {
        super("Texty - " + file);
        
        textyView = this;
        
        setSize(new Dimension(JPANEL_WIDTH_INT, JPANEL_HEIGHT_INT));
        setLocationRelativeTo(null);
        toolbarPanel.setPreferredSize(new Dimension(JPANEL_WIDTH_INT, 30));
        
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(Color.GRAY, 1));
        UIManager.put("Separator.foreground", Color.LIGHT_GRAY);
        
        toolbar.setMargin(new Insets(4, 4, 0, 4));
        toolbar.setFloatable(false);
        
        textarea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        setLayout(new BorderLayout());
        toolbarPanel.setLayout(new BorderLayout());
        textareaPanel.setLayout(new BorderLayout());
        
        // set file menu button actions
        newMenuBtn.setActionCommand("FileMenuNew");
        openMenuBtn.setActionCommand("FileMenuOpen");
        saveMenuBtn.setActionCommand("FileMenuSave");
        saveasMenuBtn.setActionCommand("FileMenuSaveAs");
        exitMenuBtn.setActionCommand("FileMenuExit");
        // toolbar button actions
        boldBtn.setActionCommand("ToolbarEmbolden");
        italicBtn.setActionCommand("ToolbarItalicize");
        underlineBtn.setActionCommand("ToolbarUnderline");
        
        setJMenuBar(menuBar);
        menuBar.add(fileMenu);
        fileMenu.add(newMenuBtn);
        fileMenu.addSeparator();
        fileMenu.add(openMenuBtn);
        fileMenu.addSeparator();
        fileMenu.add(saveMenuBtn);
        fileMenu.add(saveasMenuBtn);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuBtn);
        
        add(toolbarPanel, BorderLayout.NORTH);
        toolbarPanel.add(toolbar);
        toolbar.add(boldBtn);
        toolbar.add(italicBtn);
        toolbar.add(underlineBtn);
        
        add(textareaPanel, BorderLayout.CENTER);
        textareaPanel.add(scrollpane);
        
        setVisible(true);
        textarea.requestFocusInWindow();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(TextyModel.getInstanceCount() > 1) { // check if last Texty instance
                    TextyModel.removeInstance();
                }
                else {
                    System.exit(0); // if last instance, close application
                }
            }
        });
        
    }

    protected class SaveAnywayWin extends JFrame {    
        String fullFilepath;
        
        private final JPanel northPanel = new JPanel();
        private final JPanel southPanel = new JPanel();
        private final JLabel alreadyExists;
        
        protected SaveAnywayWin(String filepath, String filename) {
            super("File Already Exists");
            textyView.setEnabled(false);
            
            setLocationRelativeTo(null);
            
            fullFilepath = filepath + filename;
            alreadyExists = new JLabel("File: \"" + fullFilepath + "\" already exists. Save anyway?");
            
            textyView.saveAnywayBtn.setActionCommand("SaveAnyway");
            textyView.cancelBtn.setActionCommand("CancelSaveAnyway");
            
            northPanel.add(alreadyExists);
            southPanel.add(textyView.saveAnywayBtn);
            southPanel.add(textyView.cancelBtn);
            add(northPanel, BorderLayout.NORTH);
            add(southPanel, BorderLayout.SOUTH);
            pack();
            
            setVisible(true);
            setResizable(false);
            
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    TextyHelper.closeWindow(saveAnywayWin, textyView); // remove resource
                }
            });
            
        }
    }
    
}