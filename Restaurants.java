import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Restaurants {

  private String sqlUsername;
  private String sqlPassword;
  private void setSqlUsername(String sqlUsername) { this.sqlUsername = sqlUsername; }
  private void setSqlPassword(String sqlPassword) { this.sqlPassword = sqlPassword; }

  public Connection connection = null;
  public Scanner scanner = null;

  public String appUsername;
  /**
   * Get a new database connection
   * @throws SQLException failed to connect
   */
  public void getConnection() throws SQLException {
    Properties connectionProps = new Properties();
    connectionProps.put("user", this.sqlUsername);
    connectionProps.put("password", this.sqlPassword);

    String serverName = "localhost";
    int portNumber = 3306;
    String dbName = "fakeYelp_db";
    this.connection = DriverManager.getConnection("jdbc:mysql://"
            + serverName + ":" + portNumber + "/" + dbName
            + "?characterEncoding=UTF-8&useSSL=false" + connectionProps);
  }

  /**
   * Prompt user for username and password
   */
  public void getSQLLogin() {
    System.out.println("Please enter your MySQL username");
    if (scanner.hasNext()) {
      setSqlUsername(scanner.next());
    }
    System.out.println("Please enter your MySQL password");
    if (scanner.hasNext()) {
      setSqlPassword(scanner.next());
    }
  }

  public void getYelpLogin() {
    System.out.println("Do you have an account?\nY - yes\nN - no");
    String accountCheck = scanner.nextLine().toLowerCase();

    switch (accountCheck) {
      case "y" -> login();
      case "n" -> createLoginAccount();
      default -> System.out.println("""
              Invalid input!
              Please enter 'y' if you have an account
              Please enter 'n' if you don't' have an account""");
    }
  }

  private void login() {
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
            this.appUsername = storedUsername; // track the logged-in user.
            loggedIn = true;
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

  private void createLoginAccount() {
    boolean usernameUniq = false;
    while (!usernameUniq) {
      System.out.println("Enter your new username: ");
      String newUsername = scanner.nextLine();

      // Check if the entered username already exists
      if (usernameExists(newUsername)) {
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
          this.appUsername = newUsername; // track the logged-in user.

          // You can add additional steps or actions after creating the account

        } catch (SQLException e) {
          // Handle any SQL exceptions
          e.printStackTrace();
        }
      }
    }
  }

  private boolean usernameExists(String username) {
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

  private void displayMenu() {
    System.out.println("Choose an option:");
    System.out.println("1. View Restaurants");
    System.out.println("2. View Reviews");
    System.out.println("3. View Reservations");
  }

  /**
   * method to retrieve restaurants information.
   * */
  private void viewRestaurants(Scanner scanner) throws SQLException {
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

    //select a restaurant.
    String selectedRestaurant = "";
    filterAndChooseRestaurants(filters, selectedRestaurant);

    //prompt the user to proceed with 4 options
    restaurantMainMenu(scanner, selectedRestaurant, connection);

  }
  /**
   * fetch and display the restaurants using the filters given.
   * */
  private void filterAndChooseRestaurants(List<String> filters, String selectedRestaurant) throws SQLException {
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
          return ;
      }
    }
    // remove the trailing "AND" from the query.
    queryBuilder.setLength(queryBuilder.length() - 5);

    // Execute the constructed query and display results.
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery(queryBuilder.toString());
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
        selectedRes = scanner.nextLine();}
      selectedRestaurant = selectedRes;
  }

  /***
   * check if the user's input matches with any of filtered restaurants.
   */
  private boolean isValidRestaurantName(String enteredName, ResultSet resultSet) throws SQLException{
    resultSet.beforeFirst(); // move the cursor back to the beginning.
    while(resultSet.next()) {
      String name = resultSet.getString("name");
      if(enteredName.equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * show the main menu and access to 4 options.
   */
  private void restaurantMainMenu(Scanner scanner, String selectedRes, Connection connection) throws SQLException {
    boolean exit = false;

    while (!exit) {
      // Display the main menu
      System.out.println("Main Menu:");
      System.out.println("a. View Reviews");
      System.out.println("b. Add Reviews");
      System.out.println("c. Make a New Reservation");
      System.out.println("d. View the Menu");
      System.out.println("e. Exit");

      // Get user input for selecting an option
      System.out.println("Enter your choice: ");
      String choice = scanner.nextLine().toLowerCase();

      switch (choice) {
        case "a":
          viewReviews(selectedRes, connection);
          break;
        case "b":
          addReview(selectedRes, connection, scanner);
          break;
        case "c":
          makeReservation(selectedRes, connection, scanner);
          break;
        case "d":
          viewMenu(selectedRes, connection, scanner);
          break;
        case "e":
          exit = true;
          System.out.println("Exiting the application. Goodbye!");
          break;
        default:
          System.out.println("Invalid choice. Please enter a valid option.");
          break;
      }
    }
  }

  /**
   * retrieve the reviews for a given restaurant name.
   * */
  private static void viewReviews(String resName, Connection connection) throws SQLException {
    // Implementation for viewing reviews
    System.out.println("Viewing Reviews...");
    String callStatement = "{call GetReviewsByRestaurantName(?)}";
    try (CallableStatement callableStatement = connection.prepareCall(callStatement)){
      callableStatement.setString(1, resName);

      // Execute the stored procedure
      ResultSet resultSet = callableStatement.executeQuery();

      // Process the result set
      while (resultSet.next()) {
        // Retrieve and print review details
        int id  = resultSet.getInt("review_id");
        int stars = resultSet.getInt("stars");
        Date date =  resultSet.getDate("review_date");
        String reviewer = resultSet.getString("reviewer");
        String content = resultSet.getString("content");
        System.out.println("Review id: " + id + " Stars :" + stars + "Review Data: " + date
                + " Reviewer: " + reviewer);
        System.out.println("Content: " + content);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * insert a review to the reviews table.
   * */
  private void addReview(String restaurantName, Connection connection, Scanner scanner) {
    // Implementation for adding a review
    System.out.println("Adding Review...");
    // get the stars.
    System.out.println("Enter stars (1-5): ");
    int stars = scanner.nextInt();

    scanner.nextLine();  // Consume the newline character
    // get the review contents.
    System.out.println("Enter review content: ");
    String content = scanner.nextLine();

    String sql = "INSERT INTO reviews (review_date, restaurant, reviewer, stars, content) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      // insert the current date.
      preparedStatement.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
      preparedStatement.setString(2, restaurantName);
      preparedStatement.setString(3, appUsername);
      preparedStatement.setInt(4, stars);
      preparedStatement.setString(5, content);

      // Execute the INSERT statement
      int rowsAffected = preparedStatement.executeUpdate();

      if (rowsAffected > 0) {
        System.out.println("Review added successfully!");
      } else {
        System.out.println("Failed to add the review.");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * make reservation and check if the reserved time is in the restaurant's open hours.
   * */
  private void makeReservation(String selectedRes, Connection connection, Scanner scanner) {
    // Implementation for making a new reservation
    System.out.println("Making a New Reservation...");
    // prompt reservation time.
    System.out.println("Enter reservation time (YYYY-MM-DD HH:mm:ss): ");
    String reservedTime = scanner.nextLine();
    Timestamp rsTime = Timestamp.valueOf(reservedTime);
    // prompt guest numbers.
    System.out.println("Enter guest numbers: ");
    int guestNumber = scanner.nextInt();

    scanner.nextLine(); // Consume the newline character
    //get the customer's username.
    String customerName = appUsername;
    String sql = "{call InsertReservationWithTimeCheck(?, ?, ?, ?)}";
    try(CallableStatement callableStatement = connection.prepareCall(sql)) {
      callableStatement.setTimestamp(1, rsTime);
      callableStatement.setInt(2, guestNumber);
      callableStatement.setString(3, customerName);
      callableStatement.setString(4, selectedRes);
      callableStatement.registerOutParameter(4, Types.INTEGER); // Register the OUT parameter for the generated ID
      // Execute the stored procedure
      callableStatement.executeUpdate();

      // Retrieve the generated ID
      int reservationId = callableStatement.getInt(4);
      System.out.println("Reservation with ID " + reservationId + " created successfully!");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * retrieve menu info of the given restaurant.
   * */
  private static void viewMenu(String selectedRes, Connection connection, Scanner scanner) {
    // Implementation for viewing the menu
    System.out.println("Viewing Menu ...");
    String selectStatement = "SELECT * FROM menus WHERE restaurant_name = ?";
    boolean continueViewing = true;
    while (continueViewing) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(selectStatement)) {
        // Set parameter
        preparedStatement.setString(1, selectedRes);
        // Execute the SELECT statement
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          while (resultSet.next()) {
            // Retrieve and display menu details
            String menuName = resultSet.getString("menu_name");
            String description = resultSet.getString("description");
            System.out.println("Menu Name: " + menuName);
            System.out.println("Description: " + description);
            // Display more fields as needed
            System.out.println("------");
          }
        }
        System.out.println("Enter the menu name you want to view (type 'exit' to go back): ");
        String userInput = scanner.nextLine();
        retrieveCuisines(userInput, connection);

        if ("exit".equalsIgnoreCase(userInput)) {
          continueViewing = false;
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * retrieve cuisines info using a given menu name.
   * */
  public static void retrieveCuisines(String menuName, Connection connection) {
    try {
      String selectStatement = "SELECT name, price FROM cuisines WHERE menu_name = ?";
      try (PreparedStatement preparedStatement = connection.prepareStatement(selectStatement)) {
        // Set parameter
        preparedStatement.setString(1, menuName);

        // Execute the SELECT statement
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          while (resultSet.next()) {
            // Retrieve and display cuisine details
            String cuisineName = resultSet.getString("name");
            double price = resultSet.getDouble("price");
            System.out.println("Cuisine Name: " + cuisineName + " Price: $" + price);
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void run() {
    scanner = new Scanner(System.in);

    while (true) {
      try {
        getSQLLogin();
        getConnection();
        System.out.println("Connected to the database!");
        break;
      } catch (SQLException dbConnectionError) {
        System.out.println("Connection failed. Error: " + dbConnectionError.getMessage());
      }
    }
    getYelpLogin();
    displayMenu();
    int choice = scanner.nextInt();
    switch (choice) {
      case 1:
        try {
          viewRestaurants(scanner);
        } catch (SQLException e) {
          e.printStackTrace();
        }

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

  public static void main(String[] args) {
    Restaurants restaurants = new Restaurants();
    restaurants.run();
  }

}
