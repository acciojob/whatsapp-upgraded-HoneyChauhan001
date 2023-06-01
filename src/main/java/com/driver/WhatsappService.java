package com.driver;

import org.apache.catalina.authenticator.SavedRequest;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class WhatsappService {

    WhatsappRepository whatsappRepository = new WhatsappRepository();
    public String createUser(String name, String mobile) throws Exception {
        Optional<User> userOpt = whatsappRepository.getUserByMobile(mobile);
        if(userOpt.isPresent()){
            throw new Exception("User already exists");
        }
        else {
            User user = new User(name,mobile);
            whatsappRepository.createUser(mobile,user);
            return "SUCCESS";
        }
    }

    public Group createGroup(List<User> users) {
        int numberOfParticipants = users.size();
        Group group;
        if(numberOfParticipants == 2){
            group = new Group(users.get(1).getName(),numberOfParticipants);
        }
        else {
            int groupCount = whatsappRepository.getCustomGroupCount() + 1;
            whatsappRepository.setCustomGroupCount(groupCount);
            String groupName = "Group " + groupCount;
            group = new Group(groupName,numberOfParticipants);
        }
        whatsappRepository.createGroup(group,users);
        whatsappRepository.addGroup(group);
        return group;

    }

    public int createMessage(String content) {
        int messageId = whatsappRepository.getMessageId() + 1;
        whatsappRepository.setMessageId(messageId);
        Message message = new Message(messageId,content);
        whatsappRepository.createMessage(message);
        return messageId;

    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        String groupName = group.getName();
        Optional<Group> groupOpt = whatsappRepository.getGroupByName(groupName);
        if(groupOpt.isEmpty()){
            throw new Exception("Group does not exist");
        }
        else {
            Optional<User> userOpt = whatsappRepository.getUserInGroup(groupOpt.get(),sender);
            if(userOpt.isEmpty()){
                throw new Exception("You are not allowed to send message");
            }
            else {
                return whatsappRepository.sendMessage(message,userOpt.get(),groupOpt.get());
            }
        }
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        Optional<Group> groupOpt = whatsappRepository.getGroup(group);
        if(groupOpt.isEmpty()){
            throw new Exception("Group does not exist");
        }
        else {
            Optional<User> adminOpt = whatsappRepository.getAdmin(group);
            if(!(adminOpt.get()).equals(approver)){
                throw new Exception("Approver does not have rights");
            }
            else {
                Optional<User> userOpt = whatsappRepository.getUserInGroup(group,user);
                if(userOpt.isEmpty()){
                    throw new Exception("User is not a participant");
                }
                else {
                    whatsappRepository.changeAdmin(group,user);
                    return "SUCCESS";
                }
            }
        }
    }

    public int removeUser(User user) throws Exception {
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)

        Optional<Group> groupOpt = whatsappRepository.getUserGroup(user);
        if(groupOpt.isEmpty()){
            throw new Exception("User not found");
        }
        Group group = groupOpt.get();

        User admin = whatsappRepository.getAdmin(group).get();

        if(admin.equals(user)){
            throw new Exception("Cannot remove admin");
        }

        whatsappRepository.removeUserFromGroup(group,user);
        HashMap<Message,User> senderMap = whatsappRepository.getSenderMap();
        List<Message> userMessages = new ArrayList<>();

        for(Message message : senderMap.keySet()){
            if(senderMap.get(message).equals(user)){
                userMessages.add(message);
            }
        }

        for(Message message : userMessages){
            whatsappRepository.deleteMessage(message,group,user);
        }

        int ans = 0;
        ans += whatsappRepository.getusersInGroup(group).size();
        ans += whatsappRepository.getMessagesInGroup(group).size();
        ans += whatsappRepository.getMessagesCount();
        return ans;

    }
}

