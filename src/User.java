public class User {
    int points;
    String username;

    public User(String username) {
        this.username = username;
        this.points = 0;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints() {
        this.points += 1;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "points=" + points +
                ", username='" + username + '\'' +
                '}';
    }
}
