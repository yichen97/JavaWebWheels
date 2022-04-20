import com.example.Server;

/**
 * @className: Test
 * @description: Test webServer functions
 **/
public class Test {
    public static void main(String[] args) {
        Server server = new Server(8080);
        server.start();
    }
}
