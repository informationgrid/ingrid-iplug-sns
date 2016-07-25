package de.ingrid.iplug.sns.webapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IViews;
import de.ingrid.admin.command.PlugdescriptionCommandObject;

@Controller
@SessionAttributes("plugDescription")
public class SnsParameterController {

	@RequestMapping(value = { "/iplug-pages/welcome.html", "/iplug-pages/snsParams.html" }, method = RequestMethod.GET)
	public String getParameters(final ModelMap modelMap,
			@ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject) {

		return IViews.SAVE;
	}
}
