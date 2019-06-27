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
import id.apwdevs.moviecatalogue.model.FilmTopBilledCastModel;

public class TopBilledCastAdapter extends BaseAdapter {

    private List<FilmTopBilledCastModel> filmTopBilledCastModels;
    private Context mContext;

    public TopBilledCastAdapter(Context mContext) {
        this.mContext = mContext;
        filmTopBilledCastModels = new ArrayList<>();
    }

    public void setFilmTopBilledCastModels(List<FilmTopBilledCastModel> filmTopBilledCastModels) {
        this.filmTopBilledCastModels = filmTopBilledCastModels;
    }

    @Override
    public int getCount() {
        return filmTopBilledCastModels.size();
    }

    @Override
    public Object getItem(int position) {
        return filmTopBilledCastModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_actor, parent, false);

        FilmTopBilledCastModel filmTopBilledCastModel = (FilmTopBilledCastModel) getItem(position);

        TopBilledCastViewHolder topBilledCastViewHolder = new TopBilledCastViewHolder(convertView);
        topBilledCastViewHolder.bind(filmTopBilledCastModel);
        return convertView;
    }

    private class TopBilledCastViewHolder {
        private TextView name, alias;

        TopBilledCastViewHolder(View view) {
            name = view.findViewById(R.id.item_list_actor_name);
            alias = view.findViewById(R.id.item_list_actor_alias);
        }

        void bind(FilmTopBilledCastModel filmTopBilledCastModel) {
            name.setText(filmTopBilledCastModel.getActorName());
            alias.setText(filmTopBilledCastModel.getAliasPeople());
        }
    }
}
