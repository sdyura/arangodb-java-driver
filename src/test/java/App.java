import com.arangodb.ArangoDB;

/**
 * @author Michele Rastelli
 */
public class App {
    public static void main(String[] args) throws InterruptedException {
        ArangoDB arangoDB = new ArangoDB.Builder()
                .keepAliveInterval(1)
                .timeout(3_000)
                .build();

        while (true) {
            Thread.sleep(1_000);
            try {
                System.out.println(arangoDB.getVersion().getVersion());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
