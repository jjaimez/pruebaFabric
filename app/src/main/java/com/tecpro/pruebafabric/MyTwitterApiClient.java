package com.tecpro.pruebafabric;

import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.TwitterApiClient;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by jjaimez on 29/11/15.
 */
public class MyTwitterApiClient extends TwitterApiClient{


    public MyTwitterApiClient(Session session) {
        super(session);
    }

    /**
     * Provide FriendsService with ids
     */
    public FriendsService getFriendsService() {
        return getService(FriendsService.class);
    }

    public FollowersService getFollowersService() {
        return getService(FollowersService.class);
    }

    public FriendshipsDestroyService getFriendshipsDestroyService() {
        return getService(FriendshipsDestroyService.class);
    }

    public FriendshipsCreateService getFriendshipsCreateService() {
        return getService(FriendshipsCreateService.class);
    }
    

    public interface FriendsService {
        @GET("/1.1/friends/ids.json")
        void ids(@Query("user_id") Long userId,
                 @Query("screen_name") String screenName,
                 @Query("cursor") Long cursor,
                 @Query("stringify_ids") Boolean stringifyIds,
                 @Query("count") Integer count,
                 Callback<Ids> cb);

        @GET("/1.1/friends/ids.json")
        void idsByUserId(@Query("user_id") Long userId,
                         Callback<Ids> cb);

        @GET("/1.1/friends/ids.json")
        Ids idsByUserIdCursor(@Query("user_id") Long userId,
                               @Query("cursor") Long cursor);
    }

    public interface FollowersService {
        @GET("/1.1/followers/ids.json")
        void ids(@Query("user_id") Long userId,
                 @Query("screen_name") String screenName,
                 @Query("cursor") Long cursor,
                 @Query("stringify_ids") Boolean stringifyIds,
                 @Query("count") Integer count,
                 Callback<Ids> cb);

        @GET("/1.1/followers/ids.json")
        void idsByUserId(@Query("user_id") Long userId,
                         Callback<Ids> cb);

        @GET("/1.1/followers/ids.json")
        Ids idsByUserIdCursor(@Query("user_id") Long userId,
                               @Query("cursor") Long cursor);
    }

    public interface FriendshipsDestroyService{
        @POST("/1.1/friendships/destroy.json")
        ResponseFriendships friendshipsDestroy(@Query("user_id") Long userId);

    }



    public interface FriendshipsCreateService{
        @POST("/1.1/friendships/create.json")
        ResponseFriendships friendshipsCreate(@Query("user_id") Long userId);

    }


    public class Ids {
        @SerializedName("previous_cursor")
        public final Long previousCursor;

        @SerializedName("ids")
        public final Long[] ids;

        @SerializedName("next_cursor")
        public final Long nextCursor;


        public Ids(Long previousCursor, Long[] ids, Long nextCursor) {
            this.previousCursor = previousCursor;
            this.ids = ids;
            this.nextCursor = nextCursor;
        }
    }


    public class ResponseFriendships{
        @SerializedName("following")
        public final boolean following;

        @SerializedName("name")
        public final String name;

        public ResponseFriendships(boolean following, String name) {
            this.following = following;
            this.name = name;
        }
    }



}
