# Remember Me Functionality Implementation

The "Remember Me" feature allows users to save their login credentials locally and automatically log in when they return to the application.

## Key Components

1. **UserPreferences.java**: A utility class that handles storing and retrieving login credentials using Java's Preferences API

2. **Login UI**: The login screen has been updated with a "Remember Me" checkbox

3. **Auto-login**: Users who have checked "Remember Me" previously will be automatically logged in

## Security Considerations

- The passwords are stored using Base64 encoding, which is NOT secure encryption. This is sufficient for a demo but should be enhanced for production use.
- For better security in a real application, consider:
  - Using stronger encryption for stored passwords
  - Storing authentication tokens instead of actual passwords
  - Adding an option to require re-authentication for sensitive operations

## How to Use

1. **Login with Remember Me**:
   - Enter your username and password
   - Check the "Remember Me" checkbox
   - Click Login

2. **Auto Login**:
   - After enabling "Remember Me", the next time you start the application you'll be logged in automatically

3. **Clear Saved Credentials**:
   - If you want to remove saved credentials, uncheck the "Remember Me" checkbox during login
   - You can also programmatically clear credentials using `UserPreferences.clearLoginCredentials()`

## Technical Implementation

The implementation follows these key steps:

1. When a user logs in with "Remember Me" checked, their credentials are saved using Java's Preferences API
2. When the application starts, it checks if credentials are saved and tries to authenticate automatically
3. Password changes are tracked and saved credentials are updated when needed

## Future Enhancements

- Add a "Forget Me" button to explicitly clear saved credentials
- Implement credential expiration for enhanced security
- Store credentials in a more secure way (encryption instead of encoding)
