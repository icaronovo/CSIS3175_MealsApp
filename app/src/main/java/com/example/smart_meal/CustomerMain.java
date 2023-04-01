package com.example.smart_meal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class CustomerMain extends AppCompatActivity implements ItemAdapter.ItemClickListener {

    BottomNavigationView bottomNavigationView;
    ArrayList<ItemModel> itemList;

    ImageView imageView;
    ItemAdapter itemAdapter;
    TextView titleText;
    CustomerProfileFragment customerProfileFragment = new CustomerProfileFragment();

    private SharedPreferences sharedPreferences;

    DBHelper DB = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        //Recycler view
        androidx.recyclerview.widget.RecyclerView recyclerView = findViewById(R.id.mRecyclerView);
        int numOfColumns = 1;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numOfColumns));
        itemAdapter = new ItemAdapter(this, initData());
        itemAdapter.setClickListener(this);
        recyclerView.setAdapter(itemAdapter);

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        sharedPreferences.getString("user", "");
        String email = sharedPreferences.getString("user","");
        Log.d("TAG", email);

        if (!TextUtils.isEmpty(email)) {
            Cursor cursor = DB.getUserData(email);
            Log.d("TAG", String.valueOf(cursor));

            if (cursor != null && cursor.getCount() > 0) {
                StringBuilder str = new StringBuilder();

                while (cursor.moveToNext()) {
                    str.append(cursor.getString(0));
                    str.append(cursor.getString(1));
                    str.append(cursor.getString(2));
                    str.append(cursor.getString(3));
                    str.append(cursor.getString(5));
                    str.append(cursor.getString(6));
                    str.append(cursor.getString(7));
                    str.append(cursor.getString(8));
                    str.append(cursor.getString(9));
                    str.append("\n");
                }
                Log.d("TAG", String.valueOf(str));

                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                String data = str.toString();
                editor.putString("data", data);
                editor.apply();
            } else {
                // Handle case where cursor is null or empty
            }
        } else {
            // Handle case where email is empty or null
        }

        //Top text
        titleText = findViewById(R.id.topText);
        titleText.setText("Welcome User");

        ConstraintLayout constraintLayout = findViewById(R.id.fragmentLayout);

        //Bottom naviagation view
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        titleText.setText("Welcome User");
                        recyclerView.setVisibility(View.VISIBLE);
                        constraintLayout.setVisibility(View.INVISIBLE);
                        return true;
                    case R.id.search:
                        startActivity(new Intent(CustomerMain.this, CustomerSearch.class));
                        return true;
                    case R.id.order:
                        titleText.setText("Order");
                        recyclerView.setVisibility(View.INVISIBLE);
                        return true;
                    case R.id.profile:
                        titleText.setText("Account");
                        constraintLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.INVISIBLE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentLayout, customerProfileFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        //Test to see if the items on recycler view is clickable
        Intent intent = new Intent(CustomerMain.this, CustomerSearch.class);
        intent.putExtra("TEST", position);
        startActivity(intent);
        //End
    }

    //Created a list to tryout the recyclervier
    //Latter figure out how to import from database :)
    private List<ItemModel> initData() {
        itemList = new ArrayList<>();

        //DB.addItem(String itemName, int itemImage, String itemDescription, Double itemPrice) CONSTRUCTOR TO ADD ITEMS TO DATABASE.

        itemList.add(new ItemModel("Title Item", R.drawable.ic_launcher_background, "Description, description bla bla bla", 9.99));
        itemList.add(new ItemModel("Title Item", R.drawable.ic_launcher_background, "Description, description bla bla bla", 9.99));
        itemList.add(new ItemModel("Title Item", R.drawable.ic_launcher_background, "Description, description bla bla bla", 9.99));
        itemList.add(new ItemModel("Title Item", R.drawable.ic_launcher_background, "Description, description bla bla bla", 9.99));
        itemList.add(new ItemModel("Title Item", R.drawable.ic_launcher_background, "Description, description bla bla bla", 9.99));
        itemList.add(new ItemModel("Title Item", R.drawable.ic_launcher_background, "Description, description bla bla bla", 9.99));
        itemList.add(new ItemModel("Title Item", R.drawable.ic_launcher_background, "Description, description bla bla bla", 9.99));
        itemList.add(new ItemModel("Title Item", R.drawable.ic_launcher_background, "Description, description bla bla bla", 9.99));
        itemList.add(new ItemModel("Title Item", R.drawable.ic_launcher_background, "Description, description bla bla bla", 9.99));
        itemList.add(new ItemModel("Title Item", R.drawable.ic_launcher_background, "Description, description bla bla bla", 9.99));
        return itemList;
    }
}
