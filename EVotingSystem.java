import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

record Candidate(int id, String name, int votes) {
}

class VotingDAO {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/voting_system?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Ekta@1204";

    public VotingDAO() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");  // Ensure MySQL JDBC Driver is loaded
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found. Exiting.");
            System.exit(1);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public List<Candidate> getAllCandidates() {
        List<Candidate> candidates = new ArrayList<>();
        String sql = "SELECT * FROM candidates ORDER BY id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                candidates.add(new Candidate(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("votes")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading candidates: " + e.getMessage());
        }
        return candidates;
    }

    public boolean hasVoted(String voterId) {
        String sql = "SELECT * FROM votes WHERE voter_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, voterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();  // true if already voted
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error checking vote history: " + e.getMessage());
            return true;  // Return true to block voting if error occurs
        }
    }

    public boolean vote(String voterId, int candidateId) {
        if (hasVoted(voterId)) {
            return false; // Already voted
        }

        String updateVotesSQL = "UPDATE candidates SET votes = votes + 1 WHERE id = ?";
        String insertVoteSQL = "INSERT INTO votes (voter_id, candidate_id) VALUES (?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement updateVotes = conn.prepareStatement(updateVotesSQL);
                 PreparedStatement insertVote = conn.prepareStatement(insertVoteSQL)) {

                updateVotes.setInt(1, candidateId);
                updateVotes.executeUpdate();

                insertVote.setString(1, voterId);
                insertVote.setInt(2, candidateId);
                insertVote.executeUpdate();

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error casting vote: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            return false;
        }
    }
}

public class EVotingSystem extends JFrame {

    private final VotingDAO dao;
    private JTextField voterIdField;
    private JPanel candidatePanel;
    private ButtonGroup candidateGroup;
    private JTextArea resultArea;

    public EVotingSystem() {
        dao = new VotingDAO();

        setTitle("Electronic Voting System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(550, 600);
        setLocationRelativeTo(null);

        initComponents();
        loadCandidates();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel: Voter ID + Buttons
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel voterLabel = new JLabel("Enter Voter ID:");
        voterIdField = new JTextField(20);

        JButton voteButton = new JButton("Cast Vote");
        JButton resultsButton = new JButton("Show Results");
        JButton candidatesButton = new JButton("Show Candidates");

        voteButton.addActionListener(e -> castVote());
        resultsButton.addActionListener(e -> showResults());
        candidatesButton.addActionListener(e -> showCandidatesDialog());

        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        topPanel.add(voterLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        topPanel.add(voterIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        topPanel.add(voteButton, gbc);

        gbc.gridx = 1;
        topPanel.add(resultsButton, gbc);

        gbc.gridx = 2;
        topPanel.add(candidatesButton, gbc);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Candidate selection panel (center)
        candidatePanel = new JPanel();
        candidatePanel.setBorder(BorderFactory.createTitledBorder("Select a Candidate"));
        candidatePanel.setLayout(new BoxLayout(candidatePanel, BoxLayout.Y_AXIS));
        JScrollPane candidateScroll = new JScrollPane(candidatePanel);
        candidateScroll.setPreferredSize(new Dimension(500, 300));
        mainPanel.add(candidateScroll, BorderLayout.CENTER);

        // Results area at bottom
        resultArea = new JTextArea(8, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Results"));
        mainPanel.add(resultScroll, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadCandidates() {
        candidatePanel.removeAll();
        candidateGroup = new ButtonGroup();

        List<Candidate> candidates = dao.getAllCandidates();
        if (candidates.isEmpty()) {
            JLabel noCandidatesLabel = new JLabel("No candidates found in database.");
            noCandidatesLabel.setForeground(Color.RED);
            candidatePanel.add(noCandidatesLabel);
        } else {
            for (Candidate c : candidates) {
                JRadioButton rb = new JRadioButton(c.name());
                rb.setActionCommand(String.valueOf(c.id()));
                candidateGroup.add(rb);
                candidatePanel.add(rb);
            }
        }

        candidatePanel.revalidate();
        candidatePanel.repaint();
    }

    private void castVote() {
        String voterId = voterIdField.getText().trim();
        if (voterId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your Voter ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (candidateGroup.getSelection() == null) {
            JOptionPane.showMessageDialog(this, "Please select a candidate.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int candidateId = Integer.parseInt(candidateGroup.getSelection().getActionCommand());

        boolean success = dao.vote(voterId, candidateId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Vote cast successfully. Thank you!", "Success", JOptionPane.INFORMATION_MESSAGE);
            voterIdField.setText("");
            candidateGroup.clearSelection();
            showResults();  // Update results after vote
        } else {
            JOptionPane.showMessageDialog(this, "You have already voted or an error occurred.", "Vote Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showResults() {
        List<Candidate> candidates = dao.getAllCandidates();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s  %s%n", "Candidate", "Votes"));
        sb.append("-----------------------------------\n");
        for (Candidate c : candidates) {
            sb.append(String.format("%-20s  %d%n", c.name(), c.votes()));
        }
        resultArea.setText(sb.toString());
    }

    private void showCandidatesDialog() {
        List<Candidate> candidates = dao.getAllCandidates();
        if (candidates.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No candidates found.", "Candidates", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-5s %-20s%n", "ID", "Name"));
        sb.append("-------------------------\n");
        for (Candidate c : candidates) {
            sb.append(String.format("%-5d %-20s%n", c.id(), c.name()));
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Candidate Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EVotingSystem evs = new EVotingSystem();
            evs.setVisible(true);
        });
    }
}
