package admin.utils;

public class Constants {
    public final static String FILE_NAME = "file";
    public final static int REFRESH_RATE = 2000;
    public final static int START_RATE = 2000;
    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/PredictionsServlets";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;
    //////////////
    public static final String WORLD_ID = "worldID";
    public static final String WALKTHROUGH_TYPE = "walkthroughType";

    //////////////
    // Server resources locations
    public final static String ADMIN_LOGIN = FULL_SERVER_PATH + "/admin/login";
    public final static String LOGOUT = FULL_SERVER_PATH + "/logout";
    public final static String LOAD_FILE = FULL_SERVER_PATH + "/admin/load/file";
    public static final String SIMULATION_LIST = FULL_SERVER_PATH + "/simulation/list";
    public static final String WORLD_INFO = FULL_SERVER_PATH + "/world/info";
    public static final String ADMIN_ALLOCATIONS = FULL_SERVER_PATH + "/admin/allocations/requests";
    public static final String SET_THREAD_POOL = FULL_SERVER_PATH + "/threadPool";
    public static final String SIMULATION_STATE = FULL_SERVER_PATH + "/simulation/state";
    public static final String SIMULATION_RUN = FULL_SERVER_PATH + "/simulation/run";

}
