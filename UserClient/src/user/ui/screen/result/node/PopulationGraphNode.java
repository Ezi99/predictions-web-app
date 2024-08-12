package user.ui.screen.result.node;

import javafx.scene.chart.LineChart;

public class PopulationGraphNode {
    private final LineChart<Number, Number> lineChart;

    public PopulationGraphNode(LineChart<Number, Number> lineChart) {
        this.lineChart = lineChart;
    }

    public LineChart<Number, Number> getLineChart() {
        return lineChart;
    }

    @Override
    public String toString() {
        return "Population chart stats";
    }
}
