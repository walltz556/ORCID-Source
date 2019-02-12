package org.orcid.scheduler.tasks;

public interface ProcessUnclaimedRecords {

    void processUnclaimedProfilesToFlagForIndexing();
    void processUnclaimedProfilesForReminder();
}
