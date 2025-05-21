public class TestDriver {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver found!");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver NOT found!");
            e.printStackTrace();
        }
    }
}
