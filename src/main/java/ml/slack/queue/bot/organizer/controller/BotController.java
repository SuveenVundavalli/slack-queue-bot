package ml.slack.queue.bot.organizer.controller;

import ml.slack.queue.bot.organizer.db.model.Next;
import ml.slack.queue.bot.organizer.db.model.User;
import ml.slack.queue.bot.organizer.model.RequestPayload;
import ml.slack.queue.bot.organizer.model.ResponsePayload;
import ml.slack.queue.bot.organizer.repository.NextRepository;
import ml.slack.queue.bot.organizer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BotController {
    Logger logger = LoggerFactory.getLogger(BotController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NextRepository nextRepository;

    @PostMapping(path = "/cookie", produces = "application/json")
    public ResponseEntity<ResponsePayload> cookie(RequestPayload requestPayload) {
        logger.debug("RequestPayload: {}", requestPayload);

        String response;
        String[] commands = requestPayload.getText().trim().split(" ", 2);
        for (String command : commands) {
            logger.debug("{}", command);
        }
        if (!(commands.length == 1 || commands.length == 2)) {
            response = "Invalid command!";
        } else {
            if ("add".equals(commands[0])) {
                response = addUser(commands[1]);
            } else if ("remove".equals(commands[0])) {
                response = removeUser(commands[1]);
            } else if ("next".equals(commands[0])) {
                response = nextUser();
            } else if ("line".equals(commands[0])) {
                response = userLine();
            } else if ("skip".equals(commands[0]) || "done".equals(commands[0])) {
                response = changeNextUser();
            } else if ("help".equals(commands[0])) {
                response = returnHelpText(requestPayload.getUser_id(), requestPayload.getUser_name());
            } else {
                response = "Invalid command!";
            }
        }

        ResponsePayload responsePayload = new ResponsePayload(response);
        logger.debug("responsePayload: {}", responsePayload);
        return new ResponseEntity<>(responsePayload, HttpStatus.OK);
    }

    private String returnHelpText(String user_id, String user_name) {

        return String.format("Hi, <@%s|%s>, Try using `/cookie [add, remove, next, line, skip] [@user]`", user_id, user_name);
    }

    private String changeNextUser() {
        Next next = nextRepository.findAll().get(0);
        User currentUser = next.getUser();
        List<User> allUsers = userRepository.findAll();
        logger.debug("allUsers: {}", allUsers);
        logger.debug("currentUser: {}", currentUser);
        Integer indexCounter = 0;
        for (User user : allUsers) {
            if (user == currentUser) {
                break;
            }
            indexCounter++;
        }
        if (indexCounter < allUsers.size()) {
            Integer nextUserIndex = (indexCounter + 1) % allUsers.size();
            logger.debug("CurrentUserIndex: {}", indexCounter);
            logger.debug("nextUserIndex: {}", nextUserIndex);
            next.setUser(allUsers.get(nextUserIndex));
            nextRepository.save(next);
            return nextUser();
        }
        return "Sorry, failed to set next user!";
    }

    private String addUser(String userCode) {
        try {

            String[] processedUser = processUserCode(userCode);
            if (processedUser == null) {
                return "Invalid user: " + userCode;
            }

            User existingUser = userRepository.findByUserId(processedUser[0]);
            if (existingUser != null) {
                return userCode + " is already in club!";
            }
            User user = new User();
            user.setUserId(processedUser[0]);
            user.setUserName(processedUser[1]);
            user = userRepository.save(user);
            if (userRepository.count() == 1 && nextRepository.count() == 0) {
                Next next = new Next();
                next.setPosition(1l);
                next.setUser(user);
                nextRepository.save(next);
            }
            return "Added " + userCode;
        } catch (Exception e) {
            return "Error adding user";
        }
    }


    private String removeUser(String userCode) {
        String[] processedUser = processUserCode(userCode);
        if (processedUser == null) {
            return "Invalid user: " + userCode;
        }
        User existingUser = userRepository.findByUserId(processedUser[0]);
        if (existingUser == null) {
            return userCode + " is not in the club!";
        }
        if (nextRepository.findAll().get(0).getUser() == existingUser) {
            return "Sorry, " + userCode + " cannot leave as they are next in queue!";
        }
        userRepository.delete(existingUser);
        return userCode + " removed!";
    }

    private String nextUser() {
        User user = nextRepository.findAll().get(0).getUser();
        return String.format("It's <%s|%s> turn now!", user.getUserId(), user.getUserName());
    }

    private String userLine() {
        List<User> allUsers = userRepository.findAll();
        Next nextUser = nextRepository.findAll().get(0);
        Integer nextUserIndex = allUsers.indexOf(nextUser.getUser());
        int userCount = allUsers.size();
        int toIndex = (nextUserIndex + 2) % userCount;
        List<User> nextInLineUsers;
        if (userCount <= 3) {
            nextInLineUsers = allUsers.subList(nextUserIndex, userCount);
            nextInLineUsers.addAll(allUsers.subList(0, nextUserIndex));
        } else {
            if (toIndex > nextUserIndex && toIndex + 1 <= userCount) {
                logger.debug("triggered if");
                nextInLineUsers = allUsers.subList(nextUserIndex, toIndex + 1);
            } else {
                logger.debug("triggered else");
                nextInLineUsers = allUsers.subList(nextUserIndex, userCount);
                nextInLineUsers.addAll(allUsers.subList(0, toIndex + 1));
            }
            logger.debug("toIndex: {}, \nnextUserIndex: {}, \nallUsersCount: {}", toIndex, nextUserIndex, userCount);
        }
        logger.debug("nextInLineUsers: {}", nextInLineUsers);
        boolean first = true;
        String users = "";
        for (User user : nextInLineUsers) {
            if (!first) {
                users += ", ";
            }
            users += "<" + user.getUserId() + "|" + user.getUserName() + ">";
            first = false;
        }

        return "Next in queue are " + users;
    }

    private String[] processUserCode(String userCode) {
        if (!(userCode.startsWith("<") && userCode.endsWith(">"))) {
            return null;
        }
        String userId = userCode.substring(userCode.indexOf("<") + 1, userCode.indexOf("|"));
        String userName = userCode.substring(userCode.indexOf("|") + 1, userCode.indexOf(">"));

        logger.debug("userCode:{} -> userId: {}, userName: {}", userCode, userId, userName);
        return new String[]{userId, userName};
    }
}
