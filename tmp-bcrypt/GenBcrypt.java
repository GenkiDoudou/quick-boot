import cn.hutool.crypto.digest.BCrypt;
public class GenBcrypt {
  public static void main(String[] a) {
    System.out.println("{bcrypt}" + BCrypt.hashpw("admin", BCrypt.gensalt()));
  }
}