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
import ru.magnit.demo.service.PhoneNumberService;
import ru.magnit.demo.service.UserService;

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

    @GetMapping("/msal4jsample/secure/aad")
    public ModelAndView securePage(HttpServletRequest httpRequest) throws ParseException {
        //на данный урл стоит фильтр => если зашли внутрь метода, то уже авторизовались
        //TODO не возвращать страницу, а возвращать SUCCESS если пользователь успешно авторизован
        //TODO + заполнять куки таким образом, чтобы было понятно, какая у пользователя роль и email
        //TODO + вызывать /msal4jsample/graph/me для заполнения БД

        ModelAndView mav = new ModelAndView("auth_page");

        setAccountInfo(mav, httpRequest);

        return mav;
    }

    @GetMapping("/msal4jsample/sign_out")
    public void signOut(HttpServletRequest httpRequest, HttpServletResponse response) throws IOException {

        httpRequest.getSession().invalidate();

        String endSessionEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/logout";

        //TODO убрать, так как мне возвращать это не нужно ИЛИ заменить на редирект на исходную страницу
        String redirectUrl = "https://localhost:8443/msal4jsample/";
        response.sendRedirect(endSessionEndpoint + "?post_logout_redirect_uri=" +
                URLEncoder.encode(redirectUrl, "UTF-8"));
    }

    //TODO убрать mapping
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

                return new ModelAndView("redirect:" + authorizationCodeUrl);
            } else {

                mav = new ModelAndView("error");
                mav.addObject("error", e);
                return mav;
            }
        }

        if (result == null) {
            mav = new ModelAndView("error");
            mav.addObject("error", new Exception("AuthenticationResult not found in session."));
        } else {
            mav = new ModelAndView("auth_page");
            setAccountInfo(mav, httpRequest);

            try {
                mav.addObject("userInfo", getUserInfoFromGraph(result.accessToken()));

                return mav;
            } catch (Exception e) {
                mav = new ModelAndView("error");
                mav.addObject("error", e);
            }
        }
        return mav;
    }

    private String getUserInfoFromGraph(String accessToken) throws Exception {
        // Microsoft Graph user endpoint
        System.out.println("access Token = " + accessToken);
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

        JSONObject responseObject = HttpClientHelper.processResponse(responseCode, response);
        return responseObject.toString();
    }

    private String getUserRole(String accessToken) throws Exception {
        // Microsoft Graph user endpoint
        System.out.println("access Token = " + accessToken);
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
        mass[0] = "9c4f6fd3-0a58-491b-8af1-d0683d09edab";//users
        mass[1] = "861afe35-712d-456b-9642-cb853d236fe1";//admins
        mass[2] = "0abd53ed-f139-42df-8f2d-98eec18c280c";//moderators

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

        JSONObject responseObject = HttpClientHelper.processResponse(responseCode, response);
        return responseObject.toString();
    }

    private String getUserPhoto(String accessToken) throws Exception {
        // Microsoft Graph user endpoint
        System.out.println("access Token = " + accessToken);
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
        mass[0] = "9c4f6fd3-0a58-491b-8af1-d0683d09edab";//users
        mass[1] = "861afe35-712d-456b-9642-cb853d236fe1";//admins
        mass[2] = "0abd53ed-f139-42df-8f2d-98eec18c280c";//moderators

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

        JSONObject responseObject = HttpClientHelper.processResponse(responseCode, response);
        return responseObject.toString();
    }

    private void setAccountInfo(ModelAndView model, HttpServletRequest httpRequest) throws ParseException {
        IAuthenticationResult auth = SessionManagementHelper.getAuthSessionObject(httpRequest);

        String tenantId = JWTParser.parse(auth.idToken()).getJWTClaimsSet().getStringClaim("tid");

        model.addObject("tenantId", tenantId);
        model.addObject("account", SessionManagementHelper.getAuthSessionObject(httpRequest).account());
    }
}
