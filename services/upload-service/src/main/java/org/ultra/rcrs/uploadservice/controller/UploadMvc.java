package org.ultra.rcrs.uploadservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UploadMvc {

    @GetMapping("/upload")
    public String uploadPage(Model model) {
        return "upload-page";

    }
}
