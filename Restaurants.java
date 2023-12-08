import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Restaurants {
  public static void main(String[] args) {
    run();
  }
  public static String appUsername;
  public static void run() throws SQLException {
    Scanner scanner = new Scanner(System.in);

    // prompt for sql username and passwords.
    String username, password;
    String accountCheck;

    boolean validInput, validCreate;

    // Prompt the user for MySQL username and password
    System.out.println("Enter MySQL username: ");
    username = scanner.nextLine();

    System.out.println("Enter MySQL password: ");
    password = scanner.nextLine();

    Connection connection = connectToDatabase(username, password);

    if (connection != null) {
      System.out.println("Connected to the database!");
    }

    // Prompt the user for the app username and password/
    // Create an account if the user has no username.
    // check if the input is valid -- only move forward when the input is valid.
    do {
      System.out.println("Do you have an account?\nY - yes\nN - no");
      accountCheck = scanner.nextLine();
    } while (!validateAccountInput(accountCheck));
    // if the user has an account -- login.
    if(accountCheck.equalsIgnoreCase("y")) {
      login(connection, scanner);
    }
    // if the user does not have an account -- create and login.
    if(accountCheck.equalsIgnoreCase("n")) {
      createloginAccount(connection, scanner);
    }
    // three options:
    // 1. view restaurants
    // 2. view reviews created by the user.
    // 3. view reservations made by the user.
    displayMenu();
    int choice = scanner.nextInt();
    switch (choice) {
      case 1:
        viewRestaurants(connection, scanner);
        break;
      case 2:
        // viewReviews(connection);
        break;
      case 3:
        // viewReservations(connection);
        break;
      default:
        System.out.println("Invalid choice. Please enter a valid option.");
        break;
    }
  }

  // connect to the database.
  private static Connection connectToDatabase(String username, String password) {
    try {
      String url = "jdbc:mysql://localhost:3306/music1_db";
      return DriverManager.getConnection(url, username, password);

    } catch (SQLException e) {
      System.out.println("Connection failed. Error: " + e.getMessage());
      return null;
    }
  }
  /** account existence input validity check.
   * check if user input valid answer for having an account or not.
   * anything other than y/ n is invalid.
   * */
  private static boolean validateAccountInput(String accountcheck) {

    if(!accountcheck.equalsIgnoreCase("y") && !accountcheck.equalsIgnoreCase("n")) {
      System.out.println("Invalid input! ");
      System.out.println("please enter 'y' if you have an account\n please enter 'n' if you dont' have an account");
      return false;
    }
    return true;
  }

  /**
   * login method.
   * check if the username and password is correct.
   * else close the
   */
  private static void login(Connection connection, Scanner scanner) {
    boolean loggedIn = false;
    while (!loggedIn) {
      System.out.print("Enter username: ");
      String inputUsername = scanner.nextLine();

      System.out.print("Enter password: ");
      String inputPassword = scanner.nextLine();
      String sql = "SELECT user_name, password FROM users WHERE user_name = ?";
      try {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, inputUsername);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
          // the username exists in the database.
          String storedUsername = resultSet.getString("user_name");
          String storedPassword = resultSet.getString("user_password");

          if (inputPassword.equals(storedPassword)) {
            System.out.println("Login successful!");
            appUsername = storedUsername; // track the logged-in user.
          } else {
            System.out.println("Invalid password! Try again.");
          }
        } else {
          // the username does not exist in the database.
          System.out.println("Username not found! Try again");
        }

        // close the result set and statement.
        resultSet.close();
        preparedStatement.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
  /**
   * create a new account and login for the user.
   * */
  private static void createloginAccount(Connection connection, Scanner scanner) {
    boolean usernameUniq = false;
    while (!usernameUniq) {
      System.out.println("Enter your new username: ");
      String newUsername = scanner.nextLine();

      // Check if the entered username already exists
      if (usernameExists(connection, newUsername)) {
        System.out.println("Username already exists. Please choose a different username.");
      } else {
        System.out.print("Enter a password: ");
        String newPassword = scanner.nextLine();

        // Insert the new user into the database
        String sql = "INSERT INTO users (user_name, password) VALUES (?, ?)";
        try {
          PreparedStatement preparedStatement = connection.prepareStatement(sql);
          preparedStatement.setString(1, newUsername);
          preparedStatement.setString(2, newPassword);
          preparedStatement.executeUpdate();

          System.out.println("Account created successfully!");
          appUsername = newUsername; // track the logged-in user.

          // You can add additional steps or actions after creating the account

        } catch (SQLException e) {
          // Handle any SQL exceptions
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Helper function to check if a username already exists in the database.
   */
  private static boolean usernameExists(Connection connection, String username) {
    String sql = "SELECT user_name FROM users WHERE user_name = ?";
    try {
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, username);
      ResultSet resultSet = preparedStatement.executeQuery();

      // If resultSet has any rows, the username already exists
      return resultSet.next();

    } catch (SQLException e) {
      // Handle any SQL exceptions
      e.printStackTrace();
      return false; // Return false in case of an exception
    }
  }

  /**
   * display 3 options for the user:  view restaurants, view reviews, view reservations.
   * */
  private static void displayMenu() {
    System.out.println("Choose an option:");
    System.out.println("1. View Restaurants");
    System.out.println("2. View Reviews");
    System.out.println("3. View Reservations");
  }

  /**
   * method to retrieve restaurants information.
   * */
  private static void viewRestaurants(Connection connection, Scanner scanner) throws SQLException {
    // Implement logic to fetch and display restaurants from the database
    // Use a PreparedStatement to execute SQL queries
    List<String> filters = new ArrayList<>();
    System.out.println("Choose filter options (enter 0 when done):");
    System.out.println("1. Area： Allston Cambridge Quincy Somerville");
    System.out.println("2. Category: American Chinese Greek Italian Japanese Spanish");
    System.out.println("3. Review Average Star"); // filter restaurants than have more stars than the given value.
    int filterChoice;

    do {
      filterChoice = scanner.nextInt();
      if(filterChoice != 0) {
        System.out.println("Enter your desired filter value: ");
        String filterValue = scanner.nextLine();
        filters.add(filterChoice + ":" + filterValue); // record user's choices in the list.
      }
    } while (filterChoice != 0);

    FilterandChooseRestaurants(connection, filters, scanner);



  }
  /**
   * fetch and display the restaurants using the filters given.
   * */
  private static void FilterandChooseRestaurants(Connection connection, List<String> filters, Scanner scanner) throws SQLException {
    StringBuilder queryBuilder = new StringBuilder("SELECT * FROM restaurants WHERE ");
    for(String filter : filters) {
      String[] parts = filter.split(":");
      int filterChoice = Integer.parseInt(parts[0]);
      String filterValue = parts[1];

      switch (filterChoice) {
        case 1:
          queryBuilder.append("area = '").append(filterValue).append("' AND ");
          break;
        case 2:
          queryBuilder.append("category = '").append(filterValue).append("' AND ");
          break;
        case 3:
          queryBuilder.append("avg_stars >= ").append(Double.parseDouble(filterValue)).append(" AND ");
          break;
        default:
          System.out.println("Invalid filter choice.");
          return;
      }
    }
    // remove the trailing "AND" from the query.
    queryBuilder.setLength(queryBuilder.length() - 5);

    // Execute the constructed query and display results.
    try(Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(queryBuilder.toString())) {
      while(resultSet.next()) {
        String name = resultSet.getString("restaurant_name");
        String address = resultSet.getString("address");
        Time openTime = resultSet.getTime("open_time");
        Time closeTime = resultSet.getTime("closed_time");
        String area = resultSet.getString("area");
        String category = resultSet.getString("category");
        BigDecimal average_star = resultSet.getBigDecimal("avg_stars");
        System.out.println("Restaurant name: " + name);
        System.out.println("Area: " + area);
        System.out.println("Category: " + category);
        System.out.println("Average Stars: " + average_star);
        System.out.println("Restaurant Hours: " + openTime + " - " + closeTime);
        System.out.println("Direction: " + address);
      }
      // prompt the user for restaurant selection.
      System.out.println("Please enter your selected restaurant (input the restaurant name): ");
      String selectedRes = scanner.nextLine();
      // check the validity of the input.
      while(!isValidRestaurantName(selectedRes, resultSet)) {
        System.out.println("Invalid input! please input a restaurant listed above. ");
        selectedRes = scanner.nextLine();
      }



    } catch (SQLException e) {
      e.printStackTrace();
    }
    }

  /***
   * check if the user's input matches with any of filtered restaurants.
   */
    private static boolean isValidRestaurantName(String enteredName, ResultSet resultSet) throws SQLException{
      resultSet.beforeFirst(); // move the cursor back to the beginning.
      while(resultSet.next()) {
        String name = resultSet.getString("name");
        if(enteredName.equalsIgnoreCase(name)) {
          return true;
        }
      }
      return false;
    }







}
