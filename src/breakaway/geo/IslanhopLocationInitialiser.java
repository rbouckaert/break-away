package breakaway.geo;


import java.util.List;

import beast.base.core.BEASTObject;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.StateNode;
import beast.base.inference.StateNodeInitialiser;
import beast.base.inference.parameter.IntegerParameter;
import beast.base.core.Input.Validate;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beast.base.util.Randomizer;

@Description("State node initialiser for the locations in a break-away model")
public class IslanhopLocationInitialiser extends BEASTObject implements StateNodeInitialiser {
	public Input<IntegerParameter> initInput = new Input<IntegerParameter>("initial","location parameter to be initialised", Validate.REQUIRED);
	public Input<Tree> treeInput = new Input<Tree>("tree", "tree structure to sample from", Validate.REQUIRED);

	private Integer [] locations;
	
	@Override
	public void initAndValidate() {
		// make sure this initialiser is last in the list of initialisers
		for (Input<?> input : listInputs()) {
			if (input.getName().equals("init")) {
				if (input.get() instanceof List) {
					List<Object> list = (List) input.get();
					if (list.contains(this) && list.get(list.size()-1) != this) {
						// ensure this object is last
						list.remove(this);
						list.add(this);
					}
					
				}
			}
		}
	}

	@Override
	public void initStateNodes() {
		Tree tree = treeInput.get();
		locations = new Integer[tree.getNodeCount()];
		positionInternlNodes(tree.getRoot());
		IntegerParameter traitParameter = new IntegerParameter(locations);
		initInput.get().assignFromWithoutID(traitParameter);
		initInput.get().setBounds(0, tree.getLeafNodeCount());
	}

	@Override
	public void getInitialisedStateNodes(List<StateNode> stateNodes) {
		stateNodes.add(initInput.get());
	}
	private void positionInternlNodes(Node node) {
		if (!node.isLeaf()) {
			positionInternlNodes(node.getLeft());
			positionInternlNodes(node.getRight());
			
			if (Randomizer.nextBoolean()) {
				locations[node.getNr()] = node.getLeft().getNr();
			} else {
				locations[node.getNr()] = node.getRight().getNr();				
			}
		}
		
	}
}
