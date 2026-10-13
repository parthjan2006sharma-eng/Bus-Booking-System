package Project1.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
public class Passenger {
    public void addpassenger(Scanner sc){

           
            System.out.print("Enter your Name: ");
            String name=sc.nextLine();
            System.out.println("Enter your Age: ");
            int age=sc.nextInt();
            sc.nextLine();
            if(age<14){
                System.out.println("Age Restriction");
                return;
            }
            System.out.print("Enter your Email: ");
            String email=sc.nextLine();

            System.out.println("Enter your Phone Number: ");
            String phone=sc.nextLine();
        String query="INSERT INTO passenger(name,age,email,phone) VALUES(?,?,?,?)";

        try (Connection con= DBConnection.getConnection();
            PreparedStatement pre=con.prepareStatement(query))
        {
            pre.setString(1,name);
            pre.setInt(2,age);
            pre.setString(3,email);
            pre.setString(4,phone);

            int rowsaffected=pre.executeUpdate();
            if(rowsaffected>0) System.out.println("Passenger Registered Successfully");
            else System.out.println("Registering Unsuccessfully");

        }catch(SQLException e){
            if ("23505".equals(e.getSQLState())) {
                System.out.println("Email or phone number already registered.");
            } else {
                e.printStackTrace();
            }
        }
    }
    
}
