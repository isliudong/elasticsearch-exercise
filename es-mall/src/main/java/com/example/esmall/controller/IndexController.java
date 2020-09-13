package com.example.esmall.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.example.esmall.service.MallService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author 28415@hand-china.com 2020/08/26 20:35
 */
@Controller
public class IndexController {

    private final MallService mallService;

    public IndexController(MallService mallService) {
        this.mallService = mallService;
    }

    @GetMapping({"/","/index"})
    public String index(){
        return "index";
    }

    @ResponseBody
    @GetMapping("/initES/{keyword}")
    public String initEs(@PathVariable String keyword) throws IOException {
        mallService.initEs(keyword);
        return "success";
    }

    @ResponseBody
    @GetMapping("/search")
    public List<Map<String, Object>> search(String keyword, int page, int size) throws IOException {
        return mallService.searchKeyLight(keyword, page, size);
    }


}
