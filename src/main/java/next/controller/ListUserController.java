package next.controller;

import core.annotation.web.Controller;
import core.annotation.web.RequestMapping;
import core.db.DataBase;
import core.mvc.view.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class ListUserController {
    @RequestMapping("/users")
    public ModelAndView execute(HttpServletRequest req, HttpServletResponse resp) {
        if (!UserSessionUtils.isLogined(req.getSession())) {
            return ModelAndView.from("redirect:/users/loginForm");
        }

        ModelAndView modelAndView = ModelAndView.from("/user/list.jsp");
        modelAndView.addObject("users", DataBase.findAll());
        return modelAndView;
    }
}
