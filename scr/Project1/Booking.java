package Project1;
import java.sql.*;
import java.util.Scanner;

public class Booking {
    public void viewbooking(){
        Connection co=null;
        try{
            co=DBConnection.getConnection();
            String que="""
            SELECT booking.booking_id,
            passenger.name,bus.bus_name,bus.source,bus.destination,booking.amount,booking.date_booked FROM booking 
            JOIN passenger On booking.passenger_id=passenger.passenger_id
            Join bus ON booking.bus_id=bus.bus_id
                    
                    """;

            PreparedStatement pes=co.prepareStatement(que);
            ResultSet rse=pes.executeQuery();
            while(rse.next()){
                System.out.println(
                        "Booking ID: "+rse.getInt("booking_id")+
                                " | Bus Name: "+rse.getString("bus_name")+
                                " | Passenger Name: "+rse.getString("name")+
                                " | Amount: "+rse.getInt("amount")+
                                " | Booking Date: "+rse.getTimestamp("date_booked")+
                                " | Source: "+rse.getString("source")+"-->"+
                                "  Destination: "+rse.getString("destination")
                );
            }
            rse.close();
            pes.close();
            co.close();
        }catch(SQLException es){
            es.printStackTrace();
        }
    }
    public void bookTicket(Scanner sc){
        Connection con=null;
        try{
            con=DBConnection.getConnection();
            con.setAutoCommit(false);
            String query="SELECT* FROM bus";
            PreparedStatement pre= con.prepareStatement(query);
            ResultSet res=pre.executeQuery();

            while(res.next()){
                System.out.println(
                        "Bus ID: "+res.getInt("bus_id")+
                                " | Name: "+res.getString("bus_name")+
                                " | Source: "+res.getString("source")+
                                " | Destination: "+res.getString("destination")
                );
            }
            System.out.println("Enter a Bus ID: ");
            int bi=sc.nextInt();

            String quer="SELECT * FROM bus WHERE bus_id=?";
            PreparedStatement ps=con.prepareStatement(quer);
            ps.setInt(1,bi);
            ResultSet rs=ps.executeQuery();
            if(rs.next()){
                System.out.println("Bus exists");
                int seats=rs.getInt("available_seats");
                if(seats>0){
                    System.out.println("Enter a Passenger ID: ");
                    int pi=sc.nextInt();
                    PreparedStatement pai=con.prepareStatement("SELECT* FROM passenger WHERE passenger_id=?");
                    pai.setInt(1,pi);
                    ResultSet pass=pai.executeQuery();
                    if(pass.next()) {System.out.println("Passenger Exists");

                        System.out.println("Enter Amount: ");
                        int amount =sc.nextInt();

                        String qu="INSERT INTO booking(bus_id,passenger_id,amount) VALUES(?,?,?)";
                        PreparedStatement pe=con.prepareStatement(qu);

                        pe.setInt(1,bi);
                        pe.setInt(2,pi);
                        pe.setInt(3,amount);
                        int bookingrows=pe.executeUpdate();
                        PreparedStatement p=con.prepareStatement("UPDATE bus SET available_seats=available_seats-1 WHERE bus_id=?");
                        p.setInt(1,bi);
                        int updaterows=p.executeUpdate();
                        if(bookingrows>0 && updaterows>0){
                            con.commit();
                            System.out.println("Booking Successful");
                        }
                        else { con.rollback();
                            System.out.println("Booking failed");}
                        p.close();
                        pe.close();
                        pass.close();
                    }
                    else System.out.println("Passenger does not exist");
                    pai.close();

                }
                else System.out.println("Seats are not available");

            }
            else System.out.println("Bus does not exist");

            res.close();
            rs.close();
            pre.close();
            ps.close();
            con.setAutoCommit(true);
            con.close();

        }catch(Exception e){
            try {if(con !=null){
                con.rollback();}
            }catch(SQLException ex){
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
