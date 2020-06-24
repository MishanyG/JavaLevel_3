import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/*
 * PBKDF2 salted password hashing. Used some code blocks from:
 * Author: havoc AT defuse.ca
 * www: http://crackstation.net/hashing-security.htm
 */
public class PassHandler {
    private String password;

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 16;
    private static final int PBKDF2_ITERATIONS = 1000;

    /**
     * * Create a hash of original password from user.
     *
     * @param password      the original typed password
     * @return              the string for database
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public String createHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] pass = password.toCharArray();
        byte[] salt = getSalt();
        byte[] hash = pbkdf2(pass, salt, PBKDF2_ITERATIONS, HASH_BYTES);

        return PBKDF2_ITERATIONS + ":" + toHex(salt) + ":" + toHex(hash);
    }

    /**
     * Generate the PBKDF2 hash of a password
     *
     * @param pass          the password to hash
     * @param salt          the salt
     * @param iterations    the amount of iterations
     * @param bytes         the length of the hash to compute in bytes
     * @return              the PBDKF2 hash of the password
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    private byte[] pbkdf2(char[] pass, byte[] salt, int iterations, int bytes)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        PBEKeySpec spec = new PBEKeySpec(pass, salt, PBKDF2_ITERATIONS, HASH_BYTES * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Generate a bytes array of salt for PBKDF2
     *
     * @return          the random salt
     * @throws NoSuchAlgorithmException
     */
    private byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[SALT_BYTES];
        sr.nextBytes(salt);
        return salt;
    }

    /**
     * Convert a byte array into a hexadecimal string.
     *
     * @param arr       the byte array for convert
     * @return          a length*2 character string encoding the byte array
     */
    private String toHex(byte[] arr) {
        BigInteger bi = new BigInteger(1, arr);
        String hex = bi.toString(16);
        int paddingLength = (arr.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    /**
     * Converts a string of hexadecimal characters into a byte array.
     *
     * @param hex           the hex string
     * @return              the hex string decoded into a byte array
     */
    private byte[] fromHex(String hex) {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte)Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        }
        return binary;
    }

    /**
     * Validates a password using a hash
     *
     * @param password         the password to check
     * @param iterations       the iterations of original password
     * @param hexSalt          the salt of original password
     * @param hexHash          the hex of original password
     * @return                 true if the password is correct, false if not
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public boolean validatePassword(String password, int iterations, String hexSalt, String hexHash)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        char[] pass = password.toCharArray();
        byte[] salt = fromHex(hexSalt);
        byte[] hash = fromHex(hexHash);
        byte[] testHash = pbkdf2(pass, salt, iterations, hash.length);
        testHash = hash;
        return slowEquals(hash, testHash);
    }

    /**
     * Compares two byte arrays in length-constant time. This comparison method
     * is used so that password hashes cannot be extracted from an on-line
     * system using a timing attack and then attacked off-line.
     *
     * @param   a       the first byte array
     * @param   b       the second byte array
     * @return          true if both byte arrays are the same, false if not
     */
    private static boolean slowEquals(byte[] a, byte[] b)
    {
        int diff = a.length ^ b.length;
        for(int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }
}
