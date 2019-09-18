package JavaExtractor.Visitors;

import JavaExtractor.Common.CommandLineValues;
import JavaExtractor.Common.Common;
import JavaExtractor.Common.MethodContent;
import JavaExtractor.FeatureExtractor;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("StringEquality")
public class FunctionVisitor extends VoidVisitorAdapter<Object> {
    private final ArrayList<MethodContent> m_Methods = new ArrayList<>();
    private final CommandLineValues m_CommandLineValues;

    public FunctionVisitor(CommandLineValues commandLineValues) {
        this.m_CommandLineValues = commandLineValues;
    }

    @Override
    public void visit(MethodDeclaration node, Object arg) {
        visitMethod(node);

        super.visit(node, arg);
    }

    private void visitMethod(MethodDeclaration node) {
        LeavesCollectorVisitor leavesCollectorVisitor = new LeavesCollectorVisitor();
        leavesCollectorVisitor.visitDepthFirst(node);
        ArrayList<Node> leaves = leavesCollectorVisitor.getLeaves();

        String normalizedMethodName = Common.normalizeName(getFullMethodPath(node), Common.BlankWord);
        ArrayList<String> splitNameParts = Common.splitToSubtokens(getFullMethodPath(node));
        String splitName = normalizedMethodName;
        if (splitNameParts.size() > 0) {
            splitName = String.join(Common.internalSeparator, splitNameParts);
        }

        if (node.getBody() != null) {
            long methodLength = getMethodLength(node.getBody().toString());
            if (m_CommandLineValues.MaxCodeLength > 0) {
                if (methodLength >= m_CommandLineValues.MinCodeLength && methodLength <= m_CommandLineValues.MaxCodeLength) {
                    m_Methods.add(new MethodContent(leaves, splitName));
                }
            } else {
                m_Methods.add(new MethodContent(leaves, splitName));
            }
        }
    }

    static String getFucntionParameterAsString(MethodDeclaration node) {
        return node.getParameters().stream()
                .map(p -> p.getElementType()+ "|" + p.getName())
                .collect(Collectors.joining("|"));

    }

    public static String getFullMethodPath(MethodDeclaration node) {
        String args = getFucntionParameterAsString(node);

        ArrayList<Node> stack = FeatureExtractor.getTreeStack(node);
        Collections.reverse(stack);

        String fullyQualifiedName = stack.stream()
                .skip(1)
                .filter(n -> n instanceof TypeDeclaration || n instanceof MethodDeclaration)
                .map(n -> ((NodeWithName) n))
                .map(NodeWithName::getName)
                .collect(Collectors.joining("|"));

        return fullyQualifiedName + "(" + args + ")";
    }

    private long getMethodLength(String code) {
        String cleanCode = code.replaceAll("\r\n", "\n").replaceAll("\t", " ");
        if (cleanCode.startsWith("{\n"))
            cleanCode = cleanCode.substring(3).trim();
        if (cleanCode.endsWith("\n}"))
            cleanCode = cleanCode.substring(0, cleanCode.length() - 2).trim();
        if (cleanCode.length() == 0) {
            return 0;
        }
        return Arrays.stream(cleanCode.split("\n"))
                .filter(line -> (line.trim() != "{" && line.trim() != "}" && line.trim() != ""))
                .filter(line -> !line.trim().startsWith("/") && !line.trim().startsWith("*")).count();
    }

    public ArrayList<MethodContent> getMethodContents() {
        return m_Methods;
    }
}
