import com.testplatform.model.TestSuite;

public class TestSuiteIdTest {
    public static void main(String[] args) {
        TestSuite testSuite = new TestSuite();
        System.out.println("Before generateId(): " + testSuite.getId());
        
        testSuite.generateId();
        System.out.println("After generateId(): " + testSuite.getId());
        
        // 测试通过构造函数创建的情况
        TestSuite testSuite2 = new TestSuite("test-id", "Test Name", "Test Description", TestSuite.TestSuiteType.API);
        System.out.println("Constructor with ID: " + testSuite2.getId());
    }
}