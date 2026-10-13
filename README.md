# Bus Booking System 🚌

A simple bus booking web application built using Java, JDBC, PostgreSQL, HTML, CSS, and JavaScript.

Users can search for buses by route and travel date, select seats, enter passenger details, and complete a simulated booking flow.

## Features

- Search buses by source, destination, and travel date
- View available buses and fares
- Select seats and enter passenger details
- View booking confirmation
- Connect the frontend to a Java backend API
- Store bus, passenger, and booking data in PostgreSQL
- Use JDBC to interact with the database

> **Note:** Payment is simulated for demonstration purposes. No real payments are processed.

## Technologies Used

- **Backend:** Java, JDBC, Java HTTP Server
- **Frontend:** HTML, CSS, JavaScript
- **Database:** PostgreSQL
- **Tools:** IntelliJ IDEA, Git, GitHub

## How It Works

1. The user searches for buses using the frontend.
2. JavaScript sends a request to the Java API.
3. The backend retrieves matching buses from PostgreSQL using JDBC.
4. The results are displayed on the webpage.
5. The user selects seats, enters passenger details, and completes the demo booking flow.

## Database

The project uses a PostgreSQL database named `transaction` with three tables:

- `bus`: stores bus details, routes, fares, and seat capacity
- `passenger`: stores passenger information
- `booking`: stores booking details and payment status

The database setup script is available at:

`database/transaction.sql`

## Project Structure

```text
Bus-Booking-System/
├── database/
│   └── transaction.sql
├── scr/
│   └── Project1/
│       ├── Main.java
│       ├── backend/
│       │   ├── ApiServer.java
│       │   ├── DBConnection.java
│       │   ├── Bus.java
│       │   ├── Passenger.java
│       │   ├── Booking.java
│       │   └── BusSearch.java
│       └── frontend/
│           ├── bus.html
│           ├── bus.js
│           ├── bus.css
│           └── bus2.css
└── README.md
```

## Running the Project Locally

1. Install Java and PostgreSQL.
2. Create a PostgreSQL database named `transaction`.
3. Run `database/transaction.sql` to set up the database.
4. Add the PostgreSQL JDBC driver to the project.
5. Configure your database connection in `DBConnection.java`.
6. Run `ApiServer.java` to start the backend API.
7. Open `bus.html` in your browser to use the frontend.

Make sure the backend is running before searching for buses.

## Author

**Parth Sharma**

- GitHub: [parthjan2006sharma-eng](https://github.com/parthjan2006sharma-eng)
