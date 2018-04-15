package it.nicolagiacchetta.github.polling;

import it.nicolagiacchetta.github.repository.GithubRepositoryManager;
import it.nicolagiacchetta.github.utils.BotCommandsUtils;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.CommitComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CommentsPollingProcessor implements CommentsPoller {

    private final static Logger LOGGER = LoggerFactory.getLogger(CommentsPollingProcessor.class.getName());

    private final static long INITIAL_DELAY = 0L;
    private final static long PERIOD = 2L;
    private final static TimeUnit UNIT = TimeUnit.SECONDS;

    private final static ScheduledThreadPoolExecutor COMMENTS_POLLERS_EXECUTOR = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10);

    private final GithubRepositoryManager githubRepositoryManager;

    public CommentsPollingProcessor (GithubRepositoryManager githubRepositoryManager) {
        this.githubRepositoryManager = githubRepositoryManager;
    }

    @Override
    public void startPollingComments (int pullRequestNumber) {
        COMMENTS_POLLERS_EXECUTOR.scheduleAtFixedRate(createPollingTask(pullRequestNumber), INITIAL_DELAY, PERIOD, UNIT);
    }

    private CommentsPollingTask createPollingTask (int pullRequestNumber) {
        LOGGER.info("Init task to poll messages of Pull Request with number=" + pullRequestNumber + "...");
        CommentsPollingTask task = new CommentsPollingTask(this.githubRepositoryManager, pullRequestNumber);
        LOGGER.info("...task to poll messages of Pull Request with number=" + pullRequestNumber + " initialized.");
        return task;
    }

    static class CommentsPollingTask implements Runnable {

        private final GithubRepositoryManager githubRepositoryManager;
        private final int pullRequestNumber;

        private Date dateLastCommentChecked = Date.from(Instant.EPOCH);

        private CommentsPollingTask(GithubRepositoryManager githubRepositoryManager, int pullRequestNumber) {
            this.pullRequestNumber = pullRequestNumber;
            this.githubRepositoryManager = githubRepositoryManager;
        }

        @Override
        public void run() {
            try {
                List<Comment> commentsToCheck = this.githubRepositoryManager.pollCommentsUpdatedAfter(this.pullRequestNumber,
                                                                                                      this.dateLastCommentChecked);
                handleComments(commentsToCheck);

                updateDateLastCommentChecked(commentsToCheck);
            } catch (Throwable t) {
                LOGGER.error("Failed to poll comments on pull request with number={}", this.pullRequestNumber, t);
                // TODO After a failure, the polling of the comments of the pull requests will continue:
                // define a strategy to manage the occurrence of continuous failures
            }
        }

        private void updateDateLastCommentChecked(List<Comment> commentsToCheck) {
            Optional<Comment> youngerComment = commentsToCheck.stream()
                                                              .max(Comparator.comparing(Comment::getUpdatedAt));

            youngerComment.ifPresent(comment -> {
                if (comment.getUpdatedAt().after(this.dateLastCommentChecked))
                    this.dateLastCommentChecked = comment.getUpdatedAt();
            });
        }

        private void handleComments (List<Comment> comments) {
            LOGGER.debug("Handling {} new comments...", comments.size());
            comments.stream()
                    .filter(BotCommandsUtils::isHotComment)
                    .forEach(this::handleHotComment);
            LOGGER.debug("...{} new comments handled.", comments.size());
        }

        private void handleHotComment(Comment comment) {
            LOGGER.debug("Handling hot comment with id={}...", comment.getId());
            String command = BotCommandsUtils.extractCommand(comment);
            switch (command) {
                case "say-hello" :
                    replyToComment(comment, "Hello World");
                    break;

                default:
                    LOGGER.warn("Command '{}' not implemented", command);
            }
            LOGGER.debug("...hot comment with id={} handled.", comment.getId());
        }

        private void replyToComment (Comment comment, String responseBody) {
            try {
                LOGGER.info("Replying to comment with id={}...", comment.getId());
                if (comment instanceof CommitComment)
                    this.githubRepositoryManager.replyToCommitComment(this.pullRequestNumber, comment, responseBody);
                else
                    this.githubRepositoryManager.createIssueComment(this.pullRequestNumber, responseBody);
                LOGGER.info("...comment with id={} successfully replied.", comment.getId());
            } catch (Throwable t) {
                LOGGER.error("Failed to reply to comment with id={} on pull request with number={}", comment.getId(), this.pullRequestNumber, t);
                // TODO Manage failure issuing the reply
            }
        }

    }
}
