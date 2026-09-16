package com.example.springboot.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.exception.ServiceException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${ip:localhost}")
    String ip;

    @Value("${server.port}")
    String port;

    private static final String ROOT_PATH =  System.getProperty("user.dir") + File.separator + "files";

    /** 上传扩展名白名单：图片 + 富文本用视频；其余一律拒绝（svg 可携带脚本，同样禁止） */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "mp4", "webm");

    /** 扩展名白名单校验，不合法直接抛业务异常 */
    private void checkExtension(String originalFilename) {
        String extName = FileUtil.extName(originalFilename);
        if (extName == null || !ALLOWED_EXTENSIONS.contains(extName.toLowerCase(Locale.ROOT))) {
            throw new ServiceException("不支持的文件类型，仅允许图片与视频文件");
        }
    }

    /** 下载路径规范化 + 白名单校验（应用自身兜底，不再单纯依赖容器对编码斜杠的拒绝） */
    private File resolveSafe(String fileName) throws IOException {
        File root = new File(ROOT_PATH).getCanonicalFile();
        File target = new File(root, fileName).getCanonicalFile();
        if (!target.toPath().startsWith(root.toPath())) {
            throw new ServiceException("非法路径");
        }
        return target;
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws IOException {
        // 文件的原始名称 ps：aaa.png
        String originalFilename = file.getOriginalFilename();
        checkExtension(originalFilename);
        // aaa
        String mainName = FileUtil.mainName(originalFilename);
        // png
        String extName = FileUtil.extName(originalFilename);
        if (!FileUtil.exist(ROOT_PATH)) {
            // 如果当前文件的父级目录不存在，就创建
            FileUtil.mkdir(ROOT_PATH);
        }
        // 如果当前上传的文件已经存在了，那么这个时候我就要重名一个文件名称
        if (FileUtil.exist(ROOT_PATH + File.separator + originalFilename)) {
            originalFilename = System.currentTimeMillis() + "_" + mainName + "." + extName;
        }
        File saveFile = resolveSafe(originalFilename);
        // 存储文件到本地的磁盘里面去
        file.transferTo(saveFile);
        String url = "http://" + ip + ":" + port + "/file/download/" + originalFilename;
        return Result.success(url);
    }

    /**
     * 下载文件
     */
    @AuthAccess
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        // 附件下载
        //response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        // 预览
        response.addHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        File target = resolveSafe(fileName);
        if (!FileUtil.exist(target)) {
            return;
        }
        byte[] bytes = FileUtil.readBytes(target);
        ServletOutputStream outputStream = response.getOutputStream();
        try {
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 富文本上传图片
     */
    @PostMapping("/editor/upload")
    public Dict editorUpload(MultipartFile file) throws IOException {
        // 文件的原始名称 aaa.png
        String originalFilename = file.getOriginalFilename();
        checkExtension(originalFilename);
        // aaa
        String mainName = FileUtil.mainName(originalFilename);
        // png
        String extName = FileUtil.extName(originalFilename);
        if (!FileUtil.exist(ROOT_PATH)) {
            // 如果当前文件的父级目录不存在，就创建
            FileUtil.mkdir(ROOT_PATH);
        }
        // 如果当前上传的文件已经存在了，那么这个时候我就要重名一个文件名称
        if (FileUtil.exist(ROOT_PATH + File.separator + originalFilename)) {
            originalFilename = System.currentTimeMillis() + "_" + mainName + "." + extName;
        }
        File saveFile = new File(ROOT_PATH + File.separator + originalFilename);
        // 存储文件到本地的磁盘里面去
        file.transferTo(saveFile);
        String url = "http://" + ip + ":" + port + "/file/download/" + originalFilename;

        Dict dict = Dict.create().set("errno", 0).set("data", CollUtil.newArrayList(Dict.create().set("url", url)));
        //返回文件的链接，这个链接就是文件的下载地址，这个下载地址就是我的后台提供出来的
        return dict;
    }

    /**
     * 富文本上传视频
     */
    @PostMapping("/editor/uploadVideo")
    public Dict editorUploadVideo(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        checkExtension(originalFilename);
        // aaa.png
        String mainName = FileUtil.mainName(originalFilename);
        String extName = FileUtil.extName(originalFilename);
        if (!FileUtil.exist(ROOT_PATH)) {
            FileUtil.mkdir(ROOT_PATH);
        }
        if (FileUtil.exist(ROOT_PATH + File.separator + originalFilename)) {
            originalFilename = System.currentTimeMillis() + "_" + mainName + "." + extName;
        }
        File saveFile = new File(ROOT_PATH + File.separator + originalFilename);
        file.transferTo(saveFile);
        String url = "http://" + ip + ":" + port + "/file/download/" + originalFilename;

        Dict dict = Dict.create().set("errno", 0).set("data", Dict.create().set("url", url));
        return dict;
    }
}
