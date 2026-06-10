#include <iostream>
#include <string>
#include <windows.h>

// Gọi các package cần test
#include "../core/AlgorithmRSA.h"
#include "../model/KeyPair.h"

using namespace std;

int main() {
    // Ép Console hiển thị tiếng Việt
    SetConsoleOutputCP(CP_UTF8);

    cout << "==========================================\n";
    cout << "      KỊCH BẢN KIỂM THỬ RSA VỚI SỐ LỚN    \n";
    cout << "==========================================\n\n";

    // ── TEST 1: Mô phỏng tạo khóa (Dùng InfInt thay cho long long)
    cout << "=== TEST 1: Tinh toan khoa (P=61, Q=53) ===" << endl;

    // Khởi tạo bằng string để tránh mất dữ liệu
    InfInt p("61");
    InfInt q("53");

    InfInt n = p * q;
    InfInt phi = (p - 1) * (q - 1);

    InfInt e("3");
    while (e < phi) {
        if (core::AlgorithmRSA::gcd(e, phi) == 1) break;
        e += 2;
    }

    InfInt d = core::AlgorithmRSA::modInverse(e, phi);

    model::PublicKey pubKey(n, e);
    model::PrivateKey privKey(n, d);
    model::KeyPair kp(pubKey, privKey);

    cout << "N = " << kp.publicKey.n << endl;
    cout << "E = " << kp.publicKey.e << endl;
    cout << "D = " << kp.privateKey.d << endl;

    // ── TEST 2: Ký số
    cout << "\n=== TEST 2: Ky so (Chu ky = Hash^D mod N) ===" << endl;
    InfInt fakeHash("123456789"); // Dùng string để khởi tạo InfInt
    cout << "Ma bam nguyen ban (Hash) : " << fakeHash << endl;

    InfInt signature = core::AlgorithmRSA::powerMod(fakeHash, kp.privateKey.d, kp.privateKey.n);
    cout << "Chu ky so tao ra (Sig)   : " << signature << endl;

    // ── TEST 3: Xác minh chữ ký
    cout << "\n=== TEST 3: Xac minh (Giai ma = Sig^E mod N) ===" << endl;
    InfInt decryptedHash = core::AlgorithmRSA::powerMod(signature, kp.publicKey.e, kp.publicKey.n);
    cout << "Gia tri sau khi giai ma  : " << decryptedHash << endl;

    if (decryptedHash == fakeHash) {
        cout << "=> KET LUAN: Chu ky HOP LE! (Khop voi ma bam goc)\n";
    } else {
        cout << "=> KET LUAN: Chu ky KHONG HOP LE!\n";
    }

    cout << "\n==========================================\n";
    system("pause");
    return 0;
}

