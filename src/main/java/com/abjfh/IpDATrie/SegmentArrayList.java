package com.abjfh.IpDATrie;

public class SegmentArrayList {

    public static class Segment {
        protected int[] arr = {-1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1};
    }

    private Segment[] segments;

    /**
     * 1.Offset < 0: 结果集索引=-Offset
     * 2.Offset = 0: 当前节点未占用
     * 3.Offset > 0: 下一节点基础偏移量
     * @return Offset
     */


}