package group.hound.demo.service.impl;

import group.hound.demo.service.IDemoService;
import group.hound.mvcframework.annotation.HoundService;

@HoundService
public class DemoServiceImpl implements IDemoService {
    @Override
    public String get(String name) {
        return "My name is " + name;
    }
}
