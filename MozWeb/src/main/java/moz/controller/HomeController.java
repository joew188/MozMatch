package moz.controller;


import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Created by Laxton-Joe on 2016/7/15.
 */
@Controller
public class HomeController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Map<String, Object> model) {


        model.put("title", "title");
        model.put("msg", "helloWorldService");

        return "index";
    }
    @RequestMapping(value = "/home/login", method = RequestMethod.GET)
    public String login()
    {
        return "home/login";
    }
}
