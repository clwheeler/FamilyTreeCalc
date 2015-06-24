package CousinCalculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class FamilyTreeNode {

	public enum Relation {
		FATHER(0, "P22", "Child of: "),
		MOTHER(1, "P25", "Child of: "),
		SPOUSE(2, "P26", "Spouse of: "),
		CHILD(3, "P40", "Parent of: "),
		SISTER(4, "P7", "Sibling to: "),
		BROTHER(5, "P9", "Sibling to: ");
		
		//enum max value + 1
		static int getEnumCount() { return 6; }
		
		final int f_index;
		final String f_code;
		final String f_inverseRelation;
		
		private Relation(int index, String wd_code, String invRel) {
			f_index = index;
			f_code = wd_code;			
			f_inverseRelation = invRel;
		}
		
		int getIndex() { return f_index; }
		String getCode() { return f_code; }
		String getInverseRelation() { return f_inverseRelation; }
		
		private static final Map<Integer,Relation> lookup = new HashMap<Integer,Relation>();
		static { for(Relation w : EnumSet.allOf(Relation.class))lookup.put(w.getIndex(), w); }
				
		public static Relation getEnumFromIndex(int index) {
			return lookup.get(index);
		}
		
		private static final Map<String,Relation> codelookup = new HashMap<String,Relation>();
		static { for(Relation w : EnumSet.allOf(Relation.class))codelookup.put(w.getCode(), w); }
		
		public static Relation getEnumFromCode(String code) {
			return codelookup.get(code);
		}
	}
	
	private String m_wikiDataID;
	private String m_displayName;
	
	private int m_depthIndex;
	
	//links in the Family Tree
	ArrayList<LinkedList<FamilyTreeNode>> m_allChildrenArray;	
	
	public FamilyTreeNode(String person_id, int depth) {

		m_depthIndex = depth;
		m_wikiDataID = person_id;
		
		m_wikiDataID = XMLHelper.getEntityIDFromString(m_wikiDataID);
		m_displayName = this.loadDisplayName();
		
		m_allChildrenArray = new ArrayList<LinkedList<FamilyTreeNode>>();
		for(int i = 0; i < Relation.getEnumCount(); i++) {
			m_allChildrenArray.add(new LinkedList<FamilyTreeNode>());
		}
				
		FamilyTreeManager.getInstance().addNewNode(m_wikiDataID, this);
		
		//separate request for each property
		//for(Relation rel : Relation.values()) {
		//	getRelationshipsOfType(rel);
		//}
		getAllRelationships();
	}
	
	public ArrayList<LinkedList<FamilyTreeNode>> getAllAdjacentNodes()  {
		return m_allChildrenArray;
	}
	
	private String loadDisplayName() {
		StringBuilder name_sb = new StringBuilder();

		try	{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			
			String URLString = String.format(CousinCalculator.WIKIDATA_STRING_QUERY_URL, m_wikiDataID);
			
			URL sourceURL = new URL(URLString);
			HttpURLConnection yc = (HttpURLConnection)sourceURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

			//parse using builder to get DOM representation of the XML file
			Document wikidata_contents = db.parse(new InputSource(in));
			
			Element doc_root = wikidata_contents.getDocumentElement();
			
			Element entity = null;
			Element labels = null;
			
			Element entities = (Element)XMLHelper.getNode("entities", doc_root.getChildNodes());
			if(entities != null)
				entity = (Element)XMLHelper.getNode("entity", entities.getChildNodes());
			if(entity != null)
				labels = (Element)XMLHelper.getNode("labels", entity.getChildNodes());
		
			if(labels != null)
			{
				//for each claim
				NodeList label_list = labels.getElementsByTagName("label");
				for ( int x = 0; x < label_list.getLength(); x++ ) {
					Element label_node = (Element)label_list.item(x);
					
					//add numeric-id to ids_to_add;
					String value = label_node.getAttribute("value");
					name_sb.append(value);
					name_sb.append(" ");
				}
				in.close();
			}
		}
		catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch(SAXException se) {
			se.printStackTrace();
		}
		catch(IOException e) {
		}	
		
		return name_sb.toString();
	}
	
	private void getAllRelationships() {
		
		TreeMap<String, Relation> ids_to_add = new TreeMap<String, Relation>();
		
		try	{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			String URLString = String.format(CousinCalculator.WIKIDATA_ID_ENTITY_QUERY_URL, m_wikiDataID);
			
			URL sourceURL = new URL(URLString);
			HttpURLConnection yc =(HttpURLConnection)sourceURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

			//parse using builder to get DOM representation of the XML file
			Document wikidata_contents = db.parse(new InputSource(in));
			
			Element doc_root = wikidata_contents.getDocumentElement();
			
			Element property = null;

			Element claims = (Element)XMLHelper.getNode("claims", doc_root.getChildNodes());
			if(claims != null)
			{
				NodeList all_property_elements = claims.getElementsByTagName("property");
				for ( int elem = 0; elem < all_property_elements.getLength(); elem++ ) {
					property = (Element)all_property_elements.item(elem);

					Relation relation_type = Relation.getEnumFromCode(property.getAttribute("id"));

					if(relation_type != null)
					{						
						//for each claim in that property
						NodeList claim_list = property.getElementsByTagName("claim");
						for ( int x = 0; x < claim_list.getLength(); x++ ) {
							Element claim_node = (Element)claim_list.item(x);

							//extract numeric-id
							Element mainsnak = null;
							Element datavalue = null;
							Element value_node = null;

							mainsnak = (Element)XMLHelper.getNode("mainsnak", claim_node.getChildNodes());
							if(mainsnak != null)
								datavalue = (Element)XMLHelper.getNode("datavalue", mainsnak.getChildNodes());
							if(datavalue != null)
								value_node = (Element)XMLHelper.getNode("value", datavalue.getChildNodes());
							if (value_node != null)
							{
								String relation_id = "Q" + value_node.getAttribute("numeric-id");
								ids_to_add.put(relation_id, relation_type);
							}
						}
					}

				}
				in.close();
			}
		}
		catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch(SAXException se) {
			se.printStackTrace();
		}
		catch(IOException e) {
		}	

		for(Map.Entry<String,Relation> entry : ids_to_add.entrySet()) {
			  String key = entry.getKey();
			  Relation value = entry.getValue();

			  createChild(key, value);
			}
	}
	
	private void getRelationshipsOfType(Relation rel) {
		
		LinkedList<String> ids_to_add = new LinkedList<String>();
		
		try	{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			String URLString = String.format(CousinCalculator.WIKIDATA_ID_QUERY_URL, m_wikiDataID, rel.getCode());
			
			URL sourceURL = new URL(URLString);
			HttpURLConnection yc =(HttpURLConnection)sourceURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

			//parse using builder to get DOM representation of the XML file
			Document wikidata_contents = db.parse(new InputSource(in));
			
			Element doc_root = wikidata_contents.getDocumentElement();
			
			Element property = null;

			Element claims = (Element)XMLHelper.getNode("claims", doc_root.getChildNodes());
			if(claims != null)
				property = (Element)XMLHelper.getNode("property", claims.getChildNodes());
			
			if(property != null)
			{
				//for each claim
				NodeList claim_list = property.getElementsByTagName("claim");
				for ( int x = 0; x < claim_list.getLength(); x++ ) {
					Element claim_node = (Element)claim_list.item(x);

					//extract numeric-id
					Element mainsnak = null;
					Element datavalue = null;
					Element value_node = null;
					
					mainsnak = (Element)XMLHelper.getNode("mainsnak", claim_node.getChildNodes());
					if(mainsnak != null)
						datavalue = (Element)XMLHelper.getNode("datavalue", mainsnak.getChildNodes());
					if(datavalue != null)
						value_node = (Element)XMLHelper.getNode("value", datavalue.getChildNodes());
					if (value_node != null)
					{
						String relation_id = "Q" + value_node.getAttribute("numeric-id");
						ids_to_add.addLast(relation_id);
					}
				}
				in.close();
			}
		}
		catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch(SAXException se) {
			se.printStackTrace();
		}
		catch(IOException e) {
		}	

		createChildren(ids_to_add, rel);
	}
	
	public void createChild(String child_id, Relation rel) {
		if(!(m_depthIndex > 0))
			return;
		
		FamilyTreeNode new_node = FamilyTreeManager.getInstance().findUserID(child_id);
				
		if(new_node == null)
			new_node = new FamilyTreeNode(child_id, m_depthIndex - 1);
		else {
			//if node already exists, it may have been seen at a deeper level first, so it doesn't have the correct depth
			//so we want it to re-process with the correct depth
			
			//update its depth if the new depth is lower
			//if the depth changed, tell it to re-request it's children
			new_node.updateDepthIndex(m_depthIndex - 1);
		}
		
		m_allChildrenArray.get(rel.getIndex()).addLast(new_node);
	}
	
	public void createChildren(LinkedList<String> ids_to_add, Relation rel) {
		if(!(m_depthIndex > 0))
			return;
		
		for(String new_id : ids_to_add) {
			FamilyTreeNode new_node = FamilyTreeManager.getInstance().findUserID(new_id);
				
			if(new_node == null)
				new_node = new FamilyTreeNode(new_id, m_depthIndex - 1);
			else {
				//if node already exists, it may have been seen at a deeper level first, so it doesn't have the correct depth
				//so we want it to re-process with the correct depth
				
				//update its depth if the new depth is lower
				//if the depth changed, tell it to re-request it's children
				new_node.updateDepthIndex(m_depthIndex - 1);
			}
			
			m_allChildrenArray.get(rel.getIndex()).addLast(new_node);
		}

	}

	
	/*
	 * @return Shortest path from me that includes the destination node
	 * 			null if no path exists
	 */
	public LinkedList<FamilyTreeNode> searchFor(String search_id, LinkedList<FamilyTreeNode> path_to_me) {
		
		//if there is a loop
		if(path_to_me.contains(this)) {
			return null;
		}
		else if(search_id.equals(m_wikiDataID)) {
			LinkedList<FamilyTreeNode> new_path = new LinkedList<FamilyTreeNode>();
			new_path.addFirst(this);
			return new_path;
		}
		else {
			LinkedList<FamilyTreeNode> shortest_known_path = null;
			LinkedList<FamilyTreeNode> path_to_child = new LinkedList<FamilyTreeNode>(path_to_me);
			path_to_child.addLast(this);
			
			//iterate over all children
			for(LinkedList<FamilyTreeNode> aList : m_allChildrenArray) {
				for(FamilyTreeNode child : aList) {
					LinkedList<FamilyTreeNode> child_shortest_path = child.searchFor(search_id, path_to_child);

					if(child_shortest_path == null)
						continue;
					else if(shortest_known_path == null)
						shortest_known_path = child_shortest_path;
					else if(child_shortest_path.size() < shortest_known_path.size())
						shortest_known_path = child_shortest_path;
				}
				
			}
			if(shortest_known_path != null)
				shortest_known_path.addFirst(this);
			return shortest_known_path;
		}
	}
	
	public String getWikiDataID() {
		return m_wikiDataID;
	}
	
	public String getDisplayName() {
		return m_displayName;
	}

	//updates if the new depth is "lower"
	public void updateDepthIndex(int newIndex) {
		if(newIndex > m_depthIndex) {
			m_depthIndex=newIndex;
			for(Relation rel : Relation.values()) {
				getRelationshipsOfType(rel);
			}
		}
	}
	
	public String getRelationTo(FamilyTreeNode person) {		
		for(int i = 0; i < Relation.getEnumCount(); i++) {
			LinkedList<FamilyTreeNode> thisList = m_allChildrenArray.get(i);
			if(thisList.contains(person)) {
				Relation this_rel = Relation.getEnumFromIndex(i);
				return this_rel.getInverseRelation();
			}
			m_allChildrenArray.add(new LinkedList<FamilyTreeNode>());
		}
		
		return "No relation";
	}
}
