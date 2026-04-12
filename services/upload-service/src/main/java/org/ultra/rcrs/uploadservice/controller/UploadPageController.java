package org.ultra.rcrs.uploadservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UploadPageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
