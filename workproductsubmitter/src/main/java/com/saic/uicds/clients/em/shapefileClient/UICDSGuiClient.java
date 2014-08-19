package com.saic.uicds.clients.em.shapefileClient;

import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.peer.ButtonPeer;
//import java.awt.event.MouseEvent;
//import java.awt.Color;
import java.awt.Event;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Dimension;

import java.awt.Color;

import javax.swing.SwingConstants;
//import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.Box;
import javax.swing.JTextArea;

//import javax.swing.JTextArea;
import javax.swing.JTextField;
//import javax.swing.UIManager;

//import java.awt.Dimension;
import java.io.File;

import javax.swing.JList;
import javax.swing.JScrollPane;
//import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
//import javax.swing.event.TableModelEvent;
//import javax.swing.event.TableModelListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;


import java.util.List;


//import com.saic.uicds.clients.em.shapefileClient.ShapeFileSubmitter;

public class UICDSGuiClient implements ActionListener {

	private JFrame jFrame = null;
	private JMenuBar jJMenuBar = null;
	private JMenu fileMenu = null;
	private JMenu editMenu = null;
	private JMenu helpMenu = null;
	private JMenuItem exitMenuItem = null;
	private JMenuItem aboutMenuItem = null;
	private JMenuItem cutMenuItem = null;
	private JMenuItem copyMenuItem = null;
	private JMenuItem pasteMenuItem = null;
	private JMenuItem saveMenuItem = null;
	private JDialog aboutDialog = null;
	private JPanel aboutContentPane = null;
	private JLabel aboutVersionLabel = null;
	
	private JList sampleJList;
	private JTextField valueField;
	private JTextField fileNameValueField;
	private JTextField linkNameValueField;
	private JTextField datasourceNameValueField;
	//Code for Map Work Product layer
	private JTextField mapFileNameValueField;
	private JTextField mapLinkLayerNameValueField;
	private JTextField mapLinkTitleValueField;
	private JTextField mapLinkFormatValueField;
	private JTextField mapLinkServiceValueField;
	private JTextField mapLinkSerTitleValueField;
	private JTextField mapLinkVersionValueField;
	private JTextField mapLinkSRSValueField;
	private JTextField mapLinkURLValueField;
	

	private JFileChooser fileChooser;
	private JButton openButton;
	private JButton refreshButton;
	//Code for Map Work Product layer
	private JButton mapOpenButton;

	private JButton fileSvcButton;
	private JButton lkSvcButton;
    private JButton dsAddButton;
    private JButton dsDeleteButton;
    private JButton lkDsButton;
    //Code for Map Work Product layer
    private JButton mapFileSvcButton;
    private JButton mapLkSvcButton;
    
    private JTextArea statusText;

    private JList dsList;

	private	JTable table;
	private File file;
	private File MapFile;
	private static ShapeFileSubmitter sfs;
	private String shpUploadStatus = "";  //  @jve:decl-index=0:
	private String assMapFileStatus = ""; //added for Map
	String dataValues[][];  //  @jve:decl-index=0:
	private static String[] argsInternal;
	String columnNames[] = { "ID", "Name"};
	TableModel tableModel;
	private static boolean shapefileOnly = false;
    private List<String> datasourceList;
    private List<String> coreArrayList;

    private JComboBox serverList;
	private String[] serverEnum = {"OGC:WMS", "OGC:WFS"};
	private String serverChoice = "";
	public Integer serverIndex = 0;
	
	/**
     * main entry to the program
	 * @param args
	 */
	public static void main(String[] args) {
        // Get the spring context and then the ShapeFileSubmitter object that was configured in it
		
        //ApplicationContext context = new ClassPathXmlApplicationContext(
		//                                new String[] { "contexts/async-context.xml" });
      
	    argsInternal = args;
        ApplicationContext context = new FileSystemXmlApplicationContext(
        new String[] { "async-context.xml" });
      
        //ShapeFileSubmitter sfs = (ShapeFileSubmitter) context.getBean("shapeFileSubmitter");
        sfs = (ShapeFileSubmitter) context.getBean("shapeFileSubmitter");
	    if (sfs == null) {
            System.err.println("Could not instantiate shapeFileSubmitter");
	    }
	    shapefileOnly = sfs.init(argsInternal);
	    UICDSGuiClient application = new UICDSGuiClient();
	    application.getJFrame().setVisible(true);
	}

	/**
	 * This method create the main frame for the UI
	 * 
	 * @return javax.swing.JFrame
	 */
	private JFrame getJFrame() {

        // create the main frame
		if (jFrame == null) {
			jFrame = new JFrame();
			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrame.setJMenuBar(getJJMenuBar());
			jFrame.setSize(1040, 700);
			
			
			// set the font
	        Font displayFont = new Font("Times New Roman",Font.PLAIN, 14);

            // set the layout
			Container content=jFrame.getContentPane();
            if (shapefileOnly) {
                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            } else {
                // content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
                content.setLayout(new BorderLayout());
            }
			

            // create the incident panel
			content.add(createIncidentListPanel(displayFont), BorderLayout.WEST);
			content.add(createButtonsPanel(displayFont), BorderLayout.EAST);
            content.add(createStatusPanel(displayFont), BorderLayout.SOUTH);
            
		    //jFrame.pack();
		    jFrame.setVisible(true);
		}
		return jFrame;
	}

    /*
     * create the status panel
     */
    private JPanel createStatusPanel(Font displayFont) 
    {
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));

        Border statusPanelBorder =
          BorderFactory.createTitledBorder("Status");
        statusPanel.setBorder(statusPanelBorder);

        statusText = new JTextArea();
        statusText.setEditable(false);
        statusPanel.setFont(displayFont);
        statusPanel.add(statusText);
        return statusPanel;

    }

    /*
     * Create the incident list panel
     */
    private JPanel createIncidentListPanel(Font displayFont)
    {
        JPanel listPanel = new JPanel();
        Border listPanelBorder =
          BorderFactory.createTitledBorder("UICDS Core Incident List");
        listPanel.setBorder(listPanelBorder);

        //load the incident table		
        dataValues = sfs.getListOfIncidents();
        tableModel = new DefaultTableModel(dataValues, columnNames);
        table = new JTable (tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new RowListener());
        JScrollPane listPane = new JScrollPane(table);
        listPanel.add(listPane);

        //create the refresh button
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(this);
        listPanel.add(refreshButton);

        //Don't add to gui yet
        //JLabel valueLabel = new JLabel("Selected Incident:");
        //valueLabel.setFont(displayFont);

        valueField = new JTextField("", 7);
        valueField.setFont(displayFont);

        //JPanel valuePanel = new JPanel();
        //Border valuePanelBorder =
        //  BorderFactory.createTitledBorder("Incident");
        //valuePanel.setBorder(valuePanelBorder);

        return listPanel;
    }

    /*
     * Create the buttons panel, which consist of add file, add link, add datasource
     * and unregister datasource
     */
    private JPanel createButtonsPanel(Font displayFont)
    {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.PAGE_AXIS));
     
        buttonsPanel.add(createAddFilePanel(displayFont));
        buttonsPanel.add(createLinkPanel(displayFont));
        //dsh - delete the data resource panels per JWM request
        //buttonsPanel.add(createAddDsPanel(displayFont));
        //buttonsPanel.add(createUnregisterPanel(displayFont));
        //Code for Map Work Product layer
        //buttonsPanel.add(createAddMapFilePanel(displayFont));
        buttonsPanel.add(createMapLinkPanel(displayFont));

        if (!shapefileOnly) {
          return buttonsPanel;
        } else {
          return createAddFilePanel(displayFont);
        }
    }

    /*
     * Create the add file panel
     */
    private JPanel createAddFilePanel(Font displayFont)
    {
        JPanel fileNameValuePanel = new JPanel();

        Border fileNameValuePanelBorder;
        Border svcButtonBorder;

        if (shapefileOnly) {
            jFrame.setTitle("Submit Shapefile to UICDS");
            fileNameValuePanelBorder = BorderFactory.createTitledBorder("Select .shz file");
            svcButtonBorder =
                BorderFactory.createTitledBorder("Add Shapefile to Incident");
			loadJFileChooser(true);
        } else {
            jFrame.setTitle("Submit a Work Product to UICDS");
            fileNameValuePanelBorder = BorderFactory.createTitledBorder("Select Work Product file");
            svcButtonBorder =
                BorderFactory.createTitledBorder("Add Work Product to Incident");
			loadJFileChooser(false);
        }

        fileNameValuePanel.setBorder(fileNameValuePanelBorder);

        // add the file name field
        fileNameValueField = new JTextField("", 20);
        fileNameValueField.setFont(displayFont);
        fileNameValuePanel.add(fileNameValueField);

        // add the browse button
        openButton = new JButton("Browse");
        openButton.addActionListener(this);
        fileNameValuePanel.add(openButton);

        // create the add file button
        fileSvcButton = new JButton("Add");
        fileSvcButton.addActionListener(this);
        fileSvcButton.setEnabled(false);

        fileNameValuePanel.add(fileSvcButton);

        return fileNameValuePanel; 
    }

    /*
     * Create the add link panel
     */
    private JPanel createLinkPanel(Font displayFont)
    {
        // create the panel
        JPanel linkNameValuePanel = new JPanel();
        Border linkNameValuePanelBorder =
        BorderFactory.createTitledBorder("Enter Work Product Link");
        linkNameValuePanel.setBorder(linkNameValuePanelBorder);

        // add the text field
        linkNameValueField = new JTextField("", 20);
        linkNameValueField.setFont(displayFont);
        linkNameValuePanel.add(linkNameValueField);

        // create the add link button
        lkSvcButton = new JButton("Add");
        lkSvcButton.addActionListener(this);
        lkSvcButton.setEnabled(false);
        linkNameValuePanel.add(lkSvcButton);

        return linkNameValuePanel;

    }
    
    
    
    
    /*
     * Create the add MAP file panel
     * Code for Map Work Product layer
     */
    private JPanel createAddMapFilePanel(Font displayFont)
    {
        JPanel mapFileNameValuePanel = new JPanel();

        Border mapFileNameValuePanelBorder;
        Border mapSvcButtonBorder;

        if (shapefileOnly) {
            jFrame.setTitle("Submit Shapefile to UICDS");
            mapFileNameValuePanelBorder = BorderFactory.createTitledBorder("Select .shz file");
            mapSvcButtonBorder =
                BorderFactory.createTitledBorder("Add Shapefile to Incident");
			loadJFileChooser(true);
        } else {
            jFrame.setTitle("Submit a Work Product to UICDS");
            mapFileNameValuePanelBorder = BorderFactory.createTitledBorder("Select Map Layer XML file");
            mapSvcButtonBorder =
                BorderFactory.createTitledBorder("Add Map Work Product to Incident");
			loadJFileChooser(false);
        }

        mapFileNameValuePanel.setBorder(mapFileNameValuePanelBorder);

        // add the Map file name field
        mapFileNameValueField = new JTextField("", 20);
        mapFileNameValueField.setFont(displayFont);
        mapFileNameValuePanel.add(mapFileNameValueField);

        // add the Map browse button
        mapOpenButton = new JButton("Browse");
        mapOpenButton.addActionListener(this);
        mapFileNameValuePanel.add(mapOpenButton);

        // create the Map add file button
        mapFileSvcButton = new JButton("Add");
        mapFileSvcButton.addActionListener(this);
        mapFileSvcButton.setEnabled(false);

        mapFileNameValuePanel.add(mapFileSvcButton);

        return mapFileNameValuePanel; 
    }
    
    
    
    /*
     * Create the Map add link panel
     * Code for Map Work Product layer
     */
    private JPanel createMapLinkPanel(Font displayFont)
    {
        // create the panel
        JPanel mapLinkNameValuePanel = new JPanel();
        Border mapLinkNameValuePanelBorder =
        BorderFactory.createTitledBorder("Enter MapViewContext Layer Details");
        mapLinkNameValuePanel.setBorder(mapLinkNameValuePanelBorder);
        
        JPanel layerNameFieldsPanel = new JPanel();
        layerNameFieldsPanel.setLayout(new BoxLayout(layerNameFieldsPanel, BoxLayout.PAGE_AXIS));
        //dataFieldsPanel.setLayout(null);
        Border addLayerNameValuePanelBorder = BorderFactory.createTitledBorder("");
        layerNameFieldsPanel.setBorder(addLayerNameValuePanelBorder);

        // add the text field 
        mapLinkLayerNameValueField = new JTextField("", 15);
        layerNameFieldsPanel.add(new JLabel("Layer Name: "));
        mapLinkLayerNameValueField.setFont(displayFont);
        layerNameFieldsPanel.add(mapLinkLayerNameValueField);
        
        // add space between fields
        layerNameFieldsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        mapLinkTitleValueField = new JTextField("", 15);
        layerNameFieldsPanel.add(new JLabel("Title: "));
        mapLinkTitleValueField.setFont(displayFont);
        layerNameFieldsPanel.add(mapLinkTitleValueField);
        
        //add space between fields
        layerNameFieldsPanel.add(Box.createRigidArea(new Dimension(0,10)));
        
        //add the format field
        mapLinkFormatValueField = new JTextField("", 15);
        layerNameFieldsPanel.add(new JLabel("Format: "));
        mapLinkFormatValueField.setFont(displayFont);
        layerNameFieldsPanel.add(mapLinkFormatValueField);
       
        JPanel serverDataFieldPanel = new JPanel();
        serverDataFieldPanel.setLayout(new BoxLayout(serverDataFieldPanel, BoxLayout.PAGE_AXIS));
        Border addSrsDataFieldPanelBorder =
            BorderFactory.createTitledBorder("");
        serverDataFieldPanel.setBorder(addSrsDataFieldPanelBorder);
        
        //Need a drop down instead of a txt
        /*mapLinkServiceValueField = new JTextField("", 8);
        serverDataFieldPanel.add(new JLabel("Server Service: "));
        mapLinkServiceValueField.setFont(displayFont);
        serverDataFieldPanel.add(mapLinkServiceValueField);*/
        
        serverList = new JComboBox(serverEnum);
        serverList.setSelectedIndex(0);
        serverList.addActionListener(this);
        serverDataFieldPanel.add(new JLabel("Server Service: "));
        serverDataFieldPanel.add(serverList);
        
        serverDataFieldPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        mapLinkSerTitleValueField = new JTextField("", 8);
        serverDataFieldPanel.add(new JLabel("Title: "));
        mapLinkSerTitleValueField.setFont(displayFont);
        serverDataFieldPanel.add(mapLinkSerTitleValueField);
        
        serverDataFieldPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                
        mapLinkVersionValueField = new JTextField("", 8);
        serverDataFieldPanel.add(new JLabel("Version: "));
        mapLinkVersionValueField.setFont(displayFont);
        serverDataFieldPanel.add(mapLinkVersionValueField);
        
        serverDataFieldPanel.add(Box.createRigidArea(new Dimension(0,10)));
        
        mapLinkSRSValueField = new JTextField("", 8);
        serverDataFieldPanel.add(new JLabel("SRS: "));
        mapLinkSRSValueField.setText("EPSG:4326");
        mapLinkSRSValueField.setFont(displayFont);
        serverDataFieldPanel.add(mapLinkSRSValueField);
        //layerNameFieldsPanel.add(mapLinkSRSValueField);
        
        serverDataFieldPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        mapLinkURLValueField = new JTextField("", 15);
        serverDataFieldPanel.add(new JLabel("Online Resource href: "));
        mapLinkURLValueField.setFont(displayFont);
        serverDataFieldPanel.add(mapLinkURLValueField);
       // layerNameFieldsPanel.add(mapLinkURLValueField);
        
        //mapLinkNameValuePanel.add(mapLinkLayerNameValueField);
        //mapLinkNameValuePanel.add(mapLinkTitleValueField );
        //srsDataFieldPanel.add(new JLabel("SRS: "));
        mapLinkNameValuePanel.add(Box.createRigidArea(new Dimension(0, 10)));
       
       // mapLinkNameValuePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        // add space between buttons
       // srsDataFieldPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // create the add link button
        mapLkSvcButton = new JButton("Add");
        mapLkSvcButton.addActionListener(this);
        mapLkSvcButton.setEnabled(false);
       
        
       // srsDataFieldPanel.add(dataFieldsPanel);
        
        mapLinkNameValuePanel.add(layerNameFieldsPanel);
        mapLinkNameValuePanel.add(serverDataFieldPanel);
        mapLinkNameValuePanel.add(mapLkSvcButton);
       // mapLinkNameValuePanel.add(mapLkSvcButton);
       // mapLinkNameValuePanel.add(mapLkSvcButton,new Integer(2), 0);

return mapLinkNameValuePanel;
 }
    
    

    /*
     * Create the add data source panel
     */
    private JPanel createAddDsPanel(Font displayFont)
    {
        // create the panel
        JPanel addDsPanel = new JPanel();
        Border addDSNameValuePanelBorder =
            BorderFactory.createTitledBorder("Enter Datasource");
        addDsPanel.setBorder(addDSNameValuePanelBorder);

        // add the data source text field
        datasourceNameValueField = new JTextField("", 20);
        datasourceNameValueField.setFont(displayFont);

        addDsPanel.add(datasourceNameValueField);

        // add the add button
        dsAddButton = new JButton("Add");
        dsAddButton.addActionListener(this);
        addDsPanel.add(dsAddButton);

        return addDsPanel;
    }

    /*
     * Create the unregister data source panel.  It consists of the data source list,
     * the unregister data source button and the link data source button
     */
    private JPanel createUnregisterPanel(Font displayFont)
    {
        // create the panel
        JPanel deleteDSPanel = new JPanel();
        Border selectDSNameValuePanelBorder =
            BorderFactory.createTitledBorder("Select Datasource");
        deleteDSPanel.setBorder(selectDSNameValuePanelBorder);

        // add the data source list
        datasourceList = sfs.getDataSourceList();

        Object [] dsArray = datasourceList.toArray();
        dsList = new JList(dsArray);
        dsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dsList.getSelectionModel().addListSelectionListener(new DsRowListener());
        JScrollPane dsScrollPane = new JScrollPane(dsList);
        deleteDSPanel.add(dsScrollPane);

        // add the buttons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.PAGE_AXIS));
        //buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        lkDsButton = new JButton("Link");
        lkDsButton.addActionListener(this);
        lkDsButton.setEnabled(false);

        buttonsPanel.add(lkDsButton);

        // add space between buttons
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // add the unregister button
        dsDeleteButton = new JButton("Unregister");
        dsDeleteButton.addActionListener(this);
        dsDeleteButton.setEnabled(false);
        buttonsPanel.add(dsDeleteButton);

        // add the buttons to the deleteDS panel
        deleteDSPanel.add(buttonsPanel);

        return deleteDSPanel; 
    }

	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getFileMenu());
			//jJMenuBar.add(getEditMenu());
			jJMenuBar.add(getHelpMenu());
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setText("File");
			//fileMenu.add(getSaveMenuItem());
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getEditMenu() {
		if (editMenu == null) {
			editMenu = new JMenu();
			editMenu.setText("Edit");
			editMenu.add(getCutMenuItem());
			editMenu.add(getCopyMenuItem());
			editMenu.add(getPasteMenuItem());
		}
		return editMenu;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getHelpMenu() {
		if (helpMenu == null) {
			helpMenu = new JMenu();
			helpMenu.setText("Help");
			helpMenu.add(getAboutMenuItem());
		}
		return helpMenu;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getExitMenuItem() {
		if (exitMenuItem == null) {
			exitMenuItem = new JMenuItem();
			exitMenuItem.setText("Exit");
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
		}
		return exitMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getAboutMenuItem() {
		if (aboutMenuItem == null) {
			aboutMenuItem = new JMenuItem();
			aboutMenuItem.setText("About");
			aboutMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JDialog aboutDialog = getAboutDialog();
					aboutDialog.pack();
					Point loc = getJFrame().getLocation();
					loc.translate(20, 20);
					aboutDialog.setLocation(loc);
					aboutDialog.setVisible(true);
				}
			});
		}
		return aboutMenuItem;
	}

	/**
	 * This method initializes aboutDialog	
	 * 	
	 * @return javax.swing.JDialog
	 */
	private JDialog getAboutDialog() {
		if (aboutDialog == null) {
			aboutDialog = new JDialog(getJFrame(), true);
			aboutDialog.setTitle("About");
			aboutDialog.setContentPane(getAboutContentPane());
		}
		return aboutDialog;
	}

	/**
	 * This method initializes aboutContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getAboutContentPane() {
		if (aboutContentPane == null) {
			aboutContentPane = new JPanel();
			aboutContentPane.setLayout(new BorderLayout());
			aboutContentPane.add(getAboutVersionLabel(), BorderLayout.CENTER);
		}
		return aboutContentPane;
	}

	/**
	 * This method initializes aboutVersionLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getAboutVersionLabel() {
		if (aboutVersionLabel == null) {
			aboutVersionLabel = new JLabel();
			aboutVersionLabel.setText("UICDS Gui Client Version 1.0");
			aboutVersionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return aboutVersionLabel;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getCutMenuItem() {
		if (cutMenuItem == null) {
			cutMenuItem = new JMenuItem();
			cutMenuItem.setText("Cut");
			cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
					Event.CTRL_MASK, true));
		}
		return cutMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getCopyMenuItem() {
		if (copyMenuItem == null) {
			copyMenuItem = new JMenuItem();
			copyMenuItem.setText("Copy");
			copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
					Event.CTRL_MASK, true));
		}
		return copyMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getPasteMenuItem() {
		if (pasteMenuItem == null) {
			pasteMenuItem = new JMenuItem();
			pasteMenuItem.setText("Paste");
			pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
					Event.CTRL_MASK, true));
		}
		return pasteMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getSaveMenuItem() {
		if (saveMenuItem == null) {
			saveMenuItem = new JMenuItem();
			saveMenuItem.setText("Save");
			saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
					Event.CTRL_MASK, true));
		}
		return saveMenuItem;
	}
	
	private void loadJFileChooser(boolean shzOnly) {
		fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        ShapefileFilter fileFilter = new ShapefileFilter(shzOnly);
        // ff.init();
        fileChooser.setFileFilter(fileFilter);
	}
	
    /*
    action performed by the buttons
    */
	public void actionPerformed(ActionEvent e) {

		//Handle Browse button action.
        if (e.getSource() == openButton) {
            int returnVal = fileChooser.showOpenDialog(jFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                
                // save the file name to the fileNameValue field.
                fileNameValueField.setText(file.getName());
            } else {
                fileNameValueField.setText("");
            }
        // handle the Refresh button   mapOpenButton mapFileNameValueField
        }else if (e.getSource() == mapOpenButton) {
            int returnValMap = fileChooser.showOpenDialog(jFrame);
            if (returnValMap == JFileChooser.APPROVE_OPTION) {
                MapFile = fileChooser.getSelectedFile();
                
                // save the file name to the fileNameValue field.
                mapFileNameValueField.setText(MapFile.getName());
            } else {
            	mapFileNameValueField.setText("");
            }
        // handle the Refresh button   mapOpenButton mapFileNameValueField
        }
        else if (e.getSource() == refreshButton) {
            shapefileOnly = sfs.init(argsInternal);
            dataValues = sfs.getListOfIncidents();
            ((DefaultTableModel)tableModel).setDataVector(dataValues,columnNames);
            ((DefaultTableModel)tableModel).fireTableDataChanged();

        // handle the add data source button is pressed
        } else if (e.getSource() == dsAddButton) {
            if (datasourceNameValueField.getText().length()>4) {
                String dataSourceName = datasourceNameValueField.getText();

                sfs.registerDataSource(dataSourceName);
                datasourceList = sfs.getDataSourceList();

                // update the data source list
                dsList.setListData(datasourceList.toArray());
                dsList.invalidate();
                statusText.setText("URL " + dataSourceName + " was registered.");
            } else {
                JOptionPane.showMessageDialog(jFrame, "Please enter a data source");
            }
        // handle the unregister data source button
        } else if (e.getSource() == dsDeleteButton) {
            String valueSelected = (String) dsList.getSelectedValue();
            if (valueSelected != null) {
                sfs.unregisterDataSource(valueSelected);
                datasourceList = sfs.getDataSourceList();
    
                // update the data source list
                dsList.setListData(datasourceList.toArray());
                dsList.invalidate();
                statusText.setText("URL " + valueSelected + " was unregistered.");
            } else {
                JOptionPane.showMessageDialog(jFrame, "Please select a data source to delete.");
            }
        // handle the link data source button
        }  else if (e.getSource() == lkDsButton) {
            if (checkSelectIncident() == true) {

                // get the data source value and link to incident
                String valueSelected = (String) dsList.getSelectedValue();
                if (valueSelected != null  ) {
                    if (checkFileNameLength() == true) {
                        System.out.println("processing data source link...");
                        shpUploadStatus = sfs.assocLinkToIncident(valueField.getText(), valueSelected);

                        // System.out.println(shpUploadStatus);
                        statusText.setText("URL " + valueSelected + " was linked to incident.");
                    }
                }
                else {
                    JOptionPane.showMessageDialog(jFrame, "Please select a data source to link to incident.");
                }
            }
            
        } else if (e.getSource() == fileSvcButton) {
           // handle the add file button
     
            if (checkSelectIncident() == true) {

                if (checkFileNameLength() == true) {

                    if (fileNameValueField.getText().contains(".shz")) {
                        System.out.println("Processing a shapefile...");
                        shpUploadStatus = sfs.assocShpToIncidentUsingDOM(valueField.getText(), file);
                        if (shpUploadStatus.equalsIgnoreCase("Accepted"))
                            statusText.setText("Shapefile doc was uploaded.");
                        else {
                            statusText.setText("Shapefile doc was not uploaded.");
                            JOptionPane.showMessageDialog(jFrame, "There was a problem with the Shapefile. File not uploaded.");
                        }
                    
                    } else if (fileNameValueField.getText().contains(".doc") ||
                            fileNameValueField.getText().contains(".docx")) {
                        System.out.println("Processing a word doc...");
                        shpUploadStatus = sfs.assocBinaryToIncident(valueField.getText(), file, "Word");
                        if (shpUploadStatus.equalsIgnoreCase("Accepted"))
                            statusText.setText("Word doc was uploaded.");
                        else {
                            statusText.setText("Word doc was not uploaded.");
                            JOptionPane.showMessageDialog(jFrame, "There was a problem with the Word doc. File not uploaded.");
                        }
                    
                    } else if (fileNameValueField.getText().contains(".pdf")) {
                        System.out.println("Processing a pdf...");
                        shpUploadStatus = sfs.assocBinaryToIncident(valueField.getText(), file, "PDF");
                        if (shpUploadStatus.equalsIgnoreCase("Accepted"))
                            statusText.setText("PDF was uploaded.");
                        else {
                            statusText.setText("PDF was not uploaded.");
                            JOptionPane.showMessageDialog(jFrame, "There was a problem with the PDF. File not uploaded.");
                        }
                    
                    } else {
                        JOptionPane.showMessageDialog(jFrame, "Please select a .shz, .doc or .pdf file.");
                    }
                }
            } 
        // handle the add link button
        } 
        
        else if (e.getSource() == mapFileSvcButton) {
            // handle the add file button
      
             if (checkSelectIncident() == true) {

                 if (checkFileNameLength() == true) {

                    
                if (mapFileNameValueField.getText().contains(".xml")) {
                         System.out.println("Processing a xml for map layer...");
                         assMapFileStatus = sfs.assocMapFileToIncident(valueField.getText(), MapFile);
                } else {    
                    	 JOptionPane.showMessageDialog(jFrame, "Please select a .xml map layer file.");
                     }
                }
             } 
                  
         // handle the add link button
         }else if (e.getSource() == lkSvcButton) {
            if (checkSelectIncident() == true)
            {
                if (linkNameValueField.getText().length()>4) {
                    System.out.println("processing link...");
                    shpUploadStatus = sfs.assocLinkToIncident(valueField.getText(), linkNameValueField.getText());
                    System.out.println(shpUploadStatus);
                    statusText.setText("URL " + linkNameValueField.getText() + " was linked to incident.");
                } else {
                    JOptionPane.showMessageDialog(jFrame, "Please enter a URL");
                }
            }

        }
        //Handle the Drop Down Server Menu
         else if (e.getSource() == serverList) {
           serverChoice = serverList.getSelectedItem().toString();
  		   serverIndex = serverList.getSelectedIndex();
  		   System.out.println("Server Choice=" + serverChoice);
         }
        //Code for MapLayer Details ADD button mapLinkLayerNameValueField mapLinkTitleValueField mapLinkSRSValueField mapLinkURLValueField
         else if (e.getSource() == mapLkSvcButton) {
             if (checkSelectIncident() == true)
             {
            	    System.out.println("Incident Selected");
                 if (mapLinkLayerNameValueField.getText().length()>1) {
                     System.out.println("Getting the Map Layer Details...");
                     //dsh Changed to update the map WP instead of posting the layer wp
                     //shpUploadStatus = sfs.assocMapLinkToIncident(valueField.getText(), mapLinkLayerNameValueField.getText(),mapLinkTitleValueField.getText(),mapLinkSRSValueField.getText(),mapLinkURLValueField.getText());
                     shpUploadStatus = sfs.addMapLinkToMap(valueField.getText(), mapLinkLayerNameValueField.getText(),mapLinkTitleValueField.getText(),mapLinkFormatValueField.getText(),serverIndex,mapLinkSerTitleValueField.getText(),mapLinkVersionValueField.getText(),mapLinkSRSValueField.getText(),mapLinkURLValueField.getText());
                     System.out.println(shpUploadStatus);
                     JOptionPane.showMessageDialog(jFrame, "New Layer Added to MapContext");
                    // statusText.setText("URL " + linkNameValueField.getText() + " was linked to incident.");
                 } else {
                     JOptionPane.showMessageDialog(jFrame, "Please enter a URL");
                 }
             }

         }
        
        
        
        
    }

    private boolean checkSelectIncident()
    {
        boolean incidentSelected = false;
        if (table.getSelectedRow() == -1)
        {
            JOptionPane.showMessageDialog(jFrame, "Please select a UICDS Incident.");
        }
        else
        {
            incidentSelected = true;
        }

        return incidentSelected;
    }

    private boolean checkFileNameLength()
    {
        boolean fileNameOk = false;
        if (valueField.getText().length() < 5) {
        
            JOptionPane.showMessageDialog(jFrame, "Please select a .shz, .doc or .pdf file.");
        }
        else
        {
            fileNameOk = true;
        }

        return fileNameOk;
    }

    private class ValueReporter implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
          if (!event.getValueIsAdjusting()) 
            valueField.setText(sampleJList.getSelectedValue().toString());
        }
    }	
	  
    /* listener for the incident table
    */
    private class RowListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }

            // if an incident is selected, then get the incident id and save to valueField.
            if (table.getSelectedRow() != -1)
            {
                System.out.println("Table Selected Row="+table.getSelectedRow());
                System.out.println("Incident ID Selected:"+table.getModel().getValueAt(table.getSelectedRow(), 0));
                valueField.setText((String) table.getModel().getValueAt(table.getSelectedRow(), 0));
                fileSvcButton.setEnabled(true);
                lkSvcButton.setEnabled(true);
                
                //Code for Map Work Product layer
                //dsh remove the mapfile
                //mapFileSvcButton.setEnabled(true);
                mapLkSvcButton.setEnabled(true);

                // if the data source is not seleted, then disable the link data source button.
                //dsh remove the ds panel
                /*if (dsList.getSelectedIndex() != -1)
                {
                    lkDsButton.setEnabled(true);
                }*/
            }
            else
            {
                // an incident is selected: enable the add button and the link data source button.
                System.out.println("Table Selected Row="+table.getSelectedRow());
                fileSvcButton.setEnabled(false);
                lkSvcButton.setEnabled(false);
                lkDsButton.setEnabled(false);
            }
        }
    }

    /* listener for the data source list
    */
    private class DsRowListener implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent event) {
            // if the main panel is adjusting, return
            if (event.getValueIsAdjusting()) {
                return;
            }

            // if an item is selected, then enable the two unregister and link buttons.
            if (dsList.getSelectedIndex() != -1)
            {
                System.out.println("List Selected Row="+ dsList.getSelectedIndex());
                System.out.println("List Incident ID Selected:"+dsList.getSelectedValue());
                dsDeleteButton.setEnabled(true);
                lkDsButton.setEnabled(true);

                // if there is no incident selected, then disable the link button.
                if (table.getSelectedRow() == -1) {
                    lkDsButton.setEnabled(false);
                }
                
            }
            else
            {
                // if no item is selected, disable the unregister and link buttons.
                System.out.println("Selected Row="+dsList.getSelectedIndex());
                dsDeleteButton.setEnabled(false);
                lkDsButton.setEnabled(false);
            }
        }
    }
    
    private class ServerComboBoxListener implements ActionListener {
    	public void actionPerformed (ActionEvent e){
    		   serverChoice = serverList.getSelectedItem().toString();
    		   serverIndex = serverList.getSelectedIndex();
    		   /*JComboBox cb = (JComboBox)e.getSource();
    		   serverChoice = (String)cb.getSelectedItem();
    		   serverIndex = cb.getSelectedIndex();*/
    		   System.out.println("Server Choice=" + serverChoice);
    		}
    	}
    
   

}
