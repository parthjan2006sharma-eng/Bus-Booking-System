package Project1;

import Project1.backend.Booking;
import Project1.backend.Bus;
import Project1.backend.Passenger;
import Project1.backend.BusSearch;
import java.time.LocalDate;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc=new Scanner(System.in);
        while(true){ 
        System.out.println(
            " | 1.) Add Bus"+
            " | 2.) Add Passenger"+
            " | 3.) Book Ticket"+
            " | 4.) view Booking"+
            " | 5.) Exit"
        );
        System.out.println( "Enter your choice: ");
        int n=sc.nextInt();
        sc.nextLine();
        switch(n){
          case 1:{
            Bus b=new Bus();
            b.addbus(sc);
            break;
          }
            case 2:{
            Passenger p =new Passenger();
            p.addpassenger(sc);
            break;
          }
            case 3:{
            Booking bo=new Booking();
            bo.bookTicket(sc);
            break;
          }
            case 4:{
                Booking v=new Booking();
                v.viewbooking();
                break;
          }
            case 5:{
                sc.close();
                System.out.println("Thank You!");
            return;
          }
          default:
            System.out.println("Invalid Option!!");
        }
    }
    }
}
