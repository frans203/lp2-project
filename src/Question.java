public class Question {
    String question;
    String[] options;

    int indexCorrectAnswer;
    public Question(String question, String[] options) {
        this.question = question;
        this.options = options;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String[] getOptions() {
        return options;
    }

    public String getOption(int i) { return  options[i]; }

    public void setIndexCorrectAnswer(int indexCorrectAnswer) {
        this.indexCorrectAnswer = indexCorrectAnswer;
    }

    public int getIndexCorrectAnswer() {
        return indexCorrectAnswer;
    }
}
