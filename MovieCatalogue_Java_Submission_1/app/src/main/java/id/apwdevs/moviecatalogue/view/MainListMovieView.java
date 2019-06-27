package id.apwdevs.moviecatalogue.view;

import java.util.List;

import id.apwdevs.moviecatalogue.model.ShortListModel;

public interface MainListMovieView {
    void onLoadData();

    void onLoadFinished(List<ShortListModel> data);
}
