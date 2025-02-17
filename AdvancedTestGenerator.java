import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class AdvancedTestGenerator {

    private static final String JAVA_SOURCE_DIR = "path/to/your/java/source"; // Change this
    private static final String TEST_OUTPUT_DIR = "path/to/generated/testng/tests"; // Change this

    private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+(\\w+)\\s*(extends\\s+(\\w+))?");
    private static final Pattern ABSTRACT_PATTERN = Pattern.compile("abstract\\s+class\\s+(\\w+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("public\\s+\\w+\\s+(\\w+)\\(([^)]*)\\)");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get(TEST_OUTPUT_DIR));

        List<File> javaFiles = getJavaFiles(new File(JAVA_SOURCE_DIR));
        Map<String, String> classInheritanceMap = new HashMap<>();

        // First pass: collect class names and their parent classes
        for (File javaFile : javaFiles) {
            analyzeClassHierarchy(javaFile, classInheritanceMap);
        }

        // Second pass: generate test cases
        for (File javaFile : javaFiles) {
            generateTestFile(javaFile, classInheritanceMap);
        }

        System.out.println("Test generation completed!");
    }

    private static List<File> getJavaFiles(File directory) {
        return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .flatMap(file -> file.isDirectory() ? getJavaFiles(file).stream() : Stream.of(file))
                .filter(file -> file.getName().endsWith(".java"))
                .collect(Collectors.toList());
    }

    private static void analyzeClassHierarchy(File javaFile, Map<String, String> classInheritanceMap) throws IOException {
        String content = Files.readString(javaFile.toPath());

        Matcher classMatcher = CLASS_PATTERN.matcher(content);
        if (classMatcher.find()) {
            String className = classMatcher.group(1);
            String parentClass = classMatcher.group(3);
            if (parentClass != null) {
                classInheritanceMap.put(className, parentClass);
            }
        }
    }

    private static void generateTestFile(File javaFile, Map<String, String> classInheritanceMap) throws IOException {
        String content = Files.readString(javaFile.toPath());

        Matcher classMatcher = CLASS_PATTERN.matcher(content);
        Matcher abstractMatcher = ABSTRACT_PATTERN.matcher(content);

        if (!classMatcher.find()) return;

        String className = classMatcher.group(1);
        boolean isAbstract = abstractMatcher.find();

        Matcher methodMatcher = METHOD_PATTERN.matcher(content);
        List<String> methods = new ArrayList<>();

        while (methodMatcher.find()) {
            methods.add(methodMatcher.group(1));
        }

        if (!methods.isEmpty()) {
            writeTestFile(className, methods, classInheritanceMap, isAbstract);
        }
    }

    private static void writeTestFile(String className, List<String> methods, Map<String, String> classInheritanceMap, boolean isAbstract) throws IOException {
        String testClassName = className + "Test";
        String testFilePath = TEST_OUTPUT_DIR + "/" + testClassName + ".java";

        StringBuilder testContent = new StringBuilder();
        testContent.append("package com.example;\n\n")
                   .append("import static org.testng.Assert.*;\n")
                   .append("import org.testng.annotations.Test;\n\n")
                   .append("public class ").append(testClassName).append(" {\n\n");

        String parentClass = classInheritanceMap.get(className);
        String instanceType = (isAbstract ? "// Cannot instantiate abstract class" : className + " obj = new " + className + "();");

        for (String method : methods) {
            testContent.append("    @Test\n")
                       .append("    public void test").append(capitalize(method)).append("() {\n")
                       .append("        // TODO: Add test logic for ").append(method).append("\n")
                       .append("        ").append(instanceType).append("\n");

            if (parentClass != null) {
                testContent.append("        // Also testing inherited behavior from ").append(parentClass).append("\n");
            }

            testContent.append("        // obj.").append(method).append("();\n")
                       .append("        assertTrue(true); // Replace with actual assertion\n")
                       .append("    }\n\n");
        }

        testContent.append("}\n");

        Files.writeString(Paths.get(testFilePath), testContent.toString());
        System.out.println("Generated TestNG file: " + testFilePath);
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
