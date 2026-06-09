package model;

/**
 * Cặp khóa RSA: gộp PublicKey + PrivateKey.
 */
public class KeyPair {

    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public KeyPair(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() { return publicKey; }
    public PrivateKey getPrivateKey() { return privateKey; }

    @Override
    public String toString() {
        return "KeyPair{\n  " + publicKey + "\n  " + privateKey + "\n}";
    }
}
