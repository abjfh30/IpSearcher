import com.abjfh.IpDATrie.PrefixNodeTrie;
import com.abjfh.Utils.ByteArrayUtil;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv6.IPv6Address;
import inet.ipaddr.ipv6.IPv6AddressSeqRange;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class PrefixNodeTrieTest {
    @Test
    public void test001() {
        PrefixNodeTrie<String[]> trie = new PrefixNodeTrie<>();
        String fileName = "data\\BGP_INFO_IPV6.csv";
        int prefixLength = 128;

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.lines().forEach(line -> {
                String[] split = line.split(",");
                IPv6Address sip = new IPAddressString(split[0]).getAddress().toIPv6();
                IPv6Address eip = new IPAddressString(split[1]).getAddress().toIPv6();
                for (IPv6Address iPv6Address : new IPv6AddressSeqRange(sip, eip).spanWithPrefixBlocks()) {
                    byte[] bytes = iPv6Address.getBytes();
                    try {
                        trie.putIfAbsent(ByteArrayUtil.mask2ByteArray(bytes, iPv6Address.getPrefixLength()), split);
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.lines().parallel().forEach(line -> {
                String[] split = line.split(",");
                IPv6Address sip = new IPAddressString(split[0]).getAddress().toIPv6();
                IPv6Address eip = new IPAddressString(split[1]).getAddress().toIPv6();
                IPv6AddressSeqRange seqRange = new IPv6AddressSeqRange(sip, eip);
                seqRange.forEach(ip -> {
                    assert Arrays.equals(split, trie.get(ByteArrayUtil.mask2ByteArray(ip.getBytes(), prefixLength)));
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
