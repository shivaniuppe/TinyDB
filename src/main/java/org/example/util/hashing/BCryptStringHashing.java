package org.example.util.hashing;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptStringHashing implements StringHashing {

    @Override
    public String hash(String input) {
        return BCrypt.hashpw(input, BCrypt.gensalt());
    }

    @Override
    public boolean checkHash(String input, String hash) {
        return BCrypt.checkpw(input, hash);
    }
}
