package model;

/**
 * Cặp khóa RSA: gộp PublicKey + PrivateKey.
 * AlgorithmRSA.generateKey() trả về object này.
 */
public class KeyPair {

    public final PublicKey  publicKey;
    public final PrivateKey privateKey;

    public KeyPair(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey  = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public String toString() {
        return "KeyPair{\n  " + publicKey + "\n  " + privateKey + "\n}";
    }
}

