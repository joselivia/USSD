package com.jose.USSD.Controller;
import com.jose.USSD.model.Request;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/ussd")
public class Controller {
          private final Map<String, String> userSessions = new ConcurrentHashMap<>();

        @PostMapping
        public String handleUssd(@RequestBody Request request) {
            String sessionId = request.getSessionId();
            String serviceCode = request.getServiceCode();
            String phoneNumber = request.getPhoneNumber();
            String text = request.getText();

            if (sessionId == null || serviceCode == null || phoneNumber == null) {
                return "END Invalid request. Missing required parameters.";
            }
            String response;
            String userState = userSessions.getOrDefault(sessionId, "");
            switch (userState) {
                case "": // Initial Menu
                    if (text.equals("")) {
                        response = "CON Welcome to My Service\n" +
                                "1. Check Balance\n" +
                                "2. Buy Airtime\n" +
                                "3. Transfer Money\n" +
                                "4. Exit";
                        userSessions.put(sessionId, "main-menu");
                    } else {
                        response = "END Invalid input. Try again.";
                    }
                    break;

                case "main-menu":
                    switch (text) {
                        case "1":
                            response = "CON Check Balance Menu\n" +
                                    "1. Savings Account\n" +
                                    "2. Current Account\n" +
                                    "3. Go Back";
                            userSessions.put(sessionId, "check-balance");
                            break;
                        case "2":
                            response = "CON Enter amount to buy airtime:";
                            userSessions.put(sessionId, "buy-airtime");
                            break;
                        case "3":
                            response = "CON Enter recipient phone number:";
                            userSessions.put(sessionId, "transfer-money");
                            break;
                        case "4":
                            response = "END Thank you for using our service.";
                            userSessions.remove(sessionId);
                            break;
                        default:
                            response = "CON Invalid input. Try again.";
                    }
                    break;

                case "check-balance":
                    switch (text) {
                        case "1":
                            response = "END Your savings account balance is Ksh 1,000.";
                            userSessions.remove(sessionId);
                            break;
                        case "2":
                            response = "END Your current account balance is Ksh 5,000.";
                            userSessions.remove(sessionId);
                            break;
                        case "3":
                            response = "CON Welcome to My Service\n" +
                                    "1. Check Balance\n" +
                                    "2. Buy Airtime\n" +
                                    "3. Transfer Money\n" +
                                    "4. Exit";
                            userSessions.put(sessionId, "main-menu");
                            break;
                        default:
                            response = "CON Invalid input. Try again.\n" +
                                    "1. Savings Account\n" +
                                    "2. Current Account\n" +
                                    "3. Go Back";
                    }
                    break;

                case "buy-airtime":
                    try {
                        int amount = Integer.parseInt(text);
                        response = "END You have successfully bought Ksh " + amount + " airtime.";
                        userSessions.remove(sessionId);
                    } catch (NumberFormatException e) {
                        response = "CON Invalid amount. Enter a valid amount to buy airtime:";
                    }
                    break;

                case "transfer-money":
                    if (text.matches("\\d{10}")) { // Assuming a valid phone number is 10 digits
                        response = "CON Enter amount to transfer:";
                        userSessions.put(sessionId, "transfer-amount:" + text);
                    } else {
                        response = "CON Invalid phone number. Enter recipient phone number:";
                    }
                    break;

                default:
                    if (userState.startsWith("transfer-amount:")) {
                        String recipientPhone = userState.split(":")[1];
                        try {
                            int amount = Integer.parseInt(text);
                            response = "END You have transferred Ksh " + amount + " to " + recipientPhone + ".";
                            userSessions.remove(sessionId);
                        } catch (NumberFormatException e) {
                            response = "CON Invalid amount. Enter a valid amount to transfer:";
                        }
                    } else {
                        response = "END An error occurred. Please try again.";
                        userSessions.remove(sessionId);
                    }
            }

            return response;
        }
    }
