import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;
import java.util.regex.*;

public class FitnessTrackerGUI {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private JPanel userPanel, activityPanel, trainerPanel;
    private JTextField userNameField, userAgeField, userEmailField;
    private JComboBox<String> userGenderBox, userBoxActivity, userBoxTrainer, activityBox, trainerBox,deleteUserBox;
    private JButton addUserButton, assignActivityButton, assignTrainerButton,deleteUserButton,updateUserButton;
    private Connection conn;
    private JTable table;
    private JPanel panel;
    public FitnessTrackerGUI() {
        frame = new JFrame("Fitness Tracker");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        connectDatabase();
        createUserPanel();
        createActivityPanel();
        createTrainerPanel();
        createTablePanel();
        fetchDataForTable();

        tabbedPane.addTab("Users", userPanel);
        tabbedPane.addTab("Activities", activityPanel);
        tabbedPane.addTab("Trainers", trainerPanel);
        tabbedPane.addTab("FitnessTracker", tablePanel);

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void connectDatabase() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "system", "it23118");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Database Connection Failed: " + e.getMessage());
        }
    }
    private void fetchDataForTable() {
        String query = "SELECT u.Name AS UserName, t.Name AS TrainerName, a.ActivityName AS Activity, " +
                "p.Duration, p.performeddate, p.CaloriesBurned " +
                "FROM Performs p " +
                "JOIN Users u ON p.UserID = u.UserID " +
                "JOIN Activity a ON p.ActivityID = a.ActivityID " +
                "JOIN Trains tr ON u.UserID = tr.UserID " +
                "JOIN Trainer t ON tr.TrainerID = t.TrainerID " +
                "ORDER BY p.performeddate";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Get the metadata to retrieve column names dynamically
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }

            // Set the data model for the table
            DefaultTableModel model = new DefaultTableModel(data, columnNames);
            JTable table = new JTable(model);
            table.setFillsViewportHeight(true);

            JScrollPane scrollPane = new JScrollPane(table);
            // Assuming you have a JPanel to display the table
            tablePanel.removeAll();  // Remove any existing components
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            tablePanel.revalidate();
            tablePanel.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error Fetching Data: " + e.getMessage());
        }
    }
    private JPanel tablePanel;

    private void createTablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(220, 255, 220));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Assigned Activities"));
    }
    private void createUserPanel() {
        userPanel = new JPanel(new GridBagLayout());
        userPanel.setBackground(new Color(200, 255, 200));
        userPanel.setBorder(BorderFactory.createTitledBorder("User Management"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font fieldFont = new Font("Arial", Font.PLAIN, 16);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(labelFont);
        userPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        userNameField = new JTextField(20);
        userNameField.setFont(fieldFont);
        userPanel.add(userNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setFont(labelFont);
        userPanel.add(ageLabel, gbc);

        gbc.gridx = 1;
        userAgeField = new JTextField(10);
        userAgeField.setFont(fieldFont);
        userPanel.add(userAgeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setFont(labelFont);
        userPanel.add(genderLabel, gbc);

        gbc.gridx = 1;
        userGenderBox = new JComboBox<>(new String[]{"Male", "Female"});
        userGenderBox.setFont(fieldFont);
        userPanel.add(userGenderBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        userPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        userEmailField = new JTextField(25);
        userEmailField.setFont(fieldFont);
        userPanel.add(userEmailField, gbc);

        // Add User Button
        gbc.gridx = 0;
        gbc.gridy = 4;
        addUserButton = new JButton("Add User");
        addUserButton.setFont(labelFont);
        addUserButton.addActionListener(e -> addUser());
        userPanel.add(addUserButton, gbc);

        // Update User Button
        gbc.gridx = 1;
        updateUserButton = new JButton("Update User");
        updateUserButton.setFont(labelFont);
        updateUserButton.addActionListener(e -> updateUser());


        userPanel.add(updateUserButton, gbc);

        // Delete Section
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel deleteLabel = new JLabel("Select User:");
        deleteLabel.setFont(labelFont);
        userPanel.add(deleteLabel, gbc);

        gbc.gridx = 1;
        deleteUserBox = new JComboBox<>(fetchUsers());
        deleteUserBox.setFont(fieldFont);
        userPanel.add(deleteUserBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        deleteUserButton = new JButton("Delete User");
        deleteUserButton.setFont(labelFont);
        deleteUserButton.addActionListener(e -> deleteUser());
        deleteUserBox.addActionListener(e -> loadUserDetails());
        userPanel.add(deleteUserButton, gbc);
    }

    private void deleteUser() {
        String selectedUser = (String) deleteUserBox.getSelectedItem();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(frame, "Please select a user to delete!");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Users WHERE Name = ?")) {
            stmt.setString(1, selectedUser);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(frame, "User Deleted Successfully!");
                userNameField.setText("");
                userAgeField.setText("");
                userEmailField.setText("");
                userGenderBox.setSelectedIndex(0);
                refreshDropdowns();
            } else {
                JOptionPane.showMessageDialog(frame, "User Not Found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error Deleting User: " + e.getMessage());
        }
    }
    private JLabel caloriesBurnedLabel;
    private JTextField durationField;// Declare at class level

    private void createActivityPanel() {
        activityPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        activityPanel.setBackground(new Color(180, 255, 220));
        activityPanel.setBorder(BorderFactory.createTitledBorder("Activity Management"));

        activityPanel.add(new JLabel("Select User:"));
        userBoxActivity = new JComboBox<>(fetchUsers());
        activityPanel.add(userBoxActivity);

        activityPanel.add(new JLabel("Select Activity:"));
        activityBox = new JComboBox<>(fetchActivities());
        activityPanel.add(activityBox);

        activityPanel.add(new JLabel("Duration (mins):"));
        durationField = new JTextField();
        activityPanel.add(durationField);

        activityPanel.add(new JLabel("Calories Burned:"));
        caloriesBurnedLabel = new JLabel("0");
        activityPanel.add(caloriesBurnedLabel);

        assignActivityButton = new JButton("Assign Activity");
        assignActivityButton.addActionListener(e -> {
            calculateCaloriesBurned();
            assignActivity();
            refreshDropdowns();
        });
        activityPanel.add(assignActivityButton);
    }


    private void calculateCaloriesBurned() {
        try {
            String activityName = (String) activityBox.getSelectedItem();
            int duration = Integer.parseInt(durationField.getText());

            PreparedStatement stmt = conn.prepareStatement("SELECT CaloriesPerMinute FROM Activity WHERE ActivityName = ?");
            stmt.setString(1, activityName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double caloriesPerMinute = rs.getDouble("CaloriesPerMinute");
                double totalCalories = duration * caloriesPerMinute;
                caloriesBurnedLabel.setText(String.format("%.2f", totalCalories));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error Calculating Calories: " + e.getMessage());
        }
    }

    private void assignActivity() {
        try {
            String selectedUser = (String) userBoxActivity.getSelectedItem();
            String selectedActivity = (String) activityBox.getSelectedItem();
            int duration = Integer.parseInt(durationField.getText());

            PreparedStatement activityStmt = conn.prepareStatement("SELECT ActivityID FROM Activity WHERE ActivityName = ?");
            activityStmt.setString(1, selectedActivity);
            ResultSet rs = activityStmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(frame, "Activity not found!");
                return;
            }
            int activityId = rs.getInt("ActivityID");

            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO Performs (UserID, ActivityID, Duration, CaloriesBurned) " +
                            "VALUES ((SELECT UserID FROM Users WHERE Name=?), ?, ?, ?)");
            stmt.setString(1, selectedUser);
            stmt.setInt(2, activityId);
            stmt.setInt(3, duration);
            stmt.setDouble(4, Double.parseDouble(caloriesBurnedLabel.getText()));
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(frame, "Activity Assigned Successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error Assigning Activity: " + e.getMessage());
        }
    }
    private JButton addTrainerButton;
    private JButton deleteTrainerButton;
    private JLabel  trainerExperienceLabel;

    private void createTrainerPanel() {
        trainerPanel = new JPanel(new GridBagLayout());
        trainerPanel.setPreferredSize(new Dimension(700, 600)); // Larger panel size
        trainerPanel.setBackground(new Color(255, 220, 180));
        trainerPanel.setBorder(BorderFactory.createTitledBorder("Trainer Management"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel userLabel = new JLabel("Select User:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        trainerPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        userBoxTrainer = new JComboBox<>(fetchUsers());
        userBoxTrainer.setFont(new Font("Arial", Font.PLAIN, 16));
        trainerPanel.add(userBoxTrainer, gbc);

        JLabel trainerLabel = new JLabel("Select Trainer:");
        trainerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        trainerPanel.add(trainerLabel, gbc);

        gbc.gridx = 1;
        trainerBox = new JComboBox<>(fetchTrainers());
        trainerBox.setFont(new Font("Arial", Font.PLAIN, 16));
        trainerPanel.add(trainerBox, gbc);

        assignTrainerButton = new JButton("Assign Trainer");
        assignTrainerButton.setFont(new Font("Arial", Font.BOLD, 16));
        assignTrainerButton.setPreferredSize(new Dimension(200, 40));
        assignTrainerButton.addActionListener(e -> assignTrainer());
        gbc.gridx = 0;
        gbc.gridy = 2;
        trainerPanel.add(assignTrainerButton, gbc);

        addTrainerButton = new JButton("Add Trainer");
        addTrainerButton.setFont(new Font("Arial", Font.BOLD, 16));
        addTrainerButton.setPreferredSize(new Dimension(200, 40));
        addTrainerButton.addActionListener(e -> addTrainer());
        gbc.gridx = 1;
        trainerPanel.add(addTrainerButton, gbc);

        deleteTrainerButton = new JButton("Delete Trainer");
        deleteTrainerButton.setFont(new Font("Arial", Font.BOLD, 16));
        deleteTrainerButton.setPreferredSize(new Dimension(200, 40));
        deleteTrainerButton.addActionListener(e -> deleteTrainer());
        gbc.gridx = 0;
        gbc.gridy = 3;
        trainerPanel.add(deleteTrainerButton, gbc);
    }

    private void displayTrainerExperience() {
        String trainerName = (String) trainerBox.getSelectedItem();
        if (trainerName != null) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT Experience FROM Trainers WHERE Name = ?")) {
                stmt.setString(1, trainerName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    trainerExperienceLabel.setText("Experience: " + rs.getInt("Experience") + " years");
                }
            } catch (SQLException e) {
                trainerExperienceLabel.setText("Experience: N/A");
            }
        }
    }
    private void assignTrainer() {
        String selectedTrainer = (String) trainerBox.getSelectedItem();

        if (selectedTrainer == null) {
            JOptionPane.showMessageDialog(frame, "Please select a trainer!");
            return;
        }

        // Extract only the trainer's name before inserting into DB
        String trainerName = selectedTrainer.split(" - ")[0];
        String userName = (String) userBoxTrainer.getSelectedItem();

        try {
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Remove existing trainer for the user
            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM Trains WHERE UserID = (SELECT UserID FROM Users WHERE Name=?)")) {
                deleteStmt.setString(1, userName);
                deleteStmt.executeUpdate();
            }

            // Step 2: Insert new trainer assignment
            try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO Trains (UserID, TrainerID, UserName, TrainerName) VALUES ((SELECT UserID FROM Users WHERE Name=?), (SELECT TrainerID FROM Trainer WHERE Name=?), ?, ?)")) {
                insertStmt.setString(1, userName);
                insertStmt.setString(2, trainerName);
                insertStmt.setString(3, userName);
                insertStmt.setString(4, trainerName);
                insertStmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
            JOptionPane.showMessageDialog(frame, "Trainer Assigned Successfully!");

        } catch (SQLException e) {
            try {
                conn.rollback(); // Rollback if error occurs
            } catch (SQLException rollbackEx) {
                JOptionPane.showMessageDialog(frame, "Rollback Failed: " + rollbackEx.getMessage());
            }
            JOptionPane.showMessageDialog(frame, "Error Assigning Trainer: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true); // Restore auto-commit mode
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error resetting auto-commit: " + ex.getMessage());
            }
        }
    }

    private void addTrainer() {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Trainer (TrainerID, Name, Experience) VALUES (trainer_seq.NEXTVAL, ?, ?)");) {
            String trainerName = JOptionPane.showInputDialog(frame, "Enter Trainer Name:");
            String experienceStr = JOptionPane.showInputDialog(frame, "Enter Trainer Experience (years):");

            if (trainerName != null && !trainerName.trim().isEmpty() && experienceStr != null && !experienceStr.trim().isEmpty()) {
                int experience = Integer.parseInt(experienceStr);
                stmt.setString(1, trainerName);
                stmt.setInt(2, experience);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Trainer Added Successfully!");
                refreshDropdowns();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error Adding Trainer: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Experience Value! Please enter a number.");
        }
    }
    private void deleteTrainer() {
        String selectedTrainer = (String) trainerBox.getSelectedItem();

        if (selectedTrainer == null) {
            JOptionPane.showMessageDialog(frame, "Please select a trainer!");
            return;
        }

        // Extract the trainer's name if it includes experience details
        String trainerName = selectedTrainer.split(" - ")[0];

        try {
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Remove trainer from the 'Trains' table first
            try (PreparedStatement deleteTrainsStmt = conn.prepareStatement("DELETE FROM Trains WHERE TrainerID = (SELECT TrainerID FROM Trainer WHERE Name=?)")) {
                deleteTrainsStmt.setString(1, trainerName);
                deleteTrainsStmt.executeUpdate();
            }

            // Step 2: Remove trainer from the 'Trainers' table
            try (PreparedStatement deleteTrainerStmt = conn.prepareStatement("DELETE FROM Trainer WHERE Name = ?")) {
                deleteTrainerStmt.setString(1, trainerName);
                int rowsAffected = deleteTrainerStmt.executeUpdate();

                if (rowsAffected == 0) {
                    JOptionPane.showMessageDialog(frame, "Trainer not found in database!");
                    conn.rollback(); // Undo changes if trainer not found
                    return;
                }
            }

            conn.commit(); // Commit transaction
            JOptionPane.showMessageDialog(frame, "Trainer Deleted Successfully!");
            refreshDropdowns(); // Refresh GUI dropdowns

        } catch (SQLException e) {
            try {
                conn.rollback(); // Rollback on error
            } catch (SQLException rollbackEx) {
                JOptionPane.showMessageDialog(frame, "Rollback Failed: " + rollbackEx.getMessage());
            }
            JOptionPane.showMessageDialog(frame, "Error Deleting Trainer: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error resetting auto-commit: " + ex.getMessage());
            }
        }
    }
    private String[] fetchUsers() {
        ArrayList<String> users = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Name FROM Users");
            while (rs.next()) {
                users.add(rs.getString("Name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage());
        }
        return users.toArray(new String[0]);
    }

    private String[] fetchActivities() {
        return fetchData("SELECT ActivityName FROM Activity");
    }
    private String[] fetchTrainers() {
        ArrayList<String> trainers = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Name, Experience FROM Trainer ORDER BY ROWID")) {
            while (rs.next()) {
                String trainerInfo = rs.getString("Name") + " - " + rs.getInt("Experience") + " years";
                trainers.add(trainerInfo);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error Fetching Trainers: " + e.getMessage());
        }
        return trainers.toArray(new String[0]);
    }


    private String[] fetchData(String query) {
        ArrayList<String> data = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                data.add(rs.getString(1));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage());
        }
        return data.toArray(new String[0]);
    }

    private void refreshDropdowns() {
        userBoxActivity.setModel(new DefaultComboBoxModel<>(fetchUsers()));
        userBoxTrainer.setModel(new DefaultComboBoxModel<>(fetchUsers()));
        activityBox.setModel(new DefaultComboBoxModel<>(fetchActivities()));
        trainerBox.setModel(new DefaultComboBoxModel<>(fetchTrainers()));
        deleteUserBox.setModel(new DefaultComboBoxModel<>(fetchUsers()));
    }

    private void addUser() {
        String name = userNameField.getText().trim();
        String ageText = userAgeField.getText().trim();
        String gender = (String) userGenderBox.getSelectedItem();
        String email = userEmailField.getText().trim();

        // Basic validation
        if (name.isEmpty() || ageText.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "All fields must be filled!");
            return;
        }

        // Email validation using regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            JOptionPane.showMessageDialog(frame, "Invalid email format! Please enter a valid email.");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO Users (UserID, Name, Age, Gender, Email) VALUES (USER_SEQ.NEXTVAL, ?, ?, ?, ?)")) {
                stmt.setString(1, name);
                stmt.setInt(2, age);
                stmt.setString(3, gender);
                stmt.setString(4, email);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(frame, "User added successfully!");

                // Clear fields after successful insertion
                userNameField.setText("");
                userAgeField.setText("");
                userEmailField.setText("");

                refreshDropdowns();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid age! Please enter a valid number.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error Adding User: " + e);
        }
    }
    private void updateUser() {
        String selectedName = (String) deleteUserBox.getSelectedItem(); // Old Name
        String newName = userNameField.getText().trim(); // New Name
        String newAgeText = userAgeField.getText().trim();
        String newEmail = userEmailField.getText().trim();
        String newGender = (String) userGenderBox.getSelectedItem(); // New Gender

        if (selectedName == null || newName.isEmpty() || newAgeText.isEmpty() || newEmail.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "All fields must be filled!");
            return;
        }

        // Email validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!newEmail.matches(emailRegex)) {
            JOptionPane.showMessageDialog(frame, "Invalid email format! Please enter a valid email.");
            return;
        }

        try {
            int newAge = Integer.parseInt(newAgeText);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE Users SET Name = ?, Age = ?, Email = ?, Gender = ? WHERE Name = ?")) {
                stmt.setString(1, newName);
                stmt.setInt(2, newAge);
                stmt.setString(3, newEmail);
                stmt.setString(4, newGender);
                stmt.setString(5, selectedName);

                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(frame, "User updated successfully!");
                    userNameField.setText("");
                    userAgeField.setText("");
                    userEmailField.setText("");
                    refreshDropdowns(); // Refresh dropdowns after update
                } else {
                    JOptionPane.showMessageDialog(frame, "No matching user found!");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid age! Please enter a valid number.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error Updating User: " + e);
        }
    }
    private void loadUserDetails() {
        String selectedName = (String) deleteUserBox.getSelectedItem();
        if (selectedName == null) return;

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT Name, Age, Gender, Email FROM Users WHERE Name = ?")) {
            stmt.setString(1, selectedName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userNameField.setText(rs.getString("Name"));
                userAgeField.setText(String.valueOf(rs.getInt("Age")));
                userGenderBox.setSelectedItem(rs.getString("Gender"));
                userEmailField.setText(rs.getString("Email"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error Loading User Details: " + e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FitnessTrackerGUI::new);
    }
}