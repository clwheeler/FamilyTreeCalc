package CousinCalculator;

//Java includes
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.net.*;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.*;


public class CousinCalculator {
	
	public static final String WIKIDATA_STRING_QUERY_URL = "https://www.wikidata.org/w/api.php?action=wbgetentities&format=xml&props=labels&languages=en&ids=%1$s";
	public static final String WIKIDATA_ENTITY_SEARCH_URL = "https://www.wikidata.org/w/api.php?action=wbgetentities&format=xml&props=labels&languages=en&sites=enwiki&titles=%1$s";
	public static final String WIKIDATA_ID_QUERY_URL = "https://www.wikidata.org/w/api.php?action=wbgetclaims&format=xml&entity=%1$s&property=%2$s";
	public static final String WIKIDATA_ID_ENTITY_QUERY_URL = "https://www.wikidata.org/w/api.php?action=wbgetclaims&format=xml&entity=%1$s";
	public static final String WIKIDATA_QUERY_SEARCH = "https://wdq.wmflabs.org/api?q=tree[30][150][17,131]%20and%20claim[138:676555]";
			
	/**
	 * @param args
	 * @author cwheeler (clwheeler@gmail.com)
	 * Parameters: 
	 * Person 1
	 * Person 2 
	 * Number of steps to search (default 3)
	 * 
	 * TODO:
	 * - Return multiple results of same length
	 * - Use JTextPane for pretty results formatting
	 * 
	 * Some issues:
	 * - It detects the _shortest_ path, which may not be the most interesting one
	 * - If there are multiple paths of the same length, behavior is undefined (returning ALL paths = to the min length would be a reasonable improvement)
	 * - might not be well behaved for Wikimedia API - set header, etc.
	 * 
	 * Improvements
	 * - Check box: Stop building tree when you hit the target 
	 * -- Requires a BFS person creation, so you can stop at depth=X
	 * - give me all paths INCLUDING X person (i.e. for A->Z, return A-B-X-Z, instead of shorter A-C-Z)
	 * - Search for person
	 * -- search probably needs to use WikiDataQuery API: https://wdq.wmflabs.org/api_documentation.html
	 * - Tree Visualization
	 * - Node should probably have a list of lists instead of m_fatherList, etc, with each sub-list accessed by index value, to make adding new relationships easier
	 * - Improve error handling
	 * 
	 * 
	 * Optimizations
	 * - Could search as tree is being built. If connection is within 2 people, no need to query to 4 people even if that's the limit
	 * - Reduce the number of HTML requests: Currently, 5 / person.
	 * -- probably should simply request all properties at once, then parse them individually.
	 */
	
	public static void main(String[] args) {
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	CousinCalcUI myUI = new CousinCalcUI();
                myUI.setupUI();
                myUI.setupListeners();
            }
        });

		/*
		String sourcename=FamilyTreeManager.getInstance().getSourceString();
		String searchname=FamilyTreeManager.getInstance().getDestString();
		int querydepth=FamilyTreeManager.getInstance().getSearchDepth();
		
		System.out.println("Searching from " + sourcename + " to " + searchname + ", up to " + querydepth + " steps");
		
		FamilyTreeNode root = new FamilyTreeNode(sourcename, querydepth);
		
		System.out.println("Created " + FamilyTreeManager.getInstance().getTotalPersonCount() + " people with a depth of " + querydepth);
		System.out.println("=======================");
		
		LinkedList<FamilyTreeNode> resultPath = root.searchFor(searchname, new LinkedList<FamilyTreeNode>());
		if(resultPath != null) {
			System.out.println(FamilyTreeManager.getInstance().getRelationPath(resultPath));	
		}
		else {
			System.out.println("No relation found");
		}		
		*/
	}
}