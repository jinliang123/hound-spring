package group.hound.mvcframework.v1.servlet;

import group.hound.mvcframework.annotation.HoundAutowired;
import group.hound.mvcframework.annotation.HoundController;
import group.hound.mvcframework.annotation.HoundRequestMapping;
import group.hound.mvcframework.annotation.HoundService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HoundDispatcherServlet extends HttpServlet {
    //IOC容器
    private Map<String, Object> mapping = new HashMap<String, Object>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception" + Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * 执行调用逻辑
     *
     * @param req
     * @param resp
     * @throws Exception
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if (!this.mapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!!");
            return;
        }
        Method method = (Method) this.mapping.get(url);
        Map<String, String[]> params = req.getParameterMap();
        method.invoke(this.mapping.get(method.getDeclaringClass().getName()), new Object[]{req, resp, params.get("name")[0]});

    }

    /**
     * 初始化方法 先初始化所有相关的类，IOC容器，ServletBean
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try{
            Properties configContext = new Properties();
            is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
            configContext.load(is);
            String scanPackage = configContext.getProperty("scanPackage");
            doScanner(scanPackage);
            for (String className : mapping.keySet()) {
                if(!className.contains(".")){continue;}
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(HoundController.class)){
                    mapping.put(className,clazz.newInstance());
                    String baseUrl = "";
                    if (clazz.isAnnotationPresent(HoundRequestMapping.class)) {
                        HoundRequestMapping requestMapping = clazz.getAnnotation(HoundRequestMapping.class);
                        baseUrl = requestMapping.value();
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        if (!method.isAnnotationPresent(HoundRequestMapping.class)) {  continue; }
                        HoundRequestMapping requestMapping = method.getAnnotation(HoundRequestMapping.class);
                        String url = (baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                        mapping.put(url, method);
                        System.out.println("Mapped " + url + "," + method);
                    }
                }else if(clazz.isAnnotationPresent(HoundService.class)){
                    HoundService service = clazz.getAnnotation(HoundService.class);
                    String beanName = service.value();
                    if("".equals(beanName)){beanName = clazz.getName();}
                    Object instance = clazz.newInstance();
                    mapping.put(beanName,instance);
                    for (Class<?> i : clazz.getInterfaces()) {
                        mapping.put(i.getName(),instance);
                    }
                }else {continue;}
            }
            for (Object object : mapping.values()) {
                if(object == null){continue;}
                Class clazz = object.getClass();
                if(clazz.isAnnotationPresent(HoundController.class)){
                    Field [] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if(!field.isAnnotationPresent(HoundAutowired.class)){continue; }
                        HoundAutowired autowired = field.getAnnotation(HoundAutowired.class);
                        String beanName = autowired.value();
                        if("".equals(beanName)){beanName = field.getType().getName();}
                        field.setAccessible(true);
                        try {
                            field.set(mapping.get(clazz.getName()),mapping.get(beanName));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
        }finally {
            if(is != null){
                try {is.close();} catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.print("GP MVC Framework is init");

    }

    /**
     * 扫描所有的类并加载到IOC容器中
     *
     * @param scanPackage 路径
     */
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().
                getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                //如果是目录则递归往下扫描
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    //如果不是以.class结尾则继续遍历
                    continue;
                }
                String clazzName = (scanPackage + "." + file.getName().replace(".class", ""));
                mapping.put(clazzName, null);
            }
        }

    }
}
