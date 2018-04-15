package it.nicolagiacchetta.github.persistence;

public interface ActivePullRequests {

    void put (Integer number);
    void remove (Integer number);
    boolean isActive (Integer number);

}
