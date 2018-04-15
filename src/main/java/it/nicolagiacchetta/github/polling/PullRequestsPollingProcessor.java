package it.nicolagiacchetta.github.polling;


import it.nicolagiacchetta.github.persistence.ActivePullRequests;
import it.nicolagiacchetta.github.persistence.ActivePullRequestsMap;
import it.nicolagiacchetta.github.repository.GithubRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PullRequestsPollingProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(PullRequestsPollingProcessor.class.getName());

    private final static ScheduledExecutorService PULL_REQUESTS_POLLER_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private final static long INITIAL_DELAY = 0L;
    private final static long PERIOD = 10L;
    private final static TimeUnit UNIT = TimeUnit.SECONDS;

    private final GithubRepositoryManager githubRepositoryManager;
    private final CommentsPollingProcessor commentsProcessor;

    public PullRequestsPollingProcessor (GithubRepositoryManager githubRepositoryManager) {
        this.githubRepositoryManager = githubRepositoryManager;
        this.commentsProcessor = new CommentsPollingProcessor(githubRepositoryManager);
    }

    public void startPollingPullRequests () {
        LOGGER.info("Start Polling pull requests on repository {}...", this.githubRepositoryManager.getRepositoryName());
        PullRequestsPollingTask task = new PullRequestsPollingTask(this.githubRepositoryManager, this.commentsProcessor);
        PULL_REQUESTS_POLLER_EXECUTOR.scheduleAtFixedRate(task, INITIAL_DELAY, PERIOD, UNIT);
        LOGGER.info("...polling pull requests on repository {} with period {} {} started.", task.repositoryName, PERIOD, UNIT);
    }

    static class PullRequestsPollingTask implements Runnable {

        private final GithubRepositoryManager githubRepositoryManager;
        private final ActivePullRequests activePullRequests = ActivePullRequestsMap.getInstance();
        private final String repositoryName;

        private final CommentsPollingProcessor commentsProcessor;

        public PullRequestsPollingTask(GithubRepositoryManager githubRepositoryManager,
                                       CommentsPollingProcessor commentsProcessor) {
            this.githubRepositoryManager = githubRepositoryManager;
            this.repositoryName = this.githubRepositoryManager.getRepositoryName();
            this.commentsProcessor = commentsProcessor;
        }

        @Override
        public void run() {
            try {
                startPollingCommentsForNewPullRequests(this.commentsProcessor::startPollingComments);
            } catch (Throwable t) {
                LOGGER.error("Failed to poll pull requests on repository with name={}", repositoryName, t);
                // TODO After a failure, the polling of the pull requests of the repository will continue:
                // define a strategy to manage the occurrence of continuous failures
            }
        }

        void startPollingCommentsForNewPullRequests(CommentsPoller commentsPoller) throws IOException {
            LOGGER.debug("Starting polling comments on pull requests of repository with name={}...", repositoryName);
            this.githubRepositoryManager.getOpenPullRequests()
                                        .stream()
                                        .mapToInt(pr -> pr.getNumber())
                                        .filter(number -> isNotActive(number))
                                        .forEach(number -> {
                                            commentsPoller.startPollingComments(number);
                                            this.activePullRequests.put(number);
                                        });
            LOGGER.debug("...polling comments on pull requests of repository with name={} successfully started.", repositoryName);
        }

        private boolean isNotActive (final int number) {
            return ! this.activePullRequests.isActive(number);
        }
    }
}
