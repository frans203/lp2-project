public class GameInfo {
    private int currentQuestionNumber = 0;
    private boolean gameEnded = false;

    public int getCurrentQuestionNumber() {
        return currentQuestionNumber;
    }

    public void setCurrentQuestionNumber(int currentQuestionNumber) {
        this.currentQuestionNumber = currentQuestionNumber;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public void setGameEnded() {
        this.gameEnded = !this.gameEnded;
    }

    public void incrementQuestionNumber() {
        this.currentQuestionNumber += this.currentQuestionNumber;

    }

    @Override
    public String toString() {
        return "GameInfo{" +
                "currentQuestionNumber=" + currentQuestionNumber +
                ", gameEnded=" + gameEnded +
                '}';
    }
}
