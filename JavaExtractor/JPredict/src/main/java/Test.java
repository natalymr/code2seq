
import JavaExtractor.Common.Common;
import JavaExtractor.Visitors.FunctionVisitor;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.FileNotFoundException;


class Test {
    void fooBar() {
        System.out.println("http://github.com");
    }

    public static void helper(Node node) {
        for (Node childrenNode : node.getChildrenNodes()) {
            helper(childrenNode);
            if (childrenNode instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration) childrenNode;
                for (MethodDeclaration method : clazz.getMethods()) {
                    System.out.println(Common.splitToSubtokens(FunctionVisitor.getFullMethodPath(method)));
                }
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {

        CompilationUnit parse = JavaParser.parse(new File("/Users/natalia.murycheva/Documents/" +
                "gitCommitMessageCollectorStorage/aurora_blobs_splitted/0/ffb28b61b6f8734e026517d4576689dac3bd3f7e.java"));

        helper(parse);

    }
}