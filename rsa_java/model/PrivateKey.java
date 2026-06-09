package model;

import java.math.BigInteger;

/**
 * Khóa riêng tư RSA: (N, D)
 * Giữ bí mật — chỉ người ký mới được có.
 */
public class PrivateKey {

    private final BigInteger n; // modulus N = P × Q
    private final BigInteger d; // số mũ riêng tư D = E⁻¹ mod φ

    public PrivateKey(BigInteger n, BigInteger d) {
        if (n == null || d == null) {
            throw new IllegalArgumentException("Các thành phần của PrivateKey không được null.");
        }
        this.n = n;
        this.d = d;
    }

    public BigInteger getN() { return n; }
    public BigInteger getD() { return d; }

    /** Chuỗi lưu ra file .priv */
    public String toFileString() {
        return "N=" + n + "\nD=" + d;
    }

    /** Đọc từ nội dung file .priv */
    public static PrivateKey fromFileString(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Nội dung file khóa bí mật rỗng.");
        }

        BigInteger n = null, d = null;
        for (String line : content.split("\\r?\\n")) {
            line = line.trim();
            if (line.startsWith("N=")) n = new BigInteger(line.substring(2).trim());
            if (line.startsWith("D=")) d = new BigInteger(line.substring(2).trim());
        }
        if (n == null || d == null) {
            throw new IllegalArgumentException("Định dạng file .priv không hợp lệ.");
        }
        return new PrivateKey(n, d);
    }

    @Override
    public String toString() {
        String nStr = n.toString();
        String dStr = d.toString();
        return "PrivateKey{N=" + nStr.substring(0, Math.min(20, nStr.length()))
                + "..., D=" + dStr.substring(0, Math.min(20, dStr.length())) + "...}";
    }
}
