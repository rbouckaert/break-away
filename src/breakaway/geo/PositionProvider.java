package breakaway.geo;

import beast.base.core.Description;

@Description("provides position as node number in a graph, used for logging positions on a tree")
public interface PositionProvider {
	/**
	 * 
	 * @param dim = node number of node in a tree
	 * @return node number of graph where the node in the tree is located
	 */
	int getPosition(int dim);
}
