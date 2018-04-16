package it.nicolagiacchetta.github.persistence;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ActivePullRequestsMapTest {

    private static final Integer NUMBER = new Integer(7);

    private static ActivePullRequests activePullRequests = ActivePullRequestsMap.getInstance();

    @Test
    public void isActiveTest () {
        assertFalse("Failed assert before put", activePullRequests.isActive(NUMBER));

        activePullRequests.put(NUMBER);
        assertTrue("Failed assert after put", activePullRequests.isActive(NUMBER));

        activePullRequests.remove(NUMBER);
        assertFalse("Failed assert after remove", activePullRequests.isActive(NUMBER));
    }
}