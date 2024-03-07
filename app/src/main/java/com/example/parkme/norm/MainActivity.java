package com.example.parkme.norm;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.example.parkme.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // This is where we set the parameters for our lower navigation bar.
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // This code establishes the user interface design for the MainActivity.


        // Connect our BottomNavigationView in the layout to our Java code.
        bottomNav = findViewById(R.id.bottom_navigation);
        // Create a listener that reacts to the selection of items on the bottom navigation bar.
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Quickly access the map with the Floating Action Button.
        FloatingActionButton fabMap = findViewById(R.id.fab_map);
        fabMap.setOnClickListener(view -> {
            // Clicking the FAB will immediately display the MapFragment.
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();
            // Additionally, we ensure that the map icon is prominently displayed in the bottom navigation.
            bottomNav.setSelectedItemId(R.id.nav_map);
        });

        // Upon starting the app, the MapFragment will be displayed by default..
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();
            bottomNav.setSelectedItemId(R.id.nav_map); // Highlight the map item in the bottom navigation.
        }
    }

    // Our navigation listener determines the action for a selected menu item.
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        // Select the correct fragment based on the bottom navigation item chosen.
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

        // Replace the content in the fragment_container view with the fragment we have chosen.
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true; // Indicate we've handled the selection.
        }
        return false; // No action was taken, so return false.
    };
}
