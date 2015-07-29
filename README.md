# FamilyTreeCalc

Simple Java implementation of a Family Tree Generator based on wikidata. 
Intended mostly for generating family trees of Royal families.
Once the tree is built, traces shortest relationship path between 2 people.

Implementation notes:
- Builds family tree based on Wikidata properties (Mother, Father, Child, Spouse)
- DFS to build tree based on wikidata queries
- Variable search depth of tree to generate
- BFS to search tree once queries are complete
