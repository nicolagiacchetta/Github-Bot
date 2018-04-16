# GithubBot

To build and start the Github Bot application run the *start-bot.sh* script.

To run the app, you will need the following information: 
* Username and password of a Github user for the authentication.
* The username of the Github user, owner of the repo from where the Bot will be consuming the information.
* The name of the above mentioned repo.

## Assumptions and design info

The application has been built on the ideal assumption that the Github APIs would not provide any callback webhook. One thread polls every 10 seconds from the repository the informations about the pull requests. If a new pull request is found, a thread polling its comments every 2 seconds is started. The application keeps track of the pull requests already active (or rather for which a thread polling comments has already been started) in a ConcurrentHashMap. A second assumption has been made here: there is no need to restore the state of the application after the shut down (no info is persisted to a long term data storage).

## Todo Features

* Validation of input parameters
* Improve error management 
* Persist state of the app on a long term data storage
* Restore state of the app after shutdown

