import java.io.*;
import java.util.*;
import java.util.regex.*;

public class AdvancedTestGenerator {
    
    private static final String JAVA_SOURCE_DIR = "C:/Users/8207259/WorkSpace_Junit/19849-gpbs-web/efbs2-parent/efbs2/src/main/java/com/scb/efbs2"; // Change this
    private static final String TEST_OUTPUT_DIR = "C:/Users/8207259/WorkSpace_Junit/19849-gpbs-web/efbs2-parent/efbs2/src/main/java/com/scb/efbs2/output"; // Change this
    
    public static void main(String[] args) {
        File sourceDir = new File(JAVA_SOURCE_DIR);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            System.out.println("Invalid source directory: " + JAVA_SOURCE_DIR);
            return;
        }
        
        processDirectory(sourceDir);
    }
    
    private static void processDirectory(File directory) {
        File[] files = directory.listFiles();
        
        if (files == null) {
            return; // Directory is empty or not accessible
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file); // Recursively process subdirectories
            } else if (file.getName().endsWith(".java")) {
                processJavaFile(file);
            }
        }
    }
    
    private static void processJavaFile(File javaFile) {
        System.out.println("Processing file: " + javaFile.getAbsolutePath());  // Debugging log
        try (BufferedReader br = new BufferedReader(new FileReader(javaFile))) {
            String className = "";
            List<MethodInfo> methods = new ArrayList<>();
            String line;

            while ((line = br.readLine()) != null) {
                // Check for class declaration
                if (line.trim().matches(".*class\\s+([a-zA-Z0-9_]+).*")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 1) {
                        className = parts[1];
                    } else {
                        System.out.println("⚠️ Warning: Unexpected class declaration format in " + javaFile.getName());
                    }
                }

                // Check for method declarations
                Matcher methodMatcher = Pattern.compile(".*(public|protected|private)?\\s*([a-zA-Z0-9_<>]+)\\s+([a-zA-Z0-9_]+)\\(([^)]*)\\).*").matcher(line.trim());
                if (methodMatcher.matches()) {
                    String returnType = methodMatcher.group(2);
                    String methodName = methodMatcher.group(3);
                    String params = methodMatcher.group(4);
                    methods.add(new MethodInfo(returnType, methodName, params));
                }
            }

            if (!className.isEmpty()) {
                generateTestFile(javaFile, className, methods);
            } else {
                System.out.println("❌ No class found in: " + javaFile.getName());
            }
        } catch (Exception e) {
            System.out.println("❌ Error processing file: " + javaFile.getAbsolutePath());
            e.printStackTrace();  // Print full stack trace for debugging
        }
    }
    
    private static void generateTestFile(File javaFile, String className, List<MethodInfo> methods) {
        String packagePath = javaFile.getParent().replace(JAVA_SOURCE_DIR, "").replace(File.separator, ".");
        packagePath = packagePath.startsWith(".") ? packagePath.substring(1) : packagePath;
        String testClassName = className + "Test";
        String testFilePath = TEST_OUTPUT_DIR + File.separator + testClassName + ".java";
        
        try {
            File testFile = new File(testFilePath);
            testFile.getParentFile().mkdirs();  // Ensure the parent directory exists
            
            PrintWriter writer = new PrintWriter(new FileWriter(testFile));
            
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
            for (MethodInfo method : methods) {
                writer.println("    @Test");
                writer.println("    public void test" + capitalize(method.methodName) + "() {");
                
                // Generate test logic based on method return type
                if ("void".equals(method.returnType)) {
                    writer.println("        // TODO: Call the " + method.methodName + " and check for exceptions");
                    writer.println("        try {");
                    writer.println("            // Your test logic here");
                    writer.println("            // Call method, e.g., instance." + method.methodName + "();");
                    writer.println("        } catch (Exception e) {");
                    writer.println("            fail(\"Exception occurred: \" + e.getMessage());");
                    writer.println("        }");
                } else {
                    writer.println("        // TODO: Assert return value from " + method.methodName);
                    writer.println("        " + method.returnType + " result = instance." + method.methodName + "();");
                    writer.println("        assertNotNull(result, \"Expected non-null result\");");
                }

                writer.println("    }");
                writer.println();
            }

            writer.println("}");
            writer.close();
            System.out.println("✅ Test file created successfully: " + testFilePath);
        } catch (IOException e) {
            System.out.println("❌ Error writing test file: " + testFilePath);
            e.printStackTrace();  // Print full stack trace for debugging
        }
    }

    private static String capitalize(String methodName) {
        return methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
    }

    // Inner class to store method info
    private static class MethodInfo {
        String returnType;
        String methodName;
        String parameters;

        public MethodInfo(String returnType, String methodName, String parameters) {
            this.returnType = returnType;
            this.methodName = methodName;
            this.parameters = parameters;
        }
    }
}
