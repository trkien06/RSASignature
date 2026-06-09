package model;

import java.math.BigInteger;

/**
 * Khóa công khai RSA: (N, E)
 * Chia sẻ tự do cho bất kỳ ai muốn xác minh chữ ký.
 */
public class PublicKey {

    private final BigInteger n; // modulus N = P × Q
    private final BigInteger e; // số mũ công khai (thường = 65537)

    public PublicKey(BigInteger n, BigInteger e) {
        if (n == null || e == null) {
            throw new IllegalArgumentException("Các thành phần của PublicKey không được null.");
        }
        this.n = n;
        this.e = e;
    }

    public BigInteger getN() { return n; }
    public BigInteger getE() { return e; }

    /** Chuỗi lưu ra file .pub (mỗi tham số 1 dòng) */
    public String toFileString() {
        return "N=" + n + "\nE=" + e;
    }

    /** Đọc từ nội dung file .pub */
    public static PublicKey fromFileString(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Nội dung file khóa công khai rỗng.");
        }

        BigInteger n = null, e = null;
        for (String line : content.split("\\r?\\n")) {
            line = line.trim();
            if (line.startsWith("N=")) n = new BigInteger(line.substring(2).trim());
            if (line.startsWith("E=")) e = new BigInteger(line.substring(2).trim());
        }
        if (n == null || e == null) {
            throw new IllegalArgumentException("Định dạng file .pub không hợp lệ.");
        }
        return new PublicKey(n, e);
    }

    @Override
    public String toString() {
        String nStr = n.toString();
        return "PublicKey{N=" + nStr.substring(0, Math.min(20, nStr.length())) + "..., E=" + e + "}";
    }
}
