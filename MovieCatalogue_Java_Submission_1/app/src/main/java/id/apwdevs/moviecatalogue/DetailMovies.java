package id.apwdevs.moviecatalogue;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import id.apwdevs.moviecatalogue.adapter.MovieAboutAdapter;
import id.apwdevs.moviecatalogue.adapter.TopBilledCastAdapter;
import id.apwdevs.moviecatalogue.model.OtherAboutFilmModel;
import id.apwdevs.moviecatalogue.model.ShortListModel;
import id.apwdevs.moviecatalogue.presenter.MainDetailMoviePresenter;
import id.apwdevs.moviecatalogue.view.MainDetailMovieView;

public class DetailMovies extends AppCompatActivity implements MainDetailMovieView {

    public static final String EXTRA_MOVIE_DATA = "MOVIE_DATA";
    public static final String EXTRA_MOVIE_NUM = "MOVIE_POS";

    private MovieAboutAdapter movieAboutAdapter;
    private TopBilledCastAdapter topBilledCastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movies);


        ShortListModel shortListModel = getIntent().getParcelableExtra(EXTRA_MOVIE_DATA);
        int moviePosition = getIntent().getIntExtra(EXTRA_MOVIE_NUM, 0);

        ListView listAbout = findViewById(R.id.list_side_left);
        ImageView imageMovies = findViewById(R.id.image_movies);
        TextView title = findViewById(R.id.text_movie_title);
        TextView released = findViewById(R.id.text_movie_released);
        TextView overview = findViewById(R.id.detail_text_overview);

        movieAboutAdapter = new MovieAboutAdapter(this);
        topBilledCastAdapter = new TopBilledCastAdapter(this);
        listAbout.setAdapter(movieAboutAdapter);
        MainDetailMoviePresenter mainDetailMoviePresenter = new MainDetailMoviePresenter(this, this);

        imageMovies.setImageResource(shortListModel.getPhotoRes());
        title.setText(shortListModel.getTitleMovie());
        released.setText(shortListModel.getReleaseDate());
        overview.setText(shortListModel.getOverview());
        mainDetailMoviePresenter.prepareAll(moviePosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_list_actor) {
            final ListView listView = new ListView(this);
            listView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            listView.setPadding(16, 16, 16, 16);
            listView.setAdapter(topBilledCastAdapter);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Top Billed Cast");
            alertDialog.setView(listView);
            alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
            Toast.makeText(this, "List Actor", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLoadData() {

    }

    @Override
    public void onLoadFinished(OtherAboutFilmModel otherAboutFilmModel) {
        movieAboutAdapter.setAboutDataModels(otherAboutFilmModel);
        topBilledCastAdapter.setFilmTopBilledCastModels(otherAboutFilmModel.getListTopBilledCast());
    }
}
