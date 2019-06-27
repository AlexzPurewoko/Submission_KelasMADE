package id.apwdevs.moviecatalogue.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import id.apwdevs.moviecatalogue.R;
import id.apwdevs.moviecatalogue.model.ShortListModel;

public class MovieListAdapter extends BaseAdapter implements Closeable {

    private Context mContext;
    private ArrayList<ShortListModel> shortListModels;
    private LruCache<Integer, Bitmap> mBitmapCache;
    private Handler uiHandler;
    private LinkedBlockingQueue<Runnable> mTaskRunnableFuture;
    private Thread mAllocatingBitmapWorkerThread;
    private int requestedWidth, requestedHeight;

    public MovieListAdapter(Context mContext) {
        this.mContext = mContext;
        requestedWidth = (int) mContext.getResources().getDimension(R.dimen.item_poster_width);
        requestedHeight = (int) mContext.getResources().getDimension(R.dimen.item_poster_height);
        shortListModels = new ArrayList<>();
        mBitmapCache = new LruCache<>((int) (Runtime.getRuntime().totalMemory() / 8));
        uiHandler = new Handler(Looper.getMainLooper());
        mTaskRunnableFuture = new LinkedBlockingQueue<>();
        mAllocatingBitmapWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        mTaskRunnableFuture.take().run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "MovieCatalogue: AllocaterBitmap");
        mAllocatingBitmapWorkerThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        mAllocatingBitmapWorkerThread.start();
    }

    public void addAllShortListModels(List<ShortListModel> shortListModels) {
        this.shortListModels.addAll(shortListModels);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return shortListModels.size();
    }

    @Override
    public Object getItem(int position) {
        return shortListModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_movies, parent, false);
        }


        ShortListModel shortListModel = (ShortListModel) getItem(position);
        MovieViewHolder movieViewHolder = new MovieViewHolder(convertView);
        movieViewHolder.bind(shortListModels.get(position));
        addGetBitmapWorker(movieViewHolder, shortListModel.getPhotoRes());

        return convertView;
    }

    private void addGetBitmapWorker(final MovieViewHolder movieViewHolder, final int photoRes) {
        mTaskRunnableFuture.add(new Runnable() {
            @Override
            public void run() {
                final Bitmap result;
                if (mBitmapCache.get(photoRes) != null)
                    result = mBitmapCache.get(photoRes);
                else {
                    Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), photoRes);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, requestedWidth, requestedHeight, false);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    scaledBitmap.compress(Bitmap.CompressFormat.WEBP, 30, bos);
                    scaledBitmap.recycle();
                    bitmap.recycle();
                    Bitmap compressedBitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.size());
                    mBitmapCache.put(photoRes, compressedBitmap);
                    result = mBitmapCache.get(photoRes);
                }

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (movieViewHolder != null) {
                            movieViewHolder.setImagePoster(result);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void close() throws IOException {
        mTaskRunnableFuture.clear();
        mAllocatingBitmapWorkerThread.interrupt();
        mBitmapCache.evictAll();
        System.gc();
    }

    private class MovieViewHolder {

        private ImageView moviePoster;
        private TextView title, releaseDate, overview;

        MovieViewHolder(View view) {
            moviePoster = view.findViewById(R.id.item_list_image_movies);
            title = view.findViewById(R.id.item_list_text_title);
            releaseDate = view.findViewById(R.id.item_list_release_date);
            overview = view.findViewById(R.id.item_list_overview_movies);
        }

        void bind(ShortListModel shortListModel) {

            title.setText(shortListModel.getTitleMovie());
            releaseDate.setText(shortListModel.getReleaseDate());
            overview.setText(shortListModel.getOverview());
        }

        void setImagePoster(Bitmap bitmap) {
            moviePoster.setImageBitmap(bitmap);
        }
    }
}
