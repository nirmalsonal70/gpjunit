import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AdvancedTestGenerator {
    private static final String JAVA_SOURCE_DIR = "path/to/your/java/source"; // Update this path
    private static final String TEST_OUTPUT_DIR = "path/to/generated/testng/tests"; // Update this path

    public static void main(String[] args) {
        File sourceDir = new File(JAVA_SOURCE_DIR);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            System.out.println("Invalid source directory: " + JAVA_SOURCE_DIR);
            return;
        }

        File testDir = new File(TEST_OUTPUT_DIR);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }

        processDirectory(sourceDir);
    }

    private static void processDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file); // Recursively process subdirectories
            } else if (file.getName().endsWith(".java")) {
                processJavaFile(file);
            }
        }
    }

    private static void processJavaFile(File javaFile) {
        String className = javaFile.getName().replace(".java", "");
        List<String> methods = extractPublicMethods(javaFile);

        if (!methods.isEmpty()) {
            generateTestFile(javaFile, className, methods);
        }
    }

    private static List<String> extractPublicMethods(File javaFile) {
        List<String> methods = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("public ") && line.contains("(") && line.contains(")")) {
                    String methodName = line.split("\\s+")[2].split("\\(")[0];
                    methods.add(methodName);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + javaFile.getName());
        }
        return methods;
    }

    private static void generateTestFile(File javaFile, String className, List<String> methods) {
        String packagePath = javaFile.getParent().replace(JAVA_SOURCE_DIR, "").replace(File.separator, ".");
        packagePath = packagePath.startsWith(".") ? packagePath.substring(1) : packagePath;
        String testClassName = className + "Test";
        String testFilePath = TEST_OUTPUT_DIR + File.separator + packagePath.replace(".", File.separator) + File.separator + testClassName + ".java";

        try {
            new File(testFilePath).getParentFile().mkdirs(); // Create directories if needed
            PrintWriter writer = new PrintWriter(new FileWriter(testFilePath));

            // Package declaration
            if (!packagePath.isEmpty()) {
                writer.println("package " + packagePath + ";");
                writer.println();
            }

            // Imports
            writer.println("import org.testng.annotations.Test;");
            writer.println("import static org.testng.Assert.*;");
            writer.println();
            writer.println("public class " + testClassName + " {");
            writer.println();

            // Generate test methods
            for (String method : methods) {
                writer.println("    @Test");
                writer.println("    public void test" + capitalize(method) + "() {");
                writer.println("        // TODO: Implement test for " + method);
                writer.println("        fail(\"Not implemented yet\");");
                writer.println("    }");
                writer.println();
            }

            writer.println("}");
            writer.close();
            System.out.println("Generated test file: " + testFilePath);
        } catch (IOException e) {
            System.out.println("Error writing test file: " + testFilePath);
        }
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
