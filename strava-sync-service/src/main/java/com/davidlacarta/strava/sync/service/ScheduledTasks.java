package com.davidlacarta.strava.sync.service;

import com.davidlacarta.strava.sync.client.decathloncoach.DecathlonCoachClient;
import com.davidlacarta.strava.sync.client.strava.StravaClient;
import com.davidlacarta.strava.sync.model.domain.Activity;
import com.davidlacarta.strava.sync.model.domain.User;
import com.davidlacarta.strava.sync.repository.ActivityRepository;
import com.davidlacarta.strava.sync.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javastrava.api.v3.model.StravaUploadResponse;
import javastrava.api.v3.service.Strava;

/**
 * Scheduled Tasks
 */
@Component
public class ScheduledTasks {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
    @Value("${strava.upload.activities}")
    public Boolean stravaUploadActivities;
    private UserRepository userRepository;
    private ActivityRepository activityRepository;
    private StravaClient stravaClient;
    private DecathlonCoachClient decathlonCoachClient;

    @Autowired
    public ScheduledTasks(UserRepository userRepository,
                          ActivityRepository activityRepository,
                          StravaClient stravaClient,
                          DecathlonCoachClient decathlonCoachClient) {

        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.stravaClient = stravaClient;
        this.decathlonCoachClient = decathlonCoachClient;

    }

    @Scheduled(fixedDelayString = "${strava.sync.activities.time.seconds}000")
    public void syncActivitiesScheduled() throws IOException {

        LOGGER.debug("Sync activities...");
        syncActivities();

    }

    private void syncActivities() throws IOException {

        List<User> users = userRepository.findAll();
        LOGGER.debug("Users: {}", users.size());

        for (User user : users) {

            String username = user.getIdCoach();
            String password = user.getPasswordCoach();
            String codeStrava = user.getCodeStrava();
            Boolean sync = user.getSync();
            LOGGER.debug("User database, username:[{}] password:[{}] codeStrava:[{}] sync:[{}]",
                    username, password, codeStrava, sync);

            if (Optional.ofNullable(username).isPresent()
                    && Optional.ofNullable(password).isPresent()
                    && Optional.ofNullable(codeStrava).isPresent()
                    && sync) {

                LOGGER.debug("(decathloncoach) login...");
                HashMap<String, String> coachCookies = decathlonCoachClient.login(username, password);

                LOGGER.debug("(decathloncoach) getActivities...");
                HashMap<String, String> activitiesCoach = decathlonCoachClient.getActivities(coachCookies);

                LOGGER.debug("Activities decathloncoach: {}", activitiesCoach.size());
                Set<Activity> activitiesUser = Optional.ofNullable(user.getActivities()).orElse(new HashSet<>());
                LOGGER.debug("Activities database: {}", activitiesUser.size());

                activitiesUser.stream()
                        .filter(Activity::getSync)
                        .forEach(activity -> activitiesCoach.remove(activity.getKeyCoach()));
                LOGGER.debug("Activities decathloncoach for sync: {}", activitiesCoach.size());

                if (!activitiesCoach.isEmpty()) {

                    LOGGER.debug("(Strava) getToken...");
                    Strava strava = stravaClient.getStrava(stravaClient.getToken(codeStrava));

                    for (Map.Entry<String, String> acvitityEntry : activitiesCoach.entrySet()) {
                        syncActivity(user, coachCookies, activitiesUser, strava, acvitityEntry.getKey(), acvitityEntry.getValue());
                    }
                }
            }
        }
    }

    private void syncActivity(User user,
                              HashMap<String, String> coachCookies,
                              Set<Activity> activitiesUser,
                              Strava strava,
                              String keyActivity,
                              String urlActivity) throws IOException {

        LOGGER.debug("(decathloncoach) downloading activity {} ...", keyActivity);
        byte[] activityGpx = decathlonCoachClient.downloadGpxFile(urlActivity, coachCookies);

        if (stravaUploadActivities) {
            LOGGER.debug("(Strava) uploading activity {} ...", keyActivity);
            try {
                StravaUploadResponse response = stravaClient.uploadGpx(strava, activityGpx);
                LOGGER.debug("Upload {}: {}", response.getStatus());
            } catch (Exception e) {
                LOGGER.error("Sync activity:{}", e.getMessage());
            }
        }

        Activity activity = activitiesUser.stream()
                .filter(a -> a.getKeyCoach().equals(keyActivity))
                .findFirst()
                .orElseGet(() -> {
                    LOGGER.debug("Activity NEW {}", keyActivity);
                    Activity activityNew = new Activity();
                    activityNew.setKeyCoach(keyActivity);
                    activityNew.setUser(user);
                    return activityNew;
                });

        activity.setSync(Boolean.TRUE);
        activity.setSyncDate(LocalDateTime.now());

        activityRepository.save(activity);
        LOGGER.debug("Activity SAVE {}", keyActivity);
    }
}
