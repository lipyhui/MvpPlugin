import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MvpCreateAction extends AnAction {
    private Project project;
    //包名
    private String mPackageName;
    //右键选中目录
    private String mDirectoryPath;
    private String mAuthor;//作者
    private String mModuleName;//模块名称

    private enum  CodeType {
        Activity, Fragment, Contract, Presenter, BaseView, BasePresenter, BaseActivity, BaseFragment
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getData(PlatformDataKeys.PROJECT);
        mPackageName = getPackageName();
        mDirectoryPath = getSelectDir(e);

        init();
        refreshProject(e);
    }

    /**
     * 刷新项目
     * @param e
     */
    private void refreshProject(AnActionEvent e) {
        e.getProject().getBaseDir().refresh(false, true);
    }

    /**
     * 初始化Dialog
     */
    private void init(){
        MvpDialog mvpDialog = new MvpDialog(new MvpDialog.DialogCallBack() {
            @Override
            public void ok(String author, String moduleName) {
                mAuthor = author;
                mModuleName = moduleName;
                createClassFiles();
            }
        });
        mvpDialog.pack();
        mvpDialog.setVisible(true);
    }

    /**
     * 生成类文件
     */
    private void createClassFiles() {
        createClassFile(CodeType.Activity);
        createClassFile(CodeType.Fragment);
        createClassFile(CodeType.Contract);
        createClassFile(CodeType.Presenter);
        createBaseClassFile(CodeType.BaseView);
        createBaseClassFile(CodeType.BasePresenter);
        createBaseClassFile(CodeType.BaseActivity);
        createBaseClassFile(CodeType.BaseFragment);
    }

    /**
     * 生成base类
     * @param codeType
     */
    private void createBaseClassFile(CodeType codeType) {
        String fileName = "";
        String content = "";
        String basePath = getAppPath() + "base/";
        switch (codeType){
            case BaseView:
                if (!new File(basePath + "BaseView.java").exists()){
                    fileName = "TemplateBaseView.txt";
                    content = ReadTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, basePath, "BaseView.java");
                }
                break;
            case BasePresenter:
                if (!new File(basePath + "BasePresenter.java").exists()){
                    fileName = "TemplateBasePresenter.txt";
                    content = ReadTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, basePath, "BasePresenter.java");
                }
                break;
            case BaseActivity:
                if (!new File(basePath + "BaseActivity.java").exists()){
                    fileName = "TemplateBaseActivity.txt";
                    content = ReadTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, basePath, "BaseActivity.java");
                }
                break;
            case BaseFragment:
                if (!new File(basePath + "BaseFragment.java").exists()){
                    fileName = "TemplateBaseFragment.txt";
                    content = ReadTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, basePath, "BaseFragment.java");
                }
                break;
        }
    }

    /**
     * 生成mvp框架代码
     * @param codeType
     */
    private void createClassFile(CodeType codeType) {
        String fileName = "";
        String content = "";
        String appPath = getSelectPath();
        switch (codeType){
            case Activity:
                fileName = "TemplateActivity.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + mModuleName.toLowerCase(), mModuleName + "Activity.java");
                break;
            case Fragment:
                fileName = "TemplateFragment.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + mModuleName.toLowerCase(), mModuleName + "Fragment.java");
                break;
            case Contract:
                fileName = "TemplateContract.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + mModuleName.toLowerCase(), mModuleName + "Contract.java");
                break;
            case Presenter:
                fileName = "TemplatePresenter.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + mModuleName.toLowerCase(), mModuleName + "Presenter.java");
                break;
        }
    }

    /**
     * 获取包名文件路径
     * @return
     */
    private String getAppPath(){
        String packagePath = mPackageName.replace(".", "/");
        String appPath = project.getBasePath() + "/App/src/main/java/" + packagePath + "/";
        return appPath;
    }

    /**
     * 获取包名文件路径
     * @return
     */
    private String getSelectPath(){
        String path = mDirectoryPath.replace("\\", "/");
        return path + "/";
    }

    /**
     * 替换模板中字符
     * @param content
     * @return
     */
    private String dealTemplateContent(String content) {
        content = content.replace("$name", mModuleName);
        if (content.contains("$packagename")){
            String dirName = getSelectPath().substring(getAppPath().length()).replace("/", ".");
            content = content.replace("$packagename", mPackageName+ "." + dirName + mModuleName.toLowerCase());
        }
        if (content.contains("$basepackagename")){
            content = content.replace("$basepackagename", mPackageName + ".base");
        }
        content = content.replace("$author", mAuthor);
        content = content.replace("$date", getDate());
        return content;
    }

    /**
     * 获取当前时间
     * @return
     */
    public String getDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }


    /**
     * 读取模板文件中的字符内容
     * @param fileName 模板文件名
     * @return
     */
    private String ReadTemplateFile(String fileName) {
        InputStream in = null;
        in = this.getClass().getResourceAsStream("/Template/" + fileName);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }


    private byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        try {
            while ((len = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            outputStream.close();
            inputStream.close();
        }

        return outputStream.toByteArray();
    }


    /**
     * 生成
     * @param content 类中的内容
     * @param classPath 类文件路径
     * @param className 类文件名称
     */
    private void writeToFile(String content, String classPath, String className) {
        try {
            File floder = new File(classPath);
            if (!floder.exists()){
                floder.mkdirs();
            }

            File file = new File(classPath + "/" + className);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取右键选中目录
     * @return
     */
    private String getSelectDir(AnActionEvent e) {
        Project fatherProject = e.getProject();
        if (fatherProject == null) {
            return "";
        }
        //获取所选的目录，即需要添加类的的包路径file
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());

        if (virtualFile == null || !virtualFile.isDirectory()) {
            Messages.showInfoMessage(project,"get err !!!!!!!","title");
            return "";
        }
        //通过所选文件，获取包的directory
        PsiDirectory directory = PsiDirectoryFactory.getInstance(fatherProject).createDirectory(virtualFile);
        return directory.toString().substring(13);
    }

    /**
     * 从AndroidManifest中获取包名
     * @return
     */
    private String getPackageName() {
        String package_name = "";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(project.getBasePath() + "/App/src/main/AndroidManifest.xml");
                NodeList nodeList = doc.getElementsByTagName("manifest");
                for (int i = 0; i < nodeList.getLength(); i++){
                        Node node = nodeList.item(i);
                        Element element = (Element) node;
                        package_name = element.getAttribute("package");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        return package_name;
    }

}
