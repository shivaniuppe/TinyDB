package org.example.service;

import org.example.util.hashing.StringHashing;

import java.io.*;
import java.util.Objects;

import static org.example.manager.FileManager.DATABASES_DIRECTORY;

public class UserAuthService {

    private final String userCredentialsFilePath;
    private final StringHashing stringHashing;

    private String[] authenticatingUserData = null;

    public UserAuthService(String userCredentialsFileName, StringHashing stringHashing) {
        this.userCredentialsFilePath = DATABASES_DIRECTORY + File.separator + userCredentialsFileName;
        this.stringHashing = stringHashing;
        createCredentialsFile();
    }

    /**
     * Register user into the application
     * @param userId ID of the user
     * @param password Password of the user
     * @param securityQuestion Security question for extra authentication
     * @param securityAnswer Answer for the security question
     * @return True if user registration is successful, false otherwise
     */
    public boolean registerUser(String userId, String password, String securityQuestion, String securityAnswer) {
        String hashedUserId = stringHashing.hash(userId);
        String hashedPassword = stringHashing.hash(password);
        File userCredentialsFile = new File(userCredentialsFilePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userCredentialsFile, true))) {
            writer.write(hashedUserId + " | " + hashedPassword + " | " + securityQuestion + " | " + securityAnswer);
            writer.newLine();
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + userCredentialsFilePath);
            return false;
        } catch (IOException e) {
            System.out.println("Error reading file: " + userCredentialsFilePath);
            return false;
        }
    }

    /**
     * Check if user is already registered or not
     * @param userId ID of the user
     * @return True if user is registered, false otherwise
     */
    public boolean isUserRegistered(String userId) {
        String userData = getUserData(userId);
        return userData != null;
    }

    /**
     * Validates if provided userId is valid or not and saves user data for further authentication if valid
     * @param userId ID of the user
     * @return True if user is registered, false otherwise
     */
    public boolean validateUserIdToLogin(String userId) {
        authenticatingUserData = null;
        String userData = getUserData(userId);
        if (userData == null)
            return false;
        authenticatingUserData = userData.split("\\|");
        return true;
    }

    /**
     * Validates if provided password is valid or not for authenticating user and
     * provides security question if password is valid
     * @param password Password of the user
     * @return String representing security question if password is valid, null otherwise
     */
    public String validatePasswordAndGetSecurityQuestion(String password) {
        if (authenticatingUserData == null) {
            System.out.println("Login failed! Please try again.");
            return null;
        }
        String storedHashedPassword = authenticatingUserData[1].trim();
        if (stringHashing.checkHash(password, storedHashedPassword)) {
            // Return security question
            return authenticatingUserData[2].trim();
        }
        return null;
    }

    /**
     * Validates if security answer is valid or not for authenticating user
     * @param answer Security question's answer
     * @return True is answer is valid, false otherwise
     */
    public boolean validateSecurityAnswer(String answer) {
        if (authenticatingUserData == null) {
            System.out.println("Login failed! Please try again.");
            return false;
        }
        return Objects.equals(authenticatingUserData[3].trim().toLowerCase(), answer.toLowerCase());
    }

    /**
     * Creates the user credentials file if it does not exist already
     */
    private void createCredentialsFile() {
        File userCredentialsFile = new File(userCredentialsFilePath);
        try {
            if (!userCredentialsFile.exists())
                userCredentialsFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating file: " + userCredentialsFilePath);
        }
    }

    /**
     * Provides all data related to user if userId is registered
     * @param userId ID of the user
     * @return String representing a row of user data stored in user profile file
     */
    private String getUserData(String userId) {
        File userCredentialsFile = new File(userCredentialsFilePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(userCredentialsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    String storedHashedUserId = parts[0].trim();
                    // Return line (data) if user id matches
                    if (stringHashing.checkHash(userId, storedHashedUserId))
                        return line;
                }
            }
            return null;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + userCredentialsFilePath);
            return null;
        } catch (IOException e) {
            System.out.println("Error reading file: " + userCredentialsFilePath);
            return null;
        }
    }
}
