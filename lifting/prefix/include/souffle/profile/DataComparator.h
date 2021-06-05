/*
 * Souffle - A Datalog Compiler
 * Copyright (c) 2016, The Souffle Developers. All rights reserved
 * Licensed under the Universal Permissive License v 1.0 as shown at:
 * - https://opensource.org/licenses/UPL
 * - <souffle root>/licenses/SOUFFLE-UPL.txt
 */

#pragma once

#include "CellInterface.h"
#include "Row.h"

#include <cmath>
#include <memory>
#include <vector>

namespace souffle {
namespace profile {

/*
 * Data comparison functions for sorting tables
 *
 * Will sort the values of only one column, in descending order
 *
 */
class DataComparator {
public:
    /** Sort by total time. */
    static bool TIME(const std::shared_ptr<Row>& a, const std::shared_ptr<Row>& b) {
        return a->cells[0]->getDoubVal() > b->cells[0]->getDoubVal();
    }

    /** Sort by non-recursive time. */
    static bool NR_T(const std::shared_ptr<Row>& a, const std::shared_ptr<Row>& b) {
        return a->cells[1]->getDoubVal() > b->cells[1]->getDoubVal();
    }

    /** Sort by recursive time. */
    static bool R_T(const std::shared_ptr<Row>& a, const std::shared_ptr<Row>& b) {
        return a->cells[2]->getDoubVal() > b->cells[2]->getDoubVal();
    }

    /** Sort by copy time. */
    static bool C_T(const std::shared_ptr<Row>& a, const std::shared_ptr<Row>& b) {
        return a->cells[3]->getDoubVal() > b->cells[3]->getDoubVal();
    }

    /** Sort by tuple count. */
    static bool TUP(const std::shared_ptr<Row>& a, const std::shared_ptr<Row>& b) {
        return b->cells[4]->getLongVal() < a->cells[4]->getLongVal();
    }

    /** Sort by name. */
    static bool NAME(const std::shared_ptr<Row>& a, const std::shared_ptr<Row>& b) {
        return b->cells[5]->getStringVal() > a->cells[5]->getStringVal();
    }

    /** Sort by ID. */
    static bool ID(const std::shared_ptr<Row>& a, const std::shared_ptr<Row>& b) {
        // TODO: compare the actual ID values
        return b->cells[6]->getStringVal() > a->cells[6]->getStringVal();
    }
};

}  // namespace profile
}  // namespace souffle
