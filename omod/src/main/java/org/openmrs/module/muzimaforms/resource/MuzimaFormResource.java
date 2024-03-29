package org.openmrs.module.muzimaforms.resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.muzimaforms.MuzimaConstants;
import org.openmrs.module.muzimaforms.MuzimaForm;
import org.openmrs.module.muzimaforms.api.MuzimaFormService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/" + MuzimaConstants.MODULE_ID + "/form",
        supportedClass = MuzimaForm.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*"})
@Handler(supports = MuzimaForm.class)
public class MuzimaFormResource extends DataDelegatingCrudResource<MuzimaForm> {
    private static final Log log = LogFactory.getLog(MuzimaFormResource.class);

    @Override
    protected NeedsPaging<MuzimaForm> doGetAll(RequestContext context) throws ResponseException {
        MuzimaFormService service = Context.getService(MuzimaFormService.class);
        List<MuzimaForm> all = service.getAll();
        return new NeedsPaging<MuzimaForm>(all, context);
    }

    private Date parseDate(final String iso8601String) {
        if (!StringUtils.isNotBlank(iso8601String)) {
            return null;
        }
        Date date = null;
        try {
//            String s = iso8601String.replace("Z", "+00:00");
//            s = s.substring(0, 22) + s.substring(23);
//            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(s);
            String s = iso8601String.substring(0, 10);
            date = new SimpleDateFormat("yyyy-MM-dd").parse(s);
        } catch (ParseException e) {
            log.error("Unable to parse date information.");
        }
        return date;
    }

    @Override
    protected PageableResult doSearch(final RequestContext context) {
        HttpServletRequest request = context.getRequest();
        String nameParameter = request.getParameter("q");
        String syncDateParameter = request.getParameter("syncDate");
        List<MuzimaForm> muzimaForms = new ArrayList<MuzimaForm>();
        if (nameParameter != null) {
            Date syncDate = parseDate(syncDateParameter);
            muzimaForms = Context.getService(MuzimaFormService.class).findByName(nameParameter, syncDate);
        }
        return new NeedsPaging<MuzimaForm>(muzimaForms, context);
    }

    @Override
    public MuzimaForm getByUniqueId(String uuid) {
        MuzimaFormService service = Context.getService(MuzimaFormService.class);
        return service.findByUniqueId(uuid);
    }

    @Override
    public Object retrieve(String uuid, RequestContext context) throws ResponseException {
        MuzimaFormService service = Context.getService(MuzimaFormService.class);
        return asRepresentation(service.findByUniqueId(uuid), context.getRepresentation());
    }

    @Override
    protected void delete(MuzimaForm muzimaForm, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    public MuzimaForm newDelegate() {
        return new MuzimaForm();
    }

    public MuzimaForm save(MuzimaForm muzimaForm) {
        MuzimaFormService service = Context.getService(MuzimaFormService.class);
        try {
            return service.save(muzimaForm);
        } catch (Exception e) {
            log.error(e);
        }
        return muzimaForm;
    }

    @Override
    public void purge(MuzimaForm muzimaForm, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = null;

        if (rep instanceof DefaultRepresentation || rep instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("id");
            description.addProperty("name");
            description.addProperty("discriminator");
            description.addProperty("description");
            description.addProperty("model");
            description.addProperty("html");
            description.addProperty("modelJson");
            description.addProperty("form");
            description.addProperty("tags", new CustomRepresentation("(id,uuid,name)"));
            description.addSelfLink();
        }

        return description;
    }
}
