package it.nicolagiacchetta.github.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;;

public class ActivePullRequestsMap implements ActivePullRequests {

    private final static Logger LOGGER = LoggerFactory.getLogger(ActivePullRequestsMap.class.getName());

    private static final ActivePullRequestsMap INSTANCE = new ActivePullRequestsMap();

    private final ConcurrentHashMap<Integer, Object> pullRequestsMap;

    private ActivePullRequestsMap () {
        this.pullRequestsMap = new ConcurrentHashMap<>();
    }

    public static ActivePullRequests getInstance () {
        return INSTANCE;
    }

    @Override
    public void put(Integer number) {
        LOGGER.debug("Putting {} in the ActivePullRequestsMap...", number);
        pullRequestsMap.put(number, new Object());
        LOGGER.debug("...{} put in the ActivePullRequestsMap.", number);
    }

    @Override
    public void remove(Integer number) {
        LOGGER.debug("Removing {} from the ActivePullRequestsMap...", number);
        pullRequestsMap.remove(number);
        LOGGER.debug("...{} removed from the ActivePullRequestsMap.", number);
    }

    @Override
    public boolean isActive(Integer number) {
        return pullRequestsMap.containsKey(number);
    }
}
