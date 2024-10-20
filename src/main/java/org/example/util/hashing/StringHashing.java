package org.example.util.hashing;

public interface StringHashing {

    /**
     * Hash the provided input string
     * @param input Input string
     * @return String representing hashed value of input string
     */
    String hash(String input);

    /**
     * Checks if the provided hash matches the input or not
     * @param input Input string
     * @param hash Hashed value
     * @return True if input string matches the Hash, otherwise false
     */
    boolean checkHash(String input, String hash);
}
