package com.example.parkme.norm;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.example.parkme.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // Field for the bottom navigation view.
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the bottom navigation view and set its listener.
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Floating Action Button for quick access to the map.
        FloatingActionButton fabMap = findViewById(R.id.fab_map);
        fabMap.setOnClickListener(view -> {
            // Replace the current fragment with the MapFragment when FAB is clicked.
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();
            // Highlight the map icon in the bottom navigation view.
            bottomNav.setSelectedItemId(R.id.nav_map);
        });

        // Load the default fragment when the activity is first created.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();
            bottomNav.setSelectedItemId(R.id.nav_map);
        }
    }

    // Listener for the bottom navigation items.
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

        // Replace the current fragment with the selected one.
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        }
        return false;
    };
}
