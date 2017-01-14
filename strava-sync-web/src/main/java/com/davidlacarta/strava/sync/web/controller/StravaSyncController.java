package com.davidlacarta.strava.sync.web.controller;

import com.davidlacarta.strava.sync.client.strava.StravaClient;
import com.davidlacarta.strava.sync.model.domain.User;
import com.davidlacarta.strava.sync.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javastrava.api.v3.model.StravaAthlete;

/**
 * Strava Sync Controller
 *
 * <p>Step 1: BASE_MAPPING (User not logged), redirect CONNECT</p> <p>Step 2: CONNECT (User not
 * logged), template CONNECT_TEMPLATE, click CONNECT_STRAVA</p> <p>Step 3: CONNECT_STRAVA (User not
 * logged), redirect [strava] STRAVA_AUTHORIZE_URL</p> <p>Step 4: [strava] STRAVA_AUTHORIZE_URL
 * (User not loggued), authorize app (only first time), redirect EXCHANGE</p> <p>Step 5: EXCHANGE
 * (User not logged), login user (save code strava in session), redirect BASE_MAPPING</p> <p>Step 6:
 * BASE_MAPPING (User logged), create user in data base, template BASE_MAPPING_TEMPLATE, fill user
 * and password decathlon coach and click submit USER</p> <p>Step 7: USER (User logged), save user
 * and password decathlon coach in data base, redirect BASE_MAPPING</p> <p>Step 8: BASE_MAPPING
 * (User logged), template BASE_MAPPING_TEMPLATE, click LOGOUT</p> <p>Step 9: LOGOUT (User logged),
 * logout (delete session attributes), redirect BASE_MAPPING (Step 1)</p>
 */
@Controller
@Scope("session")
@RequestMapping(value = StravaSyncController.BASE_MAPPING)
public class StravaSyncController {

    static final String BASE_MAPPING = "/";
    private static final Logger LOGGER = LoggerFactory.getLogger(StravaSyncController.class);
    private static final String BASE_MAPPING_TEMPLATE = "index";
    private static final String CONNECT = "/connect";
    private static final String CONNECT_TEMPLATE = "stravaConnect";
    // redirects
    private static final String CONNECT_STRAVA = "/connect/strava";
    private static final String EXCHANGE = "/exchange";
    private static final String USER = "/user";
    private static final String LOGOUT = "/logout";

    private static final String STRAVA_AUTHORIZE_URL = "https://www.strava.com/oauth/authorize" +
            "?client_id=%s" + // param 1: client id
            "&response_type=code" +
            "&redirect_uri=%s" + EXCHANGE +// param 2: redirect uri
            "&scope=write"; //&state=mystate&approval_prompt=force";

    @Value("${strava.client_id}")
    public String stravaClientId;

    private UserRepository userRepository;
    private StravaClient stravaClient;

    @Autowired
    public StravaSyncController(UserRepository userRepository, StravaClient stravaClient) {
        this.userRepository = userRepository;
        this.stravaClient = stravaClient;
    }

    @GetMapping
    public String index(HttpServletRequest request, Model model) {

        String codeStrava = (String) request.getSession().getAttribute("codeStrava");
        // code strava
        if (!Optional.ofNullable(codeStrava).isPresent()) {
            LOGGER.debug("Context path: {}", request.getContextPath());
            return "redirect:" + request.getContextPath() + CONNECT;
        }
        // athlete strava
        StravaAthlete athlete = (StravaAthlete) request.getSession().getAttribute("athlete");
        if (!Optional.ofNullable(athlete).isPresent()) {
            athlete = stravaClient.getAthlete(stravaClient.getStrava(stravaClient.getToken(codeStrava)));
            request.getSession().setAttribute("athlete", athlete);

        }
        // user
        User user = (User) request.getSession().getAttribute("user");
        if (!Optional.ofNullable(user).isPresent()) {
            // find user
            user = userRepository.findByIdStrava(athlete.getId().toString());
            // new user
            if (!Optional.ofNullable(user).isPresent()) {
                user = new User();
                user.setSync(Boolean.FALSE);
                user.setIdStrava(athlete.getId().toString());
            }
            // update code strava
            if (!codeStrava.equals(user.getCodeStrava())) {
                user.setCodeStrava(codeStrava);
                userRepository.save(user);
            }
            request.getSession().setAttribute("user", user);
        }

        model.addAttribute("athlete", athlete);
        model.addAttribute("user", user);

        return BASE_MAPPING_TEMPLATE;

    }

    @GetMapping(CONNECT)
    public String connect() {

        return CONNECT_TEMPLATE;

    }

    @PostMapping(CONNECT_STRAVA)
    public String connectStrava(HttpServletRequest request) {

        String uriRedirect = String.format("%s://%s:%s%s",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort(),
                request.getContextPath());

        return "redirect:" + String.format(STRAVA_AUTHORIZE_URL, stravaClientId, uriRedirect);

    }

    @GetMapping(EXCHANGE)
    public String auth(HttpServletRequest request,
                       @RequestParam(value = "code") String code) {

        request.getSession().setAttribute("codeStrava", code);

        return "redirect:" + request.getContextPath() + BASE_MAPPING;
    }

    @PostMapping(USER)
    public String coach(HttpServletRequest request, HttpServletResponse response,
                        @ModelAttribute User userModel) {

        String codeStrava = (String) request.getSession().getAttribute("codeStrava");

        User user = userRepository.findByCodeStrava(codeStrava);
        Optional.ofNullable(user).ifPresent(u -> {

            u.setIdCoach(userModel.getIdCoach());
            u.setPasswordCoach(userModel.getPasswordCoach());
            u.setSync(userModel.getSync());

            userRepository.save(u);
            request.getSession().setAttribute("user", u);

        });

        return "redirect:" + request.getContextPath() + BASE_MAPPING;

    }

    @GetMapping(LOGOUT)
    public String logout(HttpServletRequest request) {

        HttpSession session = request.getSession();
        Collections.list(session.getAttributeNames())
                .forEach(session::removeAttribute);

        return "redirect:" + request.getContextPath() + BASE_MAPPING;

    }
}
