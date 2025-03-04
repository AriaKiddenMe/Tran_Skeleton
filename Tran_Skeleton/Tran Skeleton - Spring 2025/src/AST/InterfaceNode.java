package AST;

import java.util.ArrayList;
import java.util.List;

public class InterfaceNode implements Node {
    public String name;
    public List<MethodHeaderNode> methods = new ArrayList<>();

    @Override
    public String toString() {
        methods.add(new MethodHeaderNode());
        return "interface " + name + "\n" + methods;
    }
}
