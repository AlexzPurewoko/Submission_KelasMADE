package id.apwdevs.moviecatalogue.view;

import id.apwdevs.moviecatalogue.model.OtherAboutFilmModel;

public interface MainDetailMovieView {
    void onLoadData();

    void onLoadFinished(OtherAboutFilmModel otherAboutFilmModel);
}
