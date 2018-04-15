package it.nicolagiacchetta.github.repository;

import it.nicolagiacchetta.github.pojos.InputParameters;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COMMENTS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_PULLS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

public class GithubRepositoryManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(GithubRepositoryManager.class.getName());

    private final PatchedPullRequestService pullRequestService;
    private final IssueService issueService;
    private final RepositoryId repositoryId;

    public GithubRepositoryManager (PatchedPullRequestService pullRequestService,
                                    IssueService issueService,
                                    RepositoryId repositoryId) {
        this.pullRequestService = pullRequestService;
        this.issueService = issueService;
        this.repositoryId = repositoryId;
    }

    public GithubRepositoryManager (InputParameters inputParameters) {
        LOGGER.info("Initializing Repository Manager for Github repository {}...", inputParameters.getGithubRepositoryName());
        GitHubClient client = new GitHubClient();
        client.setCredentials(inputParameters.getGithubUsername(), inputParameters.getGithubPassword());
        this.repositoryId = new RepositoryId(inputParameters.getGithubRepositoryOwner(), inputParameters.getGithubRepositoryName());
        this.pullRequestService = new PatchedPullRequestService(client);
        this.issueService = new IssueService(client);
        LOGGER.info("...Repository Manager for Github repository {} initialized.", inputParameters.getGithubRepositoryName());
    }

    public List<PullRequest> getOpenPullRequests () throws IOException {
        LOGGER.debug("Fetching list of open Pull Requests for repository {}...", repositoryId.getName());
        List<PullRequest> openPRs = this.pullRequestService.getPullRequests(this.repositoryId,"open");
        LOGGER.debug("...list of open Pull Requests for repository {} successfully fetched.", repositoryId.getName());
        return openPRs;
    }

    public List<Comment> pollCommentsUpdatedAfter (int pullRequestNumber, Date date) throws IOException {
        LOGGER.debug("Polling comments update after {} for Pull Request with number={}...", date.toString(), pullRequestNumber);
        List<Comment> comments = pollComments(pullRequestNumber).stream()
                                                                .filter(comment -> comment.getUpdatedAt().after(date))
                                                                .collect(Collectors.toList());
        LOGGER.debug("...found {} comments updated after {} for Pull Request with number={}.", comments.size(), date.toString(), pullRequestNumber);
        return comments;
    }

    public List<Comment> pollComments (int pullRequestNumber) throws IOException {
        LOGGER.debug("Polling comments for Pull Request with number={}...", pullRequestNumber);
        List<Comment> issueComments = pollIssueComments(pullRequestNumber);
        List<CommitComment> commitsComments = pollCommitsComments(pullRequestNumber);

        List<Comment> allCommentsOnPullRequest = Stream.concat(
                                                           issueComments.stream(),
                                                           commitsComments.stream())
                                                       .collect(Collectors.toList());
        LOGGER.debug("...found {} comments for Pull Request with number={}.", allCommentsOnPullRequest.size(), pullRequestNumber);
        return allCommentsOnPullRequest;
    }

    public List<CommitComment> pollCommitsComments (int pullRequestNumber) throws IOException {
        LOGGER.debug("Polling commmits comments for Pull Request with number={}...", pullRequestNumber);
        List<CommitComment> commitsComments = this.pullRequestService.getComments(this.repositoryId, pullRequestNumber);
        LOGGER.debug("...found {} commits comments for Pull Request with number={}.", commitsComments.size(), pullRequestNumber);
        return commitsComments;
    }

    public List<Comment> pollIssueComments (int issueNumber) throws IOException {
        LOGGER.debug("Polling comments for Issue with number={}...", issueNumber);
        List<Comment> issueComments = this.issueService.getComments(this.repositoryId, issueNumber);
        LOGGER.debug("...found {} comments for Issue with number={}.", issueComments.size(), issueNumber);
        return issueComments;
    }

    public void replyToCommitComment(int pullRequestNumber, Comment comment, String body) throws IOException {
        LOGGER.debug("Replying to commit comment with id={} for Pull Request with number={}...", comment.getId(), pullRequestNumber);
        this.pullRequestService.replyToCommitComment(this.repositoryId, pullRequestNumber, comment.getId(), body);
        LOGGER.debug("...successfully replied to commit comment with id={} for Pull Request with number={}.", comment.getId(), pullRequestNumber);
    }

    public void deleteCommitComment(long commentId) throws IOException {
        LOGGER.debug("Deleting commit comment with id={}...", commentId);
        this.pullRequestService.deleteComment(this.repositoryId, commentId);
        LOGGER.debug("...successfully deleted commit comment with id={}.", commentId);
    }

    public void createIssueComment(int pullRequestNumber, String body) throws IOException {
        LOGGER.debug("Creating issue comment for Pull Request with number={}...", pullRequestNumber);
        this.issueService.createComment(this.repositoryId, pullRequestNumber, body);
        LOGGER.debug("...issue comment for Pull Request with number={} created.", pullRequestNumber);
    }

    public String getRepositoryName () {
        return this.repositoryId.getName();
    }

    /**
     * This class has been created as a temporary fix to a bug found in the PullRequestService.replyToComment method.
     */
    public class PatchedPullRequestService extends PullRequestService {

        PatchedPullRequestService (GitHubClient client) {
            super(client);
        }

        public CommitComment replyToCommitComment(IRepositoryIdProvider repository,
                                                  int pullRequestId, long commentId, String body) throws IOException {
            String repoId = getId(repository);
            StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
            uri.append('/').append(repoId);
            uri.append(SEGMENT_PULLS);
            uri.append('/').append(pullRequestId);
            uri.append(SEGMENT_COMMENTS);
            Map<String, Object> params = new HashMap<>();
            params.put("in_reply_to", new Long(commentId));
            params.put("body", body);
            return client.post(uri.toString(), params, CommitComment.class);
        }
    }
}
