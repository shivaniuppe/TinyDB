package org.example;

import org.example.database.Database;
import org.example.enums.LandingMenuOption;
import org.example.enums.MainMenuOption;
import org.example.manager.DatabaseManager;
import org.example.manager.FileManager;
import org.example.service.ERDGenerator;
import org.example.service.UserAuthService;
import org.example.util.hashing.BCryptStringHashing;
import org.example.util.hashing.StringHashing;

import java.util.LinkedHashMap;
import java.util.Scanner;

public class TinyDb {

    private final Scanner scanner = new Scanner(System.in);
    private final String userCredentialsFileName = "User_Profile.txt";
    private final StringHashing stringHashing = new BCryptStringHashing();
    private final UserAuthService userAuthService = new UserAuthService(userCredentialsFileName, stringHashing);

    public static void main(String[] args) {
        FileManager.createDatabaseDirectory();
        TinyDb tinyDB = new TinyDb();
        tinyDB.showLandingMenu();
    }

    /**
     * Displays the landing menu where user can go through authentication process
     */
    public void showLandingMenu() {
        boolean shouldShowMainMenu = false;
        LandingMenuOption landingMenuOption = printLandingMenuAndGetSelectedOption();
        System.out.println();

        switch (landingMenuOption) {
            case Register -> shouldShowMainMenu = registerUser();
            case Login -> shouldShowMainMenu = loginUser();
            case Exit -> System.exit(0);
        }

        if (shouldShowMainMenu) {
            showMainMenu();
        } else {
            showLandingMenu();
        }
    }

    /**
     * Displays landing menu as well as provides input provided by the user
     * @return LandingMenuOption selected by the user
     */
    private LandingMenuOption printLandingMenuAndGetSelectedOption() {
        String menu = """
                --------------------------------------
                1. Register
                2. Login
                3. Exit
                --------------------------------------
                """;
        System.out.print(menu);
        System.out.print("Select an option between 1 and 3: ");

        String selectedMenuOptionString = scanner.nextLine();
        if (isMenuOptionInvalid(selectedMenuOptionString, LandingMenuOption.values().length)) {
            System.out.println("Oops wrong input provided, please try again.\n");
            return printLandingMenuAndGetSelectedOption();
        }
        return LandingMenuOption.values()[Integer.parseInt(selectedMenuOptionString) - 1];
    }

    /**
     * Register a user into the application
     * @return True if registration successful, false otherwise
     */
    private boolean registerUser() {
        // User ID
        System.out.print("Enter UserID: ");
        String userId = scanner.nextLine();
        if (userId.isBlank()) {
            System.out.println("UserID is empty, please try again.\n");
            return false;
        } else if (userAuthService.isUserRegistered(userId)) {
            System.out.println("User is already registered \n");
            return false;
        }

        // Password
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        if (password.isBlank()) {
            System.out.println("Password is empty, please try again.\n");
            return false;
        }

        // Security Question
        System.out.print("Enter Security Question: ");
        String securityQuestion = scanner.nextLine();
        if (securityQuestion.isBlank()) {
            System.out.println("Security question is empty, please try again.\n");
            return false;
        }

        // Security Question Answer
        System.out.print("Enter Security Answer: ");
        String securityAnswer = scanner.nextLine();
        if (securityAnswer.isBlank()) {
            System.out.println("Security answer is empty, please try again.\n");
            return false;
        }

        // Register User
        boolean isUserRegistered = userAuthService.registerUser(userId, password, securityQuestion, securityAnswer);
        System.out.println(isUserRegistered ? "User registered successfully!" : "Failed to register user");
        System.out.println();
        return isUserRegistered;
    }

    /**
     * Log in user into the application
     * @return True if login successful, false otherwise
     */
    private boolean loginUser() {
        // User ID
        System.out.print("Enter UserID: ");
        String userId = scanner.nextLine();
        if (userId.isBlank()) {
            System.out.println("UserID is empty, please try again.\n");
            return false;
        } else if (!userAuthService.validateUserIdToLogin(userId)) {
            System.out.println("User not found! Please login with valid user ID. \n");
            return false;
        }

        // Password
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        if (password.isBlank()) {
            System.out.println("Password is empty, please try again.\n");
            return false;
        }

        // Security question
        String securityQuestion = userAuthService.validatePasswordAndGetSecurityQuestion(password);
        if (securityQuestion == null) {
            System.out.println("Invalid password! Please try again with valid password. \n");
            return false;
        }

        // Security answer
        System.out.println("Please answer this question: " + securityQuestion);
        String securityAnswer = scanner.nextLine();
        boolean isSecurityAnswerValid = userAuthService.validateSecurityAnswer(securityAnswer);
        if (!isSecurityAnswerValid) {
            System.out.println("Security answer invalid! Please try again with valid security answer. \n");
            return false;
        }
        return true;
    }

    /**
     * Displays the main menu where user can access application features
     */
    @SuppressWarnings("InfiniteRecursion") // Suppressing cause compiler is not figuring out System.exit
    private void showMainMenu() {
        MainMenuOption mainMenuOption = printMainMenuAndGetSelectedOption();
        System.out.println();

        switch (mainMenuOption) {
            case WriteQueries -> {
                QueryProcessor queryProcessor = new QueryProcessor();
                queryProcessor.startAcceptingQueries();
            }
            case ExportStructureAndValue -> exportStructureAndValue();
            case Erd -> generateERD();
            case Exit -> System.exit(0);
        }

        showMainMenu();
    }

    /**
     * Displays main menu as well as provides input provided by the user
     * @return MainMenuOption selected by the user
     */
    private MainMenuOption printMainMenuAndGetSelectedOption() {
        String menu = """
                --------------------------------------
                1. Write Queries
                2. Export Structure and Value
                3. ERD
                4. Exit
                --------------------------------------
                """;
        System.out.print(menu);
        System.out.print("Select an option between 1 and 4: ");

        String selectedMenuOptionString = scanner.nextLine();
        if (isMenuOptionInvalid(selectedMenuOptionString, MainMenuOption.values().length)) {
            System.out.println("Oops wrong input provided, please try again.\n");
            return printMainMenuAndGetSelectedOption();
        }
        return MainMenuOption.values()[Integer.parseInt(selectedMenuOptionString) - 1];
    }

    /**
     * Checks if the selected menu option is valid or not
     * @param selectedMenuOptionString String representing the number (menu option) selected by the user
     * @param totalOptionCount Total number of options available
     * @return True if menu option string is valid, false otherwise
     */
    private boolean isMenuOptionInvalid(String selectedMenuOptionString, int totalOptionCount) {
        if (selectedMenuOptionString == null || selectedMenuOptionString.isBlank())
            return true;
        try {
            int selectedMenuOption = Integer.parseInt(selectedMenuOptionString);
            return selectedMenuOption <= 0 || selectedMenuOption > totalOptionCount;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Inputs database name from user and exports its structure and value
     */
    private void exportStructureAndValue() {
        System.out.print("Enter Database name: ");
        String databaseName = scanner.nextLine();
        if (databaseName.isBlank()) {
            System.out.println("Database name is empty, please try again.\n");
            return;
        }
        boolean isDatabaseAbscent = DatabaseManager.getDatabases().stream()
                .noneMatch(database -> database.getName().equals(databaseName));
        if (isDatabaseAbscent) {
            System.out.println("Database not found! Please try again.\n");
            return;
        }
        try {
            FileManager.generateSQLDump(databaseName);
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println("Database exported.");
    }

    /**
     * Inpyts the database name from the user, and generates an ERD from it.
     * The ERD is stored as a txt file in the Database directory.
     */
    private void generateERD() {
        System.out.print("Enter Database name: ");
        String databaseName = scanner.nextLine();
        if (databaseName.isBlank()) {
            System.out.println("Database name is empty, please try again.\n");
            return;
        }

        Database database = DatabaseManager.getDatabases().stream()
                .filter(db -> db.getName().equals(databaseName))
                .findFirst()
                .orElse(null);

        if (database == null) {
            System.out.println("Database not found! Please try again.\n");
            return;
        }

        ERDGenerator erdGenerator = new ERDGenerator(database, new LinkedHashMap<>());
        String erd = erdGenerator.generateERD();

        String fileName = FileManager.DATABASES_DIRECTORY + "/" + databaseName + "_ERD.txt";
        FileManager.writeToFile(fileName, erd);
        System.out.println("ERD generated and saved to " + fileName);
    }
}
