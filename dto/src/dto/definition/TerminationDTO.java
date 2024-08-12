package dto.definition;

public class TerminationDTO {
    private final Double secondToEnd;
    private final Double ticksToEnd;

    public TerminationDTO(Double secondToEnd, Double ticksToEnd) {
        this.secondToEnd = secondToEnd;
        this.ticksToEnd = ticksToEnd;
    }

    public Double getSecondToEnd() {
        return secondToEnd;
    }

    public Double getTicksToEnd() {
        return ticksToEnd;
    }
}
