package Project1;

import java.sql.Connection;
import java.util.Scanner;
import java.sql.PreparedStatement;

public class Bus{
    public void addbus(Scanner s){
        try{ 
        Connection con=DBConnection.getConnection();
        String query="INSERT INTO bus(bus_name,source,destination,capacity,available_seats) VALUES(?,?,?,?,?)";
      PreparedStatement pre=con.prepareStatement(query);
      System.out.print("Enter bus name: ");
      String name=s.nextLine();
      System.out.print("Enter Source place: ");
      String source=s.nextLine();
     System.out.print("Enter bus destination: ");
      String destination=s.nextLine();
      
       System.out.print("Enter bus capacity: ");
      int capacity=s.nextInt();
     System.out.print("Enter bus available seats: ");
      int available_seats=s.nextInt();

        if (available_seats > capacity) {
    System.out.println("Available seats cannot be greater than capacity.");
    return;
}
      pre.setString(1,name);
      pre.setString(2,source);
      pre.setString(3,destination);
      pre.setInt(4,capacity);
      pre.setInt(5,available_seats);
         

      int rowsaffected=pre.executeUpdate();
      if(rowsaffected>0){
        System.out.println("Rows Added Successfully");
      }
      else System.err.println("Something went wrong");
     
      con.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

