public class User {
    int points;
    String username;

    boolean isReady;

    public User(String username) {
        this.username = username;
        this.points = 0;
        this.isReady = false;
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

    public boolean getReady() { return isReady; }

    public void setReady(boolean ready) { isReady = ready; }

    @Override
    public String toString() {
        return "User{" +
                "points=" + points +
                ", username='" + username + '\'' +
                '}';
    }
}
