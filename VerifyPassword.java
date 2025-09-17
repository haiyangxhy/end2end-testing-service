import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class VerifyPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 从日志中看到的数据库密码哈希值
        String dbPassword = "$2a$10$slYQmyNdGzTn7ZLBXBChFOCrrJkjhVzJe2OLotBK0EhzB5r8v6/Iu";
        
        // 测试密码
        String inputPassword = "password";
        
        // 验证密码
        boolean matches = encoder.matches(inputPassword, dbPassword);
        
        System.out.println("Password matches: " + matches);
        
        // 生成新的密码哈希值进行对比
        String newHash = encoder.encode(inputPassword);
        System.out.println("New hash: " + newHash);
        System.out.println("New hash matches input: " + encoder.matches(inputPassword, newHash));
    }
}