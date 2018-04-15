package it.nicolagiacchetta.github.repository;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.IssueService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubRepositoryManagerTest {

    @Mock
    private GithubRepositoryManager.PatchedPullRequestService pullRequestService;

    @Mock
    private IssueService issueService;

    @Mock
    private RepositoryId repositoryId;

    private int expectedCommentsOnPullRequest = 0;
    private final static int EXPECTED_COMMENTS_UPDATED_AFTER_TIME = 2;

    private final static long TIME_BEFORE = 1523619125000L;
    private final static long TIME = 1523619150000L;
    private final static long TIME_AFTER = 1523620528000L;


    @Before
    public void setUp () throws IOException {
        mockPullRequestService();
        mockIssueService();
    }

    @Test
    public void pollCommentsUpdatedAfterTest () throws IOException {
        GithubRepositoryManager githubRepositoryManager = new GithubRepositoryManager(pullRequestService, issueService, repositoryId);
        List<Comment> comments = githubRepositoryManager.pollCommentsUpdatedAfter(1, new Date(TIME));
        comments.forEach(comment -> {assertEquals(TIME_AFTER, comment.getUpdatedAt().getTime());});
        assertEquals("Failed on size of result", EXPECTED_COMMENTS_UPDATED_AFTER_TIME, comments.size());
    }

    @Test
    public void pollCommentsTest () throws IOException {
        GithubRepositoryManager githubRepositoryManager = new GithubRepositoryManager(pullRequestService, issueService, repositoryId);
        List<Comment> comments = githubRepositoryManager.pollComments(1);
        assertEquals(this.expectedCommentsOnPullRequest, comments.size());
    }

    private void mockPullRequestService () throws IOException {
        this.pullRequestService = mock(GithubRepositoryManager.PatchedPullRequestService.class);
        List<CommitComment> commitComments = Arrays.asList(buildDummyCommitComment(new Date(TIME_BEFORE)), buildDummyCommitComment(new Date(TIME_AFTER)));
        this.expectedCommentsOnPullRequest += commitComments.size();
        when(this.pullRequestService.getComments(any(), anyInt())).thenReturn(commitComments);
    }

    private CommitComment buildDummyCommitComment (Date updatedAt) {
        return (CommitComment) new CommitComment().setUpdatedAt(updatedAt);
    }

    private void mockIssueService () throws IOException {
        this.issueService = mock(IssueService.class);
        List<Comment> issueComments = Arrays.asList(buildDummyComment(new Date(TIME_BEFORE)), buildDummyComment(new Date(TIME_AFTER)));
        this.expectedCommentsOnPullRequest += issueComments.size();
        when(this.issueService.getComments(any(), anyInt())).thenReturn(issueComments);
    }

    private Comment buildDummyComment (Date updatedAt) {
        return new Comment().setUpdatedAt(updatedAt);
    }
}
