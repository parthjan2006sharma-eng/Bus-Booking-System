package Project1.backend;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BusSearch {

    public List<String[]> searchBuses(String source, String destination, LocalDate travelDate)
            throws SQLException {

        List<String[]> buses = new ArrayList<>();

        String query = """
                SELECT b.bus_id, b.bus_name, b.source, b.destination,
                       b.travel_date, b.fare,
                       b.total_seats - COUNT(
                           CASE
                               WHEN bk.payment_status <> 'FAILED'
                               THEN bk.booking_id
                           END
                       ) AS available_seats
                FROM bus b
                LEFT JOIN booking bk ON b.bus_id = bk.bus_id
                WHERE LOWER(b.source) = LOWER(?)
                  AND LOWER(b.destination) = LOWER(?)
                  AND b.travel_date = ?
                GROUP BY b.bus_id
                ORDER BY b.bus_id
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, source);
            ps.setString(2, destination);
            ps.setDate(3, Date.valueOf(travelDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    buses.add(new String[]{
                            String.valueOf(rs.getInt("bus_id")),
                            rs.getString("bus_name"),
                            rs.getString("source"),
                            rs.getString("destination"),
                            rs.getDate("travel_date").toString(),
                            rs.getBigDecimal("fare").toString(),
                            String.valueOf(rs.getInt("available_seats"))
                    });
                }
            }
        }

        return buses;
    }
}