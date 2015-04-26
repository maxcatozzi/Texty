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
    
    private static final int JPANEL_WIDTH_INT = 700;
    private static final int JPANEL_HEIGHT_INT = 800;
    private final JPanel toolbarPanel = new JPanel();
    private final JPanel textareaPanel = new JPanel();
    private final JToolBar toolbar = new JToolBar();
    JTextArea textarea = new JTextArea();
    private final JScrollPane scrollpane = new JScrollPane(textarea);
    private final JButton saveBtn = new JButton("Save");
    private final JButton openBtn = new JButton("Open");
    private final JButton newBtn = new JButton("New");
    private final JButton renameBtn = new JButton("Rename");
    
    public Texty(String file, TextyModel model) {
        super("Texty - " + file);
        
        textyModel = model;
        textyEvent = new TextyEvent(this, textyModel);
        
        toolbar.setMargin(new Insets(4, 4, 0, 4));
        textarea.setBorder(new EmptyBorder(10, 10, 10, 10));
        textarea.setLineWrap(true);
        textarea.setWrapStyleWord(true);
        toolbar.setFloatable(false);
        
        setSize(new Dimension(JPANEL_WIDTH_INT, JPANEL_HEIGHT_INT));
        setLocationRelativeTo(null);
        toolbarPanel.setPreferredSize(new Dimension(JPANEL_WIDTH_INT, 40));
        
        setLayout(new BorderLayout());
        toolbarPanel.setLayout(new BorderLayout());
        textareaPanel.setLayout(new BorderLayout());
        
        saveBtn.setActionCommand("InitSave");
        openBtn.setActionCommand("InitOpen");
        newBtn.setActionCommand("InitNew");
        renameBtn.setActionCommand("InitRename");
        
        toolbar.add(saveBtn);
        toolbar.add(openBtn);
        toolbar.add(newBtn);
        toolbar.add(renameBtn);
        toolbarPanel.add(toolbar);
        textareaPanel.add(scrollpane);
        add(toolbarPanel, BorderLayout.NORTH);
        add(textareaPanel, BorderLayout.CENTER);
        
        setVisible(true);
        textarea.requestFocusInWindow();
        
        saveBtn.addActionListener(textyEvent.new SaveEvent());
        openBtn.addActionListener(textyEvent.new OpenEvent());
        newBtn.addActionListener(textyEvent.new NewEvent());
        renameBtn.addActionListener(textyEvent.new RenameEvent());    
        
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
                    saveWin.dispose(); // remove resource
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
                    saveAnywayWin.dispose(); // remove resource
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
                    openWin.dispose(); // remove resource
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
                    renameWin.dispose(); // remove resource
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