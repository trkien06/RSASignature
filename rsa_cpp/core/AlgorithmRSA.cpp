            for (i = 0; i < 64; ++i) {
                t1 = h + ep1(e) + ch(e, f, g) + k[i] + m[i];
                t2 = ep0(a) + maj(a, b, c);
                h = g; g = f; f = e; e = d + t1;
                d = c; c = b; b = a; a = t1 + t2;
            }
            state[0] += a; state[1] += b; state[2] += c; state[3] += d;
            state[4] += e; state[5] += f; state[6] += g; state[7] += h;
        }
    public:
        SHA256_Hash() {
            state[0] = 0x6a09e667; state[1] = 0xbb67ae85; state[2] = 0x3c6ef372; state[3] = 0xa54ff53a;
            state[4] = 0x510e527f; state[5] = 0x9b05688c; state[6] = 0x1f83d9ab; state[7] = 0x5be0cd19;
            datalen = 0; bitlen = 0;
            uint32_t k_init[64] = {
                0x428a2f98,0x71374491,0xb5c0fbcf,0xe9b5dba5,0x3956c25b,0x59f111f1,0x923f82a4,0xab1c5ed5,
                0xd807aa98,0x12835b01,0x243185be,0x550c7dc3,0x72be5d74,0x80deb1fe,0x9bdc06a7,0xc19bf174,
                0xe49b69c1,0xefbe4786,0x0fc19dc6,0x240ca1cc,0x2de92c6f,0x4a7484aa,0x5cb0a9dc,0x76f988da,
                0x983e5152,0xa831c66d,0xb00327c8,0xbf597fc7,0xc6e00bf3,0xd5a79147,0x06ca6351,0x14292967,
                0x27b70a85,0x2e1b2138,0x4d2c6dfc,0x53380d13,0x650a7354,0x766a0abb,0x81c2c92e,0x92722c85,
                0xa2bfe8a1,0xa81a664b,0xc24b8b70,0xc76c51a3,0xd192e819,0xd6990624,0xf40e3585,0x106aa070,
                0x19a4c116,0x1e376c08,0x2748774c,0x34b0bcb5,0x391c0cb3,0x4ed8aa4a,0x5b9cca4f,0x682e6ff3,
                0x748f82ee,0x78a5636f,0x84c87814,0x8cc70208,0x90befffa,0xa4506ceb,0xbef9a3f7,0xc67178f2
            };
            for(int i=0; i<64; i++) k[i] = k_init[i];
        }
        void update(const uint8_t* data_in, size_t len) {
            for (size_t i = 0; i < len; ++i) {
                data[datalen++] = data_in[i];
                if (datalen == 64) { transform(); bitlen += 512; datalen = 0; }
            }
        }
        std::string finalize() {
            uint32_t i = datalen;
            if (datalen < 56) {
                data[i++] = 0x80;
                while (i < 56) data[i++] = 0x00;
            } else {
                data[i++] = 0x80;
                while (i < 64) data[i++] = 0x00;
                transform();
                for(i=0; i<56; i++) data[i] = 0x00;
            }
            bitlen += datalen * 8;
            data[63] = bitlen; data[62] = bitlen >> 8; data[61] = bitlen >> 16; data[60] = bitlen >> 24;
            data[59] = bitlen >> 32; data[58] = bitlen >> 40; data[57] = bitlen >> 48; data[56] = bitlen >> 56;
            transform();
            char buf[65];
            snprintf(buf, sizeof(buf), "%08x%08x%08x%08x%08x%08x%08x%08x",
                state[0], state[1], state[2], state[3], state[4], state[5], state[6], state[7]);
            return std::string(buf);
        }
    };
    // =========================================================================

    InfInt AlgorithmRSA::gcd(InfInt a, InfInt b) {
        return boost::multiprecision::gcd(a, b);
    }

    bool AlgorithmRSA::isPrime(InfInt n) {
        if (n <= 1) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0) return false;
        return boost::multiprecision::miller_rabin_test(n, 25);
    }

    InfInt AlgorithmRSA::modInverse(InfInt e, InfInt phi) {
        InfInt t = 0, newt = 1, r = phi, newr = e;
        while (newr != 0) {
            InfInt q = r / newr;
            InfInt temp = t - q * newt; t = newt; newt = temp;
            temp = r - q * newr; r = newr; newr = temp;
        }
        return (t < 0) ? t + phi : t;
    }

    InfInt AlgorithmRSA::powerMod(InfInt base, InfInt exp, InfInt mod) {
        return boost::multiprecision::powm(base, exp, mod);
    }

    std::wstring AlgorithmRSA::CalculateSHA256(const std::wstring& filePath) {
        // Mở file mức độ thấp nhất: Xuyên khóa mọi ứng dụng khác đang mở file
        HANDLE hFile = CreateFileW(filePath.c_str(), GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
        if (hFile == INVALID_HANDLE_VALUE) {
            DWORD err = GetLastError();
            return L"LỖI MỞ FILE (Mã lỗi Windows: " + std::to_wstring(err) + L")";
        }

        SHA256_Hash hasher;
        uint8_t buffer[8192];
        DWORD bytesRead = 0;

        // Tự động đọc và băm tệp không thông qua hàm Crypto của Windows
        while (ReadFile(hFile, buffer, sizeof(buffer), &bytesRead, NULL) && bytesRead > 0) {
            hasher.update(buffer, bytesRead);
        }
        CloseHandle(hFile);

        std::string hexHash = hasher.finalize();
        return std::wstring(hexHash.begin(), hexHash.end());
    }

    InfInt AlgorithmRSA::HashToInt(const std::wstring& hashHex) {
        if (hashHex.empty() || hashHex.find(L"LỖI") != std::wstring::npos) {
            return InfInt("1");
        }

        try {
            // Không cắt 15 ký tự nữa, lấy TOÀN BỘ mã Hash
            std::string narrowSub(hashHex.begin(), hashHex.end());

            // Thêm tiền tố 0x để báo cho Boost biết đây là số HEX
            if (narrowSub.substr(0, 2) != "0x" && narrowSub.substr(0, 2) != "0X") {
                narrowSub = "0x" + narrowSub;
            }

            // Boost giờ đây có thể nuốt trọn số HEX 64 ký tự giống hệt Java
            return InfInt(narrowSub);
        }
        catch (...) {
            return InfInt("1");
        }
    }
}
