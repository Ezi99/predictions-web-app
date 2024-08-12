package dto.requests;


import java.util.Map;

public class AllocationRequestsDTO {
    private Map<String, Map<Integer, RequestDTO>> requests;

    public Map<String, Map<Integer, RequestDTO>> getRequests() {
        return requests;
    }

    public void setRequests(Map<String, Map<Integer, RequestDTO>> requests) {
        this.requests = requests;
    }
}
