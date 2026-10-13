package Project1.backend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiServer {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0
        );

        server.createContext("/api/buses", ApiServer::searchBuses);
        server.createContext("/api/booking", ApiServer::bookTicket);
        server.createContext("/api/booked-seats", ApiServer::getBookedSeats);
        server.createContext("/api/cities", ApiServer::getCities);

        server.start();

        System.out.println("Server running at http://localhost:8080");
    }
    private static void bookTicket(HttpExchange exchange)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "POST, OPTIONS"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type"
        );

        if (exchange.getRequestMethod().equals("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!exchange.getRequestMethod().equals("POST")) {
            sendResponse(
                    exchange,
                    405,
                    "{\"error\":\"Method not allowed\"}"
            );
            return;
        }

        try {
            String body;

            try (InputStream input = exchange.getRequestBody()) {
                body = new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }

            Map<String, String> params = getParameters(body);

            int busId = Integer.parseInt(params.get("busId"));
            int seatNumber = Integer.parseInt(params.get("seatNumber"));
            String name = params.get("name");
            int age = Integer.parseInt(params.get("age"));
            String email = params.get("email");
            String phone = params.get("phone");

            if (name == null || name.isBlank()
                    || email == null || email.isBlank()
                    || phone == null || phone.isBlank()
                    || age <= 0 || seatNumber <= 0) {

                sendResponse(
                        exchange,
                        400,
                        "{\"error\":\"Invalid passenger or seat details\"}"
                );
                return;
            }

            try (Connection con = DBConnection.getConnection()) {

                con.setAutoCommit(false);

                try {
                    int totalSeats;

                    String busSql =
                            "SELECT total_seats FROM bus WHERE bus_id = ?";

                    try (PreparedStatement ps =
                                 con.prepareStatement(busSql)) {

                        ps.setInt(1, busId);

                        try (ResultSet rs = ps.executeQuery()) {

                            if (!rs.next()) {
                                throw new IllegalArgumentException(
                                        "Bus not found"
                                );
                            }

                            totalSeats = rs.getInt("total_seats");
                        }
                    }

                    if (seatNumber > totalSeats) {
                        throw new IllegalArgumentException(
                                "Invalid seat number"
                        );
                    }

                    String seatSql =
                            "SELECT 1 FROM booking " +
                                    "WHERE bus_id = ? AND seat_number = ? " +
                                    "AND payment_status <> 'FAILED'";

                    try (PreparedStatement ps =
                                 con.prepareStatement(seatSql)) {

                        ps.setInt(1, busId);
                        ps.setInt(2, seatNumber);

                        try (ResultSet rs = ps.executeQuery()) {

                            if (rs.next()) {
                                throw new IllegalArgumentException(
                                        "Seat is already booked"
                                );
                            }
                        }
                    }

                    int passengerId;

                    String passengerSql =
                            "INSERT INTO passenger " +
                                    "(name, age, email, phone) " +
                                    "VALUES (?, ?, ?, ?) RETURNING passenger_id";

                    try (PreparedStatement ps =
                                 con.prepareStatement(passengerSql)) {

                        ps.setString(1, name);
                        ps.setInt(2, age);
                        ps.setString(3, email);
                        ps.setString(4, phone);

                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            passengerId = rs.getInt("passenger_id");
                        }
                    }

                    String bookingSql =
                            "INSERT INTO booking " +
                                    "(passenger_id, bus_id, seat_number, " +
                                    "booking_date, payment_status) " +
                                    "VALUES (?, ?, ?, CURRENT_DATE, 'PENDING') " +
                                    "RETURNING booking_id";

                    int bookingId;

                    try (PreparedStatement ps =
                                 con.prepareStatement(bookingSql)) {

                        ps.setInt(1, passengerId);
                        ps.setInt(2, busId);
                        ps.setInt(3, seatNumber);

                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            bookingId = rs.getInt("booking_id");
                        }
                    }

                    con.commit();

                    sendResponse(
                            exchange,
                            201,
                            "{\"message\":\"Booking saved\"," +
                                    "\"bookingId\":" + bookingId + "}"
                    );

                } catch (Exception e) {
                    con.rollback();
                    throw e;
                } finally {
                    con.setAutoCommit(true);
                }
            }

        } catch (IllegalArgumentException e) {
            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"" + escape(e.getMessage()) + "\"}"
            );

        } catch (Exception e) {
            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "{\"error\":\"Could not save booking\"}"
            );
        }
    }

    private static void searchBuses(HttpExchange exchange)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET,POST, OPTIONS"
        );
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type"
        );

        if (exchange.getRequestMethod().equals("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!exchange.getRequestMethod().equals("GET")) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            Map<String, String> params = getParameters(
                    exchange.getRequestURI().getRawQuery()
            );

            String source = params.get("from");
            String destination = params.get("to");
            String date = params.get("date");

            if (source == null || source.isBlank()
                    || destination == null || destination.isBlank()
                    || date == null || date.isBlank()) {

                sendResponse(
                        exchange,
                        400,
                        "{\"error\":\"From, to and date are required\"}"
                );
                return;
            }

            BusSearch search = new BusSearch();

            List<String[]> buses = search.searchBuses(
                    source,
                    destination,
                    LocalDate.parse(date)
            );

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < buses.size(); i++) {
                String[] bus = buses.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{")
                        .append("\"id\":").append(bus[0]).append(",")
                        .append("\"name\":\"").append(escape(bus[1])).append("\",")
                        .append("\"from\":\"").append(escape(bus[2])).append("\",")
                        .append("\"to\":\"").append(escape(bus[3])).append("\",")
                        .append("\"date\":\"").append(bus[4]).append("\",")
                        .append("\"fare\":").append(bus[5]).append(",")
                        .append("\"availableSeats\":").append(bus[6])
                        .append("}");
            }

            json.append("]");

            sendResponse(exchange, 200, json.toString());

        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid date or parameters\"}");

        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }
    private static void getBookedSeats(HttpExchange exchange)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*"
        );
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, OPTIONS"
        );
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type"
        );

        if (exchange.getRequestMethod().equals("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!exchange.getRequestMethod().equals("GET")) {
            sendResponse(exchange, 405,
                    "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            Map<String, String> params = getParameters(
                    exchange.getRequestURI().getRawQuery()
            );

            int busId = Integer.parseInt(params.get("busId"));

            StringBuilder json = new StringBuilder(
                    "{\"bookedSeats\":["
            );

            String sql = "SELECT seat_number FROM booking " +
                    "WHERE bus_id = ? AND payment_status <> 'FAILED' " +
                    "ORDER BY seat_number";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, busId);

                try (ResultSet rs = ps.executeQuery()) {
                    boolean first = true;

                    while (rs.next()) {
                        if (!first) {
                            json.append(",");
                        }

                        json.append(rs.getInt("seat_number"));
                        first = false;
                    }
                }
            }

            json.append("]}");
            sendResponse(exchange, 200, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 400,
                    "{\"error\":\"Could not fetch booked seats\"}");
        }
    }

    private static void getCities(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, OPTIONS"
        );

        if (exchange.getRequestMethod().equals("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!exchange.getRequestMethod().equals("GET")) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        StringBuilder json = new StringBuilder("{\"cities\":[");
        boolean first = true;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT source AS city FROM bus " +
                                "UNION " +
                                "SELECT destination AS city FROM bus " +
                                "ORDER BY city"
                );
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }

                String city = rs.getString("city")
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"");

                json.append("\"").append(city).append("\"");
                first = false;
            }

            json.append("]}");
            sendResponse(exchange, 200, json.toString());

        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
            e.printStackTrace();
        }
    }

    private static Map<String, String> getParameters(String query) {

        Map<String, String> params = new HashMap<>();

        if (query == null || query.isEmpty()) {
            return params;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(
                    parts[0], StandardCharsets.UTF_8
            );

            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";

            params.put(key, value);
        }

        return params;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static void sendResponse(
            HttpExchange exchange, int status, String response
    ) throws IOException {

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}