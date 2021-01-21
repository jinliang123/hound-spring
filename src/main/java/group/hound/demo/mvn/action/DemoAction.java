package group.hound.demo.mvn.action;

import group.hound.demo.service.IDemoService;
import group.hound.mvcframework.annotation.HoundAutowired;
import group.hound.mvcframework.annotation.HoundController;
import group.hound.mvcframework.annotation.HoundRequestMapping;
import group.hound.mvcframework.annotation.HoundRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@HoundController
@HoundRequestMapping("/demo")
public class DemoAction {
    @HoundAutowired
    private IDemoService iDemoService;

    @HoundRequestMapping("/query.*")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @HoundRequestParam("name") String name){
//		String result = demoService.get(name);
        String result = "My name is " + name;
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @HoundRequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @HoundRequestParam("a") Integer a, @HoundRequestParam("b") Integer b){
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @HoundRequestMapping("/sub")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @HoundRequestParam("a") Double a, @HoundRequestParam("b") Double b){
        try {
            resp.getWriter().write(a + "-" + b + "=" + (a - b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @HoundRequestMapping("/remove")
    public String  remove(@HoundRequestParam("id") Integer id){
        return "" + id;
    }
}
