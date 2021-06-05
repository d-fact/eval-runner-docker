#pragma once

#include "PresenceCondition.h"

namespace souffle {

struct RamRecord {
    const RamDomain* field; 
    const PresenceCondition* pc;
    bool  owned;
#ifdef DEBUG
    std::size_t size;
#endif //DEBUG

    RamRecord(std::size_t s, const RamDomain* f, const PresenceCondition* _pc = PresenceCondition::makeTrue(), bool _owned = false) :
        field(f)
        , pc(_pc)
        , owned(_owned)
#ifdef DEBUG
        , size(s)
#endif
    {
        assert(_pc);
#ifndef NDEBUG
        _pc->validate();
#endif
    }

    ~RamRecord() {
        //if (owned) {
        //    delete[] field;
        //}
    }

    RamRecord(const RamRecord& other) : 
        field(other.field),
        pc(other.pc),
        owned(false)
#ifdef DEBUG
        , size(other.size)
#endif
        {}

    const RamDomain& operator[](std::size_t index) const {
#ifdef DEBUG
        assert(index < size);
#endif
        return field[index];
    }
}; //RamRecord

}