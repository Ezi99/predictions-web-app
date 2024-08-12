package user.ui.screen.result.node;

public class ResultTreeNode {
    private String name;
    private PropertyStatsNode propertyStatsNode;
    private PopulationGraphNode populationGraphNode;

    public ResultTreeNode(PropertyStatsNode propertyStatsNode) {
        this.propertyStatsNode = propertyStatsNode;
    }

    public ResultTreeNode(PopulationGraphNode propertyNode) {
        this.populationGraphNode = propertyNode;
    }

    public ResultTreeNode(String name) {
        this.name = name;
    }

    public PropertyStatsNode getPropertyNode() {
        return propertyStatsNode;
    }

    public void setPropertyNode(PropertyStatsNode propertyStatsNode) {
        this.propertyStatsNode = propertyStatsNode;
    }

    public PopulationGraphNode getPopulationGraphNode() {
        return populationGraphNode;
    }

    public void setPopulationGraphNode(PopulationGraphNode populationGraphNode) {
        this.populationGraphNode = populationGraphNode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        if(populationGraphNode != null){
            return populationGraphNode.toString();
        } else if (propertyStatsNode != null) {
            return propertyStatsNode.toString();
        } else {
            return name;
        }
    }
}
