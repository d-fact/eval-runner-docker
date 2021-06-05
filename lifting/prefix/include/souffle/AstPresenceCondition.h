#pragma once

#include "AstNode.h"
#include <cudd.h>

namespace souffle {

class AstPresenceCondition : public AstNode {
public:
    virtual DdNode* toBDD(DdManager* bddMgr) = 0;

    virtual bool isTrue() const {
        return false;
    }
}; // AstPresenceCondition

class AstPresenceConditionPrimitive : public AstPresenceCondition {
private:
    bool val;

protected:
    /** Implements the node comparison for this node type */
    virtual bool equal(const AstNode& node) const override {
        assert(nullptr != dynamic_cast<const AstPresenceConditionPrimitive*>(&node));
        const auto& other = static_cast<const AstPresenceConditionPrimitive&>(node);
        return (val == other.val);
    }

public:
    AstPresenceConditionPrimitive(bool v) : val(v) {};

    virtual DdNode* toBDD(DdManager* bddMgr) override {
        DdNode* ret = val ? Cudd_ReadOne(bddMgr) : Cudd_ReadLogicZero(bddMgr);
        Cudd_Ref(ret);
        return ret;
    }

    /** Creates a clone of this AST sub-structure */
    AstPresenceConditionPrimitive* clone() const override {
        auto res = new AstPresenceConditionPrimitive(*this);

        return res;
    }

    /** Mutates this node */
    void apply(const AstNodeMapper& map) override {
    }

    /** Obtains a list of all embedded child nodes */
    std::vector<const AstNode*> getChildNodes() const override {
        std::vector<const AstNode*> res; 
        return res;
    }

    /** Print this clause to a given stream */
    virtual void print(std::ostream& os) const override {
        os << (val ? "True" : "False");
    }

    virtual bool isTrue() const override {
        return val;
    }
};

class AstPresenceConditionFeat : public AstPresenceCondition {
private:
    SymbolTable& st;
    RamDomain index;

protected:
    /** Implements the node comparison for this node type */
    virtual bool equal(const AstNode& node) const override {
        assert(nullptr != dynamic_cast<const AstPresenceConditionFeat*>(&node));
        const auto& other = static_cast<const AstPresenceConditionFeat&>(node);
        return (&st == &other.st && index == other.index);
    }

public:
    AstPresenceConditionFeat(SymbolTable& _st, const std::string& sym) : 
        st(_st), index(st.lookup(sym)) {}

    virtual DdNode* toBDD(DdManager* bddMgr) override {
        DdNode* ret = Cudd_bddIthVar(bddMgr, index);
        Cudd_Ref(ret);
        return ret;
    }

    /** Creates a clone of this AST sub-structure */
    AstPresenceConditionFeat* clone() const override {
        auto res = new AstPresenceConditionFeat(*this);

        return res;
    }

    /** Mutates this node */
    void apply(const AstNodeMapper& map) override {
    }

    /** Obtains a list of all embedded child nodes */
    std::vector<const AstNode*> getChildNodes() const override {
        std::vector<const AstNode*> res; 
        return res;
    }

    /** Print this clause to a given stream */
    virtual void print(std::ostream& os) const override {
        os << st.resolve(index);
    }
};

class AstPresenceConditionNeg : public AstPresenceCondition {
private:
    std::unique_ptr<AstPresenceCondition> pc;

protected:
    /** Implements the node comparison for this node type */
    virtual bool equal(const AstNode& node) const override {
        assert(nullptr != dynamic_cast<const AstPresenceConditionNeg*>(&node));
        const auto& other = static_cast<const AstPresenceConditionNeg&>(node);
        return (pc == other.pc);
    }

public:
    AstPresenceConditionNeg(AstPresenceCondition& _pc) : pc(&_pc) {}

    virtual DdNode* toBDD(DdManager* bddMgr) override {
        DdNode* pcBDD = pc->toBDD(bddMgr);
        DdNode* ret = Cudd_Not(pcBDD);
        Cudd_Ref(ret);
        return ret;
    }

    /** Creates a clone of this AST sub-structure */
    AstPresenceConditionNeg* clone() const override {
        AstPresenceCondition* _pc = (AstPresenceCondition*)pc->clone();

        auto res = new AstPresenceConditionNeg(*_pc);

        return res;
    }

    /** Mutates this node */
    void apply(const AstNodeMapper& map) override {
        map(std::move(pc));
    }

    /** Obtains a list of all embedded child nodes */
    std::vector<const AstNode*> getChildNodes() const override {
        std::vector<const AstNode*> res; 
        res.push_back(pc.get());
        return res;
    }

    /** Print this clause to a given stream */
    virtual void print(std::ostream& os) const override {
        os << "!";
        pc->print(os);
    }
};

enum BIN_OP {
    OP_AND,
    OP_OR
};

class AstPresenceConditionBin : public AstPresenceCondition {
private:
    BIN_OP op;
    std::unique_ptr<AstPresenceCondition> pc1, pc2;
    //bool symmetric = false;
protected:
    /** Implements the node comparison for this node type */
    virtual bool equal(const AstNode& node) const override {
        assert(nullptr != dynamic_cast<const AstPresenceConditionBin*>(&node));
        const auto& other = static_cast<const AstPresenceConditionBin&>(node);
        return (pc1 == other.pc1 && pc2 == other.pc2);
    }

public:
    AstPresenceConditionBin(
        BIN_OP _op, AstPresenceCondition& _pc1, AstPresenceCondition& _pc2
        ) : op(_op), pc1(&_pc1), pc2(&_pc2) {};

    virtual DdNode* toBDD(DdManager* bddMgr) override {
        DdNode* ret = nullptr;
        auto bdd1 = pc1->toBDD(bddMgr);
        auto bdd2 = pc2->toBDD(bddMgr);

	    //if (bdd1 == bdd2) {
		//    symmetric = true;
		//    return bdd1;
	    //}

        switch (op) {
            case OP_AND: ret = Cudd_bddAnd(bddMgr, bdd1, bdd2); break;
            case OP_OR:  ret = Cudd_bddOr(bddMgr, bdd1, bdd2);  break;
        }
        Cudd_Ref(ret);
        return ret;
    }

    /** Creates a clone of this AST sub-structure */
    AstPresenceConditionBin* clone() const override {
        auto _pc1 = (AstPresenceCondition*)pc1->clone();
        auto _pc2 = (AstPresenceCondition*)pc2->clone();

        auto res = new AstPresenceConditionBin(op, *_pc1, *_pc2);

        return res;
    }

    /** Mutates this node */
    void apply(const AstNodeMapper& map) override {
        map(std::move(pc1));
        map(std::move(pc2));
    }

    /** Obtains a list of all embedded child nodes */
    std::vector<const AstNode*> getChildNodes() const override {
        std::vector<const AstNode*> res; 
        res.push_back(pc1.get());
        res.push_back(pc2.get());
        return res;
    }

    /** Print this clause to a given stream */
    virtual void print(std::ostream& os) const override {
	//if (symmetric) {
	//	pc1->print(os);
	//	return;
	//}
        os << "(";
        pc1->print(os);
        switch (op) {
            case OP_AND: os << " /\\ "; break;
            case OP_OR:  os << " \\/ "; break;
        };
        pc2->print(os);
        os << ")";
    }
};

}; // souffle
