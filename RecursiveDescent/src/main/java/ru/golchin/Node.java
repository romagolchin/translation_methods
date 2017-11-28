package ru.golchin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class Node {
    private String name;
    private List<Node> children;

    Node(String name, Node... children) {
        this.name = name;
        this.children = Arrays.asList(children);
    }

    Node(Node toCopy) {
        this.name = toCopy.name;
        this.children = toCopy.children;
    }

    public String getName() {
        return name;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    private int getEdgesAndNodes(int cnt, List<String> list) {
        int curChildrenNumber = 0;
        list.add(cnt + " [label = \"" + name + "\"]");
        for (Node ch : children) {
            int childId = curChildrenNumber + cnt + 1;
            list.add(cnt + " -> " + childId);
            curChildrenNumber += ch.getEdgesAndNodes(childId, list);
        }
        return curChildrenNumber + 1;
    }

    @Override
    public String toString() {
        return "{" + name + " " + children.stream().map(Node::toString).collect(Collectors.joining(" ")) + "}";
    }

    public String toGraphString() {
        List<String> edges = new ArrayList<>();
        getEdgesAndNodes(0, edges);
        return edges.stream().collect(Collectors.joining(";\n\t", "digraph dg {\n\t", "\n}"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(name, node.name) &&
                Objects.equals(children, node.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, children);
    }
}
