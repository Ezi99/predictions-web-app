package engine.execution;

import dto.definition.WorldInfoDTO;

import java.util.Date;

public class Termination {
    private Integer secondsToEnd;
    private Integer ticksToEnd;
    private Date startDate;
    private Integer currentSeconds;
    private Date lastChecked;

    public Termination() {
        secondsToEnd = null;
        ticksToEnd = null;
        currentSeconds = 0;
    }

    public Termination(Termination termination){
        this.secondsToEnd = termination.secondsToEnd;
        this.ticksToEnd = termination.ticksToEnd;
        currentSeconds = 0;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setLastChecked(Date lastChecked) {
        this.lastChecked = lastChecked;
    }

    public boolean isSecondsToEndFinished() {
        Date now = new Date();
        Integer timeElapsed = (int) ((now.getTime() - this.lastChecked.getTime()) / 1000);
        if (timeElapsed >= 1) {
            currentSeconds = currentSeconds + timeElapsed;
            lastChecked = new Date();
        }
        if (secondsToEnd != null) {
            return currentSeconds >= secondsToEnd;
        } else {
            return false;
        }
    }

    public boolean isTickToEndFinished(int counter) {
        if (ticksToEnd != null) {
            return ticksToEnd == counter;
        } else {
            return false;
        }
    }

    public void setWorldInfoDTOTermination(WorldInfoDTO worldInfoDTO) {
        if (ticksToEnd != null) {
            worldInfoDTO.setTicksToEnd(ticksToEnd);
        }
        if (secondsToEnd != null) {
            worldInfoDTO.setSecondsToEnd(secondsToEnd);
        }

    }

    public Integer getCurrentSeconds() {
        return currentSeconds;
    }

    public Integer getSecondsToEnd() {
        return secondsToEnd;
    }

    public void setSecondsToEnd(Integer secondsToEnd) {
        this.secondsToEnd = secondsToEnd;
    }

    public Integer getTicksToEnd() {
        return ticksToEnd;
    }

    public void setTicksToEnd(Integer ticksToEnd) {
        this.ticksToEnd = ticksToEnd;
    }

}
