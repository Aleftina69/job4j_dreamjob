package ru.job4j.dreamjob.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@SuppressWarnings("unused")
public class IndexController {
    @GetMapping("/index")
    @ResponseBody
    public String getIndex() {
        return "Hello World!";
    }
}
