package com.davidlacarta.strava.sync.client.strava;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Strava Client Test
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("TEST")
public class StravaClientTest {

    @Autowired
    StravaClient stravaClient;

    @Test
    public void test() {

//        String code = "c37f82d63756a4c75e8625a701342ec0ea5abf6a";
//        Token token = stravaClient.getToken(code);
//        Strava strava = stravaClient.getStrava(token);
//        StravaAthlete stravaAthlete = stravaClient.getAthlete(strava);
//        System.out.println("Login ok: " + stravaAthlete.getFirstname());

    }

    @Configuration
    static class ContextConfiguration {

        @Bean
        StravaClient stravaClient() {
            return new StravaClient();
        }
    }
}
