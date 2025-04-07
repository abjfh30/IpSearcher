package com.abjfh.IpDATrie;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
public class PrefixNodeTrie<V> {
    Node root = new Node(0);
    ArrayList<V> list = new ArrayList<>();

    @Data
    protected static class Node {
        Int2ObjectOpenHashMap<Node> children = new Int2ObjectOpenHashMap<>();
        byte[] shareKeys;
        int baseIndex;
        int depth;
        int tailIndex;
        boolean isLeaf; // 内联叶子节点状态

        public Node(int depth) {
            this.depth = depth;
            this.isLeaf = false;
        }

        public void setTailIndex(int tailIndex) {
            this.tailIndex = tailIndex;
            this.isLeaf = tailIndex < 0;
        }
    }

    public boolean putIfAbsent(byte[] keys, V value) {
        Node currentNode = root;
        while (currentNode.depth < keys.length) {
            if (currentNode.shareKeys != null) {
                int mismatch = Arrays.mismatch(currentNode.shareKeys, 0, currentNode.shareKeys.length, keys, currentNode.depth, keys.length);
                if (mismatch == -1) {
                    return false;
                }
                int nextDepth = mismatch + currentNode.depth;

                if (nextDepth >= keys.length) {
                    return false;
                } else if (nextDepth >= currentNode.shareKeys.length) {
                    Node node = currentNode.children.get(nextDepth);
                    if (node == null) {
                        node = new Node(nextDepth);
                        node.isLeaf = true;
                        if (keys.length - 1 != nextDepth) {
                            node.shareKeys = Arrays.copyOfRange(keys, nextDepth, keys.length);
                        }
                        list.add(value);
                        node.tailIndex = -list.size();
                        currentNode.children.put(keys[nextDepth], node);
                        return true;
                    }
                } else {
                    splitShareKeys(currentNode, nextDepth);
                    Node node = new Node(nextDepth);
                    node.isLeaf = true;
                    if (keys.length - 1 != nextDepth) {
                        node.shareKeys = Arrays.copyOfRange(keys, nextDepth, keys.length);
                    }
                    list.add(value);
                    node.tailIndex = -list.size();
                    currentNode.children.put(keys[nextDepth], node);
                    return true;
                }

            } else if (currentNode.children.isEmpty()) {
                currentNode.shareKeys = keys;
                currentNode.isLeaf = true;
                list.add(value);
                currentNode.tailIndex = -list.size();
                return true;
            }
        }
        return false;
    }

    private void splitShareKeys(Node currentNode, int newShareKeyLength) {
        int originTailIndex = currentNode.tailIndex;
        byte[] originShareKeys = currentNode.shareKeys;
        Int2ObjectOpenHashMap<Node> originChildren = currentNode.children;

        currentNode.children = new Int2ObjectOpenHashMap<>();
        currentNode.shareKeys = null;
        currentNode.setTailIndex(0);

        if (newShareKeyLength > 0) {
            currentNode.shareKeys = Arrays.copyOfRange(originShareKeys, 0, newShareKeyLength);
        }

        Node node2OriginPath = new Node(currentNode.depth + newShareKeyLength);
        node2OriginPath.setTailIndex(originTailIndex);
        node2OriginPath.children = originChildren;

        int suffixOriginShareKeyLength = originShareKeys.length - newShareKeyLength - 1;
        if (suffixOriginShareKeyLength > 0) {
            node2OriginPath.shareKeys = Arrays.copyOfRange(
                    originShareKeys,
                    newShareKeyLength + 1,
                    originShareKeys.length
            );
        }

        currentNode.children.put(originShareKeys[newShareKeyLength], node2OriginPath);
    }

    public V get(byte[] keys) {
        Node currentNode = root;
        int nextDepth;
        while (currentNode.depth < keys.length) {
            if (currentNode.shareKeys != null) {
                int mismatch = Arrays.mismatch(currentNode.shareKeys, 0, currentNode.shareKeys.length, keys, currentNode.depth, keys.length);
                nextDepth = mismatch + currentNode.depth;
                if (mismatch == currentNode.shareKeys.length) {
                    if (currentNode.isLeaf) {
                        return list.get(-currentNode.tailIndex - 1);
                    }
                }
            } else if (currentNode.isLeaf) {
                return list.get(-currentNode.tailIndex - 1);
            } else {
                nextDepth = currentNode.depth + 1;
            }
            if (nextDepth >= keys.length) {
                return null;
            }
            currentNode = currentNode.children.get(keys[nextDepth]);
            if (currentNode == null) {
                return null;
            }
        }

        return null;
    }

}