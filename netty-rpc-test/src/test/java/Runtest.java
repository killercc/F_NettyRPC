import com.config.ZooKeeperConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


@Slf4j
public class Runtest {
    private CuratorFramework client;

    @Before
    public void testconnect(){
        client = CuratorFrameworkFactory.builder().connectString(ZooKeeperConfig.HOST)
                .sessionTimeoutMs(60 * 1000)
                .connectionTimeoutMs(60 * 1000)
                .namespace(ZooKeeperConfig.ZK_NAMESPACE)
                .retryPolicy(new ExponentialBackoffRetry(10 * 1000, 10))
                .build();
        client.start();

    }

    @Test
    public void createNode() throws Exception {

        final String path = client.create().forPath("/netty-client");
        System.out.println(path);

    }

    @Test
    public void test() throws InterruptedException {
        System.out.println("test");
        log.info("test");
    }



    @After
    public void closeconnect(){
        if(client != null)
            client.close();
    }
}
