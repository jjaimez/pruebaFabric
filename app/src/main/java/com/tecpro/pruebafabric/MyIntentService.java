package com.tecpro.pruebafabric;

import android.app.IntentService;
import android.content.Intent;

import com.twitter.sdk.android.Twitter;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.services.concurrency.AsyncTask;


public class MyIntentService extends IntentService {


    private List<Long> friends,followers,toDelete,toAdd;
    private Long cursorFriends,cursorFollowers,uid;
    MyTwitterApiClient client = new MyTwitterApiClient(Twitter.getSessionManager().getActiveSession());
    int contFriends, contFollow;
    boolean follow,unfollow,running;
    String text;
    int succesUnfollow, succesFollow;

    public static final String succesFollowString = "succesFollowString";
    public static final String succesUnfollowString = "succesUnfollowString";
    public static final String ACTION_FIN = "com.tecpro.pruebafabric.action.FIN";

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        uid = intent.getLongExtra("uid",0);
        contFriends = 0;
        contFollow = 0;
        friends = new ArrayList<>();
        followers = new ArrayList<>();
        toDelete =  new ArrayList<>();
        toAdd =  new ArrayList<>();
        cursorFriends = new Long("-1");
        cursorFollowers = new Long("-1");
        follow = intent.getBooleanExtra("follow",false);
        unfollow = intent.getBooleanExtra("unfollow",false);
        succesUnfollow = 0;
        succesFollow = 0;
        running = false;
        text = getResources().getString(R.string.actionCompleted)+"\n";
        contFollow++;

        new AsyncCallerFollowers().execute();
    }


    private class AsyncCallerFollowers extends AsyncTask<String, Void, MyTwitterApiClient.Ids> {

        @Override
        protected MyTwitterApiClient.Ids doInBackground(String... params) {
            return client.getFollowersService().idsByUserIdCursor(uid, cursorFollowers);
        }

        @Override
        protected void onPostExecute(MyTwitterApiClient.Ids result) {
            for (Long l : result.ids){
                followers.add(l);
            }
            cursorFollowers = result.nextCursor;
            if (cursorFollowers != 0) {
                if (contFollow % 15 == 0){
                    try {
                        Thread.sleep(900000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                contFollow++;
                new AsyncCallerFollowers().execute();
            } else {
                contFriends++;
                new AsyncCallerFriends().execute();
            }
        }
    }


    private class AsyncCallerFriends extends AsyncTask<String, Void, MyTwitterApiClient.Ids> {


        @Override
        protected MyTwitterApiClient.Ids doInBackground(String... params) {
            return client.getFriendsService().idsByUserIdCursor(uid, cursorFriends);
        }

        @Override
        protected void onPostExecute(MyTwitterApiClient.Ids result) {
            for (Long l : result.ids){
                friends.add(l);
            }
            cursorFriends = result.nextCursor;
            if (cursorFriends != 0) {
                if (contFriends % 15 == 0) {
                    try {
                        Thread.sleep(900000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                contFriends++;
                new AsyncCallerFriends().execute();
            } else {
                toAdd.addAll(followers);
                toAdd.removeAll(friends);
                toDelete.addAll(friends);
                toDelete.removeAll(followers);
                if (unfollow && !toDelete.isEmpty()) {
                        new AsyncCallerDeleteFriend().execute();
                    } else {
                        if (follow && !toAdd.isEmpty()) {
                                new AsyncCallerAddFriend().execute();
                        } else {
                             Intent bcIntent = new Intent();
                            bcIntent.putExtra("succesFollowString",succesFollow);
                            bcIntent.putExtra("succesUnfollow",succesUnfollow);
                            bcIntent.setAction(ACTION_FIN);
                            sendBroadcast(bcIntent);
                        }
                    }
            }
        }
    }


    private class AsyncCallerDeleteFriend extends AsyncTask<String, Void, MyTwitterApiClient.ResponseFriendships> {

        @Override
        protected MyTwitterApiClient.ResponseFriendships doInBackground(String... params) {
            try {
                return client.getFriendshipsDestroyService().friendshipsDestroy(toDelete.get(0));
            } catch (Exception e) {
                try {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e.printStackTrace();
                    }
                    return client.getFriendshipsDestroyService().friendshipsDestroy(toDelete.get(0));
                } catch (Exception e3) {
                    try {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                        return client.getFriendshipsDestroyService().friendshipsDestroy(toDelete.get(0));
                    } catch (Exception e4) {
                        return null;
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(MyTwitterApiClient.ResponseFriendships result) {
            if (result != null){
                succesUnfollow++;
            }
            toDelete.remove(0);
            if (!toDelete.isEmpty()){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                new AsyncCallerDeleteFriend().execute();
            } else {
                if (follow && !toAdd.isEmpty()) {
                    new AsyncCallerAddFriend().execute();
                } else{
                    Intent bcIntent = new Intent();
                    bcIntent.setAction(ACTION_FIN);
                    bcIntent.putExtra("succesFollowString",succesFollow);
                    bcIntent.putExtra("succesUnfollow", succesUnfollow);
                    sendBroadcast(bcIntent);
                }
            }

        }
    }

    private class AsyncCallerAddFriend extends AsyncTask<String, Void, MyTwitterApiClient.ResponseFriendships> {


        @Override
        protected MyTwitterApiClient.ResponseFriendships doInBackground(String... params) {
            try {
                return client.getFriendshipsCreateService().friendshipsCreate(toAdd.get(0));
            } catch (Exception e) {
                return null;
            }
        }


        @Override
        protected void onPostExecute(MyTwitterApiClient.ResponseFriendships result) {
            if (result != null){
                succesFollow++;
            }
            toAdd.remove(0);

            if (!toAdd.isEmpty()){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                new AsyncCallerAddFriend().execute();
            } else {
                Intent bcIntent = new Intent();
                bcIntent.setAction(ACTION_FIN);
                bcIntent.putExtra(succesFollowString,succesFollow);
                bcIntent.putExtra(succesUnfollowString,succesUnfollow);
                sendBroadcast(bcIntent);
            }
        }

    }
}
