package it.nicolagiacchetta.github;

import it.nicolagiacchetta.github.pojos.InputParameters;
import it.nicolagiacchetta.github.polling.PullRequestsPollingProcessor;
import it.nicolagiacchetta.github.repository.GithubRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class BotApplication {

    private final static Logger LOGGER = LoggerFactory.getLogger(BotApplication.class.getName());

    public static void main(String [] args) {
        LOGGER.info("BotApplication Main started");

        InputParameters inputParameters = getInputParametersFromUserInput();

        // TODO validate inputs or manage invalid input values

        GithubRepositoryManager githubRepositoryManager = new GithubRepositoryManager(inputParameters);
        new PullRequestsPollingProcessor(githubRepositoryManager).startPollingPullRequests();

        for (;;) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {}
        }
    }

    private static InputParameters getInputParametersFromUserInput() {
        Scanner in = new Scanner(System.in);
        System.out.print("Username for 'https://github.com': ");
        String user = in.next();
        System.out.print("Password for 'https://github.com': ");
        String password = in.next();
        System.out.print("Repository owner: ");
        String repositoryOwner = in.next();
        System.out.print("Repository Name: ");
        String repositoryName = in.next();

        return new InputParameters(user, password, repositoryOwner, repositoryName);
    }
}
