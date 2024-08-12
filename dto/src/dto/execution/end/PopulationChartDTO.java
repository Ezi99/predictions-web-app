package dto.execution.end;


import java.util.HashMap;
import java.util.Map;

public class PopulationChartDTO {
    private String entityName;
    private final Map<Integer, Integer> populationTrack;

    public PopulationChartDTO() {
        populationTrack = new HashMap<>();
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }

    public Map<Integer, Integer> getPopulationTrack() {
        return populationTrack;
    }

    public void updatePopulation(Integer tick, Integer population){
        populationTrack.put(tick, population);
    }

    public Integer getPopulationInTick(Integer tick){
        return populationTrack.get(tick);
    }
}
