package beast.geo;



import beast.core.Input;
import beast.core.Operator;
import beast.core.parameter.IntegerParameter;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;
import beast.core.Input.Validate;

public class IslandHopeMetaOperator extends Operator {

	public Input<Operator> operatorInput = new Input<>("operator", "operator that changes the topology of a tree", Validate.REQUIRED);
	public Input<Tree> treeInput = new Input<>("tree", "beast tree to be operated on", Validate.REQUIRED);
	public Input<IntegerParameter> locationInput = new Input<>("location", "location parameter to be sampled", Validate.REQUIRED);
	
	
	Operator operator;
	Tree tree;
	IntegerParameter location;
	
	
	@Override
	public void initAndValidate() {
		location = locationInput.get();
		tree = treeInput.get();
		operator = operatorInput.get();
	}

	@Override
	public double proposal() {
		double logHR = operator.proposal();
		if (Double.isInfinite(logHR)) {
			return logHR;
		}
		
		Node [] nodes = tree.getNodesAsArray();
		for (int i = tree.getLeafNodeCount(); i < nodes.length; i++) {
			//if (nodes[i].isDirty() == Tree.IS_FILTHY) {
			int current = location.getValue(i);
			if ((nodes[i].getLeft().getNr() != current && nodes[i].getRight().getNr() != current) || tree.childrenChanged(i)) {
				int newLocation = Randomizer.nextBoolean() ? nodes[i].getLeft().getNr() : nodes[i].getRight().getNr();
				location.setValue(i, newLocation);
			}
		}
		
		return logHR;
	}

}
