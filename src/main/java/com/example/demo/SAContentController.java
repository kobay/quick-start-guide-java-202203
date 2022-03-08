package com.example.demo;

import com.box.sdk.*;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Controller

public class SAContentController {

    @GetMapping("/service-account-contents")
    public String getServiceAccountContents(Model model) throws IOException {


        // ここでは簡易的にプロジェクトフォルダ直下に置いた、config.jsonを利用します。
        // プロダクション用途では、config.jsonの情報は環境変数等で管理してください。
        Reader reader = new FileReader("config.json");
        BoxConfig boxConfig = BoxConfig.readFrom(reader);
        BoxAPIConnection api =
                BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig);

        // ルートフォルダを取得します
        BoxFolder folder = BoxFolder.getRootFolder(api);

        // Spring Bootのプロジェクト雛形をダウンロードした際についてきたHELP.md
        // BOXサービスアカウントのルートフォルダにアップロードします。
        // 重複を避けるため、アクセスされるたびにタイムスタンプをつけ、名前をかえてアップロードします。
        try (
                FileInputStream stream = new FileInputStream("HELP.md")) {
            String fileName = String.format("HELP-%s.md", System.currentTimeMillis());
            folder.uploadFile(stream, fileName);
        } catch (IOException ignore) {
            ignore.printStackTrace();
        }

        // コンテンツ情報を値オブジェクトに移し、テンプレートに渡します。
        List<ContentDto> itemList = new ArrayList<>();
        for (BoxItem.Info itemInfo : folder) {
            ContentDto contentDto = new ContentDto();
            contentDto.setId(itemInfo.getID());
            contentDto.setName(itemInfo.getName());
            itemList.add(contentDto);
        }
        model.addAttribute("itemList", itemList);
        return "contents";
    }
}
