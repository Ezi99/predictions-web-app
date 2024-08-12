package dto.definition;

import java.util.List;

public class SimulationListDTO {
    private final List<String> simulationsName;

    public SimulationListDTO(List<String> simulationsName) {
        this.simulationsName = simulationsName;
    }

    public List<String> getSimulationsName() {
        return simulationsName;
    }
}
