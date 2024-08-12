package dto.requests;

public class RequestDTO {
    private int requestID;
    private SubmitRequestDTO submitRequestDTO;
    private int running;
    private int done;
    private RequestStatus requestStatus;

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public SubmitRequestDTO getSubmitRequestDTO() {
        return submitRequestDTO;
    }

    public void setSubmitRequestDTO(SubmitRequestDTO submitRequestDTO) {
        this.submitRequestDTO = submitRequestDTO;
    }

    public int getRunning() {
        return running;
    }

    public void setRunning(int running) {
        this.running = running;
    }

    public int getDone() {
        return done;
    }

    public void setDone(int done) {
        this.done = done;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }
}
