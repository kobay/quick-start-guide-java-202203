package com.example.demo;

import com.box.sdk.*;
import com.eclipsesource.json.JsonObject;

import java.io.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;

@Controller
public class RegisterController {

    @Autowired
    HttpSession session;

    @GetMapping("/register")
    public String register(Model model) {
        return "regform";
    }

    @PostMapping("/register")
    public String register(Model model,
                           @RequestParam("name") String name,
                           @RequestParam("password") String password) throws IOException {

        // ユーザー登録として、サービスアカウントのコンテンツにユーザー情報とパスワードを
        //格納したファイルを保存します。
        // ユーザー情報の保持にはより堅牢な仕組み（DBなど）を使うべきです。
        //ここでは簡易的にBox上のファイルを利用することでユーザー情報の保存をおこなっています。
        if (name == null || name.equals("") || password == null || password.equals("")) {
            return "regform";
        }

        //AppUser作成
        Reader reader = new FileReader("config.json");
        BoxConfig boxConfig = BoxConfig.readFrom(reader);
        BoxAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig);
        CreateUserParams params = new CreateUserParams();
        params.setSpaceAmount(1000000000);
        BoxUser.Info userInfo = BoxUser.createAppUser(api, name, params);

        // ファイル書き込み用データの準備
        JsonObject userJsonObj = new JsonObject();
        userJsonObj.set("type", "user");
        userJsonObj.set("id", userInfo.getID());
        userJsonObj.set("name", userInfo.getName());
        userJsonObj.set("login", userInfo.getLogin());
        JsonObject jsonObj = new JsonObject();
        jsonObj.set("appUser", userJsonObj);
        jsonObj.set("name", name);
        jsonObj.set("password", password);
        InputStream inputStream = new ByteArrayInputStream(jsonObj.toString().getBytes("UTF-8"));

        //ファイルアップロード
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        BoxFile.Info newFileInfo = rootFolder.uploadFile(inputStream, name + ".json");
        inputStream.close();

        //セッション書き込み
        session.setAttribute("appUserID", userInfo.getID());
        model.addAttribute("name", name);
        return "regcomplete";
    }
}
