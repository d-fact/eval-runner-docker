#pragma once

#include "PresenceCondition.h"
#include "CompiledRelation.h"
#include <map>
namespace souffle {

namespace ram {

template <typename Setup, unsigned arity, typename... Indices>
class LiftedRelation: public std::map<PresenceCondition*, Relation<Setup, arity, Indices...>*> {
    using RelType = Relation<Setup, arity, Indices...>;
private:

public:
    
}; // LiftedRelation

} // ram
} // souffle