package org.orcid.frontend.web.pagination;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.orcid.core.manager.v3.WorksCacheManager;
import org.orcid.core.manager.v3.read_only.WorkManagerReadOnly;
import org.orcid.jaxb.model.common.WorkType;
import org.orcid.jaxb.model.v3.release.common.CreatedDate;
import org.orcid.jaxb.model.v3.release.common.Day;
import org.orcid.jaxb.model.v3.release.common.FuzzyDate;
import org.orcid.jaxb.model.v3.release.common.LastModifiedDate;
import org.orcid.jaxb.model.v3.release.common.Month;
import org.orcid.jaxb.model.v3.release.common.PublicationDate;
import org.orcid.jaxb.model.v3.release.common.Source;
import org.orcid.jaxb.model.v3.release.common.SourceClientId;
import org.orcid.jaxb.model.v3.release.common.Subtitle;
import org.orcid.jaxb.model.v3.release.common.Title;
import org.orcid.jaxb.model.v3.release.common.Visibility;
import org.orcid.jaxb.model.v3.release.common.Year;
import org.orcid.jaxb.model.v3.release.record.WorkTitle;
import org.orcid.jaxb.model.v3.release.record.summary.WorkGroup;
import org.orcid.jaxb.model.v3.release.record.summary.WorkSummary;
import org.orcid.jaxb.model.v3.release.record.summary.Works;
import org.orcid.pojo.ajaxForm.WorkForm;
import org.orcid.utils.DateUtils;

public class WorksPaginatorTest {

    @Mock
    private WorksCacheManager worksCacheManager;
    
    @Mock
    private WorkManagerReadOnly workManagerReadOnly;

    @InjectMocks
    private WorksPaginator worksPaginator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetWorksPage() {
        Mockito.when(worksCacheManager.getGroupedWorks(Mockito.anyString())).thenReturn(get1000PublicWorkGroups());
        Page<org.orcid.pojo.grouping.WorkGroup> page = worksPaginator.getWorksPage("orcid", 0, false, WorksPaginator.DATE_SORT_KEY, true);
        assertEquals(WorksPaginator.PAGE_SIZE, page.getGroups().size());
        org.orcid.pojo.grouping.WorkGroup workGroupPage1 = page.getGroups().get(0);

        Page<org.orcid.pojo.grouping.WorkGroup> page2 = worksPaginator.getWorksPage("orcid", page.getNextOffset(), false, WorksPaginator.DATE_SORT_KEY, true);
        org.orcid.pojo.grouping.WorkGroup workGroupPage2 = page2.getGroups().get(0);

        assertFalse(workGroupPage1.getGroupId() == workGroupPage2.getGroupId());

        Page<org.orcid.pojo.grouping.WorkGroup> sortedByTitle = worksPaginator.getWorksPage("orcid", 0, false, WorksPaginator.TITLE_SORT_KEY, false);
        workGroupPage1 = sortedByTitle.getGroups().get(0);
        assertFalse(workGroupPage1.getGroupId() == workGroupPage2.getGroupId());

        Page<org.orcid.pojo.grouping.WorkGroup> sortedByTitlePage2 = worksPaginator.getWorksPage("orcid", sortedByTitle.getNextOffset(), false, WorksPaginator.TITLE_SORT_KEY, false);
        workGroupPage2 = sortedByTitlePage2.getGroups().get(0);
        assertFalse(workGroupPage1.getGroupId() == workGroupPage2.getGroupId());

        Page<org.orcid.pojo.grouping.WorkGroup> reversedSortedByTitle = worksPaginator.getWorksPage("orcid", 0, false, WorksPaginator.TITLE_SORT_KEY, true);
        workGroupPage1 = sortedByTitle.getGroups().get(0);
        assertFalse(workGroupPage1.getGroupId() == workGroupPage2.getGroupId());

        Page<org.orcid.pojo.grouping.WorkGroup> reversedSortedByTitlePage2 = worksPaginator.getWorksPage("orcid", reversedSortedByTitle.getNextOffset(), false, WorksPaginator.TITLE_SORT_KEY, true);
        workGroupPage2 = reversedSortedByTitlePage2.getGroups().get(0);
        assertFalse(workGroupPage1.getGroupId() == workGroupPage2.getGroupId());
    }

    @Test
    public void testGetPublicWorksPage() {
        Mockito.when(worksCacheManager.getGroupedWorks(Mockito.anyString())).thenReturn(getPageSizeOfMixedWorkGroups());
        Page<org.orcid.pojo.grouping.WorkGroup> page = worksPaginator.getWorksPage("orcid", 0, true, WorksPaginator.DATE_SORT_KEY, true);
        assertFalse(WorksPaginator.PAGE_SIZE == page.getGroups().size());
        assertTrue((WorksPaginator.PAGE_SIZE / 2) == page.getGroups().size());

        for (org.orcid.pojo.grouping.WorkGroup workGroup : page.getGroups()) {
            for (WorkForm workForm : workGroup.getWorks()) {
                assertEquals(workForm.getVisibility().getVisibility(), Visibility.PUBLIC);
            }
        }
    }
    
    @Test
    public void testTitleSortCaseInsensitive() {
        Works works = get1000PublicWorkGroups();
        for (WorkGroup workGroup : works.getWorkGroup()) {
            if (new Random().nextBoolean()) {
                for (WorkSummary summary : workGroup.getWorkSummary()) {
                    summary.getTitle().setTitle(new Title(summary.getTitle().getTitle().getContent().toUpperCase()));
                }
            }
        }
        Mockito.when(worksCacheManager.getGroupedWorks(Mockito.anyString())).thenReturn(works);
        Page<org.orcid.pojo.grouping.WorkGroup> page = worksPaginator.getWorksPage("orcid", 0, false, WorksPaginator.TITLE_SORT_KEY, true);

        org.orcid.pojo.grouping.WorkGroup previous = page.getGroups().remove(0);
        while (!page.getGroups().isEmpty()) {
            org.orcid.pojo.grouping.WorkGroup next = page.getGroups().remove(0);
            String previousTitle = previous.getWorks().get(0).getTitle().getValue();
            String nextTitle = next.getWorks().get(0).getTitle().getValue();
            assertTrue(previousTitle.toLowerCase().compareTo(nextTitle.toLowerCase()) <= 0);
            previous = next;
        }
    }

    @Test
    public void testReverseSecondaryTitleSortForNullDates() {
        Works works = getWorkGroupsWithNullDates();
        Mockito.when(worksCacheManager.getGroupedWorks(Mockito.anyString())).thenReturn(works);
        Page<org.orcid.pojo.grouping.WorkGroup> page = worksPaginator.getWorksPage("orcid", 0, false, WorksPaginator.DATE_SORT_KEY, true);

        org.orcid.pojo.grouping.WorkGroup previous = page.getGroups().remove(0);
        while (!page.getGroups().isEmpty()) {
            org.orcid.pojo.grouping.WorkGroup next = page.getGroups().remove(0);
            String previousTitle = previous.getWorks().get(0).getTitle().getValue();
            String nextTitle = next.getWorks().get(0).getTitle().getValue();
            assertTrue(previousTitle.toLowerCase().compareTo(nextTitle.toLowerCase()) >= 0);
            previous = next;
        }
    }
    
    @Test
    public void testGetAllWorks() {
        Works works = get1000PublicWorkGroups();
        Mockito.when(worksCacheManager.getGroupedWorks(Mockito.anyString())).thenReturn(works);
        Page<org.orcid.pojo.grouping.WorkGroup> page = worksPaginator.getAllWorks("orcid", false, WorksPaginator.TITLE_SORT_KEY, true);
        assertEquals(1000, page.getTotalGroups());
        assertEquals(1000, page.getGroups().size());
    }
    
    @Test
    public void testGetAllPublic() {
        Mockito.when(worksCacheManager.getGroupedWorks(Mockito.anyString())).thenReturn(getPageSizeOfMixedWorkGroups());
        Page<org.orcid.pojo.grouping.WorkGroup> page = worksPaginator.getAllWorks("orcid", true, WorksPaginator.DATE_SORT_KEY, true);
        assertFalse(WorksPaginator.PAGE_SIZE == page.getGroups().size());
        assertTrue((WorksPaginator.PAGE_SIZE / 2) == page.getGroups().size());

        for (org.orcid.pojo.grouping.WorkGroup workGroup : page.getGroups()) {
            for (WorkForm workForm : workGroup.getWorks()) {
                assertEquals(workForm.getVisibility().getVisibility(), Visibility.PUBLIC);
            }
        }
    }
    
    /**
     * Check null titles don't cause errors
     */
    @Test
    public void testGetWorkWithNulltitle() {
        WorkGroup workGroup = getPublicWorkGroup(0);
        for (WorkSummary workSummary : workGroup.getWorkSummary()) {
            workSummary.setTitle(null);
        }
        Works works = new Works();
        works.getWorkGroup().add(workGroup);
        
        Mockito.when(worksCacheManager.getGroupedWorks(Mockito.anyString())).thenReturn(works);
        Page<org.orcid.pojo.grouping.WorkGroup> page = worksPaginator.getAllWorks("orcid", false, WorksPaginator.TITLE_SORT_KEY, true);
        
        for (org.orcid.pojo.grouping.WorkGroup group : page.getGroups()) {
            for (WorkForm work : group.getWorks()) {
                assertEquals("", work.getTitle().getValue());
            }
        }
    }

    private Works getWorkGroupsWithNullDates() {
        Works works = get1000PublicWorkGroups();
        for (WorkGroup workGroup : works.getWorkGroup()) {
            for (WorkSummary workSummary : workGroup.getWorkSummary()) {
                workSummary.setPublicationDate(null);
            }
        }
        return works;
    }

    private Works get1000PublicWorkGroups() {
        Works works = new Works();
        works.setLastModifiedDate(new LastModifiedDate(DateUtils.convertToXMLGregorianCalendar(System.currentTimeMillis())));
        works.setPath("some path");

        for (int i = 0; i < 1000; i++) {
            works.getWorkGroup().add(getPublicWorkGroup(i));
        }
        return works;
    }

    private Works getPageSizeOfMixedWorkGroups() {
        Works works = new Works();
        works.setLastModifiedDate(new LastModifiedDate(DateUtils.convertToXMLGregorianCalendar(System.currentTimeMillis())));
        works.setPath("some path");

        for (int i = 0; i < WorksPaginator.PAGE_SIZE; i++) {
            works.getWorkGroup().add(getMixedWorkGroup(i));
        }
        return works;
    }
    
    private Works getPageSizeOfPublicWorkGroups() {
        Works works = new Works();
        works.setLastModifiedDate(new LastModifiedDate(DateUtils.convertToXMLGregorianCalendar(System.currentTimeMillis())));
        works.setPath("some path");

        for (int i = 0; i < WorksPaginator.PAGE_SIZE; i++) {
            works.getWorkGroup().add(getPublicWorkGroup(i));
        }
        return works;
    }
    
    private Works getFiveLimitedWorkGroups() {
        Works works = new Works();
        works.setLastModifiedDate(new LastModifiedDate(DateUtils.convertToXMLGregorianCalendar(System.currentTimeMillis())));
        works.setPath("some path");

        for (int i = 0; i < 5; i++) {
            works.getWorkGroup().add(getLimitedWorkGroup(i));
        }
        return works;
    }
    
    private WorkGroup getLimitedWorkGroup(int i) {
        WorkGroup workGroup = new WorkGroup();
        workGroup.setLastModifiedDate(new LastModifiedDate(DateUtils.convertToXMLGregorianCalendar(System.currentTimeMillis())));
        for (int x = 0; x < 10; x++) {
            WorkSummary workSummary = new WorkSummary();
            workSummary.setCreatedDate(new CreatedDate(DateUtils.convertToXMLGregorianCalendar(System.currentTimeMillis())));
            workSummary.setTitle(getTitle(i));
            workSummary.setVisibility(Visibility.LIMITED);
            workSummary.setDisplayIndex(Integer.toString(x));
            workSummary.setPutCode(Long.valueOf(new StringBuilder(i).append(x).toString()));
            workSummary.setSource(getSource());
            workSummary.setType(WorkType.EDITED_BOOK);
            workGroup.getWorkSummary().add(workSummary);
        }
        return workGroup;
    }

    private WorkGroup getMixedWorkGroup(int i) {
        WorkGroup workGroup = new WorkGroup();
        workGroup.setLastModifiedDate(new LastModifiedDate(DateUtils.convertToXMLGregorianCalendar(System.currentTimeMillis())));
        for (int x = 0; x < 10; x++) {
            WorkSummary workSummary = new WorkSummary();
            workSummary.setCreatedDate(new CreatedDate(DateUtils.convertToXMLGregorianCalendar(System.currentTimeMillis())));
            workSummary.setTitle(getTitle(i));
            workSummary.setVisibility(i % 2 == 0 ? Visibility.PUBLIC : Visibility.PRIVATE);
            workSummary.setDisplayIndex(Integer.toString(x));
            workSummary.setPutCode(Long.valueOf(new StringBuilder(i).append(x).toString()));
            workSummary.setSource(getSource());
            workSummary.setType(WorkType.EDITED_BOOK);
            workGroup.getWorkSummary().add(workSummary);
        }
        return workGroup;
    }

    private WorkGroup getPublicWorkGroup(int i) {
        WorkGroup workGroup = new WorkGroup();
        workGroup.setLastModifiedDate(new LastModifiedDate(DateUtils.convertToXMLGregorianCalendar(System.currentTimeMillis())));
        for (int x = 0; x < 10; x++) {
            WorkSummary workSummary = new WorkSummary();
            workSummary.setCreatedDate(new CreatedDate(DateUtils.convertToXMLGregorianCalendar(System.currentTimeMillis())));
            workSummary.setPublicationDate(new PublicationDate(new FuzzyDate(new Year(2017), new Month(x), new Day(x))));
            workSummary.setTitle(getTitle(i));
            workSummary.setVisibility(Visibility.PUBLIC);
            workSummary.setDisplayIndex(Integer.toString(x));
            workSummary.setPutCode(Long.valueOf(new StringBuilder(i).append(x).toString()));
            workSummary.setSource(getSource());
            workSummary.setType(WorkType.EDITED_BOOK);
            workGroup.getWorkSummary().add(workSummary);
        }
        return workGroup;
    }

    private WorkTitle getTitle(int i) {
        WorkTitle title = new WorkTitle();
        title.setTitle(new Title(UUID.randomUUID().toString()));
        title.setSubtitle(new Subtitle(UUID.randomUUID().toString()));
        return title;
    }

    private Source getSource() {
        Source source = new Source();
        SourceClientId clientId = new SourceClientId();
        clientId.setPath("APP-5555-5555-5555-5555");
        source.setSourceClientId(clientId);
        return source;
    }

}
