package dto.execution;

public enum SimulationState {
    PENDING, RUNNING, FINISHED, PAUSED;

    public static SimulationState convert(String state){
        SimulationState res;

        if(state.equalsIgnoreCase(SimulationState.RUNNING.name())){
            res = SimulationState.RUNNING;
        } else if(state.equalsIgnoreCase(SimulationState.PAUSED.name())){
            res = SimulationState.PAUSED;
        } else if(state.equalsIgnoreCase(SimulationState.FINISHED.name())){
            res = SimulationState.FINISHED;
        } else if(state.equalsIgnoreCase(SimulationState.PENDING.name())) {
            res = SimulationState.PENDING;
        } else {
            throw new IllegalArgumentException("invalid simulation state");
        }

        return res;
    }
}
