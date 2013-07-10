package org.openmrs.module.muzimaforms.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.api.context.Context;
import org.openmrs.module.muzimaforms.MuzimaFormTag;
import org.openmrs.module.muzimaforms.api.TagService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class TagControllerTest {
    private TagController controller;
    private TagService service;

    @Before
    public void setUp() throws Exception {
        service = mock(TagService.class);
        controller = new TagController();
        mockStatic(Context.class);
        PowerMockito.when(Context.getService(TagService.class)).thenReturn(service);
    }

    @Test
    public void tags_shouldReturnTags() throws IOException {
        MuzimaFormTag tag1 = new MuzimaFormTag() {{
            setId(1);
        }};
        MuzimaFormTag tag2 = new MuzimaFormTag() {{
            setId(2);
        }};

        when(service.getAll()).thenReturn(asList(tag1, tag2));

        assertThat(controller.tags().size(), is(2));
        assertThat(controller.tags(), hasItems(tag1, tag2));
    }
}