// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ru.magnit.demo.controllers;

import com.microsoft.aad.msal4j.*;
import com.nimbusds.jwt.JWTParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.magnit.demo.auth.AuthHelper;
import ru.magnit.demo.auth.HttpClientHelper;
import ru.magnit.demo.auth.SessionManagementHelper;
import ru.magnit.demo.dto.Response;
import ru.magnit.demo.dto.ResponseStatus;
import ru.magnit.demo.entity.Status;
import ru.magnit.demo.entity.User;
import ru.magnit.demo.service.PhoneNumberService;
import ru.magnit.demo.service.UserService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Controller exposing application endpoints
 */
@Controller
public class AuthPageController {

    private final String USER_GROUP_ID = "9c4f6fd3-0a58-491b-8af1-d0683d09edab";
    private final String ADMIN_GROUP_ID = "861afe35-712d-456b-9642-cb853d236fe1";
    private final String MODERATOR_GROUP_ID = "0abd53ed-f139-42df-8f2d-98eec18c280c";
    private final String CONTINUE_PAGE = "http://localhost:8080/continue";
    private final String ERROR_PAGE = "http://localhost:8080/error";
    private final String START_PAGE = "http://localhost:8080/";
    private final String LK_PAGE = "http://localhost:8080/lk";

    @Autowired
    AuthHelper authHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private PhoneNumberService phoneNumberService;

    @GetMapping("/msal4jsample")
    public String homepage(){
        return "index";
    }

    @GetMapping("/msal4jsample/sign_out")
    public void signOut(HttpServletRequest httpRequest, HttpServletResponse response) throws IOException {

        httpRequest.getSession().invalidate();

        String endSessionEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/logout";

        //TODO убрать, так как мне возвращать это не нужно ИЛИ заменить на редирект на исходную страницу
//        String redirectUrl = "https://localhost:8443/msal4jsample/";

        response.sendRedirect(endSessionEndpoint + "?post_logout_redirect_uri=" +
                URLEncoder.encode(START_PAGE, "UTF-8"));
    }


    @GetMapping("/msal4jsample/secure/aad")
    public ModelAndView securePage(HttpServletRequest httpRequest, HttpServletResponse httpServletResponse) throws ParseException {
//        ModelAndView mav = new ModelAndView("auth_page");

//        setAccountInfo(mav, httpRequest);
        return new ModelAndView("redirect:" + CONTINUE_PAGE);
//        return mav;
    }

    @GetMapping("/msal4jsample/graph/me")
    public ModelAndView getUserFromGraph(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws Throwable {

        IAuthenticationResult result;
        ModelAndView mav;
        try {
            result = authHelper.getAuthResultBySilentFlow(httpRequest, httpResponse);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof MsalInteractionRequiredException) {

                // If silent call returns MsalInteractionRequired, then redirect to Authorization endpoint
                // so user can consent to new scopes
                String state = UUID.randomUUID().toString();
                String nonce = UUID.randomUUID().toString();

                SessionManagementHelper.storeStateAndNonceInSession(httpRequest.getSession(), state, nonce);
                String authorizationCodeUrl = authHelper.getAuthorizationCodeUrl(
                        httpRequest.getParameter("claims"),
                        "User.Read",
                        authHelper.getRedirectUriGraph(),
                        state,
                        nonce);

                System.out.println("111111111");
                return new ModelAndView("redirect:" + authorizationCodeUrl);
            } else {
//                mav = new ModelAndView("error");
                mav = new ModelAndView("redirect:" + ERROR_PAGE);
                mav.addObject("error", e);
                return mav;
            }
        }

        if (result == null) {
//            mav = new ModelAndView("error");
            mav = new ModelAndView("redirect:" + ERROR_PAGE);
            mav.addObject("error", new Exception("AuthenticationResult not found in session."));
        } else {
            try {
//                mav.addObject("userInfo", getUserRole(result.accessToken()));
                JSONObject object = getUser(result.accessToken());

                try{
                    addUserToDB(object);
                }catch (Exception e){
                    e.printStackTrace();
                }

                //TODO можно обойтись без куки
                Cookie cookie1 = new Cookie("email", object.getString("userPrincipalName"));
                cookie1.setMaxAge(-1);
                httpResponse.addCookie(cookie1);

                //TODO redirect using role
//                mav = new ModelAndView("auth_page");
                mav = new ModelAndView("redirect:" + LK_PAGE);
//                setAccountInfo(mav, httpRequest);

                mav.addObject("userInfo", object.toString());

                return mav;
            } catch (Exception e) {
//                mav = new ModelAndView("error");
                mav = new ModelAndView("redirect:" + ERROR_PAGE);
                mav.addObject("error", e);
            }
        }
        return mav;
    }

    //TODO мб сделать Response
    private void addUserToDB (JSONObject jsonUser){
        String email = jsonUser.getString("userPrincipalName");
        if(userService.getUserByEmail(email).isPresent())
            return;

        User user = new User();
        user.setEmail(email);

        try {
            user.setFirst_name(jsonUser.getString("displayName"));
        }catch (Exception ignored){}

        try {
            user.setLast_name(jsonUser.getString("surname"));
        }catch (Exception ignored){}

        try {
            user.setPost(jsonUser.getString("jobTitle"));
        }catch (Exception ignored){}


        //TODO set mobile phone

        Status status = new Status();
        status.setStatus_name(jsonUser.getString("role"));
        status.setStatus(jsonUser.getInt("roleCode"));
        user.setStatus(status);

        userService.addUser(user);
    }

    private JSONObject getUser(String accessToken) throws Exception{
        JSONObject firstObject = new JSONObject(getUserInfoFromGraph(accessToken));
        JSONObject secondObject = new JSONObject(getUserRole(accessToken));
        //...

        String role = "";
        int roleCode = 0;
        //Check user group
        //TODO может быть польователь не относится ни к какой группе, тогда можно делать выход
        String value = secondObject.getJSONArray("value").getString(0);
        switch (value){
            case USER_GROUP_ID:
                role = "user";
                roleCode = 1;
                break;
            case ADMIN_GROUP_ID:
                role = "admin";
                roleCode = 2;
                break;
            case MODERATOR_GROUP_ID:
                role = "moderator";
                roleCode = 3;
                break;
        }

        return firstObject.put("role", role).put("roleCode", roleCode);
    }

    private String getUserInfoFromGraph(String accessToken) throws Exception {
        // Microsoft Graph user endpoint
        URL url = new URL(authHelper.getMsGraphEndpointHost() + "v1.0/me");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set the appropriate header fields in the request header.
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        String response = HttpClientHelper.getResponseStringFromConn(conn);

        int responseCode = conn.getResponseCode();
        if(responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException(response);
        }

//        JSONObject responseObject = HttpClientHelper.processResponse(responseCode, response);
        return response;
    }

    private String getUserRole(String accessToken) throws Exception {
        // Microsoft Graph user endpoint
        URL url = new URL(authHelper.getMsGraphEndpointHost() + "v1.0/me/checkMemberObjects");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set the appropriate header fields in the request header.
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        JSONObject object = new JSONObject();
        String[] mass = new String[3];
        mass[0] = USER_GROUP_ID;//users
        mass[1] = ADMIN_GROUP_ID;//admins
        mass[2] = MODERATOR_GROUP_ID;//moderators

        object.put("ids", mass);

        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = object.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        String response = HttpClientHelper.getResponseStringFromConn(conn);

        int responseCode = conn.getResponseCode();
        if(responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException(response);
        }

//        JSONObject responseObject = HttpClientHelper.processResponse(responseCode, response);
        return response;
    }

//    private String getUserPhoto(String accessToken) throws Exception {
//        // Microsoft Graph user endpoint
//        System.out.println("access Token = " + accessToken);
//        URL url = new URL(authHelper.getMsGraphEndpointHost() + "v1.0/me/checkMemberObjects");
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//        // Set the appropriate header fields in the request header.
//        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
//        conn.setRequestProperty("Accept", "application/json");
//        conn.setRequestProperty("Content-type", "application/json");
//        conn.setDoOutput(true);
//        conn.setRequestMethod("POST");
//
//        JSONObject object = new JSONObject();
//        String[] mass = new String[3];
//        mass[0] = "9c4f6fd3-0a58-491b-8af1-d0683d09edab";//users
//        mass[1] = "861afe35-712d-456b-9642-cb853d236fe1";//admins
//        mass[2] = "0abd53ed-f139-42df-8f2d-98eec18c280c";//moderators
//
//        object.put("ids", mass);
//
//        try(OutputStream os = conn.getOutputStream()) {
//            byte[] input = object.toString().getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }
//
//        String response = HttpClientHelper.getResponseStringFromConn(conn);
//
//        int responseCode = conn.getResponseCode();
//        if(responseCode != HttpURLConnection.HTTP_OK) {
//            throw new IOException(response);
//        }
//
//        JSONObject responseObject = HttpClientHelper.processResponse(responseCode, response);
//        return responseObject.toString();
//    }

    private void setAccountInfo(ModelAndView model, HttpServletRequest httpRequest) throws ParseException {
        IAuthenticationResult auth = SessionManagementHelper.getAuthSessionObject(httpRequest);

        String tenantId = JWTParser.parse(auth.idToken()).getJWTClaimsSet().getStringClaim("tid");

        model.addObject("tenantId", tenantId);
        model.addObject("account", SessionManagementHelper.getAuthSessionObject(httpRequest).account());
    }
}
