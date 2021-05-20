import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class WatcherDemo {

    private static String CONNECTION_STR="2.0.1.131:31300";

    public static void main(String[] args) throws Exception {
        //PathChildCache  --针对于子节点的创建、删除和更新 触发事件
        //NodeCache  针对当前节点的变化触发事件
        //TreeCache  综合事件

        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().
                connectString(CONNECTION_STR).sessionTimeoutMs(5000).
                retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        curatorFramework.start();
        addListenerWithTree(curatorFramework);

        System.in.read();

    }

    //配置中心
    //创建、修改、删除
    private static void addListenerWithNode(CuratorFramework curatorFramework) throws Exception {
        CuratorCache curatorCache = CuratorCache.build( curatorFramework,"/watch" );
        CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder().forNodeCache( () -> {
            System.out.println("receive Node Changed");
            System.out.println(curatorCache.get("/watch").get().getPath()+"---"+new String(curatorCache.get("/watch").get().getData()));
        } ).build();
        curatorCache.listenable().addListener( curatorCacheListener );
        curatorCache.start();
    }

    //实现服务注册中心的时候，可以针对服务做动态感知
    private static void addListenerWithChild(CuratorFramework curatorFramework) throws Exception {
        CuratorCache curatorCache = CuratorCache.build( curatorFramework,"/watch" );
        // 缓存数据
        PathChildrenCacheListener pathChildrenCacheListener = ( curatorFramework1, pathChildrenCacheEvent ) ->
                System.out.println("事件路径:" + pathChildrenCacheEvent.getData().getPath() + "事件类型"
                        + pathChildrenCacheEvent.getType()+"事件数据:" + new String(pathChildrenCacheEvent.getData().getData()));
        CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder().forPathChildrenCache("/watch",curatorFramework, pathChildrenCacheListener ).build();
        curatorCache.listenable().addListener( curatorCacheListener );
        curatorCache.start( );
    }

    //实现服务注册中心的时候，可以针对服务做动态感知
    private static void addListenerWithTree(CuratorFramework curatorFramework) throws Exception {
        CuratorCache curatorCache = CuratorCache.build( curatorFramework,"/watch" );
        // 缓存数据
        TreeCacheListener treeCacheListener = ( curatorFramework1, treeCacheEvent ) ->
                System.out.println("事件路径:" + treeCacheEvent.getData().getPath() + "事件类型"
                + treeCacheEvent.getType()+"事件数据:" + new String(treeCacheEvent.getData().getData()));
        CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder().forTreeCache(curatorFramework, treeCacheListener ).build();
        curatorCache.listenable().addListener( curatorCacheListener );
        curatorCache.start();
    }
}
