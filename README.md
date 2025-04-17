# Transportation Management System - Reservation Module

This module handles the reservation system for the Transportation Management System, allowing users to create, view, confirm, and cancel reservations for various transportation methods.

## Prerequisites

- Java 17 or higher
- Maven
- MySQL (via XAMPP or other installation)
- JavaFX

## Database Setup

1. Start your MySQL server (e.g., via XAMPP)
2. Create a new database named `transportation_management`
3. Import the SQL scripts in the following order:
   - `database_script_updated.sql` - Creates the database tables
   - `sample_data.sql` - Adds sample data

You can do this by:
- Opening phpMyAdmin
- Selecting the `transportation_management` database
- Going to the "Import" tab
- Selecting each SQL file and clicking "Go"

## Configuration

1. Make sure the `.env` file has the correct database credentials:
```
DB_USER="root"
DB_PASSWORD=""
DB_URL="jdbc:mysql://localhost:3306/transportation_management"
```

Modify these values if your MySQL setup uses different credentials.

## Building and Running

### Using Maven Wrapper

```bash
# On Windows
./mvnw clean javafx:run

# On Linux/Mac
chmod +x mvnw
./mvnw clean javafx:run
```

### Using Maven Directly (if installed)

```bash
mvn clean javafx:run
```

## Project Structure

- `src/main/java` - Java source files
  - `controllers` - JavaFX controllers
  - `entities` - Data model classes
  - `services` - Business logic and database operations
  - `utils` - Utility classes
  - `test` - Application entry point
- `src/main/resources` - FXML layouts and resources
  - `css` - Stylesheet files
  - FXML views

## Features

The reservation system includes the following features:

1. View all reservations for a user
2. Filter reservations by status (Pending, Confirmed, Cancelled, Completed)
3. Create new reservations
   - Select route and transport
   - Choose date and time
   - Option for round trips
   - Payment method selection
4. Confirm reservations
5. Cancel reservations
6. View detailed reservation information

## Contributing

This module integrates with other parts of the Transportation Management System, including:
- User management
- Route management
- Transport management
- Subscriptions

When making changes, ensure compatibility with these other modules.

## Troubleshooting

### Database Connection Issues

If you have trouble connecting to the database:
1. Verify XAMPP/MySQL is running
2. Check your database credentials in the `.env` file
3. Make sure the `transportation_management` database exists
4. Ensure all required tables are created

### JavaFX Issues

If the application doesn't display properly:
1. Verify you have the correct Java version (17+)
2. Make sure JavaFX is properly configured in your build path
3. Check the console for error messages 