package com.retroscore.dto;

import com.retroscore.enums.GameDifficulty;
import com.retroscore.enums.TimerDurations;
import lombok.Data;

@Data
public class UserSettingsDto {
    private boolean notificationsEnabled;
    private boolean matchReminders;
    private boolean scoreUpdates;
    private String preferredLeague;
    private boolean showHints;
    private GameDifficulty gameDifficulty;
    private TimerDurations timeLimit;


    public UserSettingsDto() {

    }

    public UserSettingsDto(boolean notificationsEnabled, boolean matchReminders, boolean scoreUpdates, String preferredLeague, boolean showHints, GameDifficulty gameDifficulty, TimerDurations timeLimit) {
        this.notificationsEnabled = notificationsEnabled;
        this.matchReminders = matchReminders;
        this.scoreUpdates = scoreUpdates;
        this.preferredLeague = preferredLeague;
        this.showHints = showHints;
        this.gameDifficulty = gameDifficulty;
        this.timeLimit = timeLimit;
    }


}
