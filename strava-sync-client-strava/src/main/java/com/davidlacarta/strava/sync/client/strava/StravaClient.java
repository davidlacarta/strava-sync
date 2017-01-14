package com.davidlacarta.strava.sync.client.strava;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javastrava.api.v3.auth.AuthorisationService;
import javastrava.api.v3.auth.impl.retrofit.AuthorisationServiceImpl;
import javastrava.api.v3.auth.model.Token;
import javastrava.api.v3.model.StravaAthlete;
import javastrava.api.v3.model.StravaUploadResponse;
import javastrava.api.v3.service.Strava;

/**
 * Strava Client
 */
@Component
public class StravaClient {

    private static final String NAME_FILE_DEFAULT = "Carrera %s";
    private static final String NAME_PATTERN = "<name>([\\s\\S]*?)<\\/name>";

    @Value("${strava.client_id}")
    public String stravaClientId;

    @Value("${strava.client_secret}")
    public String stravaClientSecret;

    public Token getToken(String code) {

        AuthorisationService service = new AuthorisationServiceImpl();

        return service.tokenExchange(Integer.valueOf(stravaClientId), stravaClientSecret, code);

    }

    public Strava getStrava(Token token) {

        return new Strava(token);

    }

    public StravaAthlete getAthlete(Strava strava) {

        return strava.getAuthenticatedAthlete();

    }

    public StravaUploadResponse uploadGpx(Strava strava, byte[] fileByteArray) throws IOException {

        String name = getName(fileByteArray);

        File file = new File(name);
        FileUtils.writeByteArrayToFile(file, fileByteArray);

        return strava.upload(null, name, null, null, null, null, "gpx", null, file);

    }

    private String getName(byte[] fileByteArray) {

        Matcher matcher = Pattern.compile(NAME_PATTERN).matcher(new String(fileByteArray));

        return matcher.find()
                ? matcher.group(1)
                : String.format(NAME_FILE_DEFAULT, LocalDate.now());

    }
}
