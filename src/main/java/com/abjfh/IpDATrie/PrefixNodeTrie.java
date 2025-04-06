package com.abjfh.IpDATrie;


import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class PrefixNodeTrie<V> {
    Node root = new Node(0);
    ArrayList<V> list = new ArrayList<>();

    @Data
    protected static class Node {
        Int2ObjectOpenHashMap<Node> children = new Int2ObjectOpenHashMap<>();
        byte[] shareKeys;
        int depth;
        int tailIndex;

        public Node(int depth) {
            this.depth = depth;
        }

        public boolean isLeaf() {
            return tailIndex < 0;
        }
    }

    public boolean putIfAbsent(byte[] keys, V value) {
        Node currentNode = root;
        int currentDepth = currentNode.depth;
        while (currentDepth < keys.length) {
            int nextDepth;
            Node nextNode = null;
            //检查节点是否有公共路径
            if (currentNode.shareKeys != null) {
                //如果存在公共路径,则children中存储的映射关系相当于shareKeys + key -> childrenNode
                //提取shareKeys与keys[currentDepth,keys.length)的公共部分
                int newShareKeyLength = 0;
                int suffixKeyLength = keys.length - currentDepth;
                int maximumShareKeyLength = Math.min(suffixKeyLength, currentNode.shareKeys.length);
                while (newShareKeyLength < maximumShareKeyLength && currentNode.shareKeys[newShareKeyLength] == keys[newShareKeyLength + currentDepth]) {
                    newShareKeyLength++;
                }

                nextDepth = currentDepth + newShareKeyLength + 1;
                if (newShareKeyLength == currentNode.shareKeys.length) {
                    nextNode = currentNode.children.get(keys[nextDepth - 1]);
                } else if (newShareKeyLength == suffixKeyLength) {
                    //路径keys作为其他路径的前缀，已存在于树中
                    return false;
                } else {
                    //保存新的公共路径到当前节点
                    int originTailIndex = currentNode.tailIndex;
                    Int2ObjectOpenHashMap<Node> originChildren = currentNode.children;
                    byte[] originShareKeys = currentNode.shareKeys;

                    //shareKeys分为三部分shareKeys[0,maxShareKeyLength),shareKeys[maxShareKeyLength],[maxShareKeyLength + 1,shareKeys.length)
                    currentNode.tailIndex = 0;
                    currentNode.children = new Int2ObjectOpenHashMap<>();
                    currentNode.shareKeys = null;
                    if (newShareKeyLength > 0) {
                        byte[] newShareKeys = new byte[newShareKeyLength];
                        System.arraycopy(originShareKeys, 0, newShareKeys, 0, newShareKeyLength);
                        currentNode.shareKeys = newShareKeys;
                    }
                    //开通到达原路径的子节点
                    Node node2OriginPath = new Node(nextDepth);
                    node2OriginPath.tailIndex = originTailIndex;
                    node2OriginPath.children = originChildren;
                    int suffixOriginShareKeyLength = originShareKeys.length - newShareKeyLength - 1;
                    if (suffixOriginShareKeyLength > 0) {
                        //如果还有公共路径
                        byte[] newShareKeys2OriginPath = new byte[suffixOriginShareKeyLength];
                        System.arraycopy(originShareKeys, newShareKeyLength + 1, newShareKeys2OriginPath, 0, suffixOriginShareKeyLength);
                        node2OriginPath.shareKeys = newShareKeys2OriginPath;
                    }
                    currentNode.children.put(originShareKeys[newShareKeyLength], node2OriginPath);
                }
            } else if (currentNode.isLeaf()) {
                // 当前节点没有公共路径且为叶子节点,代表已存在key->keys[0,i]
                return false;
            } else {
                nextNode = currentNode.children.get(keys[currentDepth]);
                nextDepth = currentDepth + 1;
            }
            if (nextNode == null) {
                //如果下一个节点为null,则将后续路径作为公共路径存入下一个节点,结束循环
                nextNode = new Node(nextDepth);
                int shareKeysLength = keys.length - nextDepth;
                if (shareKeysLength > 0) {
                    nextNode.shareKeys = new byte[shareKeysLength];
                    System.arraycopy(keys, nextDepth, nextNode.shareKeys, 0, shareKeysLength);
                }
                list.add(value);
                nextNode.tailIndex = -list.size();
                currentNode.children.put(keys[nextDepth - 1], nextNode);
                return true;
            }
            currentNode = nextNode;
            currentDepth = nextDepth;
        }

        return false;
    }

    public V get(byte[] keys) {
        Node currentNode = root;
        int currentDepth = currentNode.depth;
        while (currentDepth < keys.length) {
            Node nextNode;
            if (currentNode.shareKeys != null) {
                if (keys.length - currentDepth < currentNode.shareKeys.length) {
                    return null;
                } else {
                    for (int j = 0; j < currentNode.shareKeys.length; j++) {
                        if (currentNode.shareKeys[j] != keys[currentDepth + j]) {
                            return null;
                        }
                    }
                    if (currentNode.isLeaf()) {
                        return list.get(-currentNode.tailIndex - 1);
                    }
                    nextNode = currentNode.children.get(keys[currentDepth + currentNode.shareKeys.length]);
                }
            } else if (currentNode.isLeaf()) {
                return list.get(-currentNode.tailIndex - 1);
            } else {
                nextNode = currentNode.children.get(keys[currentDepth]);
            }
            if (nextNode == null) {
                return null;
            }
            currentNode = nextNode;
            currentDepth = currentNode.depth;
        }
        if (currentNode.isLeaf() && currentNode.shareKeys == null) {
            return list.get(-currentNode.tailIndex - 1);
        }
        return null;
    }

}
