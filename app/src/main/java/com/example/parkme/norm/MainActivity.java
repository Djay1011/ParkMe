package com.example.parkme.norm;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.example.parkme.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * The Main activity class  that contains the bottom navigation and handles the switching of fragments.
 */
public class MainActivity extends AppCompatActivity {

    // A variable for the bottom navigation view.
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the BottomNavigationView.
        bottomNav = findViewById(R.id.bottom_navigation);
        // Set the listener for the navigation items.
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Sets up the Floating Action Button (FAB) and assign a function to be triggered when it is clicked.
        FloatingActionButton fabMap = findViewById(R.id.fab_map);
        fabMap.setOnClickListener(view -> {
            // Change to the MapFragment when the Floating Action Button is pressed.
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();
            // Chooses the map item from the bottom navigation to make it the selected option.
            bottomNav.setSelectedItemId(R.id.nav_map);
        });

        // When the activity is initially created, the default fragment (MapFragment) will be loaded.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();
            bottomNav.setSelectedItemId(R.id.nav_map);
        }
    }

    /**
     * The navigation listener changes fragments depending on the item selected in the bottom navigation.
     */
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();


        // Determine which fragment to display based on the selected item ID.
        if (itemId == R.id.nav_map) {
            selectedFragment = new MapFragment();
        } else if (itemId == R.id.nav_wallet) {
            selectedFragment = new WalletFragment();
        } else if (itemId == R.id.nav_notification) {
            selectedFragment = new NotificationFragment();
        } else if (itemId == R.id.nav_more) {
            selectedFragment = new MoreFragment();
        } else if (itemId == R.id.nav_booking) {
            selectedFragment = new BookingFragment();
        }

        // Replaces the current fragment with the new one.
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true; // Indicate that we handled the navigation item selection
        }

        return false; // False here means the navigation item selection wasn't handled
    };
}
