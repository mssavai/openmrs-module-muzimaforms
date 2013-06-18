package org.openmrs.module.html5forms.web.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.html5forms.HTML5Form;
import org.openmrs.module.html5forms.api.HTML5FormService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
@RequestMapping(value = "module/html5forms/forms.form")

public class HTML5FormsController {
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<HTML5Form> forms() {
        HTML5FormService service = Context.getService(HTML5FormService.class);
        return service.getAll();
    }

    @RequestMapping(method = RequestMethod.POST, value="form")
    @ResponseBody
    public void create(@RequestBody HTML5Form form) {
        HTML5FormService service = Context.getService(HTML5FormService.class);
        service.saveForm(form);
    }

    @RequestMapping(method = RequestMethod.PUT, value="form")
    @ResponseBody
    public void update(@RequestBody HTML5Form form) {
        HTML5FormService service = Context.getService(HTML5FormService.class);
        service.saveForm(form);
    }
}
