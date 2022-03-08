package com.example.demo;

import com.box.sdk.*;
import com.eclipsesource.json.*;

import java.io.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;

@Controller
public class MyFileController {

    @Autowired
    HttpSession session;

    @GetMapping("/myfile")
    public String register(Model model) throws IOException {
        String appUserID = (String) session.getAttribute("appUserID");
        if (appUserID == null || appUserID.equals("")) {
            return "login";
        } else {

            //AppUserのアクセストークンの発行
            Reader readerAU = new FileReader("config.json");
            BoxConfig boxConfigAU = BoxConfig.readFrom(readerAU);
            BoxAPIConnection apiAU = BoxDeveloperEditionAPIConnection.getAppUserConnection(appUserID, boxConfigAU);
            readerAU.close();
            model.addAttribute("token", apiAU.getAccessToken());
            return "myfile";
        }
    }

    @PostMapping("/myfile")
    public String register(Model model,
                           @RequestParam("name") String name,
                           @RequestParam("password") String password) throws IOException {

        // ログイン処理として、ユーザ名と同じファイルをサービスアカウントのファイルから探し、
        //ファイルに格納されたパスワードが一致するかを検証します。
        // ユーザー情報の保持にはより堅牢な仕組み（DBなど）を使うべきです。
        //ここでは簡易的にBox上のファイルを利用することでユーザー認証をおこなっています。
        if (name == null || name.equals("") || password == null || password.equals("")) {
            return "login";
        }

        //ファイル検索
        Reader reader = new FileReader("config.json");
        BoxConfig boxConfig = BoxConfig.readFrom(reader);
        BoxAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig);
        reader.close();
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        String fileId = null;
        for (BoxItem.Info itemInfo : rootFolder) {
            if (itemInfo instanceof BoxFile.Info && itemInfo.getName().equals(name + ".json")) {
                fileId = itemInfo.getID();
                break;
            }
        }
        if (fileId == null) {
            return "login";
        }

        //ファイルの読み込み
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BoxFile file = new BoxFile(api, fileId);
        file.download(outputStream);
        outputStream.close();
        String jsonString = new String(outputStream.toByteArray());
        JsonObject jsonObj = JsonObject.readFrom(jsonString);
        String regPassword = jsonObj.get("password").asString();

        //パスワードチェック
        if (!regPassword.equals(password)) {
            return "login";
        }

        //AppUserID取得
        JsonObject userJsonObj = jsonObj.get("appUser").asObject();
        String appUserID = userJsonObj.get("id").asString();

        //AppUserのアクセストークンの発行
        Reader readerAU = new FileReader("config.json");
        BoxConfig boxConfigAU = BoxConfig.readFrom(readerAU);
        BoxAPIConnection apiAU = BoxDeveloperEditionAPIConnection.getAppUserConnection(appUserID, boxConfigAU);
        readerAU.close();

        session.setAttribute("appUserID", appUserID);
        model.addAttribute("token", apiAU.getAccessToken());
        return "myfile";
    }
}
