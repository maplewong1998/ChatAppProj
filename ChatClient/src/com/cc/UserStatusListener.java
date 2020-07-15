package com.cc;

public interface UserStatusListener {
    void online(String username);
    void offline(String username);
}
