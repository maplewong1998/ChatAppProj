package com.cc;

public interface UserStatusListener {
    public void online(String username);
    public void offline(String username);
}
