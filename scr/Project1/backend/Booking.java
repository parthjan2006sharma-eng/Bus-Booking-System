package Project1.backend;

import java.sql.*;
import java.util.Scanner;

public class Booking {

    public void viewbooking() {
        String query = """
            SELECT booking.booking_id,
                   passenger.name,
                   bus.bus_name,
                   bus.source,
                   bus.destination,
                   bus.fare,
                   booking.seat_number,
                   booking.booking_date,
                   booking.payment_status
            FROM booking
            JOIN passenger
                ON booking.passenger_id = passenger.passenger_id
            JOIN bus
                ON booking.bus_id = bus.bus_id
            ORDER BY booking.booking_id
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println(
                        "Booking ID: " + rs.getInt("booking_id") +
                                " | Passenger: " + rs.getString("name") +
                                " | Bus: " + rs.getString("bus_name") +
                                " | Seat: " + rs.getInt("seat_number") +
                                " | Route: " + rs.getString("source") +
                                " -> " + rs.getString("destination") +
                                " | Fare: " + rs.getBigDecimal("fare") +
                                " | Booking Date: " + rs.getTimestamp("booking_date") +
                                " | Payment Status: " + rs.getString("payment_status")
                );
            }

            if (!found) {
                System.out.println("No bookings found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void bookTicket(Scanner sc) {
        Connection con = null;

        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            String busQuery = """
                    SELECT b.bus_id, b.bus_name, b.source, b.destination,
                           b.travel_date, b.fare, b.total_seats,
                           b.total_seats - COUNT(
                               CASE WHEN bk.payment_status <> 'FAILED'
                                    THEN bk.booking_id END
                           ) AS available_seats
                    FROM bus b
                    LEFT JOIN booking bk ON b.bus_id = bk.bus_id
                    GROUP BY b.bus_id
                    ORDER BY b.bus_id
                    """;

            try (PreparedStatement ps = con.prepareStatement(busQuery);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    System.out.println(
                            "Bus ID: " + rs.getInt("bus_id") +
                                    " | Name: " + rs.getString("bus_name") +
                                    " | Route: " + rs.getString("source") +
                                    " -> " + rs.getString("destination") +
                                    " | Travel Date: " + rs.getDate("travel_date") +
                                    " | Fare: " + rs.getBigDecimal("fare") +
                                    " | Available Seats: " + rs.getInt("available_seats")
                    );
                }
            }

            System.out.println("Enter Bus ID:");
            int busId = sc.nextInt();
            sc.nextLine();

            String busQueryById = """
                    SELECT bus_name, fare, total_seats
                    FROM bus
                    WHERE bus_id = ?
                    FOR UPDATE
                    """;

            String busName;
            java.math.BigDecimal fare;
            int totalSeats;

            try (PreparedStatement ps = con.prepareStatement(busQueryById)) {
                ps.setInt(1, busId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        System.out.println("Bus does not exist.");
                        return;
                    }

                    busName = rs.getString("bus_name");
                    fare = rs.getBigDecimal("fare");
                    totalSeats = rs.getInt("total_seats");
                }
            }

            System.out.println("Enter Phone no:");
            String phone = sc.nextLine().trim();

            String passengerQuery =
                    "SELECT passenger_id FROM passenger WHERE phone = ?";
            int passengerId;

            try (PreparedStatement ps = con.prepareStatement(passengerQuery)) {
                ps.setString(1,phone);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        System.out.println("Passenger does not exist.");
                        return;
                    }
                passengerId = rs.getInt("passenger_id");
                }
            }

            String countQuery = """
                    SELECT COUNT(*)
                    FROM booking
                    WHERE bus_id = ?
                      AND payment_status <> 'FAILED'
                    """;

            int bookedSeats;

            try (PreparedStatement ps = con.prepareStatement(countQuery)) {
                ps.setInt(1, busId);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    bookedSeats = rs.getInt(1);
                }
            }

            if (bookedSeats >= totalSeats) {
                con.rollback();
                System.out.println("No seats are available.");
                return;
            }

            System.out.println("Available seat numbers:");

            for (int seat = 1; seat <= totalSeats; seat++) {
                String seatQuery = """
                        SELECT COUNT(*)
                        FROM booking
                        WHERE bus_id = ?
                          AND seat_number = ?
                          AND payment_status <> 'FAILED'
                        """;

                try (PreparedStatement ps = con.prepareStatement(seatQuery)) {
                    ps.setInt(1, busId);
                    ps.setInt(2, seat);

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();

                        if (rs.getInt(1) == 0) {
                            System.out.print(seat + " ");
                        }
                    }
                }
            }

            System.out.println("\nEnter your preferred seat number:");
            int seatNumber = sc.nextInt();

            if (seatNumber < 1 || seatNumber > totalSeats) {
                con.rollback();
                System.out.println("Invalid seat number.");
                return;
            }

            String seatCheck = """
                    SELECT COUNT(*)
                    FROM booking
                    WHERE bus_id = ?
                      AND seat_number = ?
                      AND payment_status <> 'FAILED'
                    """;

            try (PreparedStatement ps = con.prepareStatement(seatCheck)) {
                ps.setInt(1, busId);
                ps.setInt(2, seatNumber);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();

                    if (rs.getInt(1) > 0) {
                        con.rollback();
                        System.out.println("Seat already booked. Choose another seat.");
                        return;
                    }
                }
            }

            String insertQuery = """
                    INSERT INTO booking
                        (passenger_id, bus_id, seat_number)
                    VALUES (?, ?, ?)
                    """;

            try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
                ps.setInt(1, passengerId);
                ps.setInt(2, busId);
                ps.setInt(3, seatNumber);

                ps.executeUpdate();
            }

            con.commit();

            System.out.println("Booking Successful!");
            System.out.println("Bus: " + busName);
            System.out.println("Seat Number: " + seatNumber);
            System.out.println("Fare: " + fare);
            System.out.println("Payment Status: PENDING");

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }

            e.printStackTrace();

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}