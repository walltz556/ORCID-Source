package org.orcid.scheduler.tasks;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.orcid.core.manager.v3.NotificationManager;
import org.orcid.core.manager.v3.ProfileEntityManager;
import org.orcid.core.manager.v3.read_only.EmailManagerReadOnly;
import org.orcid.persistence.dao.ProfileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class ProcessUnclaimedRecordsImpl implements ProcessUnclaimedRecords {

    protected static final Logger LOG = LoggerFactory.getLogger(ProcessUnclaimedRecordsImpl.class);

    private static final int INDEXING_BATCH_SIZE = 100;

    private int claimReminderAfterDays = 8;

    @Value("${org.orcid.core.claimWaitPeriodDays:10}")
    private int claimWaitPeriodDays;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private ProfileDao profileDaoReadOnly;

    @Resource(name = "profileEntityManagerV3")
    private ProfileEntityManager profileEntityManager;

    @Resource(name = "notificationManagerV3")
    private NotificationManager notificationManager;

    @Resource(name = "emailManagerReadOnlyV3")
    private EmailManagerReadOnly emailManager;

    @Override
    synchronized public void processUnclaimedProfilesToFlagForIndexing() {
        LOG.info("About to process unclaimed profiles to flag for indexing");
        List<String> orcidsToFlag = Collections.<String> emptyList();
        do {
            orcidsToFlag = profileDaoReadOnly.findUnclaimedNotIndexedAfterWaitPeriod(claimWaitPeriodDays, claimWaitPeriodDays * 2, INDEXING_BATCH_SIZE, orcidsToFlag);
            LOG.info("Got batch of {} unclaimed profiles to flag for indexing", orcidsToFlag.size());
            for (String orcid : orcidsToFlag) {
                LOG.info("About to flag unclaimed profile for indexing: {}", orcid);
                profileEntityManager.updateLastModifed(orcid);
            }
        } while (!orcidsToFlag.isEmpty());
    }

    @Override
    synchronized public void processUnclaimedProfilesForReminder() {
        LOG.info("About to process unclaimed profiles for reminder");
        List<String> orcidsToRemind = Collections.<String> emptyList();
        do {
            orcidsToRemind = profileDaoReadOnly.findUnclaimedNeedingReminder(claimReminderAfterDays, INDEXING_BATCH_SIZE, orcidsToRemind);
            LOG.info("Got batch of {} unclaimed profiles for reminder", orcidsToRemind.size());
            for (final String orcid : orcidsToRemind) {
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        LOG.info("About to process unclaimed profile for reminder: {}", orcid);
                        String primaryEmail = emailManager.findPrimaryEmail(orcid).getEmail();
                        notificationManager.sendClaimReminderEmail(orcid, claimWaitPeriodDays - claimReminderAfterDays, primaryEmail);
                    }
                });
            }
        } while (!orcidsToRemind.isEmpty());
    }
}
