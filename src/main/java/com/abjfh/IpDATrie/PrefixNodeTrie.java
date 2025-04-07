package com.abjfh.IpDATrie;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;

public class PrefixNodeTrie<V> {
    Node root;
    ArrayList<V> list = new ArrayList<>();

    @Data
    protected static class Node implements Cloneable {
        Int2ObjectOpenHashMap<Node> children = new Int2ObjectOpenHashMap<>();
        byte[] shareKeys;
        int baseIndex;
        int depth;
        int tailIndex;

        public Node(int depth) {
            this.depth = depth;
        }

        @Override
        protected Node clone() throws CloneNotSupportedException {
            return (Node) super.clone();
        }

        protected boolean isLeaf() {
            return tailIndex > 0;
        }

        protected void clear() {
            children = new Int2ObjectOpenHashMap<>();
            shareKeys = null;
            baseIndex = 0;
            tailIndex = 0;
        }
    }

    public boolean putIfAbsent(byte[] keys, V value) throws CloneNotSupportedException {
        if (root == null) {
            root = new Node(0);
            root.shareKeys = Arrays.copyOf(keys, keys.length);
            list.add(value);
            root.tailIndex = list.size();
            return true;
        }
        Node currentNode = root;
        int keyLength = keys.length;
        while (true) {

            if (currentNode.shareKeys != null) {

                int mismatch = Arrays.mismatch(
                        currentNode.shareKeys, 0, currentNode.shareKeys.length,
                        keys, currentNode.depth, keyLength
                );
                int minLength = Math.min(currentNode.shareKeys.length, keyLength - currentNode.depth);

                int currentKeysIdx = currentNode.depth + mismatch;
                if (mismatch >= 0 && mismatch < minLength) {
                    // 备份当前节点
                    Node cloneOriginNode = currentNode.clone();
                    currentNode.clear();
                    // 将cloneOriginNode添加到currentNode的子节点
                    currentNode.children.put(cloneOriginNode.shareKeys[mismatch], cloneOriginNode);

                    // 调整currentNode的shareKeys
                    if (mismatch > 0) {
                        currentNode.shareKeys = Arrays.copyOfRange(cloneOriginNode.shareKeys, 0, mismatch);
                    }

                    // 调整cloneOriginNode的shareKeys 和 depth
                    if (mismatch + 1 < cloneOriginNode.shareKeys.length) {
                        cloneOriginNode.shareKeys = Arrays.copyOfRange(
                                cloneOriginNode.shareKeys, mismatch + 1, cloneOriginNode.shareKeys.length
                        );
                    } else {
                        cloneOriginNode.shareKeys = null;
                    }
                    cloneOriginNode.depth += mismatch + 1;

                    // 创建新节点处理剩余keys
                    Node nextNode = new Node(cloneOriginNode.depth);
                    if (nextNode.depth < keys.length) {
                        nextNode.shareKeys = Arrays.copyOfRange(keys, nextNode.depth, keyLength);
                    }
                    list.add(value);
                    nextNode.tailIndex = list.size();
                    currentNode.children.put(keys[currentKeysIdx], nextNode);
                    return true;

                } else if (mismatch == currentNode.shareKeys.length) {
                    if (currentNode.isLeaf()) {
                        return false;
                    }

                    // 继续处理后续字节
                    Node nextNode = currentNode.children.get(keys[currentKeysIdx]);
                    if (nextNode == null) {
                        // 创建新节点处理剩余keys
                        nextNode = new Node(currentNode.depth + mismatch + 1);
                        currentNode.children.put(keys[currentKeysIdx], nextNode);
                        if (nextNode.depth < keys.length) {
                            nextNode.shareKeys = Arrays.copyOfRange(keys, nextNode.depth, keyLength);
                        }
                        list.add(value);
                        nextNode.tailIndex = list.size();
                        return true;
                    } else {
                        currentNode = nextNode;
                    }
                } else {
                    // 完全匹配
                    return false;
                }
            } else {
                if (currentNode.isLeaf() || currentNode.depth >= keyLength) {
                    return false;
                }
                // 无shareKeys，继续处理后续字节
                Node nextNode = currentNode.children.get(keys[currentNode.depth]);
                if (nextNode == null) {

                    nextNode = new Node(currentNode.depth + 1);
                    if (nextNode.depth + 1 < keys.length) {
                        nextNode.shareKeys = Arrays.copyOfRange(keys, nextNode.depth + 1, keyLength);
                    }
                    list.add(value);
                    nextNode.tailIndex = list.size();
                    currentNode.children.put(keys[nextNode.depth], nextNode);
                    return true;
                } else {
                    currentNode = nextNode;
                }
            }
        }
    }

    public V get(byte[] keys) {
        if (root == null) return null;
        Node currentNode = root;
        int keyLength = keys.length;
        while (currentNode.depth < keyLength) {
            if (currentNode.shareKeys != null) {
                int mismatch = Arrays.mismatch(
                        currentNode.shareKeys, 0, currentNode.shareKeys.length,
                        keys, currentNode.depth, keyLength
                );

                int currentKeysIdx = currentNode.depth + mismatch;
                if (mismatch == currentNode.shareKeys.length) {
                    if (currentNode.isLeaf()) {
                        return list.get(currentNode.tailIndex - 1);
                    } else {
                        currentNode = currentNode.children.get(keys[currentKeysIdx]);
                        if (currentNode == null) return null;
                    }
                } else return null;
            } else {
                if (currentNode.isLeaf()) {
                    return list.get(currentNode.tailIndex - 1);
                } else {
                    currentNode = currentNode.children.get(keys[currentNode.depth]);
                    if (currentNode == null) return null;
                }
            }
        }
        return null;
    }
}