#pragma once
#include "../core/InfInt.h"

namespace model {
    class PublicKey {
    public:
        InfInt n;
        InfInt e;

        PublicKey(InfInt n_val = 0, InfInt e_val = 0) : n(n_val), e(e_val) {}
    };
}

