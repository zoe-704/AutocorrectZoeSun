/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 * @author Zach Blick
 * @author Zoe Sun
 */

class Pair {
    String word;
    int dist;

    public Pair(String word, int dist) {
        this.word = word;
        this.dist = dist;
    }

    public int getDist() {
        return dist;
    }

    public String getWord() {
        return word;
    }

    public String toString() {
        return word + " " + dist;
    }


}
