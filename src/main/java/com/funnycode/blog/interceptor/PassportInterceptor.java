package com.funnycode.blog.interceptor;

import com.funnycode.blog.configration.Constants;
import com.funnycode.blog.model.HostHolder;
import com.funnycode.blog.model.Ticket;
import com.funnycode.blog.model.User;
import com.funnycode.blog.service.TicketService;
import com.funnycode.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author CC
 * @date 2019-09-18 22:08
 */
@Component
public class PassportInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private Constants constants;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String ticket = null;
        if (httpServletRequest.getCookies() != null) {
            for (Cookie cookie : httpServletRequest.getCookies()) {
                if (cookie.getName().equals(constants.getTOKEN())) {
                    ticket = cookie.getValue();
                    break;
                }
            }
        }

        if (ticket != null) {
            Ticket loginTicket = ticketService.selectByTicket(ticket);
            if (loginTicket == null || loginTicket.getExpired().before(new Date()) || loginTicket.getStatus() != 0) {
                return true;
            }

            User user = userService.getUserByUserId(loginTicket.getUserId());
            hostHolder.setUser(user);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && hostHolder.getUser() != null) {
            modelAndView.addObject("user", hostHolder.getUser());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.clear();
    }
}
