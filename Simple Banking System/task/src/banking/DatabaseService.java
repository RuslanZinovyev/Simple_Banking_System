package banking;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseService {
    private final String url;
    private File databaseFile;

    public DatabaseService(String url) {
        this.url = "jdbc:sqlite:" + url;
        databaseFile = new File(url);
    }

    public void createDatabase(String fileName) {
        try {
            if (!databaseFile.createNewFile()) {
                databaseFile = new File(fileName);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }

    public void createCardTable() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS card (\n" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    number TEXT NOT NULL,\n" +
                "    pin TEXT NOT NULL,\n" +
                "    balance INTEGER DEFAULT 0\n" +
                "   );";

        try (PreparedStatement preparedStatement = getConnection().prepareStatement(createTableSql)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addCard(String cardNumber, String pinCode) {
        String sql = "INSERT INTO card(number, pin) VALUES(?, ?)";

        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
             preparedStatement.setString(1, cardNumber);
             preparedStatement.setString(2, pinCode);
             preparedStatement.executeUpdate();
        } catch (SQLException e) {
             System.out.println(e.getMessage());
        }
    }

    public int getCardBalance(String cardNumber) {
        String sql = "SELECT balance FROM card WHERE number = ?";

        int result = 0;

        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, cardNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                result = resultSet.getInt("balance");
            }

        } catch (SQLException e) {
             System.out.println(e.getMessage());
        }
        return result;
    }

    public int checkIfCardAndPinExist(String cardNumber, String pinCode) {
        String sql = "SELECT COUNT(*) FROM card WHERE number = ? AND pin = ?";

        int result = 0;

        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, cardNumber);
            preparedStatement.setString(2, pinCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                result = resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public boolean checkIfCardExists(String cardNumber) {
        String sql = "SELECT number FROM card WHERE EXISTS(SELECT number FROM card WHERE number = ?)";

        boolean result = false;

        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, cardNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                result = resultSet.getBoolean(1);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    public boolean checkIfAmountExceedBalance(String cardNumber, String balance) {
        String sql = "SELECT balance FROM card WHERE EXISTS(SELECT balance FROM card WHERE number = ? AND balance > ?);";

        boolean result = false;

        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, cardNumber);
            preparedStatement.setString(2, balance);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                result = resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    public void addIncomeToCardBalance(int income, String cardNumber) {
        String sql = "UPDATE card SET balance = balance + ? WHERE number = ?";

        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            preparedStatement.setInt(1, income);
            preparedStatement.setString(2, cardNumber);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void transferMoneyFromCardToCard(String toCard, String fromCard, String amount) {
        String withdrawMoneySql = "UPDATE card SET balance = balance - ? WHERE number = ?";
        String addMoneySql = "UPDATE card SET balance = balance + ? WHERE  number = ?";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement withdrawMoneyPreparedStatement = getConnection().prepareStatement(withdrawMoneySql);
                 PreparedStatement addMoneyPreparedStatement = getConnection().prepareStatement(addMoneySql)) {

                 withdrawMoneyPreparedStatement.setInt(1, Integer.parseInt(amount));
                 withdrawMoneyPreparedStatement.setString(2, fromCard);
                 withdrawMoneyPreparedStatement.executeUpdate();

                 addMoneyPreparedStatement.setInt(1, Integer.parseInt(amount));
                 addMoneyPreparedStatement.setString(2, toCard);
                 addMoneyPreparedStatement.executeUpdate();

                connection.commit();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public void deleteCard(String cardNumber) {
        String sql = "DELETE FROM card WHERE number = ?";

        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, cardNumber);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void closeDatabaseConnection() {
        try {
            getConnection().close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
