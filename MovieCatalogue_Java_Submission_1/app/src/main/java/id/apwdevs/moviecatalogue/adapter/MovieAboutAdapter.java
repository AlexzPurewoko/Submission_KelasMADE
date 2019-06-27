package id.apwdevs.moviecatalogue.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import id.apwdevs.moviecatalogue.R;
import id.apwdevs.moviecatalogue.model.OtherAboutFilmModel;

public class MovieAboutAdapter extends BaseAdapter {

    private Context mContext;
    private List<AboutDataModel> aboutDataModels = null;

    public MovieAboutAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setAboutDataModels(OtherAboutFilmModel otherAboutFilmModel) {
        aboutDataModels = new ArrayList<>();
        aboutDataModels.add(new AboutDataModel("Original Language :", otherAboutFilmModel.getOriginalLanguage()));
        aboutDataModels.add(new AboutDataModel("Long Runtime :", otherAboutFilmModel.getLongMovieRuntime()));
        aboutDataModels.add(new AboutDataModel("Movie Budget :", otherAboutFilmModel.getMovieBudget()));
        aboutDataModels.add(new AboutDataModel("Revenue : ", otherAboutFilmModel.getMovieRevenue()));
        aboutDataModels.add(new AboutDataModel("Genre : ", otherAboutFilmModel.getMovieGenres()));
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object getItem(int position) {
        if (aboutDataModels == null)
            return null;
        return aboutDataModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_side_left_about, parent, false);

        AboutDataModel model = (AboutDataModel) getItem(position);
        AboutDataViewHolder aboutDataViewHolder = new AboutDataViewHolder(convertView);
        aboutDataViewHolder.bind(model);
        return convertView;
    }

    private class AboutDataViewHolder {

        private TextView aboutTitle;
        private TextView aboutValue;

        AboutDataViewHolder(View view) {
            aboutTitle = view.findViewById(R.id.item_side_movie_title);
            aboutValue = view.findViewById(R.id.item_side_movie_query);
        }

        void bind(AboutDataModel model) {
            aboutValue.setText(model.aboutValue);
            aboutTitle.setText(model.aboutName);
        }
    }

    private class AboutDataModel {
        private String aboutName;
        private String aboutValue;

        AboutDataModel(String aboutName, String aboutValue) {
            this.aboutName = aboutName;
            this.aboutValue = aboutValue;
        }
    }
}
