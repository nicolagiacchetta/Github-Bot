package it.nicolagiacchetta.github.polling;

import it.nicolagiacchetta.github.polling.PullRequestsPollingProcessor.PullRequestsPollingTask;
import it.nicolagiacchetta.github.repository.GithubRepositoryManager;
import org.eclipse.egit.github.core.PullRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PullRequestsPollingProcessorTest {

    @Mock
    private GithubRepositoryManager githubRepositoryManager;

    private int expectedNumberOfPullRequest = 0;
    private int expectedNumberOfPullRequestAfterUpdate = 0;

    @Before
    public void setUp () throws IOException {
        mockGithubRepositoryManager();
    }


    @Test
    public void startPollingCommentsForNewPullRequestsTest () throws IOException {
        PullRequestsPollingTask task = new PullRequestsPollingTask(this.githubRepositoryManager, new CommentsPollingProcessor(this.githubRepositoryManager));

        AtomicInteger counterOfPullRequestsStarted = new AtomicInteger(0);
        CommentsPoller poller = number -> {counterOfPullRequestsStarted.incrementAndGet();};

        task.startPollingCommentsForNewPullRequests(poller);
        assertEquals("Failed on first assertion", this.expectedNumberOfPullRequest, counterOfPullRequestsStarted.get());

        task.startPollingCommentsForNewPullRequests(poller);
        assertEquals("Failed on second assertion", this.expectedNumberOfPullRequest, counterOfPullRequestsStarted.get());

        task.startPollingCommentsForNewPullRequests(poller);
        assertEquals("Failed on third assertion", this.expectedNumberOfPullRequestAfterUpdate, counterOfPullRequestsStarted.get());

        task.startPollingCommentsForNewPullRequests(poller);
        assertEquals("Failed on fourth assertion", this.expectedNumberOfPullRequestAfterUpdate, counterOfPullRequestsStarted.get());
    }

    private void mockGithubRepositoryManager() throws IOException {
        this.githubRepositoryManager = mock(GithubRepositoryManager.class);
        when(this.githubRepositoryManager.getRepositoryName()).thenReturn("REPOSITORY_NAME");
        List<PullRequest> pullRequests = Arrays.asList(buildDummyPullRequest(1), buildDummyPullRequest(2));
        this.expectedNumberOfPullRequest = pullRequests.size();
        List<PullRequest> updatedPullRequests = Arrays.asList(buildDummyPullRequest(1), buildDummyPullRequest(2), buildDummyPullRequest(3));
        this.expectedNumberOfPullRequestAfterUpdate = updatedPullRequests.size();
        when(this.githubRepositoryManager.getOpenPullRequests()).thenReturn(pullRequests)
                                                                .thenReturn(pullRequests)
                                                                .thenReturn(updatedPullRequests)
                                                                .thenReturn(updatedPullRequests);
    }

    private PullRequest buildDummyPullRequest (int number) {
        return new PullRequest().setNumber(number);
    }
}
