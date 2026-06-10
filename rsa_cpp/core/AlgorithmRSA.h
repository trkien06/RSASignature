#pragma once
#include <string>
#include "InfInt.h"

namespace core {
    class AlgorithmRSA {
    public:
        static InfInt gcd(InfInt a, InfInt b);
        static bool isPrime(InfInt n);
        static InfInt modInverse(InfInt e, InfInt phi);
        static InfInt powerMod(InfInt base, InfInt exp, InfInt mod);
        static std::wstring CalculateSHA256(const std::wstring& filePath);
        static InfInt HashToInt(const std::wstring& hashHex);
    };
}

