import com.abjfh.IpDATrie.PrefixNodeTrie;
import com.abjfh.Utils.ByteArrayUtil;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.Arrays;

public class PrefixNodeTrieTest {
    @Test
    public void test003() throws Exception {
        PrefixNodeTrie<Integer> trie = new PrefixNodeTrie<>();
        trie.putIfAbsent(new byte[]{1, 3}, 1);
        trie.putIfAbsent(new byte[]{1, 4}, 2);
        trie.putIfAbsent(new byte[]{1, 5}, 3);
        byte[] keys = ByteArrayUtil.mask2ByteArray(InetAddress.getByName("2001:503:e239::").getAddress(), 48);
        trie.putIfAbsent(keys, 5);
        System.out.println(trie.get(keys));
        System.out.println(trie.get(new byte[]{1, 3}));
        System.out.println(trie.get(new byte[]{1, 4}));
        System.out.println(trie.get(new byte[]{1, 5}));

    }

    @Test
    public void test004() {
        byte[] keys = {1, 4, 2, 1};
        byte[] shareKeys = {1, 4, 2, 2};
        int mismatch = Arrays.mismatch(
                keys,
                0,
                keys.length,
                shareKeys,
                0,
                shareKeys.length
        );
        System.out.println(mismatch);
        System.out.println(mismatch);
    }
}
