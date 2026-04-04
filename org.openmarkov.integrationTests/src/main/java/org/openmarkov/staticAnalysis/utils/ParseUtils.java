package org.openmarkov.staticAnalysis.utils;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.plugin.PluginSearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParseUtils {
    
    public static void prepareJavaParserConfiguration() {
        //This method does nothing on purpose, it only forces the static initializer to run.
    }
    
    static {
        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver(false)); //Adds parsing of JDK code
        ScanResult scan = new ClassGraph().scan();
        var classPaths = scan.getClasspathURLs().stream()
                             .map(URL::getFile)
                             .map(File::new)
                             .filter(File::exists)
                             .toList();
        scan.close();
        for (File classPath : classPaths) {
            //Adds external dependencies jar for resolving classes.
            String absolutePath = classPath.getAbsolutePath();
            if (absolutePath.endsWith(".jar")) {
                try {
                    typeSolver.add(new JarTypeSolver(classPath));
                } catch (IOException e) {
                }
            } else {
                if (absolutePath.contains("target\\classes")) {
                    absolutePath = absolutePath.replace("target\\classes", "src\\main\\java");
                }
                typeSolver.add(new JavaParserTypeSolver(new File(absolutePath)));
            }
        }
        config.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        StaticJavaParser.setConfiguration(config);
    }
    
    public static @NotNull Stream<CompilationUnit> baseOpenMarkovParsedClasses() {
        return PluginSearch
                .init()
                .stream()
                .filter(openmarkovClass -> openmarkovClass.getModule() != ParseUtils.class.getModule())
                .map(openmarkovClass ->
                     {
                         try {
                             return StaticJavaParser.parse(ClassUtils.fileOfClass(openmarkovClass));
                         } catch (FileNotFoundException | IllegalArgumentException e) {
                             return null;
                         }
                     })
                .filter(Objects::nonNull)
                .sorted(ParseUtils.COMPILATION_UNIT_COMPARATOR);
    }
    
    private static Map<String, TypeDeclaration> OPENMARKOV_PARSED_CLASSES;
    
    public synchronized static TypeDeclaration openMarkovParsedClass(Class<?> openMarkovClass) {
        if (OPENMARKOV_PARSED_CLASSES == null) {
            var classNameToDeclaration = new TreeMap<String, TypeDeclaration>();
            PluginSearch
                    .init()
                    .stream()
                    .flatMap(openmarkovClass -> {
                        try {
                            CompilationUnit parsedClass = StaticJavaParser.parse(ClassUtils.fileOfClass(openmarkovClass));
                            //return parsedClass.findAll(TypeDeclaration.class).stream();
                            var typeDeclarations = new ArrayList<TypeDeclaration>();
                            Queue<Node> stack = new ArrayDeque<>();
                            stack.add(parsedClass);
                            while (!stack.isEmpty()) {
                                var element = stack.remove();
                                stack.addAll(element.getChildNodes());
                                if (element instanceof TypeDeclaration typeDeclaration) {
                                    typeDeclarations.add(typeDeclaration);
                                }
                            }
                            return typeDeclarations.stream();
                        } catch (FileNotFoundException | IllegalArgumentException e) {
                            return Stream.empty();
                        }
                    })
                    .forEach(typeDeclaration -> {
                        String qualifiedName = (String) typeDeclaration.getFullyQualifiedName().get();
                        classNameToDeclaration.put(qualifiedName, typeDeclaration);
                    });
            OPENMARKOV_PARSED_CLASSES = Collections.unmodifiableMap(classNameToDeclaration);
        }
        return ParseUtils.OPENMARKOV_PARSED_CLASSES.get(openMarkovClass.getName().replace('$', '.'));
    }
    
    private static final Map<String, Class<? extends Object>> CLASSES_BY_NAME = PluginSearch
            .full()
            .stream()
            .filter(aClass -> aClass.getCanonicalName() != null)
            .collect(Collectors.toMap(Class::getCanonicalName, value -> value));
    
    public static Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return ParseUtils.CLASSES_BY_NAME.get(className);
        }
    }
    
    public static Class<?> classOf(Type type) {
        return switch (type.resolve()) {
            case ResolvedReferenceType referenceType -> ParseUtils.classForName(referenceType.getQualifiedName());
            default -> throw new IllegalStateException("Unexpected value: " + type.resolve());
        };
    }
    
    public static CompilationUnit sourceOf(Node node) {
        return ParseUtils.superSearch(node, CompilationUnit.class).get();
    }
    
    public static <SearchingClass extends Node> Optional<SearchingClass> superSearch(Node node, Class<? extends SearchingClass> searchingClass) {
        while (node != null && !searchingClass.isAssignableFrom(node.getClass())) {
            node = node.getParentNode().orElse(null);
        }
        if (node == null) {
            return Optional.empty();
        }
        return Optional.of(searchingClass.cast(node));
    }
    
    public static @NotNull String getSourceLine(Node objectCreationExpr) {
        CompilationUnit origin = sourceOf(objectCreationExpr);
        Optional<Range> range = objectCreationExpr.getRange();
        String packageName = origin.getPackageDeclaration().map(NodeWithName::getNameAsString)
                                   .orElse("");
        String className = origin.getPrimaryTypeName().orElse(null);
        String qualifiedName = packageName + "." + className;
        var methodName = superSearch(objectCreationExpr, CallableDeclaration.class)
                .map(CallableDeclaration::getNameAsString)
                .orElse("somewhere");
        int line = range.get().begin.line;
        return String.format("%s.%s(%s.java:%d)", qualifiedName, methodName, className, line);
    }
    
    public static final Comparator<CompilationUnit> COMPILATION_UNIT_COMPARATOR = Comparator
            .comparing((CompilationUnit unit) -> unit
                    .getPackageDeclaration()
                    .map(NodeWithName::getNameAsString)
                    .orElse(""))
            .thenComparing(unit -> unit
                    .getPrimaryTypeName().orElse(""));
}
