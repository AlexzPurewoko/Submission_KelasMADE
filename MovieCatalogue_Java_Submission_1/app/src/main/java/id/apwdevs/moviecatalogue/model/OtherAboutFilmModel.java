package id.apwdevs.moviecatalogue.model;

import java.util.ArrayList;

public class OtherAboutFilmModel {
    private String originalLanguage;
    private String longMovieRuntime;
    private String movieRevenue;
    private String movieBudget;
    private String movieGenres;
    private ArrayList<FilmTopBilledCastModel> listTopBilledCast;

    public OtherAboutFilmModel(String sParser, String strList) {
        this(sParser);
        setListTopBilledCast(strList);
    }

    private OtherAboutFilmModel(String sParser) {
        setAll(sParser);
    }

    public OtherAboutFilmModel() {
    }

    public void setAll(String sParser) {
        String[] allComponents = sParser.split("[|]");
        this.originalLanguage = allComponents[0];
        this.longMovieRuntime = allComponents[1];
        this.movieBudget = allComponents[2];
        this.movieRevenue = allComponents[3];
        this.movieGenres = allComponents[4];
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getLongMovieRuntime() {
        return longMovieRuntime;
    }

    public void setLongMovieRuntime(String longMovieRuntime) {
        this.longMovieRuntime = longMovieRuntime;
    }

    public String getMovieRevenue() {
        return movieRevenue;
    }

    public void setMovieRevenue(String movieRevenue) {
        this.movieRevenue = movieRevenue;
    }

    public String getMovieBudget() {
        return movieBudget;
    }

    public void setMovieBudget(String movieBudget) {
        this.movieBudget = movieBudget;
    }

    public String getMovieGenres() {
        return movieGenres;
    }

    public void setMovieGenres(String movieGenres) {
        this.movieGenres = movieGenres;
    }

    public ArrayList<FilmTopBilledCastModel> getListTopBilledCast() {
        return listTopBilledCast;
    }

    public void setListTopBilledCast(ArrayList<FilmTopBilledCastModel> listTopBilledCast) {
        this.listTopBilledCast = listTopBilledCast;
    }


    public void setListTopBilledCast(String strList) {

        listTopBilledCast = new ArrayList<>();
        StringBuilder buff = new StringBuilder();
        String buff2 = null;
        for (int x = 0; x < strList.length(); x++) {
            if (strList.charAt(x) == '=') {
                buff2 = buff.toString();
                buff.delete(0, buff.length());
            } else if (strList.charAt(x) == ',') {
                if (buff2 != null) {
                    listTopBilledCast.add(new FilmTopBilledCastModel(buff2, buff.toString()));
                    buff2 = null;
                }
                buff.delete(0, buff.length());
            } else
                buff.append(strList.charAt(x));
        }
        listTopBilledCast.add(new FilmTopBilledCastModel(buff2, buff.toString()));
    }
}
