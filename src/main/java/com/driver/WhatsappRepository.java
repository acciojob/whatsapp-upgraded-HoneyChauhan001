package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashMap<String,User> mobileUserMap;
    private HashMap<Integer,Message> messageMap;
    private HashMap<String,Group> groupMap;
    private int customGroupCount;
    private int messageId;

    public int getCustomGroupCount() {
        return customGroupCount;
    }

    public void setCustomGroupCount(int customGroupCount) {
        this.customGroupCount = customGroupCount;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.mobileUserMap = new HashMap<>();
        this.messageMap = new HashMap<>();
        this.groupMap = new HashMap<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public void createUser(String mobile, User user) {
        mobileUserMap.put(mobile,user);
    }

    public Optional<User> getUserByMobile(String mobile) {
        if(mobileUserMap.containsKey(mobile)){
            return Optional.of(mobileUserMap.get(mobile));
        }
        return Optional.empty();

    }

    public void createGroup(Group group, List<User> users) {
        groupUserMap.put(group,users);
        adminMap.put(group,users.get(0));
    }

    public void createMessage(Message message) {
        messageMap.put(message.getId(),message);
    }

    public Optional<Group> getGroup(Group group) {
        if(groupUserMap.containsKey(group)){
            return Optional.of(group);
        }
        else return Optional.empty();
    }

    public Optional<User> getUserInGroup(Group group, User sender) {
        for(User user : groupUserMap.get(group)){
            if((user.getMobile()).equals(sender.getMobile())){
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public int sendMessage(Message message, User sender, Group group) {
        int numberOfMessageInGroup;
        if(groupMessageMap.containsKey(group)){
            List<Message> oldList = groupMessageMap.get(group);
            oldList.add(message);
            numberOfMessageInGroup = oldList.size();
            groupMessageMap.put(group,oldList);

        }
        else {
            List<Message> newList = new ArrayList<>();
            newList.add(message);
            numberOfMessageInGroup = newList.size();
            groupMessageMap.put(group,newList);
        }
        senderMap.put(message,sender);
        return numberOfMessageInGroup;

    }

    public Optional<User> getAdmin(Group group) {
        return Optional.of(adminMap.get(group));
    }

    public void changeAdmin(Group group, User user) {
        adminMap.put(group,user);
    }

    public void addGroup(Group group) {
        groupMap.put(group.getName(),group);
    }

    public Optional<Group> getGroupByName(String groupName) {
        if(groupMap.containsKey(groupName)){
            return Optional.of(groupMap.get(groupName));
        }
        else return Optional.empty();
    }
}

