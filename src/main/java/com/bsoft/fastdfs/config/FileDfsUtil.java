package com.bsoft.fastdfs.config;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

@Component
public class FileDfsUtil {
    private  final  static Logger LOGGER = LoggerFactory.getLogger(FileDfsUtil.class);
    @Resource
    private FastFileStorageClient storageClient ;

    /**
     *	MultipartFile类型的文件上传ַ
     * @param file
     * @return
     * @throws IOException
     */
    public String uploadFile(MultipartFile file) throws IOException {
        StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(),
                FilenameUtils.getExtension(file.getOriginalFilename()), null);
        return getResAccessUrl(storePath);
    }

    /**
     * 普通的文件上传
     *
     * @param file
     * @return
     * @throws IOException
     */
    public String uploadFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        StorePath path = storageClient.uploadFile(inputStream, file.length(),
                FilenameUtils.getExtension(file.getName()), null);
        return getResAccessUrl(path);
    }

    /**
     * 带输入流形式的文件上传
     *
     * @param is
     * @param size
     * @param fileName
     * @return
     */
    public String uploadFileStream(InputStream is, long size, String fileName) {
        StorePath path = storageClient.uploadFile(is, size, fileName, null);
        return getResAccessUrl(path);
    }

    /**
     * 将一段文本文件写到fastdfs的服务器上
     * @param content
     * @param fileExtension
     * @return
     */
    public String uploadFile(String content, String fileExtension) {
        byte[] buff = content.getBytes(Charset.forName("UTF-8"));
        ByteArrayInputStream stream = new ByteArrayInputStream(buff);
        StorePath path = storageClient.uploadFile(stream, buff.length, fileExtension, null);
        return getResAccessUrl(path);
    }

    /**
     * 上传图片
     * @param is
     * @param size
     * @param fileExtName
     * @param metaData
     * @return
     */
    public String upfileImage(InputStream is, long size, String fileExtName, Set metaData) {
        StorePath path = storageClient.uploadImageAndCrtThumbImage(is, size, fileExtName, metaData);
        return getResAccessUrl(path);
    }

    /**
     * 返回文件上传成功后的地址名称ַ
     * @param storePath
     * @return
     */
    private String getResAccessUrl(StorePath storePath) {
        String fileUrl = storePath.getFullPath();
        return fileUrl;
    }



    /**
     * 上传图片并且生成缩略图
     */
    public String upload(MultipartFile multipartFile) throws Exception{
        String originalFilename = multipartFile.getOriginalFilename().
                substring(multipartFile.getOriginalFilename().
                        lastIndexOf(".") + 1);
        StorePath storePath = this.storageClient.uploadImageAndCrtThumbImage(
                multipartFile.getInputStream(),
                multipartFile.getSize(),originalFilename , null);
        return storePath.getFullPath() ;
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            LOGGER.info("fileUrl == >>文件路径为空...");
            return;
        }
        try {
            StorePath storePath = StorePath.parseFromUrl(fileUrl);
            storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param fileUrl
     * @return
     * @throws IOException
     */
    public byte[] downloadFile(String fileUrl) throws IOException {
        String group = fileUrl.substring(0, fileUrl.indexOf("/"));
        String path = fileUrl.substring(fileUrl.indexOf("/") + 1);
        DownloadByteArray downloadByteArray = new DownloadByteArray();
        byte[] bytes = storageClient.downloadFile(group, path, downloadByteArray);
        return bytes;

    }

    /**
            * 查看文件的信息
     *
             * @param groupName
     * @param path
     * @return
             */
    public FileInfo getFile(String groupName, String path) {
        FileInfo fileInfo = null;
        LOGGER.info("文件信息入参，groupName:{}，path:{}", groupName, path);
        try {
            fileInfo = storageClient.queryFileInfo(groupName, path);
            LOGGER.info("文件信息:{}", fileInfo.toString());
        } catch (Exception e) {
            LOGGER.error("Non IO Exception: Get File from Fast DFS failed", e);
        }
        return fileInfo;
    }

    /**
     * 获取文件元信息
     *
     * @param groupName
     * @param path
     * @return
     */
    public Set getMetadata(String groupName, String path) {
        Set mataData = new HashSet<>();
        try {
            mataData = storageClient.getMetadata(groupName, path);
            LOGGER.info("文件元信息:{}", mataData.toArray());
        } catch (Exception e) {
            LOGGER.error("Non IO Exception: Get File from Fast DFS failed", e);
        }
        return mataData;
    }


}