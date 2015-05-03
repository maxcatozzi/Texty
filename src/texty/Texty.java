package texty;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Steve Karwacki
 */
public class Texty extends JFrame {
    
    // Access to Model and Controller (event)
    private final TextyModel textyModel;
    private final TextyEvent textyEvent;
    
    // Access to gui windows
    protected Texty.SaveAnywayWin saveAnywayWin;
    protected Texty.RenameFileWin renameWin;
    protected Texty.OpenFileWin openWin;
    protected Texty.saveLocationWin saveWin;
    
    // Menu Bar
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    private final JSeparator sep = new JSeparator();  
    private final JMenuItem newMenuBtn = new JMenuItem("New");
    private final JMenuItem openMenuBtn = new JMenuItem("Open");
    private final JMenuItem saveMenuBtn = new JMenuItem("Save");
    private final JMenuItem renameMenuBtn = new JMenuItem("Rename");
    private final JMenuItem exitMenuBtn = new JMenuItem("Exit");
    
    // Toolbar
    private final JPanel toolbarPanel = new JPanel();
    private final JToolBar toolbar = new JToolBar();
    
    // Document
    private final JPanel textareaPanel = new JPanel();
    JTextArea textarea = new JTextArea();
    private final JScrollPane scrollpane = new JScrollPane(textarea);
    private static final int JPANEL_WIDTH_INT = 700;
    private static final int JPANEL_HEIGHT_INT = 800;
    
    public Texty(String file, TextyModel model) {
        super("Texty - " + file);
        
        textyModel = model;
        textyEvent = new TextyEvent(this, textyModel);
        
        setSize(new Dimension(JPANEL_WIDTH_INT, JPANEL_HEIGHT_INT));
        setLocationRelativeTo(null);
        toolbarPanel.setPreferredSize(new Dimension(JPANEL_WIDTH_INT, 40));
        
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(Color.black, 1));
        
        toolbar.setMargin(new Insets(4, 4, 0, 4));
        toolbar.setFloatable(false);
        
        textarea.setBorder(new EmptyBorder(10, 10, 10, 10));
        textarea.setLineWrap(true);
        textarea.setWrapStyleWord(true);
        
        setLayout(new BorderLayout());
        toolbarPanel.setLayout(new BorderLayout());
        textareaPanel.setLayout(new BorderLayout());
        
        // set file menu button actions
        
        newMenuBtn.setActionCommand("FileMenuNew");
        openMenuBtn.setActionCommand("FileMenuOpen");
        saveMenuBtn.setActionCommand("FileMenuSave");
        renameMenuBtn.setActionCommand("FileMenuRename");
        exitMenuBtn.setActionCommand("FileMenuExit");
        
        setJMenuBar(menuBar);
        menuBar.add(fileMenu);
        fileMenu.add(newMenuBtn);
        fileMenu.addSeparator();
        fileMenu.add(openMenuBtn);
        fileMenu.addSeparator();
        fileMenu.add(saveMenuBtn);
        fileMenu.addSeparator();
        fileMenu.add(renameMenuBtn);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuBtn);
        add(toolbarPanel, BorderLayout.NORTH);
        toolbarPanel.add(toolbar);
        add(textareaPanel, BorderLayout.CENTER);
        textareaPanel.add(scrollpane);
        
        // action listeners
        saveMenuBtn.addActionListener(textyEvent.new SaveEvent());
        openMenuBtn.addActionListener(textyEvent.new OpenEvent());
        newMenuBtn.addActionListener(textyEvent.new NewEvent());
        renameMenuBtn.addActionListener(textyEvent.new RenameEvent());
        exitMenuBtn.addActionListener(textyEvent.new ExitEvent());
        
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
    
    protected class saveLocationWin extends JFrame {
        private static final int SAVEWINDOW_JPANEL_WIDTH = 440;
        private static final int SAVEWINDOW_JPANEL_HEIGHT = 80;
        private static final int SAVEWINDOW_VERTICAL_PADDING = 10;
        
        private final JPanel savePanel = new JPanel();
        private final JTextField filepathField;
        private final JTextField filenameField;
        private final JButton saveBtn = new JButton("Save");
        
        protected saveLocationWin() {
            super("Save New File");
            
            String currentDirectory = TextyModel.globalFilepath;
            String currentFilename = textyModel.getFilename();
            
            filepathField = new JTextField(currentDirectory);
            filepathField.setMargin(new Insets(0, 4, 0, 4));
            filenameField = new JTextField(currentFilename);
            filenameField.setMargin(new Insets(0, 4, 0, 4));
            
            setSize(new Dimension(SAVEWINDOW_JPANEL_WIDTH + 60, SAVEWINDOW_JPANEL_HEIGHT + 50));
            setLocationRelativeTo(null);
            savePanel.setPreferredSize(new Dimension(SAVEWINDOW_JPANEL_WIDTH, SAVEWINDOW_JPANEL_HEIGHT));
            filepathField.setPreferredSize(new Dimension(SAVEWINDOW_JPANEL_WIDTH - 100, SAVEWINDOW_JPANEL_HEIGHT / 2 - SAVEWINDOW_VERTICAL_PADDING));
            filenameField.setPreferredSize(new Dimension(SAVEWINDOW_JPANEL_WIDTH - 300, SAVEWINDOW_JPANEL_HEIGHT / 2 - SAVEWINDOW_VERTICAL_PADDING));
            saveBtn.setPreferredSize(new Dimension(saveBtn.getPreferredSize().width, SAVEWINDOW_JPANEL_HEIGHT / 2 - SAVEWINDOW_VERTICAL_PADDING - 2));
            
            saveBtn.setActionCommand("SaveNew");
            
            savePanel.add(filepathField);
            savePanel.add(filenameField);
            savePanel.add(saveBtn);
            add(savePanel, BorderLayout.CENTER);

            setVisible(true);

            filenameField.requestFocusInWindow();
            
            saveBtn.addActionListener(textyEvent.new SaveEvent());
            
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    TextyHelper.closeWindow(saveWin); // remove resource
                }
            });
            
        }
        
        protected String[] getFileLocation() {
            String[] fileLocation = new String[]{filepathField.getText(), filenameField.getText()};
            return fileLocation;
        }
        
    }

    protected class SaveAnywayWin extends JFrame {
        
        private static final int SAVEANYWAY_WINDOW_JPANEL_WIDTH = 300;
        private static final int SAVEANYWAY_WINDOW_JPANEL_HEIGHT = 60;
        
        boolean saveAnyway;
        
        String filename = textyModel.getFilename();
        private final JPanel saveAnywayPanel = new JPanel();
        private final JLabel alreadyExists = new JLabel("File: " + filename + " already exists. Save anyway?");
        private final JButton saveAnywayBtn = new JButton("Save Anyway");
        private final JButton cancelBtn = new JButton("Cancel");
        
        protected SaveAnywayWin() {
            super("File Already Exists");
            
            setSize(new Dimension(SAVEANYWAY_WINDOW_JPANEL_WIDTH + 60, SAVEANYWAY_WINDOW_JPANEL_HEIGHT + 50));
            setLocationRelativeTo(null);
            
            saveAnywayBtn.setActionCommand("SaveAnyway");
            cancelBtn.setActionCommand("CancelSaveAnyway");
            
            saveAnywayPanel.add(alreadyExists);
            saveAnywayPanel.add(saveAnywayBtn);
            saveAnywayPanel.add(cancelBtn);
            add(saveAnywayPanel, BorderLayout.CENTER);

            setVisible(true);
            
            saveAnywayBtn.addActionListener(textyEvent.new SaveEvent());
            cancelBtn.addActionListener(textyEvent.new SaveEvent());
            
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    TextyHelper.closeWindow(saveAnywayWin); // remove resource
                }
            });
            
        }
    }

    protected class OpenFileWin extends JFrame {
        
        private static final int OPENWINDOW_JPANEL_WIDTH = 440;
        private static final int OPENWINDOW_JPANEL_HEIGHT = 80;
        private static final int OPENWINDOW_VERTICAL_PADDING = 10;
        
        private final JPanel openPanel = new JPanel();
        private final JTextField filepathField;
        private final JTextField filenameField;
        private final JButton openBtn = new JButton("Open");
        
        protected OpenFileWin() {
            super("Open File");
            
            String currentDir = TextyModel.globalFilepath;
            
            filepathField = new JTextField(currentDir);
            filepathField.setMargin(new Insets(0, 4, 0, 4));
            filenameField = new JTextField();
            filenameField.setMargin(new Insets(0, 4, 0, 4));
            
            setSize(new Dimension(OPENWINDOW_JPANEL_WIDTH + 60, OPENWINDOW_JPANEL_HEIGHT + 50));
            setLocationRelativeTo(null);
            openPanel.setPreferredSize(new Dimension(OPENWINDOW_JPANEL_WIDTH, OPENWINDOW_JPANEL_HEIGHT));
            filepathField.setPreferredSize(new Dimension(OPENWINDOW_JPANEL_WIDTH - 100, OPENWINDOW_JPANEL_HEIGHT / 2 - OPENWINDOW_VERTICAL_PADDING));
            filenameField.setPreferredSize(new Dimension(OPENWINDOW_JPANEL_WIDTH - 300, OPENWINDOW_JPANEL_HEIGHT / 2 - OPENWINDOW_VERTICAL_PADDING));
            openBtn.setPreferredSize(new Dimension(openBtn.getPreferredSize().width, OPENWINDOW_JPANEL_HEIGHT / 2 - OPENWINDOW_VERTICAL_PADDING - 2));
            
            openPanel.add(filepathField);
            openPanel.add(filenameField);
            openPanel.add(openBtn);
            add(openPanel, BorderLayout.CENTER);

            setVisible(true);

            filenameField.requestFocusInWindow();
            
            openBtn.addActionListener(textyEvent.new OpenEvent());
            
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    TextyHelper.closeWindow(openWin); // remove resource
                }
            });
            
        }
        
        protected String[] getFileLocation() {
            String[] fileLocation = new String[]{filepathField.getText(), filenameField.getText()};
            return fileLocation;
        }

    }
    
    protected class RenameFileWin extends JFrame {
        
        private static final int RENAMEWINDOW_JPANEL_WIDTH = 240;
        private static final int RENAMEWINDOW_JPANEL_HEIGHT = 30;
        
        private final JPanel renamePanel = new JPanel();
        private final JTextField renameField;
        private final JButton saveRenameBtn = new JButton("Rename");
        
        protected RenameFileWin() {
            super("Rename File");
            
            String currentFilename = textyModel.getFilename();
            renameField = new JTextField(currentFilename);
            renameField.setMargin(new Insets(0, 4, 0, 4));
            
            setSize(new Dimension(RENAMEWINDOW_JPANEL_WIDTH + 60, RENAMEWINDOW_JPANEL_HEIGHT + 50));
            setLocationRelativeTo(null);
            renamePanel.setPreferredSize(new Dimension(RENAMEWINDOW_JPANEL_WIDTH, RENAMEWINDOW_JPANEL_HEIGHT));
            renameField.setPreferredSize(new Dimension(RENAMEWINDOW_JPANEL_WIDTH - 100, RENAMEWINDOW_JPANEL_HEIGHT));
            saveRenameBtn.setPreferredSize(new Dimension(saveRenameBtn.getPreferredSize().width, RENAMEWINDOW_JPANEL_HEIGHT - 2));

            renamePanel.add(renameField);
            renamePanel.add(saveRenameBtn);
            add(renamePanel, BorderLayout.CENTER);

            setVisible(true);

            renameField.requestFocusInWindow();
            
            saveRenameBtn.addActionListener(textyEvent.new RenameEvent());
            
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    TextyHelper.closeWindow(renameWin); // remove resource
                }
            });
            
        }
        
        protected String getNewFilename() {
            return renameField.getText();
        }

    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TextyModel textyEditor = new TextyModel();
    }
    
}