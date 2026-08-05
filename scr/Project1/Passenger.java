package Project1;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;
public class Passenger {
    public void addpassenger(Scanner sc){
        try{
            Connection con=DBConnection.getConnection();
            String query="INSERT INTO passenger(name,age) VALUES(?,?)";
            PreparedStatement pre=con.prepareStatement(query);
           
            System.out.print("Enter your name: ");
            String name=sc.nextLine();
            System.out.println("Enter your age: ");
            int age=sc.nextInt();
            if(age<14){
                System.out.println("Age Restriction");
                return;
            }
            pre.setString(1,name);
            pre.setInt(2,age);

            int rowsaffected=pre.executeUpdate();
            if(rowsaffected>0) System.out.println("Passenger Registered Successfully");
            else System.out.println("Registering Unsuccessfully");
            con.close();
            pre.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
