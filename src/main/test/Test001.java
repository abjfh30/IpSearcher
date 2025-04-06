import com.abjfh.IpDATrie.PrefixNodeTrie;
import org.junit.jupiter.api.Test;


public class Test001 {
    @Test
    public void test001() {
        System.out.println(7 >> 3);
        System.out.println(8 >> 3);
        System.out.println(9 >> 3);
        System.out.println(16 >> 3);
    }

    @Test
    public void test002() {
        PrefixNodeTrie<Integer> trie = new PrefixNodeTrie<>();
        trie.putIfAbsent(new byte[]{1, 1, 1, 0}, 1);
        trie.putIfAbsent(new byte[]{1, 1, 1, 2}, 2);
        trie.putIfAbsent(new byte[]{1, 2, 1, 3}, 3);
        System.out.println(trie.get(new byte[]{1, 1, 1, 0}));
        System.out.println(trie.get(new byte[]{1, 1, 1, 2}));
        System.out.println(trie.get(new byte[]{1, 2, 1, 3}));
    }
}
