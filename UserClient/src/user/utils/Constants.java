package user.utils;

public class Constants {
    public final static String BASE_DOMAIN = "localhost";
    public static final long REFRESH_RATE = 2000;
    public static final long START_RATE = 2000;
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/PredictionsServlets";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;
    public static final String WORLD_ID = "worldID";
    public static final String REQUEST_ID = "requestID";
    public final static String USERNAME = "userName";
    public final static String CONTROL = "control";
    public final static String WALKTHROUGH_TYPE = "walkthroughType";


    // resource
    public static final String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/user/ui/login/login.fxml";
    public static final String CHAT_ROOM_FXML_RESOURCE_LOCATION = "/user/ui/body/body.fxml";

    //servlets
    public static final String LOGIN = FULL_SERVER_PATH + "/user/login";
    public final static String LOGOUT = FULL_SERVER_PATH + "/logout";
    public static final String SIMULATION_LIST = FULL_SERVER_PATH + "/simulation/list";
    public static final String WORLD_INFO = FULL_SERVER_PATH + "/world/info";
    public static final String REQUEST = FULL_SERVER_PATH + "/request";
    public static final String SIMULATION_RUN = FULL_SERVER_PATH + "/simulation/run";
    public static final String TERMINATION = FULL_SERVER_PATH + "/simulation/termination";
    public static final String SIMULATION_STATE = FULL_SERVER_PATH + "/simulation/state";
    public static final String SIMULATION_CONTROL = FULL_SERVER_PATH + "/simulation/control";
    public static final String ACTIVE_ENVIRONMENT = FULL_SERVER_PATH + "/activeEnvironment";
}
