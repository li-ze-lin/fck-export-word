package cn.lzl.fckexportword;


import org.apache.commons.io.FileUtils;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.UUID;


/**
 * <p>
 * FCKeditor富文本上传文件
 * <p>
 * 要把上传图片的路径在/com.dream.springbootframe/src/main/resources/static/ckeditor/config.js文件内配置
 * config.filebrowserUploadUrl="/imgUpdate/upfile"
 */
@CrossOrigin("*")
@Controller
@RequestMapping("/fck")
public class FckController {

    private volatile FckDTO fckDTO;

    /**
     * 上传图片
     * ps:前后端未分离时可以这么用 分离后请勿参考该方法上传
     */
    //@RequestMapping(value = "/upload_img")
    public void uploadFile(@RequestParam("upload") MultipartFile multipartFile, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        String CKEditorFuncNum = request.getParameter("CKEditorFuncNum");
        String filename = multipartFile.getOriginalFilename();
        //得到文件上传的服务器路径	后面拼接static加跳转页面的@RequestMapping("/imgUpdate")内的路径
        //这个上传到项目内
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath() + "static\\fck\\";

        //上传到tomcat的临时文件路径
        //String path = request.getSession().getServletContext().getRealPath("") + "\\fck\\";

        //解决文件同名问题
        filename = UUID.randomUUID().toString().replace("-", "") + filename.substring(filename.lastIndexOf("."));

        //定义服务器的新文件
        File newFile = new File(path + filename);
        File f = null;

        f = File.createTempFile("tmp", null);
        multipartFile.transferTo(f);
        //真正上传
        FileUtils.copyFile(f, newFile);
        f.deleteOnExit();


        PrintWriter out;
        String s = "<script type=\"text/javascript\">window.parent.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ", '" + filename + "');</script>";
        try {
            out = response.getWriter();
            out.print(s);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/save")
    public void save(@RequestBody FckDTO fckDTO) {
        this.fckDTO = fckDTO;
    }

    /**
     * 测试富文本导出word
     */
    @RequestMapping("/export")
    public void exportWord(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Objects.requireNonNull(fckDTO);
        //word内容
        String content = "<html><meta charset=\"utf-8\" /><body>";
        content += fckDTO.getText();
        content += "</body></html>";
        //获取绝对路径 这里要与文件存储的路径一样
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath() + "static/fck/";
        path = path.substring(1);
        //替换里面的所有图片路径
        content = content.replaceAll("<img alt=\"\" src=\"", "<img alt=\"\" src=\"" + path);

        byte b[] = content.getBytes("utf-8");  //这里是必须要设置编码的，不然导出中文就会乱码。
        ByteArrayInputStream bais = new ByteArrayInputStream(b);//将字节数组包装到流中

        //* 关键地方
        //* 生成word格式
        POIFSFileSystem poifs = new POIFSFileSystem();
        DirectoryEntry directory = poifs.getRoot();
        DocumentEntry documentEntry = directory.createDocument("WordDocument", bais);

        //设置文件名
        String fileName = UUID.randomUUID().toString();
        //处理文件名乱码
        String userAgent = request.getHeader("User-Agent");
        // 针对IE或者以IE为内核的浏览器：
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        } else {
            // 非IE浏览器的处理：
            fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
        }

        //输出文件
        request.setCharacterEncoding("utf-8");
        response.setContentType("application/msword");//导出word格式
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName + ".doc");
        OutputStream ostream = response.getOutputStream();
        poifs.writeFilesystem(ostream);
        bais.close();
        ostream.close();

    }
}
