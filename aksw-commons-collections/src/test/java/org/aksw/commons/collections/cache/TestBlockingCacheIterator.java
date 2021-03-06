package org.aksw.commons.collections.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class TestBlockingCacheIterator {
    @Test
    public void test() {
        //List<String> testData = Arrays.asList("john", "doe", "alice", "bob");
        List<String> testData = IntStream.range(0, 1000).mapToObj(i -> "item-" + i).collect(Collectors.toList());
        Cache<List<String>> cache = new Cache<>(new ArrayList<>());

        CachingIterable<String> driver = new CachingIterable<>(testData.iterator(), cache);

        BlockingCacheIterator<String> it = new BlockingCacheIterator<>(cache);

        new Thread(() -> {
            int i = 0;
            for(String item : driver) {
                ++i;
//                if(i == 100) {
//                    cache.setAbanoned(true);
//                    break;
//                }
                System.out.println("Driver: " + item);
            }
        }).start();

        while(it.hasNext()) {
            String item = it.next();
            System.out.println("Client: " + item);
        }

    }

}
