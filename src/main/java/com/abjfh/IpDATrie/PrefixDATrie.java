package com.abjfh.IpDATrie;

import com.abjfh.Utils.ByteArrayUtil;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv6.IPv6Address;
import inet.ipaddr.ipv6.IPv6AddressSeqRange;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class PrefixDATrie<V> {
    Int2ObjectOpenHashMap<int[]> map;
    ArrayList<V> list;
    int base0 = 1;

    private PrefixDATrie(PrefixNodeTrie<V> trie) {
        map = new Int2ObjectOpenHashMap<>();
        list = new ArrayList<>(trie.list);
        map.put(0, getDefaultIntArr());
        //广度优先遍历
        Queue<PrefixNodeTrie.Node> queue = new LinkedList<>();
        queue.add(trie.root);
        Random random = new Random(10);
        while (!queue.isEmpty()) {
            PrefixNodeTrie.Node poll = queue.poll();
            int[] segment = map.get(poll.baseIndex >>> 3);
            int offset = segment[(poll.baseIndex & 7) + 8];
            Int2ObjectMap.FastEntrySet<PrefixNodeTrie.Node> entries = poll.children.int2ObjectEntrySet();
            //寻找放置所有子节点的偏移量
            while1:
            while (true) {
                for (Int2ObjectMap.Entry<PrefixNodeTrie.Node> entry : entries) {
                    //判断当前子节点是否可用当前偏移量
                    int t = poll.baseIndex + entry.getIntKey() + offset;
                    int[] nextSegment = map.get(t >>> 3);
                    if (nextSegment != null) {
                        if (entry.getValue().shareKeys != null || nextSegment[t & 7] != -1) {
                            offset += random.nextInt(1024) + 1;
                            continue while1;
                        }
                    }
                }
                break;
            }
            //修改当前节点偏移量
            segment[(poll.baseIndex & 7) + 8] = offset;

            for (Int2ObjectMap.Entry<PrefixNodeTrie.Node> entry : entries) {
                //修改子节点的check值
                PrefixNodeTrie.Node value = entry.getValue();
                value.baseIndex = poll.baseIndex + entry.getIntKey() + offset;
                int[] nextSegment = map.computeIfAbsent(value.baseIndex >>> 3, k -> getDefaultIntArr());
                nextSegment[value.baseIndex & 7] = poll.baseIndex;


                if (value.shareKeys != null) {
                    //如果子节点存在公共路径
                    int s = value.baseIndex, c = 1, t;
                    for (int i = 0; i < value.shareKeys.length; i++) {
                        while (true) {
                            t = s + value.shareKeys[i] + c;
                            nextSegment = map.get(t >>> 3);
                            if (nextSegment == null || nextSegment[t & 7] == -1) {
                                nextSegment = map.computeIfAbsent(t >>> 3, k -> getDefaultIntArr());
                                nextSegment[t & 7] = s;
                                break;
                            } else {
                                c++;
                            }
                        }
                        s = t;
                    }
                    value.baseIndex = s;
                }
                if (value.isLeaf) {
                    nextSegment[value.baseIndex & 7] = value.tailIndex;
                } else {
                    queue.add(value);
                }
            }
        }

        base0 = map.get(0)[8];

    }

    private int[] getDefaultIntArr() {
        return new int[]{-1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1};
    }

    public V search(String ip) throws UnknownHostException {
        return search(InetAddress.getByName(ip).getAddress());
    }

    public V search(byte[] bytes) {
        int offset = base0;
        int s = 0, t;
        for (byte b : bytes) {
            for (int i = 7; i >= 0; i--, b = (byte) (b >> 1)) {
                t = s + (b & 1) + offset;
                int[] segment = map.get(t >>> 3);
                if (segment == null || segment[t & 7] != s) {
                    return null;
                }
                offset = segment[(t & 7) + 8];
                if (offset < 0) {
                    return list.get(-offset - 1);
                }
                s = t;
            }
        }
        return null;
    }

    public static void main(String[] args) throws UnknownHostException {
        PrefixNodeTrie<String[]> trie = new PrefixNodeTrie<>();
        try (BufferedReader br = new BufferedReader(new FileReader("BGP_INFO_IPV6.csv"))) {
            br.lines().forEach(line -> {
                String[] split = line.split(",");
                IPv6Address sip = new IPAddressString(split[0]).getAddress().toIPv6();
                IPv6Address eip = new IPAddressString(split[1]).getAddress().toIPv6();
                for (IPv6Address iPv6Address : new IPv6AddressSeqRange(sip, eip).spanWithPrefixBlocks()) {
                    byte[] bytes = iPv6Address.getBytes();
                    trie.putIfAbsent(ByteArrayUtil.mask2ByteArray(bytes, iPv6Address.getPrefixLength()), split);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PrefixDATrie<String[]> daTrie = new PrefixDATrie<>(trie);
        daTrie.search("2001:503:e239::");
        System.out.println(Arrays.toString(trie.get(ByteArrayUtil.mask2ByteArray(InetAddress.getByName("2001:503:e239::").getAddress(), 48))));
    }

}
