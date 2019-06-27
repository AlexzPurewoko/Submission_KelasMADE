package id.apwdevs.moviecatalogue;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.util.List;

import id.apwdevs.moviecatalogue.adapter.MovieListAdapter;
import id.apwdevs.moviecatalogue.model.ShortListModel;
import id.apwdevs.moviecatalogue.presenter.MainListMoviePresenter;
import id.apwdevs.moviecatalogue.view.MainListMovieView;

public class MainActivity extends AppCompatActivity implements MainListMovieView {

    private ListView mListMovies;
    private MovieListAdapter movieListAdapter;
    private MainListMoviePresenter mainListMoviePresenter;
    private List<ShortListModel> data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListMovies = findViewById(R.id.list_movies);
        mListMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailMovies.class);
                intent.putExtra(DetailMovies.EXTRA_MOVIE_NUM, position);
                intent.putExtra(DetailMovies.EXTRA_MOVIE_DATA, data.get(position));
                startActivity(intent);
            }
        });
        movieListAdapter = new MovieListAdapter(this);
        mainListMoviePresenter = new MainListMoviePresenter(this, this);
        mainListMoviePresenter.prepareAll();
    }

    @Override
    protected void onDestroy() {
        try {
            movieListAdapter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onLoadData() {
        movieListAdapter = new MovieListAdapter(this);
        mListMovies.setAdapter(movieListAdapter);
    }

    @Override
    public void onLoadFinished(List<ShortListModel> data) {
        movieListAdapter.addAllShortListModels(data);
        this.data = data;
    }
}
