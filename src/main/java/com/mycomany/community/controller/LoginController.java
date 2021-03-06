package com.mycomany.community.controller;

import com.google.code.kaptcha.Producer;
import com.mycomany.community.dao.UserMapper;
import com.mycomany.community.entity.User;
import com.mycomany.community.services.UserService;
import com.mycomany.community.util.MailClient;
import com.mycomany.community.util.RedisKeyUtil;
import com.mycomany.community.util.communityConstant;
import com.mycomany.community.util.communityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements communityConstant{

    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private UserMapper userMapper;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;



    @RequestMapping(path="/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path="/login", method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path="/register", method = RequestMethod.POST)
    public String register(Model model, User user){
       Map<String,Object> map = userService.register(user);

       if(map==null || map.isEmpty()){
           model.addAttribute("msg","register account success,please check your email for activiation");
           model.addAttribute("target","/index");
           return "/site/operate-result";
       }else{
           model.addAttribute("usernameMsg",map.get("usernameMsg"));
           model.addAttribute("passwordMsg",map.get("passwordMsg"));
           model.addAttribute("emailMsg",map.get("emailMsg"));
          return "/site/register";

       }

    }

    @RequestMapping(path = "/forgetPassword",method = RequestMethod.GET)
    public String forgetPassword(){
        return "/site/forget";
    }

    @RequestMapping(path = "/forgetPassword",method = RequestMethod.POST)
    public String forgetPassword(Model model, User user, String code, String newPassword){

        Map<String, Object> map = userService.resetPassword(user,code,newPassword);
        if(map==null || map.isEmpty()){
            model.addAttribute("target","/login");
            return "/site/login";

        }else{
            model.addAttribute("emailMsg",map.get("emailMsg"));
            model.addAttribute("codeMsg",map.get("codeMsg"));
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            return "/site/forget";
        }

    }

    @RequestMapping(path = "/sendCode", method = RequestMethod.POST)
    @ResponseBody
    public String sendCode(String email){

        if(email==null){
            return communityUtil.getJSONString(1,"please enter the email");
        }

        User user = userMapper.selectByEmail(email);

        // send verification code to user email
         Context context = new Context();
        context.setVariable("username",user.getUsername());

         // generate the verification code.
         String code = communityUtil.generateUUID().substring(0,4);
         context.setVariable("code",code);

         String content = templateEngine.process("/mail/forget",context);
         mailClient.sendMail(email,"reset password",content);


         if(user==null){
             return communityUtil.getJSONString(1,"please enter valid email");
         }

         String redisKey = RedisKeyUtil.getCodeKey(user.getId());
         redisTemplate.opsForValue().set(redisKey,code,300,TimeUnit.SECONDS);


        return communityUtil.getJSONString(0,"verification code sended");
    }




    @RequestMapping(path="/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {

    int result = userService.activation(userId,code);

    if(result == ACTIVATION_SUCCESS){
        model.addAttribute("msg"," activation success,your account is ready for uses");
        model.addAttribute("target","/login");

    }else if(result== ACTIVATION_REPECT){
        model.addAttribute("msg"," activation fail,your account is already activated");
        model.addAttribute("target","/index");

    }else{
        model.addAttribute("msg"," activation fail, wrong activate code entered");
        model.addAttribute("target","/index");

    }
        return "/site/operate-result";
    }

    @RequestMapping(path="/kaptcha", method=RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){

        String text = kaptchaProducer.createText();

       BufferedImage image = kaptchaProducer.createImage(text);

//        session.setAttribute("kaptcha", text);

        String kaptchaOwner = communityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);

        response.addCookie(cookie);
        // insert verification code into redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);



       response.setContentType("image/png");
       try {
           OutputStream os = response.getOutputStream();
           ImageIO.write(image,"png",os);
       } catch(IOException e){
           logger.error("Error:" +e.getMessage());
       }
    }

    @RequestMapping(path="login" , method=RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, /*HttpSession session,*/ HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){

//        String kaptcha = (String)session.getAttribute("kaptcha");

        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }


        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||
        !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","wrong code entered");
            return "/site/login";
        }

        // username and password validations
        int expiredSeconds = rememberMe? REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;

        Map<String,Object> map=userService.login(username,password,expiredSeconds);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);

            response.addCookie(cookie);

            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));

            return "/site/login";
        }

    }

    @RequestMapping(path="/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){

        userService.logout(ticket);

        SecurityContextHolder.clearContext();

        return "redirect:/login";
    }







}
