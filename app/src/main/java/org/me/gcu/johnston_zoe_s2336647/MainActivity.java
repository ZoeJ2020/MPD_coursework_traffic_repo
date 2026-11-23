//
// Name                 Zoe Johnston
// Student ID           S2336647
// Programme of Study   Bsc/Bsc (Hons) Software Development
//

package org.me.gcu.johnston_zoe_s2336647;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import org.me.gcu.johnston_zoe_s2336647.view.SituationDetailFragment;
import org.me.gcu.johnston_zoe_s2336647.view.SituationListFragment;
import org.me.gcu.johnston_zoe_s2336647.view.SituationMapFragment;
import org.me.gcu.johnston_zoe_s2336647.viewmodel.TrafficViewModel;

public class MainActivity extends AppCompatActivity {

    private int lastBackStackCount = 0;

    private boolean twoPane = false;

    private boolean skipReplay = false;

    private final ActivityResultLauncher<String> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                TrafficViewModel viewModel = new ViewModelProvider(this).get(TrafficViewModel.class);
                updateOrigin(viewModel);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Traffic Scotland");

        twoPane = findViewById(R.id.detailContainer) != null;
        skipReplay = savedInstanceState != null;

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new SituationListFragment())
                    .commit();
        }

        if (twoPane){
            if (getSupportFragmentManager().getBackStackEntryCount() > 0){
                getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            //    the right-hand pane is always there in landscape, it fills in when something is selected
            if (getSupportFragmentManager().findFragmentByTag("detail") == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detailContainer, new SituationDetailFragment(), "detail")
                        .commit();
            }
        } else {
            Fragment oldPane = getSupportFragmentManager().findFragmentByTag("detail");
            if (oldPane != null){
                getSupportFragmentManager().beginTransaction()
                        .remove(oldPane)
                        .commitNow();
            }
        }

        TrafficViewModel viewModel = new ViewModelProvider(this).get(TrafficViewModel.class);

        viewModel.getSelectedSituation().observe(this, s -> {
            if (skipReplay){
                skipReplay = false;
                return;
            }

            if (s == null){
                return;
            }

            if (twoPane){
                //    landscape: if the map is in the right-hand pane, a selection switches it back to the detail
                Fragment pane = getSupportFragmentManager().findFragmentById(R.id.detailContainer);
                if (pane instanceof SituationMapFragment){
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detailContainer, new SituationDetailFragment(), "detail")
                            .commit();
                }
                return;
            }

            //    portrait: already showing the detail, so do nothing
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (current instanceof SituationDetailFragment){
                return;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new SituationDetailFragment())
                    .addToBackStack(null)
                    .commit();
        });

        lastBackStackCount = getSupportFragmentManager().getBackStackEntryCount();
        updateUpArrow();
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            int count = getSupportFragmentManager().getBackStackEntryCount();
            if (count < lastBackStackCount){
                Fragment top = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (!(top instanceof SituationDetailFragment)){
                    viewModel.clearSelection();
                }
            }
            lastBackStackCount = count;
            updateUpArrow();
        });
    }

    public void showMap(){
        if (twoPane){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detailContainer, new SituationMapFragment(), "detail")
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new SituationMapFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void updateUpArrow(){
        boolean showArrow = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(showArrow);
    }

    @Override
    public boolean onSupportNavigateUp(){
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Road, for example A9 or M8");

        TrafficViewModel viewModel = new ViewModelProvider(this).get(TrafficViewModel.class);

        //    after a rotation, open the search box again with the text that was in it
        String current = viewModel.getRoadQuery();
        if (current != null && !current.isEmpty()){
            searchItem.expandActionView();
            searchView.setQuery(current, false);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query){
                viewModel.setRoadQuery(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText){
                viewModel.setRoadQuery(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.action_nearby){
            showNearbyDialog();
            return true;
        }

        if (item.getItemId() == R.id.action_map){
            //    the map shows whatever the list is showing now, so a road search gives several situations
            TrafficViewModel viewModel = new ViewModelProvider(this).get(TrafficViewModel.class);
            viewModel.setMapSituations(viewModel.getVisibleSituations().getValue());
            showMap();
            return true;
        }

        if (item.getItemId() == R.id.action_refresh){
            new ViewModelProvider(this).get(TrafficViewModel.class).load();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showNearbyDialog(){
        TrafficViewModel viewModel = new ViewModelProvider(this).get(TrafficViewModel.class);

        String[] options = {"Off - show everything", "Within 5 km", "Within 10 km", "Within 25 km", "Within 50 km"};
        int[] radii = {0, 5, 10, 25, 50};

        new AlertDialog.Builder(this)
                .setTitle("Search nearby")
                .setItems(options, (dialog, which) -> {
                    viewModel.setRadiusKm(radii[which]);

                    if (radii[which] > 0){
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED){
                            updateOrigin(viewModel);
                        } else {
                            locationPermissionRequest.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
                        }
                    }
                })
                .show();
    }

    private void updateOrigin(TrafficViewModel viewModel){
        Location last = null;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            try {
                last = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (last == null){
                    last = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                }
            } catch (SecurityException | IllegalArgumentException e){
                last = null;
            }
        }

        if (last != null){
            viewModel.setOrigin(last.getLatitude(), last.getLongitude(), "your location");
        } else {
            //    emulated so no real location, so use a fixed point
            viewModel.setOrigin(55.8668, -4.2500, "Glasgow Caledonian University");
            Toast.makeText(this, "Location not available, using Glasgow Caledonian University",
                    Toast.LENGTH_LONG).show();
        }
    }
}