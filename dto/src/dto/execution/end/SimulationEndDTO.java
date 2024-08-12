package dto.execution.end;

import dto.definition.EntityInfoDTO;

import java.util.List;
import java.util.Map;

public class SimulationEndDTO {
    private Map<String, PopulationChartDTO> populationChart;
    private List<PropertyHistogramDTO> propertyHistogram;
    private List<EntityInfoDTO> consistencies;

    public List<EntityInfoDTO> getConsistencies() {
        return consistencies;
    }

    public void setConsistencies(List<EntityInfoDTO> consistencies) {
        this.consistencies = consistencies;
    }

    public List<PropertyHistogramDTO> getPropertyHistogram() {
        return propertyHistogram;
    }

    public void setPropertyHistogram(List<PropertyHistogramDTO> propertyHistogram) {
        this.propertyHistogram = propertyHistogram;
    }

    public Map<String, PopulationChartDTO> getPopulationChart() {
        return populationChart;
    }

    public void setPopulationChart(Map<String, PopulationChartDTO> populationChart) {
        this.populationChart = populationChart;
    }


}
