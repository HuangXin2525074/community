package com.mycomany.community.controller;


import com.mycomany.community.entity.User;
import com.mycomany.community.services.LikeService;
import com.mycomany.community.util.HostHolder;
import com.mycomany.community.util.communityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId){
        User user = hostHolder.getUser();

        likeService.like(user.getId(),entityType,entityId,entityUserId);

        // number of like
        long likeCount = likeService.findEntityLikeCount(entityType,entityId);
        // status
        int likeStatus = likeService.findEntityLikeStatus(user.getId(),entityType,entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        return communityUtil.getJSONString(0,null, map);
    }

}