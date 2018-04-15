package it.nicolagiacchetta.github.pojos;


public class InputParameters {

    private String githubUsername;
    private String githubPassword;
    private String githubRepositoryOwner;
    private String githubRepositoryName;

    public InputParameters(String githubUsername,
                           String githubPassword,
                           String githubRepositoryOwner,
                           String getGithubRepositoryName) {
        this.githubUsername = githubUsername;
        this.githubPassword = githubPassword;
        this.githubRepositoryOwner = githubRepositoryOwner;
        this.githubRepositoryName = getGithubRepositoryName;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public String getGithubPassword() {
        return githubPassword;
    }

    public String getGithubRepositoryOwner() {
        return githubRepositoryOwner;
    }

    public String getGithubRepositoryName() {
        return githubRepositoryName;
    }
}
