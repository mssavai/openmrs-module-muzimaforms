package org.openmrs.module.muzimaforms.api;

import org.dom4j.DocumentException;
import org.javarosa.xform.parse.ValidationMessages;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Form;
import org.openmrs.module.muzimaforms.MuzimaForm;
import org.openmrs.module.muzimaforms.api.db.hibernate.MuzimaFormDAO;
import org.openmrs.module.muzimaforms.api.impl.CompositeEnketoResult;
import org.openmrs.module.muzimaforms.api.impl.EnketoResult;
import org.openmrs.module.muzimaforms.api.impl.MuzimaFormServiceImpl;
import org.openmrs.module.muzimaforms.xForm2MuzimaTransform.ModelXml2JsonTransformer;
import org.openmrs.module.muzimaforms.xForm2MuzimaTransform.ODK2HTML5Transformer;
import org.openmrs.module.muzimaforms.xForm2MuzimaTransform.ODK2JavarosaTransformer;
import org.openmrs.module.muzimaforms.xForm2MuzimaTransform.XForm2Html5Transformer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.openmrs.module.muzimaforms.MuzimaFormBuilder.muzimaform;
import static org.openmrs.module.muzimaforms.MuzimaFormTagBuilder.tag;
import static org.openmrs.module.muzimaforms.XFormBuilder.xForm;

public class MuzimaFormServiceImplTest {

    private MuzimaFormService service;
    MuzimaFormDAO dao;
    XForm2Html5Transformer transformer;
    ModelXml2JsonTransformer modelTransformer;
    private ODK2JavarosaTransformer odk2JavarosaTransformer;
    private Date syncDate;
    private ODK2HTML5Transformer odk2HTML5Transformer;

    @Before
    public void setUp() throws Exception {
        dao = mock(MuzimaFormDAO.class);
        transformer = mock(XForm2Html5Transformer.class);
        modelTransformer = mock(ModelXml2JsonTransformer.class);
        odk2JavarosaTransformer = mock(ODK2JavarosaTransformer.class);
        odk2HTML5Transformer = mock(ODK2HTML5Transformer.class);
        syncDate = new Date();
        service = new MuzimaFormServiceImpl(dao, transformer, modelTransformer, odk2JavarosaTransformer, odk2HTML5Transformer);
    }

    void setUpDao() {
        List<MuzimaForm> muzimaForms = new ArrayList<MuzimaForm>();
        muzimaForms.add(
                muzimaform().withId(1).withName("Registration Form").withDescription("Form for registration")
                        .with(tag().withId(1).withName("Registration"))
                        .with(tag().withId(2).withName("Patient"))
                        .instance());
        muzimaForms.add(muzimaform().withId(2).withName("PMTCT Form").withDescription("Form for PMTCT")
                .with(tag().withId(1).withName("Registration"))
                .with(tag().withId(3).withName("Encounter"))
                .with(tag().withId(4).withName("HIV"))
                .instance());

        muzimaForms.add(muzimaform().withId(3).withName("Ante-Natal Form").withDescription("Form for ante-natal care")
                .instance());

        when(dao.getAll()).thenReturn(muzimaForms);
    }

    @Test
    public void getAll_shouldGetAllForms() throws Exception {
        setUpDao();
        List<MuzimaForm> list = service.getAll();
        assertThat(list.size(), is(3));
        verify(dao, times(1)).getAll();
    }

    @Test
    public void getXform_shouldLoadXForm() throws Exception {
        service.getXForms();
        verify(dao, times(1)).getXForms();
    }

    @Test
    public void shouldNotInteractWithAnyTransformersWhileUploadingHTML() throws Exception {
        service.createHTMLForm("name", "form", "description", "discriminator", "html");
        verifyZeroInteractions(transformer,modelTransformer,odk2JavarosaTransformer,odk2HTML5Transformer);
        verify(dao).saveForm(any(MuzimaForm.class));
    }

    @Ignore
    @Test(expected = DocumentException.class)
    public void shouldNotCreateHTMLFormIfFormNameAlreadyExists() throws Exception {
        List<MuzimaForm> muzimaForms = asList(getMuzimaFormWithName("Something like name"),
                getMuzimaFormWithName("name"));
        when(dao.findByName("name", syncDate)).thenReturn(muzimaForms);
        service.createHTMLForm("name", "form", "description", "discriminator", "html");
        verifyZeroInteractions(transformer,modelTransformer,odk2JavarosaTransformer,odk2HTML5Transformer);
        verify(dao,never()).saveForm(any(MuzimaForm.class));
    }

    @Test
    public void importExisting_shouldRetrieveExistingXFormAndConvertItIntoHTML5AndPersistAMuzimaForm() throws Exception {
        String xFormXml = "<xml><some/><valid/></xml>";
        String htmlForm = "<foo><form><ul><li/><li/></ul></form><model/></foo>";
        String modelJson = "{form : [{name:'', bind: ''}]}";

        when(dao.getXform(1)).thenReturn(xForm().withId(1).withXFormXml(xFormXml).instance());
        when(transformer.transform(xFormXml)).thenReturn(new EnketoResult(htmlForm));
        when(modelTransformer.transform(htmlForm)).thenReturn(new CompositeEnketoResult(htmlForm, modelJson));

        service.importExisting(1, "name", "form", "description", "discriminator");

        verify(dao, times(1)).getXform(1);
        verify(dao, times(1)).saveForm(muzimaform()
                .withName("name")
                .withForm("form")
                .withDescription("description")
                .withDiscriminator("discriminator")
                .instance());
    }

    @Test
    public void importExisting_shouldSetConvertedXform() throws Exception {

        String htmlForm = "<foo><form><ul><li/><li/></ul></form><model/></foo>";
        String xFormXml = "<foo><some/><valid/></foo>";
        String modelJson = "{form : [{name:'', bind: ''}]}";

        when(transformer.transform(xFormXml)).thenReturn(new EnketoResult(htmlForm));
        when(modelTransformer.transform(htmlForm)).thenReturn(new CompositeEnketoResult(htmlForm, modelJson));
        when(dao.getXform(1)).thenReturn(xForm().withId(1).withXFormXml(xFormXml).instance());

        service.importExisting(1, "name", "form", "description", "discriminator");

        verify(dao, times(1)).saveForm(muzimaform()
                .withName("name")
                .withForm("form")
                .withDescription("description")
                .withDiscriminator("discriminator")
                .instance());
        verify(transformer, times(1)).transform(xFormXml);
        verify(modelTransformer, times(1)).transform(htmlForm);
        verify(dao, times(1)).getXform(1);
    }

    @Test
    public void importODKShouldTransformUsingTheODK2HTML5Pipeline() throws Exception {
        when(odk2HTML5Transformer.transform("odk")).thenReturn(new EnketoResult("xml"));
        CompositeEnketoResult result = mock(CompositeEnketoResult.class);
        when(modelTransformer.transform("xml")).thenReturn(result);

        service.importODK("odk", "name",  "form", "description", "discriminator");

        verify(modelTransformer).transform("xml");
        verify(odk2HTML5Transformer).transform("odk");
        verify(dao).saveForm(any(MuzimaForm.class));

    }

    @Test
    public void save_shouldSaveExistingForm() throws Exception {
        MuzimaForm form = muzimaform().withId(1).instance();
        service.save(form);
        verify(dao).saveForm(form);
    }

    @Test
    public void findById_shouldFindFormById() {
        service.findById(1);
        verify(dao, times(1)).findById(1);
    }

    @Test
    public void findByUUID_shouldFindFormByUUID() {
        service.findByUniqueId("foo");
        verify(dao, times(1)).findByUuid("foo");
    }

    @Test
    public void validateJavaRosa() throws Exception {
        ValidationMessages messages = service.validateJavaRosa("xml");
        assertThat(messages.getList().get(0).getMessage(), is("Document has no root element!"));
    }

    @Test
    public void validateODK() throws Exception {
        when(odk2JavarosaTransformer.transform("odk")).thenReturn(new EnketoResult("xml"));

        ValidationMessages messages = service.validateODK("odk");

        verify(odk2JavarosaTransformer).transform("odk");
        assertThat(messages.getList().get(0).getMessage(), is("Document has no root element!"));
    }

    @Ignore
    @Test(expected = DocumentException.class)
    public void shouldNotCreateFormIfTheNameAlreadyExists() throws ParserConfigurationException, TransformerException, DocumentException, IOException {
        List<MuzimaForm> muzimaForms = asList(getMuzimaFormWithName("Something like name"),
                getMuzimaFormWithName("name"));
        when(dao.findByName("name", syncDate)).thenReturn(muzimaForms);
        try {
            service.create("xml", "name", "form", "description", "discriminator");
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(modelTransformer,never()).transform(anyString());
    }

    @Test
    public void shouldCreateFormIfSimilarNameExistsButNotExactMatch() throws ParserConfigurationException, TransformerException, DocumentException, IOException {
        List<MuzimaForm> muzimaForms = asList(getMuzimaFormWithName("Something like name"),
                getMuzimaFormWithName("very much similar to name"));
        EnketoResult enketoResult = mock(EnketoResult.class);
        CompositeEnketoResult compositeEnketResult = mock(CompositeEnketoResult.class);

        when(dao.findByName("name", syncDate)).thenReturn(muzimaForms);
        when(transformer.transform(anyString())).thenReturn(enketoResult);
        when(modelTransformer.transform(anyString())).thenReturn(compositeEnketResult);

        try {
            service.create("xml", "name", "form", "description", "discriminator");
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(modelTransformer).transform(anyString());
    }

    private MuzimaForm getMuzimaFormWithName(String name) {
        MuzimaForm form1 = new MuzimaForm();
        form1.setName(name);
        return form1;
    }


}
