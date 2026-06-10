#pragma once
#include "PublicKey.h"
#include "PrivateKey.h"

namespace model {
    class KeyPair {
    public:
        PublicKey publicKey;
        PrivateKey privateKey;

        KeyPair(PublicKey pub, PrivateKey priv) : publicKey(pub), privateKey(priv) {}
        KeyPair() {}
    };
}

