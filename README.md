# Bus Management System

A console-based Bus Management System built using Java, JDBC, and MySQL/MariaDB.

## Features

- Add buses
- Add passengers
- Book bus tickets
- View booking records
- Check bus and passenger availability
- Update available seats after booking
- Use database transactions with commit and rollback

## Technologies Used

- Java
- JDBC
- MySQL/MariaDB
- XAMPP
- IntelliJ IDEA

## Database

The project uses a database named `transaction` with three tables:

- `bus`
- `passenger`
- `booking`

The database SQL file is available in:

`database/transaction.sql`

## How to Run

1. Start MySQL from XAMPP.
2. Create a database named `transaction`.
3. Import `database/transaction.sql`.
4. Make sure MySQL Connector/J is added to the project.
5. Check the database details in `DBConnection.java`.
6. Run `Main.java`.

## Project Structure

```text
src/
└── Project1/
    ├── Bus.java
    ├── Passenger.java
    ├── Booking.java
    ├── DBConnection.java
    └── Main.java

database/
└── transaction.sql


AUTHOR 
Parth Sharma