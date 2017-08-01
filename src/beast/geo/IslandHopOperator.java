package beast.geo;

import beast.core.Operator;
import beast.core.Param;
import beast.core.parameter.IntegerParameter;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

public class IslandHopOperator extends Operator {
	
	Tree tree;
	IntegerParameter location;
	
	public IslandHopOperator() {}
	public IslandHopOperator(@Param(name="tree", description="BEAST tree, used to find child nodes") Tree tree,
			@Param(name="location", description="location parameter to be sampled") IntegerParameter location) {
		this.tree = tree;
		this.location = location;
	}
	
	@Override
	public void initAndValidate() {
	}

	@Override
	public double proposal() {
		int n = tree.getInternalNodeCount();
		int i = Randomizer.nextInt(n) + tree.getLeafNodeCount();
		Node node = tree.getNode(i);
		int current = location.getValue(i);
		if (Randomizer.nextBoolean()) {
			int newLocation = node.getLeft().getNr();
			if (newLocation != current) {
				location.setValue(i, newLocation);
			}
		} else {
			int newLocation = node.getRight().getNr();
			if (newLocation != current) {
				location.setValue(i, newLocation);
			}
		}
		return 0;
	}

	public Tree getTree() {
		return tree;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}

	public IntegerParameter getLocation() {
		return location;
	}

	public void setLocation(IntegerParameter location) {
		this.location = location;
	}

}
