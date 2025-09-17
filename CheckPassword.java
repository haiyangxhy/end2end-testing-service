import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CheckPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 数据库中的密码哈希值
        String hashedPassword = "$2a$10$slYQmyNdGzTn7ZLBXBChFOCrrJkjhVzJe2OLotBK0EhzB5r8v6/Iu";
        
        // 测试密码
        String testPassword = "password";
        
        // 验证密码
        boolean matches = encoder.matches(testPassword, hashedPassword);
        
        System.out.println("Password matches: " + matches);
        
        // 生成新的密码哈希值进行对比
        String newHash = encoder.encode(testPassword);
        System.out.println("New hash: " + newHash);
        System.out.println("New hash matches: " + encoder.matches(testPassword, newHash));
    }
}