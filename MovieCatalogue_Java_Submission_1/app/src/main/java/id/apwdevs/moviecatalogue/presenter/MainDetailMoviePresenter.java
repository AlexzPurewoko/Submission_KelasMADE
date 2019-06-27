package id.apwdevs.moviecatalogue.presenter;

import android.content.Context;
import android.content.res.Resources;

import id.apwdevs.moviecatalogue.R;
import id.apwdevs.moviecatalogue.model.OtherAboutFilmModel;
import id.apwdevs.moviecatalogue.view.MainDetailMovieView;

public class MainDetailMoviePresenter {
    private Context mContext;
    private MainDetailMovieView mainDetailMovieView;

    public MainDetailMoviePresenter(Context mContext, MainDetailMovieView mainDetailMovieView) {
        this.mContext = mContext;
        this.mainDetailMovieView = mainDetailMovieView;
    }

    public void prepareAll(int moviePosition) {
        mainDetailMovieView.onLoadData();
        Resources data = mContext.getResources();
        OtherAboutFilmModel otherAboutFilmModel = new OtherAboutFilmModel();
        String[] arrShortAboutList = data.getStringArray(R.array.movie_short_about_list);
        String[] arrTopBilledCast = data.getStringArray(R.array.movie_top_billed_cast_list);

        otherAboutFilmModel.setAll(arrShortAboutList[moviePosition]);
        otherAboutFilmModel.setListTopBilledCast(arrTopBilledCast[moviePosition]);
        mainDetailMovieView.onLoadFinished(otherAboutFilmModel);
    }
}
