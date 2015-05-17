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
    
    // Access to Model, View and Controller (event)
    private final TextyModel textyModel;
    private final TextyView textyView;
    private final TextyEvent textyEvent;
    
    // Access to gui windows
    protected TextyView.SaveAnywayWin saveAnywayWin;
    protected TextyView.RenameFileWin renameWin;
    protected TextyView.OpenFileWin openWin;
    protected TextyView.saveLocationWin saveWin;
    
    // Menu Bar
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
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
    JTextPane textarea = new JTextPane();
    private final JScrollPane scrollpane = new JScrollPane(textarea);
    private static final int JPANEL_WIDTH_INT = 700;
    private static final int JPANEL_HEIGHT_INT = 800;
    
    public TextyView(String file, TextyModel model) {
        super("Texty - " + file);
        
        textyView = this;
        textyModel = model;
        textyEvent = new TextyEvent(this, textyModel);
        
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
        private final JPanel northPanel = new JPanel();
        private final JPanel southPanel = new JPanel();
        private final JLabel pathLabel = new JLabel("Filepath:");
        private final JLabel fileLabel = new JLabel("Filename:");
        private final JTextField filepathField;
        private final JTextField filenameField;
        private final JButton saveBtn = new JButton("Save");
        
        protected saveLocationWin() {
            super("Save New File");
            textyView.setEnabled(false);
            
            String currentDirectory = TextyModel.globalFilepath;
            String currentFilename = textyModel.getFilename();
            
            filepathField = new JTextField(currentDirectory);
            filepathField.setMargin(new Insets(0, 4, 0, 4));
            filenameField = new JTextField(currentFilename);
            filenameField.setMargin(new Insets(0, 4, 0, 4));
            
            setLocationRelativeTo(null);
            filepathField.setPreferredSize(new Dimension(340, 30));
            filenameField.setPreferredSize(new Dimension(140, 30));
            
            saveBtn.setActionCommand("SaveNew");
            
            northPanel.add(pathLabel);
            northPanel.add(filepathField);
            southPanel.add(fileLabel);
            southPanel.add(filenameField);
            southPanel.add(saveBtn);
            add(northPanel, BorderLayout.NORTH);
            add(southPanel, BorderLayout.SOUTH);
            pack();
            
            setVisible(true);
            setResizable(false);

            filenameField.requestFocusInWindow();
            
            saveBtn.addActionListener(textyEvent.new SaveEvent());
            
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    TextyHelper.closeWindow(saveWin, textyView); // remove resource
                }
            });
            
        }
        
        protected String[] getFileLocation() {
            String[] fileLocation = new String[]{filepathField.getText(), filenameField.getText()};
            return fileLocation;
        }
        
    }

    protected class SaveAnywayWin extends JFrame {    
        String filepath = textyModel.getFilepath();
        String filename = textyModel.getFilename();
        String fullFilepath = filepath + filename;
        
        private final JPanel northPanel = new JPanel();
        private final JPanel southPanel = new JPanel();
        private final JLabel alreadyExists = new JLabel("File: \"" + fullFilepath + "\" already exists. Save anyway?");
        private final JButton saveAnywayBtn = new JButton("Save Anyway");
        private final JButton cancelBtn = new JButton("Cancel");
        
        protected SaveAnywayWin() {
            super("File Already Exists");
            textyView.setEnabled(false);
            
            setLocationRelativeTo(null);
            
            saveAnywayBtn.setActionCommand("SaveAnyway");
            cancelBtn.setActionCommand("CancelSaveAnyway");
            
            northPanel.add(alreadyExists);
            southPanel.add(saveAnywayBtn);
            southPanel.add(cancelBtn);
            add(northPanel, BorderLayout.NORTH);
            add(southPanel, BorderLayout.SOUTH);
            pack();
            
            setVisible(true);
            setResizable(false);
            
            saveAnywayBtn.addActionListener(textyEvent.new SaveEvent());
            cancelBtn.addActionListener(textyEvent.new SaveEvent());
            
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    TextyHelper.closeWindow(saveAnywayWin, textyView); // remove resource
                }
            });
            
        }
    }

    protected class OpenFileWin extends JFrame {        
        private final JPanel northPanel = new JPanel();
        private final JPanel southPanel = new JPanel();
        private final JLabel pathLabel = new JLabel("Filepath:");
        private final JLabel fileLabel = new JLabel("Filename:");
        private final JTextField filepathField;
        private final JTextField filenameField;
        private final JButton openBtn = new JButton("Open");
        
        protected OpenFileWin() {
            super("Open File");
            textyView.setEnabled(false);
            
            String currentDir = TextyModel.globalFilepath;
            
            filepathField = new JTextField(currentDir);
            filepathField.setMargin(new Insets(0, 4, 0, 4));
            filenameField = new JTextField();
            filenameField.setMargin(new Insets(0, 4, 0, 4));
            
            setLocationRelativeTo(null);

            filepathField.setPreferredSize(new Dimension(340, 30));
            filenameField.setPreferredSize(new Dimension(140, 30));
            
            northPanel.add(pathLabel);
            northPanel.add(filepathField);
            southPanel.add(fileLabel);
            southPanel.add(filenameField);
            southPanel.add(openBtn);
            add(northPanel, BorderLayout.NORTH);
            add(southPanel, BorderLayout.SOUTH);
            pack();
            
            setVisible(true);
            setResizable(false);

            filenameField.requestFocusInWindow();
            
            openBtn.addActionListener(textyEvent.new OpenEvent());
            
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    TextyHelper.closeWindow(openWin, textyView); // remove resource
                }
            });
            
        }
        
        protected String[] getFileLocation() {
            String[] fileLocation = new String[]{filepathField.getText(), filenameField.getText()};
            return fileLocation;
        }

    }
    
    protected class RenameFileWin extends JFrame {       
        private final JPanel renamePanel = new JPanel();
        private final JLabel fileLabel = new JLabel("Filename:");
        private final JTextField renameField;
        private final JButton saveRenameBtn = new JButton("Rename");
        
        protected RenameFileWin() {
            super("Rename File");
            textyView.setEnabled(false);
            
            String currentFilename = textyModel.getFilename();
            renameField = new JTextField(currentFilename);
            renameField.setMargin(new Insets(0, 4, 0, 4));
            
            setLocationRelativeTo(null);
            renameField.setPreferredSize(new Dimension(140, 30));
            
            renamePanel.add(fileLabel);
            renamePanel.add(renameField);
            renamePanel.add(saveRenameBtn);
            add(renamePanel, BorderLayout.CENTER);
            pack();
            
            setVisible(true);
            setResizable(false);

            renameField.requestFocusInWindow();
            
            saveRenameBtn.addActionListener(textyEvent.new RenameEvent());
            
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    TextyHelper.closeWindow(renameWin, textyView); // remove resource
                }
            });
            
        }
        
        protected String getNewFilename() {
            return renameField.getText();
        }

    }
    
}