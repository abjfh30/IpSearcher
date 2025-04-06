package com.abjfh.search.impl;

import inet.ipaddr.IPAddress;

import java.io.Serializable;

public interface IpInfoSearcherImp<V, T extends IPAddress> extends Serializable {

    V seach(T ipAddress);
}
