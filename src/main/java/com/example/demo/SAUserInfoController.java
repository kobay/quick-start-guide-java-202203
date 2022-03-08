package com.example.demo;

import com.box.sdk.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

@RestController
public class SAUserInfoController {

    @GetMapping("/service-account")
    public UserDto getServiceAccountInfo() throws IOException {

        // ここでは簡易的にプロジェクトフォルダ直下に置いた、config.jsonを利用します。
        // プロダクション用途では、config.jsonの情報は環境変数等で管理してください。
        Reader reader = new FileReader("config.json");
        BoxConfig boxConfig = BoxConfig.readFrom(reader);
        BoxAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig);

        // Box上のサービスアカウントのユーザー情報を取得します。
        BoxUser user = BoxUser.getCurrentUser(api);
        BoxUser.Info userInfo = user.getInfo();

        // DTOに詰め替えます。
        UserDto userDto = new UserDto();

        userDto.setId(userInfo.getID());
        userDto.setName(userInfo.getName());
        return userDto;
    }
}
