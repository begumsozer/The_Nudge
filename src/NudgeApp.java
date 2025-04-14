import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class NudgeApp extends JFrame {
    // Data structures to manage user profiles, requests, tasks, and chats
    private Map<String, UserProfile> userProfiles = new HashMap<>();
    private Map<String, List<Request>> userRequests = new HashMap<>();
    private Map<String, List<Request>> studentTasks = new HashMap<>();
    private Map<String, Chat> activeChats = new HashMap<>(); // Maps request title to chat instance
    private UserProfile currentUserProfile;

    // Constructor: Initializes the application and loads necessary data
    public NudgeApp() {
        setTitle("Nudge App"); // Optional: Sets the title of the window
        setSize(300, 300); // Set your desired width and height here
        setResizable(false); // Optional: Prevents the window from being resized
        setLocationRelativeTo(null); // Centers the window on the screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loadUserProfiles();
        loadRequests();
        loadStudentTasks();
        loadChats();
        showWelcomeScreen();
    }

    // Displays the welcome screen with a logo and an entry button
    private void showWelcomeScreen() {
        getContentPane().removeAll(); // Clear previous components
        setLayout(new BorderLayout());

        // Create the main panel and set its size
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(700, 700)); // Set desired size for the JPanel

        // Welcome message at the top
        JLabel welcomeMessage = new JLabel("Welcome to Nudge!", SwingConstants.CENTER);
        welcomeMessage.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(welcomeMessage, BorderLayout.NORTH);

        // Displays a logo image centered
        JLabel imageLabel = new JLabel(new ImageIcon(Objects.requireNonNull(getClass().getResource("/Logo.jpg"))));
        mainPanel.add(imageLabel, BorderLayout.CENTER);

        // Entry button at the bottom
        JButton enterButton = new JButton("Log in or Sign in here");
        mainPanel.add(enterButton, BorderLayout.SOUTH);
        enterButton.addActionListener(e -> showLoginScreen());

        // Add the main panel to the JFrame
        add(mainPanel);

        // Pack the JFrame to fit the preferred sizes of the components
        pack();

        // JFrame properties
        setLocationRelativeTo(null); // Centers the window
        setVisible(true);  // Make sure the frame is visible
    }

    // Displays the login screen where users can log in or create a new account
    private void showLoginScreen() {
        getContentPane().removeAll();
        repaint();

        setLayout(new BorderLayout());

        // Header
        JLabel loginMessage = new JLabel("Welcome Back!", SwingConstants.CENTER);
        loginMessage.setFont(new Font("Arial", Font.BOLD, 24));
        loginMessage.setForeground(Color.BLACK); // Keeping the same color
        add(loginMessage, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setOpaque(false); // Keep background transparent
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Spacing

        // Email Field
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        // Status Field
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(statusLabel, gbc);

        JTextField statusField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(statusField, gbc);

        JButton loginButton = new JButton("Log In");
        JButton newUserButton = new JButton("I'm New");

        // Login button action
        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String status = statusField.getText().trim().toLowerCase();

            // Validate credentials
            if (validateLogin(email, status)) {
                currentUserProfile = userProfiles.get(email); // Set the current user profile
                if (currentUserProfile.status.equals("requester")) {
                    showRequesterHome(currentUserProfile);
                } else {
                    showStudentHome(currentUserProfile);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials! Please try again.");
            }
        });

        // Action for new user registration
        newUserButton.addActionListener(e -> showRegistrationScreen());

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(newUserButton);
        add(buttonPanel, BorderLayout.SOUTH);

        revalidate();
    }

    // Validates login credentials by checking email and status match
    private boolean validateLogin(String email, String status) {
        return userProfiles.containsKey(email) && userProfiles.get(email).status.equals(status);
    }

    // Displays the home screen for a requester user type
    private void showRequesterHome(UserProfile profile) {
        getContentPane().removeAll();
        repaint();

        setLayout(new BorderLayout());

        JLabel welcomeMessage = new JLabel("Welcome, " + profile.firstName, SwingConstants.CENTER);
        welcomeMessage.setFont(new Font("Arial", Font.BOLD, 20));
        add(welcomeMessage, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));

        // Buttons for various actions
        JButton postRequestButton = new JButton("Post a Request");
        JButton viewRequestsButton = new JButton("View My Requests");
        JButton logoutButton = new JButton("Log Out");

        postRequestButton.addActionListener(e -> showPostRequestScreen(profile));
        viewRequestsButton.addActionListener(e -> showRequestList(profile));
        logoutButton.addActionListener(e -> showLoginScreen());

        buttonPanel.add(postRequestButton);
        buttonPanel.add(viewRequestsButton);
        buttonPanel.add(logoutButton);

        add(buttonPanel, BorderLayout.CENTER);
        revalidate();
    }

    // Displays the screen to post a new request
    private void showPostRequestScreen(UserProfile profile) {
        getContentPane().removeAll();
        repaint();

        setLayout(new BorderLayout());

        JLabel postRequestMessage = new JLabel("Post a New Request", SwingConstants.CENTER);
        postRequestMessage.setFont(new Font("Arial", Font.BOLD, 20));
        add(postRequestMessage, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(6, 2));

        // Form fields for request details
        formPanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField();
        formPanel.add(titleField);

        formPanel.add(new JLabel("Description:"));
        JTextField descriptionField = new JTextField();
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("Deadline (dd/mm/yyyy):"));
        JTextField deadlineField = new JTextField();
        formPanel.add(deadlineField);

        formPanel.add(new JLabel("Time Range (HH:mm-HH:mm):"));
        JTextField timeRangeField = new JTextField();
        formPanel.add(timeRangeField);

        formPanel.add(new JLabel("Address:"));
        JTextField addressField = new JTextField();
        formPanel.add(addressField);

        formPanel.add(new JLabel("Price:"));
        JTextField priceField = new JTextField();
        formPanel.add(priceField);

        JButton submitButton = new JButton("Submit");
        JButton returnButton = new JButton("Return");

        // Submit button action
        submitButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            String deadline = deadlineField.getText().trim();
            String timeRange = timeRangeField.getText().trim();
            String address = addressField.getText().trim();
            String price = priceField.getText().trim();

            // Validate and save the request
            if (validateRequestForm(title, description, deadline, timeRange, address, price)) {
                Request request = new Request(title, description, deadline, timeRange, address, price);
                request.requesterEmail = profile.email; // Assign requester email to request
                userRequests.computeIfAbsent(profile.email, k -> new ArrayList<>()).add(request);
                saveRequests();
                JOptionPane.showMessageDialog(this, "Request posted successfully!");
                showRequesterHome(profile);
            }
        });

        returnButton.addActionListener(e -> showRequesterHome(profile));

        add(formPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.add(returnButton);
        add(buttonPanel, BorderLayout.SOUTH);

        revalidate();
    }

    // Validates the form for creating a new request
    private boolean validateRequestForm(String title, String description, String deadline, String timeRange, String address, String price) {
        if (title.isEmpty() || description.isEmpty() || deadline.isEmpty() || timeRange.isEmpty() || address.isEmpty() || price.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return false;
        }

        // Validate price format and value
        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be strictly positive!");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price! Please enter a valid number.");
            return false;
        }

        // Validate deadline format
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        Date requestDate;
        try {
            requestDate = sdf.parse(deadline);
            if (requestDate.before(new Date())) {
                JOptionPane.showMessageDialog(this, "Deadline must be a future date!");
                return false;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid deadline format! Please use dd/MM/yyyy.");
            return false;
        }

        // Validate time range format
        if (!timeRange.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Invalid time range format! Please use HH:mm-HH:mm.");
            return false;
        }

        return true;
    }

    // Displays the request list for the requester
    private void showRequestList(UserProfile profile) {
        getContentPane().removeAll();
        repaint();

        setLayout(new BorderLayout());

        JLabel requestListMessage = new JLabel("Your Requests", SwingConstants.CENTER);
        requestListMessage.setFont(new Font("Arial", Font.BOLD, 20));
        add(requestListMessage, BorderLayout.NORTH);

        List<Request> requests = userRequests.getOrDefault(profile.email, new ArrayList<>());

        JPanel requestPanel = new JPanel(new GridLayout(requests.size(), 1));

        // For each request, create a UI element to view/modify/delete the request
        for (Request request : requests) {
            JPanel requestItem = new JPanel(new GridLayout(1, 5));
            requestItem.add(new JLabel(request.toString()));

            JButton modifyButton = new JButton("Modify");
            modifyButton.setEnabled(request.status.equals("in the wait of answer"));
            modifyButton.addActionListener(e -> showModifyRequestScreen(profile, request));

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> {
                // Remove request from list
                requests.remove(request);
                if (request.status.equals("accepted")) {
                    removeRequestFromStudentTasks(request);
                }
                removeChat(request);
                saveRequests();
                showRequestList(profile); // Refresh the list
            });

            // Button for marking request as done
            JButton markDoneButton = new JButton("Mark as Done");
            markDoneButton.setEnabled(request.status.equals("accepted"));
            markDoneButton.addActionListener(e -> {
                // Mark request as done and refresh the list
                requests.remove(request);
                if (request.status.equals("accepted")) {
                    removeRequestFromStudentTasks(request);
                }
                removeChat(request);
                saveRequests();
                showRequestList(profile);
            });

            JButton chatButton = new JButton("Open Chat");
            chatButton.setEnabled(request.status.equals("accepted"));
            chatButton.addActionListener(e -> {
                if (request.status.equals("accepted")) {
                    showChatScreen(request);
                }
            });

            // Add buttons and request info to panel
            requestItem.add(modifyButton);
            requestItem.add(deleteButton);
            requestItem.add(markDoneButton);
            requestItem.add(chatButton);
            requestPanel.add(requestItem);
        }

        add(requestPanel, BorderLayout.CENTER);

        JButton backButton = new JButton("Back to Home");
        backButton.addActionListener(e -> showRequesterHome(profile));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        revalidate();
    }

    // Removes a request from student tasks if accepted
    private void removeRequestFromStudentTasks(Request request) {
        for (Map.Entry<String, List<Request>> entry : studentTasks.entrySet()) {
            List<Request> tasks = entry.getValue();
            if (tasks.removeIf(r -> r.equals(request))) {
                saveStudentTasks(); // Save updated tasks
            }
        }
    }

    // Displays the screen to modify an existing request
    private void showModifyRequestScreen(UserProfile profile, Request request) {
        getContentPane().removeAll();
        repaint();

        setLayout(new BorderLayout());

        JLabel modifyRequestMessage = new JLabel("Modify Request", SwingConstants.CENTER);
        modifyRequestMessage.setFont(new Font("Arial", Font.BOLD, 20));
        add(modifyRequestMessage, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(6, 2));

        // Pre-filled fields with current request details
        formPanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField(request.title);
        formPanel.add(titleField);

        formPanel.add(new JLabel("Description:"));
        JTextField descriptionField = new JTextField(request.description);
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("Deadline (dd/MM/yyyy):"));
        JTextField deadlineField = new JTextField(request.deadline);
        formPanel.add(deadlineField);

        formPanel.add(new JLabel("Time Range (HH:mm-HH:mm):"));
        JTextField timeRangeField = new JTextField(request.timeRange);
        formPanel.add(timeRangeField);

        formPanel.add(new JLabel("Address:"));
        JTextField addressField = new JTextField(request.address);
        formPanel.add(addressField);

        formPanel.add(new JLabel("Price:"));
        JTextField priceField = new JTextField(request.price);
        formPanel.add(priceField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            // Save modifications to the request
            request.title = titleField.getText().trim();
            request.description = descriptionField.getText().trim();
            request.deadline = deadlineField.getText().trim();
            request.timeRange = timeRangeField.getText().trim();
            request.address = addressField.getText().trim();
            request.price = priceField.getText().trim();
            saveRequests();
            JOptionPane.showMessageDialog(this, "Request modified successfully!");
            showRequestList(profile);
        });

        add(formPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        revalidate();
    }

    // Displays the home screen for a student user type
    private void showStudentHome(UserProfile profile) {
        getContentPane().removeAll();
        repaint();

        setLayout(new BorderLayout());

        JLabel welcomeMessage = new JLabel("Welcome, " + profile.firstName, SwingConstants.CENTER);
        welcomeMessage.setFont(new Font("Arial", Font.BOLD, 20));
        add(welcomeMessage, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));

        // Buttons for student actions
        JButton searchRequestsButton = new JButton("Look for Requests");
        JButton viewTasksButton = new JButton("See My Tasks");
        JButton logoutButton = new JButton("Log Out");

        searchRequestsButton.addActionListener(e -> showSearchRequestsScreen(profile));
        viewTasksButton.addActionListener(e -> showStudentTasksScreen(profile));
        logoutButton.addActionListener(e -> showLoginScreen());

        buttonPanel.add(searchRequestsButton);
        buttonPanel.add(viewTasksButton);
        buttonPanel.add(logoutButton);

        add(buttonPanel, BorderLayout.CENTER);
        revalidate();
    }

    // Displays the screen for new user registration
    private void showRegistrationScreen() {
        getContentPane().removeAll();
        repaint();

        setLayout(new BorderLayout());

        JLabel registrationMessage = new JLabel("Create a New Account", SwingConstants.CENTER);
        registrationMessage.setFont(new Font("Arial", Font.BOLD, 20));
        add(registrationMessage, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(6, 2));

        // Registration form fields
        formPanel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("First Name:"));
        JTextField firstNameField = new JTextField();
        formPanel.add(firstNameField);

        formPanel.add(new JLabel("Last Name:"));
        JTextField lastNameField = new JTextField();
        formPanel.add(lastNameField);

        formPanel.add(new JLabel("Age:"));
        JTextField ageField = new JTextField();
        formPanel.add(ageField);

        formPanel.add(new JLabel("School (for students):"));
        JTextField schoolField = new JTextField();
        formPanel.add(schoolField);

        formPanel.add(new JLabel("Status (student/requester):"));
        JTextField statusField = new JTextField();
        formPanel.add(statusField);

        JButton registerButton = new JButton("Register");
        JButton returnButton = new JButton("Return");

        // Registration button action
        registerButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String ageText = ageField.getText().trim();
            String school = schoolField.getText().trim();
            String status = statusField.getText().trim().toLowerCase();

            // Validate registration fields
            if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || ageText.isEmpty() || status.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!");
            } else if (userProfiles.containsKey(email)) {
                JOptionPane.showMessageDialog(this, "Email already exists!");
            } else {
                int age;
                try {
                    age = Integer.parseInt(ageText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid age!");
                    return;
                }

                // Additional validation based on status
                if (status.equals("student") && age < 16) {
                    JOptionPane.showMessageDialog(this, "Students must be at least 16 years old!");
                } else if (status.equals("student") && school.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Students must provide their school name");
                } else if (status.equals("requester") && age < 18) {
                    JOptionPane.showMessageDialog(this, "Requesters must be at least 18 years old!");
                } else {
                    // Create and save new profile
                    UserProfile newProfile = new UserProfile(email, firstName, lastName, age, school, status);
                    userProfiles.put(email, newProfile);
                    saveUserProfiles();
                    JOptionPane.showMessageDialog(this, "Account created successfully!");
                    showLoginScreen();
                }
            }
        });

        returnButton.addActionListener(e -> showLoginScreen());

        add(formPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(registerButton);
        buttonPanel.add(returnButton);
        add(buttonPanel, BorderLayout.SOUTH);

        revalidate();
    }

    // Displays the screen for searching requests by student users
    private void showSearchRequestsScreen(UserProfile profile) {
        getContentPane().removeAll();
        repaint();

        setLayout(new BorderLayout());

        JLabel searchRequestMessage = new JLabel("Search for Requests", SwingConstants.CENTER);
        searchRequestMessage.setFont(new Font("Arial", Font.BOLD, 20));
        add(searchRequestMessage, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new GridLayout(4, 2));

        // Search filters
        filterPanel.add(new JLabel("Keyword:"));
        JTextField keywordField = new JTextField();
        filterPanel.add(keywordField);

        filterPanel.add(new JLabel("Date (dd/mm/yyyy):"));
        JTextField dateField = new JTextField();
        filterPanel.add(dateField);

        filterPanel.add(new JLabel("Time Range (HH:mm-HH:mm):"));
        JTextField timeRangeField = new JTextField();
        filterPanel.add(timeRangeField);

        filterPanel.add(new JLabel("Price (min):"));
        JTextField priceField = new JTextField();
        filterPanel.add(priceField);

        JButton searchButton = new JButton("Search");
        JButton backButton = new JButton("Back to Home");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(searchButton);
        buttonPanel.add(backButton);

        // Search button action
        searchButton.addActionListener(e -> {
            List<Request> filteredRequests = searchRequests(keywordField.getText().trim(),
                    dateField.getText().trim(), timeRangeField.getText().trim(), priceField.getText().trim());
            showSearchResults(profile, filteredRequests);
        });

        backButton.addActionListener(e -> showStudentHome(profile));

        add(filterPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        revalidate();
    }

    // Searches requests based on keyword, date, time range, and price filters
    private List<Request> searchRequests(String keyword, String date, String timeRange, String minPrice) {
        List<Request> filteredRequests = new ArrayList<>();

        for (List<Request> requests : userRequests.values()) {
            for (Request request : requests) {
                // Skip non-waiting requests
                if (!request.status.equals("in the wait of answer")) continue;

                // Filter by keyword
                if (!keyword.isEmpty() && !request.title.toLowerCase().contains(keyword.toLowerCase()) &&
                        !request.description.toLowerCase().contains(keyword.toLowerCase())) {
                    continue;
                }

                // Filter by date
                if (!date.isEmpty() && !request.deadline.equals(date)) {
                    continue;
                }

                // Filter by time range
                if (!timeRange.isEmpty() && !request.timeRange.equals(timeRange)) {
                    continue;
                }

                // Filter by price
                if (!minPrice.isEmpty()) {
                    try {
                        double minPriceValue = Double.parseDouble(minPrice);
                        double requestPriceValue = Double.parseDouble(request.price);
                        if (requestPriceValue <= minPriceValue) {
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Invalid price format!");
                        return Collections.emptyList();
                    }
                }

                // Add to filtered list if all criteria match
                filteredRequests.add(request);
            }
        }
        return filteredRequests;
    }

    // Displays search results for students
    private void showSearchResults(UserProfile profile, List<Request> filteredRequests) {
        getContentPane().removeAll();
        repaint();

        setLayout(new BorderLayout());

        JLabel searchResultsMessage = new JLabel("Search Results", SwingConstants.CENTER);
        searchResultsMessage.setFont(new Font("Arial", Font.BOLD, 20));
        add(searchResultsMessage, BorderLayout.NORTH);

        JPanel resultsPanel = new JPanel(new GridLayout(filteredRequests.size(), 1));

        // Display each request with the option to accept
        for (Request request : filteredRequests) {
            JPanel requestItem = new JPanel(new GridLayout(1, 3));
            requestItem.add(new JLabel(request.toString()));

            JButton acceptButton = new JButton("Accept");
            acceptButton.setEnabled(canAcceptRequest(profile, request));
            acceptButton.addActionListener(e -> {
                if (canAcceptRequest(profile, request)) {
                    request.status = "accepted";
                    studentTasks.computeIfAbsent(profile.email, k -> new ArrayList<>()).add(request);
                    activeChats.put(request.title, new Chat(request.title, profile.email, request.requesterEmail));
                    saveRequests();
                    saveStudentTasks();
                    saveChats();
                    JOptionPane.showMessageDialog(this, "Request accepted successfully!");
                    showStudentHome(profile);
                } else {
                    JOptionPane.showMessageDialog(this, "You cannot accept more than one request per half-day!");
                }
            });

            requestItem.add(acceptButton);
            resultsPanel.add(requestItem);
        }

        add(resultsPanel, BorderLayout.CENTER);

        JButton backButton = new JButton("Back to Search");
        backButton.addActionListener(e -> showSearchRequestsScreen(profile));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        revalidate();
    }

    // Displays the screen showing tasks assigned to the student
    private void showStudentTasksScreen(UserProfile profile) {
        getContentPane().removeAll();
        repaint();

        setLayout(new BorderLayout());

        JLabel taskListMessage = new JLabel("My Tasks", SwingConstants.CENTER);
        taskListMessage.setFont(new Font("Arial", Font.BOLD, 20));
        add(taskListMessage, BorderLayout.NORTH);

        List<Request> tasks = studentTasks.getOrDefault(profile.email, new ArrayList<>());

        JPanel taskPanel = new JPanel(new GridLayout(tasks.size(), 1));

        // Display each task with chat and decline options
        for (Request request : tasks) {
            JPanel taskItem = new JPanel(new GridLayout(1, 3));
            taskItem.add(new JLabel(request.toString()));

            JButton chatButton = new JButton("Open Chat");
            chatButton.setEnabled(request.status.equals("accepted"));
            chatButton.addActionListener(e -> showChatScreen(request));

            JButton declineButton = new JButton("Decline");
            declineButton.addActionListener(e -> {
                tasks.remove(request);
                request.status = "in the wait of answer";
                saveRequests();
                saveStudentTasks();
                removeChat(request);
                showStudentTasksScreen(profile);
            });

            taskItem.add(chatButton);
            taskItem.add(declineButton);
            taskPanel.add(taskItem);
        }

        add(taskPanel, BorderLayout.CENTER);

        JButton backButton = new JButton("Back to Home");
        backButton.addActionListener(e -> showStudentHome(profile));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        revalidate();
    }

    // Displays the chat screen for a specific request
    private void showChatScreen(Request request) {
        getContentPane().removeAll();
        repaint();

        setLayout(new BorderLayout());

        JLabel chatLabel = new JLabel("Chat for: " + request.title, SwingConstants.CENTER);
        chatLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(chatLabel, BorderLayout.NORTH);

        Chat chat = activeChats.computeIfAbsent(request.title, k -> new Chat(request.title, request.requesterEmail, request.assignedStudentEmail));

        JPanel chatPanel = new JPanel(new BorderLayout());
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        // Load previous chat messages
        for (String message : chat.messages) {
            chatArea.append(message + "\n");
        }

        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("Send");

        // Send message button action
        sendButton.addActionListener(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty() && (currentUserProfile.email.equals(chat.requesterEmail) || currentUserProfile.email.equals(chat.studentEmail))) {
                String fullMessage = currentUserProfile.firstName + ": " + message;
                chat.messages.add(fullMessage);
                chatArea.append(fullMessage + "\n");
                messageField.setText("");
                saveChats();
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        add(chatPanel, BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            if (currentUserProfile.status.equals("requester")) {
                showRequesterHome(currentUserProfile);
            } else {
                showStudentHome(currentUserProfile);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        revalidate();
    }

    // Removes a chat when a request is declined or marked as done
    private void removeChat(Request request) {
        activeChats.remove(request.title);
        saveChats();
    }

    // Helper method to check if two time ranges overlap
    private boolean overlapTimeRanges(String range1, String range2) {
        String[] range1Parts = range1.split("-");
        String[] range2Parts = range2.split("-");
        return !(range1Parts[1].compareTo(range2Parts[0]) <= 0 || range2Parts[1].compareTo(range1Parts[0]) <= 0);
    }

    // Checks if a student can accept a request based on overlapping time ranges
    private boolean canAcceptRequest(UserProfile profile, Request request) {
        List<Request> tasks = studentTasks.getOrDefault(profile.email, new ArrayList<>());
        return tasks.stream().noneMatch(r -> r.deadline.equals(request.deadline) &&
                overlapTimeRanges(r.timeRange, request.timeRange));
    }

    // Persistence method to save active chats to file
    private void saveChats() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("chats.ser"))) {
            oos.writeObject(activeChats);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loads active chats from file
    @SuppressWarnings("unchecked")
    private void loadChats() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("chats.ser"))) {
            activeChats = (Map<String, Chat>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Saves user profiles to file
    private void saveUserProfiles() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("userProfiles.ser"))) {
            oos.writeObject(userProfiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loads user profiles from file
    @SuppressWarnings("unchecked")
    private void loadUserProfiles() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("userProfiles.ser"))) {
            userProfiles = (Map<String, UserProfile>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Saves user requests to file
    private void saveRequests() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("userRequests.ser"))) {
            oos.writeObject(userRequests);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loads user requests from file
    @SuppressWarnings("unchecked")
    private void loadRequests() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("userRequests.ser"))) {
            userRequests = (Map<String, List<Request>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Saves student tasks to file
    private void saveStudentTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("studentTasks.ser"))) {
            oos.writeObject(studentTasks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loads student tasks from file
    @SuppressWarnings("unchecked")
    private void loadStudentTasks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("studentTasks.ser"))) {
            studentTasks = (Map<String, List<Request>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Main Method: Entry point of the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(NudgeApp::new);
    }

    // Class representing a user profile
    static class UserProfile implements Serializable {
        String email, firstName, lastName, school, status;
        int age;

        public UserProfile(String email, String firstName, String lastName, int age, String school, String status) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.school = school;
            this.status = status;
        }
    }

    // Class representing a request made by a user
    static class Request implements Serializable {
        String title, description, deadline, timeRange, address, price, status;
        String requesterEmail;
        String assignedStudentEmail;

        public Request(String title, String description, String deadline, String timeRange, String address, String price) {
            this.title = title;
            this.description = description;
            this.deadline = deadline;
            this.timeRange = timeRange;
            this.address = address;
            this.price = price;
            this.status = "in the wait of answer";
        }

        @Override
        public String toString() {
            return String.format("Title: %s, Description: %s, Deadline: %s, Time Range: %s, Address: %s, Price: %s, Status: %s",
                    title, description, deadline, timeRange, address, price, status);
        }
    }

    // Class representing a chat for a request
    static class Chat implements Serializable {
        String requestTitle;
        String requesterEmail;
        String studentEmail;
        List<String> messages;

        public Chat(String requestTitle, String studentEmail, String requesterEmail) {
            this.requestTitle = requestTitle;
            this.studentEmail = studentEmail;
            this.requesterEmail = requesterEmail;
            this.messages = new ArrayList<>();
        }
    }
}
