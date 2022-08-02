package core.mvc.asis;

import java.util.HashMap;
import java.util.Map;
import next.controller.CreateUserController;
import next.controller.HomeController;
import next.controller.ListUserController;
import next.controller.LoginController;
import next.controller.LogoutController;
import next.controller.ProfileController;
import next.controller.UpdateFormUserController;
import next.controller.UpdateUserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestMapping {

    private static final Logger logger = LoggerFactory.getLogger(RequestMapping.class);
    private static final Map<String, Controller> MAPPINGS = new HashMap<>();

    void initMapping() {
        MAPPINGS.put("/", new HomeController());
        MAPPINGS.put("/users/form", new ForwardController("/user/form.jsp"));
        MAPPINGS.put("/users/loginForm", new ForwardController("/user/login.jsp"));
        MAPPINGS.put("/users", new ListUserController());
        MAPPINGS.put("/users/login", new LoginController());
        MAPPINGS.put("/users/profile", new ProfileController());
        MAPPINGS.put("/users/logout", new LogoutController());
        MAPPINGS.put("/users/create", new CreateUserController());
        MAPPINGS.put("/users/updateForm", new UpdateFormUserController());
        MAPPINGS.put("/users/update", new UpdateUserController());

        logger.info("Initialized Request Mapping!");
        MAPPINGS.keySet().forEach(path -> {
            logger.info("Path : {}, Controller : {}", path, MAPPINGS.get(path).getClass());
        });
    }

    public Controller findController(String url) {
        return MAPPINGS.get(url);
    }

    void put(String url, Controller controller) {
        MAPPINGS.put(url, controller);
    }
}
