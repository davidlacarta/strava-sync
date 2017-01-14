package com.davidlacarta.strava.sync.client.decathloncoach;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decathlon Coach Client
 */
@Component
public class DecathlonCoachClient {

    private static final int TIMEOUT = 30000;

    private static final String USER_AGENT = "\"Mozilla/5.0 (Windows NT\" +\n" +
            "          \" 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2\"";

    private static final String COACH_URL = "http://www.decathloncoach.com";
    private static final String ACCOUNT_URL = "https://account.geonaute.com";

    private static final String LOGIN_FORM_URL = COACH_URL + "/es/portal";
    private static final String EXPORT_TO_GPX_URL = COACH_URL + "/widget/summary/export-to-gpx";
    private static final String ACTIVITIES_URL = COACH_URL + "/es-FR/portal/activities";

    private static final String RESPONSE_TYPE = "code";
    private static final String CLIENT_ID = "mygeonaute";
    private static final String REDIRECT_URI = COACH_URL + "/es/portal";
    private static final String LOGIN_ACTION_URL = ACCOUNT_URL +
            "/oauth/authorize" +
            "?response_type=" + RESPONSE_TYPE +
            "&client_id=" + CLIENT_ID +
            "&redirect_uri=" + REDIRECT_URI;

    private static final String[] EXPORT_TO_GPX_PARAMS = new String[]{
            "ldid", "token", "firstname", "lastname", "gender", "birthdate", "email", "lang", "level"};

    private static final String EXPORT_TO_GPX_PARAMS_REGEXP = "window.Context\\s*=\\s*(\\{.*\\})";

    public HashMap<String, String> getActivities(HashMap<String, String> cookies) throws IOException {

        Document activities = getActivitiesCoach(cookies);

        return parseActivities(activities);

    }

    public byte[] downloadGpxFile(String urlGpxfile, HashMap<String, String> cookies) throws IOException {

        Connection.Response file = Jsoup
                .connect(urlGpxfile)
                .timeout(TIMEOUT)
                .cookies(cookies)
                .ignoreContentType(true)
                .execute();

        return file.bodyAsBytes();

    }

    public HashMap<String, String> login(String username, String password) throws IOException {

        HashMap<String, String> cookies = new HashMap<>();

        // Login form
        Connection.Response loginForm = Jsoup
                .connect(LOGIN_FORM_URL)
                .timeout(TIMEOUT)
                .method(Connection.Method.GET)
                .userAgent(USER_AGENT)
                .execute();

        cookies.putAll(loginForm.cookies());

        // Get the value of authenticity_token with the CSS selector we saved before
        HashMap<String, String> formData = new HashMap<>();
        formData.put("redirect_uri", REDIRECT_URI);
        formData.put("client_id", CLIENT_ID);
        formData.put("response_type", RESPONSE_TYPE);
        formData.put("email", username);
        formData.put("password", password);

        // Login action
        Connection.Response homePage = Jsoup.connect(LOGIN_ACTION_URL)
                .timeout(TIMEOUT)
                .cookies(cookies)
                .data(formData)
                .method(Connection.Method.POST)
                .userAgent(USER_AGENT)
                .execute();

        cookies.putAll(homePage.cookies());

        return cookies;

    }

    private Document getActivitiesCoach(HashMap<String, String> cookies) throws IOException {

        return Jsoup.connect(ACTIVITIES_URL)
                .timeout(TIMEOUT)
                .cookies(cookies)
                .userAgent(USER_AGENT)
                .get();

    }

    private HashMap<String, String> parseActivities(Document activities) throws IOException {

        HashMap<String, String> activitiesGpxUrl = new HashMap<>();

        Matcher matcher = Pattern.compile(EXPORT_TO_GPX_PARAMS_REGEXP).matcher(activities.html());

        if (matcher.find()) {

            JsonNode exportToGpxParams = new ObjectMapper().readTree(matcher.group(1));

            JsonNode user = exportToGpxParams.get("user");

            StringBuilder activityGpxUrlbuilder = new StringBuilder(EXPORT_TO_GPX_URL);
            // Add params url
            for (String param : EXPORT_TO_GPX_PARAMS) {

                JsonNode paramNode = user.get(param);
                String paramText = Optional.ofNullable(paramNode.textValue()).isPresent()
                        ? paramNode.textValue()
                        : paramNode.toString();

                activityGpxUrlbuilder.append(param.equals(EXPORT_TO_GPX_PARAMS[0]) ? "?" : "&")
                        .append(param)
                        .append("=")
                        .append(URLEncoder.encode(paramText, "UTF-8"));
            }
            // Add activity url
            String activityGpxUrl = activityGpxUrlbuilder.append("&")
                    .append("activityId")
                    .append("=")
                    .toString();

            // Add activities url
            Iterator<String> activitiesId = exportToGpxParams.get("activities").fieldNames();
            while (activitiesId.hasNext()) {
                String activityId = activitiesId.next();
                activitiesGpxUrl.put(activityId, activityGpxUrl + activityId);
            }
        }

        return activitiesGpxUrl;
    }

}
