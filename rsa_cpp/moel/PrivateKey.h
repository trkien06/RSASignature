#pragma once
#include "../core/InfInt.h"

namespace model {
    class PrivateKey {
    public:
        InfInt n;
        InfInt d;

        PrivateKey(InfInt n_val = 0, InfInt d_val = 0) : n(n_val), d(d_val) {}
    };
}
