package com.tangrun.mdm.boxwindow.controller;

import com.tangrun.mdm.boxwindow.core.BaseController;
import com.tangrun.mdm.boxwindow.core.BaseUIController;
import com.tangrun.mdm.boxwindow.service.IOService;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class TipUSBDebugGuideController extends BaseUIController {

    public ImageView ivHW;
    public ImageView ivXM;

    @Override
    public void initialLazy() {

    }

    public void onCloseClick(MouseEvent mouseEvent) {
        getContext().popBack();
    }

    public void onUSBVivoClick(MouseEvent mouseEvent) {
        openURL("http://wk.safe-app.cn/usbVivo.html");
    }

    public void onUSBOppoClick(MouseEvent mouseEvent) {
        openURL("http://wk.safe-app.cn/usbOppo.html");
    }

    public void onUSBXiaomiClick(MouseEvent mouseEvent) {
        openURL("http://wk.safe-app.cn/usbXiaomi.html");
    }

    public void onUSBSanxingClick(MouseEvent mouseEvent) {
        openURL("http://wk.safe-app.cn/usbSamsung.html");
    }

    public void onUSBHuaweiClick(MouseEvent mouseEvent) {
        openURL("http://wk.safe-app.cn/usbHuawei.html");
    }

    private void openURL(String url)
    {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(URI.create(url));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            showTipDialog("打开网页 "+url+" 失败\n"+e.getMessage());
        }
    }

}
