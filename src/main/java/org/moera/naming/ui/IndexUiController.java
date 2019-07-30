package org.moera.naming.ui;

import javax.servlet.http.HttpServletRequest;

import org.moera.naming.util.UriUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexUiController {

    @GetMapping("/")
    public String index(HttpServletRequest request, Model model) {
        String endpointUri = UriUtil.createBuilderFromRequest(request).path("/moera-naming/").build().toUriString();
        model.addAttribute("endpointUri", endpointUri);

        return "index";
    }

}
