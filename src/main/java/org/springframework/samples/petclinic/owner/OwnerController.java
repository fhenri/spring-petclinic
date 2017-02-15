/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.awt.Color;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.samples.petclinic.pdf.LineBox;

import org.apache.pdfbox.pdmodel.font.PDType1Font;

import rst.pdfbox.layout.elements.Document;
import rst.pdfbox.layout.elements.ImageElement;
import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.elements.render.VerticalLayoutHint;
import rst.pdfbox.layout.text.Alignment;
import rst.pdfbox.layout.text.BaseFont;
import rst.pdfbox.layout.text.Indent;
import rst.pdfbox.layout.text.NewLine;
import rst.pdfbox.layout.text.SpaceUnit;


/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
class OwnerController {

    private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";
    private final OwnerRepository owners;


    @Autowired
    public OwnerController(OwnerRepository clinicService) {
        this.owners = clinicService;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @RequestMapping(value = "/owners/new", method = RequestMethod.GET)
    public String initCreationForm(Map<String, Object> model) {
        Owner owner = new Owner();
        model.put("owner", owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @RequestMapping(value = "/owners/new", method = RequestMethod.POST)
    public String processCreationForm(@Valid Owner owner, BindingResult result) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        } else {
            this.owners.save(owner);
            return "redirect:/owners/" + owner.getId();
        }
    }

    @RequestMapping(value = "/owners/find", method = RequestMethod.GET)
    public String initFindForm(Map<String, Object> model) {
        model.put("owner", new Owner());
        return "owners/findOwners";
    }

    @RequestMapping(value = "/owners", method = RequestMethod.GET)
    public String processFindForm(Owner owner, BindingResult result, Map<String, Object> model) {

        // allow parameterless GET request for /owners to return all records
        if (owner.getLastName() == null) {
            owner.setLastName(""); // empty string signifies broadest possible search
        }

        // find owners by last name
        Collection<Owner> results = this.owners.findByLastName(owner.getLastName());
        if (results.isEmpty()) {
            // no owners found
            result.rejectValue("lastName", "notFound", "not found");
            return "owners/findOwners";
        } else if (results.size() == 1) {
            // 1 owner found
            owner = results.iterator().next();
            return "redirect:/owners/" + owner.getId();
        } else {
            // multiple owners found
            model.put("selections", results);
            return "owners/ownersList";
        }
    }

    @RequestMapping(value = "/owners/{ownerId}/edit", method = RequestMethod.GET)
    public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
        Owner owner = this.owners.findById(ownerId);
        model.addAttribute(owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @RequestMapping(value = "/owners/{ownerId}/export", method = RequestMethod.GET)
    public void exportOwnerPDF(@PathVariable("ownerId") int ownerId, HttpServletResponse response) {
        try {
            Owner owner = this.owners.findById(ownerId);
            ByteArrayOutputStream output = createPDF(owner);
            response.addHeader("Content-Type", "application/force-download");
            response.addHeader("Content-Disposition", "attachment; filename=\"owner_export.pdf\"");
            response.getOutputStream().write(output.toByteArray());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ByteArrayOutputStream createPDF(Owner owner) throws IOException {

        Document document = new Document(40, 60, 80, 60);

        Paragraph paragraph = new Paragraph();
        paragraph.add(new Indent("Name", 100, SpaceUnit.pt, 11, PDType1Font.HELVETICA));
        paragraph.addMarkup(owner.getFirstName() + " " + owner.getLastName() + "\n", 11, BaseFont.Times);

        paragraph.add(new Indent("Address", 100, SpaceUnit.pt, 11, PDType1Font.HELVETICA));
        paragraph.addMarkup(owner.getAddress() + "\n", 11, BaseFont.Times);
        
        paragraph.add(new Indent("City", 100, SpaceUnit.pt, 11, PDType1Font.HELVETICA));
        paragraph.addMarkup(owner.getCity() + "\n", 11, BaseFont.Times);

        paragraph.add(new NewLine(12));
        document.add(paragraph);

        paragraph = new Paragraph();
        LineBox lbox = new LineBox(paragraph, 500, 8);
        lbox.setMargins(10, 10, 20, 5);
        lbox.setBackgroundColor(Color.black);
        paragraph.add(new NewLine(12));
        document.add(lbox);

        //Resource resource = new ClassPathResource("spring-pivotal-logo.png");
        ImageElement image = new ImageElement(
            this.getClass().getResourceAsStream(
                "/static/resources/images/spring-pivotal-logo.png"));
        //ImageElement image = new ImageElement(springlogo.getInputStream());
        if (image != null) {
            document.add(image, new VerticalLayoutHint(Alignment.Center, 0, 0,
                20, 0, false));
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        document.save(output);
        return output;
    }

    @RequestMapping(value = "/owners/{ownerId}/edit", method = RequestMethod.POST)
    public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        } else {
            owner.setId(ownerId);
            this.owners.save(owner);
            return "redirect:/owners/{ownerId}";
        }
    }

    /**
     * Custom handler for displaying an owner.
     *
     * @param ownerId the ID of the owner to display
     * @return a ModelMap with the model attributes for the view
     */
    @RequestMapping("/owners/{ownerId}")
    public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
        ModelAndView mav = new ModelAndView("owners/ownerDetails");
        mav.addObject(this.owners.findById(ownerId));
        return mav;
    }

}
