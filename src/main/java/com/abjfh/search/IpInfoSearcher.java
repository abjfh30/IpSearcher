package com.abjfh.search;

import inet.ipaddr.IPAddress;

public class IpInfoSearcher<T> {
    Ipv6IpInfoSearcher<T> ipv6Searcher;
    Ipv4IpInfoSearcher<T> ipv4Searcher;

    public T seach(IPAddress ipAddress) {
        if (ipAddress == null) {
            return null;
        } else if (ipAddress.isIPv4()) {
            return ipv4Searcher.seach(ipAddress.toIPv4());
        } else if (ipAddress.isIPv6()) {
            return ipv6Searcher.seach(ipAddress.toIPv6());
        }
        return null;
    }


}
