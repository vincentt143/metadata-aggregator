/** Copyright (c) 2011, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package au.org.intersect.sydma.webapp.controller;

import java.security.Principal;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.org.intersect.sydma.webapp.domain.DBSchema;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.dto.DatasetSchema;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.service.ResearchDatasetDBService;

/**
 * Controller to handle dataset dbSchemas
 */
@RequestMapping("/dbschema/**")
@Controller
public class DBSchemaController
{
    private static final Logger LOG = LoggerFactory.getLogger(DBSchemaController.class);

    private static final String CREATE_SCHEMA_VIEW = "dbschema/create";
    private static final String SCHEMAS_ATTR = "schemas";
    private static final String DATASET_ATTR = "dataset";
    private static final String DATASET_ID_PARAM = "datasetId";
    private static final String DATASET_SCHEMA_ATTR = "datasetSchema";

    @Autowired
    private ResearchDatasetDBService dbService;

    @Autowired
    private PermissionService permissionService;

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String createSchemaForDatasetRender(final Model model, @RequestParam(DATASET_ID_PARAM) final Long datasetId,
            final Principal principal)
    {
        final ResearchDataset dataset = ResearchDataset.findResearchDataset(datasetId);
        if (dataset == null)
        {
            throw new EntityNotFoundException("Failed to find Research Dataset with id [" + datasetId + "]");
        }
        return permissionService.canDataset(PermissionType.EDIT_DATASET, dataset.getId(), principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        final DatasetSchema datasetSchema = new DatasetSchema();

                        datasetSchema.setDatasetId(datasetId);

                        return setupCreateSchemaView(dataset, datasetSchema, model);
                    }
                });
    }

    private String setupCreateSchemaView(ResearchDataset dataset, final DatasetSchema datasetSchema, final Model model)
    {

        model.addAttribute(DATASET_ATTR, dataset);
        model.addAttribute(DATASET_SCHEMA_ATTR, datasetSchema);
        model.addAttribute(SCHEMAS_ATTR, DBSchema.findAllDBSchemas());

        return CREATE_SCHEMA_VIEW;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String createSchemaForDataset(final Model model, @RequestParam("submit") final String submit,
            @Valid @ModelAttribute("datasetSchema") final DatasetSchema datasetSchema, final BindingResult result,
            final Principal principal)
    {
        LOG.info("Create Schema for Dataset::Submit::" + submit);

        final String schemaName = datasetSchema.getSchemaName();
        final Long datasetId = datasetSchema.getDatasetId();

        final ResearchDataset dataset = ResearchDataset.findResearchDataset(datasetId);
        if (dataset == null)
        {
            throw new EntityNotFoundException("Research Dataset with id [" + datasetId + "] does not exist");
        }

        return permissionService.canDataset(PermissionType.EDIT_DATASET, dataset.getId(), principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        if ("Create".equals(submit))
                        {
                            if (result.hasErrors())
                            {
                                return setupCreateSchemaView(dataset, datasetSchema, model);
                            }

                            final DBSchema dbSchema = DBSchema.findDBSchema(schemaName);

                            if (dbSchema == null)
                            {
                                throw new EntityNotFoundException("Schema with name [" + schemaName
                                        + "] does not exist");
                            }

                            dbService.createDBForDatasetAndGrantUser(dataset, dbSchema, user);
                        }
                        return "redirect:/";
                    }
                });

    }
}
