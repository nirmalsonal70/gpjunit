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
            break;
        }
    }
    
    private static void processJavaFile(File javaFile) {
        System.out.println("Processing file: " + javaFile.getAbsolutePath());  // Debugging log
        try (BufferedReader br = new BufferedReader(new FileReader(javaFile))) {
            String className = "";
            List<String> methods = new ArrayList<>();
            String line;

            while ((line = br.readLine()) != null) {
//                System.out.println("Reading line: " + line);  // Debugging log

                // Check for class declaration
                if (line.trim().matches(".*class\\s+([a-zA-Z0-9_]+).*")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 1) {
                        className = parts[1];
//                        System.out.println("Found class: " + className);
                    } else {
                        System.out.println("⚠️ Warning: Unexpected class declaration format in " + javaFile.getName());
                    }
                }

                // Check for method declarations
                if (line.trim().matches(".*\\s+([a-zA-Z0-9_]+)\\(.*\\)\\s*\\{")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 1) {
                        String methodName = parts[1];
                        methods.add(methodName);
//                        System.out.println("Found method: " + methodName);
                    } else {
                        System.out.println("⚠️ Warning: Unexpected method declaration format in " + javaFile.getName());
                    }
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
    
    private static void generateTestFile(File javaFile, String className, List<String> methods) {
        String packagePath = javaFile.getParent().replace(JAVA_SOURCE_DIR, "").replace(File.separator, ".");
        packagePath = packagePath.startsWith(".") ? packagePath.substring(1) : packagePath;
        String testClassName = className + "Test";
        String testFilePath = TEST_OUTPUT_DIR + File.separator + testClassName + ".java";
        try {
            File testFile = new File(testFilePath);
            testFile.getParentFile().mkdirs();  // Ensure the parent directory exists
            
            System.out.println("Attempting to create test file: " + testFilePath);  // Debugging log
            
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
            System.out.println("✅ Test file created successfully: " + testFilePath);
        } catch (IOException e) {
            System.out.println("❌ Error writing test file: " + testFilePath);
            e.printStackTrace();  // Print full stack trace for debugging
        }
    }

    private static String capitalize(String methodName) {
        return methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
    }
}
