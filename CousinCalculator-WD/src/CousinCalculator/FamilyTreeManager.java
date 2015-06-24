package CousinCalculator;

import java.util.*;

import CousinCalculator.CousinCalcUI.SearchUpdateListener;

public class FamilyTreeManager {

	private String m_sourceString;
	private String m_destString;
	private int m_searchDepth;
	
	private FamilyTreeNode m_treeRoot;
	private TreeMap<String, FamilyTreeNode> m_allPeopleList;
	private SearchUpdateListener m_updateListener;
	
	private static final FamilyTreeManager ftm_instance = new FamilyTreeManager();
	
	public static FamilyTreeManager getInstance() {
		return ftm_instance;
	}
	
	public FamilyTreeManager() {
		m_allPeopleList = new TreeMap<String, FamilyTreeNode>();
		m_sourceString = "Q1339109";
		m_destString = "Q4691505";
		m_searchDepth = 3;

		m_updateListener = null;
		m_treeRoot = null;
	}

	public String getSourceString() { return m_sourceString; }	
	public String getDestString() { return m_destString;  }
	public int getSearchDepth() { return m_searchDepth; }
	
	public void setSourceString(String new_source) { m_sourceString = new_source; }
	public void setDestString(String new_dest) { m_destString = new_dest; }
	public void setSearchDepth(int new_depth) { m_searchDepth = new_depth; }
	
	public void addNewNode(String newID, FamilyTreeNode node) {
		m_allPeopleList.put(newID, node);
		if(m_updateListener != null)
			m_updateListener.sendUpdate(node.getDisplayName());
	}
	
	public FamilyTreeNode findUserID(String findID) {
		return m_allPeopleList.get(findID);
	}

	public int getTotalPersonCount() {
		return m_allPeopleList.size();
	}

	public void generateTree(SearchUpdateListener sul) {
		m_sourceString = XMLHelper.getEntityIDFromString(m_sourceString);
		m_allPeopleList.clear();
		
		m_updateListener = sul;
		m_treeRoot = new FamilyTreeNode(m_sourceString, m_searchDepth);	
		m_updateListener = null;
	}
	
	public String doRelationPathSearch(SearchUpdateListener sul) {
		if(m_treeRoot == null) {
			sul.sendUpdate("No family tree exists to search");
			return null;
		}
		sul.sendUpdate("Searching...");
		m_destString = XMLHelper.getEntityIDFromString(m_destString);
		LinkedList<FamilyTreeNode> path = m_treeRoot.searchFor(m_destString, new LinkedList<FamilyTreeNode>());
		
		if(path == null || path.isEmpty())
			return "No relation found";
		else
			return getRelationPath(path);
	}
	
	public String doRelationPathBFS(SearchUpdateListener sul) {
		if(m_treeRoot == null) {
			return "No family tree exists to search";
		}
		sul.sendUpdate("Searching...");
		String target_string = XMLHelper.getEntityIDFromString(m_destString);
		
		LinkedList<LinkedList<FamilyTreeNode>> search_queue = new LinkedList<LinkedList<FamilyTreeNode>>();
		LinkedList<FamilyTreeNode> rootPath = new LinkedList<FamilyTreeNode>();
		LinkedList<FamilyTreeNode> alreadySearched = new LinkedList<FamilyTreeNode>();
		int search_depth=0;
		rootPath.addLast(m_treeRoot);
		search_queue.addLast(rootPath);
		alreadySearched.add(m_treeRoot);
		
		while(!search_queue.isEmpty()) {			
			LinkedList<FamilyTreeNode> this_path = search_queue.removeFirst();
			if(this_path.size() > search_depth+1) {
				search_depth++;
				//sul.sendUpdate(String.format("Searching depth %1$s", search_depth));
			}
			
			FamilyTreeNode path_end_node = this_path.getLast();
			
			if(target_string.equalsIgnoreCase(path_end_node.getWikiDataID()))
				return getRelationPath(this_path);
			
			//otherwise, add each child to queue
			ArrayList<LinkedList<FamilyTreeNode>> allAdjacent = path_end_node.getAllAdjacentNodes();
			for(LinkedList<FamilyTreeNode> relation_list : allAdjacent) {
				for(FamilyTreeNode child_node : relation_list) {
					if(!alreadySearched.contains(child_node)) {
						//mark child as searched so we don't duplicate it
						alreadySearched.add(child_node);
						LinkedList<FamilyTreeNode> new_path = new LinkedList<FamilyTreeNode>(this_path);
						new_path.addLast(child_node);
						search_queue.addLast(new_path);
					}
				}				
			}
		}
		
		return "No relation found";
	}
	
	public String getRelationPath(LinkedList<FamilyTreeNode> relationpath) {
		StringBuilder path_sb = new StringBuilder();

		FamilyTreeNode lastNode = null;
		for(FamilyTreeNode node : relationpath) {
			if(lastNode == null) {
				path_sb.append(node.getDisplayName());
				lastNode = node;
				continue;
			}

			path_sb.append("\n");
			path_sb.append(lastNode.getRelationTo(node));
			path_sb.append(node.getDisplayName());
			lastNode = node;
		}		
		return path_sb.toString();
	}
	
}
