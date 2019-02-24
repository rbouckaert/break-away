package beast.geo;

import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.parameter.IntegerParameter;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

@Description("translates integer state representing child node nrs to graph locations")
public class IslandHopLocationParameter extends IntegerParameter {
	public Input<Tree> treeInput = new Input<>("tree", "BEAST tree, used to find child nodes");
	public Input<IntegerParameter> locationInput = new Input<>("location", "location parameter to be sampled");
	public Input<Graph> graphInput = new Input<>("graph", "the graph specifying rates among nodes");
	public Input<Boolean> alwaysUpdateInput = new Input<>("alwaysUpdate", "set to true when sampling from prior", false);

	Tree tree;
	IntegerParameter location;
	Graph graph;
	
	boolean needsUpdate = true;
	boolean alwaysUpdate = false;
	
	
	public IslandHopLocationParameter() {
		valuesInput.setRule(Validate.OPTIONAL);
	}

	@Override
	public void initAndValidate() {
		this.tree = treeInput.get();
		this.location = locationInput.get();
		this.graph = graphInput.get();
		
		values = new Integer[tree.getNodeCount()];
		storedValues = new Integer[tree.getNodeCount()];
		m_bIsDirty = new boolean[tree.getNodeCount()];
		
		m_fLower = 0;
        m_fUpper = (graph != null ? graph.getSize() : 0);
        alwaysUpdate = alwaysUpdateInput.get();
	}
	
	private void collectLocations() {
		collectLocations(tree.getRoot());
		needsUpdate = alwaysUpdate;//false;
	}
	
	private void collectLocations(Node node) {
		if (node.isLeaf()) {
			values[node.getNr()] = location.getValue(node.getNr());
		} else {
			collectLocations(node.getLeft());
			collectLocations(node.getRight());
			
			int loc = location.getValue(node.getNr());
			values[node.getNr()] = values[loc];
			if (loc != node.getLeft().getNr() && loc != node.getRight().getNr()) {
				throw new IllegalArgumentException("Uh oh -- location should be left or right node, but is none of them: "
						+ loc +"!="+ node.getLeft().getNr() +" && "+ loc + "!="+ node.getRight().getNr() + "\n"
						+ "Check that there are no operators that change the tree topology, or if there are such operators, "
						+ "check that they are wrapped inside a IslandHopeMetaOperator.");
			}
		}
		
	}

	@Override
	public Integer getValue() {
		if (needsUpdate) {
			collectLocations();
		}
		return super.getValue();
	}
	
	@Override
	public Integer getValue(int param) {
		if (needsUpdate) {
			collectLocations();
		}
		return super.getValue(param);
	}
	
	@Override
	public Integer[] getValues() {
		if (needsUpdate) {
			collectLocations();
		}
		return super.getValues();
	}
	
	
	@Override
	protected boolean requiresRecalculation() {
		needsUpdate = true;
		return true;
	}
	
}
