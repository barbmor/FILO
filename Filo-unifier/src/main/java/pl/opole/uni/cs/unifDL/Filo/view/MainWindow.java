package pl.opole.uni.cs.unifDL.Filo.view;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.border.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;

import pl.opole.uni.cs.unifDL.Filo.controller.FiloLogger;
import pl.opole.uni.cs.unifDL.Filo.controller.Solver;

public class MainWindow extends JFrame implements PropertyChangeListener {

	private JTextArea outputText;
	private Solver solver = new Solver();

	public Solver getSolver() {
		return solver;
	}

	private JComboBox<String> fileCombobox;
	private File selectedFile;
	private String message = "";
	private String result = "";
	private File lastDirectory = new File(System.getProperty("user.home"));

	public MainWindow() {
		setTitle("FILO -- FL_0 unifier");
		setSize(1000, 800);
		setMinimumSize(new Dimension(1000, 800));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setLocationRelativeTo(null);

		setupTopPanel();
		setupOutputPanel();
		setupBottomPanel();

		setVisible(true);
	}

	private void setupTopPanel() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		topPanel.setBackground(new Color(230, 230, 230));
		topPanel.setPreferredSize(new Dimension(1000, 200));

		setupInputPanel(topPanel);
		setupOptionPanel(topPanel);

		add(topPanel, BorderLayout.NORTH);
	}

	private void setupInputPanel(JPanel panel) {
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new SpringLayout());
		menuPanel.setBorder(new TitledBorder("Input"));
		menuPanel.setBackground(new Color(230, 230, 230));

		JTextArea labelInfo = new JTextArea(
				"Filo accepts a unification problem in form of an ontology file, i.e. the file with a suffix  owx or owl.\n"
						+ "The input file can be created with Protege ontology editor. The variables are entities with _var suffix.");
		labelInfo.setWrapStyleWord(true);
		labelInfo.setLineWrap(true);
		labelInfo.setEditable(false);
		labelInfo.setBackground(menuPanel.getBackground());
		labelInfo.setPreferredSize(new Dimension(600, 30));

		JLabel labelSelectPredefined = new JLabel("Select a predefined test from the list:");
		JLabel labelChooseInput = new JLabel("Or choose an input file:");

		fileCombobox = setupPredefinedComboBox();

		JButton startTestButton = new JButton("Start test");
		startTestButton.addActionListener(e -> handleTestSelection());

		JButton fileChooserButton = new JButton("Select file");
		fileChooserButton.addActionListener(e -> handleFileSelection());
		fileChooserButton.setToolTipText("The test will start automatically after selecting a file.");

		SpringLayout layout = (SpringLayout) menuPanel.getLayout();
		layout.putConstraint(SpringLayout.NORTH, labelInfo, 10, SpringLayout.NORTH, menuPanel);
		layout.putConstraint(SpringLayout.WEST, labelInfo, 10, SpringLayout.WEST, menuPanel);
		layout.putConstraint(SpringLayout.NORTH, labelSelectPredefined, 10, SpringLayout.SOUTH, labelInfo);
		layout.putConstraint(SpringLayout.WEST, labelSelectPredefined, 10, SpringLayout.WEST, menuPanel);
		layout.putConstraint(SpringLayout.NORTH, fileCombobox, 5, SpringLayout.SOUTH, labelSelectPredefined);
		layout.putConstraint(SpringLayout.WEST, fileCombobox, 10, SpringLayout.WEST, menuPanel);
		layout.putConstraint(SpringLayout.NORTH, startTestButton, 0, SpringLayout.NORTH, fileCombobox);
		layout.putConstraint(SpringLayout.WEST, startTestButton, 10, SpringLayout.EAST, fileCombobox);
		layout.putConstraint(SpringLayout.WEST, labelChooseInput, 0, SpringLayout.WEST, fileCombobox);
		layout.putConstraint(SpringLayout.NORTH, labelChooseInput, 10, SpringLayout.SOUTH, fileCombobox);
		layout.putConstraint(SpringLayout.NORTH, fileChooserButton, 10, SpringLayout.SOUTH, startTestButton);
		layout.putConstraint(SpringLayout.WEST, fileChooserButton, 0, SpringLayout.WEST, startTestButton);
		layout.putConstraint(SpringLayout.EAST, startTestButton, 0, SpringLayout.EAST, fileChooserButton);

		menuPanel.add(labelInfo);
		menuPanel.add(labelSelectPredefined);
		menuPanel.add(labelChooseInput);
		menuPanel.add(fileCombobox);
		menuPanel.add(startTestButton);
		menuPanel.add(fileChooserButton);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		panel.add(menuPanel, gbc);
	}

	private void setupOptionPanel(JPanel panel) {
		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new SpringLayout());
		optionPanel.setBorder(new TitledBorder("Options"));
		optionPanel.setBackground(new Color(230, 230, 230));
		optionPanel.setPreferredSize(new Dimension(250, 200));

		JPanel logFilterCheckBoxes = setupLogFilterCheckBoxes();

		JButton saveLogButton = new JButton("Save log file");

		JButton showStatistics = new JButton("Show statistics");
		saveLogButton.addActionListener(e -> handleLogSave());
		showStatistics.addActionListener(e -> handleShowStatistics());

		SpringLayout layout = (SpringLayout) optionPanel.getLayout();
		layout.putConstraint(SpringLayout.NORTH, logFilterCheckBoxes, 10, SpringLayout.NORTH, optionPanel);
		layout.putConstraint(SpringLayout.WEST, logFilterCheckBoxes, 10, SpringLayout.WEST, optionPanel);
		layout.putConstraint(SpringLayout.NORTH, saveLogButton, 10, SpringLayout.SOUTH, logFilterCheckBoxes);
		layout.putConstraint(SpringLayout.WEST, saveLogButton, 10, SpringLayout.WEST, optionPanel);
		layout.putConstraint(SpringLayout.NORTH, showStatistics, 10, SpringLayout.SOUTH, saveLogButton);
		layout.putConstraint(SpringLayout.WEST, showStatistics, 0, SpringLayout.WEST, saveLogButton);
		layout.putConstraint(SpringLayout.EAST, saveLogButton, 0, SpringLayout.EAST, showStatistics);

		optionPanel.add(logFilterCheckBoxes);
		optionPanel.add(saveLogButton);
		optionPanel.add(showStatistics);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.VERTICAL;

		panel.add(optionPanel, gbc);
	}

	private JPanel setupLogFilterCheckBoxes() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(new Color(230, 230, 230));
		JLabel label = new JLabel("Change log level (default level is INFO):");

		JCheckBox fineCheckBox = new JCheckBox("FINE");
		fineCheckBox.setBackground(new Color(230, 230, 230));

		panel.add(label);
		panel.add(fineCheckBox);

		fineCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (fineCheckBox.isSelected()) {
					FiloLogger.setLevel("FINE");
				} else {
					FiloLogger.setLevel("INFO");
				}
			}
		});
		return panel;
	}

	private JComboBox<String> setupPredefinedComboBox() {
		String resourceFolder = "tests/";
		String[] fileNames = getResourceFileNames(resourceFolder);

		if (fileNames.length == 0) {
			fileNames = new String[] { "NO TEST" };
		} else {
			Arrays.sort(fileNames, Comparator.comparing((String s) -> s.replaceAll("\\d", "").trim())
					.thenComparingInt(s -> Integer.parseInt(s.replaceAll("\\D", "0"))));
		}

		return new JComboBox<>(fileNames);
	}

	private String[] getResourceFileNames(String resourceFolder) {
		ArrayList<String> fileNames = new ArrayList<>();

		String indexFilePath = resourceFolder + "index.txt";
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(indexFilePath)) {
			if (is == null) {
				System.err.println("index.txt not found in  " + resourceFolder);
				return new String[0];
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					fileNames.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileNames.toArray(new String[0]);
	}

	private void setupOutputPanel() {
		JPanel outputPanel = new JPanel(new BorderLayout());
		outputPanel.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED), "Output"));

		outputText = new JTextArea();
		outputText.setEditable(false);
		outputText.setLineWrap(true);
		outputText.setWrapStyleWord(true);

		JScrollPane scrollPane = new JScrollPane(outputText);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		outputPanel.add(scrollPane, BorderLayout.CENTER);

		add(outputPanel, BorderLayout.CENTER);
	}

	private void setupBottomPanel() {
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JButton saveOutputButton = new JButton("Save output (txt)");
		JButton saveOntologyButton = new JButton("Save solution (owl)");
		JButton closeButton = new JButton("Close");

		closeButton.setForeground(Color.RED);

		saveOutputButton.addActionListener(e -> handleOutputSave());
		saveOntologyButton.addActionListener(e -> {
			try {
				handleOntologySave();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		bottomPanel.add(saveOutputButton);
		bottomPanel.add(saveOntologyButton);
		bottomPanel.add(closeButton);

		add(bottomPanel, BorderLayout.SOUTH);
	}

	private void handleTestSelection() {
		String selectedFileName = (String) fileCombobox.getSelectedItem();

		if (selectedFileName == null || "NO TEST".equals(selectedFileName)) {
			JOptionPane.showMessageDialog(this, "No test selected.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String resourcePath = "tests/" + selectedFileName;
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				JOptionPane.showMessageDialog(this, "Selected test file not found: " + selectedFileName, "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			selectedFile = convertInputStreamToFile(inputStream);

			startProcessFile();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error loading the test file: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public static File convertInputStreamToFile(InputStream inputStream) throws IOException {
		File tempFile = File.createTempFile("tempFile", ".tmp");

		tempFile.deleteOnExit();

		try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		}

		return tempFile;
	}

	private void handleFileSelection() {
		JFileChooser fileChooser = new JFileChooser("./tests");
		int value = fileChooser.showOpenDialog(this);
		if (value == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			startProcessFile();
		}
	}

	private void handleOutputSave() {
		if (outputText.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "There is no output to save.", "Information",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(lastDirectory);
		fileChooser.setDialogTitle("Save Output");
		fileChooser.setSelectedFile(new File("output.txt"));

		int userSelection = fileChooser.showSaveDialog(this);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File saveFile = fileChooser.getSelectedFile();
			try (FileWriter writer = new FileWriter(saveFile)) {
				writer.write(outputText.getText());
				JOptionPane.showMessageDialog(this, "Output saved to " + saveFile.getAbsolutePath(), "Success",
						JOptionPane.INFORMATION_MESSAGE);
				lastDirectory = saveFile.getParentFile();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	private void handleLogSave() {
		File logfile = new File("filoLog.txt");
		if (logfile.exists()) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(lastDirectory);
			fileChooser.setDialogTitle("Save Log File");
			fileChooser.setSelectedFile(logfile);
			int userSelection = fileChooser.showSaveDialog(this);
			if (userSelection == JFileChooser.APPROVE_OPTION) {
				File saveFile = fileChooser.getSelectedFile();
				try (BufferedReader reader = new BufferedReader(new FileReader("filoLog.txt"));
						FileWriter writer = new FileWriter(saveFile)) {
					String line;
					while ((line = reader.readLine()) != null) {
						writer.write(line + System.lineSeparator());
					}
					JOptionPane.showMessageDialog(null, "File copied to " + saveFile.getAbsolutePath(), "Success",
							JOptionPane.INFORMATION_MESSAGE);
					lastDirectory = saveFile.getParentFile();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Error saving file: " + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "There is no log to save.", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void handleOntologySave() throws IOException {
		if (solver.getSolution() != null) {
			try {
				OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

				File defaultFile = new File("solution.owl");
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(lastDirectory);
				fileChooser.setDialogTitle("Save Ontology File");
				fileChooser.setSelectedFile(defaultFile);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int userSelection = fileChooser.showSaveDialog(this);

				if (userSelection == JFileChooser.APPROVE_OPTION) {
					File saveFile = fileChooser.getSelectedFile();

					if (!saveFile.getName().toLowerCase().endsWith(".owl")) {
						saveFile = new File(saveFile.getAbsolutePath() + ".owl");
					}
					OWLOntology ontology = solver.getSolution().toOntology(solver.getAtomManager());

					manager.saveOntology(ontology, new OWLXMLDocumentFormat(), IRI.create(saveFile.toURI()));

					JOptionPane.showMessageDialog(null, "Ontology saved: " + saveFile.getAbsolutePath());
					lastDirectory = saveFile.getParentFile();
				}
			} catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
				JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
			}
		} else {
			JOptionPane.showMessageDialog(this, "The problem has no solution.", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void handleShowStatistics() {
		StatisticWindow statisticWindow = new StatisticWindow(solver, this);
		statisticWindow.showStatisticWindow();
	}

	private void startProcessFile() {
		loadingScreen loadingScreen = new loadingScreen(this);
		loadingScreen.showLoadingScreen();

		new Thread(() -> {
			processFile();
			loadingScreen.closeLoadingScreen();
		}).start();
	}

	private void processFile() {
		try {
			new FileWriter("filoLog.txt").close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		outputText.setText("");

		 try {
		        if (solver.ini(selectedFile)) {
		            solver.solve1();
		            message = solver.getMessage();
		            addOutput("Selected file: " + selectedFile.getPath() + "\n" + message);
		            result = solver.getResult();
		            addOutput(result);
		        } else {
		            addOutput("Error: Cannot read ontology.");
		        }
		    } catch (RuntimeException e) {
		        addOutput("The selected ontology is not supported.");
		        return;
		    }
	}

	private void addOutput(String text) {
		outputText.append(text + "\n");
		outputText.setCaretPosition(outputText.getDocument().getLength());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("message".equals(evt.getPropertyName())) {
			addOutput("Filo read the unification problem:\n" + evt.getNewValue());
		} else if ("result".equals(evt.getPropertyName())) {
			addOutput("Result: " + evt.getNewValue());
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(MainWindow::new);
	}
}
