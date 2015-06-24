package CousinCalculator;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class CousinCalcUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	//using HTML tags for cheap wrapping behavior
	private static final String UI_HELP_TEXT= "<html>Enter source person to generate family tree. <br>Name Syntax: <br> &nbsp &nbsp Wikidata ID (Q1339109) <br> &nbsp &nbsp Wikipedia normalized name (Stephen_I,_Count_of_Burgundy) <br> &nbsp &nbsp Wikipedia article title (Stephen I, Count of Burgundy) <br> Multiple searches can be run against a single tree without regenerating the tree <br >Large depths (> 4) may take several minutes to complete";
	private static final String TREE_RESULT_TEXT = "Generated %1$s people in %2$s seconds";
	private static final String DIVIDER_BAR_TEXT = "=============================";
	private static final String SEARCH_TIME_RESULT = "Search took %1$s seconds";
	private static final String TREEGEN_START_TEXT = "Generating tree from %1$s up to %2$s degrees";
	private static final String SEARCH_START_TEXT = "Searching for person: %1$s";
	
	JTextField m_sourceField;
	JTextField m_destField;
	JTextField m_depthField;
	JButton m_searchButton;
	JButton m_treeButton;
	JTextArea m_resultsArea;
	
	public CousinCalcUI()  {
		m_sourceField = null;
		m_destField = null;
		m_depthField = null;
		m_searchButton = null;
		m_treeButton = null;
		m_resultsArea = null;
	}

	public void setupUI() {
        setTitle("Cousin Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450,600);          
        
		Box top_level = new Box(BoxLayout.Y_AXIS);
		this.add(top_level);
		
		top_level.add(Box.createVerticalStrut(20));

		//instructions
		Box info_box = new Box(BoxLayout.X_AXIS);
		info_box.add(Box.createHorizontalStrut(20));
		info_box.add(new JLabel(UI_HELP_TEXT, JLabel.LEFT));
		info_box.add(Box.createHorizontalStrut(20));
		top_level.add(info_box);	
		
		top_level.add(Box.createVerticalStrut(10));
		
		//Source Field
		Box source_box = new Box(BoxLayout.X_AXIS);
		top_level.add(source_box);
		source_box.add(new JLabel("Source Person:   "));
		m_sourceField = new JTextField(FamilyTreeManager.getInstance().getSourceString());
		m_sourceField.getDocument().putProperty("name", "SOURCE");
		m_sourceField.setMaximumSize(new Dimension(300, 50));
		source_box.add(m_sourceField);
		
		top_level.add(Box.createVerticalStrut(10));
		
		//Depth Field
		Box depth_box = new Box(BoxLayout.X_AXIS);
		top_level.add(depth_box);
		depth_box.add(new JLabel("Tree Depth:        "));
		String depthint = Integer.toString(FamilyTreeManager.getInstance().getSearchDepth());
		m_depthField = new JTextField(depthint);
		m_depthField.getDocument().putProperty("name", "DEPTH");
		m_depthField.setMaximumSize(new Dimension(300, 50));
		depth_box.add(m_depthField);

		top_level.add(Box.createVerticalStrut(10));
		
		//"Search" Button
		m_treeButton = new JButton("Generate Tree");
		m_treeButton.setActionCommand("TreeButtonPressed");
		top_level.add(m_treeButton);
		
		top_level.add(Box.createVerticalStrut(10));
		
		//Destination Field
		Box dest_box = new Box(BoxLayout.X_AXIS);
		top_level.add(dest_box);
		dest_box.add(new JLabel("Dest Person:        "));
		m_destField = new JTextField(FamilyTreeManager.getInstance().getDestString());
		m_destField.getDocument().putProperty("name", "DEST");
		m_destField.setMaximumSize(new Dimension(300, 50));
		dest_box.add(m_destField);
		
		top_level.add(Box.createVerticalStrut(10));
		
		//"Search" Button
		m_searchButton = new JButton("Search");
		m_searchButton.setActionCommand("SearchButtonPressed");
		top_level.add(m_searchButton);
		
		top_level.add(Box.createVerticalStrut(10));
		
		//output text box
		m_resultsArea = new JTextArea(15, 5);
		JScrollPane sp = new JScrollPane(m_resultsArea);
		top_level.add(sp);
		
		setVisible(true);
	}

	public void setupListeners() {
		m_sourceField.getDocument().addDocumentListener(new MyFieldDocumentListener());
		m_destField.getDocument().addDocumentListener(new MyFieldDocumentListener());
		m_depthField.getDocument().addDocumentListener(new MyFieldDocumentListener());
		
		
		SearchButtonEventListener treeListener = new SearchButtonEventListener();
		treeListener.setButtonOwner(this);
		m_treeButton.addActionListener(treeListener);
		
		SearchButtonEventListener searchListener = new SearchButtonEventListener();
		searchListener.setButtonOwner(this);
		m_searchButton.addActionListener(searchListener);
	}
	
	public void enableButtons(boolean enable) {
		m_treeButton.setEnabled(enable);
		m_searchButton.setEnabled(enable);
	}
	
	public void updateResultsArea(final String to_add) {
		final JTextArea f_resultsArea = m_resultsArea;
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	f_resultsArea.append(to_add);
            	f_resultsArea.append("\n");
            	f_resultsArea.setCaretPosition(f_resultsArea.getDocument().getLength());
            }
        });
	}
	

	private class SearchButtonEventListener implements ActionListener {
		CousinCalcUI buttonOwner;
		void setButtonOwner(CousinCalcUI ui) {
			buttonOwner=ui;
		}
		
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand() == "TreeButtonPressed")
			{
				String sourcename=FamilyTreeManager.getInstance().getSourceString();
				int querydepth=FamilyTreeManager.getInstance().getSearchDepth();
			
				updateResultsArea(DIVIDER_BAR_TEXT);
				updateResultsArea(String.format(TREEGEN_START_TEXT, sourcename, querydepth));
				updateResultsArea(" ");
				
				new BuildTreeWorker(buttonOwner).execute();
			}
			if(e.getActionCommand() == "SearchButtonPressed")
			{
				String searchname=FamilyTreeManager.getInstance().getDestString();
			
				updateResultsArea(" ");
				updateResultsArea(DIVIDER_BAR_TEXT);
				updateResultsArea(String.format(SEARCH_START_TEXT, searchname));
				updateResultsArea(" ");
				
				new SearchWorker(buttonOwner).execute();
			}
		}		
	}
	
	public interface SearchUpdateListener {
		public void sendUpdate(String str);	
	}
	
	private class BuildTreeWorker extends SwingWorker<String, String> 
	implements SearchUpdateListener {

		int resultCount;
		long starttime;
		CousinCalcUI buttonOwner;

		BuildTreeWorker(CousinCalcUI ui) {
			buttonOwner = ui;
		}

		protected String doInBackground() {
			resultCount = 0;
			starttime = System.currentTimeMillis();
			
			buttonOwner.enableButtons(false);
			
			FamilyTreeManager.getInstance().generateTree(this);
			return TREE_RESULT_TEXT;
		}

		public void sendUpdate(String str) { publish(str); }

		protected void process(java.util.List<String> chunks) {
			for(String name : chunks)
			{
				resultCount++;
				updateResultsArea(name);
			}
		}

		protected void done() {

			try {
				String result = get();
				if(result != null) {
					long seconds = (System.currentTimeMillis() - starttime) / 1000;
					updateResultsArea(" ");
					updateResultsArea(DIVIDER_BAR_TEXT);
					updateResultsArea(" ");
					updateResultsArea(String.format(get(), resultCount, seconds));
					
					buttonOwner.enableButtons(true);
				}
			} 
			catch (InterruptedException ignore) {}
			catch (java.util.concurrent.ExecutionException e) { e.printStackTrace(); }
		}
	}
	
	private class SearchWorker extends SwingWorker<String, String> 
								implements SearchUpdateListener {		
		long starttime;
		CousinCalcUI buttonOwner;
		
		SearchWorker(CousinCalcUI ui) {
			buttonOwner = ui;
		}
		
		protected String doInBackground() {
			starttime = System.currentTimeMillis();
			buttonOwner.enableButtons(false);
			return FamilyTreeManager.getInstance().doRelationPathBFS(this);
			//return FamilyTreeManager.getInstance().doRelationPathSearch(this);
		}
		
		public void sendUpdate(String str) { publish(str); }
		
		protected void process(java.util.List<String> chunks) {
			for(String name : chunks)
			{
				updateResultsArea(name);
			}
		}
		
		protected void done() {
			
			try {
				String result = get();
				if(result != null) {
					long seconds = (System.currentTimeMillis() - starttime) / 1000;
					buttonOwner.enableButtons(true);
					
					updateResultsArea(" ");
					updateResultsArea(String.format(SEARCH_TIME_RESULT, seconds));
					updateResultsArea(" ");
					updateResultsArea(get());
				}
			} 
			catch (InterruptedException ignore) {}
	        catch (java.util.concurrent.ExecutionException e) { e.printStackTrace(); }
		}
	}
	
	private class MyFieldDocumentListener implements DocumentListener {
		public void insertUpdate(DocumentEvent e) {
			updateValue(e);
		}
		public void removeUpdate(DocumentEvent e) {
			updateValue(e);
		}
		public void changedUpdate(DocumentEvent e) {
			//do nothing
		}
		
		public void updateValue(DocumentEvent e) {
			Document doc = (Document)e.getDocument();
			try { 
				switch(doc.getProperty("name").toString()) {
				case "SOURCE":
					FamilyTreeManager.getInstance().setSourceString(doc.getText(0, doc.getLength()));
					break;
				case "DEST":
					FamilyTreeManager.getInstance().setDestString(doc.getText(0, doc.getLength())); 
					break;
				case "DEPTH":
					String newDepth = doc.getText(0, doc.getLength());
					int result = 1;
					try {
						result = Integer.parseInt(newDepth);
					}
					catch(NumberFormatException nfe)
					{
					}
					FamilyTreeManager.getInstance().setSearchDepth(result); 
					break;
				}
				} 
			catch (BadLocationException badLocationException) {
				System.out.println("Contents: Unknown");
				}
			}
	}

}
