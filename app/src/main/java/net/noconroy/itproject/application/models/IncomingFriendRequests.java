package net.noconroy.itproject.application.models;

import java.util.List;

/**
 * Created by matt on 10/10/17.
 */

public class IncomingFriendRequests {
    public class Request {
        public String token;
        public Profile profile;
    }
    public List<Request> requests;
}
