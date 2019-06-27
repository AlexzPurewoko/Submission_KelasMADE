package id.apwdevs.moviecatalogue.model;

public class FilmTopBilledCastModel {
    private String actorName;
    private String aliasPeople;

    FilmTopBilledCastModel(String actorName, String aliasPeople) {
        this.actorName = actorName;
        this.aliasPeople = aliasPeople;
    }

    public FilmTopBilledCastModel() {
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getAliasPeople() {
        return aliasPeople;
    }

    public void setAliasPeople(String aliasPeople) {
        this.aliasPeople = aliasPeople;
    }
}
