package controllers;

import auth.Authenticator;
import auth.KuMailAuth;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Auth;

public class AuthController extends Controller {
    public static Result login(){
        JsonNode body = request().body().asJson();
        String username = body.findPath("username").textValue();
        String password = body.findPath("password").textValue();

        Authenticator auth = new KuMailAuth();
        Authenticator.AuthenticatorUser result = auth.auth(username, password);

        User user = null;
        if(result != Authenticator.INVALID){
            user = User.find.where().eq("username", result.getUsername()).findUnique();
            if(user == null){
                // TODO: User shouldn't be created here. Remove this code when backoffice can create users in bulk
                user = new User();
                user.username = result.getUsername();
                user.type = User.TYPES.VOTER;
                Ebean.save(user);
            }

            session("user", String.valueOf(user.id));
        }else{
            return forbidden(buildUser(user));
        }

        return ok(buildUser(user));
    }

    public static Result check(){
        return ok(buildUser(Auth.getUser()));
    }

    private static ObjectNode buildUser(User user){
        ObjectNode out = Json.newObject();

        if(user == null){
            out.putNull("username");
        }else{
            out = (ObjectNode) Json.toJson(user);
            out.put("type_code", user.type.ordinal());
        }

        return out;
    }
}