package id.apwdevs.moviecatalogue.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import java.util.ArrayList;
import java.util.List;

import id.apwdevs.moviecatalogue.R;
import id.apwdevs.moviecatalogue.model.ShortListModel;
import id.apwdevs.moviecatalogue.view.MainListMovieView;

public class MainListMoviePresenter {
    private Context mContext;
    private MainListMovieView view;

    public MainListMoviePresenter(Context mContext, MainListMovieView view) {
        this.mContext = mContext;
        this.view = view;
    }

    public void prepareAll() {
        view.onLoadData();

        Resources resources = mContext.getResources();
        String[] arrMovies = resources.getStringArray(R.array.movie_name_list);
        String[] arrOverview = resources.getStringArray(R.array.movie_overview_str_list);
        String[] arrReleased = resources.getStringArray(R.array.movie_released_time_list);
        TypedArray arrMoviePoster = resources.obtainTypedArray(R.array.movie_drawable_list);
        List<ShortListModel> listResult = new ArrayList<>();
        for (int x = 0; x < arrMovies.length; x++) {
            ShortListModel shortListModel = new ShortListModel();
            shortListModel.setTitleMovie(arrMovies[x]);
            shortListModel.setOverview(arrOverview[x]);
            shortListModel.setReleaseDate(arrReleased[x]);
            shortListModel.setPhotoRes(arrMoviePoster.getResourceId(x, -1));
            listResult.add(shortListModel);
        }
        arrMoviePoster.recycle();
        view.onLoadFinished(listResult);
        System.gc();
    }
}
