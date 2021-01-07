package com.jesse.elasticsearch.view;

import com.jesse.elasticsearch.contorl.ESController;
import com.jesse.elasticsearch.entity.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * TODO
 *
 * @author jesse hsj
 * @date 2021/1/6 15:36
 */
@Controller
public class ViewController {

    @Autowired
    private ESController esController;

    @RequestMapping("/")
    public String index(ModelMap map,@RequestParam(required = false,defaultValue = "") String key) {
        // 加入一个属性，用来在模板中读取
        //map.addAttribute("name", userName);
        //map.addAttribute("bookTitle", bookTitle);
        // return模板文件的名称，对应src/main/resources/templates/welcome.html

        List<Movie> movies = esController.get(key);
        map.addAttribute("movies", movies);
        map.addAttribute("key", key);
        map.addAttribute("size", movies.size());

        return "index";
    }

}
