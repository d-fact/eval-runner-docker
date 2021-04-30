package cslicer.distiller;

/*
 * #%L
 * ChangeDistiller
 * %%
 * Copyright (C) 2011 - 2013 Software Architecture and Evolution Lab, Department of Informatics, UZH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;

import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureNode;
import cslicer.coverage.StructureNodeStatus;

/**
 * Node for Java structure differencing.
 * 
 * @author Beat Fluri
 */
public class GitRefJavaStructureNode implements StructureNode {

	private Type fType;
	private String fName;
	private String fQualifier;
	private ASTNode fASTNode;
	private List<GitRefJavaStructureNode> fChildren;
	private StructureNodeStatus fStatus;

	/**
	 * Creates a new Java structure node
	 * 
	 * @param type
	 *            of the node
	 * @param qualifier
	 *            of the node
	 * @param name
	 *            of the node
	 * @param astNode
	 *            representing the structure node
	 * @param status
	 *            structure node status
	 */
	public GitRefJavaStructureNode(Type type, String qualifier, String name,
			ASTNode astNode, StructureNodeStatus status) {
		fType = type;
		fQualifier = qualifier;
		fName = name;
		fASTNode = astNode;
		fChildren = new LinkedList<GitRefJavaStructureNode>();
		setfStatus(status);
	}

	/**
	 * Adds a new {@link GitRefJavaStructureNode} child.
	 * 
	 * @param node
	 *            to add as child
	 */
	public void addChild(GitRefJavaStructureNode node) {
		fChildren.add(node);
	}

	@Override
	public List<GitRefJavaStructureNode> getChildren() {
		return fChildren;
	}

	@Override
	public String toString() {
		return fType.name() + ": " + fName;
	}

	/**
	 * Java structure node types.
	 * 
	 * @author Beat Fluri
	 */
	public enum Type {
		CU, FIELD, CONSTRUCTOR, METHOD, INTERFACE, CLASS, ANNOTATION, ENUM
	}

	public Type getType() {
		return fType;
	}

	@Override
	public String getName() {
		return fName;
	}

	/**
	 * Returns the fully qualified name of this node if the node has a
	 * qualifier. Otherwise, {@link #getName()} is returned.
	 * 
	 * @return the fully qualified name of this node, if the node has a
	 *         qualifier; name otherwise.
	 */
	@Override
	public String getFullyQualifiedName() {
		if (fQualifier != null) {
			return fQualifier + "." + fName;
		}
		return getName();
	}

	@Override
	public String getContent() {
		return fASTNode.toString();
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && (getClass() == obj.getClass())) {
			GitRefJavaStructureNode other = (GitRefJavaStructureNode) obj;
			return (fType == other.getType()) && fName.equals(other.getName());
		}
		return super.equals(obj);
	}

	public ASTNode getASTNode() {
		return fASTNode;
	}

	@Override
	public boolean isClassOrInterface() {
		return (fType == Type.CLASS) || (fType == Type.INTERFACE);
	}

	@Override
	public boolean isMethodOrConstructor() {
		return (fType == Type.METHOD) || (fType == Type.CONSTRUCTOR);
	}

	@Override
	public boolean isField() {
		return fType == Type.FIELD;
	}

	@Override
	public boolean isOfSameTypeAs(StructureNode other) {
		if (other.getClass() == getClass()) {
			return fType == ((GitRefJavaStructureNode) other).fType;
		}
		return false;
	}

	/**
	 * @return the fStatus
	 */
	public StructureNodeStatus getfStatus() {
		return fStatus;
	}

	/**
	 * @param fStatus
	 *            the fStatus to set
	 */
	public void setfStatus(StructureNodeStatus fStatus) {
		this.fStatus = fStatus;
	}

}
