package com.example.smart_meal;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class CustomerOrderMFragment extends Fragment {

    CustomerOrderModel model;
    private TextView displayOrderNum;
    private TextView displayOrdersItem;
    private TextView displayDate;
    private Button btnCancel;
    private DBHelper DB;
    private ListView myListView;
    private ArrayAdapter<String> myAdapter;
    private DecimalFormat decimalFormat = new DecimalFormat("#");
    private DecimalFormat currency = new DecimalFormat("#.##");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_order_m, container, false);

        // Initialize the ListView and the list
        myListView = view.findViewById(R.id.listViewOldOrders);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayOrderNum = getActivity().findViewById(R.id.txtOrderNumber);
        displayOrdersItem = getActivity().findViewById(R.id.orderCustomerItem);
        displayDate = getActivity().findViewById(R.id.txtDate);
        btnCancel = getActivity().findViewById(R.id.btnCancel);

        //Start the database
        DB = new DBHelper(getActivity());

        //Get customer ID
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        String customerID = sharedPreferences.getString("CustomerID", "");

        //Get the orders from the customer
        Cursor c = DB.displayOrder(customerID);

        //Check if the customer has orders
        Boolean hasNoData = updateData(c,customerID);
        if (hasNoData == true) {
            displayOrdersItem.setText("NO DATA");
            displayDate.setText("NO DATA");
            displayOrdersItem.setText("NO DATA");
        }
        c.close();

        //For the user cancel the order
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                orderStatusUpdate(customerID,)
            }
        });
    }

    //Get the data from DB
    //If the customer doesn't has any order
    //It will show that he has no order
    public boolean updateData(Cursor c, String customerID){
        List<String> dataFromDb = new ArrayList<>();
        if(c.getCount()>0){
            while(c.moveToNext()){
                dataFromDb.add(c.getString(0)); //OrderID
                dataFromDb.add(c.getString(1)); //OrderStatus
                dataFromDb.add(c.getString(2)); //ItemID
                dataFromDb.add(c.getString(3)); //Date
                dataFromDb.add(c.getString(4)); //ItemQty
                dataFromDb.add(c.getString(5)); //BusinessID
            }
        }
        else{
            return true;
        }
        addOrderModel(dataFromDb, customerID);
        return false;
    }

    public void addOrderModel(List<String> list, String customerID){
        Stack<OrderModel> stackOrders = new Stack<>();
        //Make the data being add into the list
        int index = 0;
        while (index < list.size()) {
            OrderModel order = new OrderModel(
                    Integer.parseInt(list.get(index)), //OrderID -  ARRAY 0
                    Integer.parseInt(list.get(index + 1)), //OrderStatus ARRAY 1
                    Integer.parseInt(list.get(index + 5)),//BusinessID ARRAY 5
                    Integer.parseInt(customerID), //CustomerID
                    list.get(index + 3), //DATE ARRAY 3
                    list.get(index + 2), //ItemID ARRAY 2
                    list.get(index + 4)// ItemQty ARRAY 4
            );
            stackOrders.push(order);
            index += 6;
        }
        //Get the last item
        OrderModel lastOrder = stackOrders.pop();
        displayLastOrder(lastOrder);
        displayPastOrders(stackOrders);
    }

    //Display the last order
    public void displayLastOrder(OrderModel lastSix){

        double finalTotal = 0;
        StringBuilder orderToPrint = new StringBuilder();

        //Get the order
        int orderID = lastSix.getOrderID();

        //Order status
        int status = lastSix.getOrderStatus();

        //BusinessID
        int businessID = lastSix.getBusinessID();

        //Get items id
        String itemsID = lastSix.getItemID();
        String[] itemID = itemsID.split("\\$");

        //Get date
        String date = lastSix.getDate();

        //Get items quantity
        String itemsQty = lastSix.getItemQuantity();
        String[] itemQty = itemsQty.split("\\$");

        for(int i = 0; i < itemQty.length;i++){
            String name = getName(String.valueOf(businessID),Integer.parseInt(itemID[i]));
            Double price = getPrice(String.valueOf(businessID),Integer.parseInt(itemID[i]));
            orderToPrint.append(itemQty[i] + "x "+ name +" - $" + currency.format(price) + "\n");
            finalTotal += price * Double.parseDouble(itemQty[i]);
        }
        final double FEE = 0.6 * finalTotal;
        orderToPrint.append("\nSubtotal  $" + currency.format(finalTotal)+ "\n");
        orderToPrint.append("Fees  $" + currency.format(FEE)+ "\n");
        orderToPrint.append("\nTotal  $" + currency.format(finalTotal + FEE)+ "\n");

        //Display on Order Fragment
        String businessName = DB.displayBusinessName(String.valueOf(businessID),"Business");
        displayOrderNum.setText("ORDER #" + orderID + " - " + businessName);
        displayDate.setText(date);
        displayOrdersItem.setText(String.valueOf(orderToPrint));

    }

    public void displayPastOrders(Stack<OrderModel> pastOrders){
        List<String> ordersDisplay = new ArrayList<>();

        //Make the display until the stack is empty
        while(pastOrders.size() > 10){
            OrderModel order = pastOrders.pop();
            StringBuilder display = new StringBuilder();
            double finalTotal = 1;

            display.append("Order ID #" + order.getOrderID() + "\n");
            /*0 = Pedido enviado
            1 = Pedido recebido pelo Restaurante & enviar alerta pro customer (caso dê lol)
            2 = Customer cancelar order
            3 = Business cancelar order*/
            if(order.getOrderStatus() == 2 || order.getOrderStatus() == 3){
                display.append("Order Status #CANCELED\n");
            }
            display.append("Date " + order.getDate() + "\n");

            //Caçar o nome do restaurante
            String businessID = String.valueOf(order.getBusinessID());
            String businessName = DB.displayBusinessName(businessID,"Business");
            display.append("Restaurant - " + businessName + "\n");

            //Get items id
            String itemsID = order.getItemID();
            String[] itemID = itemsID.split("\\$");

            //Get items quantity
            String itemsQty = order.getItemQuantity();
            String[] itemQty = itemsQty.split("\\$");

            for(int i = 0; i < itemQty.length;i++){
                int id = Integer.parseInt(itemID[i]);
                String name = getName(businessID,id);
                Double price = getPrice(businessID,id);
                display.append(itemQty[i] + "x "+ name +" - $" + currency.format(price) + "\n");
                finalTotal += price * Double.parseDouble(itemQty[i]);
            }
            final double FEE = 0.6 * finalTotal;
            display.append("Subtotal  $" + currency.format(finalTotal)+ "\n");
            display.append("Fees  $" + currency.format(FEE)+ "\n");
            display.append("Total  $" + currency.format(finalTotal + FEE)+ "\n");

            ordersDisplay.add(String.valueOf(display));
        }
        createListView(ordersDisplay);
    }

    //Create the view on Listview
    public void createListView(List<String> listItems) {
        myAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, listItems);
        myListView.setAdapter(myAdapter);
    }

    //Get the items price from this Restaurant on DB
    public Double getPrice(String restaurantId, int productID){
        Double itemPrice = 0.0;
        Cursor c = DB.displayPrice(restaurantId,productID);
        if(c.getCount()>0){
            while(c.moveToNext()){
                itemPrice = Double.parseDouble(c.getString(0));
            }
        }
        return itemPrice;
    }

    //Get the items price from this Restaurant on DB
    public String getName(String restaurantId, int productID){
        String itemName = "";
        Cursor c = DB.displayName(restaurantId,productID);
        if(c.getCount()>0){
            while(c.moveToNext()){
                itemName = c.getString(0);
            }
        }
        return itemName;
    }

    public void setModel(CustomerOrderModel model){
        this.model = model;
    }
}