import java.util.Random;

public class User {
    private int points;
    private String username;

    private boolean isReady;

    private int id;

    public User(String username) {
        this.username = username;
        this.points = 0;
        this.isReady = false;
        Random random = new Random();
        this.id = random.nextInt();
    }

    public int getPoints() {
        return points;
    }

    public void setPoints() {
        this.points += 1;
    }

    public void restartPoints() {
        this.points = 0;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean getReady() { return isReady; }

    public void setReady(boolean ready) { isReady = ready; }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "User{" +
                "points=" + points +
                ", username='" + username + '\'' +
                '}';
    }
}
