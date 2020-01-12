package com.mycomany.community.controller;


import com.mycomany.community.entity.DiscussPost;
import com.mycomany.community.entity.Page;
import com.mycomany.community.entity.User;
import com.mycomany.community.services.DiscussPostService;
import com.mycomany.community.services.LikeService;
import com.mycomany.community.services.UserService;
import com.mycomany.community.util.communityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements communityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path="/index", method= RequestMethod.GET)
    public String getIndexPage(Model model, Page page){

        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

     List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());

     List<Map<String, Object>> discussPosts = new ArrayList<>();
     if(list!=null){
         for(DiscussPost post : list){

             Map<String, Object> map = new HashMap<>();
             map.put("post",post);
             User user = userService.findUserById(post.getUserId());
             map.put("user",user);

             long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
             map.put("likeCount",likeCount);


             discussPosts.add(map);

         }
     }

      model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage(){
        return "/error/404";
    }


}
