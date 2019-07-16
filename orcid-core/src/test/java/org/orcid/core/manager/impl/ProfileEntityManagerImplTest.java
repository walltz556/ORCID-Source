package org.orcid.core.manager.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javax.annotation.Resource;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.orcid.core.manager.AddressManager;
import org.orcid.core.manager.BiographyManager;
import org.orcid.core.manager.EmailManager;
import org.orcid.core.manager.ExternalIdentifierManager;
import org.orcid.core.manager.OtherNameManager;
import org.orcid.core.manager.ProfileEntityCacheManager;
import org.orcid.core.manager.ProfileEntityManager;
import org.orcid.core.manager.ProfileKeywordManager;
import org.orcid.core.manager.RecordNameManager;
import org.orcid.core.manager.ResearcherUrlManager;
import org.orcid.core.oauth.OrcidOauth2TokenDetailService;
import org.orcid.jaxb.model.common_v2.Locale;
import org.orcid.jaxb.model.common_v2.Visibility;
import org.orcid.jaxb.model.record_v2.Address;
import org.orcid.jaxb.model.record_v2.Keyword;
import org.orcid.jaxb.model.record_v2.OtherName;
import org.orcid.jaxb.model.record_v2.PersonExternalIdentifier;
import org.orcid.jaxb.model.record_v2.ResearcherUrl;
import org.orcid.persistence.dao.ProfileDao;
import org.orcid.persistence.dao.UserConnectionDao;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.orcid.pojo.ajaxForm.Checkbox;
import org.orcid.pojo.ajaxForm.Claim;
import org.orcid.pojo.ajaxForm.Text;
import org.orcid.test.DBUnitTest;
import org.orcid.test.OrcidJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Declan Newman (declan) Date: 10/02/2012
 */
@RunWith(OrcidJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:orcid-core-context.xml" })
public class ProfileEntityManagerImplTest extends DBUnitTest {
    @Resource
    private OrcidOauth2TokenDetailService orcidOauth2TokenDetailService;
    
    @Resource
    private ProfileEntityManager profileEntityManager;
    
    @Resource(name = "profileEntityCacheManager")
    private ProfileEntityCacheManager profileEntityCacheManager;
    
    @Resource
    private EmailManager emailManager;
    
    @Resource
    private RecordNameManager recordNameManager;
    
    @Resource
    private BiographyManager biographyManager;
    
    @Resource
    private UserConnectionDao userConnectionDao;
    
    @Resource
    private AddressManager addressManager;
    
    @Resource
    private ExternalIdentifierManager externalIdentifierManager;
    
    @Resource
    private OtherNameManager otherNamesManager;
    
    @Resource
    private ProfileKeywordManager profileKeywordManager;
    
    @Resource
    private ResearcherUrlManager researcherUrlManager;
    
    @BeforeClass
    public static void initDBUnitData() throws Exception {
        initDBUnitData(Arrays.asList("/data/SourceClientDetailsEntityData.xml", "/data/ProfileEntityData.xml", "/data/RecordNameEntityData.xml", "/data/BiographyEntityData.xml", "/data/ClientDetailsEntityData.xml"));
    }

    @AfterClass
    public static void removeDBUnitData() throws Exception {
        removeDBUnitData(Arrays.asList("/data/ClientDetailsEntityData.xml", "/data/RecordNameEntityData.xml", "/data/BiographyEntityData.xml", "/data/ProfileEntityData.xml", "/data/SourceClientDetailsEntityData.xml"));
    }

    @Test
    public void testFindByOrcid() throws Exception {
        String harrysOrcid = "4444-4444-4444-4444";
        ProfileEntity profileEntity = profileEntityCacheManager.retrieve(harrysOrcid);
        assertNotNull(profileEntity);
        assertEquals(harrysOrcid, profileEntity.getId());
    }

    @Test    
    public void testReviewProfile() throws Exception {
    	boolean result = profileEntityManager.reviewProfile("4444-4444-4444-4441");
        assertTrue(result);
    	
    	result = profileEntityManager.unreviewProfile("4444-4444-4444-4442");
    	assertTrue(result);
    }
    
    @Test  
    @Transactional
    public void testClaimChangingVisibility() {
        String orcid = "0000-0000-0000-0001";
        Claim claim = new Claim();
        claim.setActivitiesVisibilityDefault(org.orcid.pojo.ajaxForm.Visibility.valueOf(Visibility.PRIVATE));
        claim.setPassword(Text.valueOf("passwordTest1"));
        claim.setPasswordConfirm(Text.valueOf("passwordTest1"));
        Checkbox checked = new Checkbox();
        checked.setValue(true);
        claim.setSendChangeNotifications(checked);
        claim.setSendOrcidNews(checked);
        claim.setTermsOfUse(checked);
        
        assertTrue(profileEntityManager.claimProfileAndUpdatePreferences("0000-0000-0000-0001", "public_0000-0000-0000-0001@test.orcid.org", Locale.EN, claim));
        ProfileEntity profile = profileEntityManager.findByOrcid("0000-0000-0000-0001");
        assertNotNull(profile);
        assertEquals(org.orcid.jaxb.model.common_v2.Visibility.PRIVATE.name(), biographyManager.getBiography(orcid).getVisibility().name());        
        assertEquals(3, addressManager.getAddresses(orcid).getAddress().size());
        for(Address a : addressManager.getAddresses(orcid).getAddress()) {
            assertEquals(org.orcid.jaxb.model.common_v2.Visibility.PRIVATE.name(), a.getVisibility());
        }
        
        assertEquals(3, externalIdentifierManager.getExternalIdentifiers(orcid).getExternalIdentifiers().size());
        for(PersonExternalIdentifier e : externalIdentifierManager.getExternalIdentifiers(orcid).getExternalIdentifiers()) {
            assertEquals(org.orcid.jaxb.model.common_v2.Visibility.PRIVATE.name(), e.getVisibility());
        }
        
        assertEquals(3, profileKeywordManager.getKeywords(orcid).getKeywords().size());
        for(Keyword k : profileKeywordManager.getKeywords(orcid).getKeywords()) {
            assertEquals(org.orcid.jaxb.model.common_v2.Visibility.PRIVATE.name(), k.getVisibility());
        }
        
        assertEquals(3, otherNamesManager.getOtherNames(orcid).getOtherNames().size());
        for(OtherName o : otherNamesManager.getOtherNames(orcid).getOtherNames()) {
            assertEquals(org.orcid.jaxb.model.common_v2.Visibility.PRIVATE.name(), o.getVisibility());
        }
        
        assertEquals(3, researcherUrlManager.getResearcherUrls(orcid).getResearcherUrls().size());
        for(ResearcherUrl r : researcherUrlManager.getResearcherUrls(orcid).getResearcherUrls()) {
            assertEquals(org.orcid.jaxb.model.common_v2.Visibility.PRIVATE.name(), r.getVisibility());
        }        
    }
    
    public void testDisable2FA() {
        ProfileDao profileDao = Mockito.mock(ProfileDao.class);
        Mockito.doNothing().when(profileDao).disable2FA(Mockito.eq("some-orcid"));
        profileEntityManager.disable2FA("some-orcid");
        Mockito.verify(profileDao).disable2FA(Mockito.eq("some-orcid"));
    }
    
}
