import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Autocorrect {
    // declare structures for later use
    private final String[] words;
    private final int threshold;
    private int gramSize = 2;
    private Map<String, Set<String>> gramMap;

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    public Autocorrect(String[] words, int threshold) {
        this.words = words;
        this.threshold = threshold;
        Arrays.sort(words); // only sort once
        gramMap = buildGramMap(this.words, 2);
    }
    // Autocorrect instance with varying ngram size
    public Autocorrect(String[] words, int threshold, int gramSize) {
        this.words = words;
        this.threshold = threshold;
        this.gramSize = gramSize;
        Arrays.sort(words); // only sort once
        gramMap = buildGramMap(this.words, this.gramSize);
    }

    // creates set of words with each gram as the index
    public Map<String, Set<String>> buildGramMap(String[] words, int gramSize) {
        Map<String, Set<String>> map = new HashMap<>();
        for (String word : words) {
            if (word.length() < gramSize) continue;
            for (int i = 0; i < word.length()-gramSize; i++) {
                String gram = word.substring(i, i+gramSize);
                map.putIfAbsent(gram, new HashSet<>());
                map.get(gram).add(word);
            }
        }
        return map;
    }


    private Set<String> getValid(String typed) {
        Set<String> valid = new HashSet<>();
        // 1. no word
        if (typed.isEmpty()) {
            for (String word : words) {
                if (word.length() <= threshold) {
                    valid.add(word);
                }
            }
            return valid;
        }
        // 2. other
        for (int i = 0; i < typed.length()-gramSize+1; i++) {
            String bigram = typed.substring(i, i+gramSize);
            if (gramMap.containsKey(bigram)) {
                valid.addAll(gramMap.get(bigram));
            }
        }
        return valid;
    }

    /**
     * Calculate edit distance between string m and n
     */
    private int editDist(String m, String n) {
        int m_length = m.length(), n_length = n.length();
        int[][] dp = new int[m_length+1][n_length+1];
        // base case
        for (int i = 0; i <= m_length; i++) dp[i][0] = i;
        for (int i = 0; i <= n_length; i++) dp[0][i] = i;

        for (int i = 1; i <= m_length; i++) {
            for (int j = 1; j <= n_length; j++) {
                // match
                if (m.charAt(i-1) == n.charAt(j-1)) dp[i][j] = dp[i-1][j-1];
                else dp[i][j] = 1 + Math.min(dp[i-1][j-1], Math.min(dp[i-1][j], dp[i][j-1])); // sub, del, insert
            }
        }
        return dp[m_length][n_length];
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        // initialize variables and data structures
        int t = typed.length();
        ArrayList<Pair> matches = new ArrayList<>(); // words with an edit distance within threshold
        HashSet<String> valid = (HashSet<String>) getValid(typed); // words in dictionary that share a bigram with typed words
        HashSet<String> vis = new HashSet<>();

        // valid (bigram-containing) words
        for (String word : valid) {
            vis.add(word);                                          // do not check word again later
            if (Math.abs(word.length()-t) > threshold) continue;    // word is too long or short within edit distance
            // check if edit distance is less than threshold for valid words
            int dist = editDist(typed, word);
            if (dist <= threshold) matches.add(new Pair(word, dist));
        }
        // all other words
        for (String word : words) {
            // skips words that have been checked or too long or short
            if (vis.contains(word) || Math.abs(word.length()-t) > threshold) continue;
            // check edit distance and threshold
            int dist = editDist(typed, word);
            if (dist <= threshold) matches.add(new Pair(word, dist));
        }

        // sort by distance first and then alphabetically
        matches.sort(Comparator.comparingInt(Pair::getDist).thenComparing(Pair::getWord));
        // convert to array before returning potential matches
        String[] arr = new String[matches.size()];
        for (int i = 0; i < matches.size(); i++) {
            arr[i] = matches.get(i).getWord();
        }
        return arr;
    }


    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries directory.
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary)  {
        try {
            String line;
            BufferedReader dictReader = new BufferedReader(new FileReader("dictionaries/" + dictionary + ".txt"));
            line = dictReader.readLine();

            // Update instance variables with test data
            int n = Integer.parseInt(line);
            String[] words = new String[n];

            for (int i = 0; i < n; i++) {
                line = dictReader.readLine();
                words[i] = line;
            }
            return words;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}