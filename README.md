# Transportation Management System - Reservation Module

This module handles the reservation system for the Transportation Management System, allowing users to create, view, confirm, and cancel reservations for various transportation methods.

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
