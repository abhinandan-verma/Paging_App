package com.example.pagingapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.example.pagingapp.R;
import com.example.pagingapp.adapter.MoviesAdapter;
import com.example.pagingapp.adapter.MoviesLoadStateAdapter;
import com.example.pagingapp.databinding.ActivityMainBinding;
import com.example.pagingapp.model.util.GridSpace;
import com.example.pagingapp.model.util.MovieComparator;
import com.example.pagingapp.model.util.Utils;
import com.example.pagingapp.viewmodel.MovieViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    MovieViewModel mainActivityViewModel;
    ActivityMainBinding binding;
    MoviesAdapter moviesAdapter;

    @Inject
    RequestManager requestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Utils.API_KEY == null || Utils.API_KEY.isEmpty()){
            Toast.makeText(this, "Error in API key", Toast.LENGTH_SHORT).show();
        }

        moviesAdapter = new MoviesAdapter(new MovieComparator(),requestManager);
        mainActivityViewModel = new ViewModelProvider(this).get(MovieViewModel.class);

        initRecyclerViewAndAdapter();

        //Subscribe to paging data
        mainActivityViewModel.moviePagingDataFlowable.subscribe(moviePagingData -> {
            moviesAdapter.submitData(getLifecycle(),moviePagingData);
        });
    }

    private void initRecyclerViewAndAdapter() {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        binding.recyclerview.setLayoutManager(gridLayoutManager);

        binding.recyclerview.addItemDecoration(new GridSpace(2,20,true));
        binding.recyclerview.setAdapter(
                moviesAdapter.withLoadStateFooter(
                        new MoviesLoadStateAdapter(v -> {
                            moviesAdapter.retry();
                        })
                )
        );

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return moviesAdapter.getItemViewType(position) == MoviesAdapter.LOADING_INT ? 1 : 2;
            }
        });
    }
}